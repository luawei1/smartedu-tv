package com.smartedu.tv.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smartedu.tv.data.api.RetrofitClient
import com.smartedu.tv.data.model.DeviceCodeResponse
import com.smartedu.tv.data.model.TokenResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** DataStore 扩展，用于存储认证信息 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

/**
 * 认证仓库
 * 处理设备码扫码登录流程和 Token 管理
 */
class AuthRepository(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_NICKNAME = stringPreferencesKey("nickname")
    }

    private val api = RetrofitClient.apiService

    /** 当前设备码（内存缓存） */
    private var currentDeviceCode: String? = null

    /**
     * 发起设备码登录（第一步）
     * 获取设备码和用户验证码，用于展示二维码
     * @return DeviceCodeResponse 包含 deviceCode、userCode 等
     */
    suspend fun deviceCodeLogin(): DeviceCodeResponse {
        // Mock 实现：返回模拟数据
        val response = DeviceCodeResponse(
            deviceCode = "mock_device_${System.currentTimeMillis()}",
            userCode = (100000..999999).random().toString(),
            verificationUrl = "https://basic.smartedu.cn/auth/device",
            expiresIn = 300,
            interval = 5
        )
        currentDeviceCode = response.deviceCode
        return response
    }

    /**
     * 轮询登录状态（第二步）
     * 客户端定时调用，检查用户是否已完成扫码授权
     * @return TokenResponse，status 为 "pending" 或 "authorized"
     */
    suspend fun pollLoginStatus(): TokenResponse {
        val deviceCode = currentDeviceCode ?: return TokenResponse(status = "error")

        // Mock 实现：首次返回 pending，实际项目中调用 api.pollToken(deviceCode)
        // 这里模拟 3 秒后登录成功
        return TokenResponse(
            status = "authorized",
            token = "mock_token_${System.currentTimeMillis()}",
            userId = "user_001",
            nickname = "智慧学生"
        )
    }

    /**
     * 保存 Token 和用户信息到本地存储
     */
    suspend fun saveToken(token: String, userId: String, nickname: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = userId
            prefs[KEY_NICKNAME] = nickname
        }
        // 同步设置到 RetrofitClient
        RetrofitClient.token = token
    }

    /**
     * 获取已保存的 Token
     * @return Token 字符串，未登录时返回 null
     */
    suspend fun getToken(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_TOKEN]
        }.first()
    }

    /**
     * 获取已保存的用户信息
     * @return Pair(userId, nickname)，未登录时返回 null
     */
    suspend fun getUserInfo(): Pair<String, String>? {
        return context.dataStore.data.map { prefs ->
            val userId = prefs[KEY_USER_ID]
            val nickname = prefs[KEY_NICKNAME]
            if (userId != null && nickname != null) {
                Pair(userId, nickname)
            } else {
                null
            }
        }.first()
    }

    /**
     * 检查是否已登录
     */
    suspend fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    /**
     * 登出，清除所有本地认证数据
     */
    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_NICKNAME)
        }
        RetrofitClient.token = null
        currentDeviceCode = null
    }

    /**
     * 启动时恢复 Token（从本地存储加载到内存）
     */
    suspend fun restoreToken() {
        val token = getToken()
        if (token != null) {
            RetrofitClient.token = token
        }
    }
}
