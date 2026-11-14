package com.lycoris.loconautics.client.paletteswap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycoris.loconautics.LoconauticsConstants;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
public final class PaletteColors {

    public static final FileToIdConverter FILES = FileToIdConverter.json("palette_swap/colors");

    private static final Pattern NAME = Pattern.compile("[a-z0-9_]+");

    public record Color(String name, int[][] ramp) {
    }

    private final Map<String, Color> colors;

    private PaletteColors(Map<String, Color> colors) {
        this.colors = colors;
    }

    public static PaletteColors load(ResourceManager manager) {
        Map<String, Color> colors = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Resource> file : FILES.listMatchingResources(manager).entrySet()) {
            try (Reader reader = file.getValue().openAsReader()) {
                JsonObject root = GsonHelper.parse(reader);
                for (Map.Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(root, "colors").entrySet()) {
                    readColor(file.getKey(), entry.getKey(), entry.getValue(), colors);
                }
            } catch (IOException | RuntimeException e) {
                LoconauticsConstants.LOGGER.error("[PaletteSwap] bad color file {}: {}", file.getKey(), e.getMessage());
            }
        }
        return new PaletteColors(colors);
    }

    private static void readColor(ResourceLocation file, String name, JsonElement value, Map<String, Color> colors) {
        if (!NAME.matcher(name).matches()) {
            LoconauticsConstants.LOGGER.warn("[PaletteSwap] {} skipped color {}, names are lowercase letters, digits and underscores", file, name);
            return;
        }
        if (colors.containsKey(name)) {
            LoconauticsConstants.LOGGER.warn("[PaletteSwap] {} skipped color {}, already defined", file, name);
            return;
        }
        JsonArray shades = GsonHelper.convertToJsonArray(value, name);
        int[][] ramp = new int[shades.size()][];
        for (int i = 0; i < ramp.length; i++) {
            JsonArray channels = GsonHelper.convertToJsonArray(shades.get(i), name);
            if (channels.size() != 3) {
                throw new IllegalArgumentException("color " + name + " has a shade without three channels");
            }
            ramp[i] = new int[3];
            for (int c = 0; c < 3; c++) {
                ramp[i][c] = GsonHelper.convertToInt(channels.get(c), name);
                if (ramp[i][c] < 0 || ramp[i][c] > 255) {
                    throw new IllegalArgumentException("color " + name + " has a channel outside 0 to 255");
                }
            }
        }
        if (!PaletteSwap.validRamp(ramp)) {
            LoconauticsConstants.LOGGER.warn("[PaletteSwap] {} skipped color {}, a ramp needs two shades or more, dark to light", file, name);
            return;
        }
        colors.put(name, new Color(name, ramp));
    }

    public Color get(String name) {
        return colors.get(name);
    }

    public List<Color> defaults() {
        List<Color> out = new ArrayList<>();
        for (DyeColor dye : DyeColor.values()) {
            Color color = colors.get(dye.getSerializedName());
            if (color != null) {
                out.add(color);
            }
        }
        return out;
    }

    public List<Color> pick(List<String> names) {
        if (names.isEmpty()) {
            return defaults();
        }
        List<Color> out = new ArrayList<>();
        for (String name : names) {
            Color color = colors.get(name);
            if (color == null) {
                LoconauticsConstants.LOGGER.warn("[PaletteSwap] no color named {}, skipped", name);
                continue;
            }
            out.add(color);
        }
        return out;
    }
}
