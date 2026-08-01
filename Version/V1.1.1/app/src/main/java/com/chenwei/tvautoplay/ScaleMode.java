package com.chenwei.tvautoplay;

/** Controls how video is placed inside the television viewport. */
public enum ScaleMode {
    FIT,
    ZOOM;

    public static ScaleMode fromStoredValue(String value) {
        if (ZOOM.name().equals(value)) {
            return ZOOM;
        }
        return FIT;
    }
}
