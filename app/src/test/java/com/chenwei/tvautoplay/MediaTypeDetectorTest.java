package com.chenwei.tvautoplay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class MediaTypeDetectorTest {
    @Test
    public void detectsSupportedImagesFromMimeTypesAndNames() {
        assertEquals(MediaKind.IMAGE, MediaTypeDetector.detect("image/jpeg", "poster.bin"));
        assertEquals(MediaKind.IMAGE, MediaTypeDetector.detect(null, "poster.PNG"));
        assertEquals(MediaKind.IMAGE, MediaTypeDetector.detect("application/octet-stream", "photo.heic"));
    }

    @Test
    public void detectsVideosFromMimeTypesAndNames() {
        assertEquals(MediaKind.VIDEO, MediaTypeDetector.detect("video/mp4", "intro.bin"));
        assertEquals(MediaKind.VIDEO, MediaTypeDetector.detect(
                "application/x-matroska",
                "intro.bin"
        ));
        assertEquals(MediaKind.VIDEO, MediaTypeDetector.detect(null, "intro.MKV"));
        assertEquals(MediaKind.VIDEO, MediaTypeDetector.detect(null, "loop.m3u8"));
    }

    @Test
    public void rejectsGifAndUnknownFiles() {
        assertNull(MediaTypeDetector.detect("image/gif", "animated.gif"));
        assertNull(MediaTypeDetector.detect("application/pdf", "brochure.pdf"));
        assertNull(MediaTypeDetector.detect(null, "README"));
    }
}
