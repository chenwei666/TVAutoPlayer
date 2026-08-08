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

/** Reads supported system-indexed images and videos from shared storage volumes. */
public final class MediaStoreMediaRepository {
    private static final String TAG = "MediaStoreMedia";

    public List<PlaylistItem> queryMedia(Context context) {
        ContentResolver resolver = context.getContentResolver();
        List<PlaylistItem> discovered = new ArrayList<>();
        for (Uri collection : mediaCollections(context)) {
            queryCollection(resolver, collection, discovered);
        }
        return MediaCatalog.normalize(discovered);
    }

    private Set<Uri> mediaCollections(Context context) {
        Set<Uri> collections = new LinkedHashSet<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            for (String volumeName : MediaStore.getExternalVolumeNames(context)) {
                collections.add(MediaStore.Video.Media.getContentUri(volumeName));
                collections.add(MediaStore.Images.Media.getContentUri(volumeName));
            }
        } else {
            collections.add(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            collections.add(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        return collections;
    }

    private void queryCollection(
            ContentResolver resolver,
            Uri collection,
            List<PlaylistItem> destination
    ) {
        String[] projection = new String[]{
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE
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
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
            int mimeColumn = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
            int sizeColumn = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);
            while (cursor.moveToNext()) {
                if (sizeColumn >= 0 && cursor.getLong(sizeColumn) <= 0) {
                    continue;
                }
                String displayName = cursor.getString(nameColumn);
                String mimeType = mimeColumn < 0 ? null : cursor.getString(mimeColumn);
                MediaKind kind = MediaTypeDetector.detect(mimeType, displayName);
                if (kind == null) {
                    continue;
                }
                long id = cursor.getLong(idColumn);
                Uri contentUri = ContentUris.withAppendedId(collection, id);
                destination.add(new PlaylistItem(contentUri.toString(), displayName, kind));
            }
        } catch (RuntimeException exception) {
            // A removable volume may disappear during a scan. Other volumes remain usable.
            Log.w(TAG, "Unable to scan one media collection");
        }
    }
}
