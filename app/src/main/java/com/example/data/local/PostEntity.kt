package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MediaType
import com.example.data.model.Privacy
import com.example.data.model.ReactionType

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val timestamp: Long,
    val content: String,
    val mediaType: String = MediaType.NONE.name,
    val mediaUrls: String = "", // Comma-separated URLs or local file paths
    val feeling: String? = null,
    val location: String? = null,
    val privacy: String = Privacy.PUBLIC.name,
    val likeCount: Int = 0,
    val loveCount: Int = 0,
    val hahaCount: Int = 0,
    val wowCount: Int = 0,
    val sadCount: Int = 0,
    val angryCount: Int = 0,
    val userReaction: String? = null, // "LIKE", "LOVE", etc.
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val isReel: Boolean = false,
    val audioTitle: String? = null
) {
    fun getMediaList(): List<String> {
        if (mediaUrls.isBlank()) return emptyList()
        return mediaUrls.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun getTotalReactions(): Int {
        return likeCount + loveCount + hahaCount + wowCount + sadCount + angryCount
    }

    fun getPrimaryReactions(): List<ReactionType> {
        val list = mutableListOf<ReactionType>()
        if (likeCount > 0) list.add(ReactionType.LIKE)
        if (loveCount > 0) list.add(ReactionType.LOVE)
        if (hahaCount > 0) list.add(ReactionType.HAHA)
        if (wowCount > 0) list.add(ReactionType.WOW)
        if (sadCount > 0) list.add(ReactionType.SAD)
        if (angryCount > 0) list.add(ReactionType.ANGRY)
        return if (list.isEmpty()) listOf(ReactionType.LIKE) else list.take(3)
    }
}

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val content: String,
    val timestamp: Long,
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)
