package com.chenwei.tvautoplay;

/** Pure decision logic kept independent from Android so critical startup behavior is testable. */
public final class PlaybackPolicy {
    private PlaybackPolicy() {
    }

    public static PlaybackDecision decide(
            PlaybackConfiguration configuration,
            boolean hasReadPermission
    ) {
        if (configuration == null || !configuration.hasVideo()) {
            return PlaybackDecision.NEEDS_SELECTION;
        }
        if (!hasReadPermission) {
            return PlaybackDecision.PERMISSION_REQUIRED;
        }
        return PlaybackDecision.READY;
    }

    public static boolean shouldLaunchOnBoot(
            PlaybackConfiguration configuration,
            boolean hasReadPermission
    ) {
        return configuration != null
                && configuration.autoStartEnabled()
                && decide(configuration, hasReadPermission) == PlaybackDecision.READY;
    }
}
