package ron.thewizard.roleplayextras.utils;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import ron.thewizard.roleplayextras.RoleplayExtras;

import java.util.function.Consumer;

public class EntityUtil {

    /**
     * Adds a natural-ish random offset using a customizable intensity input (Vanilla intensity is 0.5F).
     * The rest of this method is explained by {@link org.bukkit.World#dropItem(Location, ItemStack, Consumer)}
     */
    public static Item dropItemNaturally(Location location, float disperseIntensity, ItemStack itemStack, Consumer<? super Item> function) {
        return location.getWorld().dropItem(location.clone().add(
                (RoleplayExtras.getRandom().nextFloat() * disperseIntensity) + 0.25D,
                (RoleplayExtras.getRandom().nextFloat() * disperseIntensity) + 0.25D,
                (RoleplayExtras.getRandom().nextFloat() * disperseIntensity) + 0.25D), itemStack, function);
    }

    public static void cloneItemProperties(Item from, Item to) {
        to.setGravity(from.hasGravity());
        to.setVelocity(from.getVelocity());
        to.setFallDistance(from.getFallDistance());
        to.setFrictionState(from.getFrictionState());

        to.setOwner(from.getOwner());
        to.setThrower(from.getThrower());
        to.customName(from.customName());
        to.setCustomNameVisible(from.isCustomNameVisible());

        to.setWillAge(from.willAge());
        to.setHealth(from.getHealth());
        to.setInvulnerable(from.isInvulnerable());
        to.setUnlimitedLifetime(from.isUnlimitedLifetime());

        to.setGlowing(from.isGlowing());
        to.setFireTicks(from.getFireTicks());
        to.setVisualFire(from.isVisualFire());
        to.setVisibleByDefault(from.isVisibleByDefault());

        to.setSilent(from.isSilent());
        to.setPersistent(from.isPersistent());
        to.setPortalCooldown(from.getPortalCooldown());
    }
}
