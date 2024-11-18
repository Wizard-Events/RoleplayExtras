package ron.thewizard.roleplayextras.commands.rpextras.subcommands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ron.thewizard.roleplayextras.RoleplayExtras;
import ron.thewizard.roleplayextras.commands.BaseCommand;
import ron.thewizard.roleplayextras.utils.KyoriUtil;
import ron.thewizard.roleplayextras.utils.PluginPermission;

import java.util.Collections;
import java.util.List;

public class ReloadSubCmd extends BaseCommand {

    public ReloadSubCmd() {
        super("reload");
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PluginPermission.RELOAD_CMD.get())) {
            RoleplayExtras.config().cmd_no_permission.forEach(sender::sendMessage);
            return true;
        }

        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Reloading ...").color(KyoriUtil.wizardPurple));
        RoleplayExtras.scheduling().asyncScheduler().run(() -> {
            if (RoleplayExtras.getInstance().reloadConfiguration()) {
                sender.sendMessage(Component.text("Reload complete!").color(KyoriUtil.wizardWhite));
            } else {
                sender.sendMessage(Component.text("Something went wrong!").color(KyoriUtil.wizardRed));
            }
            sender.sendMessage(Component.empty());
        });

        return true;
    }
}