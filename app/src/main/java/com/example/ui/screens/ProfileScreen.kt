package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.model.UserProfile
import com.example.ui.components.PostCard
import com.example.ui.theme.FacebookBlue

@Composable
fun ProfileScreen(
    user: UserProfile,
    allPosts: List<PostEntity>,
    onUpdateProfile: (name: String, bio: String, avatarUrl: String, location: String) -> Unit,
    onCreatePostClick: () -> Unit,
    onReaction: (postId: String, ReactionType?) -> Unit,
    onCommentClick: (postId: String) -> Unit,
    onShareClick: (postId: String) -> Unit,
    onDeletePost: (postId: String) -> Unit,
    onMediaClick: (url: String, type: MediaType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedProfileTab by remember { mutableStateOf("Posts") }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val userPosts = remember(allPosts, user.id) {
        allPosts.filter { it.authorId == user.id || it.authorName == user.name }
    }

    // Extract all photos across posts
    val allPhotos = remember(allPosts) {
        allPosts.filter { it.mediaType == MediaType.PHOTO.name }
            .flatMap { it.getMediaList() }
    }

    // Extract all videos across posts
    val allVideos = remember(allPosts) {
        allPosts.filter { it.mediaType == MediaType.VIDEO.name }
            .flatMap { it.getMediaList() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Profile Header Surface
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Cover Photo & Avatar Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        // Cover Photo
                        AsyncImage(
                            model = user.coverUrl,
                            contentDescription = "Cover Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(175.dp)
                        )

                        // Camera button on cover
                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(36.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Edit Cover", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        // Avatar overlapping bottom of cover
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 16.dp)
                        ) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = user.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            )

                            // Avatar camera badge
                            IconButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Edit Profile Picture", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // User Details
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (user.bio.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.bio,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick info (Location, Workplace, Joined)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Lives in ${user.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = user.workplace, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = user.joinedDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${user.friendsCount} friends • ${user.followersCount} followers",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Add to story & Edit profile
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onCreatePostClick,
                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add to story", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit profile", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Sub Tabs: Posts | Photos | Videos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("Posts", "Photos", "Videos").forEach { tab ->
                            val isSelected = selectedProfileTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedProfileTab = tab }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sub-Tab Content
        when (selectedProfileTab) {
            "Posts" -> {
                if (userPosts.isEmpty()) {
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
                                Text(text = "✍️", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No posts from you yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Post photos or videos to start your timeline!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onCreatePostClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                                ) {
                                    Text("Post Photo or Video", color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    items(userPosts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            currentUserId = user.id,
                            onReaction = { reaction -> onReaction(post.id, reaction) },
                            onCommentClick = { onCommentClick(post.id) },
                            onShareClick = { onShareClick(post.id) },
                            onDeleteClick = { onDeletePost(post.id) },
                            onMediaClick = onMediaClick
                        )
                    }
                }
            }
            "Photos" -> {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "All Photos (${allPhotos.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // 3-column photo grid
                            allPhotos.chunked(3).forEach { rowPhotos ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    rowPhotos.forEach { photoUrl ->
                                        AsyncImage(
                                            model = photoUrl,
                                            contentDescription = "Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(115.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable { onMediaClick(photoUrl, MediaType.PHOTO) }
                                        )
                                    }
                                    if (rowPhotos.size < 3) {
                                        repeat(3 - rowPhotos.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Videos" -> {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "All Videos (${allVideos.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            allVideos.chunked(2).forEach { rowVideos ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowVideos.forEach { videoUrl ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Black)
                                                .clickable { onMediaClick(videoUrl, MediaType.VIDEO) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircle,
                                                contentDescription = "Play Video",
                                                tint = Color.White,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                    if (rowVideos.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(user.name) }
        var editBio by remember { mutableStateOf(user.bio) }
        var editLocation by remember { mutableStateOf(user.location) }
        var editAvatar by remember { mutableStateOf(user.avatarUrl) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAvatar,
                        onValueChange = { editAvatar = it },
                        label = { Text("Avatar Image URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(editName, editBio, editAvatar, editLocation)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
