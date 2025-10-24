package ron.thewizard.roleplayextras;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import io.github.thatsmusic99.configurationmaster.api.Title;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import ron.thewizard.roleplayextras.utils.KyoriUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

public final class RoleplayConfig {

    private final @NotNull ConfigFile configFile;

    public final @NotNull List<Component> cmd_no_permission, walkspeed_invalid_speed_format, walkspeed_invalid_speed,
            walkspeed_success_self, walkspeed_success_other;

    public RoleplayConfig() throws Exception {
        RoleplayExtras plugin = RoleplayExtras.getInstance();
        // Load config.yml with ConfigMaster
        this.configFile = ConfigFile.loadConfig(new File(plugin.getDataFolder(), "config.yml"));

        this.configFile.setTitle(new Title().withWidth(80)
                .addSolidLine()
                .addLine(" ", Title.Pos.CENTER)
                .addLine(plugin.getName(), Title.Pos.CENTER)
                .addLine(" ", Title.Pos.CENTER)
                .addSolidLine());

        this.cmd_no_permission = getMessage("messages.cmd.no-permission",
                List.of("<#FF334E>You don't have permissies :/"));
        this.walkspeed_invalid_speed = getMessage("messages.cmd.walkspeed.invalid-speed",
                List.of("<#FF334E>Invalid speed! Must be between -1.0 and 1.0!"));
        this.walkspeed_invalid_speed_format = getMessage("messages.cmd.walkspeed.invalid-speed-format",
                List.of("<#FF334E>Invalid speed! Format: #.##"));
        this.walkspeed_success_self = getMessage("messages.cmd.walkspeed.success-self",
                List.of("<#78E05E>Successfully set walkspeed to %walkspeed%."));
        this.walkspeed_success_other = getMessage("messages.cmd.walkspeed.success-other",
                List.of("<#78E05E>Successfully set %player%'s walkspeed to %walkspeed%."));
    }

    public boolean saveConfig() {
        try {
            this.configFile.save();
            return true;
        } catch (Exception e) {
            RoleplayExtras.logger().error("Failed to save config file!", e);
            return false;
        }
    }

    public @NotNull ConfigFile master() {
        return configFile;
    }

    public boolean getBoolean(@NotNull String path, boolean def, @NotNull String comment) {
        this.configFile.addDefault(path, def, comment);
        return this.configFile.getBoolean(path, def);
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        this.configFile.addDefault(path, def);
        return this.configFile.getBoolean(path, def);
    }

    public @NotNull String getString(@NotNull String path, @NotNull String def, @NotNull String comment) {
        this.configFile.addDefault(path, def, comment);
        return this.configFile.getString(path, def);
    }

    public @NotNull String getString(@NotNull String path, @NotNull String def) {
        this.configFile.addDefault(path, def);
        return this.configFile.getString(path, def);
    }

    public double getDouble(@NotNull String path, double def, @NotNull String comment) {
        this.configFile.addDefault(path, def, comment);
        return this.configFile.getDouble(path, def);
    }

    public double getDouble(@NotNull String path, double def) {
        this.configFile.addDefault(path, def);
        return this.configFile.getDouble(path, def);
    }

    public int getInt(@NotNull String path, int def, @NotNull String comment) {
        this.configFile.addDefault(path, def, comment);
        return this.configFile.getInteger(path, def);
    }

    public int getInt(@NotNull String path, int def) {
        this.configFile.addDefault(path, def);
        return this.configFile.getInteger(path, def);
    }

    public long getLong(@NotNull String path, long def) {
        this.configFile.addDefault(path, def);
        return this.configFile.getLong(path, def);
    }

    public long getLong(@NotNull String path, long def, @NotNull String comment) {
        this.configFile.addDefault(path, def, comment);
        return this.configFile.getLong(path, def);
    }

    public @NotNull <T> List<T> getList(@NotNull String path, @NotNull List<T> def, @NotNull String comment) {
        this.configFile.addDefault(path, def, comment);
        return this.configFile.getList(path);
    }

    public @NotNull <T> List<T> getList(@NotNull String path, @NotNull List<T> def) {
        this.configFile.addDefault(path, def);
        return this.configFile.getList(path);
    }

    public @NotNull List<Component> getMessage(@NotNull String path, @NotNull List<String> def, @NotNull String comment) {
        return this.getList(path, def, comment).stream().map(KyoriUtil::replaceAmpersand).map(MiniMessage.miniMessage()::deserialize).toList();
    }

    public @NotNull List<Component> getMessage(@NotNull String path, @NotNull List<String> def) {
        return this.getList(path, def).stream().map(KyoriUtil::replaceAmpersand).map(MiniMessage.miniMessage()::deserialize).toList();
    }

    public ConfigSection getConfigSection(String path, Map<String, Object> defaultKeyValue) {
        configFile.addDefault(path, null);
        configFile.makeSectionLenient(path);
        defaultKeyValue.forEach((string, object) -> configFile.addExample(path + "." + string, object));
        return configFile.getConfigSection(path);
    }

    public ConfigSection getConfigSection(String path, Map<String, Object> defaultKeyValue, String comment) {
        configFile.addDefault(path, null, comment);
        configFile.makeSectionLenient(path);
        defaultKeyValue.forEach((string, object) -> configFile.addExample(path + "." + string, object));
        return configFile.getConfigSection(path);
    }
}
