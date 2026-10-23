package com.superstar.android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superstar.android.ui.i18n.AppStrings
import com.superstar.android.ui.theme.StatusSuccess
import com.superstar.android.viewmodel.CourseInfo
import com.superstar.android.viewmodel.MainViewModel

/**
 * 学习星 (StudyStar) 控制台界面
 * 遵循 Google Material Design 3 规范与 120Hz/144Hz 极清高刷架构。
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

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // 本地配置状态
    var configSpeed by remember { mutableFloatStateOf(studyState.speed.toFloat()) }
    var configJobs by remember { mutableIntStateOf(studyState.jobs) }
    var configCourseIds by remember { mutableStateOf(studyState.courseIds ?: "") }

    // 监听外部配置状态（账号切换时自动同步已保存课程）
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

    // 始终自动在后台获取学习通课程列表（进入控制台后自动静默拉取）
    LaunchedEffect(loginState.isLoggedIn) {
        if (loginState.isLoggedIn && courses.isEmpty() && !isLoadingCourses) {
            viewModel.fetchCourses()
        }
    }

    // 从 studyState.llmConfig 同步（账号隔离：切换账号时若无配置则清空）
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

    // 自动保存当前大模型配置与 API Key 到当前账号
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

    // 自动保存核心刷课参数
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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = AppStrings.dashboardTitle(currentLang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${AppStrings.accountPrefix(currentLang)}${loginState.username ?: AppStrings.loggedIn(currentLang)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 状态与任务点执行卡片（操作按钮直接放置于状态卡片右侧） ---
            item(key = "status_card", contentType = "card") {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // 顶部状态指示栏（右侧集成“开始刷课”/“停止刷课”操作按钮）
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (studyState.isRunning) StatusSuccess
                                        else if (studyState.isCompleted) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (studyState.isRunning) AppStrings.statusRunning(currentLang)
                                    else if (studyState.isCompleted) AppStrings.statusCompleted(currentLang)
                                    else AppStrings.statusReady(currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (studyState.isRunning) AppStrings.statusDescRunning(currentLang)
                                    else if (studyState.isCompleted) AppStrings.statusDescCompleted(currentLang)
                                    else AppStrings.statusDescReady(currentLang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            if (studyState.isRunning) {
                                Button(
                                    onClick = { viewModel.stopStudy() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ),
                                    shape = MaterialTheme.shapes.medium,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(AppStrings.stopStudyBtn(currentLang), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.startStudy(
                                            courseIds = configCourseIds.ifBlank { null },
                                            speed = configSpeed.toDouble(),
                                            jobs = configJobs,
                                            llmConfig = buildLlmConfigJson()
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = MaterialTheme.shapes.medium,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(AppStrings.startStudyBtn(currentLang), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // 运行中实时章节与任务点双管线展示
                        if (studyState.isRunning) {
                            Spacer(modifier = Modifier.height(14.dp))
                            RunningProgressSection(
                                currentLang = currentLang,
                                currentCourse = studyState.currentCourse,
                                currentChapter = studyState.currentChapter,
                                videoProgress = studyState.videoProgress,
                                quizProgress = studyState.quizProgress,
                                progress = studyState.progress
                            )
                        }
                    }
                }
            }

            // --- 运行问题与重点提醒卡片：仅在出现异常问题时弹出来，运行正常时不显示 ---
            if (recentIssues.isNotEmpty()) {
                item(key = "issues_card", contentType = "card") {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.issuesTitle(currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = { viewModel.clearIssues() },
                                    shape = MaterialTheme.shapes.small,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(AppStrings.clearIssuesBtn(currentLang), fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                recentIssues.takeLast(5).reversed().forEach { issue ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = MaterialTheme.shapes.medium,
                                        color = if (issue.type == MainViewModel.LogType.ERROR)
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = issue.timestamp,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(top = 1.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = issue.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
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

            // --- 核心参数配置卡片（默认自动保存配置，无冗余保存按钮） ---
            item(key = "config_card", contentType = "card") {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.coreParams(currentLang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 速度调节（滑动即自动保存）
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

                        // 并发调节（滑动即自动保存）
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // 已选课程概览（统一通过下方课程列表勾选选取，禁止随意手动填入错乱数据）
                        val selectedCount = remember(configCourseIds) {
                            configCourseIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.size
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.LibraryBooks,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (selectedCount > 0) AppStrings.selectedCoursesCount(currentLang, selectedCount) else AppStrings.noCourseSelected(currentLang),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (selectedCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (selectedCount > 0) {
                                        Text(
                                            text = "ID: $configCourseIds",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 课程选择卡片头 ---
            item(key = "course_header", contentType = "card") {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (courses.isNotEmpty()) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = AppStrings.courseCountUnit(currentLang, courses.size),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            // 学习通课程网址说明小图标按钮（点击弹出窗口）
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

                        // 获取课程按钮
                        OutlinedButton(
                            onClick = {
                                showCourseList = true
                                viewModel.fetchCourses()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
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
                                Text(AppStrings.fetchCoursesBtn(currentLang))
                            }
                        }

                        // 课程列表展开状态提示（默认自动展开）
                        if (courses.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { showCourseList = !showCourseList },
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (showCourseList) AppStrings.hideCourseList(currentLang)
                                        else AppStrings.viewFetchedCourses(currentLang, courses.size),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium,
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

            // --- 虚拟化高性能课程列表项（支持多选与一键全选/清空，选后立即默认保存参数） ---
            if (showCourseList && courses.isNotEmpty()) {
                item(key = "course_multi_actions", contentType = "card") {
                    val selectedCount = remember(configCourseIds) {
                        configCourseIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.size
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = AppStrings.selectedCoursesSummary(currentLang, selectedCount, courses.size),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
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
                                    shape = MaterialTheme.shapes.small,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(AppStrings.selectAllCourses(currentLang), fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = {
                                        configCourseIds = ""
                                        viewModel.updateCourseSelection("")
                                        autoSaveCoreConfig(courseIds = "")
                                    },
                                    shape = MaterialTheme.shapes.small,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(AppStrings.clearSelectedCourses(currentLang), fontSize = 12.sp)
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
                }

                items(
                    items = courses,
                    key = { it.courseId },
                    contentType = { "course_item" }
                ) { course ->
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

            // --- AI 大模型答题配置卡片 ---
            item(key = "ai_config", contentType = "card") {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { showLlmSection = !showLlmSection },
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
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
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                LlmStatusBadge(
                                    llmTestState = llmTestState,
                                    configLlmProvider = configLlmProvider,
                                    currentLang = currentLang
                                )

                                // 小角落里的使用说明圆圈图标按钮（冒号/问号加一个圈，点击弹窗）
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
                            enter = fadeIn(animationSpec = tween(120)),
                            exit = fadeOut(animationSpec = tween(120))
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

            item(key = "bottom_spacer", contentType = "spacer") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // 学习通课程网址核对说明弹窗
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
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "https://mooc1-1.chaoxing.com/visit/interaction",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            uriHandler.openUri("https://mooc1-1.chaoxing.com/visit/interaction")
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.openWebBtn(currentLang))
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showCourseTutorialDialog = false }) {
                        Text(AppStrings.closeBtn(currentLang))
                    }
                }
            )
        }

        // AI 智能答题使用说明与快捷获取弹窗（包含详细教程与官方注册入口）
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
                                shape = MaterialTheme.shapes.small
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.deepseekBtn(currentLang), fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { uriHandler.openUri("https://cloud.siliconflow.cn/account/ak") },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.siliconFlowBtn(currentLang), fontSize = 11.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showAiGuideDialog = false }) {
                        Text(AppStrings.closeBtn(currentLang))
                    }
                }
            )
        }
    }
}

/**
 * 运行中实时章节与任务点双管线展示
 * 独立隔离重组范围，在视频秒级跳动时避免整个控制台重组
 */
@Composable
private fun RunningProgressSection(
    currentLang: String,
    currentCourse: String,
    currentChapter: String,
    videoProgress: String,
    quizProgress: String,
    progress: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 当前课程
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.LibraryBooks,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.currentCourseLabel(currentLang),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentCourse.ifBlank { AppStrings.connectingCourse(currentLang) },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 当前章节
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Layers,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.currentChapterLabel(currentLang),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentChapter.ifBlank { AppStrings.analyzingChapter(currentLang) },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 视频任务点进度
            if (videoProgress.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.videoProgressLabel(currentLang),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = videoProgress,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 测验答题进度
            if (quizProgress.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Quiz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.quizProgressLabel(currentLang),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = quizProgress,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progress_anim"
    )
    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(MaterialTheme.shapes.extraSmall),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.primaryContainer
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "${(progress * 100).toInt()}${AppStrings.completedSuffix(currentLang)}",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}


/**
 * LLM 连通状态微标
 */
@Composable
private fun LlmStatusBadge(
    llmTestState: MainViewModel.LlmTestState,
    configLlmProvider: String,
    currentLang: String
) {
    when (llmTestState) {
        is MainViewModel.LlmTestState.Success -> {
            Surface(
                shape = CircleShape,
                color = Color(0xFF2E7D32)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.llmConnectedBadge(currentLang),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        is MainViewModel.LlmTestState.Failure -> {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.llmFailedBadge(currentLang),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        else -> {
            if (configLlmProvider.isNotBlank()) {
                Surface(
                    shape = CircleShape,
                    color = StatusSuccess.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = AppStrings.aiEnabled(currentLang),
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusSuccess,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
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
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
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
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
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
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onSelect),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (isSelected) 3.dp else 1.dp
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
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${course.courseId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (isSelected) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = selectedLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = selectLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
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
                    label = { Text(provider) },
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
            shape = MaterialTheme.shapes.medium
        )

        // API Key 输入（带安全性隐藏切换，保护用户密钥防旁窥）
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
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = configLlmModel,
            onValueChange = onModelChange,
            label = { Text(AppStrings.modelLabel(currentLang)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        // 连通性测试结果反馈卡片（连通变绿，未连通变红）
        when (val state = llmTestState) {
            is MainViewModel.LlmTestState.Success -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                    border = BorderStroke(1.5.dp, Color(0xFF2E7D32))
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
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
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
            isLlmSuccess -> Color(0xFF2E7D32) // 变绿
            isLlmFailed -> MaterialTheme.colorScheme.error // 变红
            else -> MaterialTheme.colorScheme.tertiaryContainer
        }

        val buttonTextColor = when {
            isLlmSuccess || isLlmFailed -> Color.White
            else -> MaterialTheme.colorScheme.onTertiaryContainer
        }

        Button(
            onClick = onTestConnection,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
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
                Text(AppStrings.llmTesting(currentLang), fontWeight = FontWeight.SemiBold)
            } else {
                Text(AppStrings.testLlmBtn(currentLang), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
