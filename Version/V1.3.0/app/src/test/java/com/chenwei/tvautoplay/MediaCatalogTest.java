package com.chenwei.tvautoplay;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MediaCatalogTest {
    @Test
    public void normalizeSortsNamesAndRemovesDuplicateUris() {
        List<PlaylistItem> normalized = MediaCatalog.normalize(Arrays.asList(
                item("content://video/2", "Zulu.mkv"),
                item("content://video/1", "alpha.mp4"),
                item("content://video/1", "duplicate-name.mp4"),
                item("content://video/3", "Beta.webm")
        ));

        assertEquals(3, normalized.size());
        assertEquals("alpha.mp4", normalized.get(0).displayName());
        assertEquals("Beta.webm", normalized.get(1).displayName());
        assertEquals("Zulu.mkv", normalized.get(2).displayName());
    }

    @Test
    public void normalizeKeepsImageKind() {
        PlaylistItem image = new PlaylistItem(
                "content://image/1",
                "poster.png",
                MediaKind.IMAGE
        );

        assertEquals(MediaKind.IMAGE, MediaCatalog.normalize(
                Collections.singletonList(image)
        ).get(0).kind());
    }

    @Test
    public void normalizeHandlesNullAndEmptyCatalogs() {
        assertEquals(Collections.emptyList(), MediaCatalog.normalize(null));
        assertEquals(Collections.emptyList(), MediaCatalog.normalize(Collections.emptyList()));
    }

    private PlaylistItem item(String uri, String displayName) {
        return new PlaylistItem(uri, displayName);
    }
}
