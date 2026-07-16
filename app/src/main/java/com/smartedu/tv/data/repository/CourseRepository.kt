package com.smartedu.tv.data.repository

import com.smartedu.tv.data.api.*
import com.smartedu.tv.data.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 课程仓库 — 对接真实API
 *
 * 数据流：
 * 1. getDataVersion() → 获取所有教材分片URL
 * 2. getMaterialPart(分片URL) → 获取课程列表（含ID、标题、标签）
 * 3. getChapterTree(treeURL) → 获取章节目录
 * 4. 视频URL从preview字段解析
 */
class CourseRepository {

    private val api = RetrofitClient.api

    companion object {
        // 数据版本API（固定URL）
        const val DATA_VERSION_URL =
            "https://s-file-2.ykt.cbern.com.cn/zxx/ndrs/national_lesson/teachingmaterials/version/data_version.json"

        // 教材详情模板
        const val DETAIL_URL_TEMPLATE =
            "https://s-file-1.ykt.cbern.com.cn/zxx/ndrs/national_lesson/teachingmaterials/details/%s.json"

        // 章节树模板
        const val TREE_URL_TEMPLATE =
            "https://s-file-1.ykt.cbern.com.cn/zxx/ndrv2/national_lesson/trees/%s.json"

        // 教材资源模板
        const val RESOURCES_URL_TEMPLATE =
            "https://s-file-1.ykt.cbern.com.cn/zxx/ndrs/national_lesson/teachingmaterials/%s/resources/part_100.json"
    }

    /**
     * 获取课程列表（按学段、年级筛选）
     *
     * @param section 学段: "小学" / "初中" / "高中"，null返回全部
     * @param grade 年级: "一年级" / "二年级" 等，null不筛选
     * @param limit 最大返回数量
     */
    suspend fun getCourses(section: String? = null, grade: String? = null, publisher: String? = null, limit: Int = 20): Result<List<Course>> {
        return try {
            // 1. 获取数据版本
            val version = api.getDataVersion(DATA_VERSION_URL)

            // 2. 从所有分片中获取课程（取第一个分片，数据量足够）
            val partUrl = version.urls.firstOrNull()
                ?: return Result.failure(Exception("无可用数据源"))

            val materials = api.getMaterialPart(partUrl)

            // 3. 筛选和转换
            val courses = materials
                .filter { item ->
                    val tags = item.tag_list ?: emptyList()
                    val sectionMatch = if (section == null || section == "全部学段") true else {
                        tags.find { it.tag_dimension_id == "zxxxd" }?.tag_name?.contains(section) == true
                    }
                    val gradeMatch = if (grade == null || grade == "全部年级") true else {
                        tags.find { it.tag_dimension_id == "zxxnj" }?.tag_name?.contains(grade) == true
                    }
                    val publisherMatch = if (publisher == null || publisher == "全部版本") true else {
                        tags.find { it.tag_dimension_id == "zxxbb" }?.tag_name?.contains(publisher) == true
                    }
                    sectionMatch && gradeMatch && publisherMatch
                }
                .take(limit)
                .mapNotNull { item -> mapToCourse(item) }

            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取课程详情（含章节目录和视频信息）
     */
    suspend fun getCourseDetail(courseId: String): Result<CourseDetail> {
        return try {
            // 1. 获取教材详情
            val detailUrl = String.format(DETAIL_URL_TEMPLATE, courseId)
            val detail = api.getMaterialDetail(detailUrl)

            // 2. 获取章节目录树
            val treeUrl = String.format(TREE_URL_TEMPLATE, courseId)
            val chapters = api.getChapterTree(treeUrl)

            // 3. 获取资源列表（含视频）并构建映射
            val resUrl = String.format(RESOURCES_URL_TEMPLATE, courseId)
            val videoMap = try {
                val resources = api.getPartResources(resUrl)
                val map = mutableMapOf<String, VideoInfo>()
                resources.forEach { part ->
                    val relations = part.relations?.national_course_resource ?: emptyList()
                    relations.forEach { res ->
                        val props = res.custom_properties
                        val urlParams = extractVideoUrlParams(props)
                        if (urlParams != null) {
                            var preview = props?.preview?.cover
                            if (preview == null && props?.thumbnails?.isNotEmpty() == true) {
                                preview = props.thumbnails.first()
                            }
                            
                            val info = VideoInfo(urlParams.first, urlParams.second, preview ?: "")
                            // 强大的多重映射机制：只要章节树能匹配上其中任何一个（ID或标题），就能找到视频
                            map[part.id] = info
                            map[res.id] = info
                            if (!part.title.isNullOrBlank()) map[part.title] = info
                            if (!res.title.isNullOrBlank()) map[res.title] = info
                        }
                    }
                }
                map
            } catch (e: Exception) {
                emptyMap()
            }

            // 4. 转换章节
            val flatChapters = flattenChapters(chapters, videoMap)

            // 5. 构建CourseDetail
            val tags = detail.tag_list ?: emptyList()
            val grade = tags.find { it.tag_dimension_id == "zxxnj" }?.tag_name ?: ""
            val subject = tags.find { it.tag_dimension_id == "zxxxk" }?.tag_name ?: ""
            val publisher = tags.find { it.tag_dimension_id == "zxxbb" }?.tag_name ?: ""
            val title = detail.global_title?.`zh-CN` ?: detail.title
            val cover = detail.custom_properties?.thumbnails?.firstOrNull() ?: ""

            Result.success(
                CourseDetail(
                    id = courseId,
                    title = title,
                    grade = grade,
                    subject = subject,
                    coverUrl = cover,
                    publisher = publisher,
                    chapters = flatChapters
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== 内部方法 ==========

    /** 视频信息 */
    data class VideoInfo(val resourceId: String, val timestamp: String, val coverUrl: String)

    /** 将MaterialItem转为Course */
    private fun mapToCourse(item: MaterialItem): Course? {
        val tags = item.tag_list ?: return null
        val grade = tags.find { it.tag_dimension_id == "zxxnj" }?.tag_name ?: ""
        val subject = tags.find { it.tag_dimension_id == "zxxxk" }?.tag_name ?: ""
        val publisher = tags.find { it.tag_dimension_id == "zxxbb" }?.tag_name ?: ""
        val title = item.title
        val cover = item.custom_properties?.thumbnails?.firstOrNull() ?: ""

        return Course(
            id = item.id,
            title = title,
            grade = grade,
            subject = subject,
            coverUrl = cover,
            publisher = publisher
        )
    }

    /** 递归展平章节树 */
    private fun flattenChapters(
        nodes: List<ChapterNode>,
        videoMap: Map<String, VideoInfo>,
        prefix: String = ""
    ): List<Chapter> {
        val result = mutableListOf<Chapter>()
        nodes.forEachIndexed { index, node ->
            val num = if (prefix.isEmpty()) "${index + 1}" else "$prefix.${index + 1}"
            val title = node.rich_title ?: node.title
            // 强大的兜底机制：优先尝试 ID 匹配，如果不匹配，再尝试标题匹配
            val video = videoMap[node.id] ?: videoMap[title]
            
            val videoUrl = if (video != null) {
                RetrofitClient.buildVideoUrl(video.resourceId, video.timestamp)
            } else ""

            result.add(
                Chapter(
                    id = node.id,
                    title = node.rich_title ?: node.title,
                    videoUrl = videoUrl,
                    duration = "",
                    coverUrl = video?.coverUrl ?: ""
                )
            )

            // 递归子节点
            if (node.child_nodes != null) {
                result.addAll(flattenChapters(node.child_nodes, videoMap, num))
            }
        }
        return result
    }
}
