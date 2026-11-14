package com.lycoris.loconautics.foundation;

import net.minecraft.world.item.DyeColor;

import java.util.List;

public final class ColorOrder {

    public static final List<DyeColor> ORDER = List.of(
            DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.BLACK,
            DyeColor.BROWN, DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW,
            DyeColor.LIME, DyeColor.GREEN, DyeColor.CYAN, DyeColor.LIGHT_BLUE,
            DyeColor.BLUE, DyeColor.PURPLE, DyeColor.MAGENTA, DyeColor.PINK);

    private ColorOrder() {
    }

    public static int indexOf(DyeColor color) {
        return ORDER.indexOf(color);
    }

    public static DyeColor at(int index) {
        return ORDER.get(index);
    }
}
