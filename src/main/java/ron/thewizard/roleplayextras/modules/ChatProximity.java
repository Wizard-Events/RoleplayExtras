package ron.thewizard.roleplayextras.modules;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import ron.thewizard.roleplayextras.util.MathUtil;
import ron.thewizard.roleplayextras.util.permissions.PluginPermission;

import java.util.Iterator;

public class ChatProximity extends RoleplayExtrasModule implements Listener {

    private final double maxDistanceSquared;

    public ChatProximity() {
        super("gameplay.chat.proximity", true);
        this.maxDistanceSquared = MathUtil.square(config.getDouble(configPath + ".max-block-distance", 13));
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
        if (PluginPermission.BYPASS_CHAT_PROXIMITY_SEND.check(event.getPlayer())) {
            logger().fine(() -> event.getPlayer().getName() + "'s message will be seen by everyone because of permission: " +
                    PluginPermission.BYPASS_CHAT_PROXIMITY_SEND.node());
            return;
        }

        Iterator<Audience> audienceIterator = event.viewers().iterator();

        while (audienceIterator.hasNext()) {
            if (!(audienceIterator.next() instanceof Player messageReceiver)) {
                continue;
            }

            if (PluginPermission.BYPASS_CHAT_PROXIMITY_RECEIVE.check(messageReceiver)) {
                logger().fine(() -> messageReceiver.getName() + " has permission: " +
                        PluginPermission.BYPASS_CHAT_PROXIMITY_RECEIVE.node() +
                        " and will therefore see all messages from " + event.getPlayer().getName());
                continue;
            }

            // Check if worlds are the same so distanceSquared never throws an IllegalArgumentException
            if (!messageReceiver.getWorld().getUID().equals(event.getPlayer().getWorld().getUID())
                    || messageReceiver.getLocation().distanceSquared(event.getPlayer().getLocation()) > maxDistanceSquared) {
                audienceIterator.remove(); // Remove chat receiver if they are out of reach
                logger().fine(() -> messageReceiver.getName() + " will not see message from " + event.getPlayer().getName() +
                        " because they are too far away");
            }
        }
    }
}
