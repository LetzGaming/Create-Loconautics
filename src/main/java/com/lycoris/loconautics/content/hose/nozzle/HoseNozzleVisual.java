package com.lycoris.loconautics.content.hose.nozzle;

import com.lycoris.loconautics.client.LoconauticsPartialModels;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.client.renderer.LevelRenderer;

import java.util.function.Consumer;

public class HoseNozzleVisual extends AbstractBlockEntityVisual<HoseNozzleBlockEntity>
        implements SimpleDynamicVisual {

    private final TransformedInstance left;
    private final TransformedInstance right;

    public HoseNozzleVisual(VisualizationContext ctx, HoseNozzleBlockEntity be, float partialTick) {
        super(ctx, be, partialTick);
        left = instance(LoconauticsPartialModels.HOSE_NOZZLE_HANDLEBAR_LEFT);
        right = instance(LoconauticsPartialModels.HOSE_NOZZLE_HANDLEBAR_RIGHT);
        animate(partialTick);
    }

    private TransformedInstance instance(PartialModel model) {
        return instancerProvider()
                .instancer(InstanceTypes.TRANSFORMED, Models.partial(model))
                .createInstance();
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        if (blockEntity.isRemoved() || level.getBlockEntity(pos) != blockEntity) {
            delete();
            return;
        }
        animate(ctx.partialTick());
    }

    private void animate(float partialTick) {
        float fold = blockEntity.getHandlebarFold(partialTick);
        int light = LevelRenderer.getLightColor(level, pos);
        swing(left, HoseNozzleHandlebars.LEFT_PIVOT_X, -HoseNozzleHandlebars.RETRACT_DEGREES * fold, light);
        swing(right, HoseNozzleHandlebars.RIGHT_PIVOT_X, HoseNozzleHandlebars.RETRACT_DEGREES * fold, light);
    }

    private void swing(TransformedInstance instance, float pivotX, float degrees, int light) {
        instance.setIdentityTransform()
                .translate(visualPos.getX(), visualPos.getY(), visualPos.getZ())
                .translate(pivotX, HoseNozzleHandlebars.PIVOT_Y, 0.0f)
                .rotateZDegrees(degrees)
                .translate(-pivotX, -HoseNozzleHandlebars.PIVOT_Y, 0.0f);
        instance.light(light);
        instance.setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        animate(partialTick);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(left);
        consumer.accept(right);
    }

    @Override
    protected void _delete() {
        left.delete();
        right.delete();
    }
}
