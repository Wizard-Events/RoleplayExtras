package ron.thewizard.roleplayextras.utils;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

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

    public static void registerAll(PluginManager pluginManager) {
        for (PluginPermission perm : PluginPermission.values()) {
            try {
                pluginManager.addPermission(perm.get());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public static void unregisterAll(PluginManager pluginManager) {
        for (PluginPermission perm : PluginPermission.values()) {
            try {
                pluginManager.removePermission(perm.get());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}
