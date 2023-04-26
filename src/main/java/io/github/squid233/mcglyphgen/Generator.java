package io.github.squid233.mcglyphgen;

import org.overrun.unifont.UnifontUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;
import java.util.Properties;

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
            System.out.println("""
                Usage:
                java -jar gen.jar <character> [background=minecraft:white_concrete] [foreground=minecraft:black_concrete]\
                 [x0=default] [y=default] [z0=default] [x1=default] [z1=default]
                java -jar gen.jar --default <name> <value> [[<name> <value>] ...]

                Defaults:
                background=minecraft:white_concrete: The background block
                foreground=minecraft:black_concrete: The foreground block
                x0: The chunk position x0
                y: The position y
                z0: The chunk position z0
                x1: The chunk position x1
                z1: The chunk position z1""");
            return;
        }
        final Properties prop = new Properties((int) (7 / 0.75f));
        prop.put("background", "minecraft:white_concrete");
        prop.put("foreground", "minecraft:black_concrete");
        final File file = new File("defaults.properties");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                prop.load(reader);
            }
        } else {
            store(prop, file);
        }

        // defaults
        if ("--default".equals(args[0])) {
            if (args.length < 3) {
                System.out.println("""
                    Usage:
                    java -jar gen.jar --default <name> <value> [[<name> <value>] ...]""");
                return;
            }
            for (int i = 0, c = (args.length - 1) / 2; i < c; i++) {
                prop.setProperty(args[i * 2 + 1], args[i * 2 + 2]);
            }
            store(prop, file);
            return;
        }

        // generate
        if (!(prop.containsKey("x0") && prop.containsKey("y") && prop.containsKey("z0") &&
              prop.containsKey("x1") && prop.containsKey("z1"))) {
            System.out.println("Please set the range of the glyph");
            return;
        }
        int x0 = Integer.parseInt(get(args, 3, prop, "x0"));
        int y = Integer.parseInt(get(args, 4, prop, "y"));
        int z0 = Integer.parseInt(get(args, 5, prop, "z0"));
        int x1 = Integer.parseInt(get(args, 6, prop, "x1"));
        int z1 = Integer.parseInt(get(args, 7, prop, "z1"));
        String bg = get(args, 1, prop, "background");
        String fg = get(args, 2, prop, "foreground");

        // background
        System.out.printf("fill %d %d %d %d %d %d %s%n", x0 * CHUNK_SIZE, y, z0 * CHUNK_SIZE,
            (x1 + 1) * CHUNK_SIZE - 1, y, (z1 + 1) * CHUNK_SIZE - 1, bg);

        // foreground
        final int cp = args[0].codePointAt(0);
        boolean isBmp = cp >= 0 && cp <= 0xffff;
        BufferedImage image;
        try (InputStream stream = ClassLoader.getSystemResourceAsStream(
            isBmp ? "assets/_overrun/fonts/unifont.png" : "assets/_overrun/fonts/unifont_plane1.png"
        )) {
            image = ImageIO.read(Objects.requireNonNull(stream));
        }
        final int xo = UnifontUtil.xOffset(cp);
        final int yo = UnifontUtil.yOffset(cp);
        for (int x = 0, w = Math.min(UnifontUtil.xAdvance(cp), (x1 - x0) * 2); x < w; x++) {
            for (int by = 0, h = Math.min(UnifontUtil.yAdvance(), (z1 - z0) * 2); by < h; by++) {
                if (image.getRGB(x + xo, by + yo) == 0xffffffff) {
                    System.out.printf("fill %d %d %d %d %d %d %s%n", x0 * CHUNK_SIZE + x * PIXEL_SIZE, y, z0 * CHUNK_SIZE + by * PIXEL_SIZE,
                        x0 * CHUNK_SIZE + (x + 1) * PIXEL_SIZE - 1, y, z0 * CHUNK_SIZE + (by + 1) * PIXEL_SIZE - 1, fg);
                }
            }
        }
    }
}
