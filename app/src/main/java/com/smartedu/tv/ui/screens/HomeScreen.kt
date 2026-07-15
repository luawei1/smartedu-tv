package com.smartedu.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import com.smartedu.tv.data.model.Course
import com.smartedu.tv.data.repository.CourseRepository
import com.smartedu.tv.ui.theme.*
import kotlinx.coroutines.launch

/**
 * 首页 — 课程浏览（真实API版）
 *
 * 数据来源：国家中小学智慧教育平台公开API
 * - 课程列表：s-file-1/2.ykt.cbern.com.cn（CDN静态JSON）
 * - 课程封面：r1/r2/r3-ndr.ykt.cbern.com.cn（图片CDN）
 */
@Composable
fun HomeScreen(
    onCourseClick: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    val repo = remember { CourseRepository() }
    val scope = rememberCoroutineScope()

    // 学段Tab
    val sections = listOf("小学", "初中", "高中")
    var selectedSection by remember { mutableIntStateOf(0) }

    // 课程数据
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 加载课程数据
    LaunchedEffect(selectedSection) {
        isLoading = true
        errorMessage = null
        scope.launch {
            val section = sections[selectedSection]
            repo.getCourses(section, limit = 30)
                .onSuccess { courses = it }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 顶部栏
            item {
                TopBar(onLoginClick = onLoginClick)
            }

            // 学段选择Tab
            item {
                SectionTabs(
                    sections = sections,
                    selected = selectedSection,
                    onSelect = { selectedSection = it }
                )
            }

            // 加载中
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            }

            // 错误提示
            if (errorMessage != null) {
                item {
                    ErrorCard(
                        message = errorMessage!!,
                        onRetry = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                repo.getCourses(sections[selectedSection], limit = 30)
                                    .onSuccess { courses = it }
                                    .onFailure { errorMessage = it.message }
                                isLoading = false
                            }
                        }
                    )
                }
            }

            // 课程列表（按学科分组）
            if (!isLoading && courses.isNotEmpty()) {
                val grouped = courses.groupBy { it.subject }
                grouped.forEach { (subject, subjectCourses) ->
                    item {
                        CourseRow(
                            title = subject,
                            courses = subjectCourses,
                            onCourseClick = onCourseClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(onLoginClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "📺 智慧教育TV",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        IconButton(onClick = onLoginClick) {
            Icon(
                Icons.Default.Person,
                contentDescription = "登录",
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun SectionTabs(
    sections: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sections.forEachIndexed { index, section ->
            val isSelected = index == selected
            var isFocused by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        when {
                            isSelected -> Primary
                            isFocused -> SurfaceLight
                            else -> Surface
                        }
                    )
                    .focusable()
                    .onFocusChanged { isFocused = it.isFocused }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = section,
                    color = if (isSelected) Color.White else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun CourseRow(
    title: String,
    courses: List<Course>,
    onCourseClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(courses, key = { it.id }) { course ->
                CourseCard(course = course, onClick = { onCourseClick(course.id) })
            }
        }
    }
}

@Composable
private fun CourseCard(course: Course, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(180.dp)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused },
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) SurfaceLight else Surface
        ),
        border = if (isFocused) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Primary)
        ) else null
    ) {
        Column {
            // 封面图
            AsyncImage(
                model = course.coverUrl,
                contentDescription = course.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = course.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${course.grade} · ${course.publisher}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️ 加载失败",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Warning
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}
