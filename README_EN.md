# TV Auto Player

[![Android CI](https://github.com/chenwei666/TVAutoPlayer/actions/workflows/android.yml/badge.svg)](https://github.com/chenwei666/TVAutoPlayer/actions/workflows/android.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[中文说明](README.md) | English

Developer and maintainer: [chenwei666](https://github.com/chenwei666)

TV Auto Player is an open-source, offline digital-signage player for Android TV and TV boxes. V1.4.0 automatically follows the TV system language in Chinese or English. It supports mixed image and video playlists, configurable image durations, continuous looping, resolution-aware layouts, built-in media discovery, and optional playback after boot.

The app scans internal storage and mounted USB drives without requiring a third-party file picker. Playlists and playback preferences stay on the device. The app does not access the network, upload media, connect to a database, or collect user data.

## Features

- Automatically displays Chinese or English based on the TV system language.
- Opens the built-in media library on first launch.
- Selects multiple images and videos from internal storage or mounted USB drives.
- Plays mixed media in selection order and loops continuously.
- Displays still images for 5, 10, 15, 30, or 60 seconds; videos play to completion.
- Remembers the playlist, image duration, sound, display mode, and boot preference.
- Works without a system file picker or third-party file manager.
- Requests only the media read permissions required by the Android version.
- Supports D-pad, OK, Back, Menu, and Settings remote-control keys.
- Adapts layouts for common 720p, 1080p, and 4K TV resolutions.
- Supports Fit and Crop-to-fill display modes.
- Skips missing or unreadable media without crashing the full playlist.
- Can request playback after TV boot, subject to Android and vendor restrictions.
- Keeps the screen awake and uses immersive full-screen playback.
- Does not request `MANAGE_EXTERNAL_STORAGE` and never modifies original media files.

## Install

Download the verified APK from [TVAutoPlayer v1.4.0](https://github.com/chenwei666/TVAutoPlayer/releases/tag/v1.4.0).

You can copy the APK to a USB drive and install it with the TV file manager, or install it with ADB:

```powershell
adb install -r .\TVAutoPlayer-v1.4.0-debug.apk
```

First-run setup:

1. Open **TV Auto Player**.
2. Grant read access to photos and videos when Android asks.
3. Select images and videos from internal storage or a mounted USB drive in playback order.
4. Select **Save selected media and play**.
5. Press Back, Menu, or Settings on the remote during playback to reopen settings.
6. If no media appears, place videos in `Movies/TVAutoPlay` and images in `Pictures/TVAutoPlay`, reconnect the USB drive, wait briefly, and rescan.
7. Restart the TV and verify whether the vendor firmware allows auto-start.

> The downloadable APK uses the Android debug certificate and is intended for direct sideload testing in showrooms, stores, offices, and private networks. Use a stable enterprise release certificate before managed production deployment.

## Language Behavior

- English system locale: English interface.
- Chinese system locale: Chinese interface.
- Other locales: the app falls back to Chinese.
- Changing the TV language takes effect after Android recreates or restarts the app.

## Boot Auto-play

The app listens for the standard boot broadcast and common Quick Boot broadcasts used by some TV boxes. Android 10 and later restrict background activity launches, and TV vendors may impose additional auto-start controls. After installation, allow this app to auto-start and run in the background in the TV system settings.

For fully unattended deployments, use an enterprise Device Owner/Kiosk configuration or ask the hardware vendor to allowlist the app.

## Media Compatibility

The player uses Media3 ExoPlayer 1.10.1. The built-in library lists media recognized by Android MediaStore and supported by the app.

- Images: JPEG/JPG, PNG, WebP, BMP, HEIF/HEIC; AVIF requires Android 14+; animated GIF is not supported.
- Video containers: MP4, MKV, WebM, MPEG-TS, and other common containers supported by the device.
- Recommended video combination: MP4 + H.264/AVC + AAC.
- H.265/HEVC, AV1, 4K, high-bitrate, and 10-bit playback depend on TV hardware decoders.
- Older AVI content should be transcoded to MP4 for more reliable playback.

## Build

Requirements:

- JDK 17
- Android SDK Platform 36
- Android SDK Build Tools 36.0.0
- Android Gradle Plugin 9.2.1
- Gradle 9.4.1 via the included wrapper
- Minimum Android 6.0 / API 23
- Target Android 16 / API 36

Run the Windows quality gate and debug build:

```powershell
.\scripts\build.ps1
```

The script mirrors source files to the ASCII-only path `C:\tmp\TVAutoPlayer-build`, runs unit tests and Android Lint, builds the debug APK, and writes the APK and SHA-256 file to `dist`.

## Privacy and Permissions

The app requests `RECEIVE_BOOT_COMPLETED` and version-appropriate read-only media permissions. It reads only system-indexed media through MediaStore, does not access the network, does not connect to a database, and does not log media names or URIs to business logs. Uninstalling the app removes its settings but does not delete the original images or videos.

## Documentation

- [Architecture](docs/architecture.md)
- [Testing](docs/testing.md)
- [Deployment](docs/deployment.md)
- [Changelog](CHANGELOG.md)
- [Contributing](CONTRIBUTING.md)
- [Security](SECURITY.md)

## License

Licensed under the [MIT License](LICENSE). Forks, improvements, and compliant personal or commercial use are welcome.
