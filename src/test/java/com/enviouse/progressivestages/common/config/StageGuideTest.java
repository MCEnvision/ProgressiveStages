package com.enviouse.progressivestages.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StageGuideTest {
    @Test
    void keepsUnicodeAndNewlinesButRejectsControls() {
        StageGuide guide = new StageGuide("Mine the 🪨 vein.\nThen return.", "Open the map", "Near spawn", StageGuide.Recommendation.AUTO);
        assertEquals("Mine the 🪨 vein.\nThen return.", guide.howToUnlock());
        assertThrows(IllegalArgumentException.class,
            () -> new StageGuide("bad" + Character.toString((char) 1) + "text", "", "", StageGuide.Recommendation.AUTO));
    }

    @Test
    void enforcesCodePointAndByteLimits() {
        String tooLong = "x".repeat(StageGuide.MAX_TEXT_CODE_POINTS + 1);
        assertThrows(IllegalArgumentException.class,
            () -> new StageGuide(tooLong, "", "", StageGuide.Recommendation.AUTO));
        assertThrows(IllegalArgumentException.class,
            () -> new StageGuide("😀".repeat(StageGuide.MAX_TEXT_BYTES / 4 + 1), "", "", StageGuide.Recommendation.AUTO));
    }
}
