package com.example.pigbuilder.net;

import com.example.pigbuilder.PigBuilderMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server to client: turn the 2.5D side view on or off (used by commands). */
public record ModePayload(boolean on) implements CustomPacketPayload {
    public static final Type<ModePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(PigBuilderMod.MODID, "mode"));

    public static final StreamCodec<ByteBuf, ModePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ModePayload::on,
            ModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
