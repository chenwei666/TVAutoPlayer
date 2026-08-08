package com.chenwei.tvautoplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pure catalog normalization shared by MediaStore scanning and unit tests. */
public final class MediaCatalog {
    private MediaCatalog() {
    }

    public static List<PlaylistItem> normalize(List<PlaylistItem> discoveredItems) {
        if (discoveredItems == null || discoveredItems.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, PlaylistItem> uniqueByUri = new LinkedHashMap<>();
        for (PlaylistItem item : discoveredItems) {
            if (item != null && !uniqueByUri.containsKey(item.uri())) {
                uniqueByUri.put(item.uri(), item);
            }
        }

        List<PlaylistItem> normalized = new ArrayList<>(uniqueByUri.values());
        Collections.sort(normalized, new Comparator<PlaylistItem>() {
            @Override
            public int compare(PlaylistItem left, PlaylistItem right) {
                int byNameIgnoringCase = String.CASE_INSENSITIVE_ORDER.compare(
                        left.displayName(), right.displayName()
                );
                if (byNameIgnoringCase != 0) {
                    return byNameIgnoringCase;
                }

                int byName = left.displayName().compareTo(right.displayName());
                if (byName != 0) {
                    return byName;
                }
                return left.uri().compareTo(right.uri());
            }
        });
        return Collections.unmodifiableList(normalized);
    }
}
