package com.ga170212.chk_datapack_link.chzzk;

import com.ga170212.chk_datapack_link.config.ModConfig;
import com.ga170212.chk_datapack_link.integration.DatapackIntegrationManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ChzzkManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("chk-datapack-link/ChzzkManager");
    private static final ChzzkManager INSTANCE = new ChzzkManager();

    private ChzzkWebsocketClient client;

    public static ChzzkManager getInstance() {
        return INSTANCE;
    }

    public synchronized boolean isConnected() {
        return client != null && client.isConnected();
    }

    public synchronized void connect(CommandSourceStack feedbackSource) {
        connect(
                () -> {
                    if (feedbackSource != null) {
                        feedbackSource.sendSuccess(() -> Component.translatable("chat.chk-datapack-link.connect_success"), false);
                    }
                },
                (errorMsg) -> {
                    if (feedbackSource != null) {
                        feedbackSource.sendFailure(Component.translatable("chat.chk-datapack-link.connect_failed", errorMsg));
                    }
                }
        );
    }

    public synchronized void connect(Runnable onSuccess, Consumer<String> onError) {
        String channelId = ModConfig.getInstance().getChannelId();
        if (channelId.isEmpty()) {
            if (onError != null) {
                onError.accept(Component.translatable("error.chk-datapack-link.no_channel_id").getString());
            }
            return;
        }

        if (isConnected()) {
            if (onError != null) {
                onError.accept(Component.translatable("error.chk-datapack-link.already_connected").getString());
            }
            return;
        }

        // Initialize default storage immediately on connect
        DatapackIntegrationManager.initializeDefaultStorages();

        ChzzkMessageProcessor.MessageHandler handler = new ChzzkMessageProcessor.MessageHandler() {
            @Override
            public void onChatReceived(ChzzkMessageProcessor.ChatData chatData) {
                DatapackIntegrationManager.handleChat(chatData);
            }

            @Override
            public void onDonationReceived(ChzzkMessageProcessor.DonationData donationData) {
                DatapackIntegrationManager.handleDonation(donationData);
            }
        };

        if (client != null) {
            client.disconnect();
        }

        client = new ChzzkWebsocketClient(channelId, handler);
        client.connectAsync(
                () -> {
                    DatapackIntegrationManager.initializeDefaultStorages();
                    LOGGER.info("Connected to Chzzk channel: {}", channelId);
                    if (onSuccess != null) onSuccess.run();
                },
                (errorMsg) -> {
                    LOGGER.error("Connection failed: {}", errorMsg);
                    if (onError != null) onError.accept(errorMsg);
                }
        );
    }

    public synchronized void disconnect(CommandSourceStack feedbackSource) {
        disconnect();
        if (feedbackSource != null) {
            feedbackSource.sendSuccess(() -> Component.translatable("chat.chk-datapack-link.disconnect_success"), false);
        }
    }

    public synchronized void disconnect() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
    }

    public synchronized void shutdown() {
        if (client != null) {
            LOGGER.info("Shutting down Chzzk manager...");
            client.disconnect();
            client = null;
        }
    }
}
