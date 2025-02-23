package ron.thewizard.roleplayextras.modules;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ron.thewizard.roleplayextras.RoleplayExtras;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MarshmallowRoasting extends RoleplayExtrasModule implements Listener {

    private final Map<UUID, ScheduledTask> roastMap = new HashMap<>();
    private final Material marshmallowMaterial;
    private final long roastTimeTicks;
    private final int unroastedId, roastedId, radius;

    public MarshmallowRoasting() {
        super("gameplay.marshmallow-roasting", false, """
                Right-clicking with the configured item will place smoke particles in front
                of the players face.""");
        this.roastTimeTicks = config.getLong(configPath + ".marshmallow.roast-time-ticks", 100L);
        Material defaultMarshmallow = Material.SHIELD;
        Material configuredMallow;
        try {
            configuredMallow = Material.valueOf(config.getString(configPath + ".marshmallow.material", defaultMarshmallow.name()));
        } catch (IllegalArgumentException e) {
            configuredMallow = defaultMarshmallow;
        }
        this.marshmallowMaterial = configuredMallow;
        this.unroastedId = config.getInt(configPath + ".marshmallow.unroasted-model", 9);
        this.roastedId = config.getInt(configPath + ".marshmallow.roasted-model", 10);
        this.radius = config.getInt(configPath + ".near-fire-search-radius", 3);
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    private void on(PlayerInteractEvent event) {
        if (event.getAction().isLeftClick()) {
            if (isMarshmallow(event.getItem(), roastedId)) {
                event.getItem().setCustomModelData(unroastedId);
            }
            return;
        }

        if (!isMarshmallow(event.getItem(), unroastedId)) return;

        final Player player = event.getPlayer();

        roastMap.computeIfAbsent(player.getUniqueId(), uuid ->
                RoleplayExtras.scheduling().entitySpecificScheduler(player).runDelayed(() -> {
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    if (isMarshmallow(mainHand, unroastedId) && isNearFire(player.getLocation())) {
                        mainHand.setCustomModelData(roastedId);
                    }
                    roastMap.remove(uuid);
                }, null, roastTimeTicks));
    }

    private boolean isMarshmallow(ItemStack itemStack, int roasted) {
        return      itemStack != null
                &&  itemStack.getType() == marshmallowMaterial
                &&  itemStack.hasCustomModelData()
                &&  itemStack.getCustomModelData() == roasted;
    }

    private boolean isNearFire(Location location) {
        final World world = location.getWorld();
        final int centerX = location.getBlockX();
        final int centerY = location.getBlockY();
        final int centerZ = location.getBlockZ();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                for (int y = Math.max(world.getMinHeight(), centerY + radius); y > Math.min(world.getMaxHeight(), centerY - radius); y--) {
                    final Block block = world.getBlockAt(x, y, z);

                    if (block.getType() == Material.CAMPFIRE || block.getType() == Material.SOUL_CAMPFIRE) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
