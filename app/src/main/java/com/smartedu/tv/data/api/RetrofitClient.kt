package com.smartedu.tv.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 网络客户端单例
 *
 * 国家智慧教育平台的API全部是静态JSON文件，通过CDN分发
 * 不需要特殊的请求头或认证（课程浏览和视频播放公开访问）
 */
object RetrofitClient {

    var token: String? = null

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://basic.smartedu.cn/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: SmartEduApi = retrofit.create(SmartEduApi::class.java)

    /**
     * 构建视频播放URL
     *
     * 视频资源的URL格式：
     * https://r{1-3}-ndr.ykt.cbern.com.cn/edu_product/esp/micro_lesson_video/{id}.t/zh-CN/{timestamp}/transcode/videos/{quality}/1.mp4
     *
     * 常用画质：
     * - 1920x1080: transcode/videos/1920x1080/1.mp4
     * - 1280x720: transcode/videos/1280x720/1.mp4
     * - 640x480: transcode/videos/640x480/1.mp4
     */
    fun buildVideoUrl(resourceId: String, timestamp: String, quality: String = "1920x1080"): String {
        // 尝试多个CDN节点
        val cdnNodes = listOf("r1-ndr", "r2-ndr", "r3-ndr")
        val cdn = cdnNodes[(resourceId.hashCode().and(0x7FFFFFFF)) % cdnNodes.size]
        return "https://${cdn}.ykt.cbern.com.cn/edu_product/esp/micro_lesson_video/${resourceId}.t/zh-CN/${timestamp}/transcode/videos/${quality}/1.mp4"
    }

    /**
     * 从preview URL中提取resourceId和timestamp
     * 输入: https://r1-ndr.ykt.cbern.com.cn/edu_product/esp/micro_lesson_video/3cc1dafc-4efb-471d-e2c3-2d191a28bf58.t/zh-CN/1779785801600/transcode/videos/frame1-1920x1080-cover/1.jpg
     * 输出: Pair("3cc1dafc-4efb-471d-e2c3-2d191a28bf58", "1779785801600")
     */
    fun parseVideoInfo(previewUrl: String): Pair<String, String>? {
        val regex = Regex("""/([a-f0-9-]{36})\.t/zh-CN/(\d+)/""")
        val match = regex.find(previewUrl) ?: return null
        return Pair(match.groupValues[1], match.groupValues[2])
    }
}
