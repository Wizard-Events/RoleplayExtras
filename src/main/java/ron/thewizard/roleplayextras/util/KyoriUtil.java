package ron.thewizard.roleplayextras.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KyoriUtil {

    public static final TextColor ginkoBlue, wizardPurple, wizardRed, wizardWhite;

    public static final MiniMessage MINIMESSAGE_PLAYERINPUT_SAFE;

    private static final Set<Map.Entry<String, String>> COLOR_CODE_TO_TAG_ENTRY_SET;

    static {
        ginkoBlue = TextColor.fromHexString("#21FFF5");
        wizardRed = TextColor.fromHexString("#FF334E");
        wizardWhite = TextColor.fromHexString("#FFE0E2");
        wizardPurple = TextColor.fromHexString("#B442FF");

        MINIMESSAGE_PLAYERINPUT_SAFE = MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(StandardTags.color())
                        .resolver(StandardTags.decorations())
                        .resolver(StandardTags.gradient())
                        .resolver(StandardTags.rainbow())
                        .build())
                .build();

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

    public static List<Component> getBirthdayMessage() {
        final Style happy = Style.style(wizardPurple, TextDecoration.BOLD);
        final Style birthday = Style.style(wizardRed, TextDecoration.BOLD);
        final Style mal = Style.style(wizardWhite, TextDecoration.BOLD);
        return ImmutableList.of(
                Component.empty(),
                Component.text("      ██   ██  █████  ██████  ██████  ██    ██                 ").style(happy),
                Component.text("      ██   ██ ██   ██ ██   ██ ██   ██  ██  ██                  ").style(happy),
                Component.text("      ███████ ███████ ██████  ██████    ████                   ").style(happy),
                Component.text("      ██   ██ ██   ██ ██      ██         ██                    ").style(happy),
                Component.text("      ██   ██ ██   ██ ██      ██         ██                    ").style(happy),
                Component.empty(),
                Component.text("  ██████  ██ ██████  ████████ ██   ██ ██████   █████  ██    ██ ").style(birthday),
                Component.text("  ██   ██ ██ ██   ██    ██    ██   ██ ██   ██ ██   ██  ██  ██  ").style(birthday),
                Component.text("  ██████  ██ ██████     ██    ███████ ██   ██ ███████   ████   ").style(birthday),
                Component.text("  ██   ██ ██ ██   ██    ██    ██   ██ ██   ██ ██   ██    ██    ").style(birthday),
                Component.text("  ██████  ██ ██   ██    ██    ██   ██ ██████  ██   ██    ██    ").style(birthday),
                Component.empty(),
                Component.text("                          ███    ███  █████  ██      ██        ").style(mal),
                Component.text("                          ████  ████ ██   ██ ██      ██        ").style(mal),
                Component.text("                          ██ ████ ██ ███████ ██      ██        ").style(mal),
                Component.text("                          ██  ██  ██ ██   ██ ██                ").style(mal),
                Component.text("                          ██      ██ ██   ██ ███████ ██        ").style(mal),
                Component.empty()
        );
    }
}
