package com.enviouse.progressivestages.client;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

/**
 * Invokes the optional EMI lifecycle hooks without linking the normal client event path to EMI.
 */
public final class EmiLifecycleBridge {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String EMI_PLUGIN =
        "com.enviouse.progressivestages.client.emi.ProgressiveStagesEMIPlugin";

    private EmiLifecycleBridge() {}

    public static void beginClientDisconnect() {
        invoke("beginClientDisconnect");
    }

    public static void endClientDisconnect() {
        invoke("endClientDisconnect");
    }

    private static void invoke(String methodName) {
        if (!ModList.get().isLoaded("emi")) return;
        try {
            Class.forName(EMI_PLUGIN).getMethod(methodName).invoke(null);
        } catch (ReflectiveOperationException | LinkageError error) {
            LOGGER.debug("[ProgressiveStages] EMI lifecycle bridge was unavailable", error);
        }
    }
}
