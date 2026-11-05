import net.shadowmage.ancientwarfare.core.structure.AwsTemplateParser;
import java.nio.file.*;
import java.util.zip.*;
import java.io.*;

/** Standalone regression suite compiled with javac (no Minecraft client required). */
public class AwsTemplateParserTest {
    public static void main(String[] argv) throws Exception {
        if (argv.length != 1) throw new IllegalArgumentException("Provide directory containing AWS files");
        int count = 0, rules = 0, entities = 0;
        try (var files = Files.walk(Path.of(argv[0]))) {
            for (Path path : files.filter(x -> x.toString().endsWith(".aws")).toList()) {
                try (InputStream in = Files.newInputStream(path)) {
                    var template = AwsTemplateParser.parse(in);
                    if (template.layers().size() != template.height()) throw new AssertionError(path);
                    template.ruleAt(0, 0, 0);
                    template.ruleAt(template.width()-1, template.height()-1, template.depth()-1);
                    rules += template.rules().size();
                    entities += template.entities().size();
                } catch (IOException ex) {throw new IOException(path.toString(), ex);}
                count++;
            }
        }
        if (count != 1484) throw new AssertionError("Expected 1484 templates, got "+count);
        System.out.printf("PASS %d AWS templates, %d rules, %d entities%n",count,rules,entities);
        var sample = "header:\nname=TEST\nsize=3,2,2\n:endheader\nlayer: 0-1\n2x1,2|2\n:endlayer\n";
        var x = AwsTemplateParser.parse(sample);
        if (x.ruleAt(0, 1, 1) != 1 || x.ruleAt(1, 0, 0) != 2)
            throw new AssertionError("Run-length or layer-range expansion failed");
        try { AwsTemplateParser.parse("header:\nsize=90000000,1,1\n:endheader");
            throw new AssertionError("Large malformed input accepted"); }
        catch (IOException expected) {}
    }
}
