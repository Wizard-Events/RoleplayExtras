package ron.thewizard.roleplayextras.commands.walkspeed;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ron.thewizard.roleplayextras.RoleplayExtras;
import ron.thewizard.roleplayextras.commands.PluginYMLCmd;
import ron.thewizard.roleplayextras.util.AdventureUtil;
import ron.thewizard.roleplayextras.util.permissions.PluginPermission;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class WalkSpeedCmd extends PluginYMLCmd {

    private final List<String> speedSuggestions;

    public WalkSpeedCmd() {
        super("walkspeed");
        this.speedSuggestions = List.of("default", "0.03", "0.05", "0.1", "0.15");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission(PluginPermission.WALKSPEED_CMD_SELF.bukkit())) {
            return speedSuggestions;
        }

        if (args.length == 2 && sender.hasPermission(PluginPermission.WALKSPEED_CMD_OTHER.bukkit())) {
            Stream<String> onlinePlayers = Bukkit.getOnlinePlayers().stream().map(Player::getName);
            return (args[1].isBlank() ? onlinePlayers : onlinePlayers.filter(playerName -> playerName.contains(args[1]))).toList();
        }

        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final boolean canChangeOwn = sender.hasPermission(PluginPermission.WALKSPEED_CMD_SELF.bukkit());
        final boolean canChangeOthers = sender.hasPermission(PluginPermission.WALKSPEED_CMD_OTHER.bukkit());

        if (!canChangeOwn && !canChangeOthers) {
            RoleplayExtras.config().cmd_no_permission.forEach(sender::sendMessage);
            return true;
        }

        if (args.length == 0) {
            if (canChangeOthers) {
                sender.sendMessage(Component.text("Missing arguments. Syntax: /walkspeed <speed> (player)", AdventureUtil.wizardRed));
            } else {
                sender.sendMessage(Component.text("Missing argument. Syntax: /walkspeed <speed>", AdventureUtil.wizardRed));
            }
            return true;
        }

        if (args.length == 1) {
            // The command is something like "/walkspeed 0.5"
            if (!canChangeOwn) {
                RoleplayExtras.config().cmd_no_permission.forEach(sender::sendMessage);
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Missing argument. Syntax: /walkspeed <speed> <player>", AdventureUtil.wizardRed));
                return true;
            }

            final float newWalkSpeed;
            try {
                newWalkSpeed = args[0].equalsIgnoreCase("default") ? 0.2F : Float.parseFloat(args[0]);
            } catch (NumberFormatException e) {
                RoleplayExtras.config().walkspeed_invalid_speed_format.forEach(sender::sendMessage);
                return true;
            }

            try {
                player.setWalkSpeed(newWalkSpeed);
                RoleplayExtras.config().walkspeed_success_self.forEach(line -> sender.sendMessage(line
                        .replaceText(TextReplacementConfig.builder().matchLiteral("%walkspeed%").replacement(Float.toString(newWalkSpeed)).build())));
            } catch (IllegalArgumentException e) {
                RoleplayExtras.config().walkspeed_invalid_speed.forEach(sender::sendMessage);
            }
        } else {
            // The command is something like "/walkspeed 0.5 someplayer"
            if (!canChangeOthers) {
                RoleplayExtras.config().cmd_no_permission.forEach(sender::sendMessage);
                return true;
            }

            @Nullable Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage(Component.text("Player '" + args[1] + "' is not online.", AdventureUtil.wizardRed));
                return true;
            }

            final float newWalkSpeed;
            try {
                newWalkSpeed = args[0].equalsIgnoreCase("default") ? 0.2F : Float.parseFloat(args[0]);
            } catch (NumberFormatException e) {
                RoleplayExtras.config().walkspeed_invalid_speed_format.forEach(sender::sendMessage);
                return true;
            }

            try {
                player.setWalkSpeed(newWalkSpeed);
                RoleplayExtras.config().walkspeed_success_other.forEach(line -> sender.sendMessage(line
                        .replaceText(TextReplacementConfig.builder().matchLiteral("%player%").replacement(player.getName()).build())
                        .replaceText(TextReplacementConfig.builder().matchLiteral("%walkspeed%").replacement(Float.toString(newWalkSpeed)).build())));
            } catch (IllegalArgumentException e) {
                RoleplayExtras.config().walkspeed_invalid_speed.forEach(sender::sendMessage);
            }
        }

        return true;
    }
}
