package com.lycoris.loconautics.common.generated;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lycoris.loconautics.LoconauticsConstants;

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
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorVariantPack implements PackResources {

    private static final String RULES_FILE = "/data/loconautics/color_variants.json";
    private static final String DEFAULT_PARENT = DyeColor.RED.getSerializedName();

    private record FileRule(String path, String parent, List<String> colors) {
    }

    private record TagRule(String path, List<String> values) {
    }

    private static List<FileRule> fileRules;
    private static List<TagRule> tagRules;

    private final PackLocationInfo location;
    private final PackType type;
    private final Map<ResourceLocation, byte[]> files = new HashMap<>();
    private final Set<String> namespaces = new HashSet<>();

    private ColorVariantPack(PackLocationInfo location, PackType type) {
        this.location = location;
        this.type = type;
        loadRules();
        build();
    }

    public static Pack create(PackType type) {
        String id = LoconauticsConstants.MOD_ID + "_color_variants_" + (type == PackType.CLIENT_RESOURCES ? "assets" : "data");
        PackLocationInfo info = new PackLocationInfo(id, Component.literal("Loconautics color variants"),
                PackSource.BUILT_IN, Optional.empty());
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

    private static synchronized void loadRules() {
        if (fileRules != null) {
            return;
        }
        List<FileRule> loadedFiles = new ArrayList<>();
        List<TagRule> loadedTags = new ArrayList<>();
        try (InputStream in = ColorVariantPack.class.getResourceAsStream(RULES_FILE)) {
            if (in == null) {
                LoconauticsConstants.LOGGER.error("[ColorPack] missing {}", RULES_FILE);
            } else {
                try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    for (JsonElement element : root.getAsJsonArray("files")) {
                        JsonObject rule = element.getAsJsonObject();
                        String parent = rule.has("parent") ? rule.get("parent").getAsString() : DEFAULT_PARENT;
                        loadedFiles.add(new FileRule(rule.get("path").getAsString(), parent,
                                rule.has("colors") ? strings(rule.getAsJsonArray("colors")) : otherColors(parent)));
                    }
                    for (JsonElement element : root.getAsJsonArray("tags")) {
                        JsonObject rule = element.getAsJsonObject();
                        loadedTags.add(new TagRule(rule.get("path").getAsString(), strings(rule.getAsJsonArray("values"))));
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            LoconauticsConstants.LOGGER.error("[ColorPack] cannot read {}", RULES_FILE, e);
        }
        fileRules = loadedFiles;
        tagRules = loadedTags;
    }

    private static List<String> strings(JsonArray array) {
        List<String> out = new ArrayList<>();
        array.forEach(element -> out.add(element.getAsString()));
        return out;
    }

    private static List<String> otherColors(String parent) {
        List<String> out = new ArrayList<>();
        for (DyeColor dye : DyeColor.values()) {
            if (!dye.getSerializedName().equals(parent)) {
                out.add(dye.getSerializedName());
            }
        }
        return out;
    }

    private void build() {
        String side = type == PackType.CLIENT_RESOURCES ? "assets/" : "data/";
        for (FileRule rule : fileRules) {
            if (!rule.path().startsWith(side)) {
                continue;
            }
            String template = readTemplate(rule.path().replace("{color}", rule.parent()));
            if (template == null) {
                continue;
            }
            Pattern parentWord = Pattern.compile("(?<![a-z])" + rule.parent() + "(?![a-z])");
            for (String color : rule.colors()) {
                put(rule.path().replace("{color}", color),
                        parentWord.matcher(template).replaceAll(Matcher.quoteReplacement(color)));
            }
        }
        if (type != PackType.SERVER_DATA) {
            return;
        }
        for (TagRule rule : tagRules) {
            JsonArray values = new JsonArray();
            for (String color : otherColors(DEFAULT_PARENT)) {
                rule.values().forEach(value -> values.add(value.replace("{color}", color)));
            }
            JsonObject root = new JsonObject();
            root.add("values", values);
            put(rule.path(), root.toString());
        }
    }

    private static String readTemplate(String path) {
        try (InputStream in = ColorVariantPack.class.getResourceAsStream("/" + path)) {
            if (in == null) {
                LoconauticsConstants.LOGGER.error("[ColorPack] missing template {}", path);
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LoconauticsConstants.LOGGER.error("[ColorPack] cannot read template {}", path, e);
            return null;
        }
    }

    private void put(String path, String text) {
        String[] parts = path.split("/", 3);
        namespaces.add(parts[1]);
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
        return requested == type ? Set.copyOf(namespaces) : Set.of();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        if (!serializer.getMetadataSectionName().equals(PackMetadataSection.TYPE.getMetadataSectionName())) {
            return null;
        }
        return (T) new PackMetadataSection(Component.literal("Generated color variants"),
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
