# 架构说明

## 设计目标

应用采用单模块、无数据库、无网络的最小架构，优先保证电视端稳定性和可维护性。播放策略与 Android 框架隔离，界面层只负责设备交互和播放器生命周期。

## 模块职责

- `PlaylistItem`：包含 URI、显示名称和 `MediaKind` 的不可变图片/视频引用值对象；旧构造默认视频。
- `PlaylistEditor`：保持顺序的播放列表替换、追加和 URI 去重逻辑。
- `MediaTypeDetector`：按 MIME 和扩展名识别受支持的图片/视频，并在入列表前过滤 GIF 等不支持格式。
- `ImageDuration`：5/10/15/30/60 秒图片停留时间及安全默认值。
- `MediaCatalog`：对扫描结果做 URI 去重和稳定排序的纯逻辑模块。
- `MediaPermissionHelper`：按 Android 版本选择最小照片/视频读取权限，并识别全部/部分媒体授权。
- `MediaStoreMediaRepository`：查询系统 MediaStore 中全部已挂载共享存储卷的图片和视频。
- `PlaybackConfiguration`：包含有序播放列表的不可变配置值对象。
- `PlaybackPolicy`：决定当前应播放、选片还是提示授权失效；同时决定开机时是否应启动。
- `PlaybackConfigStore`：使用私有 SharedPreferences 保存少量非敏感设置。
- `UriAccess`：检查 URI 可读性、过滤可播放项、获取显示名称，并释放不再使用的 SAF 长期授权。
- `PlaybackMediaItemFactory`：视频按原时长创建 MediaItem，图片附加用户设置的 `imageDurationMs`。
- `MainActivity`：创建/释放 ExoPlayer、图片/视频混合循环、全屏与单项失败跳过。
- `MediaLibraryActivity`：请求视频权限、异步扫描、D-pad 多选、按勾选顺序替换或追加播放列表。
- `SettingsActivity`：进入内置媒体库、按能力显示可选系统文件选择器，并管理列表、声音和画面设置。
- `BootCompletedReceiver`：在满足“列表至少一项可读、开机启动开启”时尝试拉起主界面。
- `values/strings.xml` 与 `values-en/strings.xml`：完整中文/英文界面资源；Android 按电视系统语言自动选择，其他语言回退到默认中文。

## 数据流

```text
照片/视频只读权限 -> MediaStore(电视存储/已挂载U盘) -> MediaTypeDetector
                                                               |
                                                        MediaCatalog -> MediaLibraryActivity
                                                            |
可选系统文件选择器 -> 持久 URI 授权 -----------------> PlaylistEditor -> PlaybackConfigStore
                                                                            |
启动/返回前台 -> UriAccess -> PlaybackPolicy -> PlaybackMediaItemFactory -> MainActivity/ExoPlayer
                                                                            |
开机广播 -------------------------------------------------------------------> BootCompletedReceiver
```

## 安全与隐私

- Android 13+ 申请 `READ_MEDIA_IMAGES` 和 `READ_MEDIA_VIDEO`；Android 14+ 同时声明并处理 `READ_MEDIA_VISUAL_USER_SELECTED`；Android 12 及以下使用仅到 API 32 生效的 `READ_EXTERNAL_STORAGE`。
- 不申请 `MANAGE_EXTERNAL_STORAGE`，不按原始文件路径遍历整盘，只查询系统已索引的视频记录。
- Android 14+ 用户可授予全部视频或仅选择部分视频；应用以实际获得的权限和 MediaStore 可见结果为准。
- 系统文件选择器仍作为可选兼容入口，并继续使用持久 `content://` URI 授权。
- 不记录 URI、文件名或媒体内容到业务日志。
- 不联网，Manifest 明确禁止明文网络流量。
- 禁止系统云备份和设备迁移复制应用偏好，避免文件路径信息离开设备。
- 没有数据库、账号、Token、API Key 或后台服务。

## 扫描与选择边界

- API 29+ 通过 `MediaStore.getExternalVolumeNames` 枚举主共享存储和已挂载可移动卷；API 23～28 使用外部视频集合。
- 扫描在单线程执行器中进行，Activity 销毁时立即取消，避免阻塞电视主线程或泄漏界面。
- U 盘拔出或单卷查询异常只跳过该卷，其余卷仍可使用；日志不包含卷名、文件名或 URI。
- 目录或文件若尚未被电视媒体扫描器索引，不会立即出现在列表中。推荐放在 `Movies/TVAutoPlay`，重新插入 U 盘并稍等后重新扫描。
- MediaStore 只负责发现文件；`MediaTypeDetector` 再过滤应用明确支持的类型，最终解码能力仍由 Media3、Android 版本与电视硬件决定。

## 生命周期

- `onStart` 检查配置和授权，满足条件后创建播放器并从头播放。
- `onStop` 立即释放播放器，避免离开应用后继续占用解码器和音频焦点。
- `REPEAT_MODE_ALL` 负责按选择顺序循环完整播放列表。
- 图片 MediaItem 使用配置的 `imageDurationMs` 自动前进；视频沿用媒体自身时长，二者可混合排列。
- 单个文件授权失效时跳过该项；全部失效时转入重新选择流程。
- 单个媒体解码失败时尝试下一项；一轮全部失败后显示错误面板，避免无限错误循环。

## 配置迁移与分辨率适配

- V1.1.0 首次读取配置时，会把 V1.0.0 的单 URI 自动迁移为一项播放列表；升级到 V1.3.0 时，缺少 `kind` 的旧播放项默认迁移为视频，图片时长默认 10 秒。
- 播放列表 JSON 新增 `kind` 字段；SharedPreferences 新增 `image_duration`，不修改旧键，不需要数据库迁移。
- 播放列表以有序 JSON 存入应用私有 SharedPreferences，不使用数据库。
- 页面宽度不再硬编码为固定 `640dp/760dp`；基础资源和 `sw600dp`、`sw960dp` 尺寸资源会按设备逻辑宽度调整边距、内边距、按钮和文字。
- 视频内容继续由 PlayerView 按“完整显示”或“裁切铺满”适配实际输出分辨率和宽高比。

## 开机启动边界

Android 10+ 对后台启动 Activity 有平台限制，厂商还可能增加电源管理策略。应用只使用标准广播和公开 API，不申请悬浮窗等高风险权限，也不使用 Android 15 已禁止从开机广播启动的媒体播放前台服务。需要强保证时应采用设备所有者/Kiosk 管理方案。
