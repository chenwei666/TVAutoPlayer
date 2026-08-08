# 电视自动播放器

[![Android CI](https://github.com/chenwei666/TVAutoPlayer/actions/workflows/android.yml/badge.svg)](https://github.com/chenwei666/TVAutoPlayer/actions/workflows/android.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

面向 Android TV / 电视盒子的本地视频循环播放应用。V1.2.0 内置遥控器友好的视频选择器，会扫描电视内部存储和已挂载 U 盘中由系统媒体库识别的视频，不再依赖电视安装第三方文件选择器；以后打开应用会直接按选择顺序全屏循环播放，也可在电视开机后自动尝试启动。

## 已实现功能

- 首次启动自动进入内置媒体库，可一次选择电视存储或 U 盘中的 1 个或多个视频。
- 无需电视安装“文件”“文档”或第三方文件管理器；可选的系统文件选择器仅在设备支持时显示。
- 按 Android 版本申请最小视频读取权限，扫描全部已挂载共享存储卷，不申请 `MANAGE_EXTERNAL_STORAGE`。
- 多视频按选择顺序连续播放，最后一个播放完后从第一个继续循环。
- 设置页支持“重新选择播放列表”和“追加视频”，追加时按 URI 自动去重。
- 再次打开直接播放，无需重复选择；V1.0.0 的单视频设置会自动迁移。
- 使用 Android `dp/sp` 和 `smallestWidth` 资源适配 720p、1080p、4K 等电视分辨率。
- 支持遥控器方向键、确认键、返回键、菜单键和设置键。
- 播放中按“返回 / 菜单 / 设置”进入功能设置。
- 可更换/追加视频、清空播放列表、开关声音、切换完整显示/裁切铺满。
- 保存播放列表；MediaStore 视频使用系统媒体读取权限，系统选择器视频继续保存跨重启 URI 授权。
- 电视开机广播触发自动播放（默认开启，可在设置中关闭）。
- 视频被移动、删除或授权失效时给出明确提示，不崩溃。
- 播放失败时显示 Media3 错误码，并提供重试或更换视频。
- 播放期间保持屏幕常亮、沉浸式全屏。
- 不联网、不读取数据库、不收集用户数据。

## 直接安装

推荐从 GitHub Releases 下载经过校验的 APK：

[下载 TVAutoPlayer v1.2.0](https://github.com/chenwei666/TVAutoPlayer/releases/tag/v1.2.0)

方法一：把 APK 复制到 U 盘，在电视文件管理器中打开并允许“安装未知应用”。

方法二：电视打开 USB/网络调试后，在电脑执行：

```powershell
adb install -r .\TVAutoPlayer-v1.2.0-debug.apk
```

首次运行：

1. 打开“电视自动播放器”。
2. 按提示授予“视频”读取权限；Android 14+ 可选择允许全部视频或仅允许指定视频。
3. 内置媒体库会扫描电视内部存储和已挂载 U 盘，用遥控器确认键按希望的播放顺序勾选多个视频。
4. 选择“保存所选视频并开始播放”，应用会按勾选顺序循环播放。
5. 播放中按遥控器“返回”键进入设置，可重新选择整个列表或继续追加视频。
6. 如果列表为空，把视频放入电视或 U 盘的 `Movies/TVAutoPlay` 文件夹，重新插入 U 盘并稍等片刻后执行“重新扫描”。
7. 冷启动电视，验证厂商系统是否允许本应用自启动。

> 交付 APK 使用 Android Debug 证书签名，适合内网、展厅、门店等直接侧载测试。正式批量部署前请使用企业自己的发布证书构建 Release APK，并妥善保管签名密钥。

## 开机自动播放说明

应用已声明 `RECEIVE_BOOT_COMPLETED` 并监听标准开机广播及部分电视盒子使用的 Quick Boot 广播。Android 10 以后限制后台直接拉起界面，部分品牌电视还会增加自己的自启动限制。因此第一次安装后请在电视系统设置中把本应用加入“允许自启动 / 后台运行 / 不受电池优化限制”名单。

如果厂商系统仍然拦截，普通第三方 APK 无法无权限绕过。需要百分之百无人值守启动时，应把设备配置为企业 Device Owner / Kiosk，或由设备厂商把应用加入系统白名单。相关限制见 [Android 后台界面启动说明](https://developer.android.com/guide/components/activities/background-starts)。

## 格式兼容性

播放器使用 Media3 ExoPlayer 1.10.1，内置媒体库显示 Android MediaStore 已识别为视频的文件；可选系统选择器接受常见视频 MIME 类型。常见容器包括 MP4、MKV、WebM、MPEG-TS 等。容器能被识别不代表电视一定具备对应硬件解码器：

- 推荐最高兼容组合：MP4 + H.264/AVC + AAC。
- H.265/HEVC 需要电视芯片支持；老电视可能只有画面或无法播放。
- AV1 主要适用于较新的电视芯片。
- 4K、高码率、10-bit、特殊字幕或无损音频受电视硬件能力影响。
- AVI 等老旧容器兼容性不稳定，建议先转码为 MP4。

## 开发环境与构建

- JDK 17
- Android SDK Platform 36
- Android SDK Build Tools 36.0.0
- Android Gradle Plugin 9.2.1
- Gradle 9.4.1（已包含 Wrapper）
- 最低系统 Android 6.0（API 23）
- 目标系统 Android 16（API 36）

Android Studio 可直接打开项目。命令行构建：

```powershell
.\scripts\build.ps1
```

脚本会把源码同步到 `C:\tmp\TVAutoPlayer-build` 的英文临时路径，再依次运行单元测试、Lint 和 Debug APK 构建，最后把 APK 复制到 `dist`。这样可规避 Windows 下 Gradle 测试进程对中文项目路径的兼容问题。脚本只会清理自己固定的临时构建目录，不会删除项目源码。若需要改用其他英文目录，可先设置环境变量 `TV_AUTOPLAYER_BUILD_ROOT`。

GitHub Actions 会在推送到 `main` 或提交 Pull Request 时自动执行相同质量门禁，并上传 Debug APK 构建产物。

## 项目结构

```text
app/src/main/java/...        播放策略、配置、MediaStore 扫描、内置选择器、播放器与开机接收器
app/src/main/res/            电视布局、样式、图标和中文文案
app/src/test/                播放列表顺序、去重、启动与播放策略单元测试
docs/                        架构、测试和部署说明
scripts/build.ps1            Windows 可复现构建脚本
dist/                        可安装 APK 与 SHA-256
Version/V1.0.0/              V1.0.0 完整源码快照
Version/V1.1.0/              V1.1.0 完整源码快照
Version/V1.1.1/              V1.1.1 完整源码快照
Version/V1.1.2/              V1.1.2 完整源码快照
Version/V1.2.0/              V1.2.0 完整源码快照
```

## 隐私与权限

应用申请 `RECEIVE_BOOT_COMPLETED` 和 Android 版本对应的视频只读权限：Android 13+ 使用 `READ_MEDIA_VIDEO`，Android 14+ 同时兼容用户仅授权部分视频，Android 12 及以下使用受版本上限约束的 `READ_EXTERNAL_STORAGE`。应用只通过 MediaStore 枚举系统已索引的视频，不读取照片、不申请整盘管理权限、不复制或修改视频、不访问网络、不连接数据库，也不记录文件名或 URI 到业务日志。应用只保存播放列表的 URI、显示名称、顺序和非敏感播放偏好；卸载应用不会删除原视频。

更多信息：

- [架构说明](docs/architecture.md)
- [测试说明](docs/testing.md)
- [部署说明](docs/deployment.md)
- [变更记录](CHANGELOG.md)
- [贡献指南](CONTRIBUTING.md)
- [安全策略](SECURITY.md)

## 开源许可证

本项目采用 [MIT License](LICENSE) 开源。欢迎 Fork、改进和用于符合许可证要求的个人或商业场景。
