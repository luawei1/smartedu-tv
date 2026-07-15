package com.smartedu.tv.data.api

/**
 * API服务 — 已迁移至 SmartEduApi.kt（真实API）
 *
 * 本文件保留作为API端点文档参考
 *
 * === 国家中小学智慧教育平台 API 端点汇总 ===
 *
 * 【公开API · 无需登录】
 *
 * 1. 数据版本（获取所有教材分片URL）
 *    GET https://s-file-2.ykt.cbern.com.cn/zxx/ndrs/national_lesson/teachingmaterials/version/data_version.json
 *    → { module, module_version, urls: [".../part_100.json", ".../part_101.json", ...] }
 *
 * 2. 教材分片（获取课程列表，每个分片包含多个课程元数据）
 *    GET https://s-file-1.ykt.cbern.com.cn/zxx/ndrs/national_lesson/teachingmaterials/part_100.json
 *    → [{ id, title, tag_list, custom_properties, relations }, ...]
 *
 * 3. 教材详情（获取单个教材的详细信息）
 *    GET https://s-file-1.ykt.cbern.com.cn/zxx/ndrs/national_lesson/teachingmaterials/details/{material_id}.json
 *    → { id, title, global_title, tag_list, custom_properties }
 *
 * 4. 章节树（获取章节目录结构）
 *    GET https://s-file-1.ykt.cbern.com.cn/zxx/ndrv2/national_lesson/trees/{material_id}.json
 *    → [{ id, title, child_nodes: [...] }, ...]
 *
 * 5. 课程资源（获取课程包下的视频资源）
 *    GET https://s-file-1.ykt.cbern.com.cn/zxx/ndrs/national_lesson/teachingmaterials/{material_id}/resources/part_100.json
 *    → [{ id, relations: { course_resource: [...] } }, ...]
 *
 * 6. 标签体系（获取年级/学科/版本筛选条件）
 *    GET https://s-file-1.ykt.cbern.com.cn/zxx/ndrs/tags/national_lesson_tag.json
 *    → { hierarchies: [{ children: [{ tag_id, tag_name, tag_dimension_id }] }] }
 *
 * 【视频CDN】
 *
 * 视频URL格式：
 * https://r{1-3}-ndr.ykt.cbern.com.cn/edu_product/esp/micro_lesson_video/{resource_id}.t/zh-CN/{timestamp}/transcode/videos/{quality}/1.mp4
 *
 * 画质选项：1920x1080 / 1280x720 / 640x480
 * CDN节点：r1-ndr / r2-ndr / r3-ndr（自动选择）
 *
 * 封面图URL格式：
 * https://r{1-3}-ndr.ykt.cbern.com.cn/edu_product/esp/micro_lesson_video/{resource_id}.t/zh-CN/{timestamp}/transcode/videos/frame1-1920x1080-cover/1.jpg
 *
 * 【登录API · 需要认证】
 *
 * SSO认证：sso.basic.smartedu.cn/v1.1/sso/
 * APP-ID：e5649925-441d-4a53-b525-51a2f1c4e0a8
 *
 * 注意：课程浏览和视频播放不需要登录，登录仅用于：
 * - 学习进度记录
 * - 收藏功能
 * - 个人中心
 */
