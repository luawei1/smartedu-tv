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

            // 2. 并行获取所有分片的数据（共3个分片，全部合并）
            val materials = coroutineScope {
                version.urls.map { url ->
                    async {
                        try {
                            api.getMaterialPart(url)
                        } catch (e: Exception) {
                            emptyList<MaterialItem>()
                        }
                    }
                }.awaitAll().flatten()
            }

            // 3. 筛选和转换
            val courses = materials
                .filter { item ->
                    val tags = item.tag_list ?: emptyList()
                    
                    // 学段匹配
                    val sectionMatch = if (section == null || section == "全部学段") true else {
                        tags.find { it.tag_dimension_id == "zxxxd" }?.tag_name?.contains(section) == true
                    }
                    
                    // 年级匹配 (加入高中“必修/选择性必修”兜底逻辑)
                    val gradeMatch = if (grade == null || grade == "全部年级") true else {
                        val gradeTag = tags.find { it.tag_dimension_id == "zxxnj" }?.tag_name ?: ""
                        val bookTag = tags.find { it.tag_dimension_id == "zxxcc" }?.tag_name ?: ""
                        
                        if (section == "高中") {
                            // 高一匹配： explicit 高一 标签，或者教材名称含有“必修”且不含“选择性”
                            if (grade == "高一") {
                                gradeTag.contains("高一") || (bookTag.contains("必修") && !bookTag.contains("选择性必修"))
                            } 
                            // 高二/高三匹配：explicit 标签，或者教材名称包含“选择性必修”或“选修”
                            else if (grade == "高二" || grade == "高三") {
                                gradeTag.contains(grade) || bookTag.contains("选择性必修") || bookTag.contains("选修")
                            } else {
                                gradeTag.contains(grade)
                            }
                        } else {
                            gradeTag.contains(grade)
                        }
                    }
                    
                    // 版本匹配
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
                        var preview = res.custom_properties?.preview?.frame1
                        if (preview == null && res.custom_properties?.thumbnails?.isNotEmpty() == true) {
                            preview = res.custom_properties.thumbnails.first()
                        }
                        
                        if (preview != null) {
                            val info = RetrofitClient.parseVideoInfo(preview)
                            if (info != null) {
                                val videoInfo = VideoInfo(info.first, info.second, preview)
                                // 多重映射
                                map[part.id] = videoInfo
                                map[res.id] = videoInfo
                                part.chapter_ids?.forEach { chId ->
                                    map[chId] = videoInfo
                                }
                                if (!part.title.isNullOrBlank()) map[part.title] = videoInfo
                                val resTitle = res.global_title?.`zh-CN`
                                if (!resTitle.isNullOrBlank()) map[resTitle] = videoInfo
                            }
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
