package com.enviouse.progressivestages.client.editor;

import com.enviouse.progressivestages.common.network.NetworkHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EditorResponseDispatchTest {
    @Test
    @SuppressWarnings("unchecked")
    void browserResponseCompletesWithoutTheRenderThread() throws Exception {
        var field = EditorBridgeTransport.class.getDeclaredField("PENDING");
        field.setAccessible(true);
        var pending = (Map<UUID, CompletableFuture<String>>) field.get(null);
        UUID request = UUID.randomUUID();
        var response = new CompletableFuture<String>();
        pending.put(request, response);
        var handler = NetworkHandler.class.getDeclaredMethod("handleEditorResponse",
            NetworkHandler.EditorResponsePayload.class, IPayloadContext.class);
        handler.setAccessible(true);
        try (var network = Executors.newSingleThreadExecutor()) {
            network.submit(() -> {
                handler.invoke(null, new NetworkHandler.EditorResponsePayload(UUID.randomUUID(), "unrelated"), null);
                assertFalse(response.isDone());
                handler.invoke(null, new NetworkHandler.EditorResponsePayload(request, "accepted"), null);
                return null;
            }).get(5, TimeUnit.SECONDS);
            assertEquals("accepted", response.get(1, TimeUnit.SECONDS));
            assertFalse(pending.containsKey(request));
        } finally {
            pending.remove(request);
        }
    }
}
