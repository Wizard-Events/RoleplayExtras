package ron.thewizard.roleplayextras.modules;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import ron.thewizard.roleplayextras.utils.CommonUtil;
import ron.thewizard.roleplayextras.utils.LocationUtil;

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

public class BoatDespawnTimer extends RoleplayExtrasModule implements Consumer<ScheduledTask>, Listener {

    private final Map<UUID, Long> despawnCountdowns = new HashMap<>();
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
        plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(plugin, this, checkPeriodTicks, checkPeriodTicks);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void accept(ScheduledTask task) {
        for (World world : plugin.getServer().getWorlds()) {
            if (!worlds.contains(world.getName())) continue;

            for (Boat boat : world.getEntitiesByClass(Boat.class)) {
                if (boat.customName() != null) { // If boat has custom name, it's allowed to exist infinitely
                    logger().info("Ignoring {} at {} because it's nametagged",
                            boat.getType(), LocationUtil.toString(boat.getLocation()));
                    despawnCountdowns.remove(boat.getUniqueId());
                    continue;
                }

                if (containsDelayingPassenger(boat.getPassengers())) {
                    logger().info("Resetting despawn countdown for {} at {} because of delaying passenger",
                            boat.getType(), LocationUtil.toString(boat.getLocation()));
                    despawnCountdowns.remove(boat.getUniqueId());
                    continue;
                }

                long ticksLeft = despawnCountdowns.computeIfAbsent(boat.getUniqueId(), k -> removalCountdownTicks);

                if (ticksLeft <= 0) {
                    logger().info("Removing {} at {} (lifetime: {} ticks or {})",
                            boat.getType(), LocationUtil.toString(boat.getLocation()), boat.getTicksLived(),
                            CommonUtil.formatDuration(Duration.ofMillis(boat.getTicksLived() * 50L)));
                    boat.remove();
                    continue;
                }

                ticksLeft = ticksLeft - checkPeriodTicks;
                despawnCountdowns.put(boat.getUniqueId(), ticksLeft);
                logger.info("{} at {} will be removed in {} ticks or {}",
                        boat.getType(), boat.getLocation(), ticksLeft,
                        CommonUtil.formatDuration(Duration.ofSeconds(ticksLeft * 50L)));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void on(EntityRemoveFromWorldEvent event) {
        despawnCountdowns.remove(event.getEntity().getUniqueId()); // Avoid possible memory leak
    }

    private boolean containsDelayingPassenger(List<Entity> passengers) {
        for (Entity passenger : passengers) {
            if (passengerTypes.contains(passenger.getType())) {
                return true;
            }
        }
        return false;
    }
}
