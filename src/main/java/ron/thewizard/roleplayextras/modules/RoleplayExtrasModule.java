package ron.thewizard.roleplayextras.modules;

import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import ron.thewizard.roleplayextras.RoleplayConfig;
import ron.thewizard.roleplayextras.RoleplayExtras;
import ron.thewizard.roleplayextras.utils.Disableable;
import ron.thewizard.roleplayextras.utils.Enableable;
import ron.thewizard.roleplayextras.utils.logging.FilteredSLF4JLogger;
import ron.thewizard.roleplayextras.utils.logging.LogLevel;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class RoleplayExtrasModule implements Enableable, Disableable {

    protected static final Set<Class<RoleplayExtrasModule>> AVAILABLE_MODULES;
    protected static final Set<RoleplayExtrasModule> ENABLED_MODULES;

    static {
        // Disable reflection logging for this operation because its just confusing and provides no value.
        Configurator.setLevel(RoleplayExtras.class.getPackage().getName() + ".libs.reflections.Reflections", Level.OFF);
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
    protected final FilteredSLF4JLogger<ComponentLogger> logger;
    protected final String configPath;
    protected final boolean enabled_in_config;

    public RoleplayExtrasModule(String configPath, boolean defEnabled) {
        this(configPath, defEnabled, null);
    }

    public RoleplayExtrasModule(String configPath, boolean defEnabled, String comment) {
        this.configPath = configPath;

        LogLevel loggingLevel = LogLevel.INFO;
        String configuredLoggingLevel = config.getString(configPath + ".log-level", loggingLevel.name(),
                Arrays.stream(LogLevel.values()).map(Enum::name).collect(Collectors.joining(" ")));
        try {
            loggingLevel = LogLevel.valueOf(configuredLoggingLevel);
        } catch (IllegalArgumentException e) {
            notRecognized(org.slf4j.event.Level.class, configuredLoggingLevel);
        }
        this.logger = createModuleLogger(configPath, loggingLevel);

        if (comment == null || comment.isBlank()) {
            this.enabled_in_config = config.getBoolean(configPath + ".enable", defEnabled);
        } else {
            this.enabled_in_config = config.getBoolean(configPath + ".enable", defEnabled, comment);
        }
    }

    public boolean shouldEnable() {
        return enabled_in_config;
    }

    public FilteredSLF4JLogger<ComponentLogger> logger() {
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

    protected static FilteredSLF4JLogger<ComponentLogger> createModuleLogger(String configPath, LogLevel filterLevel) {
        final String[] splitPath = configPath.split("\\.");
        final String loggingPrefix = splitPath.length < 3 ? configPath : splitPath[splitPath.length - 2] + "." + splitPath[splitPath.length - 1];
        return new FilteredSLF4JLogger<>(
                ComponentLogger.logger(RoleplayExtras.getInstance().getLogger().getName() + " # " + loggingPrefix),
                filterLevel);
    }

    protected void notRecognized(Class<?> clazz, String unrecognized) {
        logger().warn("Unable to parse {} from string '{}'. Please check your configuration.", clazz.getSimpleName(), unrecognized);
    }
}
