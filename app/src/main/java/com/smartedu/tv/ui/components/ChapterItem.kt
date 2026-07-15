package com.smartedu.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.smartedu.tv.data.model.Chapter
import com.smartedu.tv.ui.theme.*

/**
 * 章节列表项组件
 * 左侧显示序号和标题，右侧显示时长
 * 支持焦点高亮
 *
 * @param chapter 章节数据
 * @param index 章节序号（从0开始）
 * @param onClick 点击/确认键回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChapterItem(
    chapter: Chapter,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Surface,
            focusedContainerColor = SurfaceLight
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryLight)),
            border = Border(border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceLight))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 序号圆圈
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isFocused) Primary else SurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 播放图标
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "播放",
                tint = if (isFocused) PrimaryLight else TextSecondary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 章节标题
            Text(
                text = chapter.title,
                color = if (isFocused) TextPrimary else TextSecondary,
                fontSize = 15.sp,
                fontWeight = if (isFocused) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 时长标签
            Text(
                text = chapter.duration,
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}
