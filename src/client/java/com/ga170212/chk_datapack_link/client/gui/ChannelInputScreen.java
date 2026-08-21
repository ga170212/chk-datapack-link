package com.ga170212.chk_datapack_link.client.gui;

import com.ga170212.chk_datapack_link.client.ChkDatapackLinkClient;
import com.ga170212.chk_datapack_link.chzzk.ChzzkManager;
import com.ga170212.chk_datapack_link.config.ModConfig;
import com.ga170212.chk_datapack_link.network.ChkLinkNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChannelInputScreen extends Screen {
    private static ChannelInputScreen currentInstance = null;

    public static ChannelInputScreen getCurrentInstance() {
        return currentInstance;
    }

    private final Screen parent;
    private EditBox channelIdField;
    private Button connectButton;
    private StringWidget statusLabel;

    public ChannelInputScreen(Screen parent) {
        super(Component.translatable("gui.chk-datapack-link.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        currentInstance = this;

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Channel ID EditBox
        this.channelIdField = new EditBox(
                this.font,
                centerX - 100, centerY - 25, 200, 20,
                Component.translatable("gui.chk-datapack-link.channel_id_hint")
        );
        this.channelIdField.setMaxLength(64);
        this.channelIdField.setValue(ModConfig.getInstance().getChannelId());
        this.addRenderableWidget(this.channelIdField);

        // Save Button
        Button saveButton = Button.builder(
                Component.translatable("gui.chk-datapack-link.save"),
                button -> {
                    String text = this.channelIdField.getValue().trim();
                    ModConfig.getInstance().setChannelId(text);
                    setStatus(Component.translatable("gui.chk-datapack-link.saved"));
                }
        ).bounds(centerX - 100, centerY + 5, 95, 20).build();
        this.addRenderableWidget(saveButton);

        // Connect/Disconnect Toggle Button
        boolean isConnected = isCurrentlyConnected();
        this.connectButton = Button.builder(
                Component.translatable(isConnected ? "gui.chk-datapack-link.disconnect" : "gui.chk-datapack-link.connect"),
                button -> handleToggleConnect()
        ).bounds(centerX + 5, centerY + 5, 95, 20).build();
        this.addRenderableWidget(this.connectButton);

        // Close Button
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.chk-datapack-link.close"),
                button -> this.onClose()
        ).bounds(centerX - 100, centerY + 35, 200, 20).build());

        // Status Label Widget
        this.statusLabel = new StringWidget(
                centerX - 100, centerY + 65, 200, 20,
                Component.empty(),
                this.font
        );
        this.addRenderableWidget(this.statusLabel);

        this.setInitialFocus(this.channelIdField);
    }

    private boolean isCurrentlyConnected() {
        if (ClientPlayNetworking.canSend(ChkLinkNetwork.TogglePayload.TYPE)) {
            return ChkDatapackLinkClient.isClientConnectedState();
        }
        return ChzzkManager.getInstance().isAnyConnected();
    }

    private void handleToggleConnect() {
        String text = this.channelIdField.getValue().trim();
        ModConfig.getInstance().setChannelId(text);

        boolean currentlyConnected = isCurrentlyConnected();

        // 1. 서버가 C2S 네트워크 패킷을 지원하는 경우 (멀티플레이어 또는 Integrated Server)
        if (ClientPlayNetworking.canSend(ChkLinkNetwork.TogglePayload.TYPE)) {
            setStatus(Component.translatable("gui.chk-datapack-link.connecting"));
            this.connectButton.active = false;
            ClientPlayNetworking.send(new ChkLinkNetwork.TogglePayload(text, !currentlyConnected));
            return;
        }

        // 2. 로컬 직접 호출 Fallback
        if (ChzzkManager.getInstance().isAnyConnected()) {
            ChzzkManager.getInstance().disconnect(ChzzkManager.DEFAULT_SERVER_UUID);
            setStatus(Component.translatable("gui.chk-datapack-link.disconnected"));
            updateButtonText();
        } else {
            setStatus(Component.translatable("gui.chk-datapack-link.connecting"));
            this.connectButton.active = false;

            ChzzkManager.getInstance().connect(
                    ChzzkManager.DEFAULT_SERVER_UUID,
                    "Player",
                    text,
                    () -> {
                        if (Minecraft.getInstance() != null) {
                            Minecraft.getInstance().execute(() -> {
                                setStatus(Component.translatable("gui.chk-datapack-link.connect_success"));
                                if (this.connectButton != null) {
                                    this.connectButton.active = true;
                                }
                                updateButtonText();
                            });
                        }
                    },
                    (errorMsg) -> {
                        if (Minecraft.getInstance() != null) {
                            Minecraft.getInstance().execute(() -> {
                                setStatus(Component.translatable("gui.chk-datapack-link.connect_failed", errorMsg));
                                if (this.connectButton != null) {
                                    this.connectButton.active = true;
                                }
                                updateButtonText();
                            });
                        }
                    }
            );
        }
    }

    public void onServerStatusReceived(boolean connected, Component statusMessage) {
        if (this.connectButton != null) {
            this.connectButton.active = true;
        }
        setStatus(statusMessage);
        updateButtonText();
    }

    private void setStatus(Component message) {
        if (this.statusLabel != null) {
            this.statusLabel.setMessage(message);
            int textWidth = this.font.width(message);
            int centerX = this.width / 2;
            this.statusLabel.setX(centerX - (textWidth / 2));
            this.statusLabel.setWidth(textWidth + 10);
        }
    }

    private void updateButtonText() {
        if (this.connectButton != null) {
            boolean isConnected = isCurrentlyConnected();
            this.connectButton.setMessage(Component.translatable(isConnected ? "gui.chk-datapack-link.disconnect" : "gui.chk-datapack-link.connect"));
        }
    }

    @Override
    public void removed() {
        super.removed();
        if (currentInstance == this) {
            currentInstance = null;
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(this.parent);
    }
}
