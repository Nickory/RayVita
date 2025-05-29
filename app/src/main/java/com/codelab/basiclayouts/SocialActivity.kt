package com.codelab.basiclayouts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.codelab.basiclayouts.ui.theme.RayVitaTheme
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * User data model representing a person in the social network
 * @param id Unique identifier for the user
 * @param name Display name of the user
 * @param avatarUrl URL to the user's profile image (can be null)
 * @param connections List of user's connections/friends
 * @param level Level in the network (0 for main user, 1 for direct friends, etc.)
 */
data class User(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val connections: List<Connection>,
    val level: Int = 0 // Level in the network (0 for main user, 1 for direct friends, etc.)
)

/**
 * Connection data model representing a link between users
 * @param userId Unique identifier for the connected user
 * @param name Display name of the connected user
 * @param avatarUrl URL to the connected user's profile image (can be null)
 * @param strength Connection strength, 0.0 - 1.0 (higher value = stronger connection)
 * @param level Level in the network relative to the main user
 * @param connections Friends of this connection (for multi-level network)
 */
data class Connection(
    val userId: String,
    val name: String,
    val avatarUrl: String?,
    val strength: Float, // Connection strength, 0.0 - 1.0
    val level: Int = 1,  // Level in the network relative to the main user
    val connections: List<Connection> = emptyList() // Friends of this connection
)

/**
 * Main activity class for the social network visualization
 * Initializes the app and sets up the main screen
 */
class SocialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RayVitaTheme {
                // Generate test data for demonstration
                val testUser = generateTestUser()

                // Set up the main social network screen with callback handlers
                SocialNetworkScreen(
                    currentUser = testUser,
                    onUserClick = { userId ->
                        // Handle user click
                        // TODO: Implement user profile view or detail page
                        // You can start a new activity or show a detail panel here
                    },
                    onDelete = { userId ->
                        // Handle friend deletion
                        // TODO: Implement connection to database to remove friendship
                        // Example: socialNetworkRepository.removeFriend(currentUserId, userId)
                    },
                    onChallenge = { userId ->
                        // Handle friend challenge
                        // TODO: Implement challenge functionality
                        // Example: challengeService.createChallenge(currentUserId, userId)
                    },
                    onAddFriend = { userId ->
                        // Handle add friend
                        // TODO: Implement friend request functionality
                        // Example: socialNetworkRepository.sendFriendRequest(currentUserId, userId)
                    },
                    onRecommend = { fromId, toId ->
                        // Handle recommendation
                        // TODO: Implement friend recommendation
                        // Example: recommendationService.createRecommendation(fromId, toId)
                    },
                    onSearch = { query ->
                        // Handle search
                        // TODO: Implement user search functionality
                        // Example: userRepository.searchUsers(query)
                    },
                    onBack = {
                        // Handle back navigation
                        finish()
                    }
                )
            }
        }
    }
}

/**
 * Generates test user data with a multi-level network for demonstration purposes
 * In a real application, this would be replaced with data from a database or API
 * @return A User object with sample connections
 */
fun generateTestUser(): User {
    val avatarUrls = listOf(
        "https://randomuser.me/api/portraits/men/1.jpg",
        "https://randomuser.me/api/portraits/women/2.jpg",
        "https://randomuser.me/api/portraits/men/3.jpg",
        "https://randomuser.me/api/portraits/women/4.jpg",
        "https://randomuser.me/api/portraits/men/5.jpg",
        "https://randomuser.me/api/portraits/women/6.jpg",
        "https://randomuser.me/api/portraits/men/7.jpg",
        "https://randomuser.me/api/portraits/women/8.jpg"
    )

    // Create level 2 connections (friends of friends)
    val createLevel2Connections = { friendIndex: Int ->
        List(2 + friendIndex % 3) { index ->
            Connection(
                userId = "friend${friendIndex}_subfriend$index",
                name = "Friend of ${friendIndex + 1}",
                avatarUrl = avatarUrls[(friendIndex + index) % avatarUrls.size],
                strength = 0.2f + (index / 15f),
                level = 2
            )
        }
    }

    // Create level 1 connections (direct friends)
    val connections = List(6) { index ->
        Connection(
            userId = "friend$index",
            name = "Friend ${index + 1}",
            avatarUrl = avatarUrls[index % avatarUrls.size],
            strength = 0.3f + (index / 10f),
            level = 1,
            connections = createLevel2Connections(index)
        )
    }

    // Create level 3 connections (friends of friends of friends) for the first two friends
    val level1Friend0 = connections[0]
    val level1Friend1 = connections[1]

    // Add level 3 connections to the first two level 2 connections of the first friend
    val updatedLevel2Connections0 = level1Friend0.connections.mapIndexed { idx, level2Conn ->
        if (idx < 2) {
            level2Conn.copy(connections = List(2) { i ->
                Connection(
                    userId = "${level2Conn.userId}_subfriend$i",
                    name = "F${level2Conn.userId.last()}'s Friend $i",
                    avatarUrl = avatarUrls[(i + 10) % avatarUrls.size],
                    strength = 0.1f + (i / 20f),
                    level = 3
                )
            })
        } else level2Conn
    }

    // Update the connections for the enhanced network structure
    val updatedConnections = connections.toMutableList()
    updatedConnections[0] = level1Friend0.copy(connections = updatedLevel2Connections0)

    return User(
        id = "me",
        name = "You",
        avatarUrl = "https://randomuser.me/api/portraits/men/10.jpg",
        connections = updatedConnections,
        level = 0
    )
}

/**
 * Main screen composable for the social network visualization
 * @param currentUser The current logged-in user with their connection network
 * @param onUserClick Callback for when a user node is clicked
 * @param onDelete Callback for when a user connection is deleted
 * @param onChallenge Callback for when a challenge is initiated
 * @param onAddFriend Callback for when a friend request is sent
 * @param onRecommend Callback for when a recommendation is made
 * @param onSearch Callback for when a search is performed
 * @param onBack Callback for when the back button is pressed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialNetworkScreen(
    currentUser: User,
    onUserClick: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onChallenge: (String) -> Unit = {},
    onAddFriend: (String) -> Unit = {},
    onRecommend: (String, String) -> Unit = { _, _ -> },
    onSearch: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    // State management
    var networkOffsetX by remember { mutableStateOf(0f) }
    var networkOffsetY by remember { mutableStateOf(0f) }
    var networkScale by remember { mutableStateOf(1f) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var draggedUserId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var showDialog by remember { mutableStateOf<String?>(null) }
    var boxSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    // Track all node positions for proper connection rendering
    var nodePositions by remember { mutableStateOf(mapOf<String, Offset>()) }

    // Remember the last successfully dropped connection to animate it
    var lastRecommendation by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Animation
    val networkScaleAnimated by animateFloatAsState(
        targetValue = networkScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "NetworkScale"
    )

    // Screen size adaptation
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Node size calculation - adjusted for better proportions
    val mainNodeSize = min(screenWidth, screenHeight) * 0.15f
    val level1NodeSize = mainNodeSize * 0.65f
    val level2NodeSize = level1NodeSize * 0.65f
    val level3NodeSize = level2NodeSize * 0.65f

    // Orbit radius calculation - adjusted for better spacing
    val level1Radius = min(screenWidth, screenHeight) * 0.35f
    val level2Radius = level1Radius * 1.5f
    val level3Radius = level2Radius * 1.3f

    Column(modifier = Modifier.fillMaxSize()) {
        // Top navigation bar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "Social Network",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { showSearch = !showSearch }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        // Network view
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (draggedUserId == null) {
                                // Only move the entire network when not dragging a user node
                                networkOffsetX += dragAmount.x
                                networkOffsetY += dragAmount.y
                            }
                        }
                    )
                }
                .onSizeChanged { boxSize = it.toSize() }
        ) {
            if (boxSize != androidx.compose.ui.geometry.Size.Zero) {
                val center = Offset(boxSize.width / 2 + networkOffsetX, boxSize.height / 2 + networkOffsetY)

                // Update node positions map with main user
                val updatedPositions = nodePositions.toMutableMap()
                updatedPositions[currentUser.id] = center

                // Calculate level 1 friend positions
                val level1Friends = currentUser.connections
                val level1AngleStep = if (level1Friends.isNotEmpty()) 360f / level1Friends.size else 0f

                val level1Offsets = level1Friends.mapIndexed { index, connection ->
                    val angle = level1AngleStep * index
                    val radiusInPx = with(LocalDensity.current) { level1Radius.toPx() }
                    val offset = Offset(
                        center.x + radiusInPx * cos(Math.toRadians(angle.toDouble())).toFloat(),
                        center.y + radiusInPx * sin(Math.toRadians(angle.toDouble())).toFloat()
                    )

                    // Store level 1 position
                    updatedPositions[connection.userId] = offset

                    offset
                }

                // Create a flat list of all connections to draw
                val allConnections = mutableListOf<Triple<Connection, String, String>>()

                // Add level 1 connections to the list (connecting to main user)
                level1Friends.forEach { connection ->
                    allConnections.add(Triple(connection, currentUser.id, connection.userId))
                }

                // Calculate level 2 friend positions
                level1Friends.forEachIndexed { parentIndex, parentConnection ->
                    val level2Friends = parentConnection.connections

                    if (level2Friends.isNotEmpty()) {
                        val level2AngleStep = 60f / level2Friends.size
                        val baseAngle = level1AngleStep * parentIndex

                        level2Friends.forEachIndexed { index, connection ->
                            val angle = baseAngle - 30f + level2AngleStep * index
                            val radiusInPx = with(LocalDensity.current) { level2Radius.toPx() }
                            val offset = Offset(
                                center.x + radiusInPx * cos(Math.toRadians(angle.toDouble())).toFloat(),
                                center.y + radiusInPx * sin(Math.toRadians(angle.toDouble())).toFloat()
                            )

                            // Store level 2 position
                            updatedPositions[connection.userId] = offset

                            // Add level 2 connection to parent
                            allConnections.add(Triple(connection, parentConnection.userId, connection.userId))

                            // Calculate level 3 positions
                            connection.connections.forEachIndexed { l3Index, l3Connection ->
                                val l3AngleStep = 30f / (connection.connections.size.coerceAtLeast(1))
                                val l3BaseAngle = angle - 15f
                                val l3Angle = l3BaseAngle + l3AngleStep * l3Index

                                val l3RadiusInPx = with(LocalDensity.current) { level3Radius.toPx() }
                                val l3Offset = Offset(
                                    center.x + l3RadiusInPx * cos(Math.toRadians(l3Angle.toDouble())).toFloat(),
                                    center.y + l3RadiusInPx * sin(Math.toRadians(l3Angle.toDouble())).toFloat()
                                )

                                // Store level 3 position
                                updatedPositions[l3Connection.userId] = l3Offset

                                // Add level 3 connection
                                allConnections.add(Triple(l3Connection, connection.userId, l3Connection.userId))
                            }
                        }
                    }
                }

                // Update the node positions state
                nodePositions = updatedPositions

                // Draw all connection lines with appropriate transformations
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(networkScaleAnimated)
                ) {
                    allConnections.forEach { (connection, startId, endId) ->
                        val startOffset = nodePositions[startId] ?: return@forEach
                        var endOffset = nodePositions[endId] ?: return@forEach

                        // Apply drag offset if this node is being dragged
                        if (draggedUserId == endId) {
                            endOffset += dragOffset
                        }

                        // Select color based on connection strength
                        val connectionColor = when {
                            connection.strength > 0.8f -> Color(0xFF4CAF50) // Strong - Green
                            connection.strength > 0.5f -> Color(0xFF2196F3) // Medium - Blue
                            else -> Color(0xFFFF9800) // Weak - Orange
                        }

                        // Calculate alpha based on connection level
                        val alpha = when (connection.level) {
                            1 -> 0.8f
                            2 -> 0.6f
                            3 -> 0.4f
                            else -> 0.8f
                        }

                        // Calculate line thickness based on level and strength
                        val lineThickness = when (connection.level) {
                            1 -> 2f + 6f * connection.strength
                            2 -> 1.5f + 4f * connection.strength
                            3 -> 1f + 2f * connection.strength
                            else -> 2f + 6f * connection.strength
                        }

                        // Check if this is a recently recommended connection
                        val isRecentRecommendation = lastRecommendation?.let {
                            (it.first == startId && it.second == endId) ||
                                    (it.first == endId && it.second == startId)
                        } ?: false

                        // Draw highlight for recent recommendations
                        if (isRecentRecommendation) {
                            // Draw glowing highlight for recommended connection
                            drawLine(
                                color = Color(0xFFFF5722).copy(alpha = 0.5f),
                                start = startOffset,
                                end = endOffset,
                                strokeWidth = lineThickness * 3,
                                cap = StrokeCap.Round
                            )
                        }

                        // Draw connection line
                        drawLine(
                            color = connectionColor.copy(alpha = alpha),
                            start = startOffset,
                            end = endOffset,
                            strokeWidth = lineThickness,
                            cap = StrokeCap.Round
                        )

                        // Add directional indicator for connections
                        val directionOffset = Offset(
                            (endOffset.x - startOffset.x) * 0.7f + startOffset.x,
                            (endOffset.y - startOffset.y) * 0.7f + startOffset.y
                        )

                        if (connection.level <= 2) {  // Only for level 1 and 2 connections
                            drawCircle(
                                color = connectionColor.copy(alpha = alpha + 0.2f),
                                radius = lineThickness + 1f,
                                center = directionOffset
                            )
                        }
                    }
                }

                // Render nodes in order from back to front (level 3 -> level 2 -> level 1 -> main)

                // LEVEL 3 (furthest back)
                level1Friends.forEach { parentConnection ->
                    parentConnection.connections.forEach { level2Connection ->
                        level2Connection.connections.forEach { level3Connection ->
                            val nodePos = nodePositions[level3Connection.userId]
                            if (nodePos != null) {
                                val finalOffset = if (draggedUserId == level3Connection.userId) {
                                    nodePos + dragOffset
                                } else {
                                    nodePos
                                }

                                UserNode(
                                    user = level3Connection,
                                    offset = finalOffset,
                                    size = level3NodeSize,
                                    isMainUser = false,
                                    isBeingDragged = draggedUserId == level3Connection.userId,
                                    onTap = { onUserClick(level3Connection.userId) },
                                    onLongPress = { showDialog = level3Connection.userId },
                                    onDragStart = { draggedUserId = level3Connection.userId },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOffset += amount
                                    },
                                    onDragEnd = {
                                        // Check for possible connections (simplified for level 3)
                                        draggedUserId = null
                                        dragOffset = Offset.Zero
                                    },
                                    scale = networkScaleAnimated,
                                    opacity = 0.7f
                                )
                            }
                        }
                    }
                }

                // LEVEL 2 (middle distance)
                level1Friends.forEach { parentConnection ->
                    parentConnection.connections.forEach { level2Connection ->
                        val nodePos = nodePositions[level2Connection.userId]
                        if (nodePos != null) {
                            val finalOffset = if (draggedUserId == level2Connection.userId) {
                                nodePos + dragOffset
                            } else {
                                nodePos
                            }

                            val onDragEndFunc = @androidx.compose.runtime.Composable {
                                // Check if dragged to another node to create recommendation
                                if (draggedUserId != null) {
                                    // Find closest nodes for possible connection
                                    var closestNodeId: String? = null
                                    var minDistance = Float.MAX_VALUE

                                    // Check level 1 and level 2 nodes for potential connections
                                    nodePositions.forEach { (nodeId, nodeOffset) ->
                                        if (nodeId != level2Connection.userId) {
                                            val targetLevel = when {
                                                nodeId == currentUser.id -> 0
                                                currentUser.connections.any { it.userId == nodeId } -> 1
                                                else -> 2
                                            }

                                            // Only allow connections to level 0 or 1
                                            if (targetLevel <= 1) {
                                                val distance = sqrt(
                                                    (finalOffset.x - nodeOffset.x).pow(2) +
                                                            (finalOffset.y - nodeOffset.y).pow(2)
                                                )

                                                // Node size threshold based on target level
                                                val nodeSize = if (targetLevel == 0) mainNodeSize else level1NodeSize
                                                val thresholdInPx = with(LocalDensity.current) { (nodeSize * 1.2f).toPx() }

                                                if (distance < thresholdInPx && distance < minDistance) {
                                                    minDistance = distance
                                                    closestNodeId = nodeId
                                                }
                                            }
                                        }
                                    }

                                    // If found a close node, trigger recommendation
                                    closestNodeId?.let {
                                        onRecommend(level2Connection.userId, it)
                                        lastRecommendation = Pair(level2Connection.userId, it)
                                    }
                                }

                                // Reset drag state
                                draggedUserId = null
                                dragOffset = Offset.Zero
                            }

                            UserNode(
                                user = level2Connection,
                                offset = finalOffset,
                                size = level2NodeSize,
                                isMainUser = false,
                                isBeingDragged = draggedUserId == level2Connection.userId,
                                onTap = { onUserClick(level2Connection.userId) },
                                onLongPress = { showDialog = level2Connection.userId },
                                onDragStart = { draggedUserId = level2Connection.userId },
                                onDrag = { change, amount ->
                                    change.consume()
                                    dragOffset += amount
                                },

                                scale = networkScaleAnimated,
                                opacity = 0.85f
                            )
                        }
                    }
                }

                // LEVEL 1 (closest to main)
                level1Friends.forEach { connection ->
                    val nodePos = nodePositions[connection.userId]
                    if (nodePos != null) {
                        val finalOffset = if (draggedUserId == connection.userId) {
                            nodePos + dragOffset
                        } else {
                            nodePos
                        }

                        val onDragEndFunc = @androidx.compose.runtime.Composable {
                            // Check if dragged to another node to create recommendation
                            if (draggedUserId != null) {
                                var closestNodeId: String? = null
                                var minDistance = Float.MAX_VALUE

                                // Check all level 1 nodes for potential connections
                                level1Friends.forEach { target ->
                                    if (target.userId != connection.userId) {
                                        val targetPos = nodePositions[target.userId] ?: return@forEach
                                        val distance = sqrt(
                                            (finalOffset.x - targetPos.x).pow(2) +
                                                    (finalOffset.y - targetPos.y).pow(2)
                                        )

                                        val thresholdInPx = with(LocalDensity.current) { (level1NodeSize * 1.3f).toPx() }
                                        if (distance < thresholdInPx && distance < minDistance) {
                                            minDistance = distance
                                            closestNodeId = target.userId
                                        }
                                    }
                                }

                                // Also check main user for connection
                                val distanceToMain = sqrt(
                                    (finalOffset.x - center.x).pow(2) +
                                            (finalOffset.y - center.y).pow(2)
                                )

                                val mainThresholdInPx = with(LocalDensity.current) { (mainNodeSize * 1.3f).toPx() }
                                if (distanceToMain < mainThresholdInPx && distanceToMain < minDistance) {
                                    minDistance = distanceToMain
                                    closestNodeId = currentUser.id
                                }

                                // If found a close node, trigger recommendation
                                closestNodeId?.let {
                                    onRecommend(connection.userId, it)
                                    lastRecommendation = Pair(connection.userId, it)
                                }
                            }

                            // Reset drag state
                            draggedUserId = null
                            dragOffset = Offset.Zero
                        }

                        UserNode(
                            user = connection,
                            offset = finalOffset,
                            size = level1NodeSize,
                            isMainUser = false,
                            isBeingDragged = draggedUserId == connection.userId,
                            onTap = { onUserClick(connection.userId) },
                            onLongPress = { showDialog = connection.userId },
                            onDragStart = { draggedUserId = connection.userId },
                            onDrag = { change, amount ->
                                change.consume()
                                dragOffset += amount
                            },
                            scale = networkScaleAnimated,
                            opacity = 1.0f
                        )
                    }
                }

                // Main user node (on top)
                UserNode(
                    user = currentUser,
                    offset = center,
                    size = mainNodeSize,
                    isMainUser = true,
                    onTap = { onUserClick(currentUser.id) },
                    onLongPress = { showDialog = currentUser.id },
                    scale = networkScaleAnimated
                )
            }

            // Show action dialog
            showDialog?.let { userId ->
                // Determine user type
                val isCurrentUser = userId == currentUser.id
                val isLevel1User = currentUser.connections.any { it.userId == userId }
                val isLevel2User = currentUser.connections.any { parent ->
                    parent.connections.any { it.userId == userId }
                }

                UserActionDialog(
                    userId = userId,
                    isCurrentUser = isCurrentUser,
                    isLevel1User = isLevel1User,
                    isLevel2User = isLevel2User,
                    onDelete = {
                        onDelete(userId)
                        showDialog = null
                    },
                    onChallenge = {
                        onChallenge(userId)
                        showDialog = null
                    },
                    onAddFriend = {
                        onAddFriend(userId)
                        showDialog = null
                    },
                    onDismiss = { showDialog = null }
                )
            }

            // Bottom control bar
            ControlBar(
                showSearch = showSearch,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchSubmit = {
                    onSearch(searchQuery)
                    showSearch = false
                },
                onToggleSearch = { showSearch = !showSearch },
                onZoomIn = { networkScale = min(networkScale * 1.2f, 2f) },
                onZoomOut = { networkScale = max(networkScale * 0.8f, 0.5f) },
                onResetView = {
                    networkOffsetX = 0f
                    networkOffsetY = 0f
                    networkScale = 1f
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * User node composable representing a single user in the network
 * @param user The user or connection to display
 * @param offset The position of the node
 * @param size The size of the node
 * @param isMainUser Whether this is the main user node
 * @param isBeingDragged Whether this node is currently being dragged
 * @param onTap Callback for when the node is tapped
 * @param onLongPress Callback for when the node is long-pressed
 * @param onDragStart Callback for when dragging begins
 * @param onDrag Callback for during dragging
 * @param onDragEnd Callback for when dragging ends
 * @param scale Scale factor for the node
 * @param opacity Opacity of the node
 */
@Composable
fun UserNode(
    user: Any, // Can be User or Connection
    offset: Offset,
    size: androidx.compose.ui.unit.Dp,
    isMainUser: Boolean,
    isBeingDragged: Boolean = false,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onDragStart: (() -> Unit)? = null,
    onDrag: ((change: androidx.compose.ui.input.pointer.PointerInputChange, amount: Offset) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    scale: Float = 1f,
    opacity: Float = 1.0f
) {
    // Extract user information
    val userId = when(user) {
        is User -> user.id
        is Connection -> user.userId
        else -> ""
    }

    val name = when(user) {
        is User -> user.name
        is Connection -> user.name
        else -> ""
    }

    val avatarUrl = when(user) {
        is User -> user.avatarUrl
        is Connection -> user.avatarUrl
        else -> null
    }

    val strength = when(user) {
        is Connection -> user.strength
        else -> 1.0f
    }

    val level = when(user) {
        is User -> user.level
        is Connection -> user.level
        else -> 0
    }

    // Animation state
    var isPressed by remember { mutableStateOf(false) }
    val nodeScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else if (isBeingDragged) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "NodeScale"
    )

    val pulseAnimation = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Node color
    val nodeColor = if (isMainUser) {
        MaterialTheme.colorScheme.primary
    } else {
        when {
            strength > 0.8f -> Color(0xFF4CAF50) // Strong - Green
            strength > 0.5f -> Color(0xFF2196F3) // Medium - Blue
            else -> Color(0xFFFF9800) // Weak - Orange
        }
    }

    var modifier = Modifier
        .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
        .size(size)
        .scale(nodeScale * scale * if (isMainUser) pulseScale else 1f)
        .shadow(if (isBeingDragged) 12.dp else 6.dp, CircleShape)
        .clip(CircleShape)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    nodeColor.copy(alpha = opacity),
                    nodeColor.copy(alpha = opacity * 0.8f)
                )
            )
        )
        .border(
            width = if (isBeingDragged) 3.dp else 1.5.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = opacity * 0.7f),
            shape = CircleShape
        )
        .pointerInput(userId) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = { onTap() },
                onLongPress = { onLongPress() }
            )
        }

    // If draggable, add drag gesture
    if (onDragStart != null && onDrag != null && onDragEnd != null) {
        val onDragEndFunction = onDragEnd // Store reference to avoid calling composable function
        modifier = modifier.pointerInput(userId) {
            detectDragGestures(
                onDragStart = {
                    isPressed = true
                    onDragStart()
                },
                onDrag = onDrag,
                onDragEnd = {
                    isPressed = false
                    onDragEndFunction()
                },
                onDragCancel = {
                    isPressed = false
                    onDragEndFunction()
                }
            )
        }
    }

    Box(modifier = modifier) {
        // User avatar
        if (avatarUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isMainUser) 4.dp else 3.dp)
                    .clip(CircleShape),
                alpha = opacity
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = name,
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = opacity),
                modifier = Modifier
                    .size(size / 2)
                    .align(Alignment.Center)
            )
        }

        // Connection strength indicator (only for friend nodes)
        if (!isMainUser && !isBeingDragged) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(size / 4)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = opacity * 0.8f))
                    .border(1.dp, nodeColor.copy(alpha = opacity), CircleShape)
            ) {
                // Show 1-5 stars to indicate connection strength
                val stars = (strength * 5).toInt().coerceIn(1, 5)
                Text(
                    text = "★".repeat(stars),
                    color = nodeColor.copy(alpha = opacity),
                    fontSize = (size.value / 10).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // User name label with improved visibility
        if (!isBeingDragged) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        nodeColor.copy(alpha = opacity * 0.85f)
                    )
                    .padding(if (isMainUser) 4.dp else 2.dp)
            ) {
                Text(
                    text = name,
                    color = Color.White.copy(alpha = opacity),
                    textAlign = TextAlign.Center,
                    fontSize = if (isMainUser) (size.value / 8).sp else (size.value / 10).sp,
                    fontWeight = if (isMainUser) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Drag indicator text
        if (isBeingDragged) {
            Text(
                text = "Drag to connect",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = (size.value / 12).sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-size * 0.6f))
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Level indicator for friends of friends with improved visibility
        if (level > 1 && !isBeingDragged) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(size / 5)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = opacity * 0.9f))
                    .border(1.dp, nodeColor.copy(alpha = opacity), CircleShape)
            ) {
                Text(
                    text = "$level°",
                    color = nodeColor.copy(alpha = opacity),
                    fontSize = (size.value / 10).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * Dialog for user actions
 * @param userId ID of the user for which to show actions
 * @param isCurrentUser Whether this is the current user
 * @param isLevel1User Whether this is a level 1 connection (direct friend)
 * @param isLevel2User Whether this is a level 2 connection (friend of friend)
 * @param onDelete Callback for when the delete button is pressed
 * @param onChallenge Callback for when the challenge button is pressed
 * @param onAddFriend Callback for when the add friend button is pressed
 * @param onDismiss Callback for when the dialog is dismissed
 */
@Composable
fun UserActionDialog(
    userId: String,
    isCurrentUser: Boolean,
    isLevel1User: Boolean = true,
    isLevel2User: Boolean = false,
    onDelete: () -> Unit,
    onChallenge: () -> Unit,
    onAddFriend: () -> Unit,
    onDismiss: () -> Unit
) {
    val userType = when {
        isCurrentUser -> "Your options"
        isLevel1User -> "Friend options"
        isLevel2User -> "Friend of friend options"
        else -> "User options"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = userType,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Divider(Modifier.padding(bottom = 16.dp))

                if (!isCurrentUser) {
                    if (isLevel1User) {
                        // Level 1 friend actions
                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE57373)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Remove friend")
                        }

                        Button(
                            onClick = onChallenge,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Challenge",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Challenge friend")
                        }
                    } else {
                        // Friend of friend or other level actions
                        Button(
                            onClick = onAddFriend,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Add as friend")
                        }

                        Button(
                            onClick = onChallenge,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "View",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("View details")
                        }
                    }
                } else {
                    // Current user actions
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("View profile")
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * Control bar for network manipulation
 * @param showSearch Whether to show the search field
 * @param searchQuery Current search query
 * @param onSearchQueryChange Callback for when the search query changes
 * @param onSearchSubmit Callback for when the search is submitted
 * @param onToggleSearch Callback for toggling search visibility
 * @param onZoomIn Callback for zooming in
 * @param onZoomOut Callback for zooming out
 * @param onResetView Callback for resetting the view
 * @param modifier Modifier for the control bar
 */
@Composable
fun ControlBar(
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onToggleSearch: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetView: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, MaterialTheme.shapes.medium)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        MaterialTheme.shapes.medium
                    ),
                placeholder = { Text("Search users...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                trailingIcon = {
                    IconButton(onClick = onSearchSubmit) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, MaterialTheme.shapes.medium)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        MaterialTheme.shapes.medium
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search button
                FloatingActionButton(
                    onClick = onToggleSearch,
                    modifier = Modifier.size(40.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Zoom controls
                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier.size(40.dp)
                ) {
                    Text(
                        text = "+",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier.size(40.dp)
                ) {
                    Text(
                        text = "-",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Reset view
                Button(
                    onClick = onResetView,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        "Reset View",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}