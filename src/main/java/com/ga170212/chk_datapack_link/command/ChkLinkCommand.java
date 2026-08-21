package com.ga170212.chk_datapack_link.command;

import com.ga170212.chk_datapack_link.chzzk.ChzzkManager;
import com.ga170212.chk_datapack_link.chzzk.ChzzkMessageProcessor;
import com.ga170212.chk_datapack_link.config.ModConfig;
import com.ga170212.chk_datapack_link.integration.DatapackIntegrationManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

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
        boolean connected = ChzzkManager.getInstance().isConnected();

        source.sendSuccess(() -> Component.translatable("command.chk-datapack-link.status.title"), false);
        source.sendSuccess(() -> Component.translatable("command.chk-datapack-link.status.channel_id", channelId.isEmpty() ? Component.translatable("command.chk-datapack-link.status.none") : channelId), false);
        source.sendSuccess(() -> Component.translatable(connected ? "command.chk-datapack-link.status.connected" : "command.chk-datapack-link.status.disconnected"), false);
        return 1;
    }

    private static int executeTestChat(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String message = StringArgumentType.getString(context, "message");

        ChzzkMessageProcessor.ParsedCmdChat parsed = ChzzkMessageProcessor.parseCommandAndChat(message);
        String cleanedRawMsg = ChzzkMessageProcessor.cleanEmojiTokens(message);

        ChzzkMessageProcessor.ChatData chatData = new ChzzkMessageProcessor.ChatData(
                "테스트유저",
                "test_hash",
                cleanedRawMsg,
                parsed.cmd(),
                parsed.chat(),
                System.currentTimeMillis()
        );

        DatapackIntegrationManager.handleChat(chatData);
        source.sendSuccess(() -> Component.translatable("command.chk-datapack-link.testchat.success", message), false);
        return 1;
    }

    private static int executeTestDonation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        String message = StringArgumentType.getString(context, "message");

        ChzzkMessageProcessor.ParsedCmdChat parsed = ChzzkMessageProcessor.parseCommandAndChat(message);
        String cleanedRawMsg = ChzzkMessageProcessor.cleanEmojiTokens(message);

        ChzzkMessageProcessor.DonationData donationData = new ChzzkMessageProcessor.DonationData(
                "테스트후원자",
                "test_hash",
                amount,
                "CHEESE",
                cleanedRawMsg,
                parsed.cmd(),
                parsed.chat(),
                System.currentTimeMillis()
        );

        DatapackIntegrationManager.handleDonation(donationData);
        source.sendSuccess(() -> Component.translatable("command.chk-datapack-link.testdonation.success", amount, message), false);
        return 1;
    }
}
