package com.smartedu.tv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smartedu.tv.ui.screens.*

/**
 * SmartEdu TV 导航图
 *
 * 路由：
 * - "login" → 登录页（可选，课程浏览不需要登录）
 * - "home" → 首页（课程浏览，真实API数据）
 * - "course/{courseId}" → 课程详情（章节目录）
 * - "player/{courseId}/{chapterIndex}" → 视频播放
 *
 * 默认直接进入首页，登录作为可选项
 */
@Composable
fun SmartEduNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"  // 直接进入首页，课程浏览不需要登录
    ) {
        // 登录页（可选）
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSkipLogin = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 首页 — 课程浏览
        composable("home") {
            HomeScreen(
                onCourseClick = { courseId ->
                    navController.navigate("course/$courseId")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToProfile = {
                    navController.navigate("login")
                }
            )
        }

        // 设置页
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 课程详情 — 章节目录
        composable(
            route = "course/{courseId}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            CourseDetailScreen(
                courseId = courseId,
                onBack = { navController.popBackStack() },
                onPlayChapter = { cid, chapterIndex ->
                    navController.navigate("player/$cid/$chapterIndex")
                }
            )
        }

        // 视频播放
        composable(
            route = "player/{courseId}/{chapterIndex}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("chapterIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val chapterIndex = backStackEntry.arguments?.getInt("chapterIndex") ?: 0
            PlayerScreen(
                courseId = courseId,
                chapterIndex = chapterIndex,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
