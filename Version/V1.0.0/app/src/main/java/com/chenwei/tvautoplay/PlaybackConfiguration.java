package com.chenwei.tvautoplay;

import java.util.Objects;

/** Immutable application settings shared by playback and boot-start decisions. */
public final class PlaybackConfiguration {
    private final String videoUri;
    private final String displayName;
    private final boolean autoStartEnabled;
    private final boolean soundEnabled;
    private final ScaleMode scaleMode;

    public PlaybackConfiguration(
            String videoUri,
            String displayName,
            boolean autoStartEnabled,
            boolean soundEnabled,
            ScaleMode scaleMode
    ) {
        this.videoUri = normalize(videoUri);
        this.displayName = normalize(displayName);
        this.autoStartEnabled = autoStartEnabled;
        this.soundEnabled = soundEnabled;
        this.scaleMode = Objects.requireNonNull(scaleMode, "scaleMode");
    }

    public static PlaybackConfiguration empty() {
        return new PlaybackConfiguration(null, null, true, true, ScaleMode.FIT);
    }

    public String videoUri() {
        return videoUri;
    }

    public String displayName() {
        return displayName;
    }

    public boolean autoStartEnabled() {
        return autoStartEnabled;
    }

    public boolean soundEnabled() {
        return soundEnabled;
    }

    public ScaleMode scaleMode() {
        return scaleMode;
    }

    public boolean hasVideo() {
        return videoUri != null;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
