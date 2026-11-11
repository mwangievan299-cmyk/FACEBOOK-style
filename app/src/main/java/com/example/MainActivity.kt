package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MediaType
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FacebookViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FacebookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                FacebookApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FacebookApp(viewModel: FacebookViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(FacebookTab.FEED) }

    // State from ViewModel
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val reels by viewModel.reels.collectAsStateWithLifecycle()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val friends by viewModel.friends.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val commentingPostId by viewModel.commentingPostId.collectAsStateWithLifecycle()
    val fullScreenMedia by viewModel.fullScreenMedia.collectAsStateWithLifecycle()
    val activeStory by viewModel.activeStory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    // Dialog & overlay states
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showMessengerDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }

    // Launcher for Story Photo/Video creation
    val storyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val isVideo = uri.toString().contains("video", ignoreCase = true)
            viewModel.createStory(
                mediaUri = uri,
                mediaType = if (isVideo) MediaType.VIDEO else MediaType.PHOTO,
                caption = "Added via Facebook Story"
            )
            Toast.makeText(context, "Story published! ✨", Toast.LENGTH_SHORT).show()
        }
    }

    // Predictive / Custom Back Handling
    BackHandler(
        enabled = showCreatePostDialog || showMessengerDialog || showSearchDialog ||
                commentingPostId != null || fullScreenMedia != null || activeStory != null ||
                currentTab != FacebookTab.FEED
    ) {
        when {
            fullScreenMedia != null -> viewModel.closeFullScreenMedia()
            activeStory != null -> viewModel.closeStory()
            commentingPostId != null -> viewModel.closeComments()
            showCreatePostDialog -> showCreatePostDialog = false
            showMessengerDialog -> showMessengerDialog = false
            showSearchDialog -> showSearchDialog = false
            currentTab != FacebookTab.FEED -> currentTab = FacebookTab.FEED
        }
    }

    val unreadNotificationsCount = remember(notifications) {
        notifications.count { it.isUnread }
    }
    val pendingFriendsCount = remember(friends) {
        friends.count { it.hasPendingRequest }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            FacebookTopBar(
                onSearchClick = { showSearchDialog = true },
                onCreateClick = { showCreatePostDialog = true },
                onMessengerClick = { showMessengerDialog = true },
                unreadMessagesCount = 3,
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            FacebookBottomNav(
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    if (tab == currentTab && tab == FacebookTab.FEED) {
                        viewModel.updateSearchQuery("")
                    }
                    currentTab = tab
                },
                userAvatarUrl = currentUser.avatarUrl,
                unreadNotificationsCount = unreadNotificationsCount,
                pendingFriendRequestsCount = pendingFriendsCount
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                FacebookTab.FEED -> {
                    FeedScreen(
                        posts = posts,
                        stories = stories,
                        currentUser = currentUser,
                        searchQuery = searchQuery,
                        onCreatePostClick = { showCreatePostDialog = true },
                        onCreateStoryClick = {
                            storyPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        onStoryClick = { story -> viewModel.openStory(story) },
                        onReaction = { postId, reaction -> viewModel.reactToPost(postId, reaction) },
                        onCommentClick = { postId -> viewModel.openComments(postId) },
                        onShareClick = { postId ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Check out this post on Facebook!")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share post"))
                        },
                        onDeletePost = { postId -> viewModel.deletePost(postId) },
                        onMediaClick = { url, type -> viewModel.openFullScreenMedia(url, type) }
                    )
                }

                FacebookTab.WATCH -> {
                    WatchVideosScreen(
                        reelsAndVideos = posts,
                        currentUser = currentUser,
                        onCreateVideoClick = { showCreatePostDialog = true },
                        onReaction = { postId, reaction -> viewModel.reactToPost(postId, reaction) },
                        onCommentClick = { postId -> viewModel.openComments(postId) },
                        onShareClick = { postId ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Check out this video on Facebook Watch!")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share video"))
                        },
                        onDeletePost = { postId -> viewModel.deletePost(postId) },
                        onMediaClick = { url, type -> viewModel.openFullScreenMedia(url, type) }
                    )
                }

                FacebookTab.FRIENDS -> {
                    FriendsScreen(
                        friends = friends,
                        onConfirmRequest = { friendId -> viewModel.confirmFriendRequest(friendId) },
                        onDeleteRequest = { friendId -> viewModel.removeFriend(friendId) },
                        onAddFriend = { friendId -> viewModel.addFriend(friendId) }
                    )
                }

                FacebookTab.NOTIFICATIONS -> {
                    NotificationsScreen(
                        notifications = notifications,
                        onMarkAllRead = { viewModel.markAllNotificationsRead() }
                    )
                }

                FacebookTab.PROFILE -> {
                    ProfileScreen(
                        user = currentUser,
                        allPosts = posts,
                        onUpdateProfile = { name, bio, avatar, location ->
                            viewModel.updateProfile(name, bio, avatar, location)
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                        },
                        onCreatePostClick = { showCreatePostDialog = true },
                        onReaction = { postId, reaction -> viewModel.reactToPost(postId, reaction) },
                        onCommentClick = { postId -> viewModel.openComments(postId) },
                        onShareClick = { postId ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Check out this post on Facebook!")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share"))
                        },
                        onDeletePost = { postId -> viewModel.deletePost(postId) },
                        onMediaClick = { url, type -> viewModel.openFullScreenMedia(url, type) }
                    )
                }
            }
        }
    }

    // Modal 1: Create Post Dialog (Supports photo and video posting)
    if (showCreatePostDialog) {
        CreatePostDialog(
            user = currentUser,
            onDismiss = { showCreatePostDialog = false },
            onPostCreated = { content, mediaUris, directMediaUrls, mediaType, feeling, location, privacy, isReel ->
                if (mediaUris.isNotEmpty()) {
                    viewModel.createPost(
                        content = content,
                        mediaUris = mediaUris,
                        mediaType = mediaType,
                        feeling = feeling,
                        location = location,
                        privacy = privacy,
                        isReel = isReel
                    ) {
                        Toast.makeText(context, if (isReel) "Reel shared to Facebook! 🎬" else "Post published! 🚀", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    viewModel.createPostWithUrls(
                        content = content,
                        mediaUrls = directMediaUrls,
                        mediaType = mediaType,
                        feeling = feeling,
                        location = location,
                        privacy = privacy,
                        isReel = isReel
                    )
                    Toast.makeText(context, if (isReel) "Reel shared to Facebook! 🎬" else "Post published! 🚀", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Modal 2: Comments Bottom Sheet
    if (commentingPostId != null) {
        val commentsFlow = remember(commentingPostId) {
            viewModel.getCommentsForPost(commentingPostId!!)
        }
        val postComments by commentsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

        CommentsBottomSheet(
            postId = commentingPostId!!,
            comments = postComments,
            currentUser = currentUser,
            onDismiss = { viewModel.closeComments() },
            onSendComment = { text -> viewModel.addComment(commentingPostId!!, text) },
            onToggleLike = { commentId, currentLiked -> viewModel.toggleCommentLike(commentId, currentLiked) }
        )
    }

    // Modal 3: Fullscreen Media Viewer
    if (fullScreenMedia != null) {
        val (mediaUrl, mediaType) = fullScreenMedia!!
        FullScreenMediaViewer(
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            onDismiss = { viewModel.closeFullScreenMedia() }
        )
    }

    // Modal 4: Fullscreen Story Viewer
    if (activeStory != null) {
        StoryViewerDialog(
            story = activeStory!!,
            onDismiss = { viewModel.closeStory() },
            onNextStory = {
                val currentIndex = stories.indexOfFirst { it.id == activeStory!!.id }
                if (currentIndex >= 0 && currentIndex < stories.size - 1) {
                    viewModel.openStory(stories[currentIndex + 1])
                } else {
                    viewModel.closeStory()
                }
            }
        )
    }

    // Modal 5: Messenger Dialog
    if (showMessengerDialog) {
        MessengerDialog(
            friends = friends,
            onDismiss = { showMessengerDialog = false }
        )
    }

    // Modal 6: Search Dialog
    if (showSearchDialog) {
        SearchDialog(
            initialQuery = searchQuery,
            onQueryChanged = { query ->
                viewModel.updateSearchQuery(query)
                currentTab = FacebookTab.FEED
            },
            onDismiss = { showSearchDialog = false }
        )
    }
}
