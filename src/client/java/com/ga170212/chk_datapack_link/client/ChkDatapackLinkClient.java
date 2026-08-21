package com.ga170212.chk_datapack_link.client;

import com.ga170212.chk_datapack_link.client.gui.ChannelInputScreen;
import com.ga170212.chk_datapack_link.chzzk.ChzzkManager;
import com.ga170212.chk_datapack_link.network.ChkLinkNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChkDatapackLinkClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("chk-datapack-link/Client");

    public static KeyMapping openConfigKey;
    private static boolean clientConnectedState = false;

    public static boolean isClientConnectedState() {
        return clientConnectedState;
    }

    public static void setClientConnectedState(boolean state) {
        clientConnectedState = state;
    }

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

        // S2C Status Packet Receiver
        ClientPlayNetworking.registerGlobalReceiver(ChkLinkNetwork.StatusPayload.TYPE, (payload, context) -> {
            boolean connected = payload.connected();
            String messageKey = payload.messageKey();
            String errorMsg = payload.errorMessage();

            setClientConnectedState(connected);

            context.client().execute(() -> {
                ChannelInputScreen screen = ChannelInputScreen.getCurrentInstance();
                if (screen != null) {
                    Component statusComp;
                    if (errorMsg != null && !errorMsg.isEmpty()) {
                        statusComp = Component.translatable(messageKey, errorMsg);
                    } else {
                        statusComp = Component.translatable(messageKey);
                    }
                    screen.onServerStatusReceived(connected, statusComp);
                }
            });
        });

        // Safe disconnection handling on Client play disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LOGGER.info("Client disconnected from server - resetting connection state...");
            setClientConnectedState(false);
            ChzzkManager.getInstance().shutdown();
        });
    }
}