package com.chenwei.tvautoplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

/** Stores only non-sensitive playback preferences; video content is never copied. */
public final class PlaybackConfigStore {
    private static final String PREFS_NAME = "playback_configuration";
    private static final String KEY_VIDEO_URI = "video_uri";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_AUTO_START = "auto_start";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_SCALE_MODE = "scale_mode";
    private static final String KEY_HINT_SHOWN = "settings_hint_shown";

    private final SharedPreferences preferences;

    public PlaybackConfigStore(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public PlaybackConfiguration load() {
        return new PlaybackConfiguration(
                preferences.getString(KEY_VIDEO_URI, null),
                preferences.getString(KEY_DISPLAY_NAME, null),
                preferences.getBoolean(KEY_AUTO_START, true),
                preferences.getBoolean(KEY_SOUND, true),
                ScaleMode.fromStoredValue(preferences.getString(KEY_SCALE_MODE, null))
        );
    }

    public void saveVideo(Uri uri, String displayName) {
        preferences.edit()
                .putString(KEY_VIDEO_URI, uri.toString())
                .putString(KEY_DISPLAY_NAME, displayName)
                .apply();
    }

    public void clearVideo() {
        preferences.edit()
                .remove(KEY_VIDEO_URI)
                .remove(KEY_DISPLAY_NAME)
                .apply();
    }

    public void setAutoStartEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_AUTO_START, enabled).apply();
    }

    public void setSoundEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public void setScaleMode(ScaleMode mode) {
        preferences.edit().putString(KEY_SCALE_MODE, mode.name()).apply();
    }

    public boolean isSettingsHintShown() {
        return preferences.getBoolean(KEY_HINT_SHOWN, false);
    }

    public void markSettingsHintShown() {
        preferences.edit().putBoolean(KEY_HINT_SHOWN, true).apply();
    }
}
