package ron.thewizard.roleplayextras.utils;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

public enum Permissions {

    RELOAD_CMD(new Permission("speedlimit.cmd.reload", PermissionDefault.OP)),
    VERSION_CMD(new Permission("speedlimit.cmd.version", PermissionDefault.OP)),
    WALKSPEED_CMD_SELF(new Permission("speedlimit.cmd.walkspeed.self", PermissionDefault.OP)),
    WALKSPEED_CMD_OTHER(new Permission("speedlimit.cmd.walkspeed.other", PermissionDefault.OP));

    private final Permission permission;

    Permissions(Permission permission) {
        this.permission = permission;
    }

    public Permission bukkit() {
        return permission;
    }

    public String string() {
        return permission.getName();
    }

    public static void registerAll(PluginManager pluginManager) {
        for (Permissions perm : Permissions.values()) {
            try {
                pluginManager.addPermission(perm.bukkit());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public static void unregisterAll(PluginManager pluginManager) {
        for (Permissions perm : Permissions.values()) {
            try {
                pluginManager.removePermission(perm.bukkit());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}
