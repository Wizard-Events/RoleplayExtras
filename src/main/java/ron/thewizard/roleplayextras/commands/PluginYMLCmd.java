package ron.thewizard.roleplayextras.commands;

import org.bukkit.command.CommandException;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;
import ron.thewizard.roleplayextras.RoleplayExtras;
import ron.thewizard.roleplayextras.commands.rpextras.RPExtrasCmd;
import ron.thewizard.roleplayextras.utils.Disableable;
import ron.thewizard.roleplayextras.utils.Enableable;

import java.util.HashSet;
import java.util.Set;

public abstract class PluginYMLCmd extends BaseCommand implements Enableable, Disableable  {

    public static final Set<PluginYMLCmd> COMMANDS = new HashSet<>();

    public final PluginCommand pluginCommand;

    protected PluginYMLCmd(@NotNull String label) throws CommandException {
        super(label);
        pluginCommand = RoleplayExtras.getInstance().getCommand(label);
        if (pluginCommand == null) throw new CommandException("Command '/" + label + "' cannot be enabled because it's not defined in the plugin.yml.");
    }

    public static void disableAll() {
        COMMANDS.forEach(Disableable::disable);
        COMMANDS.clear();
    }

    public static void reloadCommands() {
        disableAll();
        COMMANDS.add(new RPExtrasCmd());
        COMMANDS.forEach(Enableable::enable);
    }

    @Override
    public void enable() {
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
    }

    @Override
    public void disable() {
        pluginCommand.unregister(RoleplayExtras.cmdRegistration().getServerCommandMap());
    }
}
