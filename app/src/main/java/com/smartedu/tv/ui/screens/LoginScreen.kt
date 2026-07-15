package com.smartedu.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartedu.tv.ui.theme.*

/**
 * 登录页面
 *
 * 国家智慧教育平台的课程浏览和视频播放不需要登录
 * 登录仅用于：学习进度、收藏、个人中心
 *
 * 当前版本：展示"跳过登录"选项，直接进入课程浏览
 * 后续版本：接入SSO扫码登录
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSkipLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(420.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Text(
                    text = "📺",
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "智慧教育TV",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "国家中小学智慧教育平台",
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 说明
                Text(
                    text = "课程浏览和视频播放无需登录\n登录后可同步学习进度和收藏",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 登录按钮（暂时禁用，后续接入SSO）
                var loginFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = { /* TODO: 接入SSO扫码登录 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusable()
                        .onFocusChanged { loginFocused = it.isFocused },
                    enabled = false, // 暂时禁用
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = SurfaceLight
                    )
                ) {
                    Text("扫码登录（即将上线）", fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 跳过登录
                var skipFocused by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = onSkipLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusable()
                        .onFocusChanged { skipFocused = it.isFocused },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (skipFocused) Primary else TextPrimary
                    )
                ) {
                    Text("直接浏览课程 →", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "数据来源：国家中小学智慧教育平台\nbasic.smartedu.cn",
                    fontSize = 11.sp,
                    color = TextSecondary.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
