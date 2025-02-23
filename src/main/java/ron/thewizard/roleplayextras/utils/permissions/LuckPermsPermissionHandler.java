package ron.thewizard.roleplayextras.utils.permissions;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.util.Tristate;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.java.JavaPlugin;

public final class LuckPermsPermissionHandler implements PermissionHandler {

    private final LuckPerms luckPerms;
    private final BukkitPermissionHandler bukkitPermissionHandler;
    private final boolean isCitizensInstalled;

    LuckPermsPermissionHandler(JavaPlugin plugin) {
        luckPerms = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class).getProvider();
        isCitizensInstalled = plugin.getServer().getPluginManager().getPlugin("Citizens") != null;
        bukkitPermissionHandler = new BukkitPermissionHandler(plugin); // Fallback for non-players
    }

    @Override
    public void disable() {
        bukkitPermissionHandler.disable();
    }

    @Override
    public TriState permissionValue(Permissible permissible, String permission) {
        if (permissible instanceof Player) {
            Player player = (Player) permissible;
            if (isCitizensInstalled && player.hasMetadata("NPC")) {
                // If player is an NPC, LuckPerms will not be able to get a User
                return bukkitPermissionHandler.permissionValue(permissible, permission);
            }

            Tristate permState = luckPerms.getPlayerAdapter(Player.class).getUser(player)
                    .getCachedData().getPermissionData().checkPermission(permission);
            if (permState == Tristate.TRUE)
                return TriState.TRUE;
            if (permState == Tristate.FALSE)
                return TriState.FALSE;
            return TriState.UNDEFINED;
        } else {
            return bukkitPermissionHandler.permissionValue(permissible, permission);
        }
    }

    @Override
    public void setPermission(Permissible permissible, String permission, TriState state) {
        if (permissible instanceof Player) {
            Player player = (Player) permissible;
            if (isCitizensInstalled && player.hasMetadata("NPC")) {
                // If player is an NPC, LuckPerms will not be able to get a User
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
        } else {
            bukkitPermissionHandler.setPermission(permissible, permission, state);
        }
    }
}
