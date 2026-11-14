package com.lycoris.loconautics.content.hose.nozzle;

import com.lycoris.loconautics.client.LoconauticsPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class HoseNozzleRenderer extends SafeBlockEntityRenderer<HoseNozzleBlockEntity> {

    public HoseNozzleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(HoseNozzleBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) return;

        BlockState state = be.getBlockState();
        float fold = be.getHandlebarFold(partialTicks);
        bar(ms, buffer, state, light, LoconauticsPartialModels.HOSE_NOZZLE_HANDLEBAR_LEFT,
                HoseNozzleHandlebars.LEFT_PIVOT_X, -HoseNozzleHandlebars.RETRACT_DEGREES * fold);
        bar(ms, buffer, state, light, LoconauticsPartialModels.HOSE_NOZZLE_HANDLEBAR_RIGHT,
                HoseNozzleHandlebars.RIGHT_PIVOT_X, HoseNozzleHandlebars.RETRACT_DEGREES * fold);
    }

    private static void bar(PoseStack ms, MultiBufferSource buffer, BlockState state, int light,
                            PartialModel model, float pivotX, float degrees) {
        SuperByteBuffer part = CachedBuffers.partial(model, state);
        part.translate(pivotX, HoseNozzleHandlebars.PIVOT_Y, 0.0f)
                .rotateZDegrees(degrees)
                .translate(-pivotX, -HoseNozzleHandlebars.PIVOT_Y, 0.0f);
        part.light(light).renderInto(ms, buffer.getBuffer(RenderType.cutout()));
    }
}
