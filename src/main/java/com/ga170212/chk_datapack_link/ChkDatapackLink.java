package com.ga170212.chk_datapack_link;

import com.ga170212.chk_datapack_link.chzzk.ChzzkManager;
import com.ga170212.chk_datapack_link.command.ChkLinkCommand;
import com.ga170212.chk_datapack_link.config.ModConfig;
import com.ga170212.chk_datapack_link.integration.DatapackIntegrationManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;
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

        // Register Commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ChkLinkCommand.register(dispatcher);
        });

        // Server Lifecycle Events
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            DatapackIntegrationManager.setServer(server);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            LOGGER.info("Server stopping - safely shutting down Chzzk WebSocket...");
            ChzzkManager.getInstance().shutdown();
            DatapackIntegrationManager.setServer(null);
        });

        // JVM Shutdown Hook for Crash / Unexpected Termination Safety
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("JVM Shutdown hook triggered - disconnecting Chzzk WebSocket...");
            ChzzkManager.getInstance().shutdown();
        }, "Chzzk-Shutdown-Hook"));
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
