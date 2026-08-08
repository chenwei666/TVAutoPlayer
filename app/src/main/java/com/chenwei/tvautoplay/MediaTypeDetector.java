package com.chenwei.tvautoplay;

import java.util.Locale;

/** Pure MIME/name classification for formats supported by the local playback pipeline. */
public final class MediaTypeDetector {
    private MediaTypeDetector() {
    }

    public static MediaKind detect(String mimeType, String displayName) {
        String normalizedMime = normalize(mimeType);
        if (normalizedMime != null) {
            if (normalizedMime.startsWith("video/")) {
                return MediaKind.VIDEO;
            }
            if (isSupportedVideoApplicationMimeType(normalizedMime)) {
                return MediaKind.VIDEO;
            }
            if (isSupportedImageMimeType(normalizedMime)) {
                return MediaKind.IMAGE;
            }
            if (normalizedMime.startsWith("image/")) {
                return null;
            }
        }

        String extension = extension(displayName);
        if (isSupportedImageExtension(extension)) {
            return MediaKind.IMAGE;
        }
        if (isSupportedVideoExtension(extension)) {
            return MediaKind.VIDEO;
        }
        return null;
    }

    private static boolean isSupportedImageMimeType(String mimeType) {
        switch (mimeType) {
            case "image/bmp":
            case "image/x-ms-bmp":
            case "image/jpeg":
            case "image/pjpeg":
            case "image/png":
            case "image/webp":
            case "image/heif":
            case "image/heic":
            case "image/heif-sequence":
            case "image/heic-sequence":
            case "image/avif":
                return true;
            default:
                return false;
        }
    }

    private static boolean isSupportedVideoApplicationMimeType(String mimeType) {
        switch (mimeType) {
            case "application/x-matroska":
            case "application/vnd.apple.mpegurl":
            case "application/x-mpegurl":
            case "application/mpegurl":
            case "application/mp2t":
                return true;
            default:
                return false;
        }
    }

    private static boolean isSupportedImageExtension(String extension) {
        switch (extension) {
            case "bmp":
            case "jpg":
            case "jpeg":
            case "jfif":
            case "png":
            case "webp":
            case "heif":
            case "heic":
            case "avif":
                return true;
            default:
                return false;
        }
    }

    private static boolean isSupportedVideoExtension(String extension) {
        switch (extension) {
            case "mp4":
            case "m4v":
            case "mkv":
            case "webm":
            case "ts":
            case "m2ts":
            case "mov":
            case "avi":
            case "mpg":
            case "mpeg":
            case "m3u8":
                return true;
            default:
                return false;
        }
    }

    private static String extension(String displayName) {
        String normalizedName = normalize(displayName);
        if (normalizedName == null) {
            return "";
        }
        int dot = normalizedName.lastIndexOf('.');
        return dot < 0 || dot == normalizedName.length() - 1
                ? ""
                : normalizedName.substring(dot + 1);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.US);
        return normalized.isEmpty() ? null : normalized;
    }
}
