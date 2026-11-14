package com.lycoris.loconautics.client.paletteswap;

import com.mojang.blaze3d.platform.NativeImage;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public final class PaletteSwap {

    public static final double HUE_TOLERANCE = 20.0;
    public static final double MIN_SATURATION = 0.15;

    public record Knobs(double keep, double sat, double gamma) {
        public static final Knobs DEFAULT = new Knobs(0.8, 1.0, 1.0);
    }

    private PaletteSwap() {
    }

    public static boolean validRamp(int[][] ramp) {
        if (ramp.length < 2) {
            return false;
        }
        for (int i = 1; i < ramp.length; i++) {
            if (brightness(ramp[i]) < brightness(ramp[i - 1])) {
                return false;
            }
        }
        return true;
    }

    public static NativeImage recolor(NativeImage source, int[][] fromRamp, int[][] toRamp, Knobs knobs) {
        List<Integer> shades = findShades(source, fromRamp);
        Map<Integer, Integer> mapping = shadeMap(shades, toRamp, knobs);
        NativeImage out = new NativeImage(NativeImage.Format.RGBA, source.getWidth(), source.getHeight(), false);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int abgr = source.getPixelRGBA(x, y);
                Integer swapped = mapping.get(rgb(abgr));
                out.setPixelRGBA(x, y, swapped == null ? abgr : (abgr & 0xFF000000) | bgr(swapped));
            }
        }
        return out;
    }

    static List<Integer> findShades(NativeImage image, int[][] fromRamp) {
        List<Double> hues = rampHues(fromRamp);
        Set<Integer> found = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int abgr = image.getPixelRGBA(x, y);
                if ((abgr >>> 24) != 0 && belongsToRamp(channels(rgb(abgr)), hues)) {
                    found.add(rgb(abgr));
                }
            }
        }
        List<Integer> sorted = new ArrayList<>(found);
        sorted.sort((a, b) -> Double.compare(brightness(channels(a)), brightness(channels(b))));
        return sorted;
    }

    static Map<Integer, Integer> shadeMap(List<Integer> shades, int[][] ramp, Knobs knobs) {
        Map<Integer, Integer> mapping = new HashMap<>();
        if (shades.isEmpty()) {
            return mapping;
        }
        double[] positions = rampPositions(shades);
        double[] ownValues = new double[shades.size()];
        double lowest = Double.MAX_VALUE;
        double highest = -Double.MAX_VALUE;
        for (int i = 0; i < ownValues.length; i++) {
            ownValues[i] = hsv(channels(shades.get(i)))[2];
            lowest = Math.min(lowest, ownValues[i]);
            highest = Math.max(highest, ownValues[i]);
        }
        double rampCenter = (hsv(ramp[0])[2] + hsv(ramp[ramp.length - 1])[2]) / 2.0;
        double ownCenter = (lowest + highest) / 2.0;
        double scale = contrastFit(highest - lowest, rampCenter);

        for (int i = 0; i < ownValues.length; i++) {
            double[] target = hsv(sample(ramp, Math.pow(positions[i], knobs.gamma())));
            double kept = rampCenter + (ownValues[i] - ownCenter) * scale;
            double value = Math.min(1.0, Math.max(0.0, target[2] + (kept - target[2]) * knobs.keep()));
            double saturation = Math.min(1.0, target[1] * knobs.sat());
            double[] rgb = hsvToRgb(target[0], saturation, value);
            mapping.put(shades.get(i), pack((int) Math.rint(rgb[0] * 255), (int) Math.rint(rgb[1] * 255),
                    (int) Math.rint(rgb[2] * 255)));
        }
        return mapping;
    }

    private static double contrastFit(double span, double center) {
        if (span <= 0.0) {
            return 1.0;
        }
        return Math.min(1.0, Math.min(center, 1.0 - center) * 2.0 / span);
    }

    private static double[] rampPositions(List<Integer> shades) {
        double[] positions = new double[shades.size()];
        if (shades.size() == 1) {
            positions[0] = 0.5;
            return positions;
        }
        double low = brightness(channels(shades.get(0)));
        double high = brightness(channels(shades.get(shades.size() - 1)));
        for (int i = 0; i < positions.length; i++) {
            positions[i] = high - low < 1.0
                    ? (double) i / (positions.length - 1)
                    : (brightness(channels(shades.get(i))) - low) / (high - low);
        }
        return positions;
    }

    private static int[] sample(int[][] ramp, double position) {
        double spot = position * (ramp.length - 1);
        int low = (int) spot;
        int high = Math.min(low + 1, ramp.length - 1);
        double blend = spot - low;
        int[] out = new int[3];
        for (int c = 0; c < 3; c++) {
            out[c] = (int) Math.rint(ramp[low][c] + (ramp[high][c] - ramp[low][c]) * blend);
        }
        return out;
    }

    private static List<Double> rampHues(int[][] ramp) {
        List<Double> hues = new ArrayList<>();
        for (int[] shade : ramp) {
            double[] hsv = hsv(shade);
            if (hsv[1] >= MIN_SATURATION) {
                hues.add(hsv[0] * 360.0);
            }
        }
        return hues;
    }

    private static boolean belongsToRamp(int[] rgb, List<Double> hues) {
        double[] hsv = hsv(rgb);
        if (hues.isEmpty()) {
            return hsv[1] < MIN_SATURATION;
        }
        if (hsv[1] < MIN_SATURATION) {
            return false;
        }
        for (double reference : hues) {
            double gap = Math.abs(hsv[0] * 360.0 - reference) % 360.0;
            if (Math.min(gap, 360.0 - gap) <= HUE_TOLERANCE) {
                return true;
            }
        }
        return false;
    }

    private static double brightness(int[] rgb) {
        return rgb[0] * 0.299 + rgb[1] * 0.587 + rgb[2] * 0.114;
    }

    private static double[] hsv(int[] rgb) {
        double r = rgb[0] / 255.0;
        double g = rgb[1] / 255.0;
        double b = rgb[2] / 255.0;
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        if (min == max) {
            return new double[]{0.0, 0.0, max};
        }
        double span = max - min;
        double rc = (max - r) / span;
        double gc = (max - g) / span;
        double bc = (max - b) / span;
        double hue;
        if (r == max) {
            hue = bc - gc;
        } else if (g == max) {
            hue = 2.0 + rc - bc;
        } else {
            hue = 4.0 + gc - rc;
        }
        hue = (hue / 6.0) % 1.0;
        if (hue < 0.0) {
            hue += 1.0;
        }
        return new double[]{hue, span / max, max};
    }

    private static double[] hsvToRgb(double h, double s, double v) {
        if (s == 0.0) {
            return new double[]{v, v, v};
        }
        int i = (int) (h * 6.0);
        double f = h * 6.0 - i;
        double p = v * (1.0 - s);
        double q = v * (1.0 - s * f);
        double t = v * (1.0 - s * (1.0 - f));
        return switch (i % 6) {
            case 0 -> new double[]{v, t, p};
            case 1 -> new double[]{q, v, p};
            case 2 -> new double[]{p, v, t};
            case 3 -> new double[]{p, q, v};
            case 4 -> new double[]{t, p, v};
            default -> new double[]{v, p, q};
        };
    }

    private static int rgb(int abgr) {
        return pack(abgr & 0xFF, (abgr >> 8) & 0xFF, (abgr >> 16) & 0xFF);
    }

    private static int bgr(int rgb) {
        return ((rgb & 0xFF) << 16) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF);
    }

    private static int pack(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    private static int[] channels(int rgb) {
        return new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
    }
}
