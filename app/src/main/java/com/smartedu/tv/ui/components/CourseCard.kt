package com.smartedu.tv.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.smartedu.tv.data.model.Course
import com.smartedu.tv.ui.theme.*

/**
 * TV 风格课程卡片组件
 * 支持焦点导航，选中时有边框高亮和缩放动画效果
 *
 * @param course 课程数据
 * @param onClick 点击/确认键回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    // 焦点状态的缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1f,
        label = "card_scale"
    )

    // 焦点状态的边框
    val borderStroke = if (isFocused) {
        BorderStroke(3.dp, PrimaryLight)
    } else {
        BorderStroke(1.dp, SurfaceLight)
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .width(240.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(),
        shape = CardDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.colors(
            containerColor = Surface,
            focusedContainerColor = SurfaceLight
        ),
        border = CardDefaults.border(
            focusedBorder = Border(border = BorderStroke(3.dp, PrimaryLight)),
            border = Border(border = BorderStroke(1.dp, SurfaceLight))
        )
    ) {
        Column {
            // 课程封面图
            AsyncImage(
                model = course.coverUrl,
                contentDescription = course.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )

            // 课程信息区域
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // 课程标题
                Text(
                    text = course.title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 年级学科标签
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Primary.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = course.subject,
                            color = PrimaryLight,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Accent.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = course.grade,
                            color = Accent,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
