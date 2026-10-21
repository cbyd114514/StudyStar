package com.superstar.android.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ==========================================
// Material Design 3 - Ocean Blue Theme
// 契合海蓝色鲸鱼少女新图标的现代 Material 3 色彩体系
// ==========================================

// 浅色主题色彩 (Light Color Scheme)
val MdLightPrimary = Color(0xFF1E5BBB)               // 主色：深邃群青蓝
val MdLightOnPrimary = Color(0xFFFFFFFF)
val MdLightPrimaryContainer = Color(0xFFD8E2FF)      // 主容器色：柔和冰蓝
val MdLightOnPrimaryContainer = Color(0xFF001A41)

val MdLightSecondary = Color(0xFF4B607C)             // 次色：低饱和石板蓝
val MdLightOnSecondary = Color(0xFFFFFFFF)
val MdLightSecondaryContainer = Color(0xFFD3E4FF)
val MdLightOnSecondaryContainer = Color(0xFF041D35)

val MdLightTertiary = Color(0xFF3B6870)              // 第三色：清澈海浪青
val MdLightOnTertiary = Color(0xFFFFFFFF)
val MdLightTertiaryContainer = Color(0xFFBEEAF3)
val MdLightOnTertiaryContainer = Color(0xFF001F25)

val MdLightError = Color(0xFFBA1A1A)
val MdLightOnError = Color(0xFFFFFFFF)
val MdLightErrorContainer = Color(0xFFFFDAD6)
val MdLightOnErrorContainer = Color(0xFF410002)

val MdLightBackground = Color(0xFFF9F9FE)           // 背景色：明亮纯净
val MdLightOnBackground = Color(0xFF191C20)
val MdLightSurface = Color(0xFFFFFFFF)              // 表面色：纯白
val MdLightOnSurface = Color(0xFF191C20)
val MdLightSurfaceVariant = Color(0xFFDFE2EB)       // 表面变体
val MdLightOnSurfaceVariant = Color(0xFF43474E)
val MdLightOutline = Color(0xFF73777F)
val MdLightOutlineVariant = Color(0xFFC3C7D0)

// 深色主题色彩 (Dark Color Scheme)
val MdDarkPrimary = Color(0xFFADC6FF)                // 主色：通透亮蓝
val MdDarkOnPrimary = Color(0xFF002E69)
val MdDarkPrimaryContainer = Color(0xFF004494)
val MdDarkOnPrimaryContainer = Color(0xFFD8E2FF)

val MdDarkSecondary = Color(0xFFB3C8E8)
val MdDarkOnSecondary = Color(0xFF1B324B)
val MdDarkSecondaryContainer = Color(0xFF334863)
val MdDarkOnSecondaryContainer = Color(0xFFD3E4FF)

val MdDarkTertiary = Color(0xFFA2CED6)
val MdDarkOnTertiary = Color(0xFF03363E)
val MdDarkTertiaryContainer = Color(0xFF214E56)
val MdDarkOnTertiaryContainer = Color(0xFFBEEAF3)

val MdDarkError = Color(0xFFFFB4AB)
val MdDarkOnError = Color(0xFF690005)
val MdDarkErrorContainer = Color(0xFF93000A)
val MdDarkOnErrorContainer = Color(0xFFFFDAD6)

val MdDarkBackground = Color(0xFF111318)            // 背景色：沉稳深黑蓝
val MdDarkOnBackground = Color(0xFFE2E2E9)
val MdDarkSurface = Color(0xFF191C20)               // 表面色：深灰黑
val MdDarkOnSurface = Color(0xFFE2E2E9)
val MdDarkSurfaceVariant = Color(0xFF43474E)
val MdDarkOnSurfaceVariant = Color(0xFFC3C7D0)
val MdDarkOutline = Color(0xFF8D9199)
val MdDarkOutlineVariant = Color(0xFF43474E)

// 状态色
val StatusSuccess = Color(0xFF2E7D32)
val StatusWarning = Color(0xFFED6C02)
val StatusInfo = Color(0xFF0288D1)

private val LightColorScheme = lightColorScheme(
    primary = MdLightPrimary,
    onPrimary = MdLightOnPrimary,
    primaryContainer = MdLightPrimaryContainer,
    onPrimaryContainer = MdLightOnPrimaryContainer,
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
    outline = MdLightOutline,
    outlineVariant = MdLightOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = MdDarkPrimary,
    onPrimary = MdDarkOnPrimary,
    primaryContainer = MdDarkPrimaryContainer,
    onPrimaryContainer = MdDarkOnPrimaryContainer,
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
    outline = MdDarkOutline,
    outlineVariant = MdDarkOutlineVariant
)

/**
 * 递归安全解构 Context 获取 Activity，避免 ContextThemeWrapper 强转 ClassCastException 导致闪退
 */
fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@Composable
fun SuperStarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

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
        content = content
    )
}
