package ron.thewizard.roleplayextras.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class CustomWalkSpeed extends RoleplayExtrasModule implements Listener {

    private final float walkSpeed;
    private final boolean firstJoinOnly;

    public CustomWalkSpeed() {
        super("gameplay.custom-walk-speed", true);
        this.walkSpeed = (float) config.getDouble(configPath + ".speed", 0.4,
                "Must be between -1.0 and 1.0.");
        this.firstJoinOnly = config.getBoolean(configPath + ".first-join-only", true);
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
        if (firstJoinOnly && event.getPlayer().hasPlayedBefore()) {
            logger().info("Ignoring player {} because they are not a new player", event.getPlayer().getName());
            return;
        }

        event.getPlayer().setWalkSpeed(walkSpeed);
        logger().info("Set walk speed of player {} to {}", event.getPlayer().getName(), walkSpeed);
    }
}
