package com.codelab.basiclayouts.model

/**
 * 登录请求数据类
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * 登录响应数据类
 */
data class LoginResponse(
    val status: String,
    val user_id: Int?,
    val nickname: String?,
    val theme: String?,
    val msg: String? // 兼容"login ok"响应
)

/**
 * 注册请求数据类
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String
)

/**
 * 注册响应数据类
 */
data class RegisterResponse(
    val status: String? = null,
    val message: String? = null,
    val msg: String? = null, // Added to match server response
    val user_id: Int? = null
)
/**
 * 验证码请求数据类
 */
data class VerificationCodeRequest(
    val email: String
)

/**
 * 验证码响应数据类
 */
data class VerificationCodeResponse(
    val status: String,
    val message: String
)

/**
 * 重置密码请求数据类
 */
data class ResetPasswordRequest(
    val email: String,
    val password: String
)

/**
 * 用户信息数据类
 */
data class UserInfo(
    val user_id: Int,
    val email: String,
    val nickname: String?,
    val theme: String?
)