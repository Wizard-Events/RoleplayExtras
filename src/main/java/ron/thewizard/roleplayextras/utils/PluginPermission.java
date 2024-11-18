package ron.thewizard.roleplayextras.utils;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public enum PluginPermission {

    RELOAD_CMD(new Permission("rpextras.cmd.reload", PermissionDefault.OP)),
    VERSION_CMD(new Permission("rpextras.cmd.version", PermissionDefault.OP)),
    WALKSPEED_CMD_SELF(new Permission("rpextras.cmd.walkspeed.self", PermissionDefault.OP)),
    WALKSPEED_CMD_OTHER(new Permission("rpextras.cmd.walkspeed.other", PermissionDefault.OP));

    private final Permission permission;

    PluginPermission(Permission permission) {
        this.permission = permission;
    }

    public Permission get() {
        return permission;
    }

    public static void registerAll() {
        for (PluginPermission pluginPermission : PluginPermission.values()) {
            try {
                Bukkit.getPluginManager().addPermission(pluginPermission.get());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public static void unregisterAll() {
        for (PluginPermission pluginPermission : PluginPermission.values()) {
            try {
                Bukkit.getPluginManager().removePermission(pluginPermission.get());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}
