package com.chenwei.tvautoplay;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ImageDurationTest {
    @Test
    public void durationsCycleThroughSupportedValues() {
        assertEquals(ImageDuration.TEN_SECONDS, ImageDuration.FIVE_SECONDS.next());
        assertEquals(ImageDuration.FIFTEEN_SECONDS, ImageDuration.TEN_SECONDS.next());
        assertEquals(ImageDuration.THIRTY_SECONDS, ImageDuration.FIFTEEN_SECONDS.next());
        assertEquals(ImageDuration.SIXTY_SECONDS, ImageDuration.THIRTY_SECONDS.next());
        assertEquals(ImageDuration.FIVE_SECONDS, ImageDuration.SIXTY_SECONDS.next());
    }

    @Test
    public void invalidStoredValueFallsBackToTenSeconds() {
        assertEquals(ImageDuration.DEFAULT, ImageDuration.fromStoredValue(null));
        assertEquals(ImageDuration.DEFAULT, ImageDuration.fromStoredValue("INVALID"));
        assertEquals(10_000L, ImageDuration.DEFAULT.milliseconds());
    }
}
