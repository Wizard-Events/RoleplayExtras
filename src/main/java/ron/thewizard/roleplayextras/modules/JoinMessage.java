package ron.thewizard.roleplayextras.modules;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ron.thewizard.roleplayextras.utils.KyoriUtil;

import java.util.List;

public class JoinMessage extends RoleplayExtrasModule implements Listener {

    private final List<Component> join_message;

    public JoinMessage() {
        super("misc.join-message", false);
        this.join_message = config.getList(configPath + ".message",
                List.of("<#FFE0E2>This message is sent to players when joining the server.",
                        "<#FF334E>Placeholders of any kind will not work here but that can be arranged."),
                        "Uses MiniMessage formatting: https://docs.advntr.dev/minimessage/format.html")
                .stream()
                .map(KyoriUtil::replaceAmpersand)
                .map(MiniMessage.miniMessage()::deserialize)
                .toList();
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void on(PlayerJoinEvent event) {
        logger().debug("Sending join message to player {}", event.getPlayer().getName());
        for (Component component : join_message) {
            event.getPlayer().sendMessage(component);
        }
    }
}
