package ron.thewizard.roleplayextras.modules;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PipeSmoke extends RoleplayExtrasModule implements Listener {

    private final Particle particleType;
    private final double offsetX, offsetY, offsetZ, speed, headDistance;
    private final int particleCount;

    public PipeSmoke() {
        super("gameplay.pipe-smoke-particles", true);
        Particle defaultParticle = Particle.CAMPFIRE_COSY_SMOKE;
        Particle configuredParticle;
        try {
            configuredParticle = Particle.valueOf(config.getString(configPath + ".particle-type", defaultParticle.name()));
        } catch (IllegalArgumentException e) {
            configuredParticle = defaultParticle;
        }
        this.particleType = configuredParticle;
        this.particleCount = config.getInt(configPath + ".particle-count", 20);
        this.offsetX = config.getDouble(configPath + ".offset-x", 0);
        this.offsetY = config.getDouble(configPath + ".offset-y", 0);
        this.offsetZ = config.getDouble(configPath + ".offset-z", 0);
        this.speed = config.getDouble(configPath + ".speed", 0.05);
        this.headDistance = config.getDouble(configPath + ".head-distance", 0.5);
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

    @EventHandler
    public void on(PlayerInteractEvent event) {
        if (event.getMaterial() != Material.GOAT_HORN) return;

        Location location = event.getPlayer().getEyeLocation();

        event.getPlayer().getWorld().spawnParticle(
                particleType,
                location.add(location.getDirection().multiply(headDistance)),
                particleCount,
                offsetX, offsetY, offsetZ,
                speed
        );
    }
}
