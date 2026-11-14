package com.lycoris.loconautics.content.variantpicker;

import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class BlockVariantPickerData {

    public final Block targetBlock;
    public final InteractionHand hand;
    public final int familyIndex;
    public final List<BlockVariantRegistry.VariantType> availableTypes;

    public Block[] colorVariants = new Block[BlockVariantRegistry.COLOR_COUNT];

    public int hoveredTypeIndex;
    public int hoveredColorIndex;
    public int hoveredMaterialIndex = -1;
    public int confirmedTypeIndex;
    public int confirmedColorIndex;
    public int confirmedMaterialIndex = -1;

    public boolean open = true;
    public int openTicks = 0;

    public final LerpedFloat panelAlpha = LerpedFloat.linear().startWithValue(0);
    public final LerpedFloat panelScale = LerpedFloat.linear().startWithValue(0.85);

    public final LerpedFloat[] appearScale;
    public final LerpedFloat[] hoverScale;

    public final LerpedFloat highlightTypeX = LerpedFloat.linear();
    public final LerpedFloat highlightColorX = LerpedFloat.linear();
    public final LerpedFloat highlightColorY = LerpedFloat.linear();

    public final LerpedFloat colorGridAlpha = LerpedFloat.linear().startWithValue(1);

    public final LerpedFloat nameAlpha = LerpedFloat.linear().startWithValue(0);

    public final LerpedFloat tooltipAlpha = LerpedFloat.linear().startWithValue(0);
    public final LerpedFloat tooltipOffset = LerpedFloat.linear().startWithValue(4);

    public BlockVariantPickerData(Block targetBlock, InteractionHand hand,
                                  BlockVariantRegistry.Location loc) {
        this.targetBlock = targetBlock;
        this.hand = hand;
        this.familyIndex = loc.family();
        this.availableTypes = BlockVariantRegistry.family(loc.family());
        this.confirmedTypeIndex = loc.type();
        this.confirmedColorIndex = loc.color();
        this.confirmedMaterialIndex = loc.material();
        this.hoveredTypeIndex = loc.type();
        this.hoveredColorIndex = loc.color();
        this.hoveredMaterialIndex = loc.material();

        int slots = availableTypes.size() + BlockVariantRegistry.COLOR_COUNT + BlockVariantRegistry.MAX_WRAPPINGS;
        appearScale = new LerpedFloat[slots];
        hoverScale = new LerpedFloat[slots];
        for (int i = 0; i < slots; i++) {
            appearScale[i] = LerpedFloat.linear().startWithValue(0.6);
            hoverScale[i] = LerpedFloat.linear().startWithValue(1.0);
        }
        rebuildColorVariants();
    }

    public int typeCount() {
        return availableTypes.size();
    }

    public int materialCount() {
        return availableTypes.get(confirmedTypeIndex).materialCount();
    }

    public boolean hasWrappings() {
        return availableTypes.get(confirmedTypeIndex).hasWrappings();
    }

    public Block wrappedVariant(int m) {
        return availableTypes.get(confirmedTypeIndex).wrappedAt(confirmedColorIndex, m);
    }

    public int colorSlotIndex(int c) {
        return typeCount() + c;
    }

    public int wrappingSlotIndex(int m) {
        return typeCount() + BlockVariantRegistry.COLOR_COUNT + m;
    }

    public void rebuildColorVariants() {
        BlockVariantRegistry.VariantType type = availableTypes.get(confirmedTypeIndex);
        for (int c = 0; c < BlockVariantRegistry.COLOR_COUNT; c++) {
            colorVariants[c] = type.at(c);
        }
    }

    public void clampMaterial() {
        if (confirmedMaterialIndex >= 0 && wrappedVariant(confirmedMaterialIndex) == null) {
            confirmedMaterialIndex = -1;
        }
        hoveredMaterialIndex = confirmedMaterialIndex;
    }

    private Block resolve(int typeIndex, int colorIndex, int materialIndex) {
        BlockVariantRegistry.VariantType type = availableTypes.get(typeIndex);
        if (materialIndex >= 0) {
            Block w = type.wrappedAt(colorIndex, materialIndex);
            if (w != null) return w;
        }
        return type.at(colorIndex);
    }

    public Block hoveredBlock() {
        return resolve(hoveredTypeIndex, hoveredColorIndex, hoveredMaterialIndex);
    }

    public Block confirmedBlock() {
        return resolve(confirmedTypeIndex, confirmedColorIndex, confirmedMaterialIndex);
    }
}
