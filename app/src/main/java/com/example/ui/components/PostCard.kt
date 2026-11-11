package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.PostEntity
import com.example.data.model.MediaType
import com.example.data.model.ReactionType
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.FacebookTextSecondaryLight
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: PostEntity,
    currentUserId: String,
    onReaction: (ReactionType?) -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMediaClick: (url: String, type: MediaType) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    var showReactionDock by remember { mutableStateOf(false) }

    val userReaction = ReactionType.fromString(post.userReaction)
    val mediaList = remember(post.mediaUrls) { post.getMediaList() }
    val isVideo = post.mediaType == MediaType.VIDEO.name
    val isPhoto = post.mediaType == MediaType.PHOTO.name

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}"),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Post Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author Avatar
                AsyncImage(
                    model = post.authorAvatarUrl,
                    contentDescription = post.authorName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Name & Metadata
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (post.feeling != null) {
                            Text(
                                text = " is ${post.feeling}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatTimestamp(post.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (post.privacy == "PUBLIC") "🌐" else if (post.privacy == "FRIENDS") "👥" else "🔒",
                            fontSize = 11.sp
                        )
                        if (post.location != null) {
                            Text(
                                text = "• at ${post.location}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // More Menu Button
                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Post Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Save post") },
                            onClick = { showMoreMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Copy link") },
                            onClick = {
                                onShareClick()
                                showMoreMenu = false
                            }
                        )
                        if (post.authorId == currentUserId || currentUserId == "user_me") {
                            DropdownMenuItem(
                                text = { Text("Delete post", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMoreMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }

            // Post Text
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Post Media (Video or Photos)
            if (isVideo && mediaList.isNotEmpty()) {
                val videoUrl = mediaList.first()
                VideoPlayerView(
                    videoUrl = videoUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    autoPlay = false,
                    isLooping = true,
                    onFullScreenClick = {
                        onMediaClick(videoUrl, MediaType.VIDEO)
                    }
                )
            } else if (isPhoto && mediaList.isNotEmpty()) {
                PostPhotosGrid(
                    mediaList = mediaList,
                    onPhotoClick = { url -> onMediaClick(url, MediaType.PHOTO) }
                )
            }

            // Reaction Count and Comment Count Row
            val totalReactions = post.getTotalReactions()
            if (totalReactions > 0 || post.commentCount > 0 || post.shareCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reactions Counter with mini emojis
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val reactionsToShow = post.getPrimaryReactions()
                        reactionsToShow.forEach { r ->
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(r.color),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = r.emoji, fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (userReaction != null && totalReactions == 1) "You"
                            else if (userReaction != null) "You and ${totalReactions - 1} others"
                            else totalReactions.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Comments and Shares count
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (post.commentCount > 0) {
                            Text(
                                text = "${post.commentCount} comments",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable { onCommentClick() }
                            )
                        }
                        if (post.shareCount > 0) {
                            Text(
                                text = "${post.shareCount} shares",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 14.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                thickness = 0.5.dp
            )

            // Reaction Dock Floating Menu
            Box(modifier = Modifier.fillMaxWidth()) {
                if (showReactionDock) {
                    ReactionDock(
                        visible = showReactionDock,
                        onReactionSelect = { reaction ->
                            onReaction(reaction)
                            showReactionDock = false
                        },
                        onDismiss = { showReactionDock = false },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(y = (-48).dp)
                    )
                }

                // Interactive Bottom Action Bar (Like, Comment, Share)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like / React Button
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .combinedClickable(
                                onClick = {
                                    if (userReaction != null) {
                                        onReaction(null) // Unlike
                                    } else {
                                        onReaction(ReactionType.LIKE)
                                    }
                                },
                                onLongClick = {
                                    showReactionDock = true
                                }
                            )
                            .padding(vertical = 8.dp)
                            .testTag("post_like_button_${post.id}"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (userReaction != null) {
                            Text(text = userReaction.emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = userReaction.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = userReaction.color
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.ThumbUp,
                                contentDescription = "Like",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Like",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Comment Button
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onCommentClick)
                            .padding(vertical = 8.dp)
                            .testTag("post_comment_button_${post.id}"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Comment,
                            contentDescription = "Comment",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Comment",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Share Button
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onShareClick)
                            .padding(vertical = 8.dp)
                            .testTag("post_share_button_${post.id}"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostPhotosGrid(
    mediaList: List<String>,
    onPhotoClick: (String) -> Unit
) {
    when (mediaList.size) {
        1 -> {
            AsyncImage(
                model = mediaList[0],
                contentDescription = "Post photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .clickable { onPhotoClick(mediaList[0]) }
            )
        }
        2 -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                AsyncImage(
                    model = mediaList[0],
                    contentDescription = "Photo 1",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onPhotoClick(mediaList[0]) }
                )
                AsyncImage(
                    model = mediaList[1],
                    contentDescription = "Photo 2",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onPhotoClick(mediaList[1]) }
                )
            }
        }
        else -> {
            // 3 or more photos
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                AsyncImage(
                    model = mediaList[0],
                    contentDescription = "Photo 1",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { onPhotoClick(mediaList[0]) }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    AsyncImage(
                        model = mediaList[1],
                        contentDescription = "Photo 2",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onPhotoClick(mediaList[1]) }
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onPhotoClick(mediaList[2]) }
                    ) {
                        AsyncImage(
                            model = mediaList[2],
                            contentDescription = "Photo 3",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (mediaList.size > 3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${mediaList.size - 2}",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val mins = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        mins < 1 -> "Just now"
        mins < 60 -> "${mins}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
