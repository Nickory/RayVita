package com.codelab.basiclayouts.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.codelab.basiclayouts.model.UserInfo
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * User session manager with avatar support
 * Handles user login state persistence and avatar selection
 */
class UserSessionManager(context: Context) {

    companion object {
        private const val TAG = "UserSessionManager"
        private const val PREF_NAME = "RayVitaUserSession"
        private const val KEY_USER_INFO = "user_info"
        private const val KEY_EXPIRY_DATE = "expiry_date"
        private const val KEY_SELECTED_AVATAR = "selected_avatar_index"
        private const val SESSION_EXPIRY_DAYS = 7 // Login session lasts 7 days
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Save user session information
     */
    fun saveUserSession(userInfo: UserInfo) {
        try {
            val editor = sharedPreferences.edit()

            // Save user info
            val userJson = gson.toJson(userInfo)
            editor.putString(KEY_USER_INFO, userJson)

            // Set expiry date (current time + 7 days)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, SESSION_EXPIRY_DAYS)
            val expiryDate = dateFormat.format(calendar.time)
            editor.putString(KEY_EXPIRY_DATE, expiryDate)

            editor.apply()

            Log.d(TAG, "User session saved, expires: $expiryDate")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user session", e)
        }
    }

    /**
     * Get current logged-in user information
     * Returns null if not logged in or session expired
     */
    fun getUserSession(): UserInfo? {
        try {
            // Check if user info exists
            val userJson = sharedPreferences.getString(KEY_USER_INFO, null) ?: return null

            // Check if session expired
            val expiryDateStr = sharedPreferences.getString(KEY_EXPIRY_DATE, null) ?: return null
            val expiryDate = dateFormat.parse(expiryDateStr)

            if (expiryDate != null && expiryDate.before(Date())) {
                Log.d(TAG, "User session expired")
                clearUserSession()
                return null
            }

            // Parse user info
            return gson.fromJson(userJson, UserInfo::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user session", e)
            return null
        }
    }

    /**
     * Clear user session
     */
    fun clearUserSession() {
        try {
            val editor = sharedPreferences.edit()
            editor.remove(KEY_USER_INFO)
            editor.remove(KEY_EXPIRY_DATE)
            editor.apply()
            Log.d(TAG, "User session cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear user session", e)
        }
    }

    /**
     * Refresh session expiry time
     * Call this method on app start or user interaction
     */
    fun refreshSessionExpiry() {
        val userJson = sharedPreferences.getString(KEY_USER_INFO, null) ?: return

        try {
            val editor = sharedPreferences.edit()

            // Set new expiry date
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, SESSION_EXPIRY_DAYS)
            val expiryDate = dateFormat.format(calendar.time)
            editor.putString(KEY_EXPIRY_DATE, expiryDate)

            editor.apply()

            Log.d(TAG, "Session expiry updated: $expiryDate")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update session expiry", e)
        }
    }

    /**
     * Save selected avatar index
     */
    fun saveSelectedAvatar(avatarIndex: Int) {
        try {
            sharedPreferences.edit()
                .putInt(KEY_SELECTED_AVATAR, avatarIndex)
                .apply()
            Log.d(TAG, "Avatar index saved: $avatarIndex")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save avatar index", e)
        }
    }

    /**
     * Get selected avatar index
     * Returns 0 (default) if not set
     */
    fun getSelectedAvatar(): Int {
        return try {
            sharedPreferences.getInt(KEY_SELECTED_AVATAR, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get avatar index", e)
            0 // Return default avatar index
        }
    }

    /**
     * Clear all data including avatar
     */
    fun clearAllData() {
        try {
            sharedPreferences.edit().clear().apply()
            Log.d(TAG, "All data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all data", e)
        }
    }
}