package com.ga170212.chk_datapack_link.integration;

import com.ga170212.chk_datapack_link.chzzk.ChzzkMessageProcessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

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
                String defaultChatNbt = "{player_name:\"\", player_uuid:\"\", cmd:\"\", chat:\"\", args:[], arg0:\"\", arg1:\"\", arg2:\"\", arg3:\"\", arg4:\"\", arg5:\"\", arg_count:0, sender_nick:\"\", raw_msg:\"\", sender_id:\"\", time:0L}";
                currentServer.getCommands().performPrefixedCommand(source, "data merge storage minecraft:chat " + defaultChatNbt);

                // Initialize default minecraft:donation storage
                String defaultDonationNbt = "{player_name:\"\", player_uuid:\"\", cmd:\"\", chat:\"\", amount:0, args:[], arg0:\"\", arg1:\"\", arg2:\"\", arg3:\"\", arg4:\"\", arg5:\"\", arg_count:0, sender_nick:\"\", pay_type:\"CHEESE\", raw_msg:\"\", sender_id:\"\", time:0L}";
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

    private static String formatArgsNbt(java.util.List<String> args) {
        if (args == null || args.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(escapeNbtString(args.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String getArgSafe(java.util.List<String> args, int index) {
        if (args != null && index >= 0 && index < args.size()) {
            return args.get(index);
        }
        return "";
    }

    private static String formatArgsFieldsNbt(java.util.List<String> args) {
        return String.format(
                "args:%s, arg0:%s, arg1:%s, arg2:%s, arg3:%s, arg4:%s, arg5:%s, arg_count:%d",
                formatArgsNbt(args),
                escapeNbtString(getArgSafe(args, 0)),
                escapeNbtString(getArgSafe(args, 1)),
                escapeNbtString(getArgSafe(args, 2)),
                escapeNbtString(getArgSafe(args, 3)),
                escapeNbtString(getArgSafe(args, 4)),
                escapeNbtString(getArgSafe(args, 5)),
                args != null ? args.size() : 0
        );
    }

    private static void mergeStorageAndTriggerFunction(String storageName, String nbtString, String functionTag, String playerName) {
        if (currentServer == null) {
            LOGGER.warn("Received event for {} but MinecraftServer is not available yet.", storageName);
            return;
        }

        currentServer.execute(() -> {
            try {
                CommandSourceStack source = currentServer.createCommandSourceStack().withSuppressedOutput();

                // 1. Storage 업데이트
                currentServer.getCommands().performPrefixedCommand(source, "data merge storage " + storageName + " " + nbtString);

                // 2. Datapack Function Tag 실행
                currentServer.getCommands().performPrefixedCommand(source, "function " + functionTag + " with storage " + storageName);

                LOGGER.info("Updated storage {} for player '{}'! NBT: {}", storageName, playerName, nbtString);
            } catch (Exception e) {
                LOGGER.error("Failed to execute datapack integration for " + storageName, e);
            }
        });
    }

    public static void handleChat(UUID playerUuid, String playerName, ChzzkMessageProcessor.ChatData chatData) {
        String nbtString = String.format(
                "{player_name:%s, player_uuid:%s, cmd:%s, chat:%s, %s, sender_nick:%s, raw_msg:%s, sender_id:%s, time:%dL}",
                escapeNbtString(playerName != null ? playerName : ""),
                escapeNbtString(playerUuid != null ? playerUuid.toString() : ""),
                escapeNbtString(chatData.cmd()),
                escapeNbtString(chatData.chat()),
                formatArgsFieldsNbt(chatData.args()),
                escapeNbtString(chatData.nickname()),
                escapeNbtString(chatData.rawMessage()),
                escapeNbtString(chatData.userId()),
                chatData.msgTime()
        );
        mergeStorageAndTriggerFunction("minecraft:chat", nbtString, "#chklink:chat", playerName);
    }

    public static void handleDonation(UUID playerUuid, String playerName, ChzzkMessageProcessor.DonationData donationData) {
        String nbtString = String.format(
                "{player_name:%s, player_uuid:%s, cmd:%s, chat:%s, amount:%d, %s, sender_nick:%s, pay_type:%s, raw_msg:%s, sender_id:%s, time:%dL}",
                escapeNbtString(playerName != null ? playerName : ""),
                escapeNbtString(playerUuid != null ? playerUuid.toString() : ""),
                escapeNbtString(donationData.cmd()),
                escapeNbtString(donationData.chat()),
                donationData.amount(),
                formatArgsFieldsNbt(donationData.args()),
                escapeNbtString(donationData.nickname()),
                escapeNbtString(donationData.payType()),
                escapeNbtString(donationData.rawMessage()),
                escapeNbtString(donationData.userId()),
                donationData.msgTime()
        );
        mergeStorageAndTriggerFunction("minecraft:donation", nbtString, "#chklink:donation", playerName);
    }
}
