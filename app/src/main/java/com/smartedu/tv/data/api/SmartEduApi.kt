package com.smartedu.tv.data.api

import retrofit2.http.GET
import retrofit2.http.Url

/**
 * 国家中小学智慧教育平台 · 真实API接口
 *
 * API架构：
 * - 课程元数据: s-file-1/2.ykt.cbern.com.cn (JSON静态文件，CDN分发)
 * - 视频资源: r1/r2/r3-ndr.ykt.cbern.com.cn (多CDN节点)
 * - 用户认证: sso.basic.smartedu.cn (需要登录的接口)
 *
 * 课程浏览和视频播放不需要登录，公开访问
 */
interface SmartEduApi {

    /**
     * 获取数据版本信息（包含所有教材分片URL）
     * 返回的urls是所有part JSON文件的地址
     */
    @GET
    suspend fun getDataVersion(@Url url: String): DataVersion

    /**
     * 获取教材分片列表（包含具体课程元数据）
     * 每个分片包含多个teachingmaterials的详细信息
     */
    @GET
    suspend fun getMaterialPart(@Url url: String): List<MaterialItem>

    /**
     * 获取课程详情（教材元数据）
     */
    @GET
    suspend fun getMaterialDetail(@Url url: String): MaterialDetail

    /**
     * 获取章节目录树
     */
    @GET
    suspend fun getChapterTree(@Url url: String): List<ChapterNode>

    /**
     * 获取课程包资源（含视频信息）
     */
    @GET
    suspend fun getPartResources(@Url url: String): List<PartResource>
}

// ========== 数据模型 ==========

/** 数据版本响应 */
data class DataVersion(
    val module: String,
    val module_version: Long,
    val urls: List<String>
)

/** 教材分片中的单个课程项 */
data class MaterialItem(
    val id: String,
    val title: String,
    val custom_properties: MaterialCustomProps?,
    val tag_list: List<Tag>?,
    val relations: MaterialRelations?
)

data class MaterialCustomProps(
    val thumbnails: List<String>?,
    val global_resource_id: String?
)

data class MaterialRelations(
    val national_course_resource: List<CourseResource>?
)

data class CourseResource(
    val id: String,
    val global_title: MultiLang?,
    val custom_properties: CourseResourceProps?
)

data class CourseResourceProps(
    val preview: VideoPreview?,
    val thumbnails: List<String>?
)

data class VideoPreview(
    val frame1: String?,
    val cover: String?
)

/** 教材详情 */
data class MaterialDetail(
    val id: String,
    val title: String,
    val global_title: MultiLang?,
    val tag_list: List<Tag>?,
    val custom_properties: MaterialCustomProps?
)

data class MultiLang(
    @Suppress("PropertyName") val `zh-CN`: String?
)

/** 标签 */
data class Tag(
    val tag_id: String,
    val tag_name: String,
    val tag_dimension_id: String
)

/** 章节树节点 */
data class ChapterNode(
    val id: String,
    val title: String,
    val rich_title: String?,
    val child_nodes: List<ChapterNode>?
)

/** 课程包资源 */
data class PartResource(
    val id: String,
    val title: String?,
    val custom_properties: PartCustomProps?,
    val relations: PartRelations?
)

data class PartCustomProps(
    val thumbnails: List<String>?,
    val lesson_extra: Any?
)

data class PartRelations(
    val national_course_resource: List<CourseResource>?
)
