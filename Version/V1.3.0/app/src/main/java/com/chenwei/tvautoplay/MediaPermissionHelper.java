package com.chenwei.tvautoplay;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/** Version-aware runtime permissions for the app's built-in image/video library. */
public final class MediaPermissionHelper {
    private MediaPermissionHelper() {
    }

    public static String[] requiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            };
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            };
        }
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    public static boolean hasReadAccess(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return isGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
                    || isGranted(context, Manifest.permission.READ_MEDIA_VIDEO)
                    || isGranted(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return isGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
                    || isGranted(context, Manifest.permission.READ_MEDIA_VIDEO);
        }
        return isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private static boolean isGranted(Context context, String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }
}
