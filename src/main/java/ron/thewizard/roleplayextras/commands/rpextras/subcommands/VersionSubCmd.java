package ron.thewizard.roleplayextras.commands.rpextras.subcommands;

import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ron.thewizard.roleplayextras.RoleplayExtras;
import ron.thewizard.roleplayextras.commands.BaseCommand;
import ron.thewizard.roleplayextras.util.AdventureUtil;
import ron.thewizard.roleplayextras.util.permissions.PluginPermission;

import java.util.Collections;
import java.util.List;

public class VersionSubCmd extends BaseCommand {

    public VersionSubCmd() {
        super("version");
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PluginPermission.VERSION_CMD.bukkit())) {
            RoleplayExtras.config().cmd_no_permission.forEach(sender::sendMessage);
            return true;
        }

        PluginMeta pluginMeta = RoleplayExtras.getInstance().getPluginMeta();

        sender.sendMessage(Component.newline()
                .append(
                        Component.text(String.join(" ", pluginMeta.getName(), pluginMeta.getVersion()))
                                .color(AdventureUtil.wizardPurple)
                                .clickEvent(ClickEvent.openUrl(pluginMeta.getWebsite()))
                )
                .append(Component.text(" by ").color(NamedTextColor.DARK_GRAY))
                .append(
                        Component.text(String.join(", ", pluginMeta.getAuthors()))
                                .color(AdventureUtil.ginkoBlue)
                                .clickEvent(ClickEvent.openUrl("https://github.com/xGinko"))
                )
                .append(Component.newline())
        );

        return true;
    }
}