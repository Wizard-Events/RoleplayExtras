package ron.thewizard.roleplayextras.util.permissions;

import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.java.JavaPlugin;
import ron.thewizard.roleplayextras.util.Disableable;
import ron.thewizard.roleplayextras.util.ReflectionUtil;

public interface PermissionHandler extends Disableable {

    static PermissionHandler create(JavaPlugin plugin) {
        if (ReflectionUtil.hasClass("net.luckperms.api.model.user.User")
                && ReflectionUtil.hasClass("net.luckperms.api.node.Node")
                && ReflectionUtil.hasClass("net.luckperms.api.util.Tristate")
                && ReflectionUtil.hasClass("net.luckperms.api.LuckPerms")) {
            return new LuckPermsPermissionHandler(plugin);
        }

        return new BukkitPermissionHandler(plugin);
    }

    void disable();
    TriState permissionValue(Permissible permissible, String permission);
    void setPermission(Permissible permissible, String permission, TriState state);

}
