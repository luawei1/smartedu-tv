package com.smartedu.tv.data.model

/**
 * 课程模型（对齐真实API数据结构）
 */
data class Course(
    val id: String,
    val title: String,
    val grade: String,
    val subject: String,
    val coverUrl: String,
    val publisher: String
)

data class CourseDetail(
    val id: String,
    val title: String,
    val grade: String,
    val subject: String,
    val coverUrl: String,
    val publisher: String,
    val chapters: List<Chapter>
)

data class Chapter(
    val id: String,
    val title: String,
    val videoUrl: String,
    val duration: String,
    val coverUrl: String,
    /** 视频资源ID (用于WebView播放器) */
    val resourceId: String = ""
) {
    /** 是否有可播放的视频 */
    val hasVideo: Boolean get() = resourceId.isNotBlank() || videoUrl.isNotBlank()
}
