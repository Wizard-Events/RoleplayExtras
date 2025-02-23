package ron.thewizard.roleplayextras.modules;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MarshmallowRoasting extends RoleplayExtrasModule implements Listener {

    private final Material marshmallowMaterial;
    private final Particle particleType;
    private final long roastDurationTicks, initialDelayTicks, periodTicks;
    private final double offsetY, speed, headDistance;
    private final int unroastedId, roastedId, radius, particleCount;
    private final boolean smokeWhileRoast, smokeOnFinish;

    private Map<Player, ScheduledTask> roastMap = new ConcurrentHashMap<>();

    public MarshmallowRoasting() {
        super("gameplay.marshmallow-roasting", false, """
                Right-clicking with the configured item will place smoke particles in front
                of the players face.""");
        this.roastDurationTicks = config.getLong(configPath + ".marshmallow.roast-time-ticks", 100L);
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

        this.smokeWhileRoast = config.getBoolean(configPath + ".smoke-particles.while-roasting", true);
        this.smokeOnFinish = config.getBoolean(configPath + ".smoke-particles.when-finished-roasting", true);

        this.initialDelayTicks = config.getLong(configPath + ".smoke-particles.initial-delay", 20L);
        this.periodTicks = config.getLong(configPath + ".smoke-particles.period-ticks", 30L);

        Particle defaultParticle = Particle.POOF;
        Particle configuredParticle;
        try {
            configuredParticle = Particle.valueOf(config.getString(configPath + ".smoke-particles.particle-type", defaultParticle.name()));
        } catch (IllegalArgumentException e) {
            configuredParticle = defaultParticle;
        }
        this.particleType = configuredParticle;
        this.particleCount = config.getInt(configPath + ".smoke-particles.particle-count", 1);
        this.headDistance = config.getDouble(configPath + ".smoke-particles.head-distance", 1.0);
        this.offsetY = config.getDouble(configPath + ".smoke-particles.offset-y", 0.3);
        this.speed = config.getDouble(configPath + ".smoke-particles.speed", 0.03);
    }

    @Override
    public void enable() {
        roastMap = new ConcurrentHashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (roastMap != null) {
            roastMap.forEach((player, scheduledTask) -> scheduledTask.cancel());
            roastMap.clear();
            roastMap = null;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    private void on(PlayerInteractEvent event) {
        if (event.getAction().isLeftClick()) {
            // Assume player wants to restock with un-roasted marshmallow
            if (isMarshmallow(event.getItem(), roastedId)) {
                event.getItem().setCustomModelData(unroastedId);
            }
        } else {
            // Assume player wants to roast marshmallow
            if (isMarshmallow(event.getItem(), unroastedId)) {
                roastMap.computeIfAbsent(event.getPlayer(), player -> player.getScheduler().runAtFixedRate(
                        plugin,
                        new MarshmallowRoastTask(this, player, roastDurationTicks),
                        null,
                        initialDelayTicks,
                        periodTicks));
            }
        }
    }

    public static class MarshmallowRoastTask implements Consumer<ScheduledTask> {

        private final MarshmallowRoasting module;
        private final Player player;
        private long ticksLeft;

        public MarshmallowRoastTask(MarshmallowRoasting module, Player player, long ticksLeft) {
            this.module = module;
            this.player = player;
            this.ticksLeft = ticksLeft;
        }

        @Override
        public void accept(ScheduledTask scheduledTask) {
            if (!module.isNearFire(player.getLocation())) {
                scheduledTask.cancel();
                module.roastMap.remove(player);
                return;
            }

            if (ticksLeft <= 0) {
                scheduledTask.cancel();

                ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
                if (module.isMarshmallow(heldItem, module.unroastedId)) {
                    heldItem.setCustomModelData(module.roastedId);
                }

                if (module.smokeOnFinish) {
                    module.spawnSmoke(player);
                }

                module.roastMap.remove(player);
                return;
            }

            ticksLeft = ticksLeft - module.periodTicks;
            if (module.smokeWhileRoast) {
                module.spawnSmoke(player);
            }
        }
    }

    private void spawnSmoke(Player player) {
        Location eyeLocation = player.getEyeLocation().clone();
        player.getWorld().spawnParticle(
                particleType,
                eyeLocation
                        .add(eyeLocation.getDirection().multiply(headDistance))
                        .add(0, offsetY, 0),
                particleCount,
                0,
                0,
                0,
                speed);
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
