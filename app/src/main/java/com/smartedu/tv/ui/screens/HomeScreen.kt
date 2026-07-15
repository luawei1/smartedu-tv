package com.smartedu.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.smartedu.tv.data.local.UserPreferences
import com.smartedu.tv.data.model.Course
import com.smartedu.tv.data.repository.CourseRepository
import com.smartedu.tv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onCourseClick: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    
    val section by userPrefs.sectionFlow.collectAsState(initial = UserPreferences.DEFAULT_SECTION)
    val grade by userPrefs.gradeFlow.collectAsState(initial = UserPreferences.DEFAULT_GRADE)
    val currentSubject by userPrefs.subjectFlow.collectAsState(initial = UserPreferences.DEFAULT_SUBJECT)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    NavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(
                currentRoute = "home",
                onNavigateToHome = { },
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) {
        HomeContent(
            section = section,
            grade = grade,
            currentSubject = currentSubject,
            onSubjectSelected = { scope, subj -> scope.launch { userPrefs.saveSubject(subj) } },
            onCourseClick = onCourseClick
        )
    }
}

@Composable
private fun DrawerMenu(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Surface.copy(alpha = 0.9f))
            .padding(vertical = 48.dp, horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "智慧教育TV",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp, start = 16.dp)
        )

        DrawerMenuItem(
            icon = Icons.Default.Home,
            text = "发现课程",
            isSelected = currentRoute == "home",
            onClick = onNavigateToHome
        )
        DrawerMenuItem(
            icon = Icons.Default.Person,
            text = "我的学习",
            isSelected = currentRoute == "profile",
            onClick = onNavigateToProfile
        )
        DrawerMenuItem(
            icon = Icons.Default.Settings,
            text = "系统设置",
            isSelected = currentRoute == "settings",
            onClick = onNavigateToSettings
        )
    }
}

@Composable
private fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    androidx.tv.material3.Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.2f) else Color.Transparent,
            focusedContainerColor = Primary,
            contentColor = if (isSelected) PrimaryLight else TextSecondary,
            focusedContentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun HomeContent(
    section: String,
    grade: String,
    currentSubject: String,
    onSubjectSelected: (kotlinx.coroutines.CoroutineScope, String) -> Unit,
    onCourseClick: (String) -> Unit
) {
    val repo = remember { CourseRepository() }
    val scope = rememberCoroutineScope()

    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Hardcoded subjects for demo purposes
    val subjects = listOf("语文", "数学", "英语", "物理", "化学", "生物学", "道德与法治", "历史", "地理", "科学", "艺术·音乐", "艺术·美术", "体育与健康")
    
    var currentPublisher by remember { mutableStateOf("全部版本") }
    var publishers by remember { mutableStateOf<List<String>>(listOf("全部版本")) }

    LaunchedEffect(section, grade, currentSubject) {
        isLoading = true
        errorMessage = null
        currentPublisher = "全部版本" // 重置版本筛选
        
        scope.launch {
            repo.getCourses(section = section, grade = grade, limit = 100)
                .onSuccess { allCourses ->
                    val subjectCourses = allCourses.filter { it.subject.contains(currentSubject) || it.subject.isBlank() }
                    
                    // 提取此学科下的所有版本
                    val pubSet = subjectCourses.map { it.publisher }.filter { it.isNotBlank() }.toSet().sorted()
                    publishers = listOf("全部版本") + pubSet
                    
                    courses = subjectCourses
                }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(start = 100.dp) // Leave room for closed drawer peeking
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 48.dp, bottom = 48.dp, start = 32.dp, end = 32.dp)
        ) {
            // Header info
            item {
                Text(
                    text = "$section · $grade",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Subject Filter
            item {
                SubjectTabs(
                    subjects = subjects,
                    selected = currentSubject,
                    onSelect = { onSubjectSelected(scope, it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Publisher Filter
            if (publishers.size > 1 && !isLoading) {
                item {
                    PublisherTabs(
                        publishers = publishers,
                        selected = currentPublisher,
                        onSelect = { currentPublisher = it }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            } else if (errorMessage != null) {
                item {
                    Text("⚠️ 加载失败: $errorMessage", color = Warning, modifier = Modifier.padding(32.dp))
                }
            } else {
                val filteredCourses = if (currentPublisher == "全部版本") courses else courses.filter { it.publisher == currentPublisher }
                
                if (filteredCourses.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("暂无此学科/版本的课程数据", color = TextSecondary)
                        }
                    }
                } else {
                    item {
                        CourseGrid(courses = filteredCourses, onCourseClick = onCourseClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectTabs(
    subjects: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(subjects) { subject ->
            val isSelected = subject == selected
            var isFocused by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) Primary else if (isFocused) SurfaceLight else Surface)
                    .focusable()
                    .onFocusChanged { isFocused = it.isFocused }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                androidx.compose.material3.Text(
                    text = subject,
                    color = if (isSelected || isFocused) Color.White else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PublisherTabs(
    publishers: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(publishers) { pub ->
            val isSelected = pub == selected
            var isFocused by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) SurfaceLight else if (isFocused) Surface.copy(alpha = 0.5f) else Color.Transparent)
                    .focusable()
                    .onFocusChanged { isFocused = it.isFocused }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                androidx.compose.material3.Text(
                    text = pub,
                    color = if (isSelected || isFocused) Color.White else TextTertiary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CourseGrid(
    courses: List<Course>,
    onCourseClick: (String) -> Unit
) {
    // FlowRow or just a LazyRow for simplicity, let's use LazyRow or split into multiple rows
    // To make it look like an Apple TV grid, we can just display them in a horizontal list for now
    // or chunk them into a vertical column of rows.
    
    val rows = courses.chunked(4) // 4 items per row
    
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        rows.forEach { rowCourses ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                rowCourses.forEach { course ->
                    Box(modifier = Modifier.weight(1f)) {
                        AppleStyleCourseCard(course = course, onClick = { onCourseClick(course.id) })
                    }
                }
                // Fill remaining space if less than 4 items
                repeat(4 - rowCourses.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AppleStyleCourseCard(course: Course, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    androidx.tv.material3.Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .onFocusChanged { isFocused = it.isFocused },
        shape = CardDefaults.shape(shape = RoundedCornerShape(16.dp)),
        scale = CardDefaults.scale(focusedScale = 1.05f),
        glow = CardDefaults.glow(
            focusedGlow = Glow(elevationColor = PrimaryLight.copy(alpha = 0.5f), elevation = 12.dp)
        ),
        colors = CardDefaults.colors(
            containerColor = Surface
        )
    ) {
        Column {
            AsyncImage(
                model = course.coverUrl,
                contentDescription = course.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isFocused) SurfaceLight else Surface)
                    .padding(16.dp)
            ) {
                androidx.compose.material3.Text(
                    text = course.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.material3.Text(
                    text = "${course.grade} · ${course.publisher}",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
