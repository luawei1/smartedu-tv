package com.smartedu.tv.ui.screens

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.Text
import com.smartedu.tv.data.repository.CourseRepository
import com.smartedu.tv.ui.theme.*

/**
 * 视频播放页面
 *
 * 使用 WebView 嵌入国家智慧教育平台官方视频播放器
 *
 * 平台视频基于私有 CDN，流媒体需要 MAC 鉴权，无法直接通过 ExoPlayer 访问。
 * 解决方案：通过 WebView 加载官方播放页面，由浏览器 JS SDK 自动处理鉴权。
 *
 * URL 格式：
 *   https://basic.smartedu.cn/syncClassroom?resourceId={resourceId}
 *
 * @param courseId 课程ID
 * @param chapterIndex 章节索引
 * @param onBack 返回回调
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(
    courseId: String,
    chapterIndex: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val courseRepository = remember { CourseRepository() }

    var chapterTitle by remember { mutableStateOf("") }
    var resourceId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf("") }

    // 加载章节数据并准备播放
    LaunchedEffect(courseId, chapterIndex) {
        isLoading = true
        loadError = ""
        try {
            val detailResult = courseRepository.getCourseDetail(courseId)
            val chapters = detailResult.getOrNull()?.chapters ?: emptyList()
            if (chapterIndex in chapters.indices) {
                val chapter = chapters[chapterIndex]
                chapterTitle = chapter.title

                if (chapter.resourceId.isNotBlank()) {
                    resourceId = chapter.resourceId
                } else {
                    loadError = "该章节暂无视频资源"
                }
            } else {
                loadError = "章节数据加载失败"
            }
        } catch (e: Exception) {
            loadError = "加载失败：${e.message}"
        }
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown &&
                    event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                    onBack()
                    true
                } else {
                    false
                }
            }
    ) {
        when {
            isLoading -> {
                // 加载中
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "加载中…",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                }
            }

            loadError.isNotBlank() -> {
                // 错误提示
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = loadError,
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "按返回键退出",
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }

            resourceId.isNotBlank() -> {
                // WebView 播放器
                // 官方播放页：https://basic.smartedu.cn/syncClassroom?resourceId={id}
                val playerUrl = "https://basic.smartedu.cn/syncClassroom?resourceId=$resourceId"

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                allowFileAccess = false
                                cacheMode = WebSettings.LOAD_DEFAULT
                                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                        "Chrome/120.0.0.0 Safari/537.36"
                            }

                            webChromeClient = object : WebChromeClient() {}

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    url: String?
                                ): Boolean {
                                    // 允许在 smartedu.cn 和 cbern.com.cn 之间跳转
                                    return false
                                }
                            }

                            // 支持 TV 遥控器方向键
                            isFocusable = true
                            isFocusableInTouchMode = true

                            loadUrl(playerUrl)
                        }
                    },
                    update = { webView ->
                        // 仅在 resourceId 改变时重新加载
                        val currentUrl = webView.url ?: ""
                        val expectedUrl = "https://basic.smartedu.cn/syncClassroom?resourceId=$resourceId"
                        if (!currentUrl.contains(resourceId)) {
                            webView.loadUrl(expectedUrl)
                        }
                    }
                )

                // 顶部标题栏（半透明叠加层，3秒后不显示）
                var showTitle by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    showTitle = false
                }
                if (showTitle) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "← 返回",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = chapterTitle,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
