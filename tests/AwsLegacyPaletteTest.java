import net.shadowmage.ancientwarfare.core.structure.AwsTemplateParser;
import net.shadowmage.ancientwarfare.core.structure.AwsLegacyPalette;
import java.nio.file.*;
import java.util.*;

public class AwsLegacyPaletteTest {
    private static void check(String raw, String expected) {
        var converted=AwsLegacyPalette.convert(new AwsTemplateParser.Rule("vanillaBlocks", "JSON:{blockState:{blockName:\""+raw+"\"}}"));
        if (converted.isEmpty() || !converted.get().id().equals(expected))
            throw new AssertionError(raw +" -> "+converted+" but expected "+expected);
    }
    public static void main(String[] argv) throws Exception {
        check("minecraft:grass", "minecraft:grass_block");
        check("minecraft:brick_block", "minecraft:bricks");
        check("minecraft:wooden_door", "minecraft:oak_door");
        check("minecraft:cobblestone", "minecraft:cobblestone");
        check("minecraft:water", "minecraft:water");
        var alias=AwsLegacyPalette.convert(new AwsTemplateParser.Rule("vanillaBlocks", "JSON:{blockState:{blockName:\"minecraft:log\",properties:{variant:\"spruce\",axis:\"y\"}}}"));
        if (!alias.orElseThrow().id().equals("minecraft:spruce_log")) throw new AssertionError("Log variant lost");
        if (AwsLegacyPalette.convert(new AwsTemplateParser.Rule("inventory", "JSON:{blockState:{blockName:\"minecraft:chest\"}}" )).isPresent())
            throw new AssertionError("Unsafe tile entity should not be converted");
        int count=0, convertible=0, ignored=0;
        try(var walk=Files.walk(Path.of(argv[0]))) {
            for (Path f:walk.filter(x->x.toString().endsWith(".aws")).toList()) {
                var temp=AwsTemplateParser.parse(Files.newInputStream(f));
                for (var rule:temp.rules().values()) {
                    if (AwsLegacyPalette.convert(rule).isPresent()) convertible++;
                    else ignored++;
                }
                count++;
            }
        }
        if (count!=1484) throw new AssertionError("Expected 1484 templates, got "+count);
        System.out.printf("PASS %d templates, %d safely mappable rules, %d requiring further migration%n", count,convertible,ignored);
    }
}
