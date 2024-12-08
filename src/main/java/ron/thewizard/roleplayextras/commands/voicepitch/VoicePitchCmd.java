package ron.thewizard.roleplayextras.commands.voicepitch;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ron.thewizard.roleplayextras.RoleplayExtras;
import ron.thewizard.roleplayextras.commands.PluginYMLCmd;
import ron.thewizard.roleplayextras.utils.KyoriUtil;
import ron.thewizard.roleplayextras.utils.PluginPermission;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class VoicePitchCmd extends PluginYMLCmd implements VoicechatPlugin {

    private VoicechatServerApi voicechatServerApi;

    private NamespacedKey voicePitchKey;
    private List<String> pitchSuggestions;

    private Map<UUID, Short> cachedPitchSettings;
    private Map<UUID, OpusEncoder> encoderMap;
    private Map<UUID, OpusDecoder> decoderMap;

    protected VoicePitchCmd() throws CommandException {
        super("voicepitch");
    }

    @Override
    public void enable() {
        BukkitVoicechatService voicechatService = RoleplayExtras.getInstance().getServer().getServicesManager()
                .load(BukkitVoicechatService.class);
        if (voicechatService == null) {
            disable();
            return;
        }

        voicePitchKey = new NamespacedKey(RoleplayExtras.getInstance(), "voicechat-pitch");
        pitchSuggestions = List.of("-5", "-4", "-3", "-2", "-1", "default", "1", "2", "3", "4", "5");

        Collection<? extends Player> onlinePlayers = RoleplayExtras.getInstance().getServer().getOnlinePlayers();

        cachedPitchSettings = new ConcurrentHashMap<>();
        for (Player onlinePlayer : onlinePlayers) {
            cachedPitchSettings.put(onlinePlayer.getUniqueId(), getVoiceChatPitch(onlinePlayer));
        }

        encoderMap = new ConcurrentHashMap<>(onlinePlayers.size());
        decoderMap = new ConcurrentHashMap<>(onlinePlayers.size());

        voicechatService.registerPlugin(this);

        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
    }

    @Override
    public void disable() {
        pluginCommand.unregister(RoleplayExtras.cmdRegistration().getServerCommandMap());
        RoleplayExtras.getInstance().getServer().getServicesManager().unregister(this);
        if (decoderMap != null) {
            decoderMap.forEach((uuid, opusDecoder) -> opusDecoder.close());
            decoderMap.clear();
            decoderMap = null;
        }
        if (encoderMap != null) {
            encoderMap.forEach((uuid, opusEncoder) -> opusEncoder.close());
            encoderMap.clear();
            encoderMap = null;
        }
        if (cachedPitchSettings != null) {
            cachedPitchSettings.clear();
            cachedPitchSettings = null;
        }
        pitchSuggestions = null;
        voicechatServerApi = null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission(PluginPermission.VOICEPITCH_CMD_SELF.get())) {
            return pitchSuggestions;
        }

        if (args.length == 2 && sender.hasPermission(PluginPermission.VOICEPITCH_CMD_OTHER.get())) {
            Stream<String> onlinePlayers = Bukkit.getOnlinePlayers().stream().map(Player::getName);
            return (args[1].isBlank() ? onlinePlayers : onlinePlayers.filter(playerName -> playerName.contains(args[1]))).toList();
        }

        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final boolean canChangeOwn = sender.hasPermission(PluginPermission.VOICEPITCH_CMD_SELF.get());
        final boolean canChangeOthers = sender.hasPermission(PluginPermission.VOICEPITCH_CMD_OTHER.get());

        if (!canChangeOwn && !canChangeOthers) {
            RoleplayExtras.config().cmd_no_permission.forEach(sender::sendMessage);
            return true;
        }

        if (args.length == 0) {
            if (canChangeOthers) {
                sender.sendMessage(Component.text("Missing arguments. Syntax: /voicepitch <pitch> (player)", KyoriUtil.wizardRed));
            } else {
                sender.sendMessage(Component.text("Missing argument. Syntax: /voicepitch <pitch>", KyoriUtil.wizardRed));
            }
            return true;
        }

        if (args.length == 1) {
            // The command is something like "/voicepitch 2"
            if (!canChangeOwn) {
                RoleplayExtras.config().cmd_no_permission.forEach(sender::sendMessage);
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Missing argument. Syntax: /voicepitch <pitch> <player>", KyoriUtil.wizardRed));
                return true;
            }

            final short newVoicePitch;
            try {
                newVoicePitch = args[0].equalsIgnoreCase("default") ? 1 : Short.parseShort(args[0]);
            } catch (NumberFormatException e) {
                RoleplayExtras.config().voicepitch_invalid_pitch_format.forEach(sender::sendMessage);
                return true;
            }

            try {
                setVoiceChatPitch(player, newVoicePitch);
                RoleplayExtras.config().voicepitch_success_self.forEach(line -> sender.sendMessage(line
                        .replaceText(TextReplacementConfig.builder().matchLiteral("%voicepitch%").replacement(Short.toString(newVoicePitch)).build())));
            } catch (IllegalArgumentException e) {
                RoleplayExtras.config().voicepitch_invalid_pitch.forEach(sender::sendMessage);
            }
        } else {
            // The command is something like "/voicepitch 2 someplayer"
            if (!canChangeOthers) {
                RoleplayExtras.config().cmd_no_permission.forEach(sender::sendMessage);
                return true;
            }

            @Nullable Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage(Component.text("Player '" + args[1] + "' is not online.", KyoriUtil.wizardRed));
                return true;
            }

            final short newVoicePitch;
            try {
                newVoicePitch = args[0].equalsIgnoreCase("default") ? 1 : Short.parseShort(args[0]);
            } catch (NumberFormatException e) {
                RoleplayExtras.config().voicepitch_invalid_pitch_format.forEach(sender::sendMessage);
                return true;
            }

            try {
                setVoiceChatPitch(player, newVoicePitch);
                RoleplayExtras.config().voicepitch_success_other.forEach(line -> sender.sendMessage(line
                        .replaceText(TextReplacementConfig.builder().matchLiteral("%player%").replacement(player.getName()).build())
                        .replaceText(TextReplacementConfig.builder().matchLiteral("%voicepitch%").replacement(Short.toString(newVoicePitch)).build())));
            } catch (IllegalArgumentException e) {
                RoleplayExtras.config().voicepitch_invalid_pitch.forEach(sender::sendMessage);
            }
        }

        return true;
    }

    @Override
    public String getPluginId() {
        return "voicepitch-command";
    }

    @Override
    public void registerEvents(EventRegistration eventRegistration) {
        eventRegistration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        eventRegistration.registerEvent(PlayerConnectedEvent.class, this::onPlayerConnected);
        eventRegistration.registerEvent(PlayerDisconnectedEvent.class, this::onPlayerDisconnected);
        eventRegistration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        voicechatServerApi = event.getVoicechat();
    }

    private void onPlayerConnected(PlayerConnectedEvent event) {
        final UUID playerUUID = event.getConnection().getPlayer().getUuid();
        if (!encoderMap.containsKey(playerUUID))
            encoderMap.put(playerUUID, event.getVoicechat().createEncoder());
        if (!decoderMap.containsKey(playerUUID))
            decoderMap.put(playerUUID, event.getVoicechat().createDecoder());
        if (!cachedPitchSettings.containsKey(playerUUID)
                && event.getConnection().getPlayer().getPlayer() instanceof Player player) {
            cachedPitchSettings.put(playerUUID, getVoiceChatPitch(player));
        }
    }

    private void onPlayerDisconnected(PlayerDisconnectedEvent event) {
        if (encoderMap.containsKey(event.getPlayerUuid()))
            encoderMap.remove(event.getPlayerUuid()).close();
        if (decoderMap.containsKey(event.getPlayerUuid()))
            decoderMap.remove(event.getPlayerUuid()).close();
        cachedPitchSettings.remove(event.getPlayerUuid());
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null) return;
        if (!(event.getSenderConnection().getPlayer().getPlayer() instanceof Player speakingPlayer)) return;

        final short voiceChatPitch = getVoiceChatPitch(speakingPlayer);
        if (voiceChatPitch == 1 || voiceChatPitch == 0) return;

        event.cancel(); // Cancel microphone packet event so we can adjust pitch and then send it ourselves

        final EntitySoundPacket pitchedSoundPacket = event.getPacket().entitySoundPacketBuilder()
                .entityUuid(speakingPlayer.getUniqueId())
                .opusEncodedData(pitchAudio(event.getPacket().getOpusEncodedData(), speakingPlayer, voiceChatPitch))
                .build();

        for (Player listeningPlayer : speakingPlayer.getWorld().getPlayers()) {
            // Don't send the audio to the player that is speaking
            if (listeningPlayer.getUniqueId().equals(speakingPlayer.getUniqueId())) continue;

            VoicechatConnection voiceConnection = event.getVoicechat().getConnectionOf(listeningPlayer.getUniqueId());
            // Check if the player is actually connected to the voice chat
            if (voiceConnection == null) continue;

            // Send the pitched entity packet
            event.getVoicechat().sendEntitySoundPacketTo(voiceConnection, pitchedSoundPacket);
        }
    }

    private byte[] pitchAudio(byte[] opusEncodedData, Player player, short pitch) {
        short[] decodedData = decoderMap.computeIfAbsent(player.getUniqueId(), k -> voicechatServerApi.createDecoder()).decode(opusEncodedData);
        short[] pitchedData = new short[decodedData.length];
        for (int i = 0; i < decodedData.length; i++) {
            pitchedData[i] = i * pitch < decodedData.length ? (decodedData[i * pitch]) : 0;
        }
        return encoderMap.computeIfAbsent(player.getUniqueId(), k -> voicechatServerApi.createEncoder()).encode(pitchedData);
    }

    private short getVoiceChatPitch(Player player) {
        if (cachedPitchSettings.containsKey(player.getUniqueId())) {
            return cachedPitchSettings.get(player.getUniqueId());
        }

        short pitch;
        if (player.getPersistentDataContainer().has(voicePitchKey)) {
            pitch = player.getPersistentDataContainer().get(voicePitchKey, PersistentDataType.SHORT);
        } else {
            pitch = 1;
        }

        return cachedPitchSettings.put(player.getUniqueId(), pitch);
    }

    private void setVoiceChatPitch(Player player, short pitch) {
        if (pitch == 1 || pitch == 0) {
            player.getPersistentDataContainer().remove(voicePitchKey);
        } else {
            player.getPersistentDataContainer().set(voicePitchKey, PersistentDataType.SHORT, pitch);
        }

        cachedPitchSettings.put(player.getUniqueId(), pitch);
    }
}
