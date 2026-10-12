package com.example.pigbuilder.client;

import com.example.pigbuilder.entity.ContraptionEntity;
import com.example.pigbuilder.entity.ContraptionEntity.Cell;
import com.example.pigbuilder.registry.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

public class ContraptionRenderer extends EntityRenderer<ContraptionEntity, ContraptionRenderState> {
    public ContraptionRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ContraptionRenderState createRenderState() {
        return new ContraptionRenderState();
    }

    @Override
    public void extractRenderState(ContraptionEntity e, ContraptionRenderState s, float partialTick) {
        super.extractRenderState(e, s, partialTick);
        s.cells = e.getCells();
        s.sizeX = e.getSizeX();
        s.sizeZ = e.getSizeZ();
        s.wheelDeg = (float) (-(Mth.lerp(partialTick, e.xo, e.getX()) * 120.0) % 360.0);
        s.propDeg = (e.tickCount + partialTick) * 45f;
        s.spinning = e.isVehicle();
    }

    @Override
    public void submit(ContraptionRenderState s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(s, ps, collector, camera);
        for (Cell c : s.cells) {
            ps.pushPose();
            ps.translate(c.pos().getX() - s.sizeX / 2.0, c.pos().getY(), c.pos().getZ() - s.sizeZ / 2.0);
            if (c.state().is(ModBlocks.WHEEL.get())) {
                spin(ps, Axis.ZP.rotationDegrees(s.wheelDeg));
            } else if (s.spinning && c.state().is(ModBlocks.PROPELLER.get())) {
                spin(ps, Axis.YP.rotationDegrees(s.propDeg));
            }
            collector.submitBlock(ps, c.state(), s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            ps.popPose();
        }
    }

    private static void spin(PoseStack ps, Quaternionf q) {
        ps.translate(0.5, 0.5, 0.5);
        ps.mulPose(q);
        ps.translate(-0.5, -0.5, -0.5);
    }
}
