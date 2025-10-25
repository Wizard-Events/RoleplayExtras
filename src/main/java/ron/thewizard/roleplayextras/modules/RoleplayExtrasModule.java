package ron.thewizard.roleplayextras.modules;

import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.core.config.Configurator;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import ron.thewizard.roleplayextras.RoleplayConfig;
import ron.thewizard.roleplayextras.RoleplayExtras;
import ron.thewizard.roleplayextras.util.Disableable;
import ron.thewizard.roleplayextras.util.Enableable;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class RoleplayExtrasModule implements Enableable, Disableable {

    protected static final Set<Class<RoleplayExtrasModule>> AVAILABLE_MODULES;
    protected static final Set<RoleplayExtrasModule> ENABLED_MODULES;

    static {
        // Disable reflection logging for this operation because its just confusing and provides no value.
        Configurator.setLevel(RoleplayExtras.class.getPackage().getName() + ".libs.reflections.Reflections", org.apache.logging.log4j.Level.OFF);
        AVAILABLE_MODULES = new Reflections(RoleplayExtrasModule.class.getPackage().getName())
                .get(Scanners.SubTypes.of(RoleplayExtrasModule.class).asClass())
                .stream()
                .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
                .map(clazz -> (Class<RoleplayExtrasModule>) clazz)
                .sorted(Comparator.comparing(Class::getSimpleName))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));
        ENABLED_MODULES = new HashSet<>(AVAILABLE_MODULES.size());
    }

    protected final RoleplayExtras plugin = RoleplayExtras.getInstance();
    protected final RoleplayConfig config = RoleplayExtras.config();
    protected final Logger logger;
    protected final String configPath;
    protected final boolean enabled_in_config;

    public RoleplayExtrasModule(String configPath, boolean defEnabled) {
        this(configPath, defEnabled, null);
    }

    public RoleplayExtrasModule(String configPath, boolean defEnabled, String comment) {
        this.configPath = configPath;

        if (comment == null || comment.isBlank()) {
            this.enabled_in_config = config.getBoolean(configPath + ".enable", defEnabled);
        } else {
            this.enabled_in_config = config.getBoolean(configPath + ".enable", defEnabled, comment);
        }

        Level loggingLevel = Level.INFO;
        String configuredLoggingLevel = config.getString(configPath + ".log-level", loggingLevel.getName(), """
                Levels: OFF - SEVERE (highest value) - WARNING - INFO - CONFIG - FINE - FINER - FINEST (lowest value) - ALL""");
        try {
            loggingLevel = Level.parse(configuredLoggingLevel);
        } catch (IllegalArgumentException e) {
            logger().warning(() -> "Unable to parse logging level from string '" + configuredLoggingLevel +
                    "', falling back to " + Level.INFO.getName());
        }
        this.logger = getModuleLogger(configPath, loggingLevel);
    }

    public boolean shouldEnable() {
        return enabled_in_config;
    }

    public Logger logger() {
        return logger;
    }

    public static void disableAll() {
        ENABLED_MODULES.forEach(Disableable::disable);
        ENABLED_MODULES.clear();
    }

    public static void reloadModules() {
        disableAll();

        for (Class<RoleplayExtrasModule> moduleClass : AVAILABLE_MODULES) {
            try {
                RoleplayExtrasModule module = moduleClass.getDeclaredConstructor().newInstance();
                if (module.shouldEnable()) {
                    ENABLED_MODULES.add(module);
                }
            } catch (Throwable t) { // This is not laziness. We want to catch everything here if it fails to init
                if (t.getCause() instanceof NoClassDefFoundError) {
                    RoleplayExtras.logger().info("Dependencies for module class {} missing, not enabling.", moduleClass.getSimpleName());
                } else {
                    RoleplayExtras.logger().warn("Failed initialising module class '{}'.", moduleClass.getSimpleName(), t);
                }
            }
        }

        ENABLED_MODULES.forEach(Enableable::enable);
    }

    protected static Logger getModuleLogger(String configPath, Level level) {
        final String[] splitPath = configPath.split("\\.");
        final String loggingPrefix = splitPath.length < 3 ? configPath : splitPath[splitPath.length - 2] + "." + splitPath[splitPath.length - 1];
        Logger moduleLogger = Logger.getLogger(RoleplayExtras.getInstance().getLogger().getName() + " # " + loggingPrefix);
        moduleLogger.setLevel(level);
        return moduleLogger;
    }

    protected void notRecognized(Class<?> clazz, String unrecognized) {
        logger().warning(() -> "Unable to parse " + clazz.getSimpleName() + " from string '" + unrecognized + "'. Please check your configuration.");
    }
}
