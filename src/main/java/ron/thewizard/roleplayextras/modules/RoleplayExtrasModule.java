package ron.thewizard.roleplayextras.modules;

import com.google.common.collect.ImmutableSet;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import ron.thewizard.roleplayextras.RoleplayConfig;
import ron.thewizard.roleplayextras.RoleplayExtras;
import ron.thewizard.roleplayextras.utils.Disableable;
import ron.thewizard.roleplayextras.utils.Enableable;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public abstract class RoleplayExtrasModule implements Enableable, Disableable {

    protected static final Set<Class<RoleplayExtrasModule>> AVAILABLE_MODULES;
    protected static final Set<RoleplayExtrasModule> ENABLED_MODULES;

    static {
        AVAILABLE_MODULES = new Reflections(RoleplayExtrasModule.class.getPackage().getName())
                .get(Scanners.SubTypes.of(RoleplayExtrasModule.class).asClass())
                .stream()
                .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
                .map(clazz -> (Class<RoleplayExtrasModule>) clazz)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Class::getSimpleName))),
                        ImmutableSet::copyOf));
        ENABLED_MODULES = new HashSet<>();
    }

    protected final RoleplayExtras plugin;
    protected final RoleplayConfig config;
    protected final GracefulScheduling scheduling;
    protected final String configPath, logFormat;
    protected final boolean enabled_in_config;

    public RoleplayExtrasModule(String configPath, boolean defEnabled) {
        this(configPath, defEnabled, null);
    }

    public RoleplayExtrasModule(String configPath, boolean defEnabled, String comment) {
        this.configPath = configPath;
        this.plugin = RoleplayExtras.getInstance();
        this.config = RoleplayExtras.config();
        this.scheduling = RoleplayExtras.scheduling();

        if (comment == null || comment.isBlank()) {
            this.enabled_in_config = config.getBoolean(configPath + ".enable", defEnabled);
        } else {
            this.enabled_in_config = config.getBoolean(configPath + ".enable", defEnabled, comment);
        }

        String[] paths = configPath.split("\\.");
        if (paths.length <= 2) {
            this.logFormat = "<" + configPath + "> {}";
        } else {
            this.logFormat = "<" + paths[paths.length - 2] + "." + paths[paths.length - 1] + "> {}";
        }
    }

    public boolean shouldEnable() {
        return enabled_in_config;
    }

    public static void disableAll() {
        ENABLED_MODULES.forEach(Disableable::disable);
        ENABLED_MODULES.clear();
    }

    public static void reloadModules() {
        disableAll();

        for (Class<RoleplayExtrasModule> clazz : AVAILABLE_MODULES) {
            try {
                RoleplayExtrasModule module = clazz.getDeclaredConstructor().newInstance();
                if (module.shouldEnable()) {
                    ENABLED_MODULES.add(module);
                }
            } catch (Throwable t) { // This is not laziness. We want to catch everything here if it fails to init
                RoleplayExtras.logger().warn("Failed initialising module class '{}'.", clazz.getSimpleName(), t);
            }
        }

        ENABLED_MODULES.forEach(Enableable::enable);
    }

    protected void error(String message, Throwable throwable) {
        RoleplayExtras.logger().error(logFormat, message, throwable);
    }

    protected void error(String message) {
        RoleplayExtras.logger().error(logFormat, message);
    }

    protected void warn(String message) {
        RoleplayExtras.logger().warn(logFormat, message);
    }

    protected void info(String message) {
        RoleplayExtras.logger().info(logFormat, message);
    }

    protected void notRecognized(Class<?> clazz, String unrecognized) {
        warn("Unable to parse " + clazz.getSimpleName() + " at '" + unrecognized + "'. Please check your configuration.");
    }
}
