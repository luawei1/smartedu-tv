# 智慧教育TV (SmartEdu TV)

国家中小学智慧教育平台 · Android TV 版本

## 功能特性

- 📺 **课程浏览** — 按年级/学科浏览同步课堂课程
- 🔐 **扫码登录** — TV端二维码扫码，手机确认授权
- ▶️ **视频播放** — ExoPlayer 高清播放，遥控器操控
- 🎮 **遥控器适配** — 完整的焦点管理和D-pad导航
- 📱 **触摸兼容** — 同时支持触摸屏操作（Pad可用）

## 技术栈

- Kotlin + Jetpack Compose for TV
- Media3 / ExoPlayer 视频播放
- Retrofit + OkHttp 网络层
- DataStore 本地存储
- ZXing 二维码生成

## 项目结构

```
app/src/main/java/com/smartedu/tv/
├── SmartEduApp.kt          # Application
├── MainActivity.kt          # 入口 Activity
├── auth/                    # 登录认证
│   └── QrCodeGenerator.kt
├── data/
│   ├── api/                 # 网络接口
│   ├── model/               # 数据模型
│   └── repository/          # 数据仓库
├── player/                  # 视频播放器
└── ui/
    ├── components/          # 通用组件
    ├── navigation/          # 路由导航
    ├── screens/             # 页面
    └── theme/               # 主题样式
```

## 构建运行

1. Android Studio 打开项目
2. 连接 Android TV 设备或启动模拟器
3. Run → Run 'app'

## 开发状态

- [x] 项目框架搭建
- [x] TV导航与焦点管理
- [x] 课程浏览首页
- [x] 课程详情页
- [x] 视频播放器
- [x] 扫码登录（Mock）
- [ ] 对接真实API
- [ ] 收藏功能
- [ ] 搜索功能
- [ ] 断点续播持久化
