package com.codelab.basiclayouts.model

import com.google.gson.annotations.SerializedName

// Friend-related models
data class Friend(
    val user_id: Int,
    val nickname: String,
    val email: String? = null,
    val avatar_index: Int = 0,
    val is_blocked: Boolean = false
)

data class FriendRequest(
    val request_id: Int,
    val from_user_id: Int,
    val to_user_id: Int,
    val status: String, // "pending", "accepted", "rejected"
    val created_at: String,
    val from_user_nickname: String? = null
)

// Post-related models
data class Post(
    val post_id: Int,
    val user_id: Int,
    val user_nickname: String,
    val user_avatar_index: Int = 0,
    val text_content: String,
    val image_url: String? = null,
    val created_at: String,
    val like_count: Int = 0,
    val comment_count: Int = 0,
    val is_liked_by_me: Boolean = false
)

data class Comment(
    val comment_id: Int,
    val post_id: Int,
    val user_id: Int,
    val user_nickname: String? = null,
    val user_avatar_index: Int = 0,
    val comment_text: String,

    // Handle both field name formats
    @SerializedName(value = "created_at", alternate = ["comment_dt"])
    val created_at: String
)

data class PostWithComments(
    val post: Post,
    val comments: List<Comment> = emptyList()
)

// Request/Response models
data class CreatePostRequest(
    val user_id: Int,
    val text_content: String,
    val image_url: String? = null
)

data class LikePostRequest(
    val user_id: Int
)

data class CommentRequest(
    val user_id: Int,
    val comment_text: String
)

data class FriendRequestAction(
    val action: String // "accept" or "reject"
)

data class BlockFriendRequest(
    val user_id: Int,
    val block: Boolean
)

// API Response wrapper
data class ApiResponse<T>(
    val status: String,
    val message: String? = null,
    val data: T? = null
)