package com.enviouse.progressivestages.compat.curios;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CuriosApiAccessTest {

    @Test
    void resolvesTheSupportedCapabilityInventorySurface() {
        assertDoesNotThrow(() -> CuriosApiAccess.resolve(getClass().getClassLoader()));
    }

    @Test
    void doesNotResolveTheRetiredInventoryPackage() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
            "top.theillusivec4.curios.api.type.inventory.ICuriosItemHandler",
            false,
            getClass().getClassLoader()
        ));
    }
}
