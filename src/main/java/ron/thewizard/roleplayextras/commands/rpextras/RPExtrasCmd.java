package ron.thewizard.roleplayextras.commands.rpextras;

import ron.thewizard.roleplayextras.commands.PluginYMLCmd;
import ron.thewizard.roleplayextras.commands.BaseCommand;
import ron.thewizard.roleplayextras.commands.rpextras.subcommands.ReloadSubCmd;
import ron.thewizard.roleplayextras.commands.rpextras.subcommands.VersionSubCmd;
import ron.thewizard.roleplayextras.utils.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RPExtrasCmd extends PluginYMLCmd {

    private final @NotNull List<BaseCommand> subCommands;
    private final @NotNull List<String> tabCompletes;

    public RPExtrasCmd() {
        super("roleplayextras");
        this.subCommands = Arrays.asList(new ReloadSubCmd(), new VersionSubCmd());
        this.tabCompletes = subCommands.stream().map(BaseCommand::label).collect(Collectors.toList());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(Permissions.VERSION_CMD.bukkit()) && !sender.hasPermission(Permissions.RELOAD_CMD.bukkit())) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return tabCompletes;
        }

        if (args.length > 1) {
            for (BaseCommand subCommand : subCommands) {
                if (args[0].equalsIgnoreCase(subCommand.label())) {
                    return subCommand.onTabComplete(sender, command, label, args);
                }
            }

            return tabCompletes.stream().filter(cmd -> cmd.startsWith(args[0])).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(Permissions.VERSION_CMD.bukkit()) && !sender.hasPermission(Permissions.RELOAD_CMD.bukkit())) {
            return true;
        }

        if (args.length > 0) {
            for (BaseCommand subCommand : subCommands) {
                if (args[0].equalsIgnoreCase(subCommand.label())) {
                    return subCommand.onCommand(sender, command, label, args);
                }
            }
        }

        return true;
    }
}
