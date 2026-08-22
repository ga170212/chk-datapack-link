package com.ga170212.chk_datapack_link.chzzk;

import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChzzkWebsocketClient implements WebSocket.Listener {
    private static final Logger LOGGER = LoggerFactory.getLogger("chk-datapack-link/ChzzkWebsocketClient");

    private final String channelId;
    private final ChzzkMessageProcessor.MessageHandler messageHandler;

    private final HttpClient httpClient;
    private WebSocket webSocket;
    private ScheduledExecutorService pingScheduler;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);

    private final StringBuilder messageBuffer = new StringBuilder();

    public ChzzkWebsocketClient(String channelId, ChzzkMessageProcessor.MessageHandler messageHandler) {
        this.channelId = channelId;
        this.messageHandler = messageHandler;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public synchronized void connectAsync(Runnable onSuccess, Consumer<String> onError) {
        if (isConnected.get() || isConnecting.get()) {
            if (onError != null) onError.accept(Component.translatable("error.chk-datapack-link.already_connected").getString());
            return;
        }

        if (channelId == null || channelId.trim().isEmpty()) {
            if (onError != null) onError.accept(Component.translatable("error.chk-datapack-link.no_channel_id").getString());
            return;
        }

        isConnecting.set(true);

        CompletableFuture.runAsync(() -> {
            try {
                LOGGER.info("Fetching Chzzk channel chat info for channelId: {}", channelId);

                // 1. Fetch live-status to get chatChannelId
                HttpRequest liveStatusReq = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.chzzk.naver.com/polling/v2/channels/" + channelId + "/live-status"))
                        .header("User-Agent", "Mozilla/5.0")
                        .GET()
                        .build();

                HttpResponse<String> liveStatusResp = httpClient.send(liveStatusReq, HttpResponse.BodyHandlers.ofString());
                if (liveStatusResp.statusCode() != 200) {
                    throw new RuntimeException(Component.translatable("error.chk-datapack-link.fetch_channel_failed").getString() + " (HTTP " + liveStatusResp.statusCode() + ")");
                }

                String chatChannelId = extractJsonValue(liveStatusResp.body(), "chatChannelId");
                if (chatChannelId == null || chatChannelId.isEmpty()) {
                    throw new RuntimeException(Component.translatable("error.chk-datapack-link.fetch_channel_failed").getString());
                }

                LOGGER.info("Acquired chatChannelId: {}", chatChannelId);

                // 2. Fetch accessToken via comm-api.game.naver.com using chatChannelId
                HttpRequest tokenReq = HttpRequest.newBuilder()
                        .uri(URI.create("https://comm-api.game.naver.com/nng_main/v1/chats/access-token?channelId=" + chatChannelId + "&chatType=STREAMING"))
                        .header("User-Agent", "Mozilla/5.0")
                        .GET()
                        .build();

                HttpResponse<String> tokenResp = httpClient.send(tokenReq, HttpResponse.BodyHandlers.ofString());
                if (tokenResp.statusCode() != 200) {
                    throw new RuntimeException(Component.translatable("error.chk-datapack-link.fetch_token_failed", tokenResp.statusCode()).getString());
                }

                String accessToken = extractJsonValue(tokenResp.body(), "accessToken");
                if (accessToken == null || accessToken.isEmpty()) {
                    throw new RuntimeException(Component.translatable("error.chk-datapack-link.token_invalid").getString());
                }

                LOGGER.info("Acquired accessToken for chatChannelId: {}", chatChannelId);

                // 3. Connect WebSocket with Origin and User-Agent headers
                String wsUrl = "wss://kr-ss1.chat.naver.com/chat";
                this.webSocket = httpClient.newWebSocketBuilder()
                        .header("Origin", "https://chzzk.naver.com")
                        .header("User-Agent", "Mozilla/5.0")
                        .connectTimeout(Duration.ofSeconds(10))
                        .buildAsync(URI.create(wsUrl), this)
                        .get(15, TimeUnit.SECONDS);

                // Handshake packet (cmd: 100)
                String handshakeJson = String.format(
                        "{\"ver\":\"2\",\"cmd\":100,\"svcid\":\"game\",\"cid\":\"%s\"," +
                        "\"bdy\":{\"uid\":null,\"devType\":2001,\"accTkn\":\"%s\",\"auth\":\"READ\"},\"tid\":1}",
                        chatChannelId, accessToken
                );

                this.webSocket.sendText(handshakeJson, true);
                LOGGER.info("Sent WebSocket handshake packet (cmd 100) for channel {}", channelId);

                // Start Ping Scheduler (cmd: 0 every 20 seconds)
                startPingScheduler();

                isConnected.set(true);
                isConnecting.set(false);

                if (onSuccess != null) onSuccess.run();

            } catch (Exception e) {
                isConnecting.set(false);
                isConnected.set(false);
                LOGGER.error("Chzzk websocket connection failed", e);
                disconnect();
                if (onError != null) {
                    onError.accept(e.getMessage() != null ? e.getMessage() : e.toString());
                }
            }
        });
    }

    private void startPingScheduler() {
        stopPingScheduler();
        pingScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Chzzk-Ping-Thread");
            t.setDaemon(true);
            return t;
        });

        pingScheduler.scheduleAtFixedRate(() -> {
            try {
                if (webSocket != null && isConnected.get()) {
                    webSocket.sendText("{\"ver\":\"2\",\"cmd\":0}", true);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to send ping packet", e);
            }
        }, 15, 20, TimeUnit.SECONDS);
    }

    private void stopPingScheduler() {
        if (pingScheduler != null && !pingScheduler.isShutdown()) {
            pingScheduler.shutdownNow();
            pingScheduler = null;
        }
    }

    public synchronized void disconnect() {
        isConnected.set(false);
        isConnecting.set(false);
        stopPingScheduler();

        if (webSocket != null) {
            try {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Disconnecting");
            } catch (Exception ignored) {
            }
            webSocket = null;
        }

        LOGGER.info("Chzzk websocket client disconnected and resources freed.");
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
        LOGGER.info("Chzzk WebSocket opened.");
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        synchronized (messageBuffer) {
            messageBuffer.append(data);
            if (last) {
                String fullMessage = messageBuffer.toString();
                messageBuffer.setLength(0);

                try {
                    ChzzkMessageProcessor.processPacket(fullMessage, messageHandler);
                } catch (Exception e) {
                    LOGGER.error("Error processing text packet", e);
                }
            }
        }
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        LOGGER.info("Chzzk WebSocket closed with status {} reason: {}", statusCode, reason);
        disconnect();
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        LOGGER.error("Chzzk WebSocket error", error);
        disconnect();
    }

    private static String extractJsonValue(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\":\\s*\"?([^,\"}]+)\"?").matcher(json);
        return m.find() ? m.group(1) : "";
    }
}
