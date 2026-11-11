package com.example.data.repository

import com.example.data.local.CommentEntity
import com.example.data.local.PostDao
import com.example.data.local.PostEntity
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class SocialRepository(private val postDao: PostDao) {

    val allPosts: Flow<List<PostEntity>> = postDao.getAllPosts()
    val allReels: Flow<List<PostEntity>> = postDao.getReels()

    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "user_me",
            name = "Alex Morgan",
            username = "alex.morgan",
            bio = "Photography lover 📸 | Tech explorer 💻 | World traveler 🌍",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
            coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200&auto=format&fit=crop&q=80",
            location = "San Francisco, California",
            workplace = "Product Designer at TechFlow",
            joinedDate = "Joined October 2018",
            friendsCount = 842,
            followersCount = 1250
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _friends = MutableStateFlow<List<FriendItem>>(emptyList())
    val friends: StateFlow<List<FriendItem>> = _friends.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    suspend fun initialize() {
        if (postDao.getPostCount() == 0) {
            seedSampleData()
        }
        seedStoriesAndFriends()
    }

    suspend fun createPost(
        content: String,
        mediaUrls: List<String>,
        mediaType: MediaType,
        feeling: String? = null,
        location: String? = null,
        privacy: Privacy = Privacy.PUBLIC,
        isReel: Boolean = false,
        audioTitle: String? = null
    ) {
        val user = _currentUser.value
        val newPost = PostEntity(
            id = UUID.randomUUID().toString(),
            authorId = user.id,
            authorName = user.name,
            authorAvatarUrl = user.avatarUrl,
            timestamp = System.currentTimeMillis(),
            content = content,
            mediaType = mediaType.name,
            mediaUrls = mediaUrls.joinToString(","),
            feeling = feeling,
            location = location,
            privacy = privacy.name,
            likeCount = 0,
            loveCount = 0,
            hahaCount = 0,
            wowCount = 0,
            sadCount = 0,
            angryCount = 0,
            userReaction = null,
            commentCount = 0,
            shareCount = 0,
            isReel = isReel,
            audioTitle = audioTitle ?: if (isReel) "Original audio • ${user.name}" else null
        )
        postDao.insertPost(newPost)
    }

    suspend fun reactToPost(postId: String, reaction: ReactionType?) {
        val post = postDao.getPostById(postId) ?: return
        val currentReaction = ReactionType.fromString(post.userReaction)

        var newLike = post.likeCount
        var newLove = post.loveCount
        var newHaha = post.hahaCount
        var newWow = post.wowCount
        var newSad = post.sadCount
        var newAngry = post.angryCount

        // Decrement old reaction if exists
        when (currentReaction) {
            ReactionType.LIKE -> newLike = (newLike - 1).coerceAtLeast(0)
            ReactionType.LOVE -> newLove = (newLove - 1).coerceAtLeast(0)
            ReactionType.CARE -> newLove = (newLove - 1).coerceAtLeast(0)
            ReactionType.HAHA -> newHaha = (newHaha - 1).coerceAtLeast(0)
            ReactionType.WOW -> newWow = (newWow - 1).coerceAtLeast(0)
            ReactionType.SAD -> newSad = (newSad - 1).coerceAtLeast(0)
            ReactionType.ANGRY -> newAngry = (newAngry - 1).coerceAtLeast(0)
            null -> {}
        }

        // Apply new reaction if not un-reacting
        if (reaction != null && reaction != currentReaction) {
            when (reaction) {
                ReactionType.LIKE -> newLike += 1
                ReactionType.LOVE, ReactionType.CARE -> newLove += 1
                ReactionType.HAHA -> newHaha += 1
                ReactionType.WOW -> newWow += 1
                ReactionType.SAD -> newSad += 1
                ReactionType.ANGRY -> newAngry += 1
            }
        }

        val updatedReactionName = if (reaction == currentReaction) null else reaction?.name

        postDao.updatePost(
            post.copy(
                userReaction = updatedReactionName,
                likeCount = newLike,
                loveCount = newLove,
                hahaCount = newHaha,
                wowCount = newWow,
                sadCount = newSad,
                angryCount = newAngry
            )
        )
    }

    suspend fun deletePost(postId: String) {
        postDao.deletePost(postId)
    }

    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>> {
        return postDao.getCommentsForPost(postId)
    }

    suspend fun addComment(postId: String, content: String) {
        val user = _currentUser.value
        val comment = CommentEntity(
            id = UUID.randomUUID().toString(),
            postId = postId,
            authorName = user.name,
            authorAvatarUrl = user.avatarUrl,
            content = content,
            timestamp = System.currentTimeMillis(),
            likesCount = 0,
            isLiked = false
        )
        postDao.insertComment(comment)

        val post = postDao.getPostById(postId)
        if (post != null) {
            postDao.updatePost(post.copy(commentCount = post.commentCount + 1))
        }
    }

    suspend fun toggleCommentLike(commentId: String, currentLiked: Boolean) {
        val delta = if (currentLiked) -1 else 1
        postDao.updateCommentLike(commentId, !currentLiked, delta)
    }

    fun addStory(mediaUrl: String, mediaType: MediaType, caption: String) {
        val user = _currentUser.value
        val newStory = Story(
            id = UUID.randomUUID().toString(),
            authorId = user.id,
            authorName = user.name,
            authorAvatarUrl = user.avatarUrl,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            timestamp = System.currentTimeMillis(),
            isSeen = false,
            caption = caption
        )
        _stories.value = listOf(newStory) + _stories.value
    }

    fun confirmFriendRequest(friendId: String) {
        _friends.value = _friends.value.map {
            if (it.id == friendId) it.copy(isFriend = true, hasPendingRequest = false) else it
        }
    }

    fun removeOrCancelFriend(friendId: String) {
        _friends.value = _friends.value.filter { it.id != friendId }
    }

    fun addFriend(friendId: String) {
        _friends.value = _friends.value.map {
            if (it.id == friendId) it.copy(hasPendingRequest = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isUnread = false) }
    }

    fun updateProfile(name: String, bio: String, avatarUrl: String, location: String) {
        _currentUser.value = _currentUser.value.copy(
            name = name.ifBlank { _currentUser.value.name },
            bio = bio,
            avatarUrl = avatarUrl.ifBlank { _currentUser.value.avatarUrl },
            location = location
        )
    }

    private suspend fun seedSampleData() {
        val samplePosts = listOf(
            // Post 1: Video Post (Nature stream MP4)
            PostEntity(
                id = "post_video_1",
                authorId = "user_sophia",
                authorName = "Sophia Bennett",
                authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
                timestamp = System.currentTimeMillis() - (1000 * 60 * 45), // 45 mins ago
                content = "Golden hour by the coast! 🌊 Sunset vibes and ocean breeze. What do you think of this view?",
                mediaType = MediaType.VIDEO.name,
                // Public domain high-availability sample MP4 video
                mediaUrls = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                feeling = "🌊 Feeling peaceful",
                location = "Big Sur, California",
                privacy = Privacy.PUBLIC.name,
                likeCount = 142,
                loveCount = 89,
                hahaCount = 2,
                wowCount = 34,
                sadCount = 0,
                angryCount = 0,
                userReaction = null,
                commentCount = 19,
                shareCount = 14,
                isReel = false,
                audioTitle = "Ocean Breeze Sounds • Relaxing Beats"
            ),
            // Post 2: Multi-Photo Post
            PostEntity(
                id = "post_photo_1",
                authorId = "user_marcus",
                authorName = "Marcus Thorne",
                authorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80",
                timestamp = System.currentTimeMillis() - (1000 * 60 * 120), // 2 hrs ago
                content = "Weekend hiking trail conquered with the squad! 🏔️ Fresh air, breathtaking views, and 12 miles later. Can't wait for the next climb! 🥾",
                mediaType = MediaType.PHOTO.name,
                mediaUrls = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1000&auto=format&fit=crop&q=80,https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1000&auto=format&fit=crop&q=80,https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1000&auto=format&fit=crop&q=80",
                feeling = "🏔️ Feeling adventurous",
                location = "Yosemite National Park",
                privacy = Privacy.PUBLIC.name,
                likeCount = 284,
                loveCount = 120,
                hahaCount = 0,
                wowCount = 45,
                sadCount = 0,
                angryCount = 0,
                userReaction = "LIKE",
                commentCount = 42,
                shareCount = 9,
                isReel = false
            ),
            // Post 3: Reel Video (Vertical Video)
            PostEntity(
                id = "post_reel_1",
                authorId = "user_chloe",
                authorName = "Chloe Rivera",
                authorAvatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80",
                timestamp = System.currentTimeMillis() - (1000 * 60 * 240),
                content = "Check out this quick timelapse art session! 🎨 Never give up on your passions.",
                mediaType = MediaType.VIDEO.name,
                mediaUrls = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                feeling = "🎨 Feeling inspired",
                location = "Brooklyn, NY",
                privacy = Privacy.PUBLIC.name,
                likeCount = 530,
                loveCount = 310,
                hahaCount = 12,
                wowCount = 88,
                sadCount = 0,
                angryCount = 1,
                userReaction = null,
                commentCount = 64,
                shareCount = 38,
                isReel = true,
                audioTitle = "Artistic Flow • Chloe Rivera"
            ),
            // Post 4: Tech & Coffee Photo Post
            PostEntity(
                id = "post_photo_2",
                authorId = "user_david",
                authorName = "David Kim",
                authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
                timestamp = System.currentTimeMillis() - (1000 * 60 * 360),
                content = "New workspace setup complete! 🖥️ Minimalist desk, mechanical keyboard, and a hot cup of espresso. Ready to ship some code today 🚀 #devlife #minimalism",
                mediaType = MediaType.PHOTO.name,
                mediaUrls = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1000&auto=format&fit=crop&q=80",
                feeling = "☕ Drinking Coffee",
                location = "Seattle, WA",
                privacy = Privacy.PUBLIC.name,
                likeCount = 95,
                loveCount = 40,
                hahaCount = 3,
                wowCount = 15,
                sadCount = 0,
                angryCount = 0,
                userReaction = null,
                commentCount = 11,
                shareCount = 3,
                isReel = false
            ),
            // Post 5: Reel 2 (Big Buck Bunny clip)
            PostEntity(
                id = "post_reel_2",
                authorId = "user_animation",
                authorName = "Studio Pixel Craft",
                authorAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80",
                timestamp = System.currentTimeMillis() - (1000 * 60 * 500),
                content = "3D character animation test in open source blender 🐰 Smooth renders & physics!",
                mediaType = MediaType.VIDEO.name,
                mediaUrls = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                feeling = "✨ Feeling excited",
                location = "Amsterdam, Netherlands",
                privacy = Privacy.PUBLIC.name,
                likeCount = 890,
                loveCount = 420,
                hahaCount = 65,
                wowCount = 110,
                sadCount = 0,
                angryCount = 0,
                userReaction = "LOVE",
                commentCount = 95,
                shareCount = 62,
                isReel = true,
                audioTitle = "Bunny Theme Orchestra • Blender Foundation"
            )
        )
        postDao.insertPosts(samplePosts)

        // Seed some starter comments
        val sampleComments = listOf(
            CommentEntity(
                id = "c1",
                postId = "post_video_1",
                authorName = "Emma Watson",
                authorAvatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&auto=format&fit=crop&q=80",
                content = "Stunning footage Sophia! What camera did you record this with?",
                timestamp = System.currentTimeMillis() - (1000 * 60 * 30),
                likesCount = 8,
                isLiked = true
            ),
            CommentEntity(
                id = "c2",
                postId = "post_video_1",
                authorName = "Sophia Bennett",
                authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
                content = "Thanks Emma! Shot on my phone 4K 60fps! 📱",
                timestamp = System.currentTimeMillis() - (1000 * 60 * 20),
                likesCount = 5,
                isLiked = false
            ),
            CommentEntity(
                id = "c3",
                postId = "post_photo_1",
                authorName = "Lucas Garcia",
                authorAvatarUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=400&auto=format&fit=crop&q=80",
                content = "Legendary trail! Wish I could have joined! Next time for sure.",
                timestamp = System.currentTimeMillis() - (1000 * 60 * 90),
                likesCount = 12,
                isLiked = false
            )
        )
        sampleComments.forEach { postDao.insertComment(it) }
    }

    private fun seedStoriesAndFriends() {
        _stories.value = listOf(
            Story(
                id = "story_1",
                authorId = "user_sophia",
                authorName = "Sophia Bennett",
                authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
                mediaUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop&q=80",
                mediaType = MediaType.PHOTO,
                timestamp = System.currentTimeMillis() - (1000 * 60 * 45),
                isSeen = false,
                caption = "Morning surf session! 🏄‍♀️"
            ),
            Story(
                id = "story_2",
                authorId = "user_marcus",
                authorName = "Marcus Thorne",
                authorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80",
                mediaUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800&auto=format&fit=crop&q=80",
                mediaType = MediaType.PHOTO,
                timestamp = System.currentTimeMillis() - (1000 * 60 * 110),
                isSeen = false,
                caption = "Campfire under the stars ✨"
            ),
            Story(
                id = "story_3",
                authorId = "user_chloe",
                authorName = "Chloe Rivera",
                authorAvatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80",
                mediaUrl = "https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=800&auto=format&fit=crop&q=80",
                mediaType = MediaType.PHOTO,
                timestamp = System.currentTimeMillis() - (1000 * 60 * 180),
                isSeen = true,
                caption = "New acrylic canvas in progress 🖌️"
            ),
            Story(
                id = "story_4",
                authorId = "user_david",
                authorName = "David Kim",
                authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
                mediaUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800&auto=format&fit=crop&q=80",
                mediaType = MediaType.PHOTO,
                timestamp = System.currentTimeMillis() - (1000 * 60 * 240),
                isSeen = true,
                caption = "Best pour-over in town ☕"
            )
        )

        _friends.value = listOf(
            FriendItem("f1", "Liam Johnson", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&auto=format&fit=crop&q=80", 18, isFriend = false, hasPendingRequest = true, isOnline = true),
            FriendItem("f2", "Olivia Martinez", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&auto=format&fit=crop&q=80", 7, isFriend = false, hasPendingRequest = true, isOnline = false),
            FriendItem("f3", "Sophia Bennett", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80", 34, isFriend = true, hasPendingRequest = false, isOnline = true),
            FriendItem("f4", "Marcus Thorne", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80", 52, isFriend = true, hasPendingRequest = false, isOnline = true),
            FriendItem("f5", "Chloe Rivera", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80", 12, isFriend = true, hasPendingRequest = false, isOnline = false),
            FriendItem("f6", "Ethan Brown", "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=400&auto=format&fit=crop&q=80", 4, isFriend = false, hasPendingRequest = false, isOnline = true),
            FriendItem("f7", "Aria Patel", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80", 9, isFriend = false, hasPendingRequest = false, isOnline = true)
        )

        _notifications.value = listOf(
            NotificationItem("n1", "Sophia Bennett", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80", "reacted ❤️ to your post", System.currentTimeMillis() - (1000 * 60 * 15), isUnread = true, targetType = "post"),
            NotificationItem("n2", "Liam Johnson", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&auto=format&fit=crop&q=80", "sent you a friend request", System.currentTimeMillis() - (1000 * 60 * 45), isUnread = true, targetType = "friend"),
            NotificationItem("n3", "Emma Watson", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&auto=format&fit=crop&q=80", "commented: 'Stunning footage!'", System.currentTimeMillis() - (1000 * 60 * 90), isUnread = false, targetType = "post"),
            NotificationItem("n4", "Marcus Thorne", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80", "shared a new video in Facebook Watch", System.currentTimeMillis() - (1000 * 60 * 200), isUnread = false, targetType = "video"),
            NotificationItem("n5", "Chloe Rivera", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80", "added to her story", System.currentTimeMillis() - (1000 * 60 * 300), isUnread = false, targetType = "story")
        )
    }
}
