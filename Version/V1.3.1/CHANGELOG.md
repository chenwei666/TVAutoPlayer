# CHANGELOG

## V1.3.1 - 2026-08-12

- 开发人员：chenwei666
- 版本类型：开发人员署名与发布元数据修正版

### 新增功能

- README 新增开发人员与维护者信息，并链接到 GitHub 账号 `chenwei666`。
- 新增 `docs/releases/v1.3.1.md` 和 `Version/V1.3.1` 完整项目快照。

### 问题修复

- 修复当前变更记录及全部历史版本归档中的开发人员署名不一致问题，统一为 `chenwei666`。
- 修复本地 Git 仓库提交者名称与 GitHub 账号名称不一致的问题。

### 优化内容

- 建立不区分大小写的全仓库署名残留检查，覆盖当前源码、文档和 `Version` 归档。
- GitHub Actions、本地构建脚本、README 和部署文档统一使用 V1.3.1 产物名称。
- 保持图片/视频混合循环、图片切换时间、媒体库、遥控器、开机启动和配置持久化行为不变。

### 删除内容

- 删除仓库文本文件中的旧开发工具署名及附带说明。
- 未删除任何播放功能、媒体格式、权限处理、测试或兼容代码。

### 修改文件

- 修改根目录和 `Version/V1.0.0`～`Version/V1.3.0` 中的 `CHANGELOG.md`。
- 修改 `README.md`、`app/build.gradle.kts`、`scripts/build.ps1`、`.github/workflows/android.yml`。
- 修改 `docs/deployment.md`、`docs/testing.md`，新增 `docs/releases/v1.3.1.md`。
- 新增 `Version/V1.3.1` 完整项目快照和 V1.3.1 APK 校验文件。

### 影响模块

- 项目署名、版本元数据、文档、构建产物命名、持续集成和开源发布流程。
- 播放器、媒体库、权限、配置和电视 UI 业务逻辑不受影响。

### 数据库变更

- 无数据库；应用不会读取、查询或连接数据库。

### 接口变更

- 无网络 API、外部业务接口或 Android 组件接口变更。

### 配置变更

- `versionCode` 从 6 升至 7，`versionName` 从 1.3.0 升至 1.3.1。
- 构建产物名称升级为 `TVAutoPlayer-v1.3.1-debug.apk`。
- 仓库级 Git `user.name` 设置为 `chenwei666`；不修改全局 Git 配置。

### 兼容性说明

- 最低 Android 6.0（API 23）和目标 Android 16（API 36）不变。
- V1.0.0～V1.3.0 的播放列表、URI 权限、图片时长、声音、画面和开机设置可直接保留。
- 应用 ID 和 Debug 签名保持不变，可覆盖安装。

### 升级方式

- 相同签名可执行 `adb install -r TVAutoPlayer-v1.3.1-debug.apk` 覆盖安装并保留设置。
- 不同签名无法直接覆盖；卸载重装会清除应用配置，但不会删除原媒体文件。

### 已知问题

- 发布 APK 使用 Android Debug 证书；正式批量部署应使用企业固定 Release 证书。
- 验证开发机没有连接真实 Android TV 或 TV 模拟器，硬件解码、U 盘、遥控器和厂商开机策略仍需目标硬件验收。

### 备注

- 本版本不包含业务功能变更，质量门禁仍完整执行单元测试、Android Lint 和 Debug APK 构建。
- 发布 APK SHA-256：`8434beae082e68dc207b2d6dc4d340a8296190a914db7869dc2d9ed86f8313f3`。

## V1.3.0 - 2026-08-08

- 开发人员：chenwei666
- 版本类型：图片轮播与混合媒体功能版本

### 新增功能

- 新增图片轮播，支持图片与视频在同一播放列表中按选择顺序混合循环。
- 新增图片停留时间设置，可用遥控器在 5、10、15、30、60 秒之间循环选择，默认 10 秒。
- 新增 `MediaKind`，播放项可明确标记为 `IMAGE` 或 `VIDEO`。
- 新增 `MediaTypeDetector`，按 MIME 和扩展名识别支持格式并过滤 GIF、PDF 等不支持文件。
- 新增图片 MediaStore 扫描和 `READ_MEDIA_IMAGES` 分版本权限处理。
- 新增图片/视频类型标签，选择列表和当前播放列表可以直观看出媒体类型。
- 新增 `ImageDurationTest`、`MediaTypeDetectorTest` 以及混合播放列表测试。

### 问题修复

- 修复 V1.2.0 只能扫描和选择视频、无法把图片加入循环播放的问题。
- 修复图片没有播放时长时 Media3 无法自动切换到下一项的问题。
- 修复媒体类型扩展后可能遗漏旧版系统选择器接受的 MKV 与 HLS MIME 类型的问题。
- 修复 GIF 等 Media3 暂不支持的图片可能进入播放列表后反复触发解码错误的问题。

### 优化内容

- 使用 `PlaybackMediaItemFactory` 集中创建 Media3 项：图片附加 `imageDurationMs`，视频保留原始时长。
- 将 `MediaStoreVideoRepository` 深化为 `MediaStoreMediaRepository`，统一扫描图片与视频，避免重复仓储实现。
- 旧播放列表 JSON 缺少 `kind` 时默认按视频读取，保持 V1.0.0～V1.2.0 无损升级。
- 图片时长使用枚举限制合法范围，配置损坏或未来未知值自动回退到 10 秒。
- Media3 不稳定 API 的 opt-in 仅限定在媒体项工厂，未做全项目 Lint 豁免。
- 延续不联网、不访问数据库、不记录 URI/文件名、不申请整盘管理权限的隐私边界。

### 删除内容

- 删除仅支持视频的 `MediaStoreVideoRepository`，由功能更完整的 `MediaStoreMediaRepository` 替代。
- 未删除视频循环、系统文件选择器、开机启动、声音、画面适配或旧播放列表兼容功能。

### 修改文件

- 新增 `MediaKind.java`、`ImageDuration.java`、`MediaTypeDetector.java`、`MediaStoreMediaRepository.java`、`PlaybackMediaItemFactory.java`。
- 修改播放项、播放配置、SharedPreferences 持久化、媒体权限、URI 类型识别、内置媒体库、播放器和设置页。
- 修改 Manifest、设置布局、中文文案、单元测试、版本号、构建脚本和 GitHub Actions。
- 更新 README、安全策略、架构、测试、部署说明及 `docs/releases/v1.3.0.md`。
- 新增 `Version/V1.3.0` 完整项目快照。

### 影响模块

- 媒体模型、媒体发现、权限、播放列表持久化、Media3 播放、设置页、电视 UI、测试、构建和发布流程。
- 开机广播、循环策略、失败跳过、声音与分辨率适配逻辑继续复用原实现。

### 数据库变更

- 无数据库；应用不会读取、查询或连接数据库。

### 接口变更

- 无网络 API 或外部业务接口变更。
- `PlaylistItem` 新增媒体类型构造参数和 `kind()`；旧双参数构造保留并默认视频。
- `PlaybackConfiguration` 新增图片停留时间和 `hasMedia/mediaCount`，旧 `hasVideo/videoCount` 作为兼容别名保留。
- `PlaybackConfigStore` 的播放列表 JSON 新增可选 `kind` 字段。

### 配置变更

- `versionCode` 从 5 升至 6，`versionName` 从 1.2.0 升至 1.3.0。
- SharedPreferences 新增 `image_duration`；默认 `TEN_SECONDS`。
- Manifest 新增 `READ_MEDIA_IMAGES`，系统文件选择器查询范围扩展为图片和视频。

### 兼容性说明

- 最低 Android 6.0（API 23）和目标 Android 16（API 36）不变。
- V1.0.0～V1.2.0 播放列表、URI 权限、声音、画面和开机设置可直接保留。
- 图片支持 BMP、JPEG、PNG、WebP、HEIF/HEIC；AVIF 只在 Android 14+ 解码；GIF 暂不支持。
- 视频解码能力仍由电视芯片决定，推荐 MP4/H.264/AAC。

### 升级方式

- 相同签名可执行 `adb install -r TVAutoPlayer-v1.3.0-debug.apk` 覆盖安装并保留设置。
- 升级后首次进入媒体库时，Android 13+ 可能追加询问照片读取权限。
- 不同签名无法覆盖；卸载重装会清除播放列表、权限和偏好，但不会删除原媒体。

### 已知问题

- 发布 APK 为 Android Debug 证书签名，正式批量部署应使用企业固定 Release 证书。
- GIF 动图没有 Media3 Extractor 支持，因此在选择阶段过滤。
- 验证开发机没有连接真实 Android TV 或 TV 模拟器，图片渲染、切换时间精度、U 盘索引、遥控器和开机策略仍需目标硬件验收。
- Android 10+ 及部分电视厂商可能限制普通第三方应用从开机广播直接拉起界面。

### 备注

- 本地已通过单元测试、Android Lint 和 Debug APK 构建；最终 APK 清单、签名、ZIP 对齐与 SHA-256 在发布前复核。
- 发布 APK SHA-256：`b3d6d94d7879b429001d8e563cc822dd3a79bab1a9c3f1cfd8ed39a2ea524392`。

## V1.2.0 - 2026-08-08

- 开发人员：chenwei666
- 版本类型：电视存储兼容性与内置媒体库功能版本

### 新增功能

- 新增遥控器友好的内置媒体库，不再依赖电视安装系统“文件/文档”选择应用。
- 新增电视内部存储与全部已挂载共享存储卷（包括系统可识别的 U 盘）视频扫描。
- 新增 Android 6～12、Android 13、Android 14+ 分版本视频读取权限处理，并兼容 Android 14 部分媒体授权。
- 新增多视频勾选顺序跟踪；重新扫描时保留仍然可见的勾选项及顺序。
- 新增扫描中、无结果、权限拒绝、扫描异常和至少选择一项等电视端明确提示。
- 新增 `MediaCatalogTest`，覆盖扫描结果排序、URI 去重、空值和空列表边界。
- 保留系统文件选择器作为可选兼容入口，仅在电视存在可处理应用时显示。

### 问题修复

- 修复部分 Android TV 没有文件选择器时首次设置直接提示“找不到打开文件的应用”、无法完成选片的问题。
- 修复设备上一个可移动存储卷在扫描期间拔出时可能导致整个扫描失败的问题；现在只跳过异常卷。
- 修复内置选择结果原先按文件名而不是按用户勾选先后保存的问题。
- 修复新增目录逻辑使用 API 24 集合方法而不兼容最低 API 23 的 Lint 错误。

### 优化内容

- MediaStore 查询在后台单线程执行，避免阻塞电视主线程；Activity 退出后停止执行器并忽略迟到的界面回调。
- 扫描结果集中排序和去重，播放列表继续复用 `PlaylistEditor` 的稳定顺序和 URI 去重规则。
- SAF 长期授权释放逻辑集中到 `UriAccess`，避免设置页与内置媒体库重复实现。
- 不记录卷名、文件名或 URI；不申请 `MANAGE_EXTERNAL_STORAGE`，不复制或修改视频。
- GitHub Actions、构建脚本和产物名称同步升级到 V1.2.0。

### 删除内容

- 删除“首次运行必须依赖系统文件选择器”的强制流程；系统文件选择器功能本身保留为可选入口。
- 未删除原有单视频、多视频循环、系统选择器、开机启动、声音和画面适配功能。

### 修改文件

- 新增 `MediaCatalog.java`、`MediaPermissionHelper.java`、`MediaStoreVideoRepository.java`、`MediaLibraryActivity.java`。
- 新增 `activity_media_library.xml`、`item_media_video.xml` 和 `MediaCatalogTest.java`。
- 修改 `AndroidManifest.xml`、`SettingsActivity.java`、`UriAccess.java`、设置页布局和中文文案。
- 修改版本号、Windows 构建脚本、GitHub Actions、README、安全策略、架构、测试和部署文档。
- 新增 `docs/releases/v1.2.0.md` 和 `Version/V1.2.0` 完整项目快照。

### 影响模块

- 首次启动、媒体权限、电视/U 盘视频发现、播放列表替换与追加、设置页、URI 授权清理、电视 UI、测试、构建和发布流程。
- 播放器循环、错误跳过、开机广播和原配置持久化格式保持兼容。

### 数据库变更

- 无数据库；应用不会读取、查询或连接数据库。

### 接口变更

- 无网络 API 或外部业务接口变更。
- Android 组件新增未导出的 `MediaLibraryActivity`。
- Manifest 新增分版本视频只读权限和系统文件选择器可见性查询声明。

### 配置变更

- `versionCode` 从 4 升至 5，`versionName` 从 1.1.2 升至 1.2.0。
- SharedPreferences 键和 JSON 播放列表格式不变，无需数据迁移。

### 兼容性说明

- 最低 Android 6.0（API 23）和目标 Android 16（API 36）不变。
- V1.0.0～V1.1.2 已保存的 SAF URI、播放顺序、声音、画面和开机设置可直接保留。
- 内置媒体库只能显示系统 MediaStore 已索引且当前授权可见的视频；电视硬件解码能力仍决定具体格式能否播放。
- 若厂商未及时索引 U 盘，建议将视频放入 `Movies/TVAutoPlay`，重新插入并稍等后重新扫描。

### 升级方式

- 相同签名可执行 `adb install -r TVAutoPlayer-v1.2.0-debug.apk` 覆盖安装并保留设置。
- 不同签名无法覆盖安装；卸载重装会清除播放列表、权限和偏好，但不会删除原视频。
- 首次进入 V1.2.0 内置媒体库时按系统提示授予视频只读权限。

### 已知问题

- 发布 APK 为 Android Debug 证书签名，适合测试和侧载；正式批量部署应使用企业固定 Release 证书。
- 验证开发机没有连接真实 Android TV 或 TV 模拟器，真实媒体索引、U 盘、遥控器、解码器和厂商开机策略仍需目标硬件验收。
- Android 10+ 及部分电视厂商可能限制普通第三方应用从开机广播直接拉起界面。

### 备注

- 本地已通过单元测试、Android Lint、Debug APK 构建、清单/权限解析、ZIP 对齐和 v1/v2 签名验证。
- 发布 APK SHA-256：`5d2aeb48d03c1fc34ed4c5985f79527f8380cc3907a564f04165285616cf821d`。

## V1.1.2 - 2026-08-01

- 开发人员：chenwei666
- 版本类型：持续集成修复版本

### 新增功能

- 无应用功能新增。

### 问题修复

- 修复首次 GitHub Actions 成功运行后出现的 Node.js 20 弃用警告。
- 修复自定义 `GRADLE_USER_HOME` 与 `setup-java` 内置 Gradle 缓存不一致导致的缓存路径警告。

### 优化内容

- CI 固定使用 GitHub 托管的 `windows-2025` 镜像。
- 升级到 Node.js 24 运行时的 `actions/checkout@v6`、`actions/setup-java@v5` 和 `actions/upload-artifact@v6`。
- 移除第三方 Android setup Action，改为使用托管镜像的 `ANDROID_HOME` 和显式 `sdkmanager` 安装所需 SDK 包。

### 删除内容

- 删除无效的 `setup-java` Gradle 缓存配置。
- 删除 `android-actions/setup-android@v3` 工作流步骤。

### 修改文件

- 修改 `.github/workflows/android.yml`、版本号、构建脚本、README 和部署说明。
- 新增 `Version/V1.1.2` 完整项目快照。

### 影响模块

- 持续集成、开源发布和版本管理；不改变播放器业务逻辑。

### 数据库变更

- 无数据库；应用不会读取、查询或连接数据库。

### 接口变更

- 无应用接口变更。

### 配置变更

- `versionCode` 从 3 升至 4，`versionName` 从 1.1.1 升至 1.1.2。
- GitHub Actions Runner 从浮动 `windows-latest` 改为 `windows-2025`。

### 兼容性说明

- Android 功能和系统兼容性与 V1.1.1 相同。
- GitHub 托管 Runner 支持新版 Action；自托管 Runner 需满足对应 Node.js 24 Action 的最低 Runner 版本要求。

### 升级方式

- 从 GitHub Release 下载 V1.1.2；相同签名可执行 `adb install -r TVAutoPlayer-v1.1.2-debug.apk`。

### 已知问题

- 发布 APK 仍为 Debug 签名，真实电视验收要求不变。

### 备注

- 本版本用于消除公开 CI 警告并验证可复现构建。

## V1.1.1 - 2026-08-01

- 开发人员：chenwei666
- 版本类型：开源发布版本

### 新增功能

- 新增 MIT 开源许可证。
- 新增贡献指南和安全漏洞报告策略。
- 新增 GitHub Actions，在 `main` 推送及 Pull Request 时自动运行单元测试、Android Lint 和 Debug APK 构建。
- 新增 GitHub Release 下载说明和 CI 状态徽章。

### 问题修复

- 修复开源仓库中 APK 被 `.gitignore` 排除后，README 本地 `dist` 下载路径不可用的问题，改为 GitHub Releases 下载地址。

### 优化内容

- 自动化构建产物通过 GitHub Actions Artifact 提供，发布 APK 通过 GitHub Release 独立托管，避免二进制文件进入 Git 历史。
- 明确开源贡献的 Android 6.0、遥控器、最小权限、隐私和测试要求。

### 删除内容

- 无。

### 修改文件

- 新增 `LICENSE`、`CONTRIBUTING.md`、`SECURITY.md`、`.github/workflows/android.yml`。
- 更新 `README.md`、`CHANGELOG.md`、测试和部署说明、版本号与构建产物名称。
- 新增 `Version/V1.1.1` 完整项目快照。

### 影响模块

- 版本管理、开源合规、持续集成、文档和发布流程；不改变播放器业务逻辑。

### 数据库变更

- 无数据库；应用不会读取、查询或连接数据库。

### 接口变更

- 无应用接口变更。
- 新增 GitHub Actions 工作流接口和 GitHub Release 发布渠道。

### 配置变更

- `versionCode` 从 2 升至 3，`versionName` 从 1.1.0 升至 1.1.1。
- 新增只读 `contents` 权限的 GitHub Actions 工作流。

### 兼容性说明

- Android 功能、最低 Android 6.0（API 23）和目标 Android 16（API 36）与 V1.1.0 相同。
- V1.0.0/V1.1.0 的播放列表和偏好设置可直接保留升级。

### 升级方式

- 从 GitHub Release 下载 V1.1.1；相同签名可执行 `adb install -r TVAutoPlayer-v1.1.1-debug.apk`。
- 不同签名无法覆盖安装，正式部署仍应使用企业自己的固定发布证书。

### 已知问题

- GitHub Release 提供的是 Debug 签名 APK，适合测试和侧载，不应作为企业正式发布签名。
- 真实电视的多选文件器、解码器和厂商开机策略仍需在目标硬件验收。

### 备注

- 源码公开仓库：`https://github.com/chenwei666/TVAutoPlayer`。
- 本项目采用 MIT License。

## V1.1.0 - 2026-08-01

- 开发人员：chenwei666
- 版本类型：功能增强版本

### 新增功能

- 新增系统文件选择器多选，可一次选择 1 个、3 个、4 个、5 个或更多视频。
- 新增有序播放列表，按选择顺序播放，末项结束后自动回到首项循环。
- 新增“追加视频”，保留已有顺序并按 URI 自动去重。
- 新增“重新选择播放列表”和带编号的当前列表展示。
- 新增 `sw600dp`、`sw960dp` 自适应尺寸资源，适配不同电视逻辑宽度与 720p/1080p/4K 输出环境。
- 新增列表顺序、追加去重和部分授权有效策略单元测试。

### 问题修复

- 修复固定 `640dp/760dp` 宽度在小尺寸或特殊缩放电视上可能产生裁切的问题。
- 修复播放列表中单个文件被移动、删除或授权失效时导致整个列表不可播放的问题；现仅跳过失效项。
- 修复单个视频解码失败后无法继续播放后续视频的问题；现会尝试下一项并在整轮失败后停止重试。
- 替换或清空播放列表时释放已移除文件的长期 URI 授权，避免授权残留。

### 优化内容

- 新增不可变 `PlaylistItem` 和纯逻辑 `PlaylistEditor`，将顺序与去重规则从界面层分离。
- Media3 循环模式由 `REPEAT_MODE_ONE` 调整为 `REPEAT_MODE_ALL`。
- 设置页使用可滚动、自适应边距与尺寸资源，支持多行播放列表展示和遥控器焦点操作。
- 开机判断改为“至少一个视频可读即可启动”，提高 U 盘文件部分变化时的容错能力。

### 删除内容

- 无。原单视频功能保留，并自然兼容为仅含一项的播放列表。

### 修改文件

- 新增 `PlaylistItem.java`、`PlaylistEditor.java`、`PlaylistBehaviorTest.java`。
- 修改播放配置、持久化、URI 访问、播放器、设置页、开机接收器和布局资源。
- 新增基础、`sw600dp`、`sw960dp` 三档尺寸资源。
- 更新 `README.md`、架构、测试、部署说明、构建脚本和版本号。
- 新增 `Version/V1.1.0` 完整项目快照及 V1.1.0 Debug APK。

### 影响模块

- 播放模块、播放列表配置、文件授权、设置模块、开机启动、电视 UI、测试与发布流程。

### 数据库变更

- 无数据库；应用不会读取、查询或连接数据库。

### 接口变更

- 无网络 API 或外部业务接口。
- 内部 `PlaybackConfiguration` 从单 URI 扩展为有序 `PlaylistItem` 列表；保留旧单 URI 构造方式用于升级兼容。
- `PlaybackPolicy` 新增按可读视频数量决策的重载方法。

### 配置变更

- SharedPreferences 新增 `playlist_json`，保存文件 URI、显示名称及顺序。
- 首次加载 V1.0.0 配置时自动把 `video_uri/display_name` 迁移到 `playlist_json`。
- `versionCode` 从 1 升至 2，`versionName` 从 1.0.0 升至 1.1.0。

### 兼容性说明

- 最低 Android 6.0（API 23）和目标 Android 16（API 36）不变。
- V1.0.0 已选视频、声音、画面和开机设置可保留升级。
- 多选能力取决于电视安装的系统文件选择器；不支持多选的提供器仍可逐次使用“追加视频”。
- 视频编码解码能力仍由电视芯片决定，推荐 MP4/H.264/AAC。

### 升级方式

- 使用相同 applicationId 和相同签名执行 `adb install -r dist/TVAutoPlayer-v1.1.0-debug.apk`。
- Debug/Release 或不同证书之间不能直接覆盖安装；卸载重装会清除播放列表设置和 URI 授权。

### 已知问题

- Android 10+ 及部分电视厂商可能限制普通第三方应用从开机广播直接启动界面。
- 当前开发机没有连接真实 Android TV/电视盒子，需在目标设备验证多选文件器、遥控器、实际编码、分辨率及冷启动策略。
- 不提供拖拽排序；播放顺序遵循系统文件选择器返回顺序，如需调整可重新选择列表。

### 备注

- 可安装交付物为 Debug 签名 APK；正式批量部署应使用企业发布证书签名。

## V1.0.0 - 2026-08-01

- 开发人员：chenwei666
- 版本类型：首次正式功能版本

### 新增功能

- 新增 Android TV 全屏视频播放器，打开应用后自动播放并无限循环。
- 新增首次启动视频选择引导与系统文件选择器集成。
- 新增单文件 URI、文件名、声音、画面比例和开机启动设置持久化。
- 新增跨设备重启的只读 URI 授权保存与旧授权释放。
- 新增开机完成及 Quick Boot 广播接收器。
- 新增遥控器返回、菜单、设置键进入设置的操作路径。
- 新增声音开关、完整显示、裁切铺满、清除视频功能。
- 新增文件授权失效、文件丢失、文件选择器不可用、播放失败等异常提示。
- 新增播放失败重试、保持屏幕常亮和沉浸式全屏。
- 新增电视启动图标、Banner、中文界面和 D-pad 聚焦状态。

### 问题修复

- 处理 Windows 中文项目路径导致 Android Gradle Plugin 拒绝构建的问题。
- 通过 `C:\tmp` 英文临时构建镜像和 ASCII 路径硬校验规避 Gradle Test Worker 在中文路径下无法装载测试类的问题。
- 修复 Android 16 预测返回模式下传统 `onBackPressed()` 不再可靠的问题。
- 修复文件长期授权参数可能包含非法标志的问题。
- 修复大尺寸 TV Banner、布局过度绘制、RTL 内边距和备份规则的 Lint 问题。
- 防止用户取消首次选片后文件选择器无限重复弹出。

### 优化内容

- 使用纯 Java 播放策略隔离 Android 框架，提升可测试性。
- 配置类保持不可变；决策逻辑无副作用。
- 使用 Media3 ExoPlayer 1.10.1 提升容器与编码兼容性。
- 只保存 URI 和偏好设置，不复制大视频文件。
- Release 构建开启 R8 压缩与无用资源移除。
- Gradle Wrapper 增加官方分发包 SHA-256 校验。

### 删除内容

- 无。本版本为首次发布。

### 修改文件

- 新增根级 Gradle 工程配置、Wrapper、README、CHANGELOG 与构建脚本。
- 新增 `app` Android 应用模块及全部源码、资源与测试。
- 新增 `docs/architecture.md`、`docs/testing.md`、`docs/deployment.md`。
- 新增 `Version/V1.0.0` 完整项目快照。

### 影响模块

- 播放模块、文件授权模块、设置模块、开机启动模块、电视 UI、构建与发布流程。

### 数据库变更

- 无数据库；应用不会读取、查询或连接数据库。

### 接口变更

- 无网络 API 或外部业务接口。
- Android 组件新增：`MainActivity`、`SettingsActivity`、`BootCompletedReceiver`。

### 配置变更

- 新增本地 SharedPreferences：视频 URI、显示名、开机启动、声音、画面模式、提示状态。
- 新增 `RECEIVE_BOOT_COMPLETED` 权限。
- 新增 Android 12+ 数据提取排除规则。

### 兼容性说明

- 最低 Android 6.0（API 23），目标 Android 16（API 36）。
- 支持 Android TV、Google TV 及大多数基于 Android 的电视盒子。
- 厂商后台限制可能阻止开机广播拉起界面，需要在系统设置中允许自启动。
- 实际编码解码能力由电视芯片决定，推荐 MP4/H.264/AAC。

### 升级方式

- V1.0.0 为初始版本，直接安装 APK。
- 后续使用相同 applicationId 和发布证书时可通过 `adb install -r` 覆盖升级并保留设置。
- Debug 证书不同的 APK 无法覆盖安装，需先卸载旧 Debug 版；卸载会清除视频选择设置。

### 已知问题

- Android 10+ 及部分电视厂商可能限制普通第三方应用从开机广播直接启动界面。
- 当前没有自动化电视模拟器或真实电视 UI 测试，需在目标电视上完成遥控器、USB 文件选择、冷启动和实际编码播放验收。
- 当前版本为单视频循环，不包含多视频播放列表和远程内容下发。

### 备注

- 可安装交付物为 Debug 签名 APK；正式批量部署应使用企业发布证书签名。
