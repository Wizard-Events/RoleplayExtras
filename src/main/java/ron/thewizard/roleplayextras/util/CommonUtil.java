package ron.thewizard.roleplayextras.util;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class CommonUtil {

    public static String formatDuration(@NotNull Duration duration) {
        int minutes = duration.toMinutesPart();
        int hours = duration.toHoursPart();
        long days = duration.toDaysPart();

        if (days > 0) {
            return String.format("%02dd %02dh %02dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%02dh %02dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%02dm %02ds", minutes, duration.toSecondsPart());
        } else {
            return String.format("%02ds", duration.toSecondsPart());
        }
    }

}
