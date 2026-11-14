package com.lycoris.loconautics.client.paletteswap;

import com.lycoris.loconautics.LoconauticsConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public record PaletteSwapSource(ResourceLocation source, String from, String target, List<String> colors,
                                double keep, double sat, double gamma) implements SpriteSource {

    public static final String COLOR_TOKEN = "{color}";
    public static final ResourceLocation ID = LoconauticsConstants.id("palette_swap");

    public static final MapCodec<PaletteSwapSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("source").forGetter(PaletteSwapSource::source),
            Codec.STRING.fieldOf("from").forGetter(PaletteSwapSource::from),
            Codec.STRING.flatXmap(PaletteSwapSource::checkTarget, PaletteSwapSource::checkTarget)
                    .fieldOf("target").forGetter(PaletteSwapSource::target),
            Codec.STRING.listOf().optionalFieldOf("colors", List.of()).forGetter(PaletteSwapSource::colors),
            Codec.DOUBLE.optionalFieldOf("keep", PaletteSwap.Knobs.DEFAULT.keep()).forGetter(PaletteSwapSource::keep),
            Codec.DOUBLE.optionalFieldOf("sat", PaletteSwap.Knobs.DEFAULT.sat()).forGetter(PaletteSwapSource::sat),
            Codec.DOUBLE.optionalFieldOf("gamma", PaletteSwap.Knobs.DEFAULT.gamma()).forGetter(PaletteSwapSource::gamma)
    ).apply(instance, PaletteSwapSource::new));

    public static final SpriteSourceType TYPE = new SpriteSourceType(CODEC);

    private static final FileToIdConverter TEXTURES = new FileToIdConverter("textures", ".png");

    private static DataResult<String> checkTarget(String target) {
        if (!target.contains(COLOR_TOKEN)) {
            return DataResult.error(() -> "target needs " + COLOR_TOKEN + " in it, got " + target);
        }
        if (ResourceLocation.tryParse(target.replace(COLOR_TOKEN, "color")) == null) {
            return DataResult.error(() -> "target is not a valid texture id: " + target);
        }
        return DataResult.success(target);
    }

    @Override
    public void run(ResourceManager manager, Output output) {
        Optional<Resource> resource = manager.getResource(TEXTURES.idToFile(source));
        if (resource.isEmpty()) {
            LoconauticsConstants.LOGGER.warn("[PaletteSwap] source texture {} not found, nothing swapped", source);
            return;
        }
        if (isAnimated(resource.get())) {
            LoconauticsConstants.LOGGER.warn("[PaletteSwap] source texture {} is animated, nothing swapped", source);
            return;
        }
        PaletteColors palette = PaletteColors.load(manager);
        PaletteColors.Color origin = palette.get(from);
        if (origin == null) {
            LoconauticsConstants.LOGGER.warn("[PaletteSwap] no color named {} to swap from, {} skipped", from, source);
            return;
        }
        PaletteSwap.Knobs knobs = new PaletteSwap.Knobs(keep, sat, gamma);
        for (PaletteColors.Color color : palette.pick(colors)) {
            if (color.name().equals(from)) {
                continue;
            }
            ResourceLocation id = ResourceLocation.parse(target.replace(COLOR_TOKEN, color.name()));
            output.add(id, loader -> swapped(resource.get(), origin, color, knobs, id));
        }
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }

    private static boolean isAnimated(Resource resource) {
        try {
            return resource.metadata().getSection(AnimationMetadataSection.SERIALIZER).isPresent();
        } catch (IOException e) {
            return false;
        }
    }

    private static SpriteContents swapped(Resource resource, PaletteColors.Color origin, PaletteColors.Color color,
                                          PaletteSwap.Knobs knobs, ResourceLocation id) {
        try (InputStream stream = resource.open(); NativeImage image = NativeImage.read(stream)) {
            NativeImage out = PaletteSwap.recolor(image, origin.ramp(), color.ramp(), knobs);
            return new SpriteContents(id, new FrameSize(out.getWidth(), out.getHeight()), out, ResourceMetadata.EMPTY);
        } catch (IOException e) {
            throw new IllegalStateException("could not read palette swap source for " + id, e);
        }
    }
}
