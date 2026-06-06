package com.bedtwlserver.punish.core.command.impl;

import com.bedtwlserver.punish.core.Punish;
import com.bedtwlserver.punish.api.PunishAPI;
import com.bedtwlserver.punish.core.command.CommandBase;
import com.bedtwlserver.punish.core.event.MuteServerEvent;
import com.bedtwlserver.punish.core.model.MojangProfile;
import com.bedtwlserver.punish.core.util.MojangApiUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

public class MuteCommand extends CommandBase {
    @Override
    protected void execute(@NonNull CommandSender sender, @NonNull String label, String @NonNull [] args) {
        if (!sender.hasPermission("punish.mute")) {
            sender.sendMessage(color(plugin.getMessage("no_permission")));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(color(plugin.getMessage("mute_command_usage")));
            return;
        }

        String targetName = args[0];
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : plugin.getMessage("no_reason");
        String executor = sender.getName();
        Player online = Bukkit.getPlayerExact(targetName);

        if (online != null) {
            applyMute(sender, online.getName(), online.getUniqueId(), executor, reason);
            return;
        }
        sender.sendMessage(color(plugin.getMessage("requesting_mojang_api")));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            MojangProfile profile = MojangApiUtil.resolveProfile(targetName);
            if (profile == null) {
                Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(color(
                        plugin.getMessage("player_not_found").replace("{player}", targetName)
                )));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> applyMute(sender, profile.name(), profile.uuid(), executor, reason));
        });
    }

    private void applyMute(CommandSender sender, String playerName, java.util.UUID uuid, String executor, String reason) {
        Punish.getStorage().addMute(uuid, playerName, executor, reason, -1L);

        // 創建並觸發跨服 Mute 事件
        MuteServerEvent muteEvent = new MuteServerEvent(
                Punish.instance.getServerId(),
                uuid,
                playerName,
                executor,
                reason,
                -1L
        );
        if (PunishAPI.getServerEventRegistry() != null) {
            PunishAPI.getServerEventRegistry().fireEvent(muteEvent);
        }

        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            online.sendMessage(color(plugin.getMessage("denied_muted")
                    .replace("{expireAt}", plugin.getMessage("permanent"))
                    .replace("{executor}", executor)
                    .replace("{reason}", reason)));
        }
        sender.sendMessage(color(
                plugin.getMessage("mute_success").replace("{player}", playerName)
        ));
    }

    @Override
    protected List<String> getTabCompletions(@NonNull CommandSender sender, String @NonNull [] args) {
        return List.of();
    }
}
