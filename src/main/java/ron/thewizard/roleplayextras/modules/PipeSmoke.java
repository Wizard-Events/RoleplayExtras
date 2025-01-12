package ron.thewizard.roleplayextras.modules;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PipeSmoke extends RoleplayExtrasModule implements Listener {

    private final Material pipeMaterial;
    private final Particle particleType;
    private final double offsetY, speed, headDistance;
    private final int particleCount;

    public PipeSmoke() {
        super("gameplay.pipe-smoke-particles", true, """
                Right-clicking with the configured item will place smoke particles in front
                of the players face.""");
        Material defaultPipe = Material.GOAT_HORN;
        Material configuredPipe;
        try {
            configuredPipe = Material.valueOf(config.getString(configPath + ".pipe-material", defaultPipe.name()));
        } catch (IllegalArgumentException e) {
            configuredPipe = defaultPipe;
        }
        this.pipeMaterial = configuredPipe;

        Particle defaultParticle = Particle.CAMPFIRE_COSY_SMOKE;
        Particle configuredParticle;
        try {
            configuredParticle = Particle.valueOf(config.getString(configPath + ".particle-type", defaultParticle.name()));
        } catch (IllegalArgumentException e) {
            configuredParticle = defaultParticle;
        }
        this.particleType = configuredParticle;

        this.particleCount = config.getInt(configPath + ".particle-count", 6);
        this.offsetY = config.getDouble(configPath + ".offset-y", 0);
        this.speed = config.getDouble(configPath + ".speed", 0.01);
        this.headDistance = config.getDouble(configPath + ".head-distance", 0.5, """
                A positive value will put the smoke in front of the player's head.
                A negative value will put it behind the player's head.""");
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    private void on(PlayerInteractEvent event) {
        if (event.getAction().isLeftClick() || event.getMaterial() != pipeMaterial) return;

        final Location eyeLocation = event.getPlayer().getEyeLocation().clone();

        event.getPlayer().getWorld().spawnParticle(
                particleType,
                eyeLocation.add(eyeLocation.getDirection().multiply(headDistance)),
                particleCount,
                0,
                offsetY,
                0,
                speed);
    }
}
