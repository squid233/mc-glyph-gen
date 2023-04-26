package io.github.squid233.mcglyphgen;

import java.util.List;
import java.util.Properties;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Options {
    public static final Entry EBackground = new Entry("background", "minecraft:white_concrete", "The background block");
    public static final Entry EForeground = new Entry("foreground", "minecraft:black_concrete", "The foreground block");
    public static final Entry EX0 = new Entry("x0", null, "The chunk position x0");
    public static final Entry EY = new Entry("y", null, "The position y");
    public static final Entry EZ0 = new Entry("z0", null, "The chunk position z0");
    public static final Entry EX1 = new Entry("x1", null, "The chunk position x1");
    public static final Entry EZ1 = new Entry("z1", null, "The chunk position z1");
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
    public record Entry(String name, String defaultValue, String description) {
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
