package com.superstar.android.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superstar.android.notification.NotificationHelper
import com.superstar.android.ui.components.ExpressiveFloatingDock
import com.superstar.android.ui.components.ExpressiveUpdateDialog
import com.superstar.android.ui.components.WavyProgressIndicator
import com.superstar.android.ui.i18n.AppStrings
import com.superstar.android.ui.theme.ShapeBentoCard
import com.superstar.android.ui.theme.ShapeBentoHero
import com.superstar.android.ui.theme.ShapePill
import com.superstar.android.ui.theme.ShapeSquircle
import com.superstar.android.ui.theme.StatusSuccess
import com.superstar.android.update.AppUpdateManager
import com.superstar.android.viewmodel.CourseInfo
import com.superstar.android.viewmodel.MainViewModel
import kotlin.math.hypot

/**
 * 学习星 (StudyStar) 控制台主界面
 * 严格遵循 Google Material Design 3 Expressive (2026 规范)
 * 5 级表面色阶替代投影、非对称 Bento Grid、物理弹簧动效、正弦波浪进度指示与悬浮胶囊工具栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit
) {
    val loginState by viewModel.loginState.collectAsState()
    val studyState by viewModel.studyState.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val isLoadingCourses by viewModel.isLoadingCourses.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val llmTestState by viewModel.llmTestState.collectAsState()
    val recentIssues by viewModel.recentIssues.collectAsState()
    val isLlmEnabled by viewModel.isLlmEnabled.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()

    val context = LocalContext.current
    val isZh = currentLang == "zh"
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // 刷课参数状态
    var configSpeed by remember { mutableFloatStateOf(studyState.speed.toFloat()) }
    var configJobs by remember { mutableIntStateOf(studyState.jobs) }
    var configCourseIds by remember { mutableStateOf(studyState.courseIds ?: "") }

    LaunchedEffect(studyState.courseIds) {
        configCourseIds = studyState.courseIds ?: ""
    }

    // LLM 配置状态
    var configLlmProvider by remember { mutableStateOf("") }
    var configLlmEndpoint by remember { mutableStateOf("") }
    var configLlmKey by remember { mutableStateOf("") }
    var configLlmModel by remember { mutableStateOf("") }

    var showCourseList by remember { mutableStateOf(false) }
    var showCourseTutorialDialog by remember { mutableStateOf(false) }
    var showLlmSection by remember { mutableStateOf(false) }
    var showAiGuideDialog by remember { mutableStateOf(false) }

    // 设置齿轮水波形铺开动画状态与坐标
    var gearCenter by remember { mutableStateOf(Offset.Zero) }
    var isSettingsOpening by remember { mutableStateOf(false) }

    val settingsRippleProgress by animateFloatAsState(
        targetValue = if (isSettingsOpening) 1f else 0f,
        animationSpec = tween(
            durationMillis = 520,
            easing = CubicBezierEasing(0.2f, 0.0f, 0.1f, 1.0f)
        ),
        label = "settings_ripple_anim",
        finishedListener = { progress ->
            if (progress >= 1f) {
                onOpenSettings()
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            isSettingsOpening = false
        }
    }

    // 自动在后台静默获取课程列表
    LaunchedEffect(loginState.isLoggedIn) {
        if (loginState.isLoggedIn && courses.isEmpty() && !isLoadingCourses) {
            viewModel.fetchCourses()
        }
    }

    // 从 studyState.llmConfig 同步配置
    LaunchedEffect(studyState.llmConfig) {
        val llmConfig = studyState.llmConfig
        if (!llmConfig.isNullOrBlank()) {
            try {
                val json = com.google.gson.Gson().fromJson(llmConfig, Map::class.java) as Map<*, *>
                configLlmProvider = json["provider"]?.toString() ?: ""
                configLlmEndpoint = json["endpoint"]?.toString() ?: ""
                configLlmKey = json["key"]?.toString() ?: ""
                configLlmModel = json["model"]?.toString() ?: ""
            } catch (_: Exception) {}
        } else {
            configLlmProvider = ""
            configLlmEndpoint = ""
            configLlmKey = ""
            configLlmModel = ""
        }
    }

    fun buildLlmConfigJson(
        provider: String = configLlmProvider,
        endpoint: String = configLlmEndpoint,
        key: String = configLlmKey,
        model: String = configLlmModel
    ): String? {
        if (provider.isBlank()) return null
        val map = linkedMapOf(
            "provider" to provider,
            "endpoint" to endpoint,
            "key" to key,
            "model" to model
        )
        return com.google.gson.Gson().toJson(map)
    }

    fun autoSaveLlm(
        provider: String = configLlmProvider,
        endpoint: String = configLlmEndpoint,
        key: String = configLlmKey,
        model: String = configLlmModel
    ) {
        val json = buildLlmConfigJson(provider, endpoint, key, model)
        if (json != null) {
            viewModel.saveUserLlmConfig(json)
            viewModel.updateConfig(
                speed = configSpeed.toDouble(),
                jobs = configJobs,
                courseIds = configCourseIds.ifBlank { null },
                llmConfig = json
            )
        }
    }

    fun autoSaveCoreConfig(
        speed: Float = configSpeed,
        jobs: Int = configJobs,
        courseIds: String = configCourseIds
    ) {
        viewModel.updateConfig(
            speed = speed.toDouble(),
            jobs = jobs,
            courseIds = courseIds.ifBlank { null },
            llmConfig = buildLlmConfigJson()
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                // M3 Expressive Top App Bar：默认与 Surface 同色无边界，滚动平滑过渡为 Surface Container
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = AppStrings.appName(currentLang),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.02).sp
                            )
                            Text(
                                text = "${AppStrings.accountPrefix(currentLang)}${loginState.username ?: AppStrings.loggedIn(currentLang)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        // 语言切换 Pill 胶囊
                        Surface(
                            shape = ShapePill,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = ShapePill,
                                    color = if (isZh) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier
                                        .clip(ShapePill)
                                        .clickable { viewModel.setLanguage("zh") }
                                ) {
                                    Text(
                                        text = "中",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isZh) FontWeight.Bold else FontWeight.Medium,
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
                                        text = "EN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (!isZh) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!isZh) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // 设置齿轮按钮（水波纹过渡）
                        IconButton(
                            onClick = {
                                if (!isSettingsOpening) {
                                    isSettingsOpening = true
                                }
                            },
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                val bounds = coordinates.boundsInRoot()
                                gearCenter = bounds.center
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = AppStrings.settingsTooltip(currentLang),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- 1. 新版本可用提醒微报卡片 (Bento Update Banner) ---
                if (updateStatus is AppUpdateManager.UpdateStatus.NewVersionAvailable) {
                    val info = (updateStatus as AppUpdateManager.UpdateStatus.NewVersionAvailable).info
                    item(key = "update_banner", contentType = "card") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ShapeBentoCard)
                                .clickable { viewModel.openUpdateDialog() },
                            shape = ShapeBentoCard,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = ShapeSquircle,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.SystemUpdate,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = AppStrings.updateAvailableBanner(currentLang, info.versionName),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = if (isZh) "点击查看更新日志与一键下载安装" else "Tap to view changelog and update in-app",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // --- 2. 核心非对称 Bento Grid 看板 ---
                // Hero Card: 核心任务看板（Primary Container 色阶，28dp 圆角，正弦波浪进度条）
                item(key = "bento_hero_card", contentType = "card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeBentoHero,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // 顶部状态指示胶囊与课程名
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = ShapePill,
                                    color = if (studyState.isRunning) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (studyState.isRunning) AppStrings.statusRunning(currentLang)
                                            else if (studyState.isCompleted) AppStrings.statusCompleted(currentLang)
                                            else AppStrings.statusReady(currentLang),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                if (studyState.currentCourse.isNotBlank()) {
                                    Text(
                                        text = studyState.currentCourse,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f, fill = false).padding(start = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 章节标题与大进度指标对比排版
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isZh) "当前学习章节" else "Current Chapter",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = studyState.currentChapter.ifBlank {
                                            if (studyState.isRunning) AppStrings.analyzingChapter(currentLang)
                                            else if (isZh) "尚未开始学习任务" else "No active study task"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 2
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // 杂志级等宽超大数字
                                Text(
                                    text = "${(studyState.progress * 100).toInt()}%",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Default,
                                    letterSpacing = (-0.03).sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 标志性正弦波浪进度指示器 (Wavy Progress Indicator)
                            WavyProgressIndicator(
                                progress = studyState.progress,
                                isWavy = studyState.isRunning,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Bento 次卡片组（双列微卡片，Surface Container Low 色阶）
                item(key = "bento_sub_cards", contentType = "card") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 视频播放任务点微卡片
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = ShapeBentoCard,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = ShapeSquircle,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.PlayCircleOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = AppStrings.videoProgressLabel(currentLang),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = studyState.videoProgress.ifBlank {
                                        if (studyState.isRunning) (if (isZh) "任务点同步中" else "Syncing...") else (if (isZh) "待机" else "Idle")
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // AI 测验作答微卡片
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = ShapeBentoCard,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = ShapeSquircle,
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.Quiz,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = AppStrings.quizProgressLabel(currentLang),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = studyState.quizProgress.ifBlank {
                                        if (isLlmEnabled) (if (isZh) "AI 答题就绪" else "AI Ready")
                                        else (if (isZh) "内置题库匹配" else "Built-in Solver")
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // --- 3. 运行问题与重点提醒卡片（仅在有异常时平滑展开） ---
                item(key = "issues_card_container") {
                    AnimatedVisibility(
                        visible = recentIssues.isNotEmpty(),
                        enter = expandVertically(animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f)) + fadeIn(animationSpec = tween(220)),
                        exit = shrinkVertically(animationSpec = spring(stiffness = 380f)) + fadeOut(animationSpec = tween(180))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = ShapeBentoCard,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = AppStrings.issuesTitle(currentLang),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedButton(
                                        onClick = { viewModel.clearIssues() },
                                        shape = ShapePill,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = AppStrings.clearIssuesBtn(currentLang),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    recentIssues.takeLast(5).reversed().forEach { issue ->
                                        val isError = issue.type == MainViewModel.LogType.ERROR
                                        val cleanDisplayMessage = NotificationHelper.humanize(issue.message, currentLang)

                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = ShapePill,
                                                    color = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                                            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = issue.timestamp,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = cleanDisplayMessage,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 4. 课程选择卡片 ---
                item(key = "course_header", contentType = "card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeBentoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.LibraryBooks,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.courseSelection(currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (courses.isNotEmpty()) {
                                    Surface(
                                        shape = ShapePill,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = AppStrings.courseCountUnit(currentLang, courses.size),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                IconButton(
                                    onClick = { showCourseTutorialDialog = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = AppStrings.webVerifyTitle(currentLang),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 获取课程按钮 (Pill 胶囊形)
                            OutlinedButton(
                                onClick = {
                                    showCourseList = true
                                    viewModel.fetchCourses()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = ShapePill,
                                enabled = !isLoadingCourses
                            ) {
                                if (isLoadingCourses) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(AppStrings.fetchingCourses(currentLang))
                                } else {
                                    Icon(Icons.Outlined.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(AppStrings.fetchCoursesBtn(currentLang), fontWeight = FontWeight.Bold)
                                }
                            }

                            if (courses.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(ShapePill)
                                        .clickable { showCourseList = !showCourseList },
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (showCourseList) AppStrings.hideCourseList(currentLang)
                                            else AppStrings.viewFetchedCourses(currentLang, courses.size),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = if (showCourseList) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 虚拟化高性能课程多选列表（带弹簧展开动效） ---
                item(key = "course_list_collapsible") {
                    AnimatedVisibility(
                        visible = showCourseList && courses.isNotEmpty(),
                        enter = expandVertically(animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f)) + fadeIn(animationSpec = tween(220)),
                        exit = shrinkVertically(animationSpec = spring(stiffness = 380f)) + fadeOut(animationSpec = tween(180))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val selectedCount = remember(configCourseIds) {
                                configCourseIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.size
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = ShapeBentoCard,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = AppStrings.selectedCoursesSummary(currentLang, selectedCount, courses.size),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedButton(
                                            onClick = {
                                                val allIds = courses.joinToString(",") { it.courseId }
                                                configCourseIds = allIds
                                                viewModel.updateCourseSelection(allIds)
                                                autoSaveCoreConfig(courseIds = allIds)
                                            },
                                            shape = ShapePill,
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(AppStrings.selectAllCourses(currentLang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        OutlinedButton(
                                            onClick = {
                                                configCourseIds = ""
                                                viewModel.updateCourseSelection("")
                                                autoSaveCoreConfig(courseIds = "")
                                            },
                                            shape = ShapePill,
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(AppStrings.clearSelectedCourses(currentLang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = AppStrings.multiSelectTip(currentLang),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            courses.forEach { course ->
                                val isSelected = remember(configCourseIds, course.courseId) {
                                    val ids = configCourseIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    ids.contains(course.courseId)
                                }
                                CourseItemCard(
                                    course = course,
                                    isSelected = isSelected,
                                    selectLabel = AppStrings.selectCourse(currentLang),
                                    selectedLabel = AppStrings.selectedCourse(currentLang),
                                    onSelect = {
                                        val currentIds = configCourseIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                                        if (currentIds.contains(course.courseId)) {
                                            currentIds.remove(course.courseId)
                                        } else {
                                            currentIds.add(course.courseId)
                                        }
                                        val newCourseIds = currentIds.joinToString(",")
                                        configCourseIds = newCourseIds
                                        viewModel.updateCourseSelection(newCourseIds)
                                        autoSaveCoreConfig(courseIds = newCourseIds)
                                    }
                                )
                            }
                        }
                    }
                }

                // --- 5. 核心参数调节卡片 ---
                item(key = "config_card", contentType = "card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeBentoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.coreParams(currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 速度调节
                            MaterialSliderRow(
                                icon = Icons.Outlined.Speed,
                                label = AppStrings.playSpeed(currentLang),
                                value = configSpeed,
                                valueDisplay = "${String.format("%.1f", configSpeed)}x",
                                valueRange = 0.5f..3.0f,
                                steps = 25,
                                onValueChange = { configSpeed = it },
                                onValueChangeFinished = {
                                    autoSaveCoreConfig(speed = configSpeed)
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 并发调节
                            MaterialSliderRow(
                                icon = Icons.Outlined.Layers,
                                label = AppStrings.concurrencyJobs(currentLang),
                                value = configJobs.toFloat(),
                                valueDisplay = AppStrings.jobsUnit(currentLang, configJobs),
                                valueRange = 1f..8f,
                                steps = 7,
                                onValueChange = { configJobs = it.toInt() },
                                onValueChangeFinished = {
                                    autoSaveCoreConfig(jobs = configJobs)
                                }
                            )
                        }
                    }
                }

                // --- 6. AI 智能答题配置卡片 (含独立开关) ---
                item(key = "ai_config", contentType = "card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeBentoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(ShapePill)
                                        .clickable { showLlmSection = !showLlmSection },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = AppStrings.aiConfigTitle(currentLang),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                val hasApiKey = configLlmKey.isNotBlank()
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // 状态微标
                                    if (llmTestState is MainViewModel.LlmTestState.Success) {
                                        Surface(
                                            shape = ShapePill,
                                            color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                                            modifier = Modifier.padding(end = 6.dp)
                                        ) {
                                            Text(
                                                text = AppStrings.llmConnectedBadge(currentLang),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (llmTestState is MainViewModel.LlmTestState.Failure) {
                                        Surface(
                                            shape = ShapePill,
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                            modifier = Modifier.padding(end = 6.dp)
                                        ) {
                                            Text(
                                                text = AppStrings.llmFailedBadge(currentLang),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // 独立 Switch 开关
                                    Box(
                                        modifier = Modifier.clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            enabled = !hasApiKey
                                        ) {
                                            showLlmSection = true
                                            Toast.makeText(
                                                context,
                                                if (isZh) "请先在下方配置并保存 API Key" else "Please configure and save API Key below first",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    ) {
                                        Switch(
                                            checked = hasApiKey && isLlmEnabled,
                                            onCheckedChange = { checked: Boolean ->
                                                viewModel.setLlmEnabled(checked)
                                            },
                                            enabled = hasApiKey,
                                            modifier = Modifier.padding(end = 2.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { showAiGuideDialog = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                            contentDescription = AppStrings.aiGuideTitle(currentLang),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { showLlmSection = !showLlmSection },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showLlmSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = showLlmSection,
                                enter = expandVertically(animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f)) + fadeIn(animationSpec = tween(220)),
                                exit = shrinkVertically(animationSpec = spring(stiffness = 380f)) + fadeOut(animationSpec = tween(180))
                            ) {
                                AiConfigForm(
                                    currentLang = currentLang,
                                    configLlmProvider = configLlmProvider,
                                    configLlmEndpoint = configLlmEndpoint,
                                    configLlmKey = configLlmKey,
                                    configLlmModel = configLlmModel,
                                    llmTestState = llmTestState,
                                    onProviderChange = { provider ->
                                        configLlmProvider = provider
                                        var endpoint = configLlmEndpoint
                                        var model = configLlmModel
                                        if (provider == "DeepSeek") {
                                            endpoint = "https://api.deepseek.com/v1/chat/completions"
                                            model = "deepseek-chat"
                                        } else if (provider == "SiliconFlow") {
                                            endpoint = "https://api.siliconflow.cn/v1/chat/completions"
                                            model = "deepseek-ai/DeepSeek-V3"
                                        }
                                        configLlmEndpoint = endpoint
                                        configLlmModel = model
                                        viewModel.clearLlmTestState()
                                        autoSaveLlm(provider = provider, endpoint = endpoint, model = model)
                                    },
                                    onEndpointChange = {
                                        configLlmEndpoint = it
                                        viewModel.clearLlmTestState()
                                        autoSaveLlm(endpoint = it)
                                    },
                                    onKeyChange = {
                                        configLlmKey = it
                                        viewModel.clearLlmTestState()
                                        autoSaveLlm(key = it)
                                    },
                                    onModelChange = {
                                        configLlmModel = it
                                        viewModel.clearLlmTestState()
                                        autoSaveLlm(model = it)
                                    },
                                    onTestConnection = {
                                        val json = buildLlmConfigJson()
                                        if (json != null) {
                                            viewModel.saveUserLlmConfig(json)
                                            viewModel.testLlmConnection(json)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 7. M3 Expressive 悬浮胶囊工具底栏 (Floating Capsule Dock) ---
        ExpressiveFloatingDock(
            statusText = if (studyState.isRunning) AppStrings.statusRunning(currentLang)
                         else if (studyState.isCompleted) AppStrings.statusCompleted(currentLang)
                         else AppStrings.statusReady(currentLang),
            statusDesc = if (studyState.isRunning) studyState.currentCourse.ifBlank { AppStrings.statusDescRunning(currentLang) }
                         else if (studyState.isCompleted) AppStrings.statusDescCompleted(currentLang)
                         else AppStrings.statusDescReady(currentLang),
            isRunning = studyState.isRunning,
            fabText = if (studyState.isRunning) AppStrings.stopStudyBtn(currentLang) else AppStrings.startStudyBtn(currentLang),
            onFabClick = {
                if (studyState.isRunning) {
                    viewModel.stopStudy()
                } else {
                    viewModel.startStudy(
                        courseIds = configCourseIds.ifBlank { null },
                        speed = configSpeed.toDouble(),
                        jobs = configJobs,
                        llmConfig = buildLlmConfigJson()
                    )
                }
            },
            onQuickActionClick = {
                showCourseList = !showCourseList
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // --- 8. 应用内自动更新高质感弹窗 (ExpressiveUpdateDialog) ---
        if (showUpdateDialog && updateStatus is AppUpdateManager.UpdateStatus.NewVersionAvailable) {
            val info = (updateStatus as AppUpdateManager.UpdateStatus.NewVersionAvailable).info
            ExpressiveUpdateDialog(
                updateInfo = info,
                updateStatus = updateStatus,
                downloadProgress = downloadProgress,
                onStartDownload = {
                    viewModel.startDownloadUpdate(info.downloadUrl)
                },
                onInstallApk = {
                    if (updateStatus is AppUpdateManager.UpdateStatus.Downloaded) {
                        viewModel.installDownloadedApk((updateStatus as AppUpdateManager.UpdateStatus.Downloaded).apkFile)
                    }
                },
                onDismiss = {
                    viewModel.dismissUpdateDialog()
                },
                isZh = isZh
            )
        }

        // 课程网址核对说明弹窗
        if (showCourseTutorialDialog) {
            val uriHandler = LocalUriHandler.current
            AlertDialog(
                onDismissRequest = { showCourseTutorialDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = AppStrings.webVerifyTitle(currentLang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = AppStrings.webVerifyDesc(currentLang),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                        Surface(
                            shape = ShapeBentoCard,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "https://mooc1-1.chaoxing.com/visit/interaction",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { uriHandler.openUri("https://mooc1-1.chaoxing.com/visit/interaction") },
                        shape = ShapePill
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.openWebBtn(currentLang), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showCourseTutorialDialog = false },
                        shape = ShapePill
                    ) {
                        Text(AppStrings.closeBtn(currentLang))
                    }
                }
            )
        }

        // AI 智能答题使用说明弹窗
        if (showAiGuideDialog) {
            val uriHandler = LocalUriHandler.current
            AlertDialog(
                onDismissRequest = { showAiGuideDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = AppStrings.aiGuideTitle(currentLang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = AppStrings.aiGuideDesc(currentLang),
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppStrings.aiQuickUrls(currentLang),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { uriHandler.openUri("https://platform.deepseek.com/api_keys") },
                                modifier = Modifier.weight(1f),
                                shape = ShapePill
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.deepseekBtn(currentLang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { uriHandler.openUri("https://cloud.siliconflow.cn/account/ak") },
                                modifier = Modifier.weight(1f),
                                shape = ShapePill
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.siliconFlowBtn(currentLang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAiGuideDialog = false },
                        shape = ShapePill
                    ) {
                        Text(AppStrings.closeBtn(currentLang), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // --- 设置齿轮水波形铺开动画 ---
        if (settingsRippleProgress > 0f) {
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
                val center = if (gearCenter != Offset.Zero) gearCenter else Offset(size.width - 48.dp.toPx(), 48.dp.toPx())
                val currentRadius = screenDiagonal * settingsRippleProgress

                // 水波主体平滑铺满全屏
                drawCircle(
                    color = destBgColor,
                    radius = currentRadius,
                    center = center
                )

                // 水波边缘过渡光晕
                drawCircle(
                    color = primaryColor.copy(alpha = (0.28f * (1f - settingsRippleProgress * 0.7f)).coerceIn(0f, 1f)),
                    radius = currentRadius,
                    center = center,
                    style = Stroke(width = 8.dp.toPx())
                )

                // 第一道同心水波涟漪环
                val wave1Radius = currentRadius * 1.12f + 16.dp.toPx()
                val wave1Alpha = ((1f - settingsRippleProgress) * 0.5f).coerceIn(0f, 1f)
                if (wave1Alpha > 0.01f) {
                    drawCircle(
                        color = primaryColor.copy(alpha = wave1Alpha),
                        radius = wave1Radius,
                        center = center,
                        style = Stroke(width = 5.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialSliderRow(
    icon: ImageVector,
    label: String,
    value: Float,
    valueDisplay: String,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = ShapePill,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = valueDisplay,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        )
    }
}

@Composable
private fun CourseItemCard(
    course: CourseInfo,
    isSelected: Boolean,
    selectLabel: String,
    selectedLabel: String,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeBentoCard)
            .clickable(onClick = onSelect),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = ShapeBentoCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${course.courseId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = ShapePill,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = if (isSelected) selectedLabel else selectLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AiConfigForm(
    currentLang: String,
    configLlmProvider: String,
    configLlmEndpoint: String,
    configLlmKey: String,
    configLlmModel: String,
    llmTestState: MainViewModel.LlmTestState,
    onProviderChange: (String) -> Unit,
    onEndpointChange: (String) -> Unit,
    onKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onTestConnection: () -> Unit
) {
    var isKeyVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = AppStrings.selectProviderTip(currentLang),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val otherLabel = AppStrings.otherAi(currentLang)
            listOf("DeepSeek", "SiliconFlow", otherLabel).forEach { provider ->
                val isSelected = if (provider == otherLabel) {
                    configLlmProvider == otherLabel || configLlmProvider == "AI" || (configLlmProvider.isNotBlank() && configLlmProvider != "DeepSeek" && configLlmProvider != "SiliconFlow")
                } else {
                    configLlmProvider == provider
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { onProviderChange(provider) },
                    label = { Text(provider, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = ShapePill,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        OutlinedTextField(
            value = configLlmEndpoint,
            onValueChange = onEndpointChange,
            label = { Text(AppStrings.endpointLabel(currentLang)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = ShapeBentoCard
        )

        OutlinedTextField(
            value = configLlmKey,
            onValueChange = onKeyChange,
            label = { Text(AppStrings.apiKeyLabel(currentLang)) },
            placeholder = { Text("sk-...") },
            trailingIcon = {
                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                    Icon(
                        imageVector = if (isKeyVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (isKeyVisible) "隐藏 API Key" else "显示 API Key"
                    )
                }
            },
            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = ShapeBentoCard
        )

        OutlinedTextField(
            value = configLlmModel,
            onValueChange = onModelChange,
            label = { Text(AppStrings.modelLabel(currentLang)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = ShapeBentoCard
        )

        // 连通性测试结果反馈微标
        when (val state = llmTestState) {
            is MainViewModel.LlmTestState.Success -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeBentoCard,
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                    border = BorderStroke(1.2.dp, Color(0xFF2E7D32))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = AppStrings.llmSuccess(currentLang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                }
            }
            is MainViewModel.LlmTestState.Failure -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeBentoCard,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = AppStrings.llmFailed(currentLang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            else -> {}
        }

        val isTestingLlm = llmTestState is MainViewModel.LlmTestState.Testing
        val isLlmSuccess = llmTestState is MainViewModel.LlmTestState.Success
        val isLlmFailed = llmTestState is MainViewModel.LlmTestState.Failure

        val buttonBgColor = when {
            isLlmSuccess -> Color(0xFF2E7D32)
            isLlmFailed -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.tertiaryContainer
        }

        val buttonTextColor = when {
            isLlmSuccess || isLlmFailed -> Color.White
            else -> MaterialTheme.colorScheme.onTertiaryContainer
        }

        Button(
            onClick = onTestConnection,
            modifier = Modifier.fillMaxWidth(),
            shape = ShapePill,
            enabled = !isTestingLlm && configLlmProvider.isNotBlank() && configLlmKey.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonBgColor,
                contentColor = buttonTextColor,
                disabledContainerColor = buttonBgColor.copy(alpha = 0.6f),
                disabledContentColor = buttonTextColor.copy(alpha = 0.7f)
            )
        ) {
            if (isTestingLlm) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = buttonTextColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(AppStrings.llmTesting(currentLang), fontWeight = FontWeight.Bold)
            } else {
                Text(AppStrings.testLlmBtn(currentLang), fontWeight = FontWeight.Bold)
            }
        }
    }
}
