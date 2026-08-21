package com.ga170212.chk_datapack_link.client;

import com.ga170212.chk_datapack_link.client.gui.ChannelInputScreen;
import com.ga170212.chk_datapack_link.chzzk.ChzzkManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChkDatapackLinkClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("chk-datapack-link/Client");

    public static KeyMapping openConfigKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Chzzk Datapack Link Client...");

        // Register KeyMapping for quick config GUI (Default: F6)
        openConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.chk-datapack-link.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                KeyMapping.Category.MISC
        ));

        // Client Tick Event for KeyBinding
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.consumeClick()) {
                if (client.player != null) {
                    client.setScreenAndShow(new ChannelInputScreen(null));
                }
            }
        });

        // Safe disconnection handling on Client play disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LOGGER.info("Client disconnected from server - shutting down Chzzk WebSocket...");
            ChzzkManager.getInstance().shutdown();
        });
    }
}