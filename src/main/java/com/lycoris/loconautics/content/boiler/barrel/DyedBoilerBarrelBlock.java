package com.lycoris.loconautics.content.boiler.barrel;

import com.lycoris.loconautics.common.DyedBlock;
import com.lycoris.loconautics.content.boiler.BoilerBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

public class DyedBoilerBarrelBlock extends BoilerBarrelBlock implements DyedBlock {

    private final String kind;
    private final DyeColor dye;

    public DyedBoilerBarrelBlock(Properties properties, String kind, DyeColor dye) {
        super(properties, dye.getSerializedName(), false);
        this.kind = kind;
        this.dye = dye;
    }

    public String kind() {
        return kind;
    }

    @Override
    public DyeColor getColor() {
        return dye;
    }

    @Override
    public BlockState paintedState(BlockState state, DyeColor dyed) {
        BlockState painted = BoilerBlocks.dyedBody(kind, dyed).defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            painted = copyValue(painted, state, property);
        }
        return painted;
    }

    private static <T extends Comparable<T>> BlockState copyValue(BlockState to, BlockState from, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return simpleCodec(properties -> new DyedBoilerBarrelBlock(properties, kind, dye));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof DyeItem item && BoilerBlocks.dyedBody(kind, item.getDyeColor()) != null) {
            ItemInteractionResult painted = paint(stack, state, level, pos, player);
            if (painted != ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
                return painted;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
}
