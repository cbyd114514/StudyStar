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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superstar.android.BuildConfig
import com.superstar.android.notification.NotificationHelper
import com.superstar.android.ui.components.ExpressiveUpdateDialog
import com.superstar.android.ui.i18n.AppStrings
import com.superstar.android.ui.theme.ShapeBentoCard
import com.superstar.android.ui.theme.ShapePill
import com.superstar.android.ui.theme.ShapeSquircle
import com.superstar.android.update.AppUpdateManager
import com.superstar.android.viewmodel.MainViewModel

/**
 * 学习星 (StudyStar) 设置与工具界面
 * 遵循 Material Design 3 Expressive 规范
 * 账号居首、退出在上、更新带开关与50字白话简述、日志默认收拢、开源与版权声明置底
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val loginState by viewModel.loginState.collectAsState()
    val logs by viewModel.deduplicatedLogs.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
    val autoCheckUpdate by viewModel.autoCheckUpdate.collectAsState()

    val isZh = currentLang == "zh"
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()

    // 运行日志默认收起来，放在小角落
    var isLogsExpanded by remember { mutableStateOf(false) }
    var showLegalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty() && isLogsExpanded && !listState.isScrollInProgress) {
            listState.scrollToItem(logs.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isZh) "设置与工具" else "Settings & Tools",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.02).sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- 1. 当前登录账号信息与退出登录 (放在最上面) ---
                item(key = "account_card_top", contentType = "card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeBentoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = ShapeSquircle,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = AppStrings.currentAccountLabel(currentLang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = loginState.username ?: loginState.savedUsername ?: (if (isZh) "已登录" else "Logged In"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = ShapePill,
                                    color = Color(0xFF2E7D32).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (isZh) "在线" else "Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 退出登录按钮 (位于上方账号卡片内)
                            Button(
                                onClick = {
                                    viewModel.logout()
                                    onBack()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                shape = ShapePill
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.logoutAccountBtn(currentLang),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // --- 2. 软件更新与自动检查开关 ---
                item(key = "update_card_section", contentType = "card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeBentoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // 标题栏与版本标识
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = ShapeSquircle,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.SystemUpdate,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = AppStrings.updateCheckTitle(currentLang),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${AppStrings.updateCurrentVerLabel(currentLang)}: v${BuildConfig.VERSION_NAME}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = ShapePill,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "v${BuildConfig.VERSION_NAME}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 自动检查更新独立开关
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = AppStrings.autoCheckUpdateLabel(currentLang),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = AppStrings.autoCheckUpdateDesc(currentLang),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = autoCheckUpdate,
                                        onCheckedChange = { viewModel.setAutoCheckUpdate(it) }
                                    )
                                }
                            }

                            // 如果检测到了新版本，在按钮与标题之间出现“更新内容”（严格控制在50字内，大白话）
                            if (updateStatus is AppUpdateManager.UpdateStatus.NewVersionAvailable) {
                                val info = (updateStatus as AppUpdateManager.UpdateStatus.NewVersionAvailable).info
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "${AppStrings.briefChangelogTitle(currentLang)} (v${info.versionName})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = info.briefChangelog.ifBlank {
                                                if (isZh) "全新升级界面与稳定性优化，修复已知问题。" else "UI improvements and stability fixes."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 手动检查新版本按钮
                            val isChecking = updateStatus is AppUpdateManager.UpdateStatus.Checking
                            Button(
                                onClick = { viewModel.checkForUpdates(manual = true) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = ShapePill,
                                enabled = !isChecking
                            ) {
                                if (isChecking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isZh) "正在检测更新..." else "Checking...")
                                } else {
                                    Icon(Icons.Outlined.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isZh) "检查新版本" else "Check for Updates",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 3. 运行日志小模块 (默认收起，放在小角落) ---
                item(key = "logs_corner_card", contentType = "card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeBentoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(ShapePill)
                                    .clickable { isLogsExpanded = !isLogsExpanded },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isZh) "精简运行日志" else "Compact Runtime Logs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                Surface(
                                    shape = ShapePill,
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Text(
                                        text = "${logs.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                if (isLogsExpanded) {
                                    IconButton(
                                        onClick = {
                                            val logText = logs.joinToString("\n") { "[${it.timestamp}] ${AppStrings.translateLog(NotificationHelper.humanize(it.message, currentLang), currentLang)}" }
                                            clipboardManager.setText(AnnotatedString(logText))
                                            Toast.makeText(context, if (isZh) "日志已复制" else "Logs copied", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Outlined.ContentCopy, contentDescription = "复制", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.clearLogs() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Outlined.DeleteSweep, contentDescription = "清空", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Icon(
                                    imageVector = if (isLogsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // 展开后才渲染日志列表
                            AnimatedVisibility(
                                visible = isLogsExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    if (logs.isEmpty()) {
                                        Text(
                                            text = if (isZh) "暂无运行日志" else "No logs recorded",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    } else {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(240.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            LazyColumn(
                                                state = listState,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                items(logs, key = { it.id }) { log ->
                                                    val color = when (log.type) {
                                                        MainViewModel.LogType.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
                                                        MainViewModel.LogType.SUCCESS -> Color(0xFF2E7D32)
                                                        MainViewModel.LogType.WARNING -> Color(0xFFE65100)
                                                        MainViewModel.LogType.ERROR -> MaterialTheme.colorScheme.error
                                                    }
                                                    // 强制双语翻译，杜绝英文模式下出现中文
                                                    val cleanMsg = AppStrings.translateLog(NotificationHelper.humanize(log.message, currentLang), currentLang)

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.Top
                                                    ) {
                                                        Text(
                                                            text = log.timestamp,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.outline,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 10.sp,
                                                            modifier = Modifier.width(60.dp)
                                                        )
                                                        Text(
                                                            text = cleanMsg,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = color,
                                                            fontSize = 11.sp,
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
                    }
                }

                // --- 4. 法律合规声明卡片 (写在下面) ---
                item(key = "legal_card_bottom", contentType = "card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeBentoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = ShapeSquircle,
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Gavel,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = AppStrings.settingsLegalCardTitle(currentLang),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = AppStrings.settingsLegalCardSubtitle(currentLang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { showLegalDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = ShapePill
                            ) {
                                Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(AppStrings.settingsLegalCardBtn(currentLang), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // --- 5. 开源项目地址与仓库链接 (写到最下面) ---
                item(key = "github_card_bottom", contentType = "card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeBentoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = ShapeSquircle,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Code,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isZh) "开源项目地址" else "GitHub Repository",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = AppStrings.GITHUB_REPO_NAME,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { uriHandler.openUri(AppStrings.GITHUB_RELEASES_URL) },
                                    modifier = Modifier.weight(1f),
                                    shape = ShapePill
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isZh) "发布页" else "Releases", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { uriHandler.openUri(AppStrings.GITHUB_REPO_URL) },
                                    modifier = Modifier.weight(1f),
                                    shape = ShapePill
                                ) {
                                    Icon(Icons.Outlined.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isZh) "源码仓库" else "Repository", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // --- 6. 灰色版权与非商业学术声明 (置于页面最底部) ---
                item(key = "footer_notice") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isZh) "学习星 (StudyStar) 仅供技术探索与学术研究使用" else "StudyStar is for educational and research purposes only",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "SuperStar_R Core Engine | Jetpack Compose Native",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // 应用内更新弹窗
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

        // 法律声明弹窗
        if (showLegalDialog) {
            LegalNoticeDialog(
                currentLang = currentLang,
                onDismiss = { showLegalDialog = false },
                onAgree = { showLegalDialog = false }
            )
        }
    }
}
