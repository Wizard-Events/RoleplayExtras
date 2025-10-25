package ron.thewizard.roleplayextras.util;

public final class MathUtil {

    public static double square(double delta) {
        return delta * delta;
    }

    public static double square(double deltaX, double deltaZ) {
        return Math.fma(deltaX, deltaX, deltaZ * deltaZ);
    }

    public static double square(double deltaX, double deltaY, double deltaZ) {
        return Math.fma(deltaX, deltaX, Math.fma(deltaY, deltaY, deltaZ * deltaZ));
    }

}
