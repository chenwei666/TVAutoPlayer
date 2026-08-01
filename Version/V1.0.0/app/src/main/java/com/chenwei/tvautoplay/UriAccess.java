package com.chenwei.tvautoplay;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;

/** Safe access helpers for the single document explicitly selected by the user. */
public final class UriAccess {
    private static final String TAG = "UriAccess";

    private UriAccess() {
    }

    public static boolean canRead(Context context, String uriValue) {
        if (uriValue == null || uriValue.trim().isEmpty()) {
            return false;
        }
        Uri uri = Uri.parse(uriValue);
        try (ParcelFileDescriptor descriptor = context.getContentResolver()
                .openFileDescriptor(uri, "r")) {
            return descriptor != null;
        } catch (SecurityException | FileNotFoundException exception) {
            Log.w(TAG, "Selected video is no longer readable", exception);
            return false;
        } catch (IOException exception) {
            Log.w(TAG, "Unable to close selected video descriptor", exception);
            return false;
        }
    }

    public static String displayName(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(
                uri,
                new String[]{OpenableColumns.DISPLAY_NAME},
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (column >= 0) {
                    String name = cursor.getString(column);
                    if (name != null && !name.trim().isEmpty()) {
                        return name;
                    }
                }
            }
        } catch (RuntimeException exception) {
            Log.w(TAG, "Unable to read selected video display name", exception);
        }

        String lastSegment = uri.getLastPathSegment();
        return lastSegment == null || lastSegment.trim().isEmpty()
                ? context.getString(R.string.unknown_video_name)
                : lastSegment;
    }
}
