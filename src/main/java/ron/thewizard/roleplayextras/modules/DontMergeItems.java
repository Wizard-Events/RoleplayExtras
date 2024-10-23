package ron.thewizard.roleplayextras.modules;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import ron.thewizard.roleplayextras.utils.EntityUtil;

public class DontMergeItems extends RoleplayExtrasModule implements Listener {

    private final float disperseIntensity;
    private final int minPickupDelayTicks;

    public DontMergeItems() {
        super("gameplay.dont-merge-dropped-items", true);
        this.disperseIntensity = (float) config.getDouble(configPath + ".disperse-intensity", 1.3, """
                Specifies the amount of spread for every single item when\s
                more than one is dropped at once.\s
                Vanilla is 0.5""");
        this.minPickupDelayTicks = config.getInt(configPath + ".min-pickup-delay-ticks", 15, """
                Helps prevent the scenario where players try to throw\s
                a stack of items and immediately pick them back up on\s
                the next tick. Therefore making it impossible to share\s
                an item.\s
                20 ticks = 1 second. 15 ticks should be just enough to\s
                be effective while not being intrusive.\s
                Vanilla is at 10 ticks.""");
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
    private void on(ItemMergeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void on(ItemSpawnEvent event) {
        Item mergedItem = event.getEntity();
        ItemStack mergedItemStack = mergedItem.getItemStack();
        if (mergedItemStack.getAmount() <= 1) return;

        event.setCancelled(true);
        /* I am the one who drops
        *  ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠿⠿⠿⠿⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
        *  ⣿⣿⣿⣿⣿⣿⣿⣿⠟⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠉⠻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
        *  ⣿⣿⣿⣿⣿⣿⣿⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢺⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
        *  ⣿⣿⣿⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠆⠜⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
        *  ⣿⣿⣿⣿⠿⠿⠛⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠻⣿⣿⣿⣿⣿
        *  ⣿⣿⡏⠁⠀⠀⠀⠀⠀⣀⣠⣤⣤⣶⣶⣶⣶⣶⣦⣤⡄⠀⠀⠀⠀⢀⣴⣿⣿⣿⣿⣿
        *  ⣿⣿⣷⣄⠀⠀⠀⢠⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⢿⡧⠇⢀⣤⣶⣿⣿⣿⣿⣿⣿⣿
        *  ⣿⣿⣿⣿⣿⣿⣾⣮⣭⣿⡻⣽⣒⠀⣤⣜⣭⠐⢐⣒⠢⢰⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿
        *  ⣿⣿⣿⣿⣿⣿⣿⣏⣿⣿⣿⣿⣿⣿⡟⣾⣿⠂⢈⢿⣷⣞⣸⣿⣿⣿⣿⣿⣿⣿⣿⣿
        *  ⣿⣿⣿⣿⣿⣿⣿⣿⣽⣿⣿⣷⣶⣾⡿⠿⣿⠗⠈⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
        *  ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠻⠋⠉⠑⠀⠀⢘⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
        *  ⣿⣿⣿⣿⣿⣿⣿⡿⠟⢹⣿⣿⡇⢀⣶⣶⠴⠶⠀⠀⢽⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
        *  ⣿⣿⣿⣿⣿⣿⡿⠀⠀⢸⣿⣿⠀⠀⠣⠀⠀⠀⠀⠀⡟⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
        *  ⣿⣿⣿⡿⠟⠋⠀⠀⠀⠀⠹⣿⣧⣀⠀⠀⠀⠀⡀⣴⠁⢘⡙⢿⣿⣿⣿⣿⣿⣿⣿⣿
        *  ⠉⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⠈⠙⢿⠗⠂⠄⠀⣴⡟⠀⠀⡃⠀⠉⠉⠟⡿⣿⣿⣿⣿
        *  ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢷⠾⠛⠂⢹⠀⠀⠀⢡⠀⠀⠀⠀⠀⠙⠛⠿⢿
        * */

        // Only call asOne once and reuse it for all spawns. That way we won't repeatedly clone the same ItemMeta
        // We can do this because spawning a new unique Item Entity doesn't require the ItemStack to be unique
        ItemStack singleItemStack = mergedItemStack.asOne();

        for (int i = 0; i < mergedItemStack.getAmount(); i++) {
            // Spawn single item with a natural feeling, random location offset.
            // We won't have to schedule this because on Folia, Entity Events are executed on the Entity's Thread
            EntityUtil.dropItemNaturally(mergedItem.getLocation(), disperseIntensity, singleItemStack, singleItem -> {
                // Try to respect all the original item's properties
                copyProperties(mergedItem, singleItem);
                // Require minimum pickup delay to prevent picking things back up immediately
                singleItem.setPickupDelay(Math.max(singleItem.getPickupDelay(), minPickupDelayTicks));
            });
        }
    }

    private void copyProperties(Item from, Item to) {
        to.setGravity(from.hasGravity());
        to.setVelocity(from.getVelocity());
        to.setFallDistance(from.getFallDistance());
        to.setFrictionState(from.getFrictionState());

        to.setOwner(from.getOwner());
        to.setThrower(from.getThrower());
        to.customName(from.customName());
        to.setCustomNameVisible(from.isCustomNameVisible());

        to.setWillAge(from.willAge());
        to.setHealth(from.getHealth());
        to.setInvulnerable(from.isInvulnerable());
        to.setUnlimitedLifetime(from.isUnlimitedLifetime());

        to.setGlowing(from.isGlowing());
        to.setFireTicks(from.getFireTicks());
        to.setVisualFire(from.isVisualFire());
        to.setVisibleByDefault(from.isVisibleByDefault());

        to.setSilent(from.isSilent());
        to.setPersistent(from.isPersistent());
        to.setPortalCooldown(from.getPortalCooldown());
    }
}
