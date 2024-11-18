package ron.thewizard.roleplayextras;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.java.JavaPlugin;
import ron.thewizard.roleplayextras.commands.PluginYMLCmd;
import ron.thewizard.roleplayextras.modules.RoleplayExtrasModule;
import ron.thewizard.roleplayextras.utils.KyoriUtil;
import ron.thewizard.roleplayextras.utils.PluginPermission;
import space.arim.morepaperlib.MorePaperLib;
import space.arim.morepaperlib.commands.CommandRegistration;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import java.nio.file.Files;
import java.util.Calendar;
import java.util.Random;
import java.util.stream.Stream;

public final class RoleplayExtras extends JavaPlugin {

    private static RoleplayExtras instance;
    private static ComponentLogger logger;
    private static RoleplayConfig config;

    private static CommandRegistration commandRegistration;
    private static GracefulScheduling scheduling;
    private static Random random;

    private static boolean isPacketEventsInstalled;

    @Override
    public void onLoad() {
        isPacketEventsInstalled = getServer().getPluginManager().getPlugin("packetevents") != null;
        if (isPacketEventsInstalled) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().getSettings()
                    .kickOnPacketException(true) // Just in case that exception comes with a bit of oomph
                    .reEncodeByDefault(false) // We don't always alter packets, therefore unnecessary
                    .downsampleColors(false) // Just in case, should be off by default for 1.20+
                    .checkForUpdates(false).bStats(false); // The voices wouldn't stop otherwise
            PacketEvents.getAPI().load();
        }
    }

    @Override
    public void onEnable() {
        logger = getComponentLogger();

        if (!isPacketEventsInstalled) {
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
        MorePaperLib morePaperLib = new MorePaperLib(instance);
        commandRegistration = morePaperLib.commandRegistration();
        scheduling = morePaperLib.scheduling();
        random = new Random();

        try {
            Files.createDirectories(getDataFolder().toPath());
        } catch (Throwable t) {
            logger.error("Failed to create plugin directory! Cannot enable!", t);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

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
        PluginPermission.registerAll(getServer().getPluginManager());
        logger.info("Done");
    }

    @Override
    public void onDisable() {
        PluginYMLCmd.disableAll();
        RoleplayExtrasModule.disableAll();
        PluginPermission.unregisterAll(getServer().getPluginManager());
        if (isPacketEventsInstalled) {
            PacketEvents.getAPI().terminate();
        }
        if (scheduling != null) {
            scheduling.cancelGlobalTasks();
            scheduling = null;
        }
        commandRegistration = null;
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

    public static CommandRegistration cmdRegistration() {
        return commandRegistration;
    }

    public static GracefulScheduling scheduling() {
        return scheduling;
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
