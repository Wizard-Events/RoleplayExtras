package ron.thewizard.roleplayextras.util;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;

public class KyoriUtil {

    public static final TextColor ginkoBlue, wizardPurple, wizardRed, wizardWhite;

    static {
        ginkoBlue = TextColor.fromHexString("#21FFF5");
        wizardRed = TextColor.fromHexString("#FF334E");
        wizardWhite = TextColor.fromHexString("#FFE0E2");
        wizardPurple = TextColor.fromHexString("#B442FF");
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

    public static String replaceAmpersand(String string) {
        string = string.replace("&0", "<black>");
        string = string.replace("&1", "<dark_blue>");
        string = string.replace("&2", "<dark_green>");
        string = string.replace("&3", "<dark_aqua>");
        string = string.replace("&4", "<dark_red>");
        string = string.replace("&5", "<dark_purple>");
        string = string.replace("&6", "<gold>");
        string = string.replace("&7", "<gray>");
        string = string.replace("&8", "<dark_gray>");
        string = string.replace("&9", "<blue>");
        string = string.replace("&a", "<green>");
        string = string.replace("&b", "<aqua>");
        string = string.replace("&c", "<red>");
        string = string.replace("&d", "<light_purple>");
        string = string.replace("&e", "<yellow>");
        string = string.replace("&f", "<white>");
        string = string.replace("&k", "<obfuscated>");
        string = string.replace("&l", "<bold>");
        string = string.replace("&m", "<strikethrough>");
        string = string.replace("&n", "<underlined>");
        string = string.replace("&o", "<italic>");
        string = string.replace("&r", "<reset>");
        return string;
    }
}
