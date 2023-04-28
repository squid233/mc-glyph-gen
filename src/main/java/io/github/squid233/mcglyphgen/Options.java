package io.github.squid233.mcglyphgen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Options {
    private static final Map<String, Entry> registry = new HashMap<>((int) (7 / 0.75));
    public static final Entry EBackground = of("-background", "minecraft:white_concrete", "The background block", "B");
    public static final Entry EForeground = of("-foreground", "minecraft:black_concrete", "The foreground block", "F");
    public static final Entry EX0 = of("-x0", null, "The chunk position x0");
    public static final Entry EY = of("-y", null, "The position y");
    public static final Entry EZ0 = of("-z0", null, "The chunk position z0");
    public static final Entry EX1 = of("-x1", null, "The chunk position x1");
    public static final Entry EZ1 = of("-z1", null, "The chunk position z1");
    public static final List<Entry> entries = List.of(EBackground, EForeground, EX0, EY, EZ0, EX1, EZ1);
    public static final Properties properties = new Properties();

    static {
        putEntry(EBackground, null);
        putEntry(EForeground, null);
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    public record Entry(String name, String defaultValue, String description, String[] aliases) {
    }

    private static Entry of(String name, String defaultValue, String description, String... aliases) {
        final Entry entry = new Entry(name, defaultValue, description, aliases);
        registry.put(name, entry);
        for (String alias : aliases) {
            registry.put(alias, entry);
        }
        return entry;
    }

    public static Entry fromName(String nameOrAlias) {
        return registry.get(nameOrAlias);
    }

    public static void putEntry(Entry entry, String value) {
        properties.setProperty(entry.name, value == null ? entry.defaultValue : value);
    }

    public static String getEntry(Entry entry) {
        return properties.getProperty(entry.name, entry.defaultValue);
    }

    public static boolean containsEntry(Entry entry) {
        return properties.containsKey(entry.name);
    }
}
