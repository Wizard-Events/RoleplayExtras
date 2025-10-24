package ron.thewizard.roleplayextras.utils;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public final class LocationUtil {

    public static String toString(Location location) {
        return "[" + location.getWorld().getName() + "] x=" + location.getBlockX() + ", y=" + location.getBlockY() + ", z=" + location.getBlockZ();
    }

    public static @NotNull Location toXZCenter(@NotNull Location location) {
        Location centered = location.clone(); // Clone so we don't alter original location
        centered.setX(centered.getBlockX() + 0.5D);
        centered.setY(centered.getBlockY());
        centered.setZ(centered.getBlockZ() + 0.5D);
        return centered;
    }

}
