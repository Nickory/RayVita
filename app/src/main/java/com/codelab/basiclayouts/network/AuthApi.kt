package com.codelab.basiclayouts.network


import com.codelab.basiclayouts.model.LoginRequest
import com.codelab.basiclayouts.model.LoginResponse
import com.codelab.basiclayouts.model.RegisterRequest
import com.codelab.basiclayouts.model.RegisterResponse
import com.codelab.basiclayouts.model.ResetPasswordRequest
import com.codelab.basiclayouts.model.UserInfo
import com.codelab.basiclayouts.model.VerificationCodeRequest
import com.codelab.basiclayouts.model.VerificationCodeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Authentication related API interface
 */
interface AuthApi {

    /**
     * User login
     * Corresponds to backend POST /api/user/login
     */
    @POST("user/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /**
     * User registration
     * Corresponds to backend POST /api/user/register
     */
    @POST("user/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    /**
     * Send verification code
     * Custom interface for requesting verification code from backend
     */
    @POST("user/send_verification")
    suspend fun sendVerificationCode(@Body request: VerificationCodeRequest): Response<VerificationCodeResponse>
    //预留 目前发送验证码不用api实现

    /**
     * Reset password
     * Corresponds to backend POST /api/user/reset_password
     */
    @POST("user/reset_password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<RegisterResponse>

    /**
     * Get user detailed information
     * Corresponds to backend GET /api/user/{id}
     */
    @GET("user/{id}")
    suspend fun getUserInfo(@Path("id") userId: Int): Response<UserInfo>
//
//    @POST("/auth/refresh")
//    suspend fun refreshToken(@Body refreshToken: String): Response<AuthResponse>

}