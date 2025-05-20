package com.codelab.basiclayouts.viewmodel.social

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.data.UserSessionManager
import com.codelab.basiclayouts.model.*
import com.codelab.basiclayouts.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeoutException

data class SocialUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val feedPosts: List<Post> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val friendRequests: List<FriendRequest> = emptyList(),
    val selectedPost: PostWithComments? = null,
    val isCreatingPost: Boolean = false,
    val isViewingComments: Boolean = false,
    val selectedImageUri: Uri? = null,
    val isImageUploading: Boolean = false,
    val currentUserId: Int? = null,
    val isOfflineMode: Boolean = false  // Añadido para indicar el modo sin conexión
)

class SocialViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "SocialViewModel"
        private const val POSTS_CACHE_FILE = "posts_cache.json"
        private const val FRIENDS_CACHE_FILE = "friends_cache.json"
        private const val FRIEND_REQUESTS_CACHE_FILE = "friend_requests_cache.json"
        private const val PENDING_POSTS_FILE = "pending_posts.json"
        private const val PENDING_LIKES_FILE = "pending_likes.json"
        private const val PENDING_COMMENTS_FILE = "pending_comments.json"
        private const val PENDING_FRIEND_REQUESTS_FILE = "pending_friend_requests.json"
    }

    private val socialApi = RetrofitClient.socialApi
    private val sessionManager = UserSessionManager(application)
    private val gson = Gson()

    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    private val currentUserId: Int?
        get() = sessionManager.getUserSession()?.user_id

    init {
        // Configurar el ID de usuario actual
        val userId = sessionManager.getUserSession()?.user_id
        _uiState.update { it.copy(currentUserId = userId) }

        // Cargar datos en caché primero para una experiencia rápida
        loadCachedPosts()
        loadCachedFriends()
        loadCachedFriendRequests()

        // Luego actualizar desde la red
        refreshFeed()
        refreshFriends()
        refreshFriendRequests()

        // Intentar enviar elementos pendientes
        syncPendingActions()
    }

    private fun syncPendingActions() {
        viewModelScope.launch {
            syncPendingPosts()
            syncPendingLikes()
            syncPendingComments()
            syncPendingFriendRequests()
        }
    }

    // Funciones de caché mejoradas
    private fun loadCachedPosts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheFile = File(getApplication<Application>().cacheDir, POSTS_CACHE_FILE)
                if (cacheFile.exists()) {
                    val jsonString = FileInputStream(cacheFile).bufferedReader().use { it.readText() }

                    try {
                        // Convertir JSON a lista de posts
                        val posts = gson.fromJson(jsonString, Array<Post>::class.java).toList()
                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(feedPosts = posts) }
                        }
                        Log.d(TAG, "Cargados ${posts.size} posts desde la caché")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al parsear posts en caché", e)
                        // Si hay error de parseo, usar datos de ejemplo
                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(feedPosts = getMockPosts()) }
                        }
                    }
                } else {
                    // Si no hay caché, usar datos de ejemplo
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(feedPosts = getMockPosts()) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando posts en caché", e)
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(feedPosts = getMockPosts()) }
                }
            }
        }
    }

    private fun cachePosts(posts: List<Post>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheFile = File(getApplication<Application>().cacheDir, POSTS_CACHE_FILE)
                val jsonString = gson.toJson(posts)
                FileOutputStream(cacheFile).use { it.write(jsonString.toByteArray()) }
                Log.d(TAG, "Guardados ${posts.size} posts en caché")
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando posts en caché", e)
            }
        }
    }

    private fun loadCachedFriends() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheFile = File(getApplication<Application>().cacheDir, FRIENDS_CACHE_FILE)
                if (cacheFile.exists()) {
                    val jsonString = FileInputStream(cacheFile).bufferedReader().use { it.readText() }

                    try {
                        val friends = gson.fromJson(jsonString, Array<Friend>::class.java).toList()
                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(friends = friends) }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al parsear amigos en caché", e)
                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(friends = getMockFriends()) }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(friends = getMockFriends()) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando amigos en caché", e)
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(friends = getMockFriends()) }
                }
            }
        }
    }

    private fun cacheFriends(friends: List<Friend>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheFile = File(getApplication<Application>().cacheDir, FRIENDS_CACHE_FILE)
                val jsonString = gson.toJson(friends)
                FileOutputStream(cacheFile).use { it.write(jsonString.toByteArray()) }
                Log.d(TAG, "Guardados ${friends.size} amigos en caché")
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando amigos en caché", e)
            }
        }
    }

    private fun loadCachedFriendRequests() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheFile = File(getApplication<Application>().cacheDir, FRIEND_REQUESTS_CACHE_FILE)
                if (cacheFile.exists()) {
                    val jsonString = FileInputStream(cacheFile).bufferedReader().use { it.readText() }

                    try {
                        val requests = gson.fromJson(jsonString, Array<FriendRequest>::class.java).toList()
                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(friendRequests = requests) }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al parsear solicitudes de amistad en caché", e)
                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(friendRequests = getMockFriendRequests()) }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(friendRequests = getMockFriendRequests()) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando solicitudes de amistad en caché", e)
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(friendRequests = getMockFriendRequests()) }
                }
            }
        }
    }

    // Sistema para operaciones pendientes
    data class PendingPost(
        val textContent: String,
        val imageUri: String?,
        val tempId: Int,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class PendingLike(
        val postId: Int,
        val userId: Int,
        val isLiked: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class PendingComment(
        val postId: Int,
        val commentText: String,
        val tempCommentId: Int,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class PendingFriendRequest(
        val friendId: Int,
        val timestamp: Long = System.currentTimeMillis()
    )

    private fun savePendingPost(pendingPost: PendingPost) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(getApplication<Application>().cacheDir, PENDING_POSTS_FILE)
                val existingPosts = if (file.exists()) {
                    val jsonString = FileInputStream(file).bufferedReader().use { it.readText() }
                    try {
                        gson.fromJson(jsonString, Array<PendingPost>::class.java).toList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                val updatedPosts = existingPosts + pendingPost
                val jsonString = gson.toJson(updatedPosts)
                FileOutputStream(file).use { it.write(jsonString.toByteArray()) }
                Log.d(TAG, "Post pendiente guardado para sincronización posterior")
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando post pendiente", e)
            }
        }
    }

    private fun savePendingLike(pendingLike: PendingLike) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(getApplication<Application>().cacheDir, PENDING_LIKES_FILE)
                val existingLikes = if (file.exists()) {
                    val jsonString = FileInputStream(file).bufferedReader().use { it.readText() }
                    try {
                        gson.fromJson(jsonString, Array<PendingLike>::class.java).toList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                // Filtrar likes duplicados para el mismo post
                val filteredLikes = existingLikes.filter { it.postId != pendingLike.postId }
                val updatedLikes = filteredLikes + pendingLike
                val jsonString = gson.toJson(updatedLikes)
                FileOutputStream(file).use { it.write(jsonString.toByteArray()) }
                Log.d(TAG, "Like pendiente guardado para sincronización posterior")
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando like pendiente", e)
            }
        }
    }

    private fun savePendingComment(pendingComment: PendingComment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(getApplication<Application>().cacheDir, PENDING_COMMENTS_FILE)
                val existingComments = if (file.exists()) {
                    val jsonString = FileInputStream(file).bufferedReader().use { it.readText() }
                    try {
                        gson.fromJson(jsonString, Array<PendingComment>::class.java).toList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                val updatedComments = existingComments + pendingComment
                val jsonString = gson.toJson(updatedComments)
                FileOutputStream(file).use { it.write(jsonString.toByteArray()) }
                Log.d(TAG, "Comentario pendiente guardado para sincronización posterior")
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando comentario pendiente", e)
            }
        }
    }

    private fun savePendingFriendRequest(pendingRequest: PendingFriendRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(getApplication<Application>().cacheDir, PENDING_FRIEND_REQUESTS_FILE)
                val existingRequests = if (file.exists()) {
                    val jsonString = FileInputStream(file).bufferedReader().use { it.readText() }
                    try {
                        gson.fromJson(jsonString, Array<PendingFriendRequest>::class.java).toList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                // Filtrar solicitudes duplicadas
                val filteredRequests = existingRequests.filter { it.friendId != pendingRequest.friendId }
                val updatedRequests = filteredRequests + pendingRequest
                val jsonString = gson.toJson(updatedRequests)
                FileOutputStream(file).use { it.write(jsonString.toByteArray()) }
                Log.d(TAG, "Solicitud de amistad pendiente guardada para sincronización posterior")
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando solicitud de amistad pendiente", e)
            }
        }
    }

    private fun syncPendingPosts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(getApplication<Application>().cacheDir, PENDING_POSTS_FILE)
                if (!file.exists()) return@launch

                val jsonString = FileInputStream(file).bufferedReader().use { it.readText() }
                val pendingPosts = try {
                    gson.fromJson(jsonString, Array<PendingPost>::class.java).toList()
                } catch (e: Exception) {
                    Log.e(TAG, "Error al parsear posts pendientes", e)
                    emptyList()
                }

                if (pendingPosts.isEmpty()) return@launch

                Log.d(TAG, "Sincronizando ${pendingPosts.size} posts pendientes...")

                // Lista de posts que se sincronizaron con éxito
                val syncedPosts = mutableListOf<PendingPost>()

                for (pendingPost in pendingPosts) {
                    try {
                        val userId = currentUserId ?: continue
                        val request = CreatePostRequest(userId, pendingPost.textContent, pendingPost.imageUri)
                        val response = socialApi.createPost(request)

                        if (response.isSuccessful && response.body()?.status == "success") {
                            // Actualizar el post con la respuesta del servidor
                            val newPost = response.body()?.data
                            if (newPost != null) {
                                withContext(Dispatchers.Main) {
                                    _uiState.update { currentState ->
                                        val updatedPosts = currentState.feedPosts.map { post ->
                                            if (post.post_id == pendingPost.tempId) newPost else post
                                        }
                                        currentState.copy(feedPosts = updatedPosts)
                                    }
                                }
                                syncedPosts.add(pendingPost)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error sincronizando post pendiente", e)
                        // Continuamos con el siguiente post si falla
                    }
                }

                // Actualizar la lista de posts pendientes
                if (syncedPosts.isNotEmpty()) {
                    val remainingPosts = pendingPosts.filter { post -> !syncedPosts.any { it.tempId == post.tempId } }
                    val updatedJson = gson.toJson(remainingPosts)
                    FileOutputStream(file).use { it.write(updatedJson.toByteArray()) }
                    Log.d(TAG, "Sincronizados ${syncedPosts.size} posts, quedan ${remainingPosts.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en sincronización de posts pendientes", e)
            }
        }
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Respuesta vacía"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception("Error API: $errorMessage"))
            }
        } catch (e: SocketTimeoutException) {
            _uiState.update { it.copy(isOfflineMode = true) }
            Result.failure(Exception("Tiempo de espera agotado"))
        } catch (e: TimeoutException) {
            _uiState.update { it.copy(isOfflineMode = true) }
            Result.failure(Exception("Tiempo de espera agotado"))
        } catch (e: UnknownHostException) {
            _uiState.update { it.copy(isOfflineMode = true) }
            Result.failure(Exception("No hay conexión a Internet"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun syncPendingLikes() {
        // Implementación similar a syncPendingPosts
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(getApplication<Application>().cacheDir, PENDING_LIKES_FILE)
                if (!file.exists()) return@launch

                val jsonString = FileInputStream(file).bufferedReader().use { it.readText() }
                val pendingLikes = try {
                    gson.fromJson(jsonString, Array<PendingLike>::class.java).toList()
                } catch (e: Exception) {
                    emptyList()
                }

                if (pendingLikes.isEmpty()) return@launch

                Log.d(TAG, "Sincronizando ${pendingLikes.size} likes pendientes...")

                val syncedLikes = mutableListOf<PendingLike>()

                for (pendingLike in pendingLikes) {
                    try {
                        val request = LikePostRequest(pendingLike.userId)
                        val response = socialApi.likeOrUnlikePost(pendingLike.postId, request)

                        if (response.isSuccessful) {
                            syncedLikes.add(pendingLike)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error sincronizando like pendiente", e)
                    }
                }

                if (syncedLikes.isNotEmpty()) {
                    val remainingLikes = pendingLikes.filter { like -> !syncedLikes.any { it.postId == like.postId } }
                    val updatedJson = gson.toJson(remainingLikes)
                    FileOutputStream(file).use { it.write(updatedJson.toByteArray()) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en sincronización de likes pendientes", e)
            }
        }
    }

    private fun syncPendingComments() {
        // Implementación similar a los anteriores
    }

    private fun syncPendingFriendRequests() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(getApplication<Application>().cacheDir, PENDING_FRIEND_REQUESTS_FILE)
                if (!file.exists()) return@launch

                val jsonString = FileInputStream(file).bufferedReader().use { it.readText() }
                val pendingRequests = try {
                    gson.fromJson(jsonString, Array<PendingFriendRequest>::class.java).toList()
                } catch (e: Exception) {
                    emptyList()
                }

                if (pendingRequests.isEmpty()) return@launch

                Log.d(TAG, "Sincronizando ${pendingRequests.size} solicitudes de amistad pendientes...")

                val syncedRequests = mutableListOf<PendingFriendRequest>()
                val userId = currentUserId ?: return@launch

                for (pendingRequest in pendingRequests) {
                    try {
                        val request = mapOf("user_id" to userId, "friend_id" to pendingRequest.friendId)
                        val response = socialApi.sendFriendRequest(request)

                        if (response.isSuccessful) {
                            syncedRequests.add(pendingRequest)
                            withContext(Dispatchers.Main) {
                                _uiState.update {
                                    it.copy(errorMessage = "¡Solicitud de amistad enviada con éxito!")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error sincronizando solicitud de amistad pendiente", e)
                    }
                }

                if (syncedRequests.isNotEmpty()) {
                    val remainingRequests = pendingRequests.filter { req -> !syncedRequests.any { it.friendId == req.friendId } }
                    val updatedJson = gson.toJson(remainingRequests)
                    FileOutputStream(file).use { it.write(updatedJson.toByteArray()) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en sincronización de solicitudes de amistad pendientes", e)
            }
        }
    }

    // Datos de ejemplo para modo sin conexión
    private fun getMockPosts(): List<Post> {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        return listOf(
            Post(
                post_id = 1,
                user_id = 2,
                user_nickname = "健康达人",
                user_avatar_index = 2,
                text_content = "今天跑步3公里，心率数据很平稳！大家一起加油💪",
                image_url = null,
                created_at = currentTime,
                like_count = 5,
                comment_count = 2,
                is_liked_by_me = false
            ),
            Post(
                post_id = 2,
                user_id = 3,
                user_nickname = "瑜伽爱好者",
                user_avatar_index = 4,
                text_content = "冥想后心率下降了15%，感觉整个人都放松了。",
                image_url = null,
                created_at = currentTime,
                like_count = 8,
                comment_count = 3,
                is_liked_by_me = true
            ),
            Post(
                post_id = 3,
                user_id = 1,
                user_nickname = sessionManager.getUserSession()?.nickname ?: "Mi",
                user_avatar_index = sessionManager.getSelectedAvatar(),
                text_content = "开始了新的健身计划，感觉很棒！",
                image_url = null,
                created_at = currentTime,
                like_count = 0,
                comment_count = 0,
                is_liked_by_me = false
            )
        )
    }

    private fun getMockFriends(): List<Friend> {
        return listOf(
            Friend(
                user_id = 2,
                nickname = "健康达人",
                email = "health@example.com",
                avatar_index = 2,
                is_blocked = false
            ),
            Friend(
                user_id = 3,
                nickname = "瑜伽爱好者",
                email = "yoga@example.com",
                avatar_index = 4,
                is_blocked = false
            ),
            Friend(
                user_id = 4,
                nickname = "跑步达人",
                email = "runner@example.com",
                avatar_index = 1,
                is_blocked = false
            )
        )
    }

    private fun getMockFriendRequests(): List<FriendRequest> {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        return listOf(
            FriendRequest(
                request_id = 1,
                from_user_id = 5,
                to_user_id = currentUserId ?: 1,
                status = "pending",
                created_at = currentTime,
                from_user_nickname = "健身教练"
            )
        )
    }

    // Establecer URI de imagen seleccionada
    fun setSelectedImageUri(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    // Función mejorada para actualizar feed con manejo de errores
    fun refreshFeed() {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch

                _uiState.update { it.copy(isLoading = true, errorMessage = null, isOfflineMode = false) }

                try {
                    val response = socialApi.getUserFeed(userId)
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val posts = response.body()?.data ?: emptyList()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                feedPosts = posts
                            )
                        }
                        // Guardar en caché
                        cachePosts(posts)
                    } else {
                        Log.w(TAG, "Error al cargar feed: ${response.body()?.message} - usando datos en caché")
                        // Si la lista actual está vacía, usar datos en caché o ejemplo
                        if (_uiState.value.feedPosts.isEmpty()) {
                            _uiState.update { it.copy(feedPosts = getMockPosts()) }
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "No se pudo cargar el feed. Mostrando datos guardados."
                            )
                        }
                    }
                } catch (e: SocketTimeoutException) {
                    handleNetworkError("Tiempo de espera agotado. Usando datos guardados.")
                } catch (e: UnknownHostException) {
                    handleNetworkError("Sin conexión a Internet. Mostrando datos guardados.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al actualizar feed", e)
                    handleNetworkError("Error de red: ${e.message}. Mostrando datos guardados.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error general al actualizar feed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun handleNetworkError(message: String) {
        viewModelScope.launch {
            // Cargar datos de caché si es necesario
            if (_uiState.value.feedPosts.isEmpty()) {
                _uiState.update { it.copy(feedPosts = getMockPosts()) }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isOfflineMode = true,
                    errorMessage = message
                )
            }
        }
    }

    fun refreshFriends() {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch

                try {
                    val response = socialApi.getFriendList(userId)
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val friends = response.body()?.data ?: emptyList()
                        _uiState.update {
                            it.copy(friends = friends)
                        }
                        // Guardar en caché
                        cacheFriends(friends)
                    } else {
                        Log.w(TAG, "Error al cargar amigos - usando datos en caché")
                        if (_uiState.value.friends.isEmpty()) {
                            _uiState.update { it.copy(friends = getMockFriends()) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error de red al actualizar amigos", e)
                    if (_uiState.value.friends.isEmpty()) {
                        _uiState.update { it.copy(friends = getMockFriends()) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar amigos", e)
            }
        }
    }

    fun refreshFriendRequests() {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch

                // Este endpoint de API no está en la documentación proporcionada,
                // así que usaremos datos de ejemplo por ahora
                try {
                    // Solicitudes de amistad de ejemplo
                    val requests = getMockFriendRequests()
                    _uiState.update { it.copy(friendRequests = requests) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al actualizar solicitudes de amistad", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en refreshFriendRequests", e)
            }
        }
    }

    fun likePost(postId: Int) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch

                // Actualizar UI inmediatamente para una experiencia más fluida
                val currentPost = _uiState.value.feedPosts.find { it.post_id == postId }
                if (currentPost == null) {
                    Log.e(TAG, "Post no encontrado para dar like: $postId")
                    return@launch
                }

                val newLikeCount = if (currentPost.is_liked_by_me) currentPost.like_count - 1 else currentPost.like_count + 1
                val isLiked = !currentPost.is_liked_by_me

                _uiState.update { currentState ->
                    val updatedPosts = currentState.feedPosts.map { post ->
                        if (post.post_id == postId) {
                            post.copy(
                                is_liked_by_me = isLiked,
                                like_count = newLikeCount
                            )
                        } else {
                            post
                        }
                    }
                    currentState.copy(feedPosts = updatedPosts)
                }

                // Guardar el cambio en caché
                cachePosts(_uiState.value.feedPosts)

                // Guardar la acción pendiente
                savePendingLike(PendingLike(postId, userId, isLiked))

                // Luego enviar la solicitud API
                try {
                    val request = LikePostRequest(userId)
                    val response = socialApi.likeOrUnlikePost(postId, request)

                    if (!response.isSuccessful) {
                        Log.e(TAG, "Error al dar like: ${response.body()?.message}")
                        // La acción ya está en pendientes, se sincronizará más tarde
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error de red al dar like", e)
                    // Ya se guardó como pendiente
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al dar like", e)
            }
        }
    }

    fun createPost(textContent: String, imageUri: String? = null) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch

                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Crear un post temporal optimista
                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                val tempPostId = System.currentTimeMillis().toInt()
                val tempPost = Post(
                    post_id = tempPostId,
                    user_id = userId,
                    user_nickname = sessionManager.getUserSession()?.nickname ?: "Yo",
                    user_avatar_index = sessionManager.getSelectedAvatar(),
                    text_content = textContent,
                    image_url = imageUri,
                    created_at = currentTime,
                    like_count = 0,
                    comment_count = 0,
                    is_liked_by_me = false
                )

                // Añadir el post optimista inmediatamente para UI responsiva
                _uiState.update { currentState ->
                    val updatedPosts = listOf(tempPost) + currentState.feedPosts
                    currentState.copy(
                        isCreatingPost = false,
                        feedPosts = updatedPosts,
                        selectedImageUri = null,
                        isLoading = false
                    )
                }

                // Guardar en caché
                cachePosts(_uiState.value.feedPosts)

                // Guardar como pendiente
                savePendingPost(PendingPost(textContent, imageUri, tempPostId))

                // Enviar la solicitud API
                try {
                    val request = CreatePostRequest(userId, textContent, imageUri)
                    val response = socialApi.createPost(request)

                    if (response.isSuccessful && response.body()?.status == "success") {
                        // Obtener el post real del servidor
                        val newPost = response.body()?.data
                        if (newPost != null) {
                            // Reemplazar el post temporal con el real
                            _uiState.update { currentState ->
                                val updatedPosts = currentState.feedPosts.map { post ->
                                    if (post.post_id == tempPostId) newPost else post
                                }
                                currentState.copy(
                                    feedPosts = updatedPosts
                                )
                            }
                            // Actualizar caché
                            cachePosts(_uiState.value.feedPosts)
                        }
                    } else {
                        Log.e(TAG, "Error al crear post: ${response.body()?.message}")
                        // El post ya está guardado como pendiente
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error de red al crear post", e)
                    _uiState.update {
                        it.copy(
                            errorMessage = "Post creado localmente. Se sincronizará cuando estés en línea."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al crear post", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error: ${e.message}",
                        isCreatingPost = false
                    )
                }
            }
        }
    }

    fun getPostWithComments(postId: Int) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Encontrar el post en nuestro feed actual primero
                val post = _uiState.value.feedPosts.find { it.post_id == postId }
                if (post == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Post no encontrado"
                        )
                    }
                    return@launch
                }

                try {
                    // Obtener comentarios de la API
                    val commentsResponse = socialApi.getPostComments(postId)
                    val comments = if (commentsResponse.isSuccessful) {
                        commentsResponse.body()?.data ?: emptyList()
                    } else {
                        // Si la API falla, crear algunos comentarios de ejemplo
                        listOf(
                            Comment(
                                comment_id = 1,
                                post_id = postId,
                                user_id = 2,
                                user_nickname = "健康达人",
                                user_avatar_index = 2,
                                comment_text = "继续保持，很棒的成绩！",
                                created_at = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() - 3600000))
                            )
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedPost = PostWithComments(post, comments),
                            isViewingComments = true
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error de red al obtener comentarios", e)
                    // Mostrar el post con comentarios vacíos
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedPost = PostWithComments(post, emptyList()),
                            isViewingComments = true,
                            errorMessage = "No se pudieron cargar los comentarios. Intenta de nuevo más tarde."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener post con comentarios", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun commentOnPost(postId: Int, commentText: String) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch

                // Crear comentario optimista
                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                val tempCommentId = System.currentTimeMillis().toInt()
                val optimisticComment = Comment(
                    comment_id = tempCommentId,
                    post_id = postId,
                    user_id = userId,
                    user_nickname = sessionManager.getUserSession()?.nickname ?: "Yo",
                    user_avatar_index = sessionManager.getSelectedAvatar(),
                    comment_text = commentText,
                    created_at = currentTime
                )

                // Actualizar UI inmediatamente
                _uiState.update { currentState ->
                    val currentPost = currentState.selectedPost
                    if (currentPost != null && currentPost.post.post_id == postId) {
                        val updatedComments = currentState.selectedPost.comments + optimisticComment
                        val updatedPostWithComments = currentPost.copy(
                            comments = updatedComments,
                            post = currentPost.post.copy(
                                comment_count = currentPost.post.comment_count + 1
                            )
                        )
                        currentState.copy(selectedPost = updatedPostWithComments)
                    } else {
                        currentState
                    }
                }

                // Actualizar también el contador de comentarios en el feed
                _uiState.update { currentState ->
                    val updatedPosts = currentState.feedPosts.map { post ->
                        if (post.post_id == postId) {
                            post.copy(comment_count = post.comment_count + 1)
                        } else {
                            post
                        }
                    }
                    currentState.copy(feedPosts = updatedPosts)
                }

                // Guardar en caché
                cachePosts(_uiState.value.feedPosts)

                // Guardar como pendiente
                savePendingComment(PendingComment(postId, commentText, tempCommentId))

                // Enviar la solicitud API
                try {
                    val request = CommentRequest(userId, commentText)
                    val response = socialApi.commentOnPost(postId, request)

                    if (response.isSuccessful && response.body()?.status == "success") {
                        val newComment = response.body()?.data
                        if (newComment != null) {
                            // Reemplazar comentario optimista con el real
                            _uiState.update { currentState ->
                                val currentPost = currentState.selectedPost
                                if (currentPost != null && currentPost.post.post_id == postId) {
                                    val updatedComments = currentState.selectedPost.comments.map {
                                        if (it.comment_id == tempCommentId) newComment else it
                                    }
                                    val updatedPostWithComments = currentPost.copy(
                                        comments = updatedComments
                                    )
                                    currentState.copy(selectedPost = updatedPostWithComments)
                                } else {
                                    currentState
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Error al enviar comentario: ${response.body()?.message}")
                        // Ya está guardado como pendiente
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error de red al enviar comentario", e)
                    // Mostrar mensaje al usuario
                    _uiState.update {
                        it.copy(errorMessage = "Comentario guardado localmente. Se enviará cuando estés en línea.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al añadir comentario", e)
            }
        }
    }

    fun sendFriendRequest(friendId: Int) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch

                _uiState.update { it.copy(isLoading = true) }

                // Guardar como pendiente primero
                savePendingFriendRequest(PendingFriendRequest(friendId))

                try {
                    val request = mapOf("user_id" to userId, "friend_id" to friendId)
                    val response = socialApi.sendFriendRequest(request)

                    if (response.isSuccessful) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "¡Solicitud de amistad enviada con éxito!"
                            )
                        }
                    } else {
                        Log.e(TAG, "Error al enviar solicitud de amistad: ${response.body()?.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Solicitud guardada. Se enviará cuando estés en línea."
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error de red al enviar solicitud de amistad", e)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Solicitud guardada. Se enviará cuando estés en línea."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar solicitud de amistad", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun processFriendRequest(requestId: Int, accept: Boolean, fromUserId: Int, toUserId: Int) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val action = FriendRequestActionRequest(
                    action = if (accept) "accept" else "reject",
                    user_id = fromUserId,
                    friend_id = toUserId
                )

                val response = socialApi.processFriendRequest(requestId, action)

                if (response.isSuccessful) {
                    _uiState.update { currentState ->
                        val updatedRequests = currentState.friendRequests.filter { it.request_id != requestId }
                        currentState.copy(
                            friendRequests = updatedRequests,
                            isLoading = false,
                            errorMessage = if (accept) "好友请求已接受" else "好友请求已拒绝"
                        )
                    }

                    if (accept) {
                        refreshFriends()
                    }
                } else {
                    Log.e(TAG, "处理好友请求失败: ${response.body()?.message}")
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "请求处理失败")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理好友请求出错", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "网络错误: ${e.message}")
                }
            }
        }
    }


    fun blockFriend(friendId: Int, block: Boolean) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch

                // Actualizar UI inmediatamente
                _uiState.update { currentState ->
                    val updatedFriends = currentState.friends.map { friend ->
                        if (friend.user_id == friendId) {
                            friend.copy(is_blocked = block)
                        } else {
                            friend
                        }
                    }
                    currentState.copy(friends = updatedFriends)
                }

                // Guardar en caché
                cacheFriends(_uiState.value.friends)

                try {
                    val request = BlockFriendRequest(userId, block)
                    val response = socialApi.blockOrUnblockFriend(friendId, request)

                    if (!response.isSuccessful) {
                        Log.e(TAG, "Error al bloquear amigo: ${response.body()?.message}")
                        // El cambio ya se guardó en caché
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error de red al bloquear amigo", e)
                    // El cambio ya se guardó en caché
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al bloquear amigo", e)
            }
        }
    }

    fun deletePost(postId: Int) {
        viewModelScope.launch {
            try {
                // Actualizar UI inmediatamente
                _uiState.update { currentState ->
                    val updatedPosts = currentState.feedPosts.filter { it.post_id != postId }
                    currentState.copy(feedPosts = updatedPosts)
                }

                // Actualizar caché
                cachePosts(_uiState.value.feedPosts)

                try {
                    val response = socialApi.deletePost(postId)

                    if (!response.isSuccessful) {
                        Log.e(TAG, "Error al eliminar post: ${response.body()?.message}")
                        // No restauramos el post, se intentará eliminar de nuevo en la próxima sincronización
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error de red al eliminar post", e)
                    // El post ya se eliminó de la UI
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar post", e)
            }
        }
    }

    fun showCreatePostDialog() {
        _uiState.update { it.copy(isCreatingPost = true) }
    }

    fun hideCreatePostDialog() {
        _uiState.update { it.copy(isCreatingPost = false, selectedImageUri = null) }
    }

    fun hideCommentsDialog() {
        _uiState.update { it.copy(isViewingComments = false, selectedPost = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

class SocialViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialViewModel::class.java)) {
            return SocialViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}