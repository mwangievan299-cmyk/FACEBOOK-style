package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.PostEntity
import com.example.data.model.MediaType
import com.example.data.model.ReactionType
import com.example.data.model.Story
import com.example.data.model.UserProfile
import com.example.ui.components.PostCard
import com.example.ui.components.StoriesTray
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.FacebookGreen
import com.example.ui.theme.FacebookRed

@Composable
fun FeedScreen(
    posts: List<PostEntity>,
    stories: List<Story>,
    currentUser: UserProfile,
    searchQuery: String,
    onCreatePostClick: () -> Unit,
    onCreateStoryClick: () -> Unit,
    onStoryClick: (Story) -> Unit,
    onReaction: (postId: String, ReactionType?) -> Unit,
    onCommentClick: (postId: String) -> Unit,
    onShareClick: (postId: String) -> Unit,
    onDeletePost: (postId: String) -> Unit,
    onMediaClick: (url: String, type: MediaType) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredPosts = if (searchQuery.isBlank()) {
        posts
    } else {
        posts.filter {
            it.content.contains(searchQuery, ignoreCase = true) ||
                    it.authorName.contains(searchQuery, ignoreCase = true) ||
                    (it.location?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("feed_screen_list"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // "What's on your mind?" Composer Box
        item {
            FeedComposerBox(
                user = currentUser,
                onClick = onCreatePostClick
            )
        }

        // Stories Tray
        item {
            StoriesTray(
                stories = stories,
                currentUser = currentUser,
                onCreateStoryClick = onCreateStoryClick,
                onStoryClick = onStoryClick
            )
        }

        // Filter / Search Notice
        if (searchQuery.isNotBlank()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Results for \"$searchQuery\" (${filteredPosts.size} posts found)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }

        // Feed Posts
        if (filteredPosts.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📱", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No posts matching \"$searchQuery\"" else "No posts yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Share the first photo, video, or update with everybody!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCreatePostClick,
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                        ) {
                            Text("Create Post", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredPosts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    currentUserId = currentUser.id,
                    onReaction = { reaction -> onReaction(post.id, reaction) },
                    onCommentClick = { onCommentClick(post.id) },
                    onShareClick = { onShareClick(post.id) },
                    onDeleteClick = { onDeletePost(post.id) },
                    onMediaClick = onMediaClick
                )
            }
        }
    }
}

@Composable
fun FeedComposerBox(
    user: UserProfile,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // User Avatar + What's on your mind input fake button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = user.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onClick)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "What's on your mind?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Action shortcuts row: Photo/Video, Live, Feeling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Video
                ComposerShortcut(
                    icon = Icons.Default.Videocam,
                    iconColor = FacebookRed,
                    label = "Live",
                    onClick = onClick
                )

                // Photo/video
                ComposerShortcut(
                    icon = Icons.Default.PhotoLibrary,
                    iconColor = FacebookGreen,
                    label = "Photo/video",
                    onClick = onClick
                )

                // Feeling/activity
                ComposerShortcut(
                    icon = Icons.Default.SentimentSatisfiedAlt,
                    iconColor = Color(0xFFF7B125),
                    label = "Feeling",
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun ComposerShortcut(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
