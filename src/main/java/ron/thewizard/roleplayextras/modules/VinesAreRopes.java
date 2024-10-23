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
import ron.thewizard.roleplayextras.RoleplayExtras;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VinesAreRopes extends RoleplayExtrasModule implements Listener {

    private final Set<Material> vines;
    private final long tickRate;
    private final int minLength, maxLength;
    private final boolean requireSolidBlock, enableUnwindFromTop;

    public VinesAreRopes() {
        super("gameplay.vines-are-ropes", true, """
                Will turn vines into usable ropes that unwind on place.""");
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
        this.tickRate = config.getLong(configPath + ".growth.tick-rate", 3L, """
                Will grow one block every x ticks.""");
        int configuredMinLength = config.getInt(configPath + ".growth.min-length", 6);
        int configuredMaxLength = config.getInt(configPath + ".growth.max-length", 16);
        this.minLength = Math.min(configuredMinLength, configuredMaxLength);
        this.maxLength = Math.max(configuredMinLength, configuredMaxLength);
        this.requireSolidBlock = config.getBoolean(configPath + ".growth.require-solid-block", true);
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
        if (event.useInteractedBlock() == Event.Result.ALLOW) return;
        if (!vines.contains(event.getMaterial())) return;
        if (requireSolidBlock && !event.getClickedBlock().isSolid()) return;

        // Check if we are allowed to build here first
        final BlockPlaceEvent blockPlaceEvent = new BlockPlaceEvent(
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

        final Block startBlock;

        if (event.getBlockFace() == BlockFace.UP) { // Allows standing on top of a block and roping down
            if (!event.getPlayer().isSneaking()) return; // Be sure the player intends to do this and not just miss-clicking
            Block blockBelowClicked = event.getClickedBlock().getRelative(BlockFace.DOWN);
            if (!blockBelowClicked.getType().isAir()) return; // No need

            startBlock = blockBelowClicked;

            event.setCancelled(true); // Cancel because we will do the placing to bypass any vanilla restrictions
            startBlock.setType(event.getMaterial(), true);
            if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                event.getItem().subtract();
            }
        } else {
            startBlock = event.getClickedBlock().getRelative(event.getBlockFace());
            // If the block we want to place would be denied, we will have to do it manually.
            if (event.useItemInHand() != Event.Result.ALLOW) { // If placement works normally, don't touch so animations are played
                startBlock.setType(event.getMaterial(), true);
                if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                    event.getItem().subtract();
                }
            }
        }

        // Schedule rope placement for cool and configurable visual
        scheduling.regionSpecificScheduler(startBlock.getLocation())
                .runAtFixedRate(new UnwindRopeTask(startBlock, minLength, maxLength), 1L, tickRate);
    }

    private static class UnwindRopeTask implements Consumer<ScheduledTask> {

        private final Block startBlock;
        private final int maxLength;
        private int currentLength;

        private UnwindRopeTask(Block startBlock, int min, int max) {
            this.startBlock = startBlock;
            this.maxLength = min == max ? max : RoleplayExtras.getRandom().nextInt(min, max);
            this.currentLength = 1;
        }

        @Override
        public void accept(ScheduledTask scheduledTask) {
            if (currentLength >= maxLength) {
                scheduledTask.cancel();
                return;
            }

            Block relative = startBlock.getRelative(BlockFace.DOWN, currentLength);

            if (!relative.getType().isAir()) {
                scheduledTask.cancel();
                return;
            }

            relative.setType(startBlock.getType(), true);
            currentLength++;
        }
    }
}
