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
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val userInfo: UserInfo? = null,
    val verificationSent: Boolean = false,
    val verificationEmail: String? = null,
    val verificationTimestamp: LocalDateTime? = null,
    val selectedAvatarIndex: Int = 0,
    val allUsers: List<UserInfo> = emptyList(),
    val isLoadingAllUsers: Boolean = false
)

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

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "AuthViewModel"
        private const val VERIFICATION_VALID_MINUTES = 5
        private const val MAX_USER_ID = 100
        private const val BATCH_SIZE = 10
    }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private val authApi = RetrofitClient.authApi
    private val sessionManager = UserSessionManager(application)
    val avatarOptions = AvatarOption.values().toList()

    init {
        checkSavedLoginStatus()
        loadSavedAvatar()
    }

    private fun loadSavedAvatar() {
        try {
            val savedAvatarIndex = sessionManager.getSelectedAvatar()
            _uiState.value = _uiState.value.copy(selectedAvatarIndex = savedAvatarIndex)
            Log.d(TAG, "Loaded saved avatar index: $savedAvatarIndex")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load saved avatar", e)
        }
    }

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

    fun getCurrentAvatar(): AvatarOption {
        return avatarOptions.getOrNull(_uiState.value.selectedAvatarIndex) ?: AvatarOption.DEFAULT
    }

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
                        val userResponse = authApi.getUserInfo(loginResponse.user_id)
                        if (userResponse.isSuccessful) {
                            val userDetailResp = userResponse.body()
                            val userInfo = UserInfo(
                                user_id = loginResponse.user_id,
                                email = userDetailResp?.email ?: email,
                                nickname = userDetailResp?.nickname ?: loginResponse.nickname ?: "",
                                theme = userDetailResp?.theme ?: loginResponse.theme ?: "light"
                            )
                            sessionManager.saveUserSession(userInfo)
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                userInfo = userInfo,
                                errorMessage = null
                            )
                            Log.d(TAG, "Login successful: ${userInfo.nickname}")
                        } else {
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
                        Log.w(TAG, "Login failed: ${loginResponse?.msg}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "账户或密码输入错误"
                        )
                    }
                } else {
                    Log.e(TAG, "Login request failed: ${response.code()}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "账户或密码输入错误"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login exception", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "账户或密码输入错误"
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendVerificationCode(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.ssl.enable", "true")
                    put("mail.smtp.host", "smtp.qq.com")
                    put("mail.smtp.port", "465")
                }
                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication() =
                        PasswordAuthentication("3119349688@qq.com", "okhhgidjogtndfid")
                })
                val code = (100000..999999).random().toString()
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress("3119349688@qq.com"))
                    addRecipient(Message.RecipientType.TO, InternetAddress(email))
                    subject = "RayVita Verification Code"
                    setText("""
                        Dear RayVita User,

                        Your verification code for RayVita is: $code

                        Please enter this code in the app to verify your email. This code is valid for 5 minutes.

                        If you did not request this, please ignore this email.

                        Best regards,
                        The RayVita Team
                        support@rayvita.com
                    """.trimIndent())
                }
                Transport.send(message)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    verificationSent = true,
                    verificationEmail = email,
                    verificationTimestamp = LocalDateTime.now(),
                    errorMessage = null
                )
                Log.d(TAG, "Verification code sent to $email")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send verification code", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to send verification code: ${e.message}"
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isVerificationCodeExpired(): Boolean {
        val timestamp = _uiState.value.verificationTimestamp ?: return true
        val minutes = ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now())
        return minutes >= VERIFICATION_VALID_MINUTES
    }@RequiresApi(Build.VERSION_CODES.O)
    fun register(email: String, code: String, nickname: String, password: String) {
        if (email != _uiState.value.verificationEmail) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email does not match verification email")
            return
        }
        if (isVerificationCodeExpired()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Verification code has expired")
            return
        }
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                val request = RegisterRequest(email, password, nickname)
                val response = authApi.register(request)
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse?.msg == "registered") {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            verificationSent = false,
                            verificationEmail = null,
                            verificationTimestamp = null,
                            errorMessage = null
                        )
                        Log.d(TAG, "Registration successful")
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = registerResponse?.message ?: "Registration failed"
                        )
                    }
                } else {
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun resetPasswordRequest(email: String) {
        sendVerificationCode(email) // Reuse sendVerificationCode for password reset
    }

    fun resetPassword(email: String, newPassword: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                val request = ResetPasswordRequest(email, newPassword)
                val response = authApi.resetPassword(request)
                if (response.isSuccessful) {
                    val resetResponse = response.body()
                    if (resetResponse?.status == "success") {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            verificationSent = false,
                            verificationEmail = null,
                            verificationTimestamp = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = resetResponse?.message ?: "Password reset failed"
                        )
                    }
                } else {
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

    fun logout() {
        try {
            sessionManager.clearUserSession()
            _uiState.value = _uiState.value.copy(isLoggedIn = false, userInfo = null)
            Log.d(TAG, "User logged out")
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed", e)
        }
    }

    fun getAllUsers() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingAllUsers = true)
                val allUsers = mutableListOf<UserInfo>()
                withContext(Dispatchers.IO) {
                    var currentId = 1
                    while (currentId <= MAX_USER_ID) {
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
                        allUsers.addAll(batchResults.filterNotNull())
                        currentId += BATCH_SIZE
                        if (batchResults.all { it == null }) {
                            Log.d(TAG, "No more users found, stopping at ID $currentId")
                            break
                        }
                    }
                }
                _uiState.value = _uiState.value.copy(isLoadingAllUsers = false, allUsers = allUsers)
                Log.d(TAG, "Retrieved ${allUsers.size} users")
            } catch (e: Exception) {
                Log.e(TAG, "Get all users exception", e)
                _uiState.value = _uiState.value.copy(isLoadingAllUsers = false, allUsers = emptyList())
            }
        }
    }

    fun clearAllUsersData() {
        _uiState.value = _uiState.value.copy(allUsers = emptyList())
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}