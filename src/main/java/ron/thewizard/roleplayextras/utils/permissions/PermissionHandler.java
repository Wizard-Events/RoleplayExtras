package ron.thewizard.roleplayextras.utils.permissions;

import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.java.JavaPlugin;
import ron.thewizard.roleplayextras.utils.Disableable;
import ron.thewizard.roleplayextras.utils.ReflectionUtil;

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
