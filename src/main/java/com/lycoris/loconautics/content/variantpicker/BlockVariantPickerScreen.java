package com.lycoris.loconautics.content.variantpicker;

import com.lycoris.loconautics.network.packets.variantpicker.BlockVariantApplyPacket;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class BlockVariantPickerScreen extends Screen {

    static final int SLOT = 22;
    static final int GAP = 6;
    static final int STRIDE = SLOT + GAP;
    static final int PAD = 14;
    static final int COLS = 6;

    private static final int TINT = 0xD8060606;
    private static final int NAME_DOWN = 16;

    private static final int[] COLOR_ORDER = {15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 16};

    private final BlockVariantPickerData d;
    private int hoveredFlatSlot = -1;

    public BlockVariantPickerScreen(Block targetBlock, InteractionHand hand,
                                    BlockVariantRegistry.Location loc) {
        super(Component.translatable("gui.loconautics.variant_picker"));
        this.d = new BlockVariantPickerData(targetBlock, hand, loc);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    static final class Layout {
        int panelX, panelY, panelW, panelH;
        int centerX;
        int headerY;
        int typeRowY;
        int[][] typeSlots;
        int nameY;
        int dividerY;
        int[] presentColors;
        int[][] colorSlots;
        int wrapRowY;
        int[][] wrapSlots;
        int hintY;
    }

    private int[] presentColors() {
        List<Integer> out = new ArrayList<>();
        for (int ci : COLOR_ORDER) {
            if (d.colorVariants[ci] != null) out.add(ci);
        }
        int[] arr = new int[out.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = out.get(i);
        return arr;
    }

    Layout layout() {
        Layout l = new Layout();
        int n = d.typeCount();
        int mats = d.materialCount();
        l.presentColors = presentColors();
        int p = l.presentColors.length;
        int colorRows = Math.max(1, (p + COLS - 1) / COLS);

        int typeRowW = n * STRIDE - GAP;
        int colorRowW = Math.min(COLS, Math.max(1, p)) * STRIDE - GAP;
        int wrapRowW = mats > 0 ? mats * STRIDE - GAP : 0;
        int contentW = Math.max(Math.max(typeRowW, colorRowW), wrapRowW);

        int headerH = 16;
        int gapHeaderType = 6;
        int typeRowH = SLOT;
        int nameH = 14 + NAME_DOWN;
        int gapBeforeDivider = 12;
        int dividerToColor = 18;
        int colorH = colorRows * STRIDE - GAP;
        int wrapGap = mats > 0 ? 16 : 0;
        int wrapH = mats > 0 ? SLOT : 0;
        int hintGap = 12;
        int hintH = 12;

        int contentH = headerH + gapHeaderType + typeRowH + nameH
                + gapBeforeDivider + dividerToColor + colorH + wrapGap + wrapH + hintGap + hintH;
        l.panelW = contentW + PAD * 2;
        l.panelH = contentH + PAD * 2;
        l.centerX = this.width / 2;
        l.panelX = l.centerX - l.panelW / 2;
        l.panelY = this.height / 2 - l.panelH / 2;

        int y = l.panelY + PAD;
        l.headerY = y;
        y += headerH + gapHeaderType;

        l.typeRowY = y;
        l.typeSlots = new int[n][2];
        int typeStartX = l.centerX - typeRowW / 2;
        for (int i = 0; i < n; i++) {
            l.typeSlots[i][0] = typeStartX + i * STRIDE;
            l.typeSlots[i][1] = y;
        }
        y += typeRowH;

        l.nameY = y;
        y += nameH + gapBeforeDivider;

        l.dividerY = y;
        y += dividerToColor;

        l.colorSlots = new int[p][2];
        for (int i = 0; i < p; i++) {
            int row = i / COLS;
            int col = i % COLS;
            int countInRow = Math.min(COLS, p - row * COLS);
            int rowStartX = l.centerX - (countInRow * STRIDE - GAP) / 2;
            l.colorSlots[i][0] = rowStartX + col * STRIDE;
            l.colorSlots[i][1] = y + row * STRIDE;
        }
        y += colorH;

        l.wrapSlots = new int[mats][2];
        if (mats > 0) {
            y += wrapGap;
            l.wrapRowY = y;
            int wrapStartX = l.centerX - wrapRowW / 2;
            for (int m = 0; m < mats; m++) {
                l.wrapSlots[m][0] = wrapStartX + m * STRIDE;
                l.wrapSlots[m][1] = y;
            }
            y += wrapH;
        }

        y += hintGap;
        l.hintY = y;
        return l;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float ignored) {
        graphics.fill(0, 0, this.width, this.height, TINT);

        float partialTicks = AnimationTickHolder.getPartialTicks();

        Layout l = layout();
        updateHover(l, mouseX, mouseY);

        float alpha = d.panelAlpha.getValue(partialTicks);
        float scale = d.panelScale.getValue(partialTicks);

        PoseStack ms = graphics.pose();
        ms.pushPose();
        float cx = l.panelX + l.panelW / 2f;
        float cy = l.panelY + l.panelH / 2f;
        ms.translate(cx, cy, 0);
        ms.scale(scale, scale, 1);
        ms.translate(-cx, -cy, 0);

        Component current = d.targetBlock.getName();
        graphics.drawString(font, current, l.centerX - font.width(current) / 2, l.headerY,
                withAlpha(0xFFFFFF, alpha), true);

        for (int i = 0; i < d.typeCount(); i++) {
            Block b = d.availableTypes.get(i).at(d.confirmedColorIndex);
            if (b == null) b = d.availableTypes.get(i).at(BlockVariantRegistry.COLORLESS_INDEX);
            float f = (i == d.confirmedTypeIndex) ? 1f
                    : d.appearScale[i].getValue(partialTicks) * d.hoverScale[i].getValue(partialTicks);
            drawSlot(graphics, l.typeSlots[i][0], l.typeSlots[i][1], b, f, alpha);
        }
        drawHighlight(graphics, l.typeSlots[d.confirmedTypeIndex][0], l.typeRowY, alpha);

        Block to = d.hoveredBlock();
        if (to == null) to = d.confirmedBlock();
        if (to != null) {
            Component changing = to.getName();
            graphics.drawString(font, changing, l.centerX - font.width(changing) / 2, l.nameY + NAME_DOWN,
                    withAlpha(0xFFD700, alpha), true);
        }

        graphics.fill(l.panelX + PAD, l.dividerY, l.panelX + l.panelW - PAD, l.dividerY + 1,
                withAlpha(0x555555, alpha));

        for (int i = 0; i < l.presentColors.length; i++) {
            int ci = l.presentColors[i];
            int slotIdx = d.colorSlotIndex(ci);
            float f = (ci == d.confirmedColorIndex) ? 1f
                    : d.appearScale[slotIdx].getValue(partialTicks) * d.hoverScale[slotIdx].getValue(partialTicks);
            drawSlot(graphics, l.colorSlots[i][0], l.colorSlots[i][1], d.colorVariants[ci], f, alpha);
        }
        int hd = displayOfColor(l, d.confirmedColorIndex);
        if (hd >= 0) drawHighlight(graphics, l.colorSlots[hd][0], l.colorSlots[hd][1], alpha);

        for (int m = 0; m < d.materialCount(); m++) {
            Block w = d.wrappedVariant(m);
            int slotIdx = d.wrappingSlotIndex(m);
            float f = (m == d.confirmedMaterialIndex) ? 1f
                    : d.appearScale[slotIdx].getValue(partialTicks) * d.hoverScale[slotIdx].getValue(partialTicks);
            drawSlot(graphics, l.wrapSlots[m][0], l.wrapSlots[m][1], w, f, alpha);
        }
        if (d.confirmedMaterialIndex >= 0 && d.confirmedMaterialIndex < d.materialCount()) {
            drawHighlight(graphics, l.wrapSlots[d.confirmedMaterialIndex][0], l.wrapSlots[d.confirmedMaterialIndex][1], alpha);
        }

        Component hint = Component.translatable("gui.loconautics.variant_picker.hint",
                ScrollPickerHandler.OPEN_KEY.getTranslatedKeyMessage());
        graphics.drawString(font, hint, l.centerX - font.width(hint) / 2, l.hintY,
                withAlpha(0xBBBBBB, alpha), true);

        ms.popPose();
    }

    private static int displayOfColor(Layout l, int ci) {
        for (int i = 0; i < l.presentColors.length; i++) {
            if (l.presentColors[i] == ci) return i;
        }
        return -1;
    }

    private void drawSlot(GuiGraphics graphics, int x, int y, Block block, float factor, float alpha) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(x + SLOT / 2f, y + SLOT / 2f, 0);
        ms.scale(factor, factor, 1);
        ms.translate(-(x + SLOT / 2f), -(y + SLOT / 2f), 0);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        if (block == null) {
            AllGuiTextures.TOOLBELT_INACTIVE_SLOT.render(graphics, x, y);
        } else {
            AllGuiTextures.TOOLBELT_SLOT.render(graphics, x, y);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (block != null) {
            graphics.renderItem(new ItemStack(block), x + 3, y + 3);
        }
        ms.popPose();
    }

    private void drawHighlight(GuiGraphics graphics, int x, int y, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, x - 1, y - 1);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private void updateHover(Layout l, int mouseX, int mouseY) {
        d.hoveredTypeIndex = d.confirmedTypeIndex;
        d.hoveredColorIndex = d.confirmedColorIndex;
        d.hoveredMaterialIndex = d.confirmedMaterialIndex;
        int flat = -1;

        for (int i = 0; i < d.typeCount(); i++) {
            if (isOver(l.typeSlots[i][0], l.typeSlots[i][1], mouseX, mouseY)) {
                d.hoveredTypeIndex = i;
                flat = i;
                break;
            }
        }
        for (int i = 0; i < l.presentColors.length; i++) {
            int ci = l.presentColors[i];
            if (isOver(l.colorSlots[i][0], l.colorSlots[i][1], mouseX, mouseY)) {
                d.hoveredColorIndex = ci;
                flat = d.colorSlotIndex(ci);
                break;
            }
        }
        for (int m = 0; m < d.materialCount(); m++) {
            if (d.wrappedVariant(m) != null && isOver(l.wrapSlots[m][0], l.wrapSlots[m][1], mouseX, mouseY)) {
                d.hoveredMaterialIndex = m;
                flat = d.wrappingSlotIndex(m);
                break;
            }
        }
        hoveredFlatSlot = flat;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Layout l = layout();
            for (int i = 0; i < d.typeCount(); i++) {
                if (isOver(l.typeSlots[i][0], l.typeSlots[i][1], mouseX, mouseY)) {
                    selectType(i);
                    return true;
                }
            }
            for (int i = 0; i < l.presentColors.length; i++) {
                int ci = l.presentColors[i];
                if (isOver(l.colorSlots[i][0], l.colorSlots[i][1], mouseX, mouseY)) {
                    selectColor(ci);
                    return true;
                }
            }
            for (int m = 0; m < d.materialCount(); m++) {
                if (d.wrappedVariant(m) != null && isOver(l.wrapSlots[m][0], l.wrapSlots[m][1], mouseX, mouseY)) {
                    selectMaterial(m);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int n = d.typeCount();
        int dir = scrollY > 0 ? -1 : 1;
        selectType(((d.confirmedTypeIndex + dir) % n + n) % n);
        return true;
    }

    private void selectType(int i) {
        d.confirmedTypeIndex = i;
        d.hoveredTypeIndex = i;
        d.rebuildColorVariants();
        ensureColorSupported();
        d.clampMaterial();
    }

    private void selectColor(int i) {
        d.confirmedColorIndex = i;
        d.hoveredColorIndex = i;
        d.clampMaterial();
    }

    private void selectMaterial(int m) {
        d.confirmedMaterialIndex = (d.confirmedMaterialIndex == m) ? -1 : m;
        d.hoveredMaterialIndex = d.confirmedMaterialIndex;
    }

    private void ensureColorSupported() {
        if (d.colorVariants[d.confirmedColorIndex] != null) return;
        for (int c = 0; c < BlockVariantRegistry.COLOR_COUNT; c++) {
            if (d.colorVariants[c] != null) {
                d.confirmedColorIndex = c;
                d.hoveredColorIndex = c;
                return;
            }
        }
    }

    @Override
    public void tick() {
        if (!altDown()) {
            confirm();
            onClose();
            return;
        }
        animate();
        d.openTicks++;
    }

    private boolean altDown() {
        long win = minecraft.getWindow().getWindow();
        return InputConstants.isKeyDown(win, ScrollPickerHandler.OPEN_KEY.getKey().getValue());
    }

    private void confirm() {
        Block chosen = d.confirmedBlock();
        if (chosen != null && chosen != d.targetBlock) {
            CatnipServices.NETWORK.sendToServer(
                    new BlockVariantApplyPacket(d.hand, BuiltInRegistries.BLOCK.getKey(chosen)));
        }
    }

    private void animate() {
        d.panelAlpha.chase(1.0, 0.6, Chaser.EXP);
        d.panelScale.chase(1.0, 0.6, Chaser.EXP);
        d.panelAlpha.tickChaser();
        d.panelScale.tickChaser();

        int n = d.typeCount();
        for (int i = 0; i < n; i++) {
            double threshold = Math.abs(i - (n - 1) / 2.0) * 0.15;
            tickAppear(d.appearScale[i], threshold);
            tickHover(d.hoverScale[i], i == hoveredFlatSlot);
        }
        for (int i = 0; i < BlockVariantRegistry.COLOR_COUNT; i++) {
            int flat = d.colorSlotIndex(i);
            double col = i % COLS;
            double row = i / COLS;
            double dist = Math.hypot(col - (COLS - 1) / 2.0, row - 1.0);
            tickAppear(d.appearScale[flat], dist * 0.15);
            tickHover(d.hoverScale[flat], flat == hoveredFlatSlot);
        }
        for (int m = 0; m < d.materialCount(); m++) {
            int flat = d.wrappingSlotIndex(m);
            tickAppear(d.appearScale[flat], m * 0.1);
            tickHover(d.hoverScale[flat], flat == hoveredFlatSlot);
        }
    }

    private void tickAppear(LerpedFloat slot, double threshold) {
        if (d.openTicks >= threshold) {
            slot.chase(1.0, 0.6, Chaser.EXP);
        }
        slot.tickChaser();
    }

    private void tickHover(LerpedFloat slot, boolean hovered) {
        slot.chase(hovered ? 1.1 : 1.0, 0.6, Chaser.EXP);
        slot.tickChaser();
    }

    private boolean isOver(int x, int y, double mx, double my) {
        return mx >= x && mx < x + SLOT && my >= y && my < y + SLOT;
    }

    private static int withAlpha(int rgb, float a) {
        int na = Math.max(0, Math.min(255, Math.round(255 * a)));
        return (na << 24) | (rgb & 0xFFFFFF);
    }
}
