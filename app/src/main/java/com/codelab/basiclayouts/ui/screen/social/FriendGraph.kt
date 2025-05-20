package com.codelab.basiclayouts.ui.social

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.model.Friend
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Node(
    val id: Int,
    val name: String,
    val avatar: String,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val isCurrentUser: Boolean = false
)

@Composable
fun FriendGraph(
    friends: List<Friend>,
    currentUserId: Int,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    // Create nodes for friends and current user
    val nodes = remember(friends, currentUserId, size) {
        if (size.width == 0 || size.height == 0) return@remember emptyList()

        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = minOf(centerX, centerY) * 0.8f

        val currentUserNode = Node(
            id = currentUserId,
            name = "You",
            avatar = "👤",
            x = centerX,
            y = centerY,
            isCurrentUser = true
        )

        val friendNodes = friends.mapIndexed { index, friend ->
            val angle = 2 * Math.PI * index / friends.size
            Node(
                id = friend.user_id,
                name = friend.nickname,
                avatar = when (friend.avatar_index) {
                    1 -> "😊"
                    2 -> "😎"
                    3 -> "⭐"
                    4 -> "❤️"
                    5 -> "👍"
                    6 -> "🚀"
                    else -> "👤"
                },
                x = (centerX + radius * cos(angle).toFloat()),
                y = (centerY + radius * sin(angle).toFloat())
            )
        }

        listOf(currentUserNode) + friendNodes
    }

    // Simulate physics for node positions
    var draggedNodeId by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    // Force-directed layout algorithm
    LaunchedEffect(nodes) {
        if (nodes.isEmpty()) return@LaunchedEffect

        val iterations = 100
        for (i in 0 until iterations) {
            // Skip physics if a node is being dragged
            if (draggedNodeId == null) {
                applyForces(nodes, size.width.toFloat(), size.height.toFloat())
            }
            delay(16) // ~60 FPS
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { size = it }
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // Find the closest node to the touch point
                        val nodeRadius = with(density) { 40.dp.toPx() }
                        draggedNodeId = nodes.find { node ->
                            val distance = sqrt(
                                (node.x - offset.x).pow(2) + (node.y - offset.y).pow(2)
                            )
                            distance <= nodeRadius
                        }?.id

                        if (draggedNodeId != null) {
                            val node = nodes.find { it.id == draggedNodeId }
                            if (node != null) {
                                dragOffset = Offset(offset.x - node.x, offset.y - node.y)
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        val node = nodes.find { it.id == draggedNodeId }
                        if (node != null) {
                            node.x = change.position.x - dragOffset.x
                            node.y = change.position.y - dragOffset.y

                            // Keep node within bounds
                            node.x = node.x.coerceIn(0f, size.width.toFloat())
                            node.y = node.y.coerceIn(0f, size.height.toFloat())
                        }
                    },
                    onDragEnd = {
                        draggedNodeId = null
                    },
                    onDragCancel = {
                        draggedNodeId = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw edges between nodes
            for (i in 1 until nodes.size) {
                val source = nodes[0] // Current user is always at index 0
                val target = nodes[i]

                drawLine(
//                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    color = Color(0xFF6B5C4D) ,
                    start = Offset(source.x, source.y),
                    end = Offset(target.x, target.y),
                    strokeWidth = 2f
                )
            }
        }

        // Draw nodes with avatars
        for (node in nodes) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text(
                    text = node.avatar,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            with(density) { node.x.toDp() - 20.dp },
                            with(density) { node.y.toDp() - 20.dp }
                        )
                )

                Text(
                    text = node.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (node.isCurrentUser)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            with(density) { node.x.toDp() - 20.dp },
                            with(density) { node.y.toDp() + 15.dp }
                        )
                )
            }
        }
    }
}

// Physics simulation functions
private fun applyForces(nodes: List<Node>, width: Float, height: Float) {
    // Constants
    val repulsionStrength = 1000f
    val attractionStrength = 0.05f
    val centeringStrength = 0.01f

    // Apply forces
    for (i in nodes.indices) {
        val node1 = nodes[i]

        // Apply repulsion forces between nodes
        for (j in nodes.indices) {
            if (i == j) continue

            val node2 = nodes[j]
            val dx = node1.x - node2.x
            val dy = node1.y - node2.y
            val distance = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)

            // Repulsion force (inverse square law)
            val force = repulsionStrength / (distance * distance)
            val forceX = dx / distance * force
            val forceY = dy / distance * force

            node1.vx += forceX
            node1.vy += forceY
        }

        // Apply attraction to center for non-current user
        if (!node1.isCurrentUser) {
            val centerX = width / 2
            val centerY = height / 2
            val dx = centerX - node1.x
            val dy = centerY - node1.y
            node1.vx += dx * centeringStrength
            node1.vy += dy * centeringStrength
        }

        // Apply attraction to edges (current user to friends)
        if (node1.isCurrentUser) {
            for (j in 1 until nodes.size) {
                val node2 = nodes[j]
                val dx = node2.x - node1.x
                val dy = node2.y - node1.y
                val distance = sqrt(dx * dx + dy * dy)
                node1.vx += dx * attractionStrength
                node1.vy += dy * attractionStrength
            }
        } else {
            // Apply attraction from friends to current user
            val currentUser = nodes.first { it.isCurrentUser }
            val dx = currentUser.x - node1.x
            val dy = currentUser.y - node1.y
            val distance = sqrt(dx * dx + dy * dy)
            node1.vx += dx * attractionStrength
            node1.vy += dy * attractionStrength
        }

        // Apply velocity and damping
        val damping = 0.9f
        node1.vx *= damping
        node1.vy *= damping
        node1.x += node1.vx
        node1.y += node1.vy

        // Keep nodes within bounds
        val padding = 40
        node1.x = node1.x.coerceIn(padding.toFloat(), width - padding)
        node1.y = node1.y.coerceIn(padding.toFloat(), height - padding)
    }
}