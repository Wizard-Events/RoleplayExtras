package ron.thewizard.roleplayextras.util.permissions;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BukkitPermissionHandler implements PermissionHandler, Listener {

    private final Map<Permissible, Cache<String, TriState>> permissibleStateCacheMap;

    BukkitPermissionHandler(JavaPlugin plugin) {
        permissibleStateCacheMap = new ConcurrentHashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        permissibleStateCacheMap.keySet().forEach(this::flushCache);
    }

    @Override
    public TriState permissionValue(Permissible permissible, String permission) {
        return getPermissionStateCache(permissible).get(permission, p ->
                permissible.isPermissionSet(p) ? TriState.of(permissible.hasPermission(p)) : TriState.UNDEFINED);
    }

    @Override
    public void setPermission(Permissible permissible, String permission, TriState state) {
        for (PermissionAttachmentInfo attachmentInfo : permissible.getEffectivePermissions()) {
            if (attachmentInfo.getAttachment() == null) {
                continue;
            }

            if (attachmentInfo.getPermission().equals(permission)) {
                if (state == TriState.UNDEFINED) {
                    permissible.removeAttachment(attachmentInfo.getAttachment());
                } else {
                    permissible.addAttachment(attachmentInfo.getAttachment().getPlugin(), permission, state.toBoolean());
                }
                getPermissionStateCache(permissible).put(permission, state);
            }
        }
    }

    private Cache<String, TriState> getPermissionStateCache(Permissible permissible) {
        return permissibleStateCacheMap.computeIfAbsent(permissible, p ->
                Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(5)).build());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerQuit(PlayerQuitEvent event) {
        flushCache(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerKick(PlayerKickEvent event) {
        flushCache(event.getPlayer());
    }

    private void flushCache(Permissible permissible) {
        if (permissibleStateCacheMap.containsKey(permissible)) {
            permissibleStateCacheMap.get(permissible).invalidateAll();
            permissibleStateCacheMap.get(permissible).cleanUp();
            permissibleStateCacheMap.remove(permissible);
        }
    }
}
