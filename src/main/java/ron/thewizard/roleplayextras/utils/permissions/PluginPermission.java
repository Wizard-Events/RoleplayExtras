package ron.thewizard.roleplayextras.utils.permissions;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import ron.thewizard.roleplayextras.RoleplayExtras;

public enum PluginPermission {

    RELOAD_CMD(new Permission("rpextras.cmd.reload", PermissionDefault.OP)),
    VERSION_CMD(new Permission("rpextras.cmd.version", PermissionDefault.OP)),
    WALKSPEED_CMD_SELF(new Permission("rpextras.cmd.walkspeed.self", PermissionDefault.OP)),
    WALKSPEED_CMD_OTHER(new Permission("rpextras.cmd.walkspeed.other", PermissionDefault.OP)),
    VOICEPITCH_CMD_SELF(new Permission("rpextras.cmd.voicepitch.self", PermissionDefault.OP)),
    VOICEPITCH_CMD_OTHER(new Permission("rpextras.cmd.voicepitch.other", PermissionDefault.OP)),
    BYPASS_CHAT_PROXIMITY(new Permission("rpextras.bypass.chatproximity", PermissionDefault.FALSE));

    private final Permission permission;

    PluginPermission(Permission permission) {
        this.permission = permission;
    }

    public Permission bukkit() {
        return permission;
    }

    public boolean test(Permissible permissible) {
        return RoleplayExtras.permissions().permissionValue(permissible, permission.getName()).toBoolean();
    }

    public static void registerAll() {
        for (PluginPermission pluginPermission : PluginPermission.values()) {
            try {
                RoleplayExtras.getInstance().getServer().getPluginManager().addPermission(pluginPermission.bukkit());
            } catch (IllegalArgumentException e) {
                RoleplayExtras.logger().warn("Permission '{}' is already registered.", pluginPermission.bukkit().getName());
            }
        }
    }

    public static void unregisterAll() {
        for (PluginPermission pluginPermission : PluginPermission.values()) {
            RoleplayExtras.getInstance().getServer().getPluginManager().removePermission(pluginPermission.bukkit());
        }
    }
}
