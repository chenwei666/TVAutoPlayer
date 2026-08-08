# 测试说明

## 已确认的公开测试边界

1. 首次启动没有保存媒体时，内置媒体库请求对应 Android 版本的照片/视频只读权限。
2. 已保存视频且授权有效时，系统进入可循环播放状态。
3. 文件授权被撤销时，系统要求重新选择而不是崩溃。
4. 开机启动只在开关开启、视频已选择且授权有效时发生。
5. 内置媒体库按遥控器勾选顺序保存播放列表；可选系统选择器保持其返回顺序。
6. 追加视频时保留原顺序，并按 URI 自动去重。
7. 列表中至少一个视频可读时允许播放和开机启动；全部不可读时要求重新选择。
8. MediaStore 扫描结果按文件名稳定排序、按 URI 去重，并容忍空结果。
9. JPEG/PNG/WebP/BMP/HEIF/HEIC 等受支持图片可入列表；GIF 和未知文件会被过滤。
10. 图片停留时间按 5、10、15、30、60 秒循环，非法旧值回退为 10 秒。
11. 图片与视频混合列表保持选择顺序和媒体类型，旧播放项缺少类型时按视频兼容。

测试文件：

- `app/src/test/java/com/chenwei/tvautoplay/PlaybackPolicyTest.java`
- `app/src/test/java/com/chenwei/tvautoplay/PlaylistBehaviorTest.java`
- `app/src/test/java/com/chenwei/tvautoplay/MediaCatalogTest.java`
- `app/src/test/java/com/chenwei/tvautoplay/MediaTypeDetectorTest.java`
- `app/src/test/java/com/chenwei/tvautoplay/ImageDurationTest.java`

## 自动化命令

```powershell
.\scripts\build.ps1
```

脚本执行：

1. `testDebugUnitTest`
2. `lintDebug`
3. `assembleDebug`

GitHub Actions 工作流 `.github/workflows/android.yml` 会在 `main` 推送和 Pull Request 上使用 Windows Runner 执行同一构建脚本，并上传 Debug APK 作为工作流产物。

## 人工电视验收清单

- 在未安装或禁用系统文件选择器的电视上，首次打开仍能进入内置媒体库。
- 首次打开显示照片/视频读取权限；允许、拒绝、Android 14“仅选择部分媒体”三条路径均不崩溃。
- 取消选择后界面可用，不连续弹窗、不崩溃。
- 在电视内部存储和 U 盘分别准备图片与视频，确认两类来源都能扫描到并显示类型标签。
- 按“图片→视频→图片”顺序勾选，确认图片到时切换、视频播完切换、末项结束后回到首项。
- 分别选择 5/10/15/30/60 秒，使用计时器验证每张图片切换误差在设备可接受范围内。
- 选择 GIF、PDF 和未知扩展名，确认不会加入播放列表且应用不崩溃。
- 勾选后执行“重新扫描”，确认仍存在的勾选项及顺序被保留。
- 追加包含一个已选文件和一个新文件，确认只新增一个且原顺序不变。
- 重新选择播放列表，确认旧列表被完整替换且无需每次启动重选。
- 按 Home 再打开，确认直接播放且无需重选。
- 完全断电重启，确认厂商自启动设置开启时自动进入播放。
- 拔掉 U 盘或删除文件，确认提示重新选择。
- 扫描过程中拔掉 U 盘，确认应用不崩溃且仍能使用其他存储卷。
- 移动或删除列表中的一个视频，确认自动跳过；全部不可读时提示重新选择。
- 验证返回键、菜单键、方向键、确认键的焦点与操作。
- 验证声音开关、图片切换时间和两种画面模式；图片完整显示/裁切铺满都符合预期。
- 分别在 720p、1080p、4K 输出下验证设置页无裁切、按钮可聚焦、文字可读。
- 验证 1080p H.264/AAC；按现场需求增加 4K、HEVC、MKV 等样本。
- 连续循环 8 小时，观察卡顿、音画同步、温度和内存。

## 当前测试限制

V1.3.0 本地质量门禁已执行 `testDebugUnitTest`、`lintDebug` 和 `assembleDebug`；APK 已通过包清单、权限、ZIP 对齐、v1/v2 调试签名和 SHA-256 核验。验证开发机没有连接 Android TV、电视盒子或 TV 模拟器，因此无法执行真实图片渲染、时间精度、媒体索引、U 盘、遥控器和厂商开机策略测试。自动化测试不替代目标硬件验收。
