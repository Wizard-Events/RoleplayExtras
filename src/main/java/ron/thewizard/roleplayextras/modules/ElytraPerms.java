package ron.thewizard.roleplayextras.modules;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import ron.thewizard.roleplayextras.util.permissions.PluginPermission;

public class ElytraPerms extends RoleplayExtrasModule implements Listener {

    public ElytraPerms() {
        super("gameplay.elytra-permissions", true, "Allows setting a permission (" +
                PluginPermission.ELYTRA_USE_PERMISSION.node() + ") to allow or deny elytra usage.");
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void on(EntityToggleGlideEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;

        if (!PluginPermission.ELYTRA_USE_PERMISSION.check(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void on(PlayerElytraBoostEvent event) {
        if (!PluginPermission.ELYTRA_USE_PERMISSION.check(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
