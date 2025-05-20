package com.codelab.basiclayouts.ui.social

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.data.UserSessionManager
import com.codelab.basiclayouts.ui.theme.MySootheTheme
import com.codelab.basiclayouts.viewmodel.social.SocialViewModel
import com.codelab.basiclayouts.viewmodel.social.SocialViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SocialActivity : ComponentActivity() {
    companion object {
        private const val TAG = "SocialActivity"
    }

    private lateinit var sessionManager: UserSessionManager

    private val viewModel: SocialViewModel by viewModels {
        SocialViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el gestor de sesión
        sessionManager = UserSessionManager(application)

        // Comprobar si el usuario ha iniciado sesión
        if (sessionManager.getUserSession() == null) {
            Log.e(TAG, "Usuario no ha iniciado sesión, debería redirigir al login")
            // Descomentar cuando esté listo para implementar
            // startActivity(Intent(this, LoginActivity::class.java))
            // finish()
            // return
        }

        setContent {
            MySootheTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Obtener el estado actual de la UI
                    val uiState by viewModel.uiState.collectAsState()

                    // Selector de fotos
                    val pickMedia = rememberLauncherForActivityResult(
                        ActivityResultContracts.PickVisualMedia()
                    ) { uri ->
                        if (uri != null) {
                            // Convertir URI a ruta local para caché
                            val cachedImageUri = cacheImageLocally(uri)
                            viewModel.setSelectedImageUri(cachedImageUri)
                            Log.d(TAG, "Imagen seleccionada guardada en: $cachedImageUri")
                        } else {
                            Log.d(TAG, "No se seleccionó ningún medio")
                        }
                    }

                    // Lanzador de actividad para búsqueda de amigos
                    val friendSearchLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == RESULT_OK) {
                            result.data?.getIntExtra("selected_user_id", -1)?.let { userId ->
                                if (userId != -1) {
                                    viewModel.sendFriendRequest(userId)
                                }
                            }
                        }
                    }

                    // Banner de modo sin conexión
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Banner de sin conexión
                            AnimatedVisibility(visible = uiState.isOfflineMode) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudOff,
                                        contentDescription = "Sin conexión",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Modo sin conexión - Mostrando datos guardados",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Button(
                                        onClick = {
                                            viewModel.refreshFeed()
                                            viewModel.refreshFriends()
                                            viewModel.refreshFriendRequests()
                                        },
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                                        Text(text = "Reintentar conexión", Modifier.padding(start = 4.dp))
                                    }
                                }
                            }

                            // Pantalla social principal
                            SocialScreen(
                                uiState = uiState,
                                onNavigateToProfile = {
                                    // Manejar navegación al perfil
                                    // startActivity(Intent(this@SocialActivity, ProfileActivity::class.java))
                                },
                                onRefreshFeed = { viewModel.refreshFeed() },
                                onLikePost = { viewModel.likePost(it) },
                                onCommentClick = { viewModel.getPostWithComments(it) },
                                onAddComment = { postId, comment -> viewModel.commentOnPost(postId, comment) },
                                onCreatePost = { text, imageUri -> viewModel.createPost(text, imageUri?.toString()) },
                                onPickImage = {
                                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                onAddFriend = {
                                    // Lanzar actividad de búsqueda de amigos
                                    val intent = Intent(this@SocialActivity, FriendSearchActivity::class.java)
                                    friendSearchLauncher.launch(intent)
                                },
                                onDeletePost = { viewModel.deletePost(it) },
                                onBlockFriend = { friendId, block -> viewModel.blockFriend(friendId, block) },
                                onProcessFriendRequest = { request, accept ->
                                    viewModel.processFriendRequest(
                                        requestId = request.request_id,
                                        accept = accept,
                                        fromUserId = request.from_user_id,
                                        toUserId = request.to_user_id
                                    )
                                },
                                        onHideCreatePostDialog = { viewModel.hideCreatePostDialog() },
                                onShowCreatePostDialog = { viewModel.showCreatePostDialog() },
                                onHideCommentsDialog = { viewModel.hideCommentsDialog() },
                                onClearErrorMessage = { viewModel.clearErrorMessage() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun cacheImageLocally(uri: Uri): Uri {
        contentResolver.openInputStream(uri)?.use { input ->
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = cacheDir
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

            FileOutputStream(imageFile).use { output ->
                input.copyTo(output)
            }

            return Uri.fromFile(imageFile)
        }
        return uri
    }
}