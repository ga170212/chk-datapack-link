package com.ga170212.chk_datapack_link.integration;

import com.ga170212.chk_datapack_link.chzzk.ChzzkMessageProcessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatapackIntegrationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("chk-datapack-link/DatapackIntegrationManager");
    private static MinecraftServer currentServer = null;

    public static void setServer(MinecraftServer server) {
        currentServer = server;
        if (server != null) {
            initializeDefaultStorages();
        }
    }

    public static MinecraftServer getServer() {
        return currentServer;
    }

    public static void initializeDefaultStorages() {
        if (currentServer == null) return;

        currentServer.execute(() -> {
            try {
                CommandSourceStack source = currentServer.createCommandSourceStack().withSuppressedOutput();

                // Initialize default minecraft:chat storage
                String defaultChatNbt = "{cmd:\"\", chat:\"\", sender:\"\", raw_msg:\"\", user_id:\"\", time:0L}";
                currentServer.getCommands().performPrefixedCommand(source, "data merge storage minecraft:chat " + defaultChatNbt);

                // Initialize default minecraft:donation storage
                String defaultDonationNbt = "{cmd:\"\", chat:\"\", amount:0, sender:\"\", pay_type:\"CHEESE\", raw_msg:\"\", user_id:\"\", time:0L}";
                currentServer.getCommands().performPrefixedCommand(source, "data merge storage minecraft:donation " + defaultDonationNbt);

                LOGGER.info("Initialized default storages for minecraft:chat and minecraft:donation");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize default storages", e);
            }
        });
    }

    private static String escapeNbtString(String input) {
        if (input == null) return "\"\"";
        String escaped = input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        return "\"" + escaped + "\"";
    }

    public static void handleChat(ChzzkMessageProcessor.ChatData chatData) {
        if (currentServer == null) {
            LOGGER.warn("Received chat but MinecraftServer is not available yet.");
            return;
        }

        currentServer.execute(() -> {
            try {
                CommandSourceStack source = currentServer.createCommandSourceStack().withSuppressedOutput();

                String nbtString = String.format(
                        "{cmd:%s, chat:%s, sender:%s, raw_msg:%s, user_id:%s, time:%dL}",
                        escapeNbtString(chatData.cmd()),
                        escapeNbtString(chatData.chat()),
                        escapeNbtString(chatData.nickname()),
                        escapeNbtString(chatData.rawMessage()),
                        escapeNbtString(chatData.userId()),
                        chatData.msgTime()
                );

                // 1. Storage 업데이트
                String dataCmd = "data merge storage minecraft:chat " + nbtString;
                currentServer.getCommands().performPrefixedCommand(source, dataCmd);

                // 2. Datapack Function Tag 실행 (#chklink:chat)
                String funcCmd = "function #chklink:chat with storage minecraft:chat";
                currentServer.getCommands().performPrefixedCommand(source, funcCmd);

                LOGGER.info("Successfully updated storage minecraft:chat! NBT: {}", nbtString);
            } catch (Exception e) {
                LOGGER.error("Failed to execute chat datapack integration", e);
            }
        });
    }

    public static void handleDonation(ChzzkMessageProcessor.DonationData donationData) {
        if (currentServer == null) {
            LOGGER.warn("Received donation but MinecraftServer is not available yet.");
            return;
        }

        currentServer.execute(() -> {
            try {
                CommandSourceStack source = currentServer.createCommandSourceStack().withSuppressedOutput();

                String nbtString = String.format(
                        "{cmd:%s, chat:%s, amount:%d, sender:%s, pay_type:%s, raw_msg:%s, user_id:%s, time:%dL}",
                        escapeNbtString(donationData.cmd()),
                        escapeNbtString(donationData.chat()),
                        donationData.amount(),
                        escapeNbtString(donationData.nickname()),
                        escapeNbtString(donationData.payType()),
                        escapeNbtString(donationData.rawMessage()),
                        escapeNbtString(donationData.userId()),
                        donationData.msgTime()
                );

                // 1. Storage 업데이트
                String dataCmd = "data merge storage minecraft:donation " + nbtString;
                currentServer.getCommands().performPrefixedCommand(source, dataCmd);

                // 2. Datapack Function Tag 실행 (#chklink:donation)
                String funcCmd = "function #chklink:donation with storage minecraft:donation";
                currentServer.getCommands().performPrefixedCommand(source, funcCmd);

                LOGGER.info("Successfully updated storage minecraft:donation! NBT: {}", nbtString);
            } catch (Exception e) {
                LOGGER.error("Failed to execute donation datapack integration", e);
            }
        });
    }
}
