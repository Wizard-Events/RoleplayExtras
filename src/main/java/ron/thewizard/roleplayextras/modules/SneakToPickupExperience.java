package ron.thewizard.roleplayextras.modules;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class SneakToPickupExperience extends RoleplayExtrasModule implements Listener {

    public SneakToPickupExperience() {
        super("gameplay.sneak-to-pickup.experience", false);
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
    private void on(PlayerPickupExperienceEvent event) {
        if (!event.getPlayer().isSneaking()) {
            event.setCancelled(true);
        }
    }
}
