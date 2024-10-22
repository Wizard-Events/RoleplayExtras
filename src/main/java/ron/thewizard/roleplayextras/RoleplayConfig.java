package ron.thewizard.roleplayextras;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import io.github.thatsmusic99.configurationmaster.api.Title;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import ron.thewizard.roleplayextras.utils.KyoriUtil;

import java.io.File;
import java.util.List;

public final class RoleplayConfig {

    private final @NotNull ConfigFile configFile;

    public final @NotNull List<Component> cmd_no_permission;

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

        this.cmd_no_permission = getList("messages.cmd-no-permission", List.of("<red>Stop it you twat!"))
                .stream()
                .map(KyoriUtil::replaceAmpersand)
                .map(MiniMessage.miniMessage()::deserialize)
                .toList();
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
}
