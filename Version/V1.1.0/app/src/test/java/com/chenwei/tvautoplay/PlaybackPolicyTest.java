package com.chenwei.tvautoplay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Behavioral tests at the playback-decision seam used by both activities and boot startup.
 */
public final class PlaybackPolicyTest {

    @Test
    public void firstLaunchWithoutSelectionRequestsVideoSelection() {
        PlaybackConfiguration configuration = PlaybackConfiguration.empty();

        assertEquals(
                PlaybackDecision.NEEDS_SELECTION,
                PlaybackPolicy.decide(configuration, false)
        );
    }

    @Test
    public void savedVideoWithPermissionIsReadyForLoopingPlayback() {
        PlaybackConfiguration configuration = new PlaybackConfiguration(
                "content://videos/intro.mp4",
                "intro.mp4",
                true,
                true,
                ScaleMode.FIT
        );

        assertEquals(PlaybackDecision.READY, PlaybackPolicy.decide(configuration, true));
    }

    @Test
    public void revokedFilePermissionRequestsReselectionInsteadOfCrashing() {
        PlaybackConfiguration configuration = new PlaybackConfiguration(
                "content://videos/missing.mp4",
                "missing.mp4",
                true,
                true,
                ScaleMode.FIT
        );

        assertEquals(
                PlaybackDecision.PERMISSION_REQUIRED,
                PlaybackPolicy.decide(configuration, false)
        );
    }

    @Test
    public void bootLaunchRequiresEnabledSettingSelectedVideoAndPermission() {
        PlaybackConfiguration enabled = new PlaybackConfiguration(
                "content://videos/signage.mkv",
                "signage.mkv",
                true,
                false,
                ScaleMode.ZOOM
        );
        PlaybackConfiguration disabled = new PlaybackConfiguration(
                enabled.videoUri(),
                enabled.displayName(),
                false,
                enabled.soundEnabled(),
                enabled.scaleMode()
        );

        assertTrue(PlaybackPolicy.shouldLaunchOnBoot(enabled, true));
        assertFalse(PlaybackPolicy.shouldLaunchOnBoot(enabled, false));
        assertFalse(PlaybackPolicy.shouldLaunchOnBoot(disabled, true));
        assertFalse(PlaybackPolicy.shouldLaunchOnBoot(PlaybackConfiguration.empty(), true));
    }
}
