package com.enviouse.progressivestages.common.rehaul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewerPolicyTest {

    @Test
    void sharedHideAppliesToBothViewers() {
        ViewerPolicy policy = new ViewerPolicy(ViewerPolicy.Mode.HIDE, ViewerPolicy.Mode.INHERIT,
            ViewerPolicy.Mode.INHERIT, true, true, true, true, true);

        assertTrue(policy.hidesInEmi());
        assertTrue(policy.hidesInJei());
    }

    @Test
    void viewerSpecificModeOverridesSharedMode() {
        ViewerPolicy policy = new ViewerPolicy(ViewerPolicy.Mode.SHOW, ViewerPolicy.Mode.HIDE,
            ViewerPolicy.Mode.INHERIT, true, true, true, true, true);

        assertTrue(policy.hidesInEmi());
        assertFalse(policy.hidesInJei());
    }

    @Test
    void hiddenIngredientsHideBothViewers() {
        ViewerPolicy policy = new ViewerPolicy(ViewerPolicy.Mode.SHOW, ViewerPolicy.Mode.INHERIT,
            ViewerPolicy.Mode.INHERIT, false, true, true, true, true);

        assertTrue(policy.hidesInEmi());
        assertTrue(policy.hidesInJei());
    }
}
