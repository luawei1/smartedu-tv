package com.smartedu.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.smartedu.tv.data.model.Chapter
import com.smartedu.tv.data.model.CourseDetail
import com.smartedu.tv.data.repository.CourseRepository
import com.smartedu.tv.ui.theme.*
import kotlinx.coroutines.launch

/**
 * 课程详情页（真实API版）
 *
 * 显示课程信息、章节目录，选择章节后进入视频播放
 * 数据来源：s-file-1.ykt.cbern.com.cn 教材详情 + 章节树API
 */
@Composable
fun CourseDetailScreen(
    courseId: String,
    onBack: () -> Unit,
    onPlayChapter: (String, Int) -> Unit
) {
    val repo = remember { CourseRepository() }
    val scope = rememberCoroutineScope()

    var detail by remember { mutableStateOf<CourseDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(courseId) {
        isLoading = true
        repo.getCourseDetail(courseId)
            .onSuccess { detail = it }
            .onFailure { errorMessage = it.message }
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Primary
                )
            }
            errorMessage != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚠️ 加载失败", color = Warning, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            repo.getCourseDetail(courseId)
                                .onSuccess { detail = it }
                                .onFailure { errorMessage = it.message }
                            isLoading = false
                        }
                    }) { Text("重试") }
                }
            }
            detail != null -> {
                CourseDetailContent(
                    detail = detail!!,
                    onBack = onBack,
                    onPlayChapter = onPlayChapter
                )
            }
        }
    }
}

@Composable
private fun CourseDetailContent(
    detail: CourseDetail,
    onBack: () -> Unit,
    onPlayChapter: (String, Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // 左侧：封面和信息
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .padding(32.dp)
        ) {
            // 返回按钮
            var backFocused by remember { mutableStateOf(false) }
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .focusable()
                    .onFocusChanged { backFocused = it.isFocused }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = if (backFocused) Primary else TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 封面图
            AsyncImage(
                model = detail.coverUrl,
                contentDescription = detail.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 标题
            Text(
                text = detail.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 元信息
            InfoChip(label = detail.grade)
            Spacer(modifier = Modifier.height(6.dp))
            InfoChip(label = detail.subject)
            Spacer(modifier = Modifier.height(6.dp))
            InfoChip(label = detail.publisher)

            Spacer(modifier = Modifier.height(24.dp))

            // 播放按钮
            val firstPlayable = detail.chapters.indexOfFirst { it.hasVideo }
            if (firstPlayable >= 0) {
                var playFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = { onPlayChapter(courseId = detail.id, chapterIndex = firstPlayable) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusable()
                        .onFocusChanged { playFocused = it.isFocused },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (playFocused) PrimaryLight else Primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("立即学习", fontSize = 16.sp)
                }
            }
        }

        // 右侧：章节目录
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .padding(top = 32.dp, end = 32.dp, bottom = 32.dp)
        ) {
            Text(
                text = "章节目录",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "共 ${detail.chapters.size} 节",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(detail.chapters, key = { _, ch -> ch.id }) { index, chapter ->
                    ChapterRow(
                        index = index + 1,
                        chapter = chapter,
                        onClick = {
                            if (chapter.hasVideo) {
                                onPlayChapter(detail.id, index)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Surface)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
private fun ChapterRow(index: Int, chapter: Chapter, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .focusable()
            .onFocusChanged { isFocused = it.isFocused },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isFocused -> SurfaceLight
                chapter.hasVideo -> Surface
                else -> Surface.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 序号
            Text(
                text = String.format("%02d", index),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (chapter.hasVideo) Primary else TextSecondary,
                modifier = Modifier.width(32.dp)
            )

            // 标题
            Text(
                text = chapter.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (chapter.hasVideo) TextPrimary else TextSecondary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 状态
            if (chapter.hasVideo) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "可播放",
                    tint = Accent,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "暂无视频",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
