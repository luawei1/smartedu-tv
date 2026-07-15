package com.smartedu.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.*

/**
 * SmartEdu TV 深色主题
 * 采用深色配色方案，适合电视大屏观看，减少视觉疲劳
 */
private val SmartEduDarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = TextPrimary,
    secondary = Accent,
    onSecondary = TextPrimary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    error = Warning,
    onError = TextPrimary
)

/**
 * SmartEdu 主题包装组件
 * 所有页面应包裹在此主题内
 */
@Composable
fun SmartEduTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SmartEduDarkColorScheme,
        content = content
    )
}
