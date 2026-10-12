package com.example.pigbuilder.net;

import com.example.pigbuilder.PigBuilderMod;
import com.example.pigbuilder.entity.ContraptionEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Driver input for a ContraptionEntity. move: +1 = +X, -1 = -X. */
public record ControlPayload(int move, boolean up) implements CustomPacketPayload {
    public static final Type<ControlPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(PigBuilderMod.MODID, "control"));

    public static final StreamCodec<ByteBuf, ControlPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ControlPayload::move,
            ByteBufCodecs.BOOL, ControlPayload::up,
            ControlPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ControlPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player().getVehicle() instanceof ContraptionEntity c) {
                c.setControls(Mth.clamp(payload.move(), -1, 1), payload.up());
            }
        });
    }
}
