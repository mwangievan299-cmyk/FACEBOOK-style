package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ThumbUp
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
import com.example.data.model.FriendItem
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.FacebookGreen

data class ChatMessage(
    val id: String,
    val senderName: String,
    val isMe: Boolean,
    val text: String,
    val time: String
)

@Composable
fun MessengerDialog(
    friends: List<FriendItem>,
    onDismiss: () -> Unit
) {
    var selectedFriend by remember { mutableStateOf<FriendItem?>(friends.firstOrNull { it.isFriend }) }
    var messageInput by remember { mutableStateOf("") }
    var chatHistory by remember {
        mutableStateOf(
            listOf(
                ChatMessage("m1", "Sophia Bennett", false, "Hey Alex! Loved your recent video post!", "2:15 PM"),
                ChatMessage("m2", "Alex Morgan", true, "Thanks Sophia! Shot it on the coastline last weekend.", "2:17 PM"),
                ChatMessage("m3", "Sophia Bennett", false, "The colors and waves looked surreal! 🌊", "2:18 PM")
            )
        )
    }

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
                    .testTag("messenger_dialog")
            ) {
                // Messenger Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close Messenger")
                        }

                        if (selectedFriend != null) {
                            AsyncImage(
                                model = selectedFriend!!.avatarUrl,
                                contentDescription = selectedFriend!!.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedFriend!!.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (selectedFriend!!.isOnline) "Active now" else "Active recently",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selectedFriend!!.isOnline) FacebookGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "Messenger",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Chat Messages
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(chatHistory, key = { it.id }) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (msg.isMe) 16.dp else 4.dp,
                                            bottomEnd = if (msg.isMe) 4.dp else 16.dp
                                        )
                                    )
                                    .background(if (msg.isMe) FacebookBlue else MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (msg.isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Chat Input Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            placeholder = { Text("Aa", fontSize = 14.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .testTag("messenger_input"),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        if (messageInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val newMsg = ChatMessage(
                                        id = "msg_${System.currentTimeMillis()}",
                                        senderName = "Alex Morgan",
                                        isMe = true,
                                        text = messageInput,
                                        time = "Just now"
                                    )
                                    chatHistory = chatHistory + newMsg
                                    messageInput = ""
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(FacebookBlue, CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    val likeMsg = ChatMessage(
                                        id = "msg_${System.currentTimeMillis()}",
                                        senderName = "Alex Morgan",
                                        isMe = true,
                                        text = "👍",
                                        time = "Just now"
                                    )
                                    chatHistory = chatHistory + likeMsg
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.ThumbUp, contentDescription = "Like", tint = FacebookBlue)
                            }
                        }
                    }
                }
            }
        }
    }
}
