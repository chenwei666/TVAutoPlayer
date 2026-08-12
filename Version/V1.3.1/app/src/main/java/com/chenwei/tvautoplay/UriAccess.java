package com.chenwei.tvautoplay;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Safe access helpers for documents explicitly selected by the user. */
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
            Log.w(TAG, "Selected media is no longer readable", exception);
            return false;
        } catch (IOException exception) {
            Log.w(TAG, "Unable to close selected media descriptor", exception);
            return false;
        }
    }

    public static List<PlaylistItem> readableItems(
            Context context,
            List<PlaylistItem> playlist
    ) {
        if (playlist == null || playlist.isEmpty()) {
            return Collections.emptyList();
        }
        List<PlaylistItem> readable = new ArrayList<>();
        for (PlaylistItem item : playlist) {
            if (item != null && canRead(context, item.uri())) {
                readable.add(item);
            }
        }
        return Collections.unmodifiableList(readable);
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
            Log.w(TAG, "Unable to read selected media display name", exception);
        }

        String lastSegment = uri.getLastPathSegment();
        return lastSegment == null || lastSegment.trim().isEmpty()
                ? context.getString(R.string.unknown_video_name)
                : lastSegment;
    }

    public static MediaKind mediaKind(Context context, Uri uri, String displayName) {
        String mimeType = null;
        try {
            mimeType = context.getContentResolver().getType(uri);
        } catch (RuntimeException ignored) {
            // A provider may not expose MIME metadata; the filename fallback still works.
        }
        return MediaTypeDetector.detect(mimeType, displayName);
    }

    public static void releaseRemovedPersistedReadPermissions(
            Context context,
            List<PlaylistItem> previousItems,
            List<PlaylistItem> updatedItems
    ) {
        Set<String> retainedUris = new LinkedHashSet<>();
        for (PlaylistItem item : updatedItems) {
            retainedUris.add(item.uri());
        }
        for (PlaylistItem item : previousItems) {
            if (!retainedUris.contains(item.uri())) {
                releasePersistedReadPermission(context, item.uri());
            }
        }
    }

    public static void releasePersistedReadPermission(Context context, String uriValue) {
        if (uriValue == null) {
            return;
        }
        Uri uri = Uri.parse(uriValue);
        List<UriPermission> grants = context.getContentResolver().getPersistedUriPermissions();
        for (UriPermission grant : grants) {
            if (grant.getUri().equals(uri) && grant.isReadPermission()) {
                try {
                    context.getContentResolver().releasePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (SecurityException ignored) {
                    // The provider may already have revoked the grant.
                }
                return;
            }
        }
    }
}
