package ron.thewizard.roleplayextras.commands.rpextras;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ron.thewizard.roleplayextras.commands.BaseCommand;
import ron.thewizard.roleplayextras.commands.PluginYMLCmd;
import ron.thewizard.roleplayextras.commands.rpextras.subcommands.ReloadSubCmd;
import ron.thewizard.roleplayextras.commands.rpextras.subcommands.VersionSubCmd;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class RPExtrasCmd extends PluginYMLCmd {

    private final @NotNull List<BaseCommand> subCommands;
    private final @NotNull List<String> tabCompletes;

    public RPExtrasCmd() {
        super("roleplayextras");
        this.subCommands = List.of(new ReloadSubCmd(), new VersionSubCmd());
        this.tabCompletes = subCommands.stream().map(BaseCommand::label).toList();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return tabCompletes.stream()
                    .filter(cmd -> cmd.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .toList();
        }

        if (args.length > 1) {
            for (BaseCommand subCommand : subCommands) {
                if (args[0].equalsIgnoreCase(subCommand.label())) {
                    return subCommand.onTabComplete(sender, command, label, args);
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
