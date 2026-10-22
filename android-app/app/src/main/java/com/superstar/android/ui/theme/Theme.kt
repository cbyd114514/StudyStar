package com.superstar.android.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// =========================================================================
// Google Material Design 3 (Material You) - Oceanic Blue Tonal Palette
// 严格对标 Google M3 规范：全角色语义色彩、表面容器分层 (Surface Containers)、动态色彩支持
// =========================================================================

// --- 浅色主题色彩 (Light Color Scheme Tokens) ---
val MdLightPrimary = Color(0xFF005AC1)                 // 关键操作与焦点主色
val MdLightOnPrimary = Color(0xFFFFFFFF)
val MdLightPrimaryContainer = Color(0xFFD8E2FF)        // 主容器填色
val MdLightOnPrimaryContainer = Color(0xFF001A41)
val MdLightInversePrimary = Color(0xFFADC6FF)

val MdLightSecondary = Color(0xFF535F70)               // 次级组件与次要强调
val MdLightOnSecondary = Color(0xFFFFFFFF)
val MdLightSecondaryContainer = Color(0xFFD7E3F7)
val MdLightOnSecondaryContainer = Color(0xFF101C2B)

val MdLightTertiary = Color(0xFF286470)                // 第三色：平衡视效与测验指示
val MdLightOnTertiary = Color(0xFFFFFFFF)
val MdLightTertiaryContainer = Color(0xFFADEBF7)
val MdLightOnTertiaryContainer = Color(0xFF001F25)

val MdLightError = Color(0xFFBA1A1A)                   // 错误与危险状态
val MdLightOnError = Color(0xFFFFFFFF)
val MdLightErrorContainer = Color(0xFFFFDAD6)
val MdLightOnErrorContainer = Color(0xFF410002)

val MdLightBackground = Color(0xFFF9F9FE)              // 背景基底色
val MdLightOnBackground = Color(0xFF191C20)

// M3 表面与容器分层系统 (Surface & Surface Containers)
val MdLightSurface = Color(0xFFF9F9FE)
val MdLightOnSurface = Color(0xFF191C20)
val MdLightSurfaceVariant = Color(0xFFDFE2EB)          // 变体填色（如未选中状态、分割线）
val MdLightOnSurfaceVariant = Color(0xFF43474E)
val MdLightSurfaceDim = Color(0xFFD8DAE0)
val MdLightSurfaceBright = Color(0xFFF9F9FE)
val MdLightSurfaceContainerLowest = Color(0xFFFFFFFF)
val MdLightSurfaceContainerLow = Color(0xFFF3F3F8)
val MdLightSurfaceContainer = Color(0xFFECEEF3)        // 默认卡片容器
val MdLightSurfaceContainerHigh = Color(0xFFE6E8EE)     // 抬升卡片
val MdLightSurfaceContainerHighest = Color(0xFFE0E2E8)

val MdLightInverseSurface = Color(0xFF2E3036)
val MdLightInverseOnSurface = Color(0xFFF0F0F7)

val MdLightOutline = Color(0xFF73777F)                 // 轮廓线
val MdLightOutlineVariant = Color(0xFFC3C7D0)          // 次级轮廓线
val MdLightScrim = Color(0xFF000000)

// --- 深色主题色彩 (Dark Color Scheme Tokens) ---
val MdDarkPrimary = Color(0xFFADC6FF)
val MdDarkOnPrimary = Color(0xFF002E69)
val MdDarkPrimaryContainer = Color(0xFF004494)
val MdDarkOnPrimaryContainer = Color(0xFFD8E2FF)
val MdDarkInversePrimary = Color(0xFF005AC1)

val MdDarkSecondary = Color(0xFFBBC7DB)
val MdDarkOnSecondary = Color(0xFF253140)
val MdDarkSecondaryContainer = Color(0xFF3B4858)
val MdDarkOnSecondaryContainer = Color(0xFFD7E3F7)

val MdDarkTertiary = Color(0xFF92CFDC)
val MdDarkOnTertiary = Color(0xFF00363F)
val MdDarkTertiaryContainer = Color(0xFF064D57)
val MdDarkOnTertiaryContainer = Color(0xFFADEBF7)

val MdDarkError = Color(0xFFFFB4AB)
val MdDarkOnError = Color(0xFF690005)
val MdDarkErrorContainer = Color(0xFF93000A)
val MdDarkOnErrorContainer = Color(0xFFFFDAD6)

val MdDarkBackground = Color(0xFF111318)
val MdDarkOnBackground = Color(0xFFE2E2E9)

// M3 深色表面分层
val MdDarkSurface = Color(0xFF111318)
val MdDarkOnSurface = Color(0xFFE2E2E9)
val MdDarkSurfaceVariant = Color(0xFF43474E)
val MdDarkOnSurfaceVariant = Color(0xFFC3C7D0)
val MdDarkSurfaceDim = Color(0xFF111318)
val MdDarkSurfaceBright = Color(0xFF37393E)
val MdDarkSurfaceContainerLowest = Color(0xFF0C0E13)
val MdDarkSurfaceContainerLow = Color(0xFF191C20)
val MdDarkSurfaceContainer = Color(0xFF1D2024)
val MdDarkSurfaceContainerHigh = Color(0xFF272A2F)
val MdDarkSurfaceContainerHighest = Color(0xFF32353A)

val MdDarkInverseSurface = Color(0xFFE2E2E9)
val MdDarkInverseOnSurface = Color(0xFF2E3036)

val MdDarkOutline = Color(0xFF8D9199)
val MdDarkOutlineVariant = Color(0xFF43474E)
val MdDarkScrim = Color(0xFF000000)

// 语义状态指示色
val StatusSuccess = Color(0xFF2E7D32)
val StatusWarning = Color(0xFFED6C02)
val StatusInfo = Color(0xFF0288D1)

val LightColorScheme = lightColorScheme(
    primary = MdLightPrimary,
    onPrimary = MdLightOnPrimary,
    primaryContainer = MdLightPrimaryContainer,
    onPrimaryContainer = MdLightOnPrimaryContainer,
    inversePrimary = MdLightInversePrimary,
    secondary = MdLightSecondary,
    onSecondary = MdLightOnSecondary,
    secondaryContainer = MdLightSecondaryContainer,
    onSecondaryContainer = MdLightOnSecondaryContainer,
    tertiary = MdLightTertiary,
    onTertiary = MdLightOnTertiary,
    tertiaryContainer = MdLightTertiaryContainer,
    onTertiaryContainer = MdLightOnTertiaryContainer,
    error = MdLightError,
    onError = MdLightOnError,
    errorContainer = MdLightErrorContainer,
    onErrorContainer = MdLightOnErrorContainer,
    background = MdLightBackground,
    onBackground = MdLightOnBackground,
    surface = MdLightSurface,
    onSurface = MdLightOnSurface,
    surfaceVariant = MdLightSurfaceVariant,
    onSurfaceVariant = MdLightOnSurfaceVariant,
    surfaceDim = MdLightSurfaceDim,
    surfaceBright = MdLightSurfaceBright,
    surfaceContainerLowest = MdLightSurfaceContainerLowest,
    surfaceContainerLow = MdLightSurfaceContainerLow,
    surfaceContainer = MdLightSurfaceContainer,
    surfaceContainerHigh = MdLightSurfaceContainerHigh,
    surfaceContainerHighest = MdLightSurfaceContainerHighest,
    inverseSurface = MdLightInverseSurface,
    inverseOnSurface = MdLightInverseOnSurface,
    outline = MdLightOutline,
    outlineVariant = MdLightOutlineVariant,
    scrim = MdLightScrim
)

val DarkColorScheme = darkColorScheme(
    primary = MdDarkPrimary,
    onPrimary = MdDarkOnPrimary,
    primaryContainer = MdDarkPrimaryContainer,
    onPrimaryContainer = MdDarkOnPrimaryContainer,
    inversePrimary = MdDarkInversePrimary,
    secondary = MdDarkSecondary,
    onSecondary = MdDarkOnSecondary,
    secondaryContainer = MdDarkSecondaryContainer,
    onSecondaryContainer = MdDarkOnSecondaryContainer,
    tertiary = MdDarkTertiary,
    onTertiary = MdDarkOnTertiary,
    tertiaryContainer = MdDarkTertiaryContainer,
    onTertiaryContainer = MdDarkOnTertiaryContainer,
    error = MdDarkError,
    onError = MdDarkOnError,
    errorContainer = MdDarkErrorContainer,
    onErrorContainer = MdDarkOnErrorContainer,
    background = MdDarkBackground,
    onBackground = MdDarkOnBackground,
    surface = MdDarkSurface,
    onSurface = MdDarkOnSurface,
    surfaceVariant = MdDarkSurfaceVariant,
    onSurfaceVariant = MdDarkOnSurfaceVariant,
    surfaceDim = MdDarkSurfaceDim,
    surfaceBright = MdDarkSurfaceBright,
    surfaceContainerLowest = MdDarkSurfaceContainerLowest,
    surfaceContainerLow = MdDarkSurfaceContainerLow,
    surfaceContainer = MdDarkSurfaceContainer,
    surfaceContainerHigh = MdDarkSurfaceContainerHigh,
    surfaceContainerHighest = MdDarkSurfaceContainerHighest,
    inverseSurface = MdDarkInverseSurface,
    inverseOnSurface = MdDarkInverseOnSurface,
    outline = MdDarkOutline,
    outlineVariant = MdDarkOutlineVariant,
    scrim = MdDarkScrim
)

/**
 * 递归安全解构 Context 获取 Activity
 */
fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/**
 * Material Design 3 全局主题入口
 * @param darkTheme 是否深色模式
 * @param dynamicColor 在 Android 12+ (API 31+) 设备上是否启用壁纸取色动态色彩（默认支持品牌专属 Ocean Blue，可选择动态色彩）
 */
@Composable
fun SuperStarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
