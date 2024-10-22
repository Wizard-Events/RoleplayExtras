package ron.thewizard.roleplayextras.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;

public class SneakToPickupItems extends RoleplayExtrasModule implements Listener {

    public SneakToPickupItems() {
        super("gameplay.sneak-to-pickup.items", true);
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void on(PlayerAttemptPickupItemEvent event) {
        if (!event.getPlayer().isSneaking()) {
            event.setCancelled(true);
        }
    }
}
