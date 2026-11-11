package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CommentEntity
import com.example.data.local.PostEntity
import com.example.data.local.StorageHelper
import com.example.data.model.*
import com.example.data.repository.SocialRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FacebookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SocialRepository

    val posts: StateFlow<List<PostEntity>>
    val reels: StateFlow<List<PostEntity>>
    val stories: StateFlow<List<Story>>
    val currentUser: StateFlow<UserProfile>
    val friends: StateFlow<List<FriendItem>>
    val notifications: StateFlow<List<NotificationItem>>

    // Selected post for comment modal
    private val _commentingPostId = MutableStateFlow<String?>(null)
    val commentingPostId: StateFlow<String?> = _commentingPostId.asStateFlow()

    // Fullscreen media viewer
    private val _fullScreenMedia = MutableStateFlow<Pair<String, MediaType>?>(null) // (url, type)
    val fullScreenMedia: StateFlow<Pair<String, MediaType>?> = _fullScreenMedia.asStateFlow()

    // Active story viewer
    private val _activeStory = MutableStateFlow<Story?>(null)
    val activeStory: StateFlow<Story?> = _activeStory.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        val database = AppDatabase.getInstance(application)
        repository = SocialRepository(database.postDao())

        posts = repository.allPosts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        reels = repository.allReels.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        stories = repository.stories
        currentUser = repository.currentUser
        friends = repository.friends
        notifications = repository.notifications

        viewModelScope.launch {
            repository.initialize()
        }
    }

    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>> {
        return repository.getCommentsForPost(postId)
    }

    fun openComments(postId: String) {
        _commentingPostId.value = postId
    }

    fun closeComments() {
        _commentingPostId.value = null
    }

    fun openFullScreenMedia(url: String, type: MediaType) {
        _fullScreenMedia.value = Pair(url, type)
    }

    fun closeFullScreenMedia() {
        _fullScreenMedia.value = null
    }

    fun openStory(story: Story) {
        _activeStory.value = story
    }

    fun closeStory() {
        _activeStory.value = null
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun reactToPost(postId: String, reaction: ReactionType?) {
        viewModelScope.launch {
            repository.reactToPost(postId, reaction)
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
        }
    }

    fun addComment(postId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.addComment(postId, content)
        }
    }

    fun toggleCommentLike(commentId: String, currentLiked: Boolean) {
        viewModelScope.launch {
            repository.toggleCommentLike(commentId, currentLiked)
        }
    }

    /**
     * Creates a new post with optional photos and videos.
     * Copies picked device URIs to persistent app storage.
     */
    fun createPost(
        content: String,
        mediaUris: List<Uri>,
        mediaType: MediaType,
        feeling: String?,
        location: String?,
        privacy: Privacy,
        isReel: Boolean = false,
        onFinished: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val persistentUrls = mediaUris.map { uri ->
                StorageHelper.saveMediaToInternalStorage(context, uri, mediaType == MediaType.VIDEO)
            }
            repository.createPost(
                content = content,
                mediaUrls = persistentUrls,
                mediaType = mediaType,
                feeling = feeling,
                location = location,
                privacy = privacy,
                isReel = isReel
            )
            onFinished()
        }
    }

    /**
     * For sample/direct URL posting (e.g. preset photos or web videos)
     */
    fun createPostWithUrls(
        content: String,
        mediaUrls: List<String>,
        mediaType: MediaType,
        feeling: String?,
        location: String?,
        privacy: Privacy,
        isReel: Boolean = false
    ) {
        viewModelScope.launch {
            repository.createPost(
                content = content,
                mediaUrls = mediaUrls,
                mediaType = mediaType,
                feeling = feeling,
                location = location,
                privacy = privacy,
                isReel = isReel
            )
        }
    }

    fun createStory(mediaUri: Uri, mediaType: MediaType, caption: String) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val persistentUrl = StorageHelper.saveMediaToInternalStorage(context, mediaUri, mediaType == MediaType.VIDEO)
            repository.addStory(persistentUrl, mediaType, caption)
        }
    }

    fun confirmFriendRequest(friendId: String) {
        repository.confirmFriendRequest(friendId)
    }

    fun removeFriend(friendId: String) {
        repository.removeOrCancelFriend(friendId)
    }

    fun addFriend(friendId: String) {
        repository.addFriend(friendId)
    }

    fun markAllNotificationsRead() {
        repository.markAllNotificationsAsRead()
    }

    fun updateProfile(name: String, bio: String, avatarUrl: String, location: String) {
        repository.updateProfile(name, bio, avatarUrl, location)
    }
}
