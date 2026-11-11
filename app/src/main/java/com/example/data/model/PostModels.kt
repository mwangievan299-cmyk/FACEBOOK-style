package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class ReactionType(val label: String, val emoji: String, val color: Color) {
    LIKE("Like", "👍", ReactionLike),
    LOVE("Love", "❤️", ReactionLove),
    CARE("Care", "🥰", ReactionCare),
    HAHA("Haha", "😆", ReactionHaha),
    WOW("Wow", "😮", ReactionWow),
    SAD("Sad", "😢", ReactionSad),
    ANGRY("Angry", "😡", ReactionAngry);

    companion object {
        fun fromString(name: String?): ReactionType? {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
        }
    }
}

enum class MediaType {
    NONE, PHOTO, VIDEO
}

enum class Privacy(val label: String, val iconText: String) {
    PUBLIC("Public", "🌐"),
    FRIENDS("Friends", "👥"),
    ONLY_ME("Only Me", "🔒")
}

data class PostComment(
    val id: String,
    val postId: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val content: String,
    val timestamp: Long,
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)

data class Story(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val mediaUrl: String,
    val mediaType: MediaType = MediaType.PHOTO,
    val timestamp: Long,
    val isSeen: Boolean = false,
    val caption: String = ""
)

data class UserProfile(
    val id: String,
    val name: String,
    val username: String,
    val bio: String,
    val avatarUrl: String,
    val coverUrl: String,
    val location: String,
    val workplace: String,
    val joinedDate: String,
    val friendsCount: Int,
    val followersCount: Int
)

data class NotificationItem(
    val id: String,
    val actorName: String,
    val actorAvatarUrl: String,
    val actionText: String,
    val timestamp: Long,
    val isUnread: Boolean = true,
    val targetType: String = "post", // "post", "video", "friend", "story"
    val thumbnailPreview: String? = null
)

data class FriendItem(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val mutualFriends: Int,
    val isFriend: Boolean = false,
    val hasPendingRequest: Boolean = false,
    val isOnline: Boolean = false
)
