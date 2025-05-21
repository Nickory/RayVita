package com.codelab.basiclayouts.network

import com.codelab.basiclayouts.model.ApiResponse
import com.codelab.basiclayouts.model.BlockFriendRequest
import com.codelab.basiclayouts.model.Comment
import com.codelab.basiclayouts.model.CommentRequest
import com.codelab.basiclayouts.model.CreatePostRequest
import com.codelab.basiclayouts.model.Friend
import com.codelab.basiclayouts.model.FriendRequest
import com.codelab.basiclayouts.model.LikePostRequest
import com.codelab.basiclayouts.model.Post
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
data class FriendRequestsResponse(
    val requests: List<FriendRequest>
)
interface SocialApi {
    // Friend System APIs (Phase 2)
    @POST("/api/friend/request")
    suspend fun sendFriendRequest(@Body request: Map<String, Int>): Response<ApiResponse<Nothing>>

    @PUT("/api/friend/request/{req_id}")
    suspend fun processFriendRequest(
        @Path("req_id") requestId: Int,
        @Body action: Map<String, String>
    ): Response<ApiResponse<Nothing>>

    // 修改 getFeed API，直接接收 List<Post> 而不是包装在 ApiResponse 中
    @GET("/api/post/feed")
    suspend fun getUserFeed(@Query("user_id") userId: Int): Response<List<Post>>

    // 修改 getFriendList API
    @GET("/api/friend/list")
    suspend fun getFriendList(@Query("user_id") userId: Int): Response<List<Friend>>

    @GET("/api/friend/incoming")
    suspend fun getIncomingFriendRequests(
        @Query("user_id") userId: Int
    ): Response<FriendRequestsResponse>

    @PUT("/api/friend/{friend_id}/block")
    suspend fun blockOrUnblockFriend(
        @Path("friend_id") friendId: Int,
        @Body request: BlockFriendRequest
    ): Response<ApiResponse<Nothing>>

    // Social Posts APIs (Phase 3)
    @POST("/api/post/create")
    suspend fun createPost(@Body post: CreatePostRequest): Response<ApiResponse<Post>>

    @GET("/api/post/{post_id}")
    suspend fun getPostDetails(@Path("post_id") postId: Int): Response<ApiResponse<Post>>

    @DELETE("/api/post/{post_id}")
    suspend fun deletePost(@Path("post_id") postId: Int): Response<ApiResponse<Nothing>>

    @POST("/api/post/{post_id}/like")
    suspend fun likeOrUnlikePost(
        @Path("post_id") postId: Int,
        @Body request: LikePostRequest
    ): Response<ApiResponse<Nothing>>

    @POST("/api/post/{post_id}/comment")
    suspend fun commentOnPost(
        @Path("post_id") postId: Int,
        @Body comment: CommentRequest
    ): Response<ApiResponse<Comment>>

    @GET("/api/post/{post_id}/comments")
    suspend fun getPostComments(@Path("post_id") postId: Int): Response<ApiResponse<List<Comment>>>

    }