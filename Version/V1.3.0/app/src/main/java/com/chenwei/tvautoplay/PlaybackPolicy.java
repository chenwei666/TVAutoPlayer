package com.chenwei.tvautoplay;

/** Pure decision logic kept independent from Android so critical startup behavior is testable. */
public final class PlaybackPolicy {
    private PlaybackPolicy() {
    }

    public static PlaybackDecision decide(
            PlaybackConfiguration configuration,
            boolean hasReadPermission
    ) {
        return decide(configuration, hasReadPermission ? 1 : 0);
    }

    public static PlaybackDecision decide(
            PlaybackConfiguration configuration,
            int readableVideoCount
    ) {
        if (configuration == null || !configuration.hasMedia()) {
            return PlaybackDecision.NEEDS_SELECTION;
        }
        if (readableVideoCount <= 0) {
            return PlaybackDecision.PERMISSION_REQUIRED;
        }
        return PlaybackDecision.READY;
    }

    public static boolean shouldLaunchOnBoot(
            PlaybackConfiguration configuration,
            boolean hasReadPermission
    ) {
        return shouldLaunchOnBoot(configuration, hasReadPermission ? 1 : 0);
    }

    public static boolean shouldLaunchOnBoot(
            PlaybackConfiguration configuration,
            int readableVideoCount
    ) {
        return configuration != null
                && configuration.autoStartEnabled()
                && decide(configuration, readableVideoCount) == PlaybackDecision.READY;
    }
}
