package com.ga170212.chk_datapack_link.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class ChkLinkNetwork {
    public static final Identifier TOGGLE_ID = Identifier.fromNamespaceAndPath("chk-datapack-link", "toggle");
    public static final Identifier STATUS_ID = Identifier.fromNamespaceAndPath("chk-datapack-link", "status");

    // Client -> Server: 연동 시작/해제 요청
    public record TogglePayload(String channelId, boolean connect) implements CustomPacketPayload {
        public static final Type<TogglePayload> TYPE = new Type<>(TOGGLE_ID);
        public static final StreamCodec<FriendlyByteBuf, TogglePayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeUtf(payload.channelId());
                    buf.writeBoolean(payload.connect());
                },
                buf -> new TogglePayload(buf.readUtf(), buf.readBoolean())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Server -> Client: 연동 상태 및 피드백 응답
    public record StatusPayload(boolean connected, String messageKey, String errorMessage) implements CustomPacketPayload {
        public static final Type<StatusPayload> TYPE = new Type<>(STATUS_ID);
        public static final StreamCodec<FriendlyByteBuf, StatusPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeBoolean(payload.connected());
                    buf.writeUtf(payload.messageKey());
                    buf.writeUtf(payload.errorMessage());
                },
                buf -> new StatusPayload(buf.readBoolean(), buf.readUtf(), buf.readUtf())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void registerCommonPayloads() {
        PayloadTypeRegistry.serverboundPlay().register(TogglePayload.TYPE, TogglePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StatusPayload.TYPE, StatusPayload.CODEC);
    }
}
