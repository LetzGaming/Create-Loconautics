package com.lycoris.loconautics.content.conductorshat;

import com.lycoris.loconautics.registry.LoconauticsRegistries;
import net.minecraft.util.RandomSource;
import com.lycoris.loconautics.foundation.ColorOrder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConductorsHats {

    private ConductorsHats() {}

    public static final DyeColor TAB_COLOR = DyeColor.RED;

    public static final Map<DyeColor, DeferredHolder<Item, ConductorsHatItem>> HATS = registerHats();

    public static String hatName(DyeColor color) {
        return color.getSerializedName() + "_conductors_hat";
    }

    public static DyeColor colorFor(long seed) {
        return DyeColor.byId(RandomSource.create(seed).nextInt(DyeColor.values().length));
    }

    public static void init() {}

    private static Map<DyeColor, DeferredHolder<Item, ConductorsHatItem>> registerHats() {
        Map<DyeColor, DeferredHolder<Item, ConductorsHatItem>> hats = new LinkedHashMap<>();
        for (DyeColor color : ColorOrder.ORDER) {
            hats.put(color, LoconauticsRegistries.ITEMS.register(hatName(color), () ->
                    new ConductorsHatItem(new Item.Properties().stacksTo(1), color)));
        }
        return Collections.unmodifiableMap(hats);
    }
}
