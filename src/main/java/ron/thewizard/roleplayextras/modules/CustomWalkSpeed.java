package ron.thewizard.roleplayextras.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class CustomWalkSpeed extends RoleplayExtrasModule implements Listener {

    private final float walkSpeed;

    public CustomWalkSpeed() {
        super("gameplay.custom-walk-speed", true);
        this.walkSpeed = (float) config.getDouble(configPath + ".speed", 0.4);
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
    private void on(PlayerJoinEvent event) {
        event.getPlayer().setWalkSpeed(walkSpeed);
    }
}
