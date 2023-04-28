package io.github.squid233.mcglyphgen;

import org.overrun.unifont.UnifontUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;
import java.util.Properties;

import static io.github.squid233.mcglyphgen.Options.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Generator {
    private static final int CHUNK_SIZE = 16;
    private static final int PIXEL_SIZE = 8;

    private static void store(Properties prop, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            prop.store(writer, null);
        }
    }

    private static String get(String[] args, int index, Properties prop, String other) {
        if (args.length <= index) {
            return prop.getProperty(other);
        }
        final String v = args[index];
        return "default".equals(v) ? prop.getProperty(other) : v;
    }

    public static void main(String[] args) throws IOException {
        // TODO: GUI
        if (args.length == 0) {
            final StringBuilder sb = new StringBuilder(512);
            sb.append("""
                Usage:
                java -jar gen.jar [<option>...] <character> [output-file]
                java -jar gen.jar --default <name> <value> [[<name> <value>] ...]

                Options:
                """);
            for (Entry entry : entries) {
                sb.append("  -").append(entry.name()).append(" <value");
                final String defaultValue = entry.defaultValue();
                if (defaultValue != null) {
                    sb.append('=').append(defaultValue);
                }
                sb.append(">: ").append(entry.description()).append('\n');
                for (String alias : entry.aliases()) {
                    sb.append("   -").append(alias).append('\n');
                }
            }
            System.out.print(sb);
            return;
        }
        final File file = new File("defaults.properties");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                properties.load(reader);
            }
        } else {
            store(properties, file);
        }

        // defaults
        if ("--default".equals(args[0])) {
            if (args.length < 3) {
                System.out.println("""
                    Usage:
                    java -jar gen.jar --default <name> <value> [[<name> <value>]...]""");
                return;
            }
            for (int i = 0, c = (args.length - 1) / 2; i < c; i++) {
                properties.setProperty(args[i * 2 + 1], args[i * 2 + 2]);
            }
            store(properties, file);
            return;
        }

        // options
        String option = null;
        int codePoint = -1;
        String outputFile = null;
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            // define option
            if (arg.startsWith("-")) {
                if ((i % 2) == 0) {
                    option = arg.substring(1);
                }
            } else {
                // defining option
                if (option != null) {
                    // this will not be stored
                    final Entry entry = fromName(option);
                    if (entry == null) {
                        System.err.println("Unrecognized option: -" + option);
                        return;
                    }
                    putEntry(entry, arg);
                    option = null;
                }
                // last arguments
                else if (codePoint < 0) {
                    codePoint = arg.codePointAt(0);
                } else {
                    outputFile = arg;
                }
            }
        }

        if (option != null) {
            System.err.println("Please specify the value for the given option");
        }
        if (codePoint < 0) {
            System.err.println("Please specify the character");
            return;
        }

        // generate
        if (!(containsEntry(EX0) && containsEntry(EY) && containsEntry(EZ0) &&
            containsEntry(EX1) && containsEntry(EZ1))) {
            System.err.println("Please specify the range of the glyph");
            return;
        }

        String bg = getEntry(EBackground);
        String fg = getEntry(EForeground);
        int x0 = Integer.parseInt(getEntry(EX0));
        int y = Integer.parseInt(getEntry(EY));
        int z0 = Integer.parseInt(getEntry(EZ0));
        int x1 = Integer.parseInt(getEntry(EX1));
        int z1 = Integer.parseInt(getEntry(EZ1));

        final StringBuilder sb = new StringBuilder(512);

        // background
        sb.append("fill ")
            .append(x0 * CHUNK_SIZE).append(' ')
            .append(y).append(' ')
            .append(z0 * CHUNK_SIZE).append(' ')
            .append((x1 + 1) * CHUNK_SIZE - 1).append(' ')
            .append(y).append(' ')
            .append((z1 + 1) * CHUNK_SIZE - 1).append(' ')
            .append(bg).append('\n');

        // foreground
        boolean isBmp = codePoint <= 0xffff;
        BufferedImage image;
        try (InputStream stream = ClassLoader.getSystemResourceAsStream(
            isBmp ? "assets/_overrun/fonts/unifont.png" : "assets/_overrun/fonts/unifont_plane1.png"
        )) {
            image = ImageIO.read(Objects.requireNonNull(stream));
        }
        final int xo = UnifontUtil.xOffset(codePoint);
        final int yo = UnifontUtil.yOffset(codePoint);
        for (int x = 0, w = Math.min(UnifontUtil.xAdvance(codePoint), (x1 - x0) * 2); x < w; x++) {
            for (int by = 0, h = Math.min(UnifontUtil.yAdvance(), (z1 - z0) * 2); by < h; by++) {
                if (image.getRGB(x + xo, by + yo) == 0xffffffff) {
                    sb.append("fill ")
                        .append(x0 * CHUNK_SIZE + x * PIXEL_SIZE).append(' ')
                        .append(y).append(' ')
                        .append(z0 * CHUNK_SIZE + by * PIXEL_SIZE).append(' ')
                        .append(x0 * CHUNK_SIZE + (x + 1) * PIXEL_SIZE - 1).append(' ')
                        .append(y).append(' ')
                        .append(z0 * CHUNK_SIZE + (by + 1) * PIXEL_SIZE - 1).append(' ')
                        .append(fg).append('\n');
                }
            }
        }

        System.out.print(sb);
    }
}
