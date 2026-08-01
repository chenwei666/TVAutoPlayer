package com.chenwei.tvautoplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pure ordered-playlist operations used by the settings and persistence layers. */
public final class PlaylistEditor {
    private PlaylistEditor() {
    }

    public static List<PlaylistItem> replace(List<PlaylistItem> selected) {
        return merge(Collections.emptyList(), selected);
    }

    public static List<PlaylistItem> append(
            List<PlaylistItem> existing,
            List<PlaylistItem> selected
    ) {
        return merge(existing, selected);
    }

    private static List<PlaylistItem> merge(
            List<PlaylistItem> existing,
            List<PlaylistItem> selected
    ) {
        Map<String, PlaylistItem> itemsByUri = new LinkedHashMap<>();
        addUnique(itemsByUri, existing);
        addUnique(itemsByUri, selected);
        return Collections.unmodifiableList(new ArrayList<>(itemsByUri.values()));
    }

    private static void addUnique(
            Map<String, PlaylistItem> itemsByUri,
            List<PlaylistItem> items
    ) {
        if (items == null) {
            return;
        }
        for (PlaylistItem item : items) {
            if (item != null && !itemsByUri.containsKey(item.uri())) {
                itemsByUri.put(item.uri(), item);
            }
        }
    }
}
