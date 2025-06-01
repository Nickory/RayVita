package com.codelab.basiclayouts.ui.social

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.data.language.model.LanguagePreferences
import com.codelab.basiclayouts.data.language.model.LanguageRepository
import com.codelab.basiclayouts.data.theme.model.ThemePreferences
import com.codelab.basiclayouts.data.theme.model.ThemeRepository
import com.codelab.basiclayouts.model.UserInfo
import com.codelab.basiclayouts.ui.theme.DynamicRayVitaTheme
import com.codelab.basiclayouts.viewModel.theme.DarkModeOption
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModel
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModelFactory
import com.codelab.basiclayouts.viewmodel.profile.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
class FriendSearchActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var themeRepository: ThemeRepository
    private lateinit var themePreferences: ThemePreferences
    private lateinit var languageRepository: LanguageRepository
    private lateinit var languagePreferences: LanguagePreferences

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeRepository, this@FriendSearchActivity)
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            // 在Activity启动时应用语言设置
            val languagePrefs = LanguagePreferences(newBase)
            val currentLanguage = languagePrefs.getCurrentLanguage()
            LanguageRepository.updateAppLanguage(newBase, currentLanguage)
            super.attachBaseContext(newBase)
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化语言相关组件
        languagePreferences = LanguagePreferences(this)
        languageRepository = LanguageRepository(this, languagePreferences)

        // 应用保存的语言设置
        val currentLanguage = languagePreferences.getCurrentLanguage()
        LanguageRepository.updateAppLanguage(this, currentLanguage)

        // 初始化主题相关组件
        themePreferences = ThemePreferences(this)
        themeRepository = ThemeRepository(this, themePreferences)

        setContent {
            DynamicThemeWrapper(
                themeRepository = themeRepository,
                themeViewModel = themeViewModel
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }

    @Composable
    private fun DynamicThemeWrapper(
        themeRepository: ThemeRepository,
        themeViewModel: ThemeViewModel,
        content: @Composable () -> Unit
    ) {
        val currentThemeFlow = themeRepository.getCurrentTheme()
        val currentTheme by currentThemeFlow.collectAsState(initial = null)
        val uiState by themeViewModel.uiState.collectAsState()

        // 根据用户的深色模式选择确定是否使用深色主题
        val isSystemInDarkTheme = isSystemInDarkTheme()
        val shouldUseDarkTheme = when (uiState.darkModeOption) {
            DarkModeOption.FOLLOW_SYSTEM -> isSystemInDarkTheme
            DarkModeOption.LIGHT -> false
            DarkModeOption.DARK -> true
        }

        currentTheme?.let { theme ->
            DynamicRayVitaTheme(
                themeProfile = theme,
                darkTheme = shouldUseDarkTheme,
                content = content
            )
        } ?: run {
            // 如果主题还在加载中，使用默认主题
            content()
        }
    }

    @Composable
    private fun AppContent() {
        val uiState by authViewModel.uiState.collectAsState()
        val focusManager = LocalFocusManager.current
        var searchQuery by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.find_friends_title)) },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                        // Load users matching search
                        authViewModel.getAllUsers()
                    })
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Load all users button
                Button(
                    onClick = { authViewModel.getAllUsers() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.find_people_button))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Users list
                if (uiState.isLoadingAllUsers) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else if (uiState.allUsers.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_users_found),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.allUsers.size) { index ->
                            val user = uiState.allUsers[index]
                            // Filter users based on search query if not empty
                            if (searchQuery.isEmpty() ||
                                user.nickname?.contains(searchQuery, ignoreCase = true) == true ||
                                user.email?.contains(searchQuery, ignoreCase = true) == true) {
                                UserListItem(
                                    user = user,
                                    onAddFriend = {
                                        // Return selected user ID to SocialActivity
                                        val resultIntent = Intent()
                                        resultIntent.putExtra("selected_user_id", user.user_id)
                                        setResult(RESULT_OK, resultIntent)
                                        finish()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LoadingScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.loading),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UserListItem(
    user: UserInfo,
    onAddFriend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Avatar
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Column {
                    user.nickname?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    user.email?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(onClick = onAddFriend) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = stringResource(R.string.add_friend),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}