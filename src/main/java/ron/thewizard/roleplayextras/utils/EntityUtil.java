package ron.thewizard.roleplayextras.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import ron.thewizard.roleplayextras.RoleplayExtras;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EntityUtil {

    /**
     * Adds a natural-ish random offset using a customizable intensity input (Vanilla intensity is 0.5F).
     * The rest of this method is explained by {@link org.bukkit.World#dropItem(Location, ItemStack, Consumer)}
     */
    public static Item dropItemNaturally(
            Location location,
            float multiplier_x, float multiplier_y, float multiplier_z,
            ItemStack itemStack, Consumer<? super Item> function) {
        return location.getWorld().dropItem(location.clone().add(
                (RoleplayExtras.getRandom().nextFloat() * multiplier_x),
                (RoleplayExtras.getRandom().nextFloat() * multiplier_y),
                (RoleplayExtras.getRandom().nextFloat() * multiplier_z)), itemStack, function);
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

    private static final Lazy<Map<UUID, Boolean>> IS_NPC_CACHE = Lazy.of(ConcurrentHashMap::new);
    public static boolean isNPC(Entity entity) {
        if (entity == null) return false;
        return IS_NPC_CACHE.get().computeIfAbsent(entity.getUniqueId(), uuid -> entity.hasMetadata("NPC"));
    }
}
