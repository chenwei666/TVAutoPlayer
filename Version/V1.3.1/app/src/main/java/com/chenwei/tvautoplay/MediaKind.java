package com.chenwei.tvautoplay;

/** Supported playlist item categories. Unknown legacy values remain videos for compatibility. */
public enum MediaKind {
    VIDEO,
    IMAGE;

    public static MediaKind fromStoredValue(String value) {
        if (IMAGE.name().equals(value)) {
            return IMAGE;
        }
        return VIDEO;
    }
}
