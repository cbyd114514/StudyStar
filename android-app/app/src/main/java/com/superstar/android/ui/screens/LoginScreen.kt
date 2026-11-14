package com.superstar.android.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Security
import com.superstar.android.ui.components.ChaoXingLoginDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superstar.android.R
import com.superstar.android.ui.i18n.AppStrings
import com.superstar.android.ui.theme.ShapeBentoCard
import com.superstar.android.ui.theme.ShapeBentoHero
import com.superstar.android.ui.theme.ShapePill
import com.superstar.android.viewmodel.MainViewModel
import kotlin.math.hypot

/**
 * 学习星 (StudyStar) 登录界面
 * 遵循 Material Design 3 Expressive (2026 规范)
 * 自动登录与法律合规统一紧凑排布、去除冗余提示、全屏水波平滑揭示动效
 */
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()
    val autoLogin by viewModel.autoLogin.collectAsState()
    val legalAgreed by viewModel.legalAgreed.collectAsState()
    val userManuallyLoggedOut by viewModel.userManuallyLoggedOut.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var autoLoginAttempted by remember { mutableStateOf(false) }
    var showLegalDialog by remember { mutableStateOf(false) }
    var showChaoXingAuthDialog by remember { mutableStateOf(false) }

    // 展开动画状态与点击坐标
    var buttonCenter by remember { mutableStateOf(Offset.Zero) }
    var isExpandingToFullScreen by remember { mutableStateOf(false) }

    val expandProgress by animateFloatAsState(
        targetValue = if (isExpandingToFullScreen) 1f else 0f,
        animationSpec = tween(
            durationMillis = 520,
            easing = CubicBezierEasing(0.2f, 0.0f, 0.1f, 1.0f)
        ),
        label = "login_expand_anim",
        finishedListener = { progress ->
            if (progress >= 1f) {
                onLoginSuccess()
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            isExpandingToFullScreen = false
        }
    }

    // 从本地加载已保存账号密码
    val savedUsername = loginState.savedUsername
    val savedPassword = loginState.savedPassword
    LaunchedEffect(savedUsername, savedPassword) {
        if (!savedUsername.isNullOrBlank()) {
            username = savedUsername
        }
        if (!savedPassword.isNullOrBlank()) {
            password = savedPassword
        }

        // 自动登录判定
        if (autoLogin && legalAgreed && !userManuallyLoggedOut && !autoLoginAttempted &&
            !savedUsername.isNullOrBlank() && !savedPassword.isNullOrBlank() &&
            !loginState.isLoggedIn && !loginState.isLoading
        ) {
            autoLoginAttempted = true
            viewModel.login(savedUsername, savedPassword)
        }
    }

    LaunchedEffect(loginState.isLoggedIn) {
        if (loginState.isLoggedIn) {
            isExpandingToFullScreen = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface
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
                // 顶部语言快速切换胶囊（登录前前一级生效）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = ShapePill,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = "Language",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val isZh = currentLang == "zh"
                            Surface(
                                shape = ShapePill,
                                color = if (isZh) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .clip(ShapePill)
                                    .clickable { viewModel.setLanguage("zh") }
                            ) {
                                Text(
                                    text = "中文",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isZh) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isZh) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = ShapePill,
                                color = if (!isZh) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .clip(ShapePill)
                                    .clickable { viewModel.setLanguage("en") }
                            ) {
                                Text(
                                    text = "English",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (!isZh) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!isZh) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-0.02).sp
                )

                Text(
                    text = AppStrings.loginSubtitle(currentLang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 登录核心卡片 (M3 Bento Hero 28dp，去除了生硬提示)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeBentoHero,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (currentLang == "zh") "学习通账号登录" else "Xuexitong Account Login",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 紧凑型用户名输入框与紧随其后的小胶囊自动登录
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = username,
                                onValueChange = {
                                    username = it
                                    if (loginState.error != null) viewModel.clearLoginError()
                                },
                                label = { Text(AppStrings.usernameLabel(currentLang), fontSize = 12.sp) },
                                placeholder = { Text(AppStrings.usernamePlaceholder(currentLang), fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.AccountCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // 用户名后面的小封号/小复选“自动登录”
                            Surface(
                                shape = ShapePill,
                                color = if (autoLogin) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                                border = BorderStroke(1.dp, if (autoLogin) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .clip(ShapePill)
                                    .clickable { viewModel.setAutoLogin(!autoLogin) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = autoLogin,
                                        onCheckedChange = { viewModel.setAutoLogin(it) },
                                        modifier = Modifier.size(16.dp),
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.primary,
                                            uncheckedColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentLang == "zh") "自动登录" else "Auto",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (autoLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

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
                                    if (!legalAgreed) {
                                        Toast.makeText(context, AppStrings.legalAgreementRequired(currentLang), Toast.LENGTH_SHORT).show()
                                        showLegalDialog = true
                                        return@KeyboardActions
                                    }
                                    if (username.isNotBlank() && password.isNotBlank() && !loginState.isLoading) {
                                        viewModel.login(username, password)
                                    }
                                }
                            ),
                            shape = ShapeBentoCard
                        )

                        // 错误提示
                        if (loginState.error != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = ShapeBentoCard
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

                        // --- 自动登录与法律合规统一放置在同一区域（优雅并排排布） ---
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                // 法律合规与免责声明严格要求行
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(ShapePill)
                                        .clickable { viewModel.setLegalAgreed(!legalAgreed) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = legalAgreed,
                                        onCheckedChange = { viewModel.setLegalAgreed(it) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.primary,
                                            uncheckedColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = AppStrings.legalCheckboxPrefix(currentLang),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = AppStrings.legalTermsTitle(currentLang),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable { showLegalDialog = true }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 登录行动按钮 (Pill 胶囊形)
                        Button(
                            onClick = {
                                if (!legalAgreed) {
                                    Toast.makeText(context, AppStrings.legalAgreementRequired(currentLang), Toast.LENGTH_SHORT).show()
                                    showLegalDialog = true
                                    return@Button
                                }
                                viewModel.login(username, password)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .onGloballyPositioned { coordinates ->
                                    val bounds = coordinates.boundsInRoot()
                                    buttonCenter = bounds.center
                                },
                            enabled = !loginState.isLoading && username.isNotBlank() && password.isNotBlank(),
                            shape = ShapePill,
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
                                Text(AppStrings.loggingIn(currentLang), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text(
                                    text = AppStrings.loginButton(currentLang),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 跳转超星学习通官方网页/客户端授权登录
                        OutlinedButton(
                            onClick = {
                                if (!legalAgreed) {
                                    Toast.makeText(context, AppStrings.legalAgreementRequired(currentLang), Toast.LENGTH_SHORT).show()
                                    showLegalDialog = true
                                    return@OutlinedButton
                                }
                                showChaoXingAuthDialog = true
                            },
                            shape = ShapePill,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.chaoxingAuthLoginBtn(currentLang),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 登录成功时从按钮展开至全屏水波平滑过渡
        if (expandProgress > 0f) {
            val configuration = LocalConfiguration.current
            val density = LocalDensity.current
            val screenDiagonal = with(density) {
                val w = configuration.screenWidthDp.dp.toPx()
                val h = configuration.screenHeightDp.dp.toPx()
                hypot(w, h)
            }
            val primaryColor = MaterialTheme.colorScheme.primary
            val destBgColor = MaterialTheme.colorScheme.surface

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = if (buttonCenter != Offset.Zero) buttonCenter else Offset(size.width / 2f, size.height / 2f)
                val currentRadius = screenDiagonal * expandProgress

                drawCircle(
                    color = destBgColor,
                    radius = currentRadius,
                    center = center
                )

                val edgeAlpha = ((1f - expandProgress) * 0.35f).coerceIn(0f, 1f)
                if (edgeAlpha > 0.01f) {
                    drawCircle(
                        color = primaryColor.copy(alpha = edgeAlpha),
                        radius = currentRadius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx())
                    )
                }
            }
        }

        // 法律协议弹窗
        if (showChaoXingAuthDialog) {
            ChaoXingLoginDialog(
                onDismiss = { showChaoXingAuthDialog = false },
                onAuthSuccess = { cookies, uid ->
                    showChaoXingAuthDialog = false
                    viewModel.loginWithAuthCookies(cookies, uid)
                },
                isZh = currentLang == "zh"
            )
        }

        if (showLegalDialog) {
            LegalNoticeDialog(
                currentLang = currentLang,
                onDismiss = { showLegalDialog = false },
                onAgree = {
                    viewModel.setLegalAgreed(true)
                    showLegalDialog = false
                }
            )
        }
    }
}
