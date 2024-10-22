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
}
