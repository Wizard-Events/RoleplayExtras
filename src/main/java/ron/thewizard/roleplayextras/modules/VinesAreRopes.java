package ron.thewizard.roleplayextras.modules;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
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
    private final int minLength, maxLength, unwindFromTopMaxThickness;
    private final boolean requireSolidBlock, enableUnwindFromTop, consumeItem;

    public VinesAreRopes() {
        super("gameplay.vines-are-ropes", true, """
                Will turn vines into usable ropes that unwind on place.""");
        this.requireSolidBlock = config.getBoolean(configPath + ".require-solid-block", true, """
                Whether the block the rope is placed against has to be solid.""");
        this.consumeItem = config.getBoolean(configPath + ".consume-item", true, """
                If set to true, players """);
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
        this.enableUnwindFromTop = config.getBoolean(configPath + ".unwind-from-top.enable", true, """
                Allows a player to stand on top of a block and place a rope\s
                while sneaking. The rope will then unwind below that block.""");
        this.unwindFromTopMaxThickness = config.getInt(configPath + ".unwind-from-top.max-ledge-thickness", 3, """
                Imagine a plank on a pirate ship in minecraft.\s
                This is the maximum allowed thickness in blocks the plank is\s
                allowed to be for the rope effect to play.""");
        this.tickRate = config.getLong(configPath + ".growth.tick-rate", 3L, """
                Will grow one block every x ticks.""");
        int configuredMinLength = config.getInt(configPath + ".growth.min-length", 6);
        int configuredMaxLength = config.getInt(configPath + ".growth.max-length", 16);
        this.minLength = Math.min(configuredMinLength, configuredMaxLength);
        this.maxLength = Math.max(configuredMinLength, configuredMaxLength);
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Note: The API actually specifically advises AGAINST using this method for placing blocks where they shouldn't belong.
     * <p>
     * Quote:
     * "This method should NOT be used to "hack" physics by placing blocks in impossible locations.
     * Such blocks are liable to be removed on various events such as world upgrades.
     * Furthermore setting large amounts of such blocks in close proximity may overload the server
     * physics engine if an update is triggered at a later point. If this occurs, the resulting
     * behavior is undefined."
     * <p>
     * HOWEVER, not only does it work for us, its also completely safe since we are not creating entire chunks out of vines
     * and only placing some here and there.
     */
    private static void placeBlockIgnoringVanillaRestrictions(Material material, Block block) {
        try {
            block.setType(material, true); // Apply physics so blockStates can change naturally (ex. visually merge)
        } catch (Throwable t) {
            RoleplayExtras.logger().error("<gameplay.vines-are-ropes> Can't place block with material {}!", material, t);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void on(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!vines.contains(event.getMaterial())) return;
        if (requireSolidBlock && !event.getClickedBlock().isSolid()) return;

        // Ask any possible building permission plugin if the player can build here by simulating block placement
        Block placed = event.getClickedBlock().getRelative(event.getBlockFace());
        BlockState replacedBlockState = placed.getState(true);
        replacedBlockState.setType(event.getMaterial());
        final BlockPlaceEvent blockPlaceEvent = new BlockPlaceEvent(
                placed,
                replacedBlockState,
                event.getClickedBlock(),
                event.getItem(),
                event.getPlayer(),
                true,
                event.getHand()
        );

        // If the player isn't allowed to build here, don't continue
        if (!blockPlaceEvent.callEvent() || !blockPlaceEvent.canBuild()) return;

        final @NotNull Block startBlock;

        if (event.getBlockFace() == BlockFace.UP) {
            if (!enableUnwindFromTop) return;

            // We use sneaking as an indicator for the player intending to do this
            if (!event.getPlayer().isSneaking()) return;

            // Look for possible rope placement block below clicked block
            int blocks = 1;
            do {
                Block below = event.getClickedBlock().getRelative(BlockFace.DOWN, blocks);
                if (below.getType().isAir()) { // Winner
                    startBlock = below;
                    break;
                }
                if (blocks == unwindFromTopMaxThickness) {
                    return;
                }
                blocks++;
            } while (true);

            // Cancel use because we don't want to place the vine on top
            event.setUseItemInHand(Event.Result.DENY);

            placeBlockIgnoringVanillaRestrictions(event.getMaterial(), startBlock);

            // Consume used item for survival players because we are cancelling the original interaction
            if (consumeItem && event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                event.getItem().subtract();
            }
        } else {
            startBlock = event.getClickedBlock().getRelative(event.getBlockFace());
            if (event.useItemInHand() != Event.Result.ALLOW) {
                // If the block we want to place wouldn't be placeable, we will have to do it manually.
                placeBlockIgnoringVanillaRestrictions(event.getMaterial(), startBlock);
                // Consume used item for survival players because the original interaction wouldn't happen by default
                if (consumeItem && event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                    event.getItem().subtract();
                }
            }
        }

        // No interacting with clicked blocks when unwinding a rope
        event.setUseInteractedBlock(Event.Result.DENY);

        // Prevent player from any natural item consumption if disabled
        if (!consumeItem) {
            event.setUseItemInHand(Event.Result.DENY);
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

            VinesAreRopes.placeBlockIgnoringVanillaRestrictions(startBlock.getType(), relative);
            currentLength++;
        }
    }
}
