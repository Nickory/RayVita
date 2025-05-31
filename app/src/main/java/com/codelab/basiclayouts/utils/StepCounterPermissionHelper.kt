package com.codelab.basiclayouts.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Helper class for managing step counter permissions and sensor access
 */
class StepCounterPermissionHelper(private val activity: ComponentActivity) {

    companion object {
        const val ACTIVITY_RECOGNITION_PERMISSION = Manifest.permission.ACTIVITY_RECOGNITION
    }

    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onPermissionGranted?.invoke()
        } else {
            onPermissionDenied?.invoke()
        }
    }

    private var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null

    /**
     * Check if step counter permission is required and granted
     */
    fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                activity,
                ACTIVITY_RECOGNITION_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No permission required for older versions
        }
    }

    /**
     * Request step counter permission if needed
     */
    fun requestPermission(
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        this.onPermissionGranted = onGranted
        this.onPermissionDenied = onDenied

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (isPermissionGranted()) {
                onGranted()
            } else {
                requestPermissionLauncher.launch(ACTIVITY_RECOGNITION_PERMISSION)
            }
        } else {
            onGranted()
        }
    }

    /**
     * Check if step counter sensor is available on the device
     */
    fun isStepCounterAvailable(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
    }

    /**
     * Check if step detector sensor is available on the device
     */
    fun isStepDetectorAvailable(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)
    }
}

/**
 * Extension function for easy permission checking in Activities
 */
fun ComponentActivity.checkStepCounterPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}