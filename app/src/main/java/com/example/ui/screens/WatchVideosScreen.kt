package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PostEntity
import com.example.data.model.MediaType
import com.example.data.model.ReactionType
import com.example.data.model.UserProfile
import com.example.ui.components.PostCard
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.FacebookRed

@Composable
fun WatchVideosScreen(
    reelsAndVideos: List<PostEntity>,
    currentUser: UserProfile,
    onCreateVideoClick: () -> Unit,
    onReaction: (postId: String, ReactionType?) -> Unit,
    onCommentClick: (postId: String) -> Unit,
    onShareClick: (postId: String) -> Unit,
    onDeletePost: (postId: String) -> Unit,
    onMediaClick: (url: String, type: MediaType) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf("For you", "Reels", "Live", "Following", "Saved")
    var selectedFilter by remember { mutableStateOf("For you") }

    val videoList = remember(reelsAndVideos, selectedFilter) {
        when (selectedFilter) {
            "Reels" -> reelsAndVideos.filter { it.isReel }
            else -> reelsAndVideos.filter { it.mediaType == MediaType.VIDEO.name }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("watch_videos_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Watch Header & Filter Chips
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Watch & Reels",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = onCreateVideoClick,
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("create_reel_button")
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Post Video", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filters) { filter ->
                            val isSelected = filter == selectedFilter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) FacebookBlue.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedFilter = filter }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = filter,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) FacebookBlue else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Empty state
        if (videoList.isEmpty()) {
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
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎥", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No videos yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Upload the first video or reel to share with everybody!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCreateVideoClick,
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                        ) {
                            Text("Upload Video", color = Color.White)
                        }
                    }
                }
            }
        } else {
            // Videos stream
            items(videoList, key = { it.id }) { post ->
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
