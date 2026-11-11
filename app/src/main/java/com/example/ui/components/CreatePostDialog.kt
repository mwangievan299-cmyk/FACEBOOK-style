package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.MediaType
import com.example.data.model.Privacy
import com.example.data.model.UserProfile
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.FacebookGreen
import com.example.ui.theme.FacebookRed

data class PresetMedia(
    val title: String,
    val url: String,
    val type: MediaType,
    val previewUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onPostCreated: (
        content: String,
        mediaUris: List<Uri>,
        directMediaUrls: List<String>,
        mediaType: MediaType,
        feeling: String?,
        location: String?,
        privacy: Privacy,
        isReel: Boolean
    ) -> Unit
) {
    var contentText by remember { mutableStateOf("") }
    var selectedPrivacy by remember { mutableStateOf(Privacy.PUBLIC) }
    var showPrivacyMenu by remember { mutableStateOf(false) }

    var selectedMediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var directMediaUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var activeMediaType by remember { mutableStateOf(MediaType.NONE) }

    var feelingText by remember { mutableStateOf<String?>(null) }
    var showFeelingDialog by remember { mutableStateOf(false) }

    var locationText by remember { mutableStateOf<String?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }

    var isReel by remember { mutableStateOf(false) }
    var showPresetMediaSheet by remember { mutableStateOf(false) }

    // Android Modern Zero-Permission Photo/Video Picker
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedMediaUris = uris
            directMediaUrls = emptyList()
            // Detect if video or photo (approx based on uri or default to photo)
            activeMediaType = if (uris.any { it.toString().contains("video", ignoreCase = true) }) {
                MediaType.VIDEO
            } else {
                MediaType.PHOTO
            }
        }
    }

    // Video only picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUris = listOf(uri)
            directMediaUrls = emptyList()
            activeMediaType = MediaType.VIDEO
        }
    }

    val canPost = contentText.isNotBlank() || selectedMediaUris.isNotEmpty() || directMediaUrls.isNotEmpty()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                        Text(
                            text = if (isReel) "Create Reel" else "Create post",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (canPost) {
                                onPostCreated(
                                    contentText,
                                    selectedMediaUris,
                                    directMediaUrls,
                                    activeMediaType,
                                    feelingText,
                                    locationText,
                                    selectedPrivacy,
                                    isReel
                                )
                                onDismiss()
                            }
                        },
                        enabled = canPost,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FacebookBlue,
                            disabledContainerColor = FacebookBlue.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("publish_post_button")
                    ) {
                        Text("Post", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Author Header + Privacy selector
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = user.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Privacy Dropdown Pill
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { showPrivacyMenu = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = selectedPrivacy.iconText, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = selectedPrivacy.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Privacy",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showPrivacyMenu,
                                    onDismissRequest = { showPrivacyMenu = false }
                                ) {
                                    Privacy.entries.forEach { privacy ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(privacy.iconText, fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(privacy.label)
                                                }
                                            },
                                            onClick = {
                                                selectedPrivacy = privacy
                                                showPrivacyMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Feeling or Location Badges
                    if (feelingText != null || locationText != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (feelingText != null) {
                                InputChip(
                                    selected = true,
                                    onClick = { feelingText = null },
                                    label = { Text("Feeling $feelingText") },
                                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove feeling", modifier = Modifier.size(16.dp)) }
                                )
                            }
                            if (locationText != null) {
                                InputChip(
                                    selected = true,
                                    onClick = { locationText = null },
                                    label = { Text("📍 $locationText") },
                                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove location", modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Text Input Field
                    TextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        placeholder = {
                            Text(
                                text = "What's on your mind, ${user.name.split(" ").first()}?",
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 18.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                            .testTag("post_content_input")
                    )

                    // Media Previews
                    if (selectedMediaUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (activeMediaType == MediaType.VIDEO) "Selected Video (from device):" else "Selected Photos (from device):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(selectedMediaUris) { uri ->
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    if (activeMediaType == MediaType.VIDEO) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.DarkGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircle,
                                                contentDescription = "Video",
                                                tint = Color.White,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    } else {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Media preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    // Remove media button
                                    IconButton(
                                        onClick = {
                                            selectedMediaUris = selectedMediaUris.filter { it != uri }
                                            if (selectedMediaUris.isEmpty()) activeMediaType = MediaType.NONE
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Direct URLs preview (from preset gallery)
                    if (directMediaUrls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (activeMediaType == MediaType.VIDEO) "Selected Video:" else "Selected Photos:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (activeMediaType == MediaType.VIDEO) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                VideoPlayerView(
                                    videoUrl = directMediaUrls.first(),
                                    modifier = Modifier.fillMaxSize(),
                                    autoPlay = false
                                )
                                IconButton(
                                    onClick = {
                                        directMediaUrls = emptyList()
                                        activeMediaType = MediaType.NONE
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(28.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(directMediaUrls) { url ->
                                    Box(
                                        modifier = Modifier
                                            .size(110.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        IconButton(
                                            onClick = {
                                                directMediaUrls = directMediaUrls.filter { it != url }
                                                if (directMediaUrls.isEmpty()) activeMediaType = MediaType.NONE
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(24.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Reel toggle
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎬", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Share as Facebook Reel",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Reels appear in Facebook Watch & Feed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isReel,
                            onCheckedChange = { isReel = it }
                        )
                    }
                }

                // Add to your post Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Add to your post",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Photo / Video device picker button
                                IconButton(
                                    onClick = {
                                        mediaPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                        )
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(FacebookGreen.copy(alpha = 0.15f), CircleShape)
                                        .testTag("add_photo_video_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Pick Photos or Videos",
                                        tint = FacebookGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Quick Video Picker
                                IconButton(
                                    onClick = {
                                        videoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(FacebookRed.copy(alpha = 0.15f), CircleShape)
                                        .testTag("add_video_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = "Upload Video",
                                        tint = FacebookRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Preset / Demo Media Selector (for instant rich posting)
                                IconButton(
                                    onClick = { showPresetMediaSheet = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(FacebookBlue.copy(alpha = 0.15f), CircleShape)
                                        .testTag("add_preset_media_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Demo Media Samples",
                                        tint = FacebookBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Feeling / Activity
                                IconButton(
                                    onClick = { showFeelingDialog = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFF7B125).copy(alpha = 0.15f), CircleShape)
                                        .testTag("add_feeling_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SentimentSatisfiedAlt,
                                        contentDescription = "Add Feeling",
                                        tint = Color(0xFFF7B125),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Check in / Location
                                IconButton(
                                    onClick = { showLocationDialog = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFE44234).copy(alpha = 0.15f), CircleShape)
                                        .testTag("add_location_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Check in",
                                        tint = Color(0xFFE44234),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Feeling Dialog
    if (showFeelingDialog) {
        val feelings = listOf(
            "Happy 😊", "Blessed 🙏", "Loved ❤️", "Excited 🤩",
            "Thankful ✨", "Crazy 😜", "Relaxed 😌", "Celebrating 🎉",
            "Traveling ✈️", "Drinking Coffee ☕", "Listening to Music 🎧", "Cooking 🍳"
        )
        AlertDialog(
            onDismissRequest = { showFeelingDialog = false },
            title = { Text("How are you feeling?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    feelings.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { f ->
                                Button(
                                    onClick = {
                                        feelingText = f
                                        showFeelingDialog = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(f, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFeelingDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Location Dialog
    if (showLocationDialog) {
        val locations = listOf(
            "San Francisco, California", "New York, NY", "Tokyo, Japan",
            "London, United Kingdom", "Paris, France", "Sydney, Australia",
            "Yosemite National Park", "Miami Beach, Florida"
        )
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Where are you?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    locations.forEach { loc ->
                        TextButton(
                            onClick = {
                                locationText = loc
                                showLocationDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = FacebookRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(loc, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Preset Media Samples Sheet (For rapid testing with real photos & real playable MP4 videos)
    if (showPresetMediaSheet) {
        val presetMediaList = listOf(
            PresetMedia(
                title = "Ocean Waves (MP4 Video)",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                type = MediaType.VIDEO,
                previewUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400&auto=format&fit=crop&q=80"
            ),
            PresetMedia(
                title = "3D Animation (MP4 Video)",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                type = MediaType.VIDEO,
                previewUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80"
            ),
            PresetMedia(
                title = "Mountain Vista (Photo)",
                url = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1000&auto=format&fit=crop&q=80",
                type = MediaType.PHOTO,
                previewUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=400&auto=format&fit=crop&q=80"
            ),
            PresetMedia(
                title = "Modern Workspace (Photo)",
                url = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1000&auto=format&fit=crop&q=80",
                type = MediaType.PHOTO,
                previewUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400&auto=format&fit=crop&q=80"
            ),
            PresetMedia(
                title = "Sunset Coast (Photo)",
                url = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1000&auto=format&fit=crop&q=80",
                type = MediaType.PHOTO,
                previewUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=400&auto=format&fit=crop&q=80"
            )
        )

        AlertDialog(
            onDismissRequest = { showPresetMediaSheet = false },
            title = {
                Text(
                    text = "Pick a Demo Photo or Video",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Instant high-definition media ready to post and test playback:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    presetMediaList.forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    directMediaUrls = listOf(preset.url)
                                    selectedMediaUris = emptyList()
                                    activeMediaType = preset.type
                                    if (preset.type == MediaType.VIDEO) isReel = true
                                    showPresetMediaSheet = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                AsyncImage(
                                    model = preset.previewUrl,
                                    contentDescription = preset.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (preset.type == MediaType.VIDEO) {
                                    Icon(
                                        Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (preset.type == MediaType.VIDEO) "Video Clip • Ready to Play" else "HD Photo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPresetMediaSheet = false }) { Text("Close") }
            }
        )
    }
}
