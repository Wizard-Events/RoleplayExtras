package ron.thewizard.roleplayextras.utils.permissions;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.util.Tristate;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.java.JavaPlugin;
import ron.thewizard.roleplayextras.utils.EntityUtil;

public final class LuckPermsPermissionHandler implements PermissionHandler {

    private final LuckPerms luckPerms;
    private final BukkitPermissionHandler bukkitPermissionHandler;
    private final boolean isCitizensInstalled;

    LuckPermsPermissionHandler(JavaPlugin plugin) {
        luckPerms = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class).getProvider();
        isCitizensInstalled = plugin.getServer().getPluginManager().getPlugin("Citizens") != null;
        bukkitPermissionHandler = new BukkitPermissionHandler(plugin); // We use this one only for non-players
    }

    @Override
    public void disable() {
        bukkitPermissionHandler.disable();
    }

    @Override
    public TriState permissionValue(Permissible permissible, String permission) {
        if (!(permissible instanceof Player player) || (isCitizensInstalled && EntityUtil.isNPC(player)))
            return bukkitPermissionHandler.permissionValue(permissible, permission);
        Tristate permState = luckPerms.getPlayerAdapter(Player.class).getUser(player)
                .getCachedData().getPermissionData().checkPermission(permission);
        if (permState == Tristate.TRUE)
            return TriState.TRUE;
        if (permState == Tristate.FALSE)
            return TriState.FALSE;
        return TriState.UNDEFINED;
    }

    @Override
    public void setPermission(Permissible permissible, String permission, TriState state) {
        if (!(permissible instanceof Player player) || (isCitizensInstalled && EntityUtil.isNPC(player))) {
            bukkitPermissionHandler.setPermission(permissible, permission, state);
            return;
        }
        User luckPermsUser = luckPerms.getPlayerAdapter(Player.class).getUser(player);
        if (state == TriState.UNDEFINED) {
            luckPermsUser.data().remove(Node.builder(permission).build());
        } else {
            luckPermsUser.data().add(Node.builder(permission).value(state.toBoolean()).build());
        }
        luckPerms.getUserManager().saveUser(luckPermsUser);
    }
}
