package com.codelab.basiclayouts.viewmodel.profile

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.data.UserSessionManager
import com.codelab.basiclayouts.model.LoginRequest
import com.codelab.basiclayouts.model.RegisterRequest
import com.codelab.basiclayouts.model.ResetPasswordRequest
import com.codelab.basiclayouts.model.UserInfo
import com.codelab.basiclayouts.model.VerificationCodeRequest
import com.codelab.basiclayouts.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Authentication related UI state
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val userInfo: UserInfo? = null,
    val verificationSent: Boolean = false,
    val verificationEmail: String? = null,
    val verificationTimestamp: LocalDateTime? = null,
    val selectedAvatarIndex: Int = 0, // Avatar selection field
    val allUsers: List<UserInfo> = emptyList(), // All users from database
    val isLoadingAllUsers: Boolean = false // Loading state for all users
)

/**
 * Available avatar options
 */
enum class AvatarOption(val index: Int, val emoji: String, val description: String) {
    DEFAULT(0, "👤", "Default"),
    SMILE(1, "😊", "Smile"),
    COOL(2, "😎", "Cool"),
    STAR(3, "⭐", "Star"),
    HEART(4, "❤️", "Heart"),
    LIKE(5, "👍", "Thumbs Up"),
    ROCKET(6, "🚀", "Rocket"),
    CROWN(7, "👑", "Crown"),
    DIAMOND(8, "💎", "Diamond"),
    FIRE(9, "🔥", "Fire"),
    MAGIC(10, "✨", "Magic"),
    UNICORN(11, "🦄", "Unicorn")
}

/**
 * Authentication ViewModel with avatar management
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AuthViewModel"
        private const val VERIFICATION_VALID_MINUTES = 5
        private const val KEY_SELECTED_AVATAR = "selected_avatar_index"
        private const val MAX_USER_ID = 100 // 最大尝试的用户 ID
        private const val BATCH_SIZE = 10  // 每批并行请求的数量
    }

    // UI state
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // API and session manager
    private val authApi = RetrofitClient.authApi
    private val sessionManager = UserSessionManager(application)

    // Available avatar options
    val avatarOptions = AvatarOption.values().toList()

    init {
        // Check saved login status and avatar on app start
        checkSavedLoginStatus()
        loadSavedAvatar()
    }

    /**
     * Load saved avatar selection
     */
    private fun loadSavedAvatar() {
        try {
            val savedAvatarIndex = sessionManager.getSelectedAvatar()
            _uiState.value = _uiState.value.copy(selectedAvatarIndex = savedAvatarIndex)
            Log.d(TAG, "Loaded saved avatar index: $savedAvatarIndex")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load saved avatar", e)
        }
    }

    /**
     * Update selected avatar
     */
    fun updateSelectedAvatar(avatarIndex: Int) {
        try {
            if (avatarIndex in 0 until avatarOptions.size) {
                sessionManager.saveSelectedAvatar(avatarIndex)
                _uiState.value = _uiState.value.copy(selectedAvatarIndex = avatarIndex)
                Log.d(TAG, "Avatar updated to index: $avatarIndex")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update avatar", e)
        }
    }

    /**
     * Get current avatar option
     */
    fun getCurrentAvatar(): AvatarOption {
        return avatarOptions.getOrNull(_uiState.value.selectedAvatarIndex) ?: AvatarOption.DEFAULT
    }

    /**
     * Check saved login status
     */
    private fun checkSavedLoginStatus() {
        viewModelScope.launch {
            try {
                val savedUser = sessionManager.getUserSession()
                if (savedUser != null) {
                    Log.d(TAG, "Found saved user session: ${savedUser.nickname}")
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        userInfo = savedUser
                    )
                    // Refresh session expiry
                    sessionManager.refreshSessionExpiry()
                } else {
                    Log.d(TAG, "No valid user session found")
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = false,
                        userInfo = null
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check login status", e)
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = false,
                    userInfo = null
                )
            }
        }
    }

    /**
     * User login
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val request = LoginRequest(email, password)
                val response = authApi.login(request)

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    Log.d(TAG, "Login response: $loginResponse")

                    if (loginResponse?.msg == "login ok" && loginResponse.user_id != null) {
                        // Login successful, get user details
                        val userResponse = authApi.getUserInfo(loginResponse.user_id)

                        if (userResponse.isSuccessful) {
                            val userDetailResp = userResponse.body()
                            Log.d(TAG, "User details response: $userDetailResp")

                            // Build user info object
                            val userInfo = UserInfo(
                                user_id = loginResponse.user_id,
                                email = userDetailResp?.email ?: email,
                                nickname = userDetailResp?.nickname ?: loginResponse.nickname ?: "",
                                theme = userDetailResp?.theme ?: loginResponse.theme ?: "light"
                            )

                            // Save user session
                            sessionManager.saveUserSession(userInfo)

                            // Update UI state
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                userInfo = userInfo,
                                errorMessage = null
                            )

                            Log.d(TAG, "Login successful: ${userInfo.nickname}")
                        } else {
                            // User details failed, but login successful
                            Log.w(TAG, "Failed to get user details, using login response info")
                            val userInfo = UserInfo(
                                user_id = loginResponse.user_id,
                                email = email,
                                nickname = loginResponse.nickname ?: "",
                                theme = loginResponse.theme ?: "light"
                            )

                            sessionManager.saveUserSession(userInfo)

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                userInfo = userInfo,
                                errorMessage = null
                            )
                        }
                    } else {
                        // Login failed
                        Log.w(TAG, "Login failed: ${loginResponse?.msg}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = loginResponse?.msg ?: "Login failed"
                        )
                    }
                } else {
                    // HTTP request failed
                    Log.e(TAG, "Login request failed: ${response.code()}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Server error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login exception", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Login failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Send verification code
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendVerificationCode(email: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val request = VerificationCodeRequest(email)
                val response = authApi.sendVerificationCode(request)

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        verificationSent = true,
                        verificationEmail = email,
                        verificationTimestamp = LocalDateTime.now()
                    )
                    Log.d(TAG, "Verification code sent successfully")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to send verification code: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Send verification code exception", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to send verification code: ${e.message}"
                )
            }
        }
    }

    /**
     * Check if verification code is expired
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isVerificationCodeExpired(): Boolean {
        val timestamp = _uiState.value.verificationTimestamp ?: return true
        val minutes = ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now())
        return minutes >= VERIFICATION_VALID_MINUTES
    }

    /**
     * User registration
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun register(email: String, code: String, nickname: String, password: String) {
        // Validate email consistency
        if (email != _uiState.value.verificationEmail) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Email does not match verification email"
            )
            return
        }

        // Check if verification code is expired
        if (isVerificationCodeExpired()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Verification code has expired"
            )
            return
        }

        // Execute registration
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val request = RegisterRequest(email, password, nickname)
                val response = authApi.register(request)

                if (response.isSuccessful) {
                    val registerResponse = response.body()

                    if (registerResponse?.status == "success") {
                        // Registration successful
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            verificationSent = false,
                            verificationEmail = null,
                            verificationTimestamp = null
                        )
                        Log.d(TAG, "Registration successful")
                    } else {
                        // Registration failed
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = registerResponse?.message ?: "Registration failed"
                        )
                    }
                } else {
                    // HTTP request failed
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Server error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Registration exception", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Registration failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Reset password request
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun resetPasswordRequest(email: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val request = VerificationCodeRequest(email)
                val response = authApi.sendVerificationCode(request)

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        verificationSent = true,
                        verificationEmail = email,
                        verificationTimestamp = LocalDateTime.now()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to send reset code: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Reset password request exception", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to send reset code: ${e.message}"
                )
            }
        }
    }

    /**
     * Reset password
     */
    fun resetPassword(email: String, newPassword: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val request = ResetPasswordRequest(email, newPassword)
                val response = authApi.resetPassword(request)

                if (response.isSuccessful) {
                    val resetResponse = response.body()

                    if (resetResponse?.status == "success") {
                        // Reset successful
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            verificationSent = false,
                            verificationEmail = null,
                            verificationTimestamp = null
                        )
                    } else {
                        // Reset failed
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = resetResponse?.message ?: "Password reset failed"
                        )
                    }
                } else {
                    // HTTP request failed
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Server error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Reset password exception", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Password reset failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Logout
     */
    fun logout() {
        try {
            // Clear local session
            sessionManager.clearUserSession()

            // Update UI state
            _uiState.value = _uiState.value.copy(
                isLoggedIn = false,
                userInfo = null
            )

            Log.d(TAG, "User logged out")
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed", e)
        }
    }

    /**
     * Get all users from database (Developer function)
     */


    // 假设 _uiState 是 MutableStateFlow，包含 allUsers 和 isLoadingAllUsers
    fun getAllUsers() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingAllUsers = true)
                val allUsers = mutableListOf<UserInfo>()

                // 分批获取用户信息
                withContext(Dispatchers.IO) {
                    var currentId = 1
                    while (currentId <= MAX_USER_ID) {
                        // 创建一批 ID
                        val batchIds = (currentId until (currentId + BATCH_SIZE).coerceAtMost(MAX_USER_ID + 1))
                        val batchResults = batchIds.map { userId ->
                            async {
                                try {
                                    val response = authApi.getUserInfo(userId)
                                    if (response.isSuccessful && response.body() != null) {
                                        response.body()!!
                                    } else {
                                        Log.w(TAG, "Failed to get user $userId: ${response.code()}")
                                        null
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error fetching user $userId", e)
                                    null
                                }
                            }
                        }.awaitAll()

                        // 添加成功获取的用户到列表
                        allUsers.addAll(batchResults.filterNotNull())
                        currentId += BATCH_SIZE

                        // 如果当前批次没有任何有效用户，可能已到达用户列表末尾
                        if (batchResults.all { it == null }) {
                            Log.d(TAG, "No more users found, stopping at ID $currentId")
                            break
                        }
                    }
                }

                // 更新 UI 状态
                _uiState.value = _uiState.value.copy(
                    isLoadingAllUsers = false,
                    allUsers = allUsers
                )
                Log.d(TAG, "Retrieved ${allUsers.size} users from database")

            } catch (e: Exception) {
                Log.e(TAG, "Get all users exception", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingAllUsers = false,
                    allUsers = emptyList()
                )
            }
        }
    }

    /**
     * Clear all users data
     */
    fun clearAllUsersData() {
        _uiState.value = _uiState.value.copy(allUsers = emptyList())
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}