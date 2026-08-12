package com.chenwei.tvautoplay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/** Public playlist behavior shared by selection, playback and boot startup. */
public final class PlaylistBehaviorTest {

    @Test
    public void replacementPreservesSelectionOrder() {
        List<PlaylistItem> selected = Arrays.asList(
                item("content://videos/1", "one.mp4"),
                item("content://videos/2", "two.mkv"),
                item("content://videos/3", "three.webm")
        );

        List<PlaylistItem> result = PlaylistEditor.replace(selected);

        assertEquals(selected, result);
    }

    @Test
    public void appendKeepsExistingOrderAndDeduplicatesByUri() {
        List<PlaylistItem> existing = Arrays.asList(
                item("content://videos/1", "one.mp4"),
                item("content://videos/2", "two.mkv")
        );
        List<PlaylistItem> selected = Arrays.asList(
                item("content://videos/2", "renamed-two.mkv"),
                item("content://videos/3", "three.webm"),
                item("content://videos/3", "duplicate-three.webm")
        );

        List<PlaylistItem> result = PlaylistEditor.append(existing, selected);

        assertEquals(Arrays.asList(
                item("content://videos/1", "one.mp4"),
                item("content://videos/2", "two.mkv"),
                item("content://videos/3", "three.webm")
        ), result);
    }

    @Test
    public void playlistIsReadyWhenAtLeastOneSelectedItemIsReadable() {
        PlaybackConfiguration configuration = new PlaybackConfiguration(
                Arrays.asList(
                        item("content://videos/missing", "missing.mp4"),
                        item("content://videos/ready", "ready.mp4")
                ),
                true,
                true,
                ScaleMode.FIT
        );

        assertEquals(PlaybackDecision.READY, PlaybackPolicy.decide(configuration, 1));
        assertEquals(
                PlaybackDecision.PERMISSION_REQUIRED,
                PlaybackPolicy.decide(configuration, 0)
        );
        assertTrue(PlaybackPolicy.shouldLaunchOnBoot(configuration, 1));
        assertFalse(PlaybackPolicy.shouldLaunchOnBoot(configuration, 0));
    }

    @Test
    public void mixedPlaylistPreservesKindsAndImageDuration() {
        PlaylistItem image = new PlaylistItem(
                "content://images/1",
                "poster.png",
                MediaKind.IMAGE
        );
        PlaylistItem video = item("content://videos/1", "intro.mp4");
        PlaybackConfiguration configuration = new PlaybackConfiguration(
                Arrays.asList(image, video),
                true,
                true,
                ScaleMode.FIT,
                ImageDuration.THIRTY_SECONDS
        );

        assertEquals(Arrays.asList(image, video), configuration.playlist());
        assertEquals(MediaKind.IMAGE, configuration.playlist().get(0).kind());
        assertEquals(MediaKind.VIDEO, configuration.playlist().get(1).kind());
        assertEquals(ImageDuration.THIRTY_SECONDS, configuration.imageDuration());
        assertEquals(2, configuration.mediaCount());
    }

    private static PlaylistItem item(String uri, String name) {
        return new PlaylistItem(uri, name);
    }
}
