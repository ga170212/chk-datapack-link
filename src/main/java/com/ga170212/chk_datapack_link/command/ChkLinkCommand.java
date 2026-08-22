package com.ga170212.chk_datapack_link.command;

import com.ga170212.chk_datapack_link.chzzk.ChzzkManager;
import com.ga170212.chk_datapack_link.chzzk.ChzzkMessageProcessor;
import com.ga170212.chk_datapack_link.config.ModConfig;
import com.ga170212.chk_datapack_link.integration.DatapackIntegrationManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class ChkLinkCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("chklink")
                        .then(Commands.literal("connect")
                                .executes(ChkLinkCommand::executeConnect))
                        .then(Commands.literal("start")
                                .executes(ChkLinkCommand::executeConnect))
                        .then(Commands.literal("disconnect")
                                .executes(ChkLinkCommand::executeDisconnect))
                        .then(Commands.literal("stop")
                                .executes(ChkLinkCommand::executeDisconnect))
                        .then(Commands.literal("status")
                                .executes(ChkLinkCommand::executeStatus))
                        .then(Commands.literal("testchat")
                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                        .executes(ChkLinkCommand::executeTestChat)))
                        .then(Commands.literal("testdonation")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                                .executes(ChkLinkCommand::executeTestDonation))))
        );
    }

    private static int executeConnect(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ChzzkManager.getInstance().connect(source);
        return 1;
    }

    private static int executeDisconnect(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ChzzkManager.getInstance().disconnect(source);
        return 1;
    }

    private static int executeStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String channelId = ModConfig.getInstance().getChannelId();

        UUID playerUuid = ChzzkManager.DEFAULT_SERVER_UUID;
        if (source.getEntity() instanceof ServerPlayer player) {
            playerUuid = player.getUUID();
        }

        boolean connected = ChzzkManager.getInstance().isConnected(playerUuid);

        source.sendSuccess(() -> Component.translatable("command.chk-datapack-link.status.title"), false);
        source.sendSuccess(() -> Component.translatable("command.chk-datapack-link.status.channel_id", channelId.isEmpty() ? Component.translatable("command.chk-datapack-link.status.none") : channelId), false);
        source.sendSuccess(() -> Component.translatable(connected ? "command.chk-datapack-link.status.connected" : "command.chk-datapack-link.status.disconnected"), false);
        return 1;
    }

    private static String createMockChatJson(String message) {
        JsonObject root = new JsonObject();
        root.addProperty("cmd", 93101);

        JsonObject item = new JsonObject();
        item.addProperty("msg", message != null ? message : "");
        item.addProperty("msgTypeCode", 10001);
        item.addProperty("msgTime", System.currentTimeMillis());

        JsonObject profile = new JsonObject();
        profile.addProperty("nickname", "테스트유저");
        profile.addProperty("userIdHash", "test_hash");
        item.addProperty("profile", profile.toString());

        JsonArray bdy = new JsonArray();
        bdy.add(item);
        root.add("bdy", bdy);

        return root.toString();
    }

    private static String createMockDonationJson(int amount, String message) {
        JsonObject root = new JsonObject();
        root.addProperty("cmd", 93102);

        JsonObject item = new JsonObject();
        item.addProperty("msg", message != null ? message : "");
        item.addProperty("msgTypeCode", 10003);
        item.addProperty("msgTime", System.currentTimeMillis());

        JsonObject profile = new JsonObject();
        profile.addProperty("nickname", "테스트후원자");
        profile.addProperty("userIdHash", "test_hash");
        item.addProperty("profile", profile.toString());

        JsonObject extras = new JsonObject();
        extras.addProperty("payAmount", amount);
        extras.addProperty("payType", "CHEESE");
        item.addProperty("extras", extras.toString());

        JsonArray bdy = new JsonArray();
        bdy.add(item);
        root.add("bdy", bdy);

        return root.toString();
    }

    private static int executeTestChat(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String message = StringArgumentType.getString(context, "message");

        UUID playerUuid = ChzzkManager.DEFAULT_SERVER_UUID;
        String playerName = "Server";
        if (source.getEntity() instanceof ServerPlayer player) {
            playerUuid = player.getUUID();
            playerName = player.getScoreboardName();
        }

        final UUID finalUuid = playerUuid;
        final String finalName = playerName;

        String dummyJson = createMockChatJson(message);
        ChzzkMessageProcessor.processPacket(dummyJson, new ChzzkMessageProcessor.MessageHandler() {
            @Override
            public void onChatReceived(ChzzkMessageProcessor.ChatData chatData) {
                DatapackIntegrationManager.handleChat(finalUuid, finalName, chatData);
            }

            @Override
            public void onDonationReceived(ChzzkMessageProcessor.DonationData donationData) {
                DatapackIntegrationManager.handleDonation(finalUuid, finalName, donationData);
            }
        });

        source.sendSuccess(() -> Component.translatable("command.chk-datapack-link.testchat.success", message), false);
        return 1;
    }

    private static int executeTestDonation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        String message = StringArgumentType.getString(context, "message");

        UUID playerUuid = ChzzkManager.DEFAULT_SERVER_UUID;
        String playerName = "Server";
        if (source.getEntity() instanceof ServerPlayer player) {
            playerUuid = player.getUUID();
            playerName = player.getScoreboardName();
        }

        final UUID finalUuid = playerUuid;
        final String finalName = playerName;

        String dummyJson = createMockDonationJson(amount, message);
        ChzzkMessageProcessor.processPacket(dummyJson, new ChzzkMessageProcessor.MessageHandler() {
            @Override
            public void onChatReceived(ChzzkMessageProcessor.ChatData chatData) {
                DatapackIntegrationManager.handleChat(finalUuid, finalName, chatData);
            }

            @Override
            public void onDonationReceived(ChzzkMessageProcessor.DonationData donationData) {
                DatapackIntegrationManager.handleDonation(finalUuid, finalName, donationData);
            }
        });

        source.sendSuccess(() -> Component.translatable("command.chk-datapack-link.testdonation.success", amount, message), false);
        return 1;
    }
}
