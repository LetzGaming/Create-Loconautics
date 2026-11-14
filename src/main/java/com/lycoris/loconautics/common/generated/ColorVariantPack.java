package com.lycoris.loconautics.common.generated;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lycoris.loconautics.LoconauticsConstants;
import com.lycoris.loconautics.content.boiler.BoilerBlocks;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.item.DyeColor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// both sides, only common classes. the client copy serves assets, the server copy serves data.
public final class ColorVariantPack implements PackResources {

    private static final String SOURCE_COLOR = DyeColor.RED.getSerializedName();
    private static final Pattern SOURCE_WORD = Pattern.compile("(?<![a-z])" + SOURCE_COLOR + "(?![a-z])");
    private static final List<String> PIECES = List.of("front", "middle", "back");
    private static final String[] TAGS = {
            "data/loconautics/tags/block/boiler_body.json",
            "data/minecraft/tags/block/mineable/axe.json",
            "data/minecraft/tags/block/mineable/pickaxe.json"};

    private final PackLocationInfo location;
    private final PackType type;
    private final Map<ResourceLocation, byte[]> files = new HashMap<>();

    private ColorVariantPack(PackLocationInfo location, PackType type) {
        this.location = location;
        this.type = type;
        build();
    }

    public static Pack create(PackType type) {
        String id = LoconauticsConstants.MOD_ID + "_color_variants_" + (type == PackType.CLIENT_RESOURCES ? "assets" : "data");
        PackLocationInfo info = new PackLocationInfo(id, Component.literal("Loconautics color variants"),
                PackSource.BUILT_IN, java.util.Optional.empty());
        Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo ignored) {
                return new ColorVariantPack(info, type);
            }

            @Override
            public PackResources openFull(PackLocationInfo ignored, Pack.Metadata metadata) {
                return new ColorVariantPack(info, type);
            }
        };
        return Pack.readMetaAndCreate(info, supplier, type, new PackSelectionConfig(true, Pack.Position.TOP, false));
    }

    private void build() {
        List<DyeColor> colors = new ArrayList<>(BoilerBlocks.BODY_COLORS);
        colors.remove(DyeColor.RED);
        List<String> tagged = new ArrayList<>();
        for (String kind : BoilerBlocks.BODY_KINDS) {
            for (DyeColor dye : colors) {
                String color = dye.getSerializedName();
                String body = BoilerBlocks.bodyName(dye, kind);
                tagged.add(LoconauticsConstants.MOD_ID + ":" + body);
                if (type == PackType.CLIENT_RESOURCES) {
                    swap("assets/loconautics/blockstates/red_" + kind + "_boiler_body.json",
                            "assets/loconautics/blockstates/" + body + ".json", color);
                    swap("assets/loconautics/models/item/red_" + kind + "_boiler_body.json",
                            "assets/loconautics/models/item/" + body + ".json", color);
                    for (String piece : PIECES) {
                        swap("assets/loconautics/models/block/boiler/barrel/red/" + kind + "/" + piece + ".json",
                                "assets/loconautics/models/block/boiler/barrel/" + color + "/" + kind + "/" + piece + ".json", color);
                    }
                } else {
                    swap("data/loconautics/loot_table/blocks/red_" + kind + "_boiler_body.json",
                            "data/loconautics/loot_table/blocks/" + body + ".json", color);
                }
            }
        }
        if (type == PackType.SERVER_DATA) {
            for (String tag : TAGS) {
                JsonArray values = new JsonArray();
                tagged.forEach(values::add);
                JsonObject root = new JsonObject();
                root.add("values", values);
                put(tag, root.toString());
            }
        }
    }

    private void swap(String templatePath, String outPath, String color) {
        try (InputStream in = ColorVariantPack.class.getResourceAsStream("/" + templatePath)) {
            if (in == null) {
                LoconauticsConstants.LOGGER.error("[ColorPack] missing template {}", templatePath);
                return;
            }
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            put(outPath, SOURCE_WORD.matcher(text).replaceAll(Matcher.quoteReplacement(color)));
        } catch (IOException e) {
            LoconauticsConstants.LOGGER.error("[ColorPack] cannot read template {}", templatePath, e);
        }
    }

    private void put(String path, String text) {
        String[] parts = path.split("/", 3);
        files.put(ResourceLocation.fromNamespaceAndPath(parts[1], parts[2]), text.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType requested, ResourceLocation id) {
        byte[] bytes = requested == type ? files.get(id) : null;
        return bytes == null ? null : () -> new ByteArrayInputStream(bytes);
    }

    @Override
    public void listResources(PackType requested, String namespace, String prefix, ResourceOutput output) {
        if (requested != type) {
            return;
        }
        for (Map.Entry<ResourceLocation, byte[]> file : files.entrySet()) {
            ResourceLocation key = file.getKey();
            if (key.getNamespace().equals(namespace) && key.getPath().startsWith(prefix + "/")) {
                byte[] bytes = file.getValue();
                output.accept(key, () -> new ByteArrayInputStream(bytes));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType requested) {
        return requested == type ? Set.of(LoconauticsConstants.MOD_ID, "minecraft") : Set.of();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        if (!serializer.getMetadataSectionName().equals(PackMetadataSection.TYPE.getMetadataSectionName())) {
            return null;
        }
        return (T) new PackMetadataSection(Component.literal("Generated boiler body colors"),
                SharedConstants.getCurrentVersion().getPackVersion(type));
    }

    @Override
    public PackLocationInfo location() {
        return location;
    }

    @Override
    public void close() {
    }
}
