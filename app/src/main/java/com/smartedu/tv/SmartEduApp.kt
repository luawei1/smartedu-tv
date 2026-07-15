package com.smartedu.tv

import android.app.Application

/**
 * SmartEdu TV 应用入口
 * 负责全局初始化配置
 */
class SmartEduApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化工作将在后续添加（如 DataStore、日志框架等）
    }
}
