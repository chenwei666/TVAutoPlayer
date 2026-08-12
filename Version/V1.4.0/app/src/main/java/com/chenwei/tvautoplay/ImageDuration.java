package com.chenwei.tvautoplay;

/** User-selectable still-image display duration. */
public enum ImageDuration {
    FIVE_SECONDS(5),
    TEN_SECONDS(10),
    FIFTEEN_SECONDS(15),
    THIRTY_SECONDS(30),
    SIXTY_SECONDS(60);

    public static final ImageDuration DEFAULT = TEN_SECONDS;

    private final int seconds;

    ImageDuration(int seconds) {
        this.seconds = seconds;
    }

    public int seconds() {
        return seconds;
    }

    public long milliseconds() {
        return seconds * 1_000L;
    }

    public ImageDuration next() {
        ImageDuration[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static ImageDuration fromStoredValue(String value) {
        if (value != null) {
            try {
                return valueOf(value);
            } catch (IllegalArgumentException ignored) {
                // Fall through to a safe default for corrupt or future values.
            }
        }
        return DEFAULT;
    }
}
