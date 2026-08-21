package com.ga170212.chk_datapack_link;

import com.ga170212.chk_datapack_link.chzzk.ChzzkManager;
import com.ga170212.chk_datapack_link.command.ChkLinkCommand;
import com.ga170212.chk_datapack_link.config.ModConfig;
import com.ga170212.chk_datapack_link.integration.DatapackIntegrationManager;
import com.ga170212.chk_datapack_link.network.ChkLinkNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChkDatapackLink implements ModInitializer {
    public static final String MOD_ID = "chk-datapack-link";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Chzzk Datapack Link Mod...");

        // Load Config
        ModConfig.load();

        // Register Network Payloads
        ChkLinkNetwork.registerCommonPayloads();

        // Register Server-Side Packet Receivers
        registerServerPacketHandlers();

        // Register Commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ChkLinkCommand.register(dispatcher);
        });

        // Server Lifecycle Events
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            DatapackIntegrationManager.setServer(server);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            LOGGER.info("Server stopping - safely shutting down all Chzzk WebSockets...");
            ChzzkManager.getInstance().shutdown();
            DatapackIntegrationManager.setServer(null);
        });

        // Player Disconnect Event - Automatically close player's Chzzk WebSocket session
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (player != null) {
                LOGGER.info("Player {} disconnected - closing Chzzk session...", player.getScoreboardName());
                ChzzkManager.getInstance().disconnect(player.getUUID());
            }
        });

        // JVM Shutdown Hook for Crash / Unexpected Termination Safety
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("JVM Shutdown hook triggered - disconnecting all Chzzk WebSockets...");
            ChzzkManager.getInstance().shutdown();
        }, "Chzzk-Shutdown-Hook"));
    }

    private void registerServerPacketHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(ChkLinkNetwork.TogglePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            String channelId = payload.channelId();
            boolean requestConnect = payload.connect();

            context.server().execute(() -> {
                if (requestConnect) {
                    ChzzkManager.getInstance().connect(
                            player.getUUID(),
                            player.getScoreboardName(),
                            channelId,
                            () -> {
                                ServerPlayNetworking.send(player, new ChkLinkNetwork.StatusPayload(true, "gui.chk-datapack-link.connect_success", ""));
                                player.sendSystemMessage(Component.translatable("chat.chk-datapack-link.connect_success"));
                            },
                            (errorMsg) -> {
                                ServerPlayNetworking.send(player, new ChkLinkNetwork.StatusPayload(false, "gui.chk-datapack-link.connect_failed", errorMsg));
                                player.sendSystemMessage(Component.translatable("chat.chk-datapack-link.connect_failed", errorMsg));
                            }
                    );
                } else {
                    ChzzkManager.getInstance().disconnect(player.getUUID());
                    ServerPlayNetworking.send(player, new ChkLinkNetwork.StatusPayload(false, "gui.chk-datapack-link.disconnected", ""));
                    player.sendSystemMessage(Component.translatable("chat.chk-datapack-link.disconnect_success"));
                }
            });
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
