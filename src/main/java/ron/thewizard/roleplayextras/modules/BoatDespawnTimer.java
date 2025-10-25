package ron.thewizard.roleplayextras.modules;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import ron.thewizard.roleplayextras.util.CommonUtil;
import ron.thewizard.roleplayextras.util.EntityUtil;
import ron.thewizard.roleplayextras.util.LocationUtil;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BoatDespawnTimer extends RoleplayExtrasModule implements Listener {

    private final Map<UUID, ScheduledTask> watchdogTasks = new HashMap<>();
    private final Set<EntityType> passengerTypes;
    private final Set<String> worlds;
    private final long removalCountdownTicks, checkPeriodTicks;

    public BoatDespawnTimer() {
        super("gameplay.boat-despawn-timer", true, """
                Removes abandoned boats with a smart delay.""");
        this.removalCountdownTicks = config.getLong(configPath + ".despawn-ticks", 2 * 60 * 20);
        this.passengerTypes = config.getList(configPath + ".passengers-that-delay", List.of("PLAYER")).stream()
                .map(passengerType -> {
                    try {
                        return EntityType.valueOf(passengerType);
                    } catch (IllegalArgumentException e) {
                        notRecognized(EntityType.class, passengerType);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        this.checkPeriodTicks = config.getLong(configPath + ".check-period-ticks", 5 * 20);
        this.worlds = new HashSet<>(config.getList(configPath + ".worlds", List.of("world")));
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getGlobalRegionScheduler().run(plugin, getWorlds -> {
            for (World world : plugin.getServer().getWorlds()) {
                if (!worlds.contains(world.getName())) continue;

                for (Chunk chunk : world.getLoadedChunks()) {
                    if (!chunk.isEntitiesLoaded()) continue;

                    plugin.getServer().getRegionScheduler().run(plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(), getEntities -> {
                        for (Entity entity : chunk.getEntities()) {
                            if (EntityUtil.BOATS.get().contains(entity.getType())) {
                                attachWatchdogTask(entity);
                                logger().finest(() -> "Attached watchdog task to " + entity.getType() + " at " +
                                        LocationUtil.toString(entity.getLocation()));
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        watchdogTasks.forEach((uuid, task) -> cancelWatchdogTask(uuid));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void on(EntitySpawnEvent event) {
        if (EntityUtil.BOATS.get().contains(event.getEntityType())) {
            attachWatchdogTask(event.getEntity());
            logger().finest(() -> "Attached watchdog task to spawned " + event.getEntityType() + " at " +
                    LocationUtil.toString(event.getLocation()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void on(EntityTeleportEvent event) {
        if (EntityUtil.BOATS.get().contains(event.getEntityType())
                && event.getTo() != null && worlds.contains(event.getTo().getWorld().getName())) {
            attachWatchdogTask(event.getEntity());
            logger().finest(() -> "Attached watchdog task to " + event.getEntityType() + " teleporting from " +
                    LocationUtil.toString(event.getFrom()) + " to " + LocationUtil.toString(event.getTo()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void on(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (EntityUtil.BOATS.get().contains(entity.getType())) {
                attachWatchdogTask(entity);
                logger().finest(() -> "Attached watchdog task to now loaded " + entity.getType() + " at " +
                        LocationUtil.toString(entity.getLocation()));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void on(EntitiesUnloadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (!EntityUtil.BOATS.get().contains(entity.getType())) continue;
            if (entity.customName() != null || hasDelayingPassenger(entity)) continue;

            logger().info(() -> "Not saving " + entity.getType().name() + " in " + event.getEventName() + " at " +
                    LocationUtil.toString(entity.getLocation()) + " (lifetime: " + entity.getTicksLived() + " ticks or " +
                    CommonUtil.formatDuration(Duration.ofMillis(entity.getTicksLived() * 50L)) + ")");

            entity.setPersistent(false);
        }
    }

    private boolean hasDelayingPassenger(Entity boat) {
        for (Entity passenger : boat.getPassengers()) {
            if (passengerTypes.contains(passenger.getType())) {
                return true;
            }
        }
        return false;
    }

    private void attachWatchdogTask(Entity boat) {
        watchdogTasks.computeIfAbsent(boat.getUniqueId(), uuid -> boat.getScheduler().runAtFixedRate(
                plugin,
                new BoatWatchdog(this, boat),
                () -> watchdogTasks.remove(uuid),
                checkPeriodTicks,
                checkPeriodTicks
        ));
    }

    private void cancelWatchdogTask(UUID uuid) {
        if (watchdogTasks.containsKey(uuid)) {
            watchdogTasks.remove(uuid).cancel();
        }
    }

    private static final class BoatWatchdog implements Consumer<ScheduledTask> {

        private final BoatDespawnTimer module;
        private final Entity boat;

        private long lifeTimeTicksLeft;

        public BoatWatchdog(BoatDespawnTimer module, Entity boat) {
            this.module = module;
            this.boat = boat;
            this.lifeTimeTicksLeft = module.removalCountdownTicks;
        }

        @Override
        public void accept(ScheduledTask task) {
            if (!module.worlds.contains(boat.getWorld().getName())) {
                module.logger().fine(() -> "Ignoring " + boat.getType().name() + " at " +
                        LocationUtil.toString(boat.getLocation()) + " because boat is not in one of the configured worlds");
                module.cancelWatchdogTask(boat.getUniqueId());
                return;
            }

            if (boat.customName() != null) {
                lifeTimeTicksLeft = module.removalCountdownTicks;
                module.logger().fine(() -> "Ignoring " + boat.getType().name() + " at " +
                        LocationUtil.toString(boat.getLocation()) + " because boat has a custom name " +
                        "(name=" + PlainTextComponentSerializer.plainText().serialize(boat.customName()) + ")");
                return;
            }

            if (module.hasDelayingPassenger(boat)) {
                lifeTimeTicksLeft = module.removalCountdownTicks;
                module.logger().fine(() -> "Ignoring " + boat.getType().name() + " at " +
                        LocationUtil.toString(boat.getLocation()) + " because of delaying passenger");
                return;
            }

            if (lifeTimeTicksLeft > 0) {
                lifeTimeTicksLeft = lifeTimeTicksLeft - module.checkPeriodTicks;
                module.logger().finer(() -> boat.getType().name() + " at " + LocationUtil.toString(boat.getLocation()) +
                        " will be removed in " + lifeTimeTicksLeft + " ticks or " +
                        CommonUtil.formatDuration(Duration.ofMillis(lifeTimeTicksLeft * 50L)));
                return;
            }

            module.logger().info(() -> "Removed " + boat.getType().name() + " at " +
                    LocationUtil.toString(boat.getLocation()) + ". Lifetime: " + boat.getTicksLived() + " ticks or " +
                    CommonUtil.formatDuration(Duration.ofMillis(boat.getTicksLived() * 50L)));
            boat.remove();
            module.cancelWatchdogTask(boat.getUniqueId());
        }
    }
}
