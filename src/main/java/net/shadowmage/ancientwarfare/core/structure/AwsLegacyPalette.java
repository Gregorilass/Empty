package net.shadowmage.ancientwarfare.core.structure;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conservative, Minecraft-independent conversion of 1.12-era structure rule metadata.
 * Returns empty for plugins that need tile/entity/NBT handling not yet implemented.
 * No regex attempts to parse arbitrary SNBT beyond the blockState section.
 */
public final class AwsLegacyPalette {
    private static final Pattern BLOCK_NAME = Pattern.compile("blockName:\\s*\"([a-z0-9_.-]+:[a-z0-9_./-]+)\"");
    private static final Pattern PROPERTY = Pattern.compile("([a-zA-Z0-9_]+):\\s*\"([^\"]*)\"");
    private static final String[] WOODS = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"};
    private static final String[] COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};

    private AwsLegacyPalette() {}

    public record State(String id, Map<String, String> properties) {
        public State { properties = Collections.unmodifiableMap(new LinkedHashMap<>(properties)); }
    }

    public static Optional<State> convert(AwsTemplateParser.Rule rule) {
        // These formats can contain inventories, NBT, special rendering, or other semantics.
        // Never silently erase those structures in an allegedly lossless placement.
        if (!switch (rule.plugin()) {
            case "vanillaBlocks", "doors", "fluid", "vine" -> true;
            default -> false;
        }) return Optional.empty();

        Matcher bm = BLOCK_NAME.matcher(rule.rawData());
        if (!bm.find()) return Optional.empty();
        String id = bm.group(1).toLowerCase(Locale.ROOT);
        Map<String, String> props = new LinkedHashMap<>();
        int begin = rule.rawData().indexOf("properties:{", bm.end());
        if (begin >= 0) {
            int end = rule.rawData().indexOf('}', begin + 12);
            if (end < 0) return Optional.empty();
            Matcher pm = PROPERTY.matcher(rule.rawData().substring(begin + 12, end));
            while (pm.find()) props.put(pm.group(1).toLowerCase(Locale.ROOT), pm.group(2).toLowerCase(Locale.ROOT));
        }
        if (!id.startsWith("minecraft:")) return Optional.empty();

        String kind = id.substring(10);
        String variant = props.remove("variant");
        switch (kind) {
            case "grass" -> id = "minecraft:grass_block";
            case "dirt" -> id = "minecraft:" + switch (variant == null ? "" : variant) {
                case "coarse_dirt" -> "coarse_dirt";
                case "podzol" -> "podzol";
                default -> "dirt";
            };
            case "stone" -> id = "minecraft:" + switch (variant == null ? "" : variant) {
                case "granite", "polished_granite", "diorite", "polished_diorite", "andesite", "polished_andesite" -> variant;
                default -> "stone";
            };
            case "planks" -> id = "minecraft:" + wood(variant, false) + "_planks";
            case "log", "log2" -> {
                id = "minecraft:" + wood(variant, kind.equals("log2")) + "_log";
                String axis = props.get("axis");
                if ("none".equals(axis)) { id = id.replace("_log", "_wood"); props.remove("axis"); }
            }
            case "leaves", "leaves2" -> { id = "minecraft:" + wood(variant, kind.equals("leaves2")) + "_leaves"; props.remove("check_decay"); props.remove("decayable"); }
            case "stained_hardened_clay" -> id = "minecraft:" + color(props.remove("color")) + "_terracotta";
            case "stained_glass", "stained_glass_pane", "wool", "carpet", "concrete", "concrete_powder" -> {
                String base = kind;
                id = "minecraft:" + color(props.remove("color")) + "_" + base;
            }
            case "stonebrick" -> id = "minecraft:" + switch (variant == null ? "" : variant) {
                case "mossy_stonebrick" -> "mossy_stone_bricks";
                case "cracked_stonebrick" -> "cracked_stone_bricks";
                case "chiseled_stonebrick" -> "chiseled_stone_bricks";
                default -> "stone_bricks";
            };
            case "brick_block" -> id = "minecraft:bricks";
            case "wooden_door" -> id = "minecraft:oak_door";
            case "iron_door" -> id = "minecraft:iron_door";
            case "sandstone", "red_sandstone" -> {
                String prefix = kind.equals("sandstone") ? "sandstone" : "red_sandstone";
                String type = props.remove("type");
                id = "minecraft:" + switch (type == null ? "" : type) {
                    case "smooth_sandstone", "smooth_red_sandstone" -> "smooth_" + prefix;
                    case "chiseled_sandstone", "chiseled_red_sandstone" -> "chiseled_" + prefix;
                    default -> prefix;
                };
            }
            case "flowing_water" -> id = "minecraft:water";
            case "flowing_lava" -> id = "minecraft:lava";
            case "lit_furnace" -> id = "minecraft:furnace";
            case "lit_pumpkin" -> id = "minecraft:jack_o_lantern";
            case "web" -> id = "minecraft:cobweb";
            case "snow_layer" -> id = "minecraft:snow";
            case "mob_spawner" -> id = "minecraft:spawner";
            case "reeds" -> id = "minecraft:sugar_cane";
            case "double_plant" -> {
                String type = props.remove("variant");
                if (type == null) type = variant;
                id = "minecraft:" + switch (type == null ? "" : type) {
                    case "rose", "rose_bush" -> "rose_bush";
                    case "syringa", "lilac" -> "lilac";
                    case "paeonia", "peony" -> "peony";
                    case "sunflower" -> "sunflower";
                    case "double_grass", "tall_grass" -> "tall_grass";
                    case "double_fern", "large_fern" -> "large_fern";
                    default -> "air";
                };
            }
            default -> { /* 1.12 and current identifiers are identical for many blocks. */ }
        }
        if ("minecraft:air".equals(id) && kind.equals("double_plant")) return Optional.empty();
        // Properties that did not exist or were renamed are never injected blindly.
        props.remove("legacy_data");
        props.remove("nodrop");
        if ((kind.equals("water") || kind.equals("flowing_water") || kind.equals("lava") || kind.equals("flowing_lava"))) props.remove("level");
        return Optional.of(new State(id, props));
    }

    private static String wood(String variant, boolean secondLog) {
        if (variant == null) return secondLog ? "acacia" : "oak";
        for (String name : WOODS) if (name.equals(variant)) return name;
        return secondLog ? "acacia" : "oak";
    }

    private static String color(String name) {
        if (name != null) for (String c : COLORS) if (c.equals(name)) return c;
        return "white";
    }
}
