package ron.thewizard.roleplayextras.modules;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import ron.thewizard.roleplayextras.utils.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

public class DontPokeTheBeehive extends RoleplayExtrasModule implements Listener {

    private final int beeCount, anger;
    private final double randomTargetRange;

    public DontPokeTheBeehive() {
        super("gameplay.bees-aggressive-when-poking-hive", false, """
                Poking beehive by left clicking it spawns angry bees.""");
        this.beeCount = config.getInt(configPath + ".bee-count", 30, """
                Amount of bees to spawn per poke (left-click)""");
        this.randomTargetRange = config.getDouble(configPath + ".target-acquire-radius", 16, """
                The radius in blocks around the player that clicked
                the beehive to randomly choose additional targets from""");
        this.anger = config.getInt(configPath + ".anger-ticks", 6000, """
                Time in ticks bees will stay angry and keep attacking""");
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
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        final Block block = event.getClickedBlock();
        if (block.getType() != Material.BEE_NEST) return;
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;

        List<Location> spawnLocations = new ArrayList<>();

        for (BlockFace blockFace : BlockFace.values()) {
            Block surround = block.getRelative(blockFace);
            if (surround.isEmpty()) {
                spawnLocations.add(surround.getLocation().toCenterLocation());
            }
        }

        if (spawnLocations.isEmpty()) {
            spawnLocations.add(block.getLocation());
        }

        List<Player> playerTargets = new ArrayList<>();
        playerTargets.add(event.getPlayer());
        playerTargets.addAll(event.getPlayer().getLocation().getNearbyPlayers(randomTargetRange));

        for (int i = 0; i < beeCount; i++) {
            event.getPlayer().getWorld()
                    .spawn(CollectionUtil.getRandomElement(spawnLocations), Bee.class, CreatureSpawnEvent.SpawnReason.BEEHIVE, bee -> {
                        bee.setHive(block.getLocation());
                        bee.setTarget(CollectionUtil.getRandomElement(playerTargets));
                        bee.setAggressive(true);
                        bee.setAnger(anger);
                    });
        }
    }
}
