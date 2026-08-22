# TV Auto Player

<p align="center">
  <strong>让 Android TV 自动循环播放 U 盘或本机中的图片和视频。</strong>
</p>

<p align="center">
  Offline image & video slideshow for Android TV, digital signage and unattended displays.
</p>

<p align="center">
  <a href="https://github.com/chenwei666/TVAutoPlayer/releases/latest"><strong>下载最新版 APK</strong></a>
  · <a href="README_EN.md">English</a>
  · <a href="ROADMAP.md">Roadmap</a>
  · <a href="https://github.com/chenwei666/TVAutoPlayer/issues">反馈问题</a>
</p>

<p align="center">
  <a href="https://github.com/chenwei666/TVAutoPlayer/actions/workflows/android.yml"><img src="https://github.com/chenwei666/TVAutoPlayer/actions/workflows/android.yml/badge.svg" alt="Android CI"></a>
  <a href="https://github.com/chenwei666/TVAutoPlayer/releases/latest"><img src="https://img.shields.io/github/v/release/chenwei666/TVAutoPlayer?label=release" alt="Release"></a>
  <img src="https://img.shields.io/badge/Android-6.0%2B-3DDC84?logo=android&logoColor=white" alt="Android 6+">
  <img src="https://img.shields.io/badge/Privacy-Offline-success" alt="Offline and private">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="MIT License"></a>
</p>

TV Auto Player 是一款面向 **Android TV、电视盒子、门店屏幕和数字标牌** 的开源离线播放器。选中图片和视频后即可按顺序无限循环；设备重启后可尝试自动恢复播放。

应用不联网、不上传媒体、不收集用户数据，也不需要第三方文件选择器。播放列表和设置全部保存在设备本地。

## 适合这些场景

- 门店促销广告和电子菜单
- 公司前台、展厅和会议室展示屏
- 工厂看板与内部通知屏
- 学校、医院和公共区域信息屏
- 家庭电视照片与视频循环播放
- 无网络或不允许上传媒体的离线环境

## 为什么选择 TV Auto Player？

- **图片和视频混合轮播**：图片按设定时间切换，视频播放结束后自动进入下一项。
- **直接扫描本机和 U 盘**：无需安装“文件”“文档”或第三方文件管理器。
- **保存播放列表**：重新打开应用无需重复选择。
- **面向电视遥控器设计**：支持方向键、确认、返回、菜单和设置键。
- **开机播放**：监听标准开机与部分 Quick Boot 广播，并明确提示厂商系统限制。
- **多分辨率适配**：适配常见 720p、1080p 和 4K 电视界面。
- **离线与隐私优先**：不联网、不上传、不采集媒体内容或用户数据。
- **中英文界面**：自动跟随电视系统语言。

## 3 分钟开始使用

1. 从 [GitHub Releases](https://github.com/chenwei666/TVAutoPlayer/releases/latest) 下载 APK。
2. 将 APK 复制到 U 盘，在电视上安装并允许“安装未知应用”。
3. 首次启动时授予照片和视频读取权限。
4. 从电视内部存储或 U 盘中按希望的顺序勾选图片和视频。
5. 选择“保存所选媒体并开始播放”。
6. 播放中按遥控器返回键，可更换列表、追加媒体、调整图片时长、声音和显示比例。

也可以通过 ADB 安装：

```powershell
adb install -r .\TVAutoPlayer-v1.4.0-debug.apk
```

当前稳定版：**V1.4.0**

> 当前 Release APK 使用 Android Debug 证书签名，适合测试、内网、展厅和门店侧载。正式批量部署前，请使用企业自己的 Release 证书构建并妥善保管签名密钥。

## 核心功能

| 能力 | 说明 |
|---|---|
| 混合播放列表 | 图片与视频可混合编排，并按勾选顺序无限循环 |
| 图片停留时间 | 支持 5、10、15、30、60 秒，默认 10 秒 |
| 多存储扫描 | 扫描内部共享存储及已挂载 U 盘 |
| 列表管理 | 重新选择、追加视频、URI 自动去重、清空列表 |
| 显示模式 | 完整显示或裁切铺满 |
| 本地记忆 | 保存播放列表、声音、比例和开机播放设置 |
| 容错 | 媒体被移动、删除或授权失效时明确提示 |
| 电视体验 | 遥控器操作、沉浸式全屏、播放期间保持屏幕常亮 |

## 推荐媒体格式

最高兼容组合：

```text
MP4 + H.264/AVC + AAC
```

- 图片：JPEG/JPG、PNG、WebP、BMP、HEIF/HEIC。
- 视频容器：MP4、MKV、WebM、MPEG-TS 等。
- H.265/HEVC、AV1、4K、高码率、10-bit 和特殊音频格式取决于电视硬件解码能力。
- AVI 等老旧格式兼容性不稳定，建议转码为 MP4。
- GIF 动图暂不支持；AVIF 需要 Android 14 及以上系统解码支持。

## 开机自动播放说明

应用声明了 `RECEIVE_BOOT_COMPLETED`，并监听标准开机广播及部分电视盒子的 Quick Boot 广播。

Android 10 以后限制后台直接启动界面，不少电视厂商还会增加自启动、电池优化或后台运行限制。第一次安装后，请在电视设置中将本应用加入：

- 允许自启动
- 允许后台运行
- 不受电池优化限制

普通第三方 APK 无法无权限绕过厂商限制。需要百分之百无人值守启动时，应使用企业 Device Owner / Kiosk 模式，或让设备厂商加入系统白名单。

## 隐私与权限

应用只申请开机广播和对应 Android 版本的照片、视频只读权限：

- Android 13+：`READ_MEDIA_IMAGES`、`READ_MEDIA_VIDEO`
- Android 12 及以下：受版本上限约束的 `READ_EXTERNAL_STORAGE`
- 不申请 `MANAGE_EXTERNAL_STORAGE`

应用不会访问网络、上传或修改媒体文件，也不会收集用户数据。卸载应用不会删除原始图片和视频。

## 开发与构建

- JDK 17
- Android SDK Platform 36
- Android SDK Build Tools 36.0.0
- Android Gradle Plugin 9.2.1
- Gradle 9.4.1
- 最低 Android 6.0（API 23）
- 目标 Android 16（API 36）

在 Windows 中运行：

```powershell
.\scripts\build.ps1
```

构建脚本会执行单元测试、Lint 和 Debug APK 构建。GitHub Actions 会在推送到 `main` 或提交 Pull Request 时运行质量门禁。

## 文档

- [架构说明](docs/architecture.md)
- [测试说明](docs/testing.md)
- [部署说明](docs/deployment.md)
- [变更记录](CHANGELOG.md)
- [贡献指南](CONTRIBUTING.md)
- [安全策略](SECURITY.md)
- [项目路线图](ROADMAP.md)

## 参与项目

特别欢迎以下贡献：

- 提交电视品牌、型号、Android 版本和 USB 存储兼容结果
- 提交真实门店、展厅、菜单屏等使用场景
- 改进电视遥控器体验和媒体格式兼容性
- 补充其他语言翻译
- 提交可复现的 Bug 与脱敏日志
- 如果项目帮到了你，请点一个 **Star**，让更多需要离线电视轮播的人找到它

## 许可证

本项目采用 [MIT License](LICENSE)。欢迎 Fork、改进，以及在符合许可证要求的个人或商业场景中使用。

维护者：[chenwei666](https://github.com/chenwei666)
