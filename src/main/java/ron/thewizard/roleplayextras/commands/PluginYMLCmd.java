package ron.thewizard.roleplayextras.commands;

import com.google.common.collect.ImmutableSet;
import org.bukkit.command.CommandException;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import ron.thewizard.roleplayextras.RoleplayExtras;
import ron.thewizard.roleplayextras.utils.Disableable;
import ron.thewizard.roleplayextras.utils.Enableable;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class PluginYMLCmd extends BaseCommand implements Enableable, Disableable  {

    protected static final Set<Class<PluginYMLCmd>> AVAILABLE_COMMANDS;
    protected static final Set<PluginYMLCmd> ENABLED_COMMANDS;

    static {
        AVAILABLE_COMMANDS = new Reflections(PluginYMLCmd.class.getPackage().getName())
                .get(Scanners.SubTypes.of(PluginYMLCmd.class).asClass())
                .stream()
                .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
                .map(clazz -> (Class<PluginYMLCmd>) clazz)
                .sorted(Comparator.comparing(Class::getSimpleName))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));
        ENABLED_COMMANDS = new HashSet<>();
    }

    public final PluginCommand pluginCommand;

    protected PluginYMLCmd(@NotNull String label) throws CommandException {
        super(label);
        pluginCommand = RoleplayExtras.getInstance().getCommand(label);
        if (pluginCommand == null) throw new CommandException("Command '/" + label + "' cannot be enabled because it's not defined in the plugin.yml.");
    }

    public static void disableAll() {
        ENABLED_COMMANDS.forEach(Disableable::disable);
        ENABLED_COMMANDS.clear();
    }

    public static void reloadCommands() {
        disableAll();

        for (Class<PluginYMLCmd> clazz : AVAILABLE_COMMANDS) {
            try {
                PluginYMLCmd pluginYMLCmd = clazz.getDeclaredConstructor().newInstance();
                pluginYMLCmd.enable();
                ENABLED_COMMANDS.add(pluginYMLCmd);
            } catch (Throwable t) {
                if (t.getCause() instanceof NoClassDefFoundError) {
                    RoleplayExtras.logger().info("Dependencies for command class {} missing, not enabling.", clazz.getSimpleName());
                } else {
                    RoleplayExtras.logger().warn("Failed initialising command class '{}'.", clazz.getSimpleName(), t);
                }
            }
        }

        ENABLED_COMMANDS.forEach(Enableable::enable);
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
