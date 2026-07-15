package com.smartedu.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.IconButton
import com.smartedu.tv.data.local.UserPreferences
import com.smartedu.tv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    val currentSection by userPrefs.sectionFlow.collectAsState(initial = UserPreferences.DEFAULT_SECTION)
    val currentGrade by userPrefs.gradeFlow.collectAsState(initial = UserPreferences.DEFAULT_GRADE)

    val sections = listOf("小学", "初中", "高中", "小学(五四学制)", "初中(五四学制)")
    
    val gradesBySection = mapOf(
        "小学" to listOf("一年级", "二年级", "三年级", "四年级", "五年级", "六年级"),
        "初中" to listOf("七年级", "八年级", "九年级"),
        "高中" to listOf("高一", "高二", "高三"),
        "小学(五四学制)" to listOf("一年级", "二年级", "三年级", "四年级", "五年级"),
        "初中(五四学制)" to listOf("六年级", "七年级", "八年级", "九年级")
    )

    var settingMode by remember { mutableStateOf("section") } // "section" or "grade"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Panel (Categories)
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .background(Surface.copy(alpha = 0.2f))
                    .padding(32.dp)
            ) {
                var backFocused by remember { mutableStateOf(false) }
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.onFocusChanged { backFocused = it.isFocused }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = if (backFocused) Primary else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "系统设置",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(32.dp))

                CategoryItem(
                    title = "学段设置",
                    subtitle = currentSection,
                    isSelected = settingMode == "section",
                    onClick = { settingMode = "section" }
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                CategoryItem(
                    title = "年级设置",
                    subtitle = currentGrade,
                    isSelected = settingMode == "grade",
                    onClick = { settingMode = "grade" }
                )
            }

            // Right Panel (Options)
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .padding(48.dp)
            ) {
                Text(
                    text = if (settingMode == "section") "选择学段" else "选择年级",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                val items = if (settingMode == "section") sections else (gradesBySection[currentSection] ?: emptyList())

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { item ->
                        val isChecked = if (settingMode == "section") item == currentSection else item == currentGrade
                        OptionItem(
                            title = item,
                            isChecked = isChecked,
                            onClick = {
                                scope.launch {
                                    if (settingMode == "section") {
                                        userPrefs.saveSection(item)
                                        // 切换学段后，自动将年级重置为该学段的第一个年级
                                        val firstGrade = gradesBySection[item]?.firstOrNull() ?: ""
                                        userPrefs.saveGrade(firstGrade)
                                        settingMode = "grade"
                                    } else {
                                        userPrefs.saveGrade(item)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .focusable()
            .onFocusChanged { isFocused = it.isFocused },
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isFocused -> Primary
                isSelected -> SurfaceLight
                else -> Color.Transparent
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (isFocused || isSelected) TextPrimary else TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = if (isFocused) TextPrimary.copy(alpha = 0.8f) else TextTertiary
            )
        }
    }
}

@Composable
private fun OptionItem(
    title: String,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .focusable()
            .onFocusChanged { isFocused = it.isFocused },
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) SurfaceLight else Surface
        ),
        border = if (isFocused) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(PrimaryLight)
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (isFocused) TextPrimary else TextSecondary
            )
            
            if (isChecked) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = PrimaryLight,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
