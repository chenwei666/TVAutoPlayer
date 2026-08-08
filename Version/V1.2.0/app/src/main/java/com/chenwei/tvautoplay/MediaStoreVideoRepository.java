package com.chenwei.tvautoplay;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Reads system-indexed local videos without relying on an external file-picker app. */
public final class MediaStoreVideoRepository {
    private static final String TAG = "MediaStoreVideos";

    public List<PlaylistItem> queryVideos(Context context) {
        ContentResolver resolver = context.getContentResolver();
        List<PlaylistItem> discovered = new ArrayList<>();
        for (Uri collection : videoCollections(context)) {
            queryCollection(resolver, collection, discovered);
        }
        return MediaCatalog.normalize(discovered);
    }

    private Set<Uri> videoCollections(Context context) {
        Set<Uri> collections = new LinkedHashSet<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            for (String volumeName : MediaStore.getExternalVolumeNames(context)) {
                collections.add(MediaStore.Video.Media.getContentUri(volumeName));
            }
        } else {
            collections.add(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        }
        return collections;
    }

    private void queryCollection(
            ContentResolver resolver,
            Uri collection,
            List<PlaylistItem> destination
    ) {
        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE
        };
        try (Cursor cursor = resolver.query(
                collection,
                projection,
                null,
                null,
                null
        )) {
            if (cursor == null) {
                return;
            }
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int sizeColumn = cursor.getColumnIndex(MediaStore.Video.Media.SIZE);
            while (cursor.moveToNext()) {
                if (sizeColumn >= 0 && cursor.getLong(sizeColumn) <= 0) {
                    continue;
                }
                long id = cursor.getLong(idColumn);
                Uri contentUri = ContentUris.withAppendedId(collection, id);
                String displayName = cursor.getString(nameColumn);
                destination.add(new PlaylistItem(contentUri.toString(), displayName));
            }
        } catch (RuntimeException exception) {
            // One removable volume may disappear during a scan. Other mounted volumes remain usable.
            Log.w(TAG, "Unable to scan one media volume");
        }
    }
}
