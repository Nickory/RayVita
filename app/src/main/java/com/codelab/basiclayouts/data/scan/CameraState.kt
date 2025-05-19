package com.codelab.basiclayouts.data.scan

/**
 * Represents the current state of the camera.
 *
 * @property cameraId ID of the currently active camera.
 * @property hasFaceDetected Whether a face is currently detected in the frame.
 * @property isFrontCamera Whether the front-facing camera is active.
 * @property isCameraActive Whether the camera is currently running.
 * @property flashEnabled Whether the flash is enabled (if supported).
 */
data class CameraState(
    val cameraId: String? = null,
    val hasFaceDetected: Boolean = false,
    val isFrontCamera: Boolean = true,
    val isCameraActive: Boolean = false,
    val flashEnabled: Boolean = false
)

/**
 * Represents available camera IDs.
 *
 * @property front ID of the front-facing camera.
 * @property back ID of the back-facing camera.
 */
data class CameraIds(
    val front: String? = null,
    val back: String? = null
)
