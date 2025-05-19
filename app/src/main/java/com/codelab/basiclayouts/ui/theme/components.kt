//
//package com.codelab.app.ui.components
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.blur
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import com.codelab.app.ui.theme.ButtonCornerRadius
//import com.codelab.app.ui.theme.CardCornerRadius
//
///**
// * Creates a frosted glass effect surface for iOS-style UI
// */
//@Composable
//fun FrostedGlassCard(
//    modifier: Modifier = Modifier,
//    cornerRadius: Dp = CardCornerRadius,
//    color: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
//    blurRadius: Dp = 10.dp,
//    content: @Composable () -> Unit
//) {
//    Box(
//        modifier = modifier
//            .clip(RoundedCornerShape(cornerRadius))
//    ) {
//        // Translucent background with blur
//        Box(
//            modifier = Modifier
//                .matchParentSize()
//                .blur(blurRadius)
//                .background(color)
//        )
//
//        // Content layer
//        Surface(
//            modifier = Modifier.matchParentSize(),
//            color = Color.Transparent,
//            shape = RoundedCornerShape(cornerRadius)
//        ) {
//            content()
//        }
//    }
//}
//
///**
// * iOS-style section header
// */
//@Composable
//fun SectionHeader(
//    title: String,
//    modifier: Modifier = Modifier
//) {
//    Text(
//        text = title,
//        style = MaterialTheme.typography.titleMedium,
//        color = MaterialTheme.colorScheme.primary,
//        modifier = modifier.padding(vertical = 8.dp)
//    )
//}
//
///**
// * iOS-style separator line
// */
//@Composable
//fun Separator(
//    modifier: Modifier = Modifier,
//    color: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
//) {
//    Box(
//        modifier = modifier
//            .fillMaxWidth()
//            .height(1.dp)
//            .background(color)
//    )
//}
//
///**
// * iOS-style rounded button with glass effect
// */
//@Composable
//fun GlassButton(
//    text: String,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    enabled: Boolean = true,
//    isPrimary: Boolean = true
//) {
//    val buttonColor = when {
//        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
//        isPrimary -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
//        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
//    }
//
//    val textColor = when {
//        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
//        isPrimary -> MaterialTheme.colorScheme.onPrimary
//        else -> MaterialTheme.colorScheme.onSurfaceVariant
//    }
//
//    androidx.compose.material3.Button(
//        onClick = onClick,
//        enabled = enabled,
//        shape = RoundedCornerShape(ButtonCornerRadius),
//        modifier = modifier.height(50.dp)
//    ) {
//        // Frosted glass background
//        Box(
//            modifier = Modifier
//
//                .blur(5.dp)
//                .background(buttonColor)
//        )
//
//        Text(
//            text = text,
//            style = MaterialTheme.typography.titleSmall,
//            color = textColor,
//            modifier = Modifier.padding(horizontal = 8.dp)
//        )
//    }
//}
//
///**
// * Creates a gradient overlay effect, useful for backgrounds or card elements
// */
//@Composable
//fun GradientOverlay(
//    startColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
//    endColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
//    modifier: Modifier = Modifier
//) {
//    Box(
//        modifier = modifier
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(startColor, endColor)
//                )
//            )
//    )
//}
//
///**
// * Creates a 3D depth effect for card elements
// */
//@Composable
//fun DepthCard(
//    modifier: Modifier = Modifier,
//    elevation: Dp = 4.dp,
//    cornerRadius: Dp = CardCornerRadius,
//    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
//    content: @Composable () -> Unit
//) {
//    Box(
//        modifier = modifier
//            .graphicsLayer {
//                shadowElevation = elevation.toPx()
//                shape = RoundedCornerShape(cornerRadius)
//                clip = true
//            }
//            .background(
//                color = backgroundColor,
//                shape = RoundedCornerShape(cornerRadius)
//            )
//    ) {
//        content()
//    }
//}
//
///**
// * iOS-style section with a title and content
// */
//@Composable
//fun Section(
//    title: String,
//    modifier: Modifier = Modifier,
//    content: @Composable () -> Unit
//) {
//    Box(modifier = modifier) {
//        Column(
//            modifier = Modifier.padding(vertical = 8.dp)
//        ) {
//            SectionHeader(title = title)
//            content()
//        }
//    }
//}