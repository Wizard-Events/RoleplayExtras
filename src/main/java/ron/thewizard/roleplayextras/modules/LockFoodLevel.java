package ron.thewizard.roleplayextras.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class LockFoodLevel extends RoleplayExtrasModule implements Listener {

    private final int foodLevel;
    private final boolean doHardLocking;

    public LockFoodLevel() {
        super("gameplay.lock-hunger-level", true);
        this.doHardLocking = config.getBoolean(configPath + ".do-hard-locking", true, """
                If set to true, players won't be able to go above or below
                the set food level no matter what. Interacting with food will
                have no effects.
                When set to false, players just won't be able to go above
                this level. They will still loose food points like in vanilla
                and therefore have to eat.""");
        this.foodLevel = config.getInt(configPath + ".food-level-cap", 19, """
                Vanilla max is 20. Every hunger symbol has 2 food points.""");
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
    private void on(FoodLevelChangeEvent event) {
        event.setFoodLevel(doHardLocking ? foodLevel : Math.min(event.getFoodLevel(), foodLevel));
        logger().info("Set level to {}/20 in {} for player {}",
                event.getFoodLevel(), event.getEventName(), event.getEntity().getName());
    }
}
