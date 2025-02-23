package ron.thewizard.roleplayextras;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.java.JavaPlugin;
import ron.thewizard.roleplayextras.commands.PluginYMLCmd;
import ron.thewizard.roleplayextras.modules.RoleplayExtrasModule;
import ron.thewizard.roleplayextras.utils.KyoriUtil;
import ron.thewizard.roleplayextras.utils.permissions.PermissionHandler;
import ron.thewizard.roleplayextras.utils.permissions.PluginPermission;

import java.nio.file.Files;
import java.util.Calendar;
import java.util.Random;
import java.util.stream.Stream;

public final class RoleplayExtras extends JavaPlugin {

    private static RoleplayExtras instance;
    private static RoleplayConfig config;
    private static PermissionHandler permissionHandler;
    private static ComponentLogger logger;
    private static Random random;

    @Override
    public void onEnable() {
        logger = getComponentLogger();

        if (getServer().getPluginManager().getPlugin("packetevents") == null) {
            Stream.of("                                                               ",
                    "       _   _   _             _   _                             ",
                    "      / \\ | |_| |_ ___ _ __ | |_(_) ___  _ __                 ",
                    "     / _ \\| __| __/ _ \\ '_ \\| __| |/ _ \\| '_ \\            ",
                    "    / ___ \\ |_| ||  __/ | | | |_| | (_) | | | |               ",
                    "   /_/   \\_\\__|\\__\\___|_| |_|\\__|_|\\___/|_| |_|          ",
                    "                                                               ",
                    "   This plugin depends on PacketEvents to function!            ",
                    "   You can either download the latest release on modrinth:     ",
                    "   https://modrinth.com/plugin/packetevents/                   ",
                    "   or choose a dev build on their jenkins:                     ",
                    "   https://ci.codemc.io/job/retrooper/job/packetevents/        ",
                    "                                                               "
            ).forEach(logger::error);
            getServer().shutdown();
            return;
        }

        instance = this;
        permissionHandler = PermissionHandler.create(instance);
        random = new Random();

        logger.info("Loading config");
        if (!reloadConfiguration()) {
            // If first ever reload fails, there's likely a bigger issue going on
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Important message for the master mind
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.MONTH) == Calendar.OCTOBER && calendar.get(Calendar.DAY_OF_MONTH) == 6) {
            KyoriUtil.getBirthdayMessage().forEach(logger::info);
        }

        // Register permissions so they show up in managers
        logger.info("Registering permissions");
        PluginPermission.registerAll();
        logger.info("Done");
    }

    @Override
    public void onDisable() {
        PluginYMLCmd.disableAll();
        RoleplayExtrasModule.disableAll();
        getServer().getGlobalRegionScheduler().cancelTasks(this);
        getServer().getAsyncScheduler().cancelTasks(this);
        PluginPermission.unregisterAll();
        if (permissionHandler != null) {
            permissionHandler.disable();
            permissionHandler = null;
        }
        instance = null;
        random = null;
        config = null;
        logger = null;
    }

    public static RoleplayExtras getInstance() {
        return instance;
    }

    public static ComponentLogger logger() {
        return logger;
    }

    public static RoleplayConfig config() {
        return config;
    }

    public static PermissionHandler permissions() {
        return permissionHandler;
    }

    public static Random getRandom() {
        return random;
    }

    public boolean reloadConfiguration() {
        try {
            Files.createDirectories(getDataFolder().toPath());
            config = new RoleplayConfig();
            RoleplayExtrasModule.reloadModules();
            PluginYMLCmd.reloadCommands();
            return config.saveConfig();
        } catch (Throwable t) {
            logger.error("Error loading config!", t);
            return false;
        }
    }
}
