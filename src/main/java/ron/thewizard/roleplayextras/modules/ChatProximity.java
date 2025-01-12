package ron.thewizard.roleplayextras.modules;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.NumberConversions;
import ron.thewizard.roleplayextras.utils.PluginPermission;

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
        Player sender = event.getPlayer();
        Iterator<Audience> iterator = event.viewers().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof Player receiver && !receiver.hasPermission(PluginPermission.BYPASS_CHAT_PROXIMITY.get())) {
                // Check if worlds are the same so distanceSquared doesn't throw an IllegalArgumentException
                if (!receiver.getWorld().getUID().equals(sender.getWorld().getUID())
                        || receiver.getLocation().distanceSquared(sender.getLocation()) > maxDistanceSquared) {
                    iterator.remove(); // Remove chat receiver if they are out of reach
                }
            }
        }
    }
}
