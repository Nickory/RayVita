package com.codelab.basiclayouts.ui.screen.insight

/**
 * Represents a single message in the AI conversation
 */

import androidx.compose.runtime.Immutable

@Immutable
data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: String,
    val isHelpful: Boolean = false,
    val isNotHelpful: Boolean = false
)