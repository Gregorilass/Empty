package net.shadowmage.ancientwarfare.core.structure;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lossless decoder for Ancient Warfare's compressed AWS structure layout.
 *
 * This decodes block-rule indices and preserves original rule/entity data without
 * pretending obsolete 1.12.2 block IDs already have valid 26.3 equivalents.
 * No world placement is performed here. Inputs are strictly bounded to avoid
 * malformed templates exhausting server memory.
 */
public final class AwsTemplateParser {
    private static final Pattern SIZE = Pattern.compile("(?m)^size=(\\d+),(\\d+),(\\d+)\\s*$");
    private static final Pattern NAME = Pattern.compile("(?m)^name=([^\\r\\n]*)$");
    private static final Pattern LAYER = Pattern.compile("^layer:[ \\t]*(\\d+)(?:-(\\d+))?[ \\t]*\\R(.*?)^:endlayer[ \\t]*$", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern RULE = Pattern.compile("^rule:[ \\t]*\\R(.*?)^:endrule[ \\t]*$", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern ENTITY = Pattern.compile("^entity:[ \\t]*\\R(.*?)^:endentity[ \\t]*$", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern NUMBER = Pattern.compile("(?m)^number=(\\d+)\\s*$");
    private static final Pattern PLUGIN = Pattern.compile("(?m)^plugin=([^\\r\\n]*)$");
    private static final Pattern DATA = Pattern.compile("(?ms)^data:[ \\t]*\\R(.*?)^:enddata[ \\t]*$");
    private static final Pattern ROW_REPEAT = Pattern.compile("^(\\d+)x(.+)$");
    private static final long MAX_VOLUME = 35_000_000L;
    private static final int MAX_TEMPLATE_CHARS = 16_000_000;

    private AwsTemplateParser() {}

    public static Template parse(InputStream input) throws IOException {
        Objects.requireNonNull(input, "input");
        // Each file is parsed separately; this guards runaway allocation.
        byte[] bytes = input.readNBytes(MAX_TEMPLATE_CHARS + 1);
        if (bytes.length > MAX_TEMPLATE_CHARS) throw new IOException("AWS template exceeds size limit");
        return parse(new String(bytes, StandardCharsets.UTF_8));
    }

    public static Template parse(Reader input) throws IOException {
        Objects.requireNonNull(input, "input");
        StringBuilder text = new StringBuilder();
        char[] buf = new char[8192];
        int n;
        while ((n = input.read(buf)) != -1) {
            if (text.length() + n > MAX_TEMPLATE_CHARS) throw new IOException("AWS template exceeds size limit");
            text.append(buf, 0, n);
        }
        return parse(text.toString());
    }

    public static Template parse(String text) throws IOException {
        Objects.requireNonNull(text, "text");
        if (text.length() > MAX_TEMPLATE_CHARS) throw new IOException("AWS template exceeds size limit");
        Matcher dimensions = SIZE.matcher(text);
        if (!dimensions.find()) throw new IOException("AWS header has no size");
        int w = parseInt(dimensions.group(1), "width");
        int h = parseInt(dimensions.group(2), "height");
        int d = parseInt(dimensions.group(3), "depth");
        if (w < 1 || h < 1 || d < 1 || (long) w * h * d > MAX_VOLUME)
            throw new IOException("Invalid AWS dimensions: " + w + "x" + h + "x" + d);

        Matcher nm = NAME.matcher(text);
        String name = nm.find() ? nm.group(1) : "unnamed";
        Map<Integer, Layer> layers = new LinkedHashMap<>();
        Matcher layerMatcher = LAYER.matcher(text);
        while (layerMatcher.find()) {
            int from = parseInt(layerMatcher.group(1), "layer start");
            int to = layerMatcher.group(2) == null ? from : parseInt(layerMatcher.group(2), "layer end");
            if (from > to || to >= h) throw new IOException("Invalid layer range " + from + "-" + to);
            List<int[]> rows = new ArrayList<>();
            for (String raw : layerMatcher.group(3).split("\\R")) {
                String line = raw.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Matcher repeated = ROW_REPEAT.matcher(line);
                int copies = 1;
                if (repeated.matches()) {
                    copies = parseInt(repeated.group(1), "row repeat");
                    line = repeated.group(2);
                }
                if (copies < 1 || copies > d - rows.size())
                    throw new IOException("Invalid row repetition in layer " + from);
                int[] row = decodeRow(line, w);
                for (int i = 0; i < copies; i++) rows.add(row);
            }
            if (rows.size() != d) throw new IOException("Expected " + d + " rows for layer " + from + ", got " + rows.size());
            Layer layer = new Layer(List.copyOf(rows));
            for (int y = from; y <= to; y++) {
                if (layers.putIfAbsent(y, layer) != null) throw new IOException("Duplicate AWS layer " + y);
            }
        }
        if (layers.size() != h) throw new IOException("Expected " + h + " layers, got " + layers.size());

        Map<Integer, Rule> rules = new LinkedHashMap<>();
        Matcher rm = RULE.matcher(text);
        while (rm.find()) {
            String body = rm.group(1);
            Matcher no = NUMBER.matcher(body);
            Matcher plugin = PLUGIN.matcher(body);
            Matcher data = DATA.matcher(body);
            if (!no.find() || !plugin.find()) throw new IOException("Malformed rule in " + name);
            int index = parseInt(no.group(1), "rule index");
            Rule rule = new Rule(plugin.group(1), data.find() ? data.group(1) : "");
            if (rules.putIfAbsent(index, rule) != null) throw new IOException("Duplicate AWS rule " + index);
        }
        List<Rule> entities = new ArrayList<>();
        Matcher em = ENTITY.matcher(text);
        while (em.find()) {
            String body = em.group(1);
            Matcher plugin = PLUGIN.matcher(body);
            Matcher data = DATA.matcher(body);
            entities.add(new Rule(plugin.find() ? plugin.group(1) : "entity", data.find() ? data.group(1) : ""));
        }
        return new Template(name, w, h, d, Collections.unmodifiableMap(layers),
                Collections.unmodifiableMap(rules), List.copyOf(entities));
    }

    private static int parseInt(String input, String context) throws IOException {
        try { return Integer.parseInt(input); }
        catch (NumberFormatException ex) { throw new IOException("Invalid number for " + context + ": " + input, ex); }
    }

    private static int[] decodeRow(String line, int w) throws IOException {
        int[] row = new int[w];
        int pos = 0;
        for (String token : line.split(",", -1)) {
            int pipe = token.indexOf('|');
            int value = parseInt((pipe < 0 ? token : token.substring(0, pipe)).strip(), "rule value");
            int length = pipe < 0 ? 1 : parseInt(token.substring(pipe + 1).strip(), "run length");
            if (value < 0 || length < 1 || length > w - pos) throw new IOException("Invalid AWS row token " + token);
            for (int i = 0; i < length; i++) row[pos++] = value;
        }
        if (pos != w) throw new IOException("AWS row width mismatch: expected " + w + ", got " + pos);
        return row;
    }

    public record Rule(String plugin, String rawData) {}
    public record Layer(List<int[]> rows) {}

    /** Zero is the implicit 'skip / no placement' rule used by original templates. */
    public record Template(String name, int width, int height, int depth,
                           Map<Integer, Layer> layers, Map<Integer, Rule> rules, List<Rule> entities) {
        public int ruleAt(int x, int y, int z) {
            if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth)
                throw new IndexOutOfBoundsException("AWS coordinates outside template");
            return layers.get(y).rows().get(z)[x];
        }
    }
}
