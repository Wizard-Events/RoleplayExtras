package ron.thewizard.roleplayextras.util;

import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AdventureUtil {

    public static final TextColor ginkoBlue, wizardPurple, wizardRed, wizardWhite;
    private static final Set<Map.Entry<String, String>> COLOR_CODE_TO_TAG_ENTRY_SET;

    static {
        ginkoBlue = TextColor.fromHexString("#21FFF5");
        wizardRed = TextColor.fromHexString("#FF334E");
        wizardWhite = TextColor.fromHexString("#FFE0E2");
        wizardPurple = TextColor.fromHexString("#B442FF");

        // Construct immutable mappings for replacing ChatColor codes with MiniMessage compatible tags
        String[] color_code_chars = { "&", "§" };
        Map<String, String> color_char_to_mm_tag = HashMap.newHashMap(21);
        color_char_to_mm_tag.put("0", "<black>");
        color_char_to_mm_tag.put("1", "<dark_blue>");
        color_char_to_mm_tag.put("2", "<dark_green>");
        color_char_to_mm_tag.put("3", "<dark_aqua>");
        color_char_to_mm_tag.put("4", "<dark_red>");
        color_char_to_mm_tag.put("5", "<dark_purple>");
        color_char_to_mm_tag.put("6", "<gold>");
        color_char_to_mm_tag.put("7", "<gray>");
        color_char_to_mm_tag.put("8", "<dark_gray>");
        color_char_to_mm_tag.put("9", "<blue>");
        color_char_to_mm_tag.put("a", "<green>");
        color_char_to_mm_tag.put("b", "<aqua>");
        color_char_to_mm_tag.put("c", "<red>");
        color_char_to_mm_tag.put("d", "<light_purple>");
        color_char_to_mm_tag.put("e", "<yellow>");
        color_char_to_mm_tag.put("f", "<white>");
        color_char_to_mm_tag.put("k", "<obfuscated>");
        color_char_to_mm_tag.put("l", "<bold>");
        color_char_to_mm_tag.put("m", "<strikethrough>");
        color_char_to_mm_tag.put("n", "<underlined>");
        color_char_to_mm_tag.put("o", "<italic>");
        color_char_to_mm_tag.put("r", "<reset>");
        Map<String, String> color_code_to_mm_tag = HashMap.newHashMap(color_char_to_mm_tag.size() * color_code_chars.length);
        for (Map.Entry<String, String> entry : color_char_to_mm_tag.entrySet()) {
            for (String color_code_char : color_code_chars) {
                color_code_to_mm_tag.put(color_code_char + entry.getKey(), entry.getValue());
            }
        }
        COLOR_CODE_TO_TAG_ENTRY_SET = ImmutableSet.copyOf(color_code_to_mm_tag.entrySet());
    }

    public static @NotNull String replaceAmpersand(@NotNull String string) {
        for (Map.Entry<String, String> colorCodeEntry : COLOR_CODE_TO_TAG_ENTRY_SET) {
            string = string.replace(colorCodeEntry.getKey(), colorCodeEntry.getValue());
        }
        return string;
    }

    /**
     * Maps an X/Z position to a color.
     *
     * @param location   Bukkit location (uses block X/Z for stability).
     * @param scale Controls how quickly colors change with distance.
     *              Bigger scale => more similar colors in a local area.
     *              Suggested: scale ≈ proximityRadius * 15..30 (e.g. 13 -> 200..400, 20 -> 300..600).
     */
    public static @NotNull TextColor coordsToRGB(@NotNull Location location, double scale) {
        if (scale <= 0.0 || Double.isNaN(scale) || Double.isInfinite(scale)) {
            scale = 300.0; // safe fallback
        }

        // Use block coords to keep the color stable while a player moves within the same block.
        final int x = location.getBlockX();
        final int z = location.getBlockZ();

        // Normalize by scale
        final double nx = x / scale;
        final double nz = z / scale;

        // Hue: direction around origin
        double hue = Math.toDegrees(Math.atan2(nz, nx)); // [-180..180]
        if (hue < 0.0) hue += 360.0;                     // [0..360)

        // Saturation: smooth radial distance -> [0..1)
        final double saturation = 1.0 - Math.exp(-Math.sqrt(nx * nx + nz * nz));

        // Fixed brightness
        final float v = 0.75f;

        return TextColor.color(HSVLike.hsvLike((float) (hue / 360.0), (float) saturation, v));
    }
}
