package com.superstar.android.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superstar.android.ui.theme.StatusInfo
import com.superstar.android.ui.theme.StatusSuccess
import com.superstar.android.ui.theme.StatusWarning
import com.superstar.android.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    viewModel: MainViewModel
) {
    val logs by viewModel.deduplicatedLogs.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // 用户是否允许自动滚动的受控开关
    var autoScrollEnabled by remember { mutableStateOf(true) }

    // 检查是否已经在最底部
    val isScrolledToBottom by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf true
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= totalItems - 1
        }
    }

    // 当用户手动向上滑动时，自动暂停自动滚动，彻底避免跳动！
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && !isScrolledToBottom) {
            autoScrollEnabled = false
        }
    }

    // 仅在允许自动滚动且用户未在主动触控滑动时，跟随最新日志（使用 scrollToItem 避免高刷屏下连续触发插值动画导致的掉帧与卡顿）
    LaunchedEffect(logs.size, autoScrollEnabled) {
        if (logs.isNotEmpty() && autoScrollEnabled && !listState.isScrollInProgress) {
            listState.scrollToItem(logs.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "运行日志",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${logs.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    // 自动滚动状态切换 Chip
                    FilterChip(
                        selected = autoScrollEnabled,
                        onClick = {
                            val target = !autoScrollEnabled
                            autoScrollEnabled = target
                            if (target && logs.isNotEmpty()) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(logs.size - 1)
                                }
                            }
                        },
                        label = {
                            Text(
                                text = if (autoScrollEnabled) "自动滚动" else "已暂停",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = if (autoScrollEnabled) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    // 复制全部日志
                    IconButton(
                        onClick = {
                            if (logs.isNotEmpty()) {
                                val fullText = logs.joinToString("\n") { "[${it.timestamp}] [${it.type}] ${it.message}" }
                                clipboardManager.setText(AnnotatedString(fullText))
                                Toast.makeText(context, "已复制全部日志到剪贴板", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "当前无日志可复制", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "复制日志",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 清空日志
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = "清空日志",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (logs.isEmpty()) {
                // 空数据态
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "暂无运行日志",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "启动刷课或执行操作后，日志将实时在此展示",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                // 日志列表：使用稳定 key，避免重新测量跳动
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        MaterialLogItem(log = log)
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }

            // 当暂停自动滚动且不在底部时，浮动展示“回到底部”悬浮按钮
            AnimatedVisibility(
                visible = !isScrolledToBottom || !autoScrollEnabled,
                enter = slideInVertically { it / 2 } + fadeIn(),
                exit = slideOutVertically { it / 2 } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Surface(
                    onClick = {
                        autoScrollEnabled = true
                        if (logs.isNotEmpty()) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(logs.size - 1)
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 4.dp,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "滚动到最新日志",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * 现代 Material Design 3 单条日志卡片
 */
@Composable
private fun MaterialLogItem(log: MainViewModel.LogEntry) {
    val (typeColor, tagText) = when (log.type) {
        MainViewModel.LogType.INFO -> StatusInfo to "INFO"
        MainViewModel.LogType.SUCCESS -> StatusSuccess to "SUCCESS"
        MainViewModel.LogType.WARNING -> StatusWarning to "WARN"
        MainViewModel.LogType.ERROR -> MaterialTheme.colorScheme.error to "ERROR"
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 类型彩色小标签
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = typeColor.copy(alpha = 0.12f),
                modifier = Modifier.padding(top = 1.dp)
            ) {
                Text(
                    text = tagText,
                    color = typeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 时间戳
            if (log.timestamp.isNotBlank()) {
                Text(
                    text = log.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 1.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // 日志内容文本
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
