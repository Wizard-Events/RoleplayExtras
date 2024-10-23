package ron.thewizard.roleplayextras.modules;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SprintBoost extends RoleplayExtrasModule implements Listener, PacketListener {

    private Map<UUID, AcceleratedPlayer> playerTracker;
    private PacketListenerAbstract packetListenerAbstract;

    private final long durationMillis;
    private final float speedMultiplier;
    private final boolean stackDuration, startOnDropItem, startOnSwapOffHand, stopOnSneak;

    public SprintBoost() {
        super("gameplay.sprint-boost", true, """
                Applies a configurable multiplier to a player's walkspeed,\s
                if they drop an item or swap offhand while sprinting.""");
        this.startOnSwapOffHand = config.getBoolean(configPath + ".start-button.swap-off-hand", true);
        this.startOnDropItem = config.getBoolean(configPath + ".start-button.drop-item", true);
        this.stopOnSneak = config.getBoolean(configPath + ".stop-button.sneak", true);
        this.speedMultiplier = (float) config.getDouble(configPath + ".speed-multiplier", 1.8, """
                The multiplier that will be applied to the player's\s
                walkspeed on activation.\s
                1.8 means a 80% speed increase.""");
        this.durationMillis = config.getLong(configPath + ".duration-millis", 5000, """
                How the player will be able to walk fast after triggering\s
                the feature.""");
        this.stackDuration = config.getBoolean(configPath + ".stack-duration", true, """
                If set to true, the duration time will be extended by\s
                an additional duration-millis if the player is already\s
                boosted.""");
    }

    @Override
    public void enable() {
        playerTracker = new ConcurrentHashMap<>(); // Packet listener is async
        packetListenerAbstract = asAbstract(PacketListenerPriority.MONITOR);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        PacketEvents.getAPI().getEventManager().registerListener(packetListenerAbstract);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (packetListenerAbstract != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(packetListenerAbstract);
            packetListenerAbstract = null;
        }
        if (playerTracker != null) {
            playerTracker.forEach((uuid, acceleratedPlayer) -> decelerate(acceleratedPlayer));
            playerTracker = null;
        }
    }

    private boolean canAccelerate(Player player) {
        return player.isSprinting() || player.isJumping();
    }

    private boolean isAccelerated(UUID uuid) {
        return playerTracker.containsKey(uuid);
    }

    private void decelerate(AcceleratedPlayer acceleratedPlayer) {
        acceleratedPlayer.revertSpeed();
        playerTracker.remove(acceleratedPlayer.player.getUniqueId());
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getConnectionState() != ConnectionState.PLAY) return;
        if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;

        Player player = event.getPlayer();
        if (!canAccelerate(player)) return;

        WrapperPlayClientPlayerDigging playerDigging = new WrapperPlayClientPlayerDigging(event);

        if (startOnSwapOffHand && playerDigging.getAction() == DiggingAction.SWAP_ITEM_WITH_OFFHAND
            || startOnDropItem && playerDigging.getAction() == DiggingAction.DROP_ITEM) {

            AcceleratedPlayer acceleratedPlayer = playerTracker.computeIfAbsent(player.getUniqueId(),
                    uuid -> new AcceleratedPlayer(player, speedMultiplier, durationMillis));

            if (stackDuration) {
                acceleratedPlayer.extendDuration(durationMillis);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void on(PlayerMoveEvent event) {
        if (!isAccelerated(event.getPlayer().getUniqueId())) return;

        AcceleratedPlayer acceleratedPlayer = playerTracker.get(event.getPlayer().getUniqueId());

        if (acceleratedPlayer.isExpired() || !canAccelerate(acceleratedPlayer.player)) {
            decelerate(acceleratedPlayer);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void on(PlayerToggleSneakEvent event) {
        if (stopOnSneak && event.isSneaking() && isAccelerated(event.getPlayer().getUniqueId())) {
            decelerate(playerTracker.get(event.getPlayer().getUniqueId()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void on(PlayerQuitEvent event) {
        if (playerTracker.containsKey(event.getPlayer().getUniqueId())) {
            decelerate(playerTracker.get(event.getPlayer().getUniqueId()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void on(PlayerKickEvent event) {
        if (playerTracker.containsKey(event.getPlayer().getUniqueId())) {
            decelerate(playerTracker.get(event.getPlayer().getUniqueId()));
        }
    }

    private static class AcceleratedPlayer {

        private final Player player;
        private final AtomicLong expireTimeMillis;
        private final float previousWalkSpeed;

        private AcceleratedPlayer(Player player, float multiplier, long durationMillis) {
            this.player = player;
            this.previousWalkSpeed = this.player.getWalkSpeed(); // So we can go back to it on expire
            this.player.setWalkSpeed(this.previousWalkSpeed * multiplier); // Accelerate
            this.expireTimeMillis = new AtomicLong(System.currentTimeMillis() + durationMillis);
        }

        private boolean isExpired() {
            return System.currentTimeMillis() >= expireTimeMillis.get();
        }

        private void extendDuration(long millis) {
            expireTimeMillis.addAndGet(millis);
        }

        private void revertSpeed() {
            player.setWalkSpeed(previousWalkSpeed);
        }
    }
}
