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
import androidx.compose.foundation.shape.CircleShape
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
import com.superstar.android.ui.theme.StatusInfo
import com.superstar.android.ui.theme.StatusSuccess
import com.superstar.android.ui.theme.StatusWarning
import com.superstar.android.update.AppUpdateManager
import com.superstar.android.viewmodel.MainViewModel

/**
 * 学习星 (StudyStar) 设置与工具界面
 * 遵循 Google Material Design 3 Expressive (2026 规范)
 * 纯色阶分层、Bento 卡片、胶囊形状、应用内一键检查更新与系统自动安装
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

    val isZh = currentLang == "zh"
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    var isLogsExpanded by remember { mutableStateOf(true) }
    var showLegalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty() && !listState.isScrollInProgress) {
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
                            text = if (isZh) "学习星 设置与工具" else "StudyStar Settings & Tools",
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
                // --- 1. 应用版本与检查更新 Bento 卡片 ---
                item(key = "update_card", contentType = "card") {
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
                                    Text(if (isZh) "正在连接 GitHub 检查..." else "Checking releases...")
                                } else {
                                    Icon(Icons.Outlined.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isZh) "检查 GitHub 最新版本" else "Check for Latest Releases",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 2. 项目开源仓库 Bento 卡片 ---
                item(key = "github_card", contentType = "card") {
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
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Code,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
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
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { uriHandler.openUri(AppStrings.GITHUB_RELEASES_URL) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = ShapePill
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.githubReleasesBtn(currentLang),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { uriHandler.openUri(AppStrings.GITHUB_REPO_URL) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = ShapePill
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Code,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.githubRepoBtn(currentLang),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // --- 3. 法律合规与用户使用协议 Bento 卡片 ---
                item(key = "legal_terms_card", contentType = "card") {
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
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Gavel,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
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
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = { showLegalDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = ShapePill
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.settingsLegalCardBtn(currentLang),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // --- 4. 当前登录账号信息与退出登录 Bento 卡片 ---
                item(key = "account_card", contentType = "card") {
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
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isZh) "当前登录账号" else "Current Account",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = loginState.username ?: (if (isZh) "已登录" else "Logged In"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
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
                                    text = if (isZh) "退出当前学习通账号" else "Log Out",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // --- 5. 脱敏运行日志记录 Bento 卡片 ---
                item(key = "logs_header", contentType = "card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeBentoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(ShapePill)
                                        .clickable { isLogsExpanded = !isLogsExpanded },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isZh) "精简运行日志" else "Runtime Logs",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = ShapePill,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${logs.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val logText = logs.joinToString("\n") { "[${it.timestamp}] ${NotificationHelper.humanize(it.message, currentLang)}" }
                                        clipboardManager.setText(AnnotatedString(logText))
                                        Toast.makeText(context, if (isZh) "日志已安全复制到剪贴板" else "Logs copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = "复制日志",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.clearLogs() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteSweep,
                                        contentDescription = "清空日志",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { isLogsExpanded = !isLogsExpanded },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLogsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isLogsExpanded,
                                enter = expandVertically(animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f)) + fadeIn(animationSpec = tween(220)),
                                exit = shrinkVertically(animationSpec = spring(stiffness = 380f)) + fadeOut(animationSpec = tween(180))
                            ) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    if (logs.isEmpty()) {
                                        Text(
                                            text = if (isZh) "暂无运行日志" else "No logs available",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 12.dp)
                                        )
                                    } else {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(280.dp),
                                            shape = ShapeBentoCard,
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            LazyColumn(
                                                state = listState,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                items(logs, key = { it.id }) { log ->
                                                    val color = when (log.type) {
                                                        MainViewModel.LogType.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
                                                        MainViewModel.LogType.SUCCESS -> Color(0xFF2E7D32)
                                                        MainViewModel.LogType.WARNING -> Color(0xFFE65100)
                                                        MainViewModel.LogType.ERROR -> MaterialTheme.colorScheme.error
                                                    }
                                                    val cleanMsg = NotificationHelper.humanize(log.message, currentLang)

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
                                                            modifier = Modifier.width(62.dp)
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
            }
        }

        // --- 应用内更新弹窗 ---
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

        // 法律合规声明对话框
        if (showLegalDialog) {
            LegalNoticeDialog(
                currentLang = currentLang,
                onDismiss = { showLegalDialog = false },
                onAgree = { showLegalDialog = false }
            )
        }
    }
}
