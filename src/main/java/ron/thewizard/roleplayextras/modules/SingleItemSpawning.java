package ron.thewizard.roleplayextras.modules;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import ron.thewizard.roleplayextras.util.EntityUtil;

public class SingleItemSpawning extends RoleplayExtrasModule implements Listener {

    private final float disperse_x, disperse_y, disperse_z;
    private final int minPickupDelayTicks;
    private final boolean onlyPlayerThrown;

    private Listener itemDropListener;

    public SingleItemSpawning() {
        super("gameplay.single-item-spawning", true, """
                Makes spawned items spawn as single items and prevents them from
                merging into stacks.""");
        this.onlyPlayerThrown = config.getBoolean(configPath + ".only-thrown-by-players", false, """
                If enabled, will only disperse items thrown by players""");
        this.disperse_x = (float) config.getDouble(configPath + ".disperse-intensity.x", 1.2, """
                The multiplier applied to the random float between 0.0 and 1.0
                that will be added to the original drop location to get the item
                disperse effect. Vanilla is ~ 0.5""");
        this.disperse_y = (float) config.getDouble(configPath + ".disperse-intensity.y", 0.2);
        this.disperse_z = (float) config.getDouble(configPath + ".disperse-intensity.z", 1.2);
        this.minPickupDelayTicks = config.getInt(configPath + ".min-pickup-delay-ticks", 15, """
                Helps prevent the scenario where players try to throw
                a stack of items and immediately pick them back up on
                the next tick. Therefore making it impossible to share
                an item.
                20 ticks = 1 second. 15 ticks should be just enough to
                be effective while not being intrusive.
                Vanilla is at 10 ticks.""");
    }

    @Override
    public void enable() {
        itemDropListener = onlyPlayerThrown ? new PlayerListener(this) : new UniversalListener(this);
        plugin.getServer().getPluginManager().registerEvents(itemDropListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (itemDropListener != null) {
            HandlerList.unregisterAll(itemDropListener);
            itemDropListener = null;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void on(ItemMergeEvent event) {
        event.setCancelled(true);
    }

    private record PlayerListener(SingleItemSpawning module) implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        private void on(PlayerDropItemEvent event) {
            if (module.disperse(event.getItemDrop())) {
                event.setCancelled(true);
            }
        }
    }

    private record UniversalListener(SingleItemSpawning module) implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        private void on(ItemSpawnEvent event) {
            if (module.disperse(event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }

    private boolean disperse(Item spawningItem) {
        if (spawningItem.getItemStack().getAmount() < 2) {
            return false;
        }

        /*  ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠿⠿⠿⠿⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
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
         *           I am the one who drops.
         * */

        ItemStack singleItemStack = spawningItem.getItemStack().asOne();

        for (int i = 0; i < spawningItem.getItemStack().getAmount(); i++) {
            // Spawn single item with a natural feeling, random location offset.
            // We won't have to schedule this because on Folia, Entity Events are executed on the Entity's Thread
            EntityUtil.dropItemNaturally(spawningItem.getLocation(), disperse_x, disperse_y, disperse_z, singleItemStack, singleCopy -> {
                // Try to respect all the original item's properties
                EntityUtil.cloneItemProperties(spawningItem, singleCopy);
                // Require minimum pickup delay to prevent picking things back up immediately
                singleCopy.setPickupDelay(Math.max(singleCopy.getPickupDelay(), minPickupDelayTicks));
            });
        }

        return true;
    }
}
