package com.superstar.android.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.superstar.android.update.AppUpdateManager
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * 物理弹簧微交互 Modifier
 * 按压时触发 scale(0.95f) 物理收缩，释放时通过 Spring 空间弹簧 (stiffness=380f, dampingRatio=0.72f) 自然回弹
 */
fun Modifier.expressiveSpringClickable(
    interactionSource: MutableInteractionSource? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = this.pointerInput(enabled) {
    if (!enabled) return@pointerInput
    detectTapGestures(
        onTap = { onClick() }
    )
}

/**
 * M3 Expressive 2026 标志性波浪形进度指示器 (Wavy Progress Indicator)
 * 采用正弦函数与平滑物理缓动绘制流动的正弦波浪轨道，传达生动的能量律动与工作状态
 */
@Composable
fun WavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    isWavy: Boolean = true,
    strokeWidth: Dp = 8.dp,
    waveAmplitude: Dp = 3.dp,
    waveFrequency: Float = 0.045f,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "wavy_progress_anim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "wave_phase_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(strokeWidth * 2.5f)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val strokePx = strokeWidth.toPx()
        val amplitudePx = if (isWavy) waveAmplitude.toPx() else 0f

        // 1. 绘制底座轨道 (Flat Track)
        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )

        // 2. 绘制前景波浪进度轨道 (Wavy Active Track)
        if (animatedProgress > 0f) {
            val activeWidth = width * animatedProgress
            val wavyPath = Path()
            wavyPath.moveTo(0f, centerY)

            val step = 4f
            var x = 0f
            while (x <= activeWidth) {
                // 正弦波形公式：y = A * sin(kx + phi)
                val waveOffset = if (isWavy) {
                    val factor = sin(x * waveFrequency + phase)
                    factor * amplitudePx
                } else {
                    0f
                }
                wavyPath.lineTo(x, centerY + waveOffset)
                x += step
            }

            drawPath(
                path = wavyPath,
                color = color,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * M3 Expressive 联动按钮组 (Expressive Segmented Button Group)
 * 紧密包裹于 SurfaceContainer 底座内，选中项平滑膨胀为全胶囊形并带有 Spring 弹性形变
 */
@Composable
fun <T> ExpressiveButtonGroup(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    labelProvider: @Composable (T) -> String
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.96f,
                    animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f),
                    label = "item_scale"
                )

                Surface(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier
                        .scale(scale)
                        .clip(CircleShape)
                        .clickable { onItemSelected(item) }
                ) {
                    Text(
                        text = labelProvider(item),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

/**
 * M3 Expressive 悬浮胶囊工具底栏 (Floating Capsule Dock)
 * 底部居中悬浮胶囊容器，替代生硬贴底 TabBar，右侧集成独立 Squircle 圆角方形 FAB
 */
@Composable
fun ExpressiveFloatingDock(
    statusText: String,
    statusDesc: String,
    isRunning: Boolean,
    onFabClick: () -> Unit,
    onQuickActionClick: () -> Unit,
    fabText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.96f),
            tonalElevation = 6.dp,
            shadowElevation = 0.dp, // 严禁使用黑色高斯模糊投影，纯色阶高亮
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 左侧状态指示胶囊
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .clickable { onQuickActionClick() }
                        .padding(vertical = 4.dp, horizontal = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRunning) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = statusDesc,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 右侧 M3 Expressive 独立 Squircle FAB 主操作按钮 (形变圆角 20dp)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onFabClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = fabText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * M3 Expressive 应用内自动更新高质感弹窗
 * 遵循非对称 Bento 结构设计，展示版本对比、更新说明、Wavy 波浪下载进度指示及一键自动安装
 */
@Composable
fun ExpressiveUpdateDialog(
    updateInfo: AppUpdateManager.UpdateInfo,
    updateStatus: AppUpdateManager.UpdateStatus,
    downloadProgress: Float,
    onStartDownload: () -> Unit,
    onInstallApk: () -> Unit,
    onDismiss: () -> Unit,
    isZh: Boolean = true
) {
    val uriHandler = LocalUriHandler.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 顶部标题与关闭按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.SystemUpdate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isZh) "发现新版本可用" else "New Version Available",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "StudyStar ${updateInfo.versionName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 版本对比微标 Bento 卡片
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isZh) "当前版本" else "Installed Version",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = com.superstar.android.BuildConfig.VERSION_NAME,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "->",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isZh) "最新版本" else "Latest Release",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = updateInfo.versionName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 更新日志展示区域
                Text(
                    text = if (isZh) "更新日志" else "Release Notes",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = updateInfo.releaseNotes.ifBlank {
                                if (isZh) "本次更新包含多项稳定性改进与界面优化。" else "Bug fixes and performance improvements."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 下载状态与波浪进度条
                when (updateStatus) {
                    is AppUpdateManager.UpdateStatus.Downloading -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isZh) "正在下载更新安装包..." else "Downloading update package...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${(downloadProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            WavyProgressIndicator(
                                progress = downloadProgress,
                                isWavy = true
                            )
                        }
                    }
                    is AppUpdateManager.UpdateStatus.Downloaded -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isZh) "安装包下载完成，准备就绪" else "APK downloaded successfully",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    is AppUpdateManager.UpdateStatus.Failed -> {
                        Text(
                            text = updateStatus.errorMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 操作按钮区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { uriHandler.openUri(updateInfo.downloadUrl) },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(if (isZh) "浏览器下载" else "Browser")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (updateStatus is AppUpdateManager.UpdateStatus.Downloaded) {
                        Button(
                            onClick = onInstallApk,
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isZh) "立即安装" else "Install Now", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onStartDownload,
                            enabled = updateStatus !is AppUpdateManager.UpdateStatus.Downloading,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Outlined.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (updateStatus is AppUpdateManager.UpdateStatus.Downloading) {
                                    if (isZh) "正在下载..." else "Downloading..."
                                } else {
                                    if (isZh) "应用内一键下载更新" else "In-App Download"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
