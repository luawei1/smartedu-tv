package com.smartedu.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.smartedu.tv.ui.navigation.SmartEduNavHost
import com.smartedu.tv.ui.theme.SmartEduTheme

/**
 * 主入口 Activity
 * 设置全屏沉浸模式和 Compose UI
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启用全屏沉浸模式（TV 应用标准做法）
        enableImmersiveMode()

        // 设置 Compose UI
        setContent {
            SmartEduTheme {
                SmartEduNavHost()
            }
        }
    }

    /**
     * 启用全屏沉浸模式
     * 隐藏状态栏和导航栏，TV 端实现真正的全屏体验
     */
    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
