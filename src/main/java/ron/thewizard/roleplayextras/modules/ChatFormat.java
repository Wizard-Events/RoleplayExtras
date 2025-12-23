package ron.thewizard.roleplayextras.modules;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import ron.thewizard.roleplayextras.RoleplayExtras;
import ron.thewizard.roleplayextras.util.KyoriUtil;

public class ChatFormat extends RoleplayExtrasModule implements Listener, ChatRenderer {

    private final String defaultFormat, oocPrefix, oocFormat;

    public ChatFormat() {
        super("gameplay.chat.format", true);
        this.defaultFormat = KyoriUtil.replaceAmpersand(config.getString(configPath + ".message-format",
                "<white><%sneakycharacters_character_name%> {message}"));
        this.oocPrefix = config.getString(configPath + ".ooc.message-prefix", "((");
        this.oocFormat = KyoriUtil.replaceAmpersand(config.getString(configPath + ".ooc.message-format",
                "<#9a9a9a><%sneakycharacters_character_name%> {message}"));
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        if (PlainTextComponentSerializer.plainText().serialize(message).startsWith(oocPrefix)) {
            return MiniMessage.miniMessage()
                    .deserialize(RoleplayExtras.isPapiInstalled ? PlaceholderAPI.setPlaceholders(source, oocFormat) : oocFormat)
                    .replaceText(builder -> builder.matchLiteral("{message}").replacement(message));
        }

        return MiniMessage.miniMessage()
                .deserialize(RoleplayExtras.isPapiInstalled ? PlaceholderAPI.setPlaceholders(source, defaultFormat) : defaultFormat)
                .replaceText(builder -> builder.matchLiteral("{message}").replacement(message));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void on(AsyncChatEvent event) {
        event.renderer(this);
    }
}
