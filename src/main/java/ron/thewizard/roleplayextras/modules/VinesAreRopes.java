package ron.thewizard.roleplayextras.modules;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VinesAreRopes extends RoleplayExtrasModule implements Listener {

    private final Set<Material> vines;
    private final long tickDelay;
    private final int maxLength;

    public VinesAreRopes() {
        super("gameplay.vines-are-ropes", true);
        this.tickDelay = config.getLong(configPath + ".rope-effect-tick-rate", 3L);
        this.maxLength = config.getInt(configPath + ".max-length", 16);
        this.vines = config.getList(configPath + ".affected-vines", List.of("WEEPING_VINES"))
                .stream()
                .map(configuredMaterial -> {
                    try {
                        return Material.valueOf(configuredMaterial);
                    } catch (IllegalArgumentException e) {
                        notRecognized(Material.class, configuredMaterial);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    private void on(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getBlockFace() == BlockFace.UP) return;
        if (!vines.contains(event.getMaterial())) return;

        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            event.getItem().subtract();
        }

        Block ropeStart = event.getClickedBlock().getRelative(event.getBlockFace());

        // Enable placing of normally unplaceable blocks
        ropeStart.setType(event.getMaterial(), true);

        // Schedule rope placement for cool and configurable visual
        scheduling.regionSpecificScheduler(event.getPlayer().getLocation())
                .runAtFixedRate(new RopeDownTask(ropeStart, event.getMaterial(), maxLength), 1L, tickDelay);
    }

    private static class RopeDownTask implements Consumer<ScheduledTask> {

        private final Block startBlock;
        private final Material ropeType;
        private final int maxLen;
        private int distance;

        public RopeDownTask(Block startBlock, Material ropeType, int maxLen) {
            this.startBlock = startBlock;
            this.ropeType = ropeType;
            this.maxLen = maxLen;
            this.distance = 1;
        }

        @Override
        public void accept(ScheduledTask scheduledTask) {
            if (distance >= maxLen) {
                scheduledTask.cancel();
                return;
            }

            Block relative = startBlock.getRelative(BlockFace.DOWN, distance);

            if (!relative.getType().isAir()) {
                scheduledTask.cancel();
                return;
            }

            relative.setType(ropeType, true);
            distance++;
        }
    }
}
