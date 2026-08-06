package com.erneto.client.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class CPPayloads {

    private CPPayloads() {
    }

    public record HandshakeC2S(String modVersion, String modId, int protocol) implements CustomPayload {
        public static final Id<HandshakeC2S> ID = new Id<>(Identifier.of("coreprotect", "handshake"));
        public static final PacketCodec<ByteBuf, HandshakeC2S> CODEC = PacketCodec.tuple(
                PacketCodec.STRING, HandshakeC2S::modVersion,
                PacketCodec.STRING, HandshakeC2S::modId,
                PacketCodec.INTEGER, HandshakeC2S::protocol,
                HandshakeC2S::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record HandshakeS2C(boolean registered) implements CustomPayload {
        public static final Id<HandshakeS2C> ID = new Id<>(Identifier.of("coreprotect", "handshake"));
        public static final PacketCodec<ByteBuf, HandshakeS2C> CODEC = PacketCodec.tuple(
                PacketCodec.of((v, buf) -> buf.writeBoolean(v), buf -> buf.readBoolean()),
                HandshakeS2C::registered,
                HandshakeS2C::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record DataS2C(int type, byte[] raw) implements CustomPayload {
        public static final Id<DataS2C> ID = new Id<>(Identifier.of("coreprotect", "data"));
        public static final PacketCodec<ByteBuf, DataS2C> CODEC = PacketCodec.of(
                (value, buf) -> {
                    buf.writeInt(value.type());
                    buf.writeBytes(value.raw());
                },
                buf -> {
                    int type = buf.readInt();
                    byte[] rest = new byte[buf.readableBytes()];
                    buf.readBytes(rest);
                    return new DataS2C(type, rest);
                }
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}