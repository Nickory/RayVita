package com.codelab.basiclayouts.ui.profile

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.viewmodel.profile.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // 获取UI状态
    val authUiState by authViewModel.uiState.collectAsState()

    // 本地UI状态
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // 焦点管理
    val focusManager = LocalFocusManager.current

    // 监听登录成功事件，登录成功后直接返回到profile_main
    LaunchedEffect(authUiState.isLoggedIn) {
        if (authUiState.isLoggedIn) {
            // 登录成功，返回到主页面
            navController.navigate("profile_main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // 清除错误消息
    LaunchedEffect(email, password) {
        authViewModel.clearErrorMessage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.login_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        // 返回到主页面
                        navController.navigate("profile_main") {
                            popUpTo("login") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.login_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 主要内容
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // 欢迎标题
                Text(
                    text = stringResource(R.string.login_welcome_back),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.login_enter_account_info),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // 邮箱输入框
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.login_email)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = authUiState.errorMessage != null,
                    enabled = !authUiState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 密码输入框
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.login_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (email.isNotBlank() && password.isNotBlank()) {
                                authViewModel.login(email, password)
                            }
                        }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) stringResource(R.string.login_hide_password) else stringResource(R.string.login_show_password)
                            )
                        }
                    },
                    isError = authUiState.errorMessage != null,
                    enabled = !authUiState.isLoading
                )

                // 错误消息
                authUiState.errorMessage?.let { errorMsg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 忘记密码按钮
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = stringResource(R.string.login_forgot_password),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            navController.navigate("reset_password")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 登录按钮
                Button(
                    onClick = { authViewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = email.isNotBlank() && password.isNotBlank() && !authUiState.isLoading
                ) {
                    if (authUiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.login_button))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 注册提示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.login_no_account),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = stringResource(R.string.login_register_now),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            navController.navigate("register")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}