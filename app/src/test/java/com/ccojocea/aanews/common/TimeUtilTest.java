package com.ccojocea.aanews.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TimeUtilTest {

    // 2020-06-12T14:22:06Z
    private static final long EXPECTED_MILLIS = 1591971726000L;

    @Test
    public void parseServerTime_isUtc() {
        assertEquals(Long.valueOf(EXPECTED_MILLIS), TimeUtil.parseServerTime("2020-06-12T14:22:06Z"));
    }

    @Test
    public void parseServerTime_ignoresMilliseconds() {
        assertEquals(Long.valueOf(EXPECTED_MILLIS), TimeUtil.parseServerTime("2020-06-12T14:22:06.123Z"));
    }

    @Test
    public void parseServerTime_dateOnly() {
        assertEquals(Long.valueOf(1591920000000L), TimeUtil.parseServerTime("2020-06-12"));
    }

    @Test
    public void parseServerTime_invalid() {
        assertNull(TimeUtil.parseServerTime(null));
        assertNull(TimeUtil.parseServerTime(""));
        assertNull(TimeUtil.parseServerTime("yesterday"));
    }

}
