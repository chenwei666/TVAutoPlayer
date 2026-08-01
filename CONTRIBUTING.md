# 贡献指南

感谢参与电视自动播放器的改进。提交代码前请先确认改动与 Android TV、本地视频播放或项目工程质量直接相关。

## 开发环境

- JDK 17
- Android SDK Platform 36
- Android SDK Build Tools 36.0.0
- Windows PowerShell（使用项目构建脚本）或 Android Studio

## 开发流程

1. Fork 仓库并从 `main` 创建功能分支。
2. 保持改动小而清晰，不提交密钥、签名文件、个人视频或构建目录。
3. 为播放策略、播放列表规则等纯逻辑补充单元测试。
4. 运行 `./scripts/build.ps1`，确保单元测试、Android Lint 和 Debug APK 构建全部通过。
5. 使用 Conventional Commits，例如 `feat: add playlist reordering`、`fix: skip unreadable media item`。
6. 提交 Pull Request，说明需求、实现、兼容性、测试结果和人工电视验收范围。

## 兼容性要求

- 最低 Android 6.0（API 23）。
- 保持遥控器 D-pad 可操作，不依赖触摸屏。
- 不扩大存储权限；仅访问用户通过系统文件选择器授权的视频。
- 不引入网络、数据库或数据收集能力，除非对应需求经过明确讨论。

## Bug 报告

请提供电视/盒子型号、Android 版本、视频容器与编码、复现步骤、预期结果和实际结果。不要上传含隐私内容的视频；可使用无敏感信息的最小样本。
