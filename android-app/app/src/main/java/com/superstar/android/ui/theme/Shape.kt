package com.superstar.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 Expressive (2026 规范) 形状层级规范
 * 严格对标情感化物理设计梯度：
 * Extra Small: 4dp (紧凑输入框、徽标)
 * Small: 10dp (次级按钮、标签)
 * Medium: 16dp (操作卡片、弹窗内嵌项)
 * Large: 24dp (Bento 核心信息卡片)
 * Extra Large: 28dp (Bento Hero 主卡片、Bottom Sheet 顶部)
 * Full / Pill: 9999dp (胶囊按钮、悬浮底栏 Dock)
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Expressive 专有形状 Tokens
val ShapeSquircle = RoundedCornerShape(20.dp)
val ShapeBentoCard = RoundedCornerShape(24.dp)
val ShapeBentoHero = RoundedCornerShape(28.dp)
val ShapePill = RoundedCornerShape(9999.dp)
