package com.ga170212.chk_datapack_link.chzzk;

import com.ga170212.chk_datapack_link.config.ModConfig;
import com.ga170212.chk_datapack_link.integration.DatapackIntegrationManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChzzkManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("chk-datapack-link/ChzzkManager");
    private static final ChzzkManager INSTANCE = new ChzzkManager();

    // 플레이어 UUID별 활성화된 웹소켓 클라이언트 세션 관리
    private final Map<UUID, ChzzkWebsocketClient> clientMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerNameMap = new ConcurrentHashMap<>();

    // 싱글/콘솔용 기본 UUID
    public static final UUID DEFAULT_SERVER_UUID = new UUID(0L, 0L);

    public static ChzzkManager getInstance() {
        return INSTANCE;
    }

    public boolean isConnected(UUID playerUuid) {
        ChzzkWebsocketClient client = clientMap.get(playerUuid);
        return client != null && client.isConnected();
    }

    public boolean isAnyConnected() {
        return clientMap.values().stream().anyMatch(ChzzkWebsocketClient::isConnected);
    }

    /**
     * 명령어(CommandSourceStack)에서 연동을 실행할 때 사용
     */
    public void connect(CommandSourceStack source) {
        UUID playerUuid = DEFAULT_SERVER_UUID;
        String playerName = "Server";

        if (source.getEntity() instanceof ServerPlayer player) {
            playerUuid = player.getUUID();
            playerName = player.getScoreboardName();
        }

        String channelId = ModConfig.getInstance().getChannelId();
        connect(playerUuid, playerName, channelId,
                () -> source.sendSuccess(() -> Component.translatable("chat.chk-datapack-link.connect_success"), false),
                (errorMsg) -> source.sendFailure(Component.translatable("chat.chk-datapack-link.connect_failed", errorMsg))
        );
    }

    /**
     * 특정 플레이어의 치지직 채널 연동 시작
     */
    public synchronized void connect(UUID playerUuid, String playerName, String channelId, Runnable onSuccess, Consumer<String> onError) {
        if (channelId == null || channelId.trim().isEmpty()) {
            if (onError != null) {
                onError.accept(Component.translatable("error.chk-datapack-link.no_channel_id").getString());
            }
            return;
        }

        if (isConnected(playerUuid)) {
            if (onError != null) {
                onError.accept(Component.translatable("error.chk-datapack-link.already_connected").getString());
            }
            return;
        }

        // 기존 세션이 있다면 정리
        disconnect(playerUuid);

        playerNameMap.put(playerUuid, playerName);

        ChzzkMessageProcessor.MessageHandler handler = new ChzzkMessageProcessor.MessageHandler() {
            @Override
            public void onChatReceived(ChzzkMessageProcessor.ChatData chatData) {
                DatapackIntegrationManager.handleChat(playerUuid, playerName, chatData);
            }

            @Override
            public void onDonationReceived(ChzzkMessageProcessor.DonationData donationData) {
                DatapackIntegrationManager.handleDonation(playerUuid, playerName, donationData);
            }
        };

        ChzzkWebsocketClient client = new ChzzkWebsocketClient(channelId, handler);
        clientMap.put(playerUuid, client);

        client.connectAsync(
                () -> {
                    DatapackIntegrationManager.initializeDefaultStorages();
                    LOGGER.info("Connected to Chzzk for player '{}' ({}) with channel: {}", playerName, playerUuid, channelId);
                    if (onSuccess != null) onSuccess.run();
                },
                (errorMsg) -> {
                    LOGGER.error("Connection failed for player '{}' ({}): {}", playerName, playerUuid, errorMsg);
                    clientMap.remove(playerUuid);
                    if (onError != null) onError.accept(errorMsg);
                }
        );
    }

    /**
     * 명령어(CommandSourceStack)에서 연동을 해제할 때 사용
     */
    public void disconnect(CommandSourceStack source) {
        UUID playerUuid = DEFAULT_SERVER_UUID;
        if (source.getEntity() instanceof ServerPlayer player) {
            playerUuid = player.getUUID();
        }

        disconnect(playerUuid);
        source.sendSuccess(() -> Component.translatable("chat.chk-datapack-link.disconnect_success"), false);
    }

    /**
     * 특정 플레이어의 연동 해제
     */
    public synchronized void disconnect(UUID playerUuid) {
        ChzzkWebsocketClient client = clientMap.remove(playerUuid);
        String playerName = playerNameMap.remove(playerUuid);

        if (client != null) {
            client.disconnect();
            LOGGER.info("Disconnected Chzzk for player '{}' ({})", playerName, playerUuid);
        }
    }

    /**
     * 모든 플레이어 세션 일괄 종료 (서버 종료 시)
     */
    public synchronized void shutdown() {
        LOGGER.info("Shutting down all Chzzk websocket sessions...");
        for (Map.Entry<UUID, ChzzkWebsocketClient> entry : clientMap.entrySet()) {
            if (entry.getValue() != null) {
                entry.getValue().disconnect();
            }
        }
        clientMap.clear();
        playerNameMap.clear();
    }
}
