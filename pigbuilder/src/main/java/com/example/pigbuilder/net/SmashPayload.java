package com.example.pigbuilder.net;

import com.example.pigbuilder.PigBuilderMod;
import com.example.pigbuilder.Smasher;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SmashPayload(int dx, int dy) implements CustomPacketPayload {
    public static final Type<SmashPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(PigBuilderMod.MODID, "smash"));

    public static final StreamCodec<ByteBuf, SmashPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SmashPayload::dx,
            ByteBufCodecs.VAR_INT, SmashPayload::dy,
            SmashPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SmashPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) {
                Smasher.smash(sp, payload.dx(), payload.dy());
            }
        });
    }
}
