package com.superstar.android.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Key
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superstar.android.notification.NotificationHelper
import com.superstar.android.ui.components.DeepSeekLoginDialog
import com.superstar.android.ui.components.ExpressiveFloatingDock
import com.superstar.android.ui.components.ExpressiveUpdateDialog
import com.superstar.android.ui.components.WavyProgressIndicator
import com.superstar.android.ui.i18n.AppStrings
import com.superstar.android.ui.theme.ShapeBentoCard
import com.superstar.android.ui.theme.ShapeBentoHero
import com.superstar.android.ui.theme.ShapePill
import com.superstar.android.ui.theme.ShapeSquircle
import com.superstar.android.update.AppUpdateManager
import com.superstar.android.viewmodel.CourseInfo
import com.superstar.android.viewmodel.MainViewModel

/**
 * 学习星 (StudyStar) 控制台主界面
 * 遵循 Material Design 3 Expressive (2026 规范)
 * 纯色阶分层、Bento 卡片、正弦波浪进度、DeepSeek 网页免填 API 抓取、数字精确弹窗修改
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

    // 课程列表展开状态
    var isCourseListExpanded by remember { mutableStateOf(false) }

    // 数字精准编辑弹窗控制
    var showEditSpeedDialog by remember { mutableStateOf(false) }
    var tempSpeedInput by remember { mutableStateOf("") }

    var showEditJobsDialog by remember { mutableStateOf(false) }
    var tempJobsInput by remember { mutableStateOf("") }

    // DeepSeek 网页登录提取弹窗控制
    var showDeepSeekLoginDialog by remember { mutableStateOf(false) }
    var showManualApiKeyEdit by remember { mutableStateOf(false) }
    var manualApiKeyInput by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }

    // 从 studyState.llmConfig 同步 API Key
    LaunchedEffect(studyState.llmConfig) {
        val llmConfig = studyState.llmConfig
        if (!llmConfig.isNullOrBlank()) {
            try {
                val json = com.google.gson.Gson().fromJson(llmConfig, Map::class.java) as Map<*, *>
                manualApiKeyInput = json["key"]?.toString() ?: ""
            } catch (_: Exception) {}
        } else {
            manualApiKeyInput = ""
        }
    }

    // 自动在后台静默获取课程列表
    LaunchedEffect(loginState.isLoggedIn) {
        if (loginState.isLoggedIn && courses.isEmpty() && !isLoadingCourses) {
            viewModel.fetchCourses()
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
            llmConfig = studyState.llmConfig
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                // Top App Bar: 去除左上角手机号，默认与 Surface 同色无缝，向下滚动填充色阶
                TopAppBar(
                    title = {
                        Text(
                            text = AppStrings.appName(currentLang),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.02).sp
                        )
                    },
                    actions = {
                        // 语言快速切换胶囊 (中 / EN)
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

                        // 设置齿轮按钮（平滑直接进入，彻底去除闪屏水波）
                        IconButton(onClick = onOpenSettings) {
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
                                        text = info.briefChangelog.ifBlank { if (isZh) "点击查看更新内容并升级" else "Tap to view changelog and update" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                        maxLines = 2
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
                // Hero Card: 任务看板（Primary Container 色阶，28dp 圆角，正弦波浪进度条）
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

                                // 等宽超大数字
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

                            // 正弦波浪进度指示器 (Wavy Progress Indicator)
                            WavyProgressIndicator(
                                progress = studyState.progress,
                                isWavy = studyState.isRunning,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Bento 次卡片组（双列微卡片）
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

                        // DeepSeek 答题与测验微卡片
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
                                        if (isLlmEnabled && manualApiKeyInput.isNotBlank()) (if (isZh) "DeepSeek 答题就绪" else "DeepSeek Ready")
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

                // --- 3. 运行问题与重点提醒卡片 ---
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
                                        val cleanDisplayMessage = AppStrings.translateLog(NotificationHelper.humanize(issue.message, currentLang), currentLang)

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

                // --- 4. 彻底重构的待学课程一体化 Bento 卡片 (逻辑清晰，告别混乱) ---
                item(key = "course_integrated_card", contentType = "card") {
                    val selectedIds = remember(configCourseIds) {
                        configCourseIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    }
                    val selectedCount = selectedIds.size

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeBentoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // 标题行：左侧名称与数量，右侧刷新与展开/收起切换
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = AppStrings.courseListTitle(currentLang),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (courses.isNotEmpty()) {
                                            if (selectedCount > 0) AppStrings.selectedCoursesSummary(currentLang, selectedCount, courses.size)
                                            else AppStrings.viewFetchedCourses(currentLang, courses.size)
                                        } else {
                                            if (isZh) "暂未拉取课程，可点击右侧刷新" else "No courses cached, tap refresh"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selectedCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // 重新拉取课程图标按钮
                                IconButton(
                                    onClick = { viewModel.fetchCourses() },
                                    enabled = !isLoadingCourses,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    if (isLoadingCourses) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.CloudSync,
                                            contentDescription = "刷新课程",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // 展开/收起切换胶囊
                                Surface(
                                    shape = ShapePill,
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    modifier = Modifier
                                        .clip(ShapePill)
                                        .clickable { isCourseListExpanded = !isCourseListExpanded }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isCourseListExpanded) AppStrings.courseCollapseBtn(currentLang) else AppStrings.courseExpandBtn(currentLang),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Icon(
                                            imageVector = if (isCourseListExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // 未展开状态：只优雅展示已勾选的课程胶囊
                            if (!isCourseListExpanded && courses.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                if (selectedCount == 0) {
                                    Text(
                                        text = if (isZh) "当前未勾选任何课程，请点击右上角【展开课程列表】进行选择" else "No courses selected. Tap [Expand List] to choose.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    val selectedCourseNames = courses.filter { selectedIds.contains(it.courseId) }.map { it.title }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        selectedCourseNames.take(3).forEach { title ->
                                            Surface(
                                                shape = ShapePill,
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                            ) {
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                        if (selectedCourseNames.size > 3) {
                                            Text(
                                                text = "+${selectedCourseNames.size - 3}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.align(Alignment.CenterVertically)
                                            )
                                        }
                                    }
                                }
                            }

                            // 展开状态：提供快捷全选/清空操作行，以及垂直列表
                            AnimatedVisibility(
                                visible = isCourseListExpanded,
                                enter = expandVertically(animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f)) + fadeIn(animationSpec = tween(220)),
                                exit = shrinkVertically(animationSpec = spring(stiffness = 380f)) + fadeOut(animationSpec = tween(180))
                            ) {
                                Column(modifier = Modifier.padding(top = 14.dp)) {
                                    // 快捷操作行
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = AppStrings.multiSelectTip(currentLang),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    val allIds = courses.joinToString(",") { it.courseId }
                                                    configCourseIds = allIds
                                                    viewModel.updateCourseSelection(allIds)
                                                    autoSaveCoreConfig(courseIds = allIds)
                                                },
                                                shape = ShapePill,
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text(AppStrings.selectAllCourses(currentLang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    configCourseIds = ""
                                                    viewModel.updateCourseSelection("")
                                                    autoSaveCoreConfig(courseIds = "")
                                                },
                                                shape = ShapePill,
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text(AppStrings.clearSelectedCourses(currentLang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // 课程列表项
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        courses.forEach { course ->
                                            val isSelected = selectedIds.contains(course.courseId)
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .clickable {
                                                        val list = selectedIds.toMutableList()
                                                        if (list.contains(course.courseId)) list.remove(course.courseId)
                                                        else list.add(course.courseId)
                                                        val newIds = list.joinToString(",")
                                                        configCourseIds = newIds
                                                        viewModel.updateCourseSelection(newIds)
                                                        autoSaveCoreConfig(courseIds = newIds)
                                                    },
                                                shape = RoundedCornerShape(14.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                        else MaterialTheme.colorScheme.surface
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = isSelected,
                                                        onCheckedChange = {
                                                            val list = selectedIds.toMutableList()
                                                            if (list.contains(course.courseId)) list.remove(course.courseId)
                                                            else list.add(course.courseId)
                                                            val newIds = list.joinToString(",")
                                                            configCourseIds = newIds
                                                            viewModel.updateCourseSelection(newIds)
                                                            autoSaveCoreConfig(courseIds = newIds)
                                                        }
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
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 5. 核心参数调节卡片（支持点击数字弹窗精确编辑） ---
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

                            // 播放倍速调节（点击数字胶囊弹出精确输入）
                            EditableSliderRow(
                                icon = Icons.Outlined.Speed,
                                label = AppStrings.playSpeed(currentLang),
                                value = configSpeed,
                                valueDisplay = "${String.format("%.1f", configSpeed)}x",
                                valueRange = 0.5f..3.0f,
                                steps = 25,
                                onValueChange = { configSpeed = it },
                                onValueChangeFinished = { autoSaveCoreConfig(speed = configSpeed) },
                                onNumberClick = {
                                    tempSpeedInput = String.format("%.1f", configSpeed)
                                    showEditSpeedDialog = true
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 并发数调节（点击数字胶囊弹出精确输入）
                            EditableSliderRow(
                                icon = Icons.Outlined.Layers,
                                label = AppStrings.concurrencyJobs(currentLang),
                                value = configJobs.toFloat(),
                                valueDisplay = AppStrings.jobsUnit(currentLang, configJobs),
                                valueRange = 1f..8f,
                                steps = 7,
                                onValueChange = { configJobs = it.toInt() },
                                onValueChangeFinished = { autoSaveCoreConfig(jobs = configJobs) },
                                onNumberClick = {
                                    tempJobsInput = configJobs.toString()
                                    showEditJobsDialog = true
                                }
                            )
                        }
                    }
                }

                // --- 6. 纯粹专精的 DeepSeek 智能答题配置卡片 (网页免填 API 抓取) ---
                item(key = "deepseek_config_card", contentType = "card") {
                    val hasApiKey = manualApiKeyInput.isNotBlank()

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
                            // 标题行与独立 Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = ShapeSquircle,
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.AutoAwesome,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = AppStrings.deepseekConfigTitle(currentLang),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isZh) "默认使用最新极速推理模型 (deepseek-chat)" else "Default: deepseek-chat",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // 独立 Switch 开关
                                Switch(
                                    checked = hasApiKey && isLlmEnabled,
                                    onCheckedChange = { checked: Boolean ->
                                        if (!hasApiKey && checked) {
                                            showDeepSeekLoginDialog = true
                                        } else {
                                            viewModel.setLlmEnabled(checked)
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 状态徽标与连通性展示
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = ShapePill,
                                    color = if (hasApiKey) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (hasApiKey) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (hasApiKey) AppStrings.deepseekBoundBadge(currentLang) else AppStrings.deepseekUnboundBadge(currentLang),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasApiKey) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (hasApiKey) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "sk-****${manualApiKeyInput.takeLast(4)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 一键打开网页登录免填 API 主按钮
                            Button(
                                onClick = { showDeepSeekLoginDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = ShapePill,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Outlined.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.deepseekLoginBtn(currentLang),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // 展开手动修改 Key 区域
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (showManualApiKeyEdit) (if (isZh) "收起手动输入" else "Hide manual input") else (if (isZh) "需要手动输入 Key?" else "Manual API Key"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clip(ShapePill)
                                        .clickable { showManualApiKeyEdit = !showManualApiKeyEdit }
                                        .padding(vertical = 4.dp)
                                )

                                if (hasApiKey) {
                                    val isTesting = llmTestState is MainViewModel.LlmTestState.Testing
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.applyDeepSeekApiKey(manualApiKeyInput)
                                        },
                                        shape = ShapePill,
                                        enabled = !isTesting,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        if (isTesting) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                        } else {
                                            Text(AppStrings.testLlmBtn(currentLang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = showManualApiKeyEdit,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    OutlinedTextField(
                                        value = manualApiKeyInput,
                                        onValueChange = { manualApiKeyInput = it },
                                        label = { Text("DeepSeek API Key") },
                                        placeholder = { Text("sk-...") },
                                        singleLine = true,
                                        shape = ShapeBentoCard,
                                        trailingIcon = {
                                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                                Icon(
                                                    imageVector = if (isKeyVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (manualApiKeyInput.isNotBlank()) {
                                                viewModel.applyDeepSeekApiKey(manualApiKeyInput)
                                                showManualApiKeyEdit = false
                                                Toast.makeText(context, if (isZh) "API Key 已保存并测试" else "API Key saved and testing", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = ShapePill,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(AppStrings.saveBtn(currentLang), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 7. M3 Expressive 悬浮胶囊工具底栏 ---
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
                        llmConfig = studyState.llmConfig
                    )
                }
            },
            onQuickActionClick = {
                isCourseListExpanded = !isCourseListExpanded
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // --- 8. DeepSeek 网页免填 API 登录抓取 Dialog ---
        if (showDeepSeekLoginDialog) {
            DeepSeekLoginDialog(
                onDismiss = { showDeepSeekLoginDialog = false },
                onApiKeyCaptured = { capturedKey ->
                    manualApiKeyInput = capturedKey
                    viewModel.applyDeepSeekApiKey(capturedKey)
                },
                isZh = isZh
            )
        }

        // --- 9. 播放倍速精准编辑弹窗 ---
        if (showEditSpeedDialog) {
            AlertDialog(
                onDismissRequest = { showEditSpeedDialog = false },
                title = { Text(AppStrings.editSpeedDialogTitle(currentLang), fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(AppStrings.editSpeedHint(currentLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = tempSpeedInput,
                            onValueChange = { tempSpeedInput = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = ShapeBentoCard,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsed = tempSpeedInput.toFloatOrNull()
                            if (parsed != null && parsed in 0.5f..3.0f) {
                                configSpeed = parsed
                                autoSaveCoreConfig(speed = parsed)
                                showEditSpeedDialog = false
                            } else {
                                Toast.makeText(context, if (isZh) "请输入 0.5 到 3.0 之间的数值" else "Please enter 0.5 - 3.0", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = ShapePill
                    ) {
                        Text(AppStrings.saveBtn(currentLang), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showEditSpeedDialog = false }, shape = ShapePill) {
                        Text(AppStrings.cancelBtn(currentLang))
                    }
                }
            )
        }

        // --- 10. 并发数精准编辑弹窗 ---
        if (showEditJobsDialog) {
            AlertDialog(
                onDismissRequest = { showEditJobsDialog = false },
                title = { Text(AppStrings.editJobsDialogTitle(currentLang), fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(AppStrings.editJobsHint(currentLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = tempJobsInput,
                            onValueChange = { tempJobsInput = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = ShapeBentoCard,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsed = tempJobsInput.toIntOrNull()
                            if (parsed != null && parsed in 1..8) {
                                configJobs = parsed
                                autoSaveCoreConfig(jobs = parsed)
                                showEditJobsDialog = false
                            } else {
                                Toast.makeText(context, if (isZh) "请输入 1 到 8 之间的整数" else "Please enter 1 - 8", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = ShapePill
                    ) {
                        Text(AppStrings.saveBtn(currentLang), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showEditJobsDialog = false }, shape = ShapePill) {
                        Text(AppStrings.cancelBtn(currentLang))
                    }
                }
            )
        }

        // --- 11. 应用内自动更新弹窗 ---
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
    }
}

/**
 * 可点击数字精准输入的滑动调节条
 */
@Composable
private fun EditableSliderRow(
    icon: ImageVector,
    label: String,
    value: Float,
    valueDisplay: String,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    onNumberClick: () -> Unit
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
            // 可点击的数字微标
            Surface(
                shape = ShapePill,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .clip(ShapePill)
                    .clickable { onNumberClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = valueDisplay,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "修改",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(12.dp)
                    )
                }
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
