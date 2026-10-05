package reika.geostrata.data;

import com.google.common.hash.Hashing;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.OreTypes;

/**
 * Generates the transparent ore-inclusion layer from the 26.2 vanilla stone-ore sprites.
 * The original 1.7.10 clipFrom algorithm removed pixels exactly equal to stone; that code
 * was disabled in the shipped renderer, but its output is a suitable static resource-pack
 * equivalent for vanilla ores. OreTypes without a vanilla sprite reuse the iron/gem inclusion
 * pattern and recolour only its non-stone pixels. Their blocks remain gated by material tags.
 */
public final class GeoOreTextureProvider implements DataProvider {
    private final PackOutput.PathProvider paths;
    private final Path artifactDirectory;

    public GeoOreTextureProvider(PackOutput output) {
        paths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "textures/block/ore_overlay");
        // Datagen output is <module>/src/generated/resources-client; the patched Minecraft jar
        // is a local build input. ModLauncher does not expose its asset entries as class resources.
        artifactDirectory = output.getOutputFolder().toAbsolutePath().getParent().getParent()
                .getParent().resolve("build/moddev/artifacts");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.runAsync(() -> {
            try {
                Path jarPath;
                try (var jars = Files.list(artifactDirectory)) {
                    jarPath = jars.filter(path -> path.getFileName().toString().matches("minecraft-patched-26\\.3\\..*\\.jar"))
                            .filter(path -> !path.getFileName().toString().contains("-merged"))
                            .filter(path -> !path.getFileName().toString().contains("-sources"))
                            .max((a, b) -> {
                                try { return Files.getLastModifiedTime(a).compareTo(Files.getLastModifiedTime(b)); }
                                catch (IOException e) { throw new java.io.UncheckedIOException(e); }
                            }).orElseThrow(() -> new IOException("Minecraft 26.2 artifact missing from " + artifactDirectory));
                }
                try (ZipFile jar = new ZipFile(jarPath.toFile())) {
                BufferedImage stone = vanilla(jar, "stone");
                // Mojang's stone PNG is grayscale, whereas ore PNGs are indexed colour.
                // ImageIO's getRGB applies a grayscale colour transform (143 -> 197),
                // so compare the actual PNG sample values instead of stone.getRGB().
                if (stone.getRaster().getNumBands() != 1)
                    throw new IOException("Expected grayscale vanilla stone sprite");
                for (OreTypes type : OreTypes.oreList) {
                    SpriteSpec spec = spec(type);
                    BufferedImage source = vanilla(jar, spec.source);
                    if (stone.getWidth() != source.getWidth() || stone.getHeight() != source.getHeight())
                        throw new IOException("Ore/stone sprite sizes differ: " + spec.source);
                    BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    for (int y = 0; y < source.getHeight(); y++) {
                        for (int x = 0; x < source.getWidth(); x++) {
                            int ore = source.getRGB(x, y);
                            int gray = stone.getRaster().getSample(x, y, 0);
                            int rawStone = 0xff000000 | gray << 16 | gray << 8 | gray;
                            if (ore == rawStone) continue;
                            if (spec.tint >= 0) ore = recolour(ore, spec.tint);
                            out.setRGB(x, y, ore);
                        }
                    }
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    if (!ImageIO.write(out, "png", bytes)) throw new IOException("PNG writer unavailable");
                    byte[] png = bytes.toByteArray();
                    cache.writeIfNeeded(paths.file(Identifier.fromNamespaceAndPath(GeoStrata.MODID,
                                    type.name().toLowerCase(java.util.Locale.ROOT)), "png"), png,
                            Hashing.sha256().hashBytes(png));
                }
                }
            } catch (IOException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        });
    }

    private static BufferedImage vanilla(ZipFile jar, String name) throws IOException {
        String path = "assets/minecraft/textures/block/" + name + ".png";
        ZipEntry entry = jar.getEntry(path);
        if (entry == null) throw new IOException("Missing Minecraft texture " + path);
        try (InputStream in = jar.getInputStream(entry)) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) throw new IOException("Unreadable Minecraft texture " + path);
            return image;
        }
    }

    private static int recolour(int pixel, int target) {
        int r = pixel >> 16 & 255, g = pixel >> 8 & 255, b = pixel & 255;
        double light = (r * 0.2126 + g * 0.7152 + b * 0.0722) / 170.0;
        int nr = Math.min(255, (int) ((target >> 16 & 255) * light));
        int ng = Math.min(255, (int) ((target >> 8 & 255) * light));
        int nb = Math.min(255, (int) ((target & 255) * light));
        return pixel & 0xff000000 | nr << 16 | ng << 8 | nb;
    }

    private record SpriteSpec(String source, int tint) {}

    private static SpriteSpec spec(OreTypes type) {
        return switch (type) {
            case IRON -> new SpriteSpec("iron_ore", -1);
            case COPPER -> new SpriteSpec("copper_ore", -1);
            case LAPIS -> new SpriteSpec("lapis_ore", -1);
            case GOLD -> new SpriteSpec("gold_ore", -1);
            case DIAMOND -> new SpriteSpec("diamond_ore", -1);
            case EMERALD -> new SpriteSpec("emerald_ore", -1);
            case SILVER -> new SpriteSpec("iron_ore", 0xc6cbd3);
            case TIN -> new SpriteSpec("iron_ore", 0xb9b9a9);
            case PLATINUM -> new SpriteSpec("iron_ore", 0xdde5df);
            case URANIUM -> new SpriteSpec("iron_ore", 0x6aaf4e);
            case LEAD -> new SpriteSpec("iron_ore", 0x5e6174);
            case NICKEL -> new SpriteSpec("iron_ore", 0xaeb5a1);
            case ALUMINIUM -> new SpriteSpec("iron_ore", 0xc2ccd2);
            case ZINC -> new SpriteSpec("iron_ore", 0xb5c1ab);
            case IRIDIUM -> new SpriteSpec("iron_ore", 0xadb1d3);
            case OSMIUM -> new SpriteSpec("iron_ore", 0x638fa3);
            case CADMIUM -> new SpriteSpec("iron_ore", 0xb9bd77);
            case INDIUM -> new SpriteSpec("iron_ore", 0x8b96cb);
            case BISMUTH -> new SpriteSpec("iron_ore", 0xc995b1);
            case ARSENIC -> new SpriteSpec("iron_ore", 0x98aa72);
            case ANTIMONY -> new SpriteSpec("iron_ore", 0xb9b8ad);
            case MANGANESE -> new SpriteSpec("iron_ore", 0x937394);
            case CHROMIUM -> new SpriteSpec("iron_ore", 0xaac0c6);
            case THORIUM -> new SpriteSpec("iron_ore", 0xb3a689);
            case LITHIUM -> new SpriteSpec("iron_ore", 0xe3d9d5);
            case TITANIUM -> new SpriteSpec("iron_ore", 0x879eaa);
            case VANADIUM -> new SpriteSpec("iron_ore", 0x8b91ae);
            case TUNGSTEN -> new SpriteSpec("iron_ore", 0x7d838a);
            case RUBY -> new SpriteSpec("emerald_ore", 0xe53b49);
            case SAPPHIRE -> new SpriteSpec("emerald_ore", 0x4675df);
            case PERIDOT -> new SpriteSpec("emerald_ore", 0x8ccb4e);
            case TOPAZ -> new SpriteSpec("emerald_ore", 0xe5bc51);
        };
    }

    @Override
    public String getName() { return "GeoStrata Ore Overlays"; }
}
