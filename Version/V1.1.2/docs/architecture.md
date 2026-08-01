# 架构说明

## 设计目标

应用采用单模块、无数据库、无网络的最小架构，优先保证电视端稳定性和可维护性。播放策略与 Android 框架隔离，界面层只负责设备交互和播放器生命周期。

## 模块职责

- `PlaylistItem`：不可变视频引用值对象。
- `PlaylistEditor`：保持顺序的播放列表替换、追加和 URI 去重逻辑。
- `PlaybackConfiguration`：包含有序播放列表的不可变配置值对象。
- `PlaybackPolicy`：决定当前应播放、选片还是提示授权失效；同时决定开机时是否应启动。
- `PlaybackConfigStore`：使用私有 SharedPreferences 保存少量非敏感设置。
- `UriAccess`：只对用户明确选择的 URI 检查可读性、过滤可播放项并获取显示名称。
- `MainActivity`：创建/释放 ExoPlayer、有序列表循环、全屏与单项失败跳过。
- `SettingsActivity`：系统多选文件、替换/追加/清空列表、声音和画面设置。
- `BootCompletedReceiver`：在满足“列表至少一项可读、开机启动开启”时尝试拉起主界面。

## 数据流

```text
系统多选文件器 -> 持久 URI 授权 -> PlaylistEditor -> PlaybackConfigStore
                                                    |
启动/返回前台 -> UriAccess 过滤可读项 -> PlaybackPolicy -> MainActivity/ExoPlayer
                                                    |
开机广播 -------------------------------------------> BootCompletedReceiver
```

## 安全与隐私

- 不申请 `READ_MEDIA_VIDEO` 或旧版整盘存储权限。
- 只使用用户在系统选择器中明确授权的 `content://` URI 列表。
- 不记录 URI、文件名或媒体内容到业务日志。
- 不联网，Manifest 明确禁止明文网络流量。
- 禁止系统云备份和设备迁移复制应用偏好，避免文件路径信息离开设备。
- 没有数据库、账号、Token、API Key 或后台服务。

## 生命周期

- `onStart` 检查配置和授权，满足条件后创建播放器并从头播放。
- `onStop` 立即释放播放器，避免离开应用后继续占用解码器和音频焦点。
- `REPEAT_MODE_ALL` 负责按选择顺序循环完整播放列表。
- 单个文件授权失效时跳过该项；全部失效时转入重新选择流程。
- 单个媒体解码失败时尝试下一项；一轮全部失败后显示错误面板，避免无限错误循环。

## 配置迁移与分辨率适配

- V1.1.0 首次读取配置时，会把 V1.0.0 的单 URI 自动迁移为一项播放列表，不需要用户重选。
- 播放列表以有序 JSON 存入应用私有 SharedPreferences，不使用数据库。
- 页面宽度不再硬编码为固定 `640dp/760dp`；基础资源和 `sw600dp`、`sw960dp` 尺寸资源会按设备逻辑宽度调整边距、内边距、按钮和文字。
- 视频内容继续由 PlayerView 按“完整显示”或“裁切铺满”适配实际输出分辨率和宽高比。

## 开机启动边界

Android 10+ 对后台启动 Activity 有平台限制，厂商还可能增加电源管理策略。应用只使用标准广播和公开 API，不申请悬浮窗等高风险权限，也不使用 Android 15 已禁止从开机广播启动的媒体播放前台服务。需要强保证时应采用设备所有者/Kiosk 管理方案。
