package com.smartedu.tv.data.model

/**
 * 用户模型
 * 登录功能预留（课程浏览和播放不需要登录）
 */
data class User(
    val userId: String,
    val nickname: String,
    val token: String
)

data class DeviceCodeResponse(
    val deviceCode: String,
    val userCode: String,
    val verificationUrl: String,
    val expiresIn: Int,
    val interval: Int
)

data class TokenResponse(
    val status: String,
    val token: String? = null,
    val userId: String? = null,
    val nickname: String? = null
)
