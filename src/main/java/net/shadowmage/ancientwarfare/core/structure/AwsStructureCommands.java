package net.shadowmage.ancientwarfare.core.structure;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Operator-only, bounded builder for legacy AWS vanilla-block templates.
 * Complex tiles, mobs and special plugins deliberately require future migration.
 * Unknown blocks abort before the world is modified.
 */
public final class AwsStructureCommands {
    private static final String ROOT = "/assets/ancientwarfare/template/";
    private static final String INDEX = ROOT + "manifest.txt";
    private static final int MAX_VOLUME = 8192;
    private static final List<String> CATALOG = loadCatalog();

    private AwsStructureCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("awstructure")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("list").executes(ctx -> list(ctx.getSource(), ""))
                        .then(Commands.argument("search", StringArgumentType.greedyString())
                                .executes(ctx -> list(ctx.getSource(), StringArgumentType.getString(ctx, "search")))))
                .then(Commands.literal("inspect")
                        .then(Commands.argument("template", StringArgumentType.greedyString())
                                .executes(ctx -> process(ctx.getSource(), StringArgumentType.getString(ctx, "template"), false))))
                .then(Commands.literal("place")
                        .then(Commands.argument("template", StringArgumentType.greedyString())
                                .executes(ctx -> process(ctx.getSource(), StringArgumentType.getString(ctx, "template"), true)))));
    }

    private static List<String> loadCatalog() {
        try (InputStream input = AwsStructureCommands.class.getResourceAsStream(INDEX)) {
            if (input == null) return List.of();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return in.lines().filter(name -> name.endsWith(".aws")).sorted().toList();
            }
        } catch (IOException ex) { return List.of(); }
    }

    private static int list(CommandSourceStack source, String query) {
        String filter = query.toLowerCase(Locale.ROOT);
        List<String> matching = CATALOG.stream().filter(x -> x.toLowerCase(Locale.ROOT).contains(filter)).toList();
        source.sendSuccess(() -> Component.literal("Ancient Warfare: " + matching.size() + " matching structures; showing first 12:"), false);
        for (String name : matching.stream().limit(12).toList()) {
            source.sendSuccess(() -> Component.literal(name), false);
        }
        return matching.size();
    }

    private static String lookup(String requested) {
        String input = requested.strip();
        if (input.contains("..") || input.startsWith("/") || input.contains("\\")) return null;
        if (!input.endsWith(".aws")) input += ".aws";
        if (CATALOG.contains(input)) return input;
        String target = input.toLowerCase(Locale.ROOT);
        List<String> matches = CATALOG.stream().filter(x -> x.toLowerCase(Locale.ROOT).endsWith("/" + target) || x.toLowerCase(Locale.ROOT).equals(target)).toList();
        return matches.size() == 1 ? matches.getFirst() : null;
    }

    private static int process(CommandSourceStack source, String rawName, boolean placement) {
        String name = lookup(rawName);
        if (name == null) {
            source.sendFailure(Component.literal("Template not found or ambiguous. Use /awstructure list <search> and supply the full path if necessary."));
            return 0;
        }
        AwsTemplateParser.Template template;
        try (InputStream input = AwsStructureCommands.class.getResourceAsStream(ROOT + name)) {
            if (input == null) throw new IOException("Resource missing: " + name);
            template = AwsTemplateParser.parse(input);
        } catch (IOException ex) {
            source.sendFailure(Component.literal("Structure could not be decoded: " + ex.getMessage()));
            return 0;
        }
        long volume = (long) template.width() * template.height() * template.depth();
        Map<Integer, BlockState> palette = new HashMap<>();
        Set<Integer> referenced = new HashSet<>();
        for (var layer : template.layers().values()) {
            for (int[] row : layer.rows()) for (int rule : row) if (rule != 0) referenced.add(rule);
        }
        List<String> unsupported = new ArrayList<>();
        for (int index : referenced) {
            var rule = template.rules().get(index);
            if (rule == null) { unsupported.add("missing rule #" + index); continue; }
            var decoded = AwsLegacyPalette.convert(rule);
            if (decoded.isEmpty()) { unsupported.add("#" + index + " " + rule.plugin()); continue; }
            var state = resolve(decoded.get());
            if (state == null) unsupported.add("#" + index + " " + decoded.get().id());
            else palette.put(index, state);
        }
        int missing = unsupported.size();
        source.sendSuccess(() -> Component.literal("AWS " + template.name() + " " + template.width() + "x" + template.height() + "x" + template.depth() + "; rules referenced: " + referenced.size() + ", supported: " + palette.size() + ", unsupported: " + missing + ", entities: " + template.entities().size()), false);
        if (!unsupported.isEmpty()) source.sendSuccess(() -> Component.literal("Unsupported examples: " + String.join(", ", unsupported.stream().limit(6).toList())), false);
        if (!placement) return 1;
        if (volume > MAX_VOLUME) {
            source.sendFailure(Component.literal("Placement denied: template volume " + volume + " exceeds " + MAX_VOLUME + " blocks. Large structures need an incremental builder."));
            return 0;
        }
        if (!unsupported.isEmpty() || !template.entities().isEmpty()) {
            source.sendFailure(Component.literal("Placement denied: this template uses rules or entities not yet supported. No blocks changed."));
            return 0;
        }
        ServerLevel level = source.getLevel();
        BlockPos origin = BlockPos.containing(source.getPosition()).offset(2, 0, 2);
        List<Placement> updates = new ArrayList<>();
        for (int y = 0; y < template.height(); y++) {
            for (int z = 0; z < template.depth(); z++) {
                for (int x = 0; x < template.width(); x++) {
                    int rule = template.ruleAt(x, y, z);
                    if (rule == 0) continue;
                    BlockPos pos = origin.offset(x, y, z);
                    if (level.isOutsideBuildHeight(pos) || !level.hasChunkAt(pos)) {
                        source.sendFailure(Component.literal("Placement denied: height out of range or destination chunk not loaded. No blocks changed."));
                        return 0;
                    }
                    updates.add(new Placement(pos, palette.get(rule)));
                }
            }
        }
        for (Placement update : updates) level.setBlock(update.pos(), update.state(), Block.UPDATE_ALL);
        int count = updates.size();
        source.sendSuccess(() -> Component.literal("Placed " + count + " blocks from " + name + " at " + origin.toShortString() + ". This is vanilla blocks only, not the complete AW structure system."), true);
        return count;
    }

    private static BlockState resolve(AwsLegacyPalette.State state) {
        int split = state.id().indexOf(':');
        if (split < 1) return null;
        Identifier id = Identifier.fromNamespaceAndPath(state.id().substring(0, split), state.id().substring(split + 1));
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
        if (block == null) return null;
        BlockState result = block.defaultBlockState();
        for (var entry : state.properties().entrySet()) {
            Property<?> property = result.getBlock().getStateDefinition().getProperty(entry.getKey());
            if (property == null) continue; // a legacy-only property
            BlockState next = apply(result, property, entry.getValue());
            if (next != null) result = next;
        }
        return result;
    }

    private static <T extends Comparable<T>> BlockState apply(BlockState state, Property<T> property, String value) {
        return property.getValue(value).map(x -> state.setValue(property, x)).orElse(null);
    }

    private record Placement(BlockPos pos, BlockState state) {}
}
