package com.bedtwlserver.punish.core.command.impl;

import com.bedtwlserver.punish.api.PunishAPI;
import com.bedtwlserver.punish.api.PunishAction;
import com.bedtwlserver.punish.core.Punish;
import com.bedtwlserver.punish.core.command.CommandBase;
import com.bedtwlserver.punish.core.event.PunishStepServerEvent;
import com.bedtwlserver.punish.core.model.MojangProfile;
import com.bedtwlserver.punish.core.util.MojangApiUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PunishCommand extends CommandBase {
    @Override
    protected void execute(@NonNull CommandSender sender, @NonNull String label, String @NonNull [] args) {
        if (!sender.hasPermission("punish.punish")) {
            sender.sendMessage(color(plugin.getMessage("no_permission")));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(color(plugin.getMessage("punish_command_usage")));
            return;
        }
        String targetName = args[0];

        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            executePunish(sender, online.getName(), online.getUniqueId(), args[1]);
            return;
        }

        sender.sendMessage(color(plugin.getMessage("requesting_mojang_api")));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            MojangProfile profile = MojangApiUtil.resolveProfile(targetName);

            if (profile == null) {
                Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(color(
                        plugin.getMessage("player_not_found")
                                .replace("{player}", targetName)
                )));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () ->
                    executePunish(sender, profile.name(), profile.uuid(), args[1]));
        });
    }

    private void executePunish(CommandSender sender, String playerName, UUID playerUuid, String stepName) {
        stepName = stepName.toLowerCase();

        List<String> steps = Punish.getPunishRegistry().getStep(stepName);
        if (steps == null || steps.isEmpty()) {
            sender.sendMessage(color(
                    plugin.getMessage("punish_action_not_found")
                            .replace("{action}", stepName)
            ));
            return;
        }

        // 創建懲罰步驟事件
        PunishStepServerEvent event = new PunishStepServerEvent(
                Punish.instance.getServerId(),
                stepName,
                playerUuid,
                playerName,
                sender.getName()
        );
        
        // 添加事件到資料庫，以便其他伺服器也能執行
        Punish.getStorage().addServerEvent(event);
        
        // 立即在本伺服器執行該事件
        if (PunishAPI.getServerEventRegistry() != null) {
            PunishAPI.getServerEventRegistry().fireEvent(event);
        }

        sender.sendMessage(color(plugin.getMessage("punish_success")
                        .replace("{player}", playerName)
                        .replace("{punish}", stepName)
        ));
    }

    @Override
    protected List<String> getTabCompletions(@NonNull CommandSender sender, String @NonNull [] args) {
        if (args.length == 1) {
            return null;
        }
        if (args.length == 2) {
            String input = args[1].toLowerCase();
            return Punish.getPunishRegistry().getStepNames().stream()
                    .filter(name -> name.startsWith(input))
                    .toList();
        }
        return List.of();
    }
}
