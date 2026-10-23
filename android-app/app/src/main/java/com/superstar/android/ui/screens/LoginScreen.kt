package com.superstar.android.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superstar.android.R
import com.superstar.android.ui.i18n.AppStrings
import com.superstar.android.viewmodel.MainViewModel
import kotlin.math.hypot

/**
 * 学习星 (StudyStar) 登录界面
 * 遵循 Material Design 3 规范、48dp 交互触控靶区与全屏涟漪揭示动画
 */
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    val loginState by viewModel.loginState.collectAsState()
    val autoLogin by viewModel.autoLogin.collectAsState()
    val userManuallyLoggedOut by viewModel.userManuallyLoggedOut.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var autoLoginAttempted by remember { mutableStateOf(false) }

    // 展开动画状态与点击坐标
    var buttonCenter by remember { mutableStateOf(Offset.Zero) }
    var isExpandingToFullScreen by remember { mutableStateOf(false) }

    val expandProgress by animateFloatAsState(
        targetValue = if (isExpandingToFullScreen) 1f else 0f,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "login_expand_anim",
        finishedListener = { progress ->
            if (progress >= 1f) {
                onLoginSuccess()
            }
        }
    )

    // 从本地加载已保存的账号密码到输入框（方便用户查看与修改）
    val savedUsername = loginState.savedUsername
    val savedPassword = loginState.savedPassword
    LaunchedEffect(savedUsername, savedPassword) {
        if (!savedUsername.isNullOrBlank()) {
            username = savedUsername
        }
        if (!savedPassword.isNullOrBlank()) {
            password = savedPassword
        }

        // 仅在明确勾选了【自动登录】且非手动登出的情况下，启动时才静默自动登录一次！
        if (autoLogin && !userManuallyLoggedOut && !autoLoginAttempted &&
            !savedUsername.isNullOrBlank() && !savedPassword.isNullOrBlank() &&
            !loginState.isLoggedIn && !loginState.isLoading
        ) {
            autoLoginAttempted = true
            viewModel.login(savedUsername, savedPassword)
        }
    }

    // 登录成功时触发从按钮展开到全屏的过渡动画
    LaunchedEffect(loginState.isLoggedIn) {
        if (loginState.isLoggedIn) {
            isExpandingToFullScreen = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 顶部语言快速切换（登录前即可直接选择中文或英文）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(3.dp)) {
                            val isZh = currentLang == "zh"
                            Surface(
                                shape = CircleShape,
                                color = if (isZh) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { viewModel.setLanguage("zh") }
                            ) {
                                Text(
                                    text = "中文",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isZh) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isZh) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = if (!isZh) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { viewModel.setLanguage("en") }
                            ) {
                                Text(
                                    text = "EN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (!isZh) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!isZh) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Logo 徽标
                Surface(
                    modifier = Modifier.size(92.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shadowElevation = 4.dp,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = AppStrings.appName(currentLang),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = AppStrings.loginSubtitle(currentLang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 账号归属提示卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "提示",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = AppStrings.loginNotice(currentLang),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 登录表单卡片
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (currentLang == "zh") "账号登录" else "Account Login",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 用户名输入
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                if (loginState.error != null) viewModel.clearLoginError()
                            },
                            label = { Text(AppStrings.usernameLabel(currentLang)) },
                            placeholder = { Text(AppStrings.usernamePlaceholder(currentLang)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.AccountCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            shape = MaterialTheme.shapes.medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 密码输入
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (loginState.error != null) viewModel.clearLoginError()
                            },
                            label = { Text(AppStrings.passwordLabel(currentLang)) },
                            placeholder = { Text(AppStrings.passwordPlaceholder(currentLang)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff
                                        else Icons.Outlined.Visibility,
                                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (username.isNotBlank() && password.isNotBlank() && !loginState.isLoading) {
                                        viewModel.login(username, password)
                                    }
                                }
                            ),
                            shape = MaterialTheme.shapes.medium
                        )

                        // 错误信息提示
                        if (loginState.error != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = loginState.error.orEmpty(),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 登录按钮（记录其中心坐标，驱动圆形揭示全屏过渡动画）
                        Button(
                            onClick = { viewModel.login(username, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .onGloballyPositioned { coordinates ->
                                    val bounds = coordinates.boundsInRoot()
                                    buttonCenter = bounds.center
                                },
                            enabled = !loginState.isLoading && username.isNotBlank() && password.isNotBlank(),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            if (loginState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(AppStrings.loggingIn(currentLang), fontSize = 15.sp)
                            } else {
                                Text(
                                    text = AppStrings.loginButton(currentLang),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // 自动登录勾选框（紧贴在登录按钮下方，保证 48dp 交互靶区）
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .clickable { viewModel.setAutoLogin(!autoLogin) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = autoLogin,
                                onCheckedChange = { viewModel.setAutoLogin(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppStrings.autoLoginCheckbox(currentLang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 底部内核状态微标
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Terminal,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Python: ${viewModel.pythonState.value}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // --- 从登录按钮展开到全屏的圆形涟漪揭示动画 ---
        if (expandProgress > 0f) {
            val configuration = LocalConfiguration.current
            val density = LocalDensity.current
            val screenDiagonal = with(density) {
                val w = configuration.screenWidthDp.dp.toPx()
                val h = configuration.screenHeightDp.dp.toPx()
                hypot(w, h) * 1.15f
            }
            val primaryColor = MaterialTheme.colorScheme.primary

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = if (buttonCenter != Offset.Zero) buttonCenter else this.center
                drawCircle(
                    color = primaryColor,
                    radius = screenDiagonal * expandProgress,
                    center = center
                )
            }
        }
    }
}
