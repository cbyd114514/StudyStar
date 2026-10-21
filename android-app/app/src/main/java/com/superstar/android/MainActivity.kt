package com.superstar.android

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.superstar.android.license.LicenseManager
import com.superstar.android.ui.i18n.AppStrings
import com.superstar.android.ui.screens.DashboardScreen
import com.superstar.android.ui.screens.LoginScreen
import com.superstar.android.ui.screens.SettingsScreen
import com.superstar.android.ui.theme.SuperStarTheme
import com.superstar.android.ui.theme.findActivity
import com.superstar.android.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (_: Throwable) {
            // 兼容特殊旧机型或极早期测试版本
        }

        // 硬件加速与高刷新率（120Hz / 144Hz）全局配置，彻底消除掉帧与卡顿
        setupHighRefreshRate()

        // Android 13+ (API 33+) 申请通知权限，确保后台/前台刷课报错与任务完成时能正常弹出系统通知
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
                }
            }
        } catch (_: Throwable) {
        }

        setContent {
            SuperStarTheme {
                SuperStarApp()
            }
        }
    }

    /**
     * 针对现代高刷屏旗舰机（如红米 K90 MAX、小米澎湃 OS 等 120Hz/144Hz 设备）
     * 申请系统最高刷新率物理模式，并解除 HyperOS 省电锁 60Hz 限制。
     */
    private fun setupHighRefreshRate() {
        try {
            // 强制启用窗口级硬件加速
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )

            // Android 11+ (API 30+)：查询屏幕物理支持模式并匹配最高刷新率（120Hz / 144Hz）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val currentDisplay = display ?: window.decorView.display
                val modes = currentDisplay?.supportedModes
                val targetMode = modes?.filter { it.refreshRate >= 119f }?.maxByOrNull { it.refreshRate }
                    ?: modes?.maxByOrNull { it.refreshRate }
                if (targetMode != null) {
                    val lp = window.attributes
                    lp.preferredDisplayModeId = targetMode.modeId
                    window.attributes = lp
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val lp = window.attributes
                lp.preferredRefreshRate = 120f
                window.attributes = lp
            }
        } catch (_: Throwable) {
            // 兼容不支持动态刷新率调节的特殊设备
        }

        // 针对 Android 14/15/16/17 (HyperOS 动态帧率引擎)：通过反射设置最小与最大高刷新率限制
        try {
            val minField = WindowManager.LayoutParams::class.java.getField("preferredMinDisplayRefreshRate")
            val maxField = WindowManager.LayoutParams::class.java.getField("preferredMaxDisplayRefreshRate")
            val lp = window.attributes
            minField.setFloat(lp, 120f)
            maxField.setFloat(lp, 144f)
            window.attributes = lp
        } catch (_: Throwable) {
        }
    }
}

@Composable
fun SuperStarApp(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentPage by viewModel.currentPage.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    var showSplash by remember { mutableStateOf(true) }
    var isCheckingLicense by remember { mutableStateOf(true) }
    var licenseDeniedMessage by remember { mutableStateOf<String?>(null) }

    // 异步快速检查许可证，不阻塞界面加载，完全防止死锁与崩溃
    LaunchedEffect(Unit) {
        try {
            val result = withContext(Dispatchers.IO) {
                LicenseManager.checkLicense()
            }
            if (!result.allowed) {
                licenseDeniedMessage = result.message
            }
        } catch (_: Throwable) {
            // 异常时默认放行，确保用户可用
        } finally {
            isCheckingLicense = false
        }
    }

    // 核心主视图
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentPage,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                fadeIn(tween(350)) togetherWith fadeOut(tween(350))
            },
            label = "page_transition"
        ) { page ->
            when (page) {
                MainViewModel.Page.LOGIN -> {
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = {
                            viewModel.navigateTo(MainViewModel.Page.MAIN)
                        }
                    )
                }
                MainViewModel.Page.MAIN -> {
                    MainScreen(viewModel = viewModel)
                }
            }
        }

        // 开头的许可证明微型浮层（Compose 纯原生物理层微型浮层，彻底杜绝 WindowManager$BadTokenException 闪退）
        AnimatedVisibility(
            visible = isCheckingLicense && !showSplash,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        ) {
            Surface(
                modifier = Modifier.wrapContentSize(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = AppStrings.verifyingLicense(currentLang),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 应用启动开场品牌动画（App Launch Animation）
        AnimatedVisibility(
            visible = showSplash,
            enter = fadeIn(),
            exit = fadeOut(tween(420, easing = FastOutSlowInEasing))
        ) {
            SplashScreen(
                appName = AppStrings.appName(currentLang),
                subtitle = AppStrings.loginSubtitle(currentLang),
                onFinish = { showSplash = false }
            )
        }

        // 如果明确被禁用，弹出阻止对话框
        if (licenseDeniedMessage != null) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = AppStrings.appDisabledTitle(currentLang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = licenseDeniedMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            context.findActivity()?.finishAffinity()
                            java.lang.System.exit(0)
                        }
                    ) {
                        Text(AppStrings.exitApp(currentLang))
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

/**
 * 应用开场品牌动画
 */
@Composable
private fun SplashScreen(
    appName: String,
    subtitle: String,
    onFinish: () -> Unit
) {
    var startAnim by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.65f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "splash_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 650),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        startAnim = true
        delay(1300)
        onFinish()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha)
            ) {
                Surface(
                    modifier = Modifier.size(108.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 8.dp,
                    border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = appName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(36.dp))

                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 登录后的主界面：沉浸式全面屏控制台，右上角设置按钮直通设置与日志
 */
@Composable
private fun MainScreen(
    viewModel: MainViewModel
) {
    var showSettings by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = showSettings,
        transitionSpec = {
            fadeIn(tween(250)) togetherWith fadeOut(tween(250))
        },
        label = "main_settings_transition"
    ) { inSettings ->
        if (inSettings) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { showSettings = false }
            )
        } else {
            DashboardScreen(
                viewModel = viewModel,
                onOpenSettings = { showSettings = true }
            )
        }
    }
}
