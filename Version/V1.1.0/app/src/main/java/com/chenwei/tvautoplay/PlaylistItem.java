package com.chenwei.tvautoplay;

import java.util.Objects;

/** Immutable reference to one user-selected video document. */
public final class PlaylistItem {
    private final String uri;
    private final String displayName;

    public PlaylistItem(String uri, String displayName) {
        this.uri = requireValue(uri, "uri");
        String normalizedName = normalize(displayName);
        this.displayName = normalizedName == null ? this.uri : normalizedName;
    }

    public String uri() {
        return uri;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PlaylistItem)) {
            return false;
        }
        PlaylistItem item = (PlaylistItem) other;
        return uri.equals(item.uri) && displayName.equals(item.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, displayName);
    }

    private static String requireValue(String value, String fieldName) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
