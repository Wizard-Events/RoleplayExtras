package ron.thewizard.roleplayextras.modules;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.NumberConversions;
import ron.thewizard.roleplayextras.utils.permissions.PluginPermission;

import java.util.Iterator;

public class ChatProximity extends RoleplayExtrasModule implements Listener {

    private final double maxDistanceSquared;

    public ChatProximity() {
        super("gameplay.text-chat-proximity", true);
        this.maxDistanceSquared = NumberConversions.square(config.getDouble(configPath + ".max-block-distance", 13));
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
    private void on(AsyncChatEvent event) {
        if (PluginPermission.BYPASS_CHAT_PROXIMITY_SEND.test(event.getPlayer())) return;

        Iterator<Audience> audienceIterator = event.viewers().iterator();
        while (audienceIterator.hasNext()) {
            if (audienceIterator.next() instanceof Player messageReceiver
                    && !PluginPermission.BYPASS_CHAT_PROXIMITY_RECEIVE.test(messageReceiver)) {
                // Check if worlds are the same so distanceSquared never throws an IllegalArgumentException
                if (!messageReceiver.getWorld().getUID().equals(event.getPlayer().getWorld().getUID())
                        || messageReceiver.getLocation().distanceSquared(event.getPlayer().getLocation()) > maxDistanceSquared) {
                    audienceIterator.remove(); // Remove chat receiver if they are out of reach
                }
            }
        }
    }
}
