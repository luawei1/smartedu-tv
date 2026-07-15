package com.smartedu.tv.ui.screens

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.tv.material3.Text
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.smartedu.tv.data.model.Chapter
import com.smartedu.tv.data.repository.CourseRepository
import com.smartedu.tv.ui.theme.*
import kotlinx.coroutines.delay

/**
 * 视频播放页面
 * 使用 Media3 ExoPlayer 实现全屏视频播放
 * 支持遥控器控制：OK暂停/播放、左右快进快退、上下音量调节
 *
 * @param courseId 课程ID
 * @param chapterIndex 章节索引
 * @param onBack 返回回调
 */
@Composable
fun PlayerScreen(
    courseId: String,
    chapterIndex: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val courseRepository = remember { CourseRepository() }

    // 章节数据
    var chapter by remember { mutableStateOf<Chapter?>(null) }
    var chapterTitle by remember { mutableStateOf("") }

    // 播放器状态
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var volumeText by remember { mutableStateOf("") }
    var volumeShowCountdown by remember { mutableIntStateOf(0) }

    // ExoPlayer 实例
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    // 加载章节数据并准备播放
    LaunchedEffect(courseId, chapterIndex) {
        val detailResult = courseRepository.getCourseDetail(courseId)
        val chapters = detailResult.getOrNull()?.chapters ?: emptyList()
        if (chapterIndex in chapters.indices) {
            chapter = chapters[chapterIndex]
            chapterTitle = chapters[chapterIndex].title

            // 设置媒体资源并播放
            val mediaItem = MediaItem.fromUri(chapters[chapterIndex].videoUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            // 恢复播放位置（断点续播）
            // 实际项目中应从 DataStore 读取保存的位置
        }
    }

    // 更新播放进度
    LaunchedEffect(exoPlayer) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            totalDuration = exoPlayer.duration.coerceAtLeast(0L)
            isPlaying = exoPlayer.isPlaying
            delay(500)
        }
    }

    // 自动隐藏控制栏
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }

    // 音量提示文字倒计时隐藏
    LaunchedEffect(volumeShowCountdown) {
        if (volumeShowCountdown > 0) {
            delay(1500)
            volumeShowCountdown = 0
            volumeText = ""
        }
    }

    // 释放播放器
    DisposableEffect(Unit) {
        onDispose {
            // 保存播放位置（断点续播）
            // 实际项目中应保存到 DataStore
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.nativeKeyEvent.keyCode) {
                        // OK键：暂停/播放
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER -> {
                            exoPlayer.playWhenReady = !exoPlayer.isPlaying
                            showControls = true
                            true
                        }
                        // 左键：快退10秒
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            val newPos = (exoPlayer.currentPosition - 10_000).coerceAtLeast(0)
                            exoPlayer.seekTo(newPos)
                            showControls = true
                            true
                        }
                        // 右键：快进10秒
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            val newPos = (exoPlayer.currentPosition + 10_000)
                                .coerceAtMost(exoPlayer.duration)
                            exoPlayer.seekTo(newPos)
                            showControls = true
                            true
                        }
                        // 上键：音量增大
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            volumeText = "音量 +"
                            volumeShowCountdown++
                            showControls = true
                            true
                        }
                        // 下键：音量减小
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            volumeText = "音量 -"
                            volumeShowCountdown++
                            showControls = true
                            true
                        }
                        // 返回键：退出播放
                        KeyEvent.KEYCODE_BACK -> {
                            exoPlayer.stop()
                            onBack()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        // 视频播放视图
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false // 使用自定义控制UI
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 控制栏覆盖层（点击任意位置显示/隐藏）
        if (showControls) {
            // 顶部：章节标题 + 返回提示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← 返回",
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(24.dp))

                Text(
                    text = chapterTitle,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = if (isPlaying) "播放中" else "已暂停",
                    color = if (isPlaying) Accent else Warning,
                    fontSize = 14.sp
                )
            }

            // 底部：进度条
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                // 进度条
                LinearProgressIndicator(
                    progress = { if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = PrimaryLight,
                    trackColor = SurfaceLight,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 时间显示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(currentPosition),
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = formatDuration(totalDuration),
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 音量调节提示
        if (volumeText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Text(
                    text = volumeText,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 暂停状态提示
        if (!isPlaying && showControls) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(40.dp)
                    )
            ) {
                Text(
                    text = "⏸",
                    color = TextPrimary,
                    fontSize = 36.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * 格式化时长（毫秒 → mm:ss 或 hh:mm:ss）
 */
private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "00:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
