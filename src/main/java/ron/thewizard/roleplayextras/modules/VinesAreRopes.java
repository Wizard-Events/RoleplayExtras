package ron.thewizard.roleplayextras.modules;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void on(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getBlockFace() == BlockFace.UP) return;
        if (!vines.contains(event.getMaterial())) return;

        // Check if we are allowed to build here first
        BlockPlaceEvent blockPlaceEvent = new BlockPlaceEvent(
                event.getClickedBlock(), // Dummy information, important part is that it works for the check
                event.getClickedBlock().getState(true),
                event.getClickedBlock(),
                event.getItem(),
                event.getPlayer(),
                true,
                event.getHand()
        );

        // If the player isn't allowed to build here, don't continue
        if (!blockPlaceEvent.callEvent() || !blockPlaceEvent.canBuild()) return;

        Block start = event.getClickedBlock().getRelative(event.getBlockFace());

        // If the block we want to place would be denied, we will have to do it manually
        if (event.useItemInHand() != Event.Result.ALLOW) {
            start.setType(event.getMaterial(), true);
            if (event.getPlayer().getGameMode() == GameMode.SURVIVAL)
                event.getItem().subtract();
        }

        // Schedule rope placement for cool and configurable visual
        scheduling.regionSpecificScheduler(event.getPlayer().getLocation())
                .runAtFixedRate(new RopeDownTask(start, event.getMaterial(), maxLength), 1L, tickDelay);
    }

    private static class RopeDownTask implements Consumer<ScheduledTask> {

        private final Block startBlock;
        private final Material ropeType;
        private final int maxDistance;
        private int currentDistance;

        public RopeDownTask(Block startBlock, Material ropeType, int maxDistance) {
            this.startBlock = startBlock;
            this.ropeType = ropeType;
            this.maxDistance = maxDistance;
            this.currentDistance = 1;
        }

        @Override
        public void accept(ScheduledTask scheduledTask) {
            if (currentDistance >= maxDistance) {
                scheduledTask.cancel();
                return;
            }

            Block relative = startBlock.getRelative(BlockFace.DOWN, currentDistance);

            if (!relative.getType().isAir()) {
                scheduledTask.cancel();
                return;
            }

            relative.setType(ropeType, true);
            currentDistance++;
        }
    }
}
