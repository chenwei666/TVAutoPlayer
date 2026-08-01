package com.chenwei.tvautoplay;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable application settings shared by playback and boot-start decisions. */
public final class PlaybackConfiguration {
    private final List<PlaylistItem> playlist;
    private final boolean autoStartEnabled;
    private final boolean soundEnabled;
    private final ScaleMode scaleMode;

    public PlaybackConfiguration(
            List<PlaylistItem> playlist,
            boolean autoStartEnabled,
            boolean soundEnabled,
            ScaleMode scaleMode
    ) {
        this.playlist = PlaylistEditor.replace(playlist);
        this.autoStartEnabled = autoStartEnabled;
        this.soundEnabled = soundEnabled;
        this.scaleMode = Objects.requireNonNull(scaleMode, "scaleMode");
    }

    /** Compatibility constructor for upgrading V1.0.0's single-video configuration. */
    public PlaybackConfiguration(
            String videoUri,
            String displayName,
            boolean autoStartEnabled,
            boolean soundEnabled,
            ScaleMode scaleMode
    ) {
        this(
                normalize(videoUri) == null
                        ? Collections.emptyList()
                        : Collections.singletonList(new PlaylistItem(videoUri, displayName)),
                autoStartEnabled,
                soundEnabled,
                scaleMode
        );
    }

    public static PlaybackConfiguration empty() {
        return new PlaybackConfiguration(Collections.emptyList(), true, true, ScaleMode.FIT);
    }

    public List<PlaylistItem> playlist() {
        return playlist;
    }

    public String videoUri() {
        return hasVideo() ? playlist.get(0).uri() : null;
    }

    public String displayName() {
        return hasVideo() ? playlist.get(0).displayName() : null;
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
        return !playlist.isEmpty();
    }

    public int videoCount() {
        return playlist.size();
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
