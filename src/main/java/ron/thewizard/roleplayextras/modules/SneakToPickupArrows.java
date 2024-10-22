package ron.thewizard.roleplayextras.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class SneakToPickupArrows extends RoleplayExtrasModule implements Listener {

    public SneakToPickupArrows() {
        super("gameplay.sneak-to-pickup.arrows", false);
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
    private void on(PlayerPickupArrowEvent event) {
        if (!event.getPlayer().isSneaking()) {
            event.setCancelled(true);
        }
    }
}
