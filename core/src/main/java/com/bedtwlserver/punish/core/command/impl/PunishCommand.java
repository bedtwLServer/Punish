package com.bedtwlserver.punish.core.command.impl;

import com.bedtwlserver.punish.api.PunishAction;
import com.bedtwlserver.punish.api.PunishAPI;
import com.bedtwlserver.punish.core.Punish;
import com.bedtwlserver.punish.core.command.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

public class PunishCommand extends CommandBase {
    @Override
    protected void execute(@NonNull CommandSender sender, @NonNull String label, String @NonNull [] args) {
        if (args.length < 2) {
            sender.sendMessage(color(plugin.getMessage("punish_command_usage")));
            return;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(color(plugin.getMessage("player_not_found").replace("{player}", targetName)));
            return;
        }

        String stepName = args[1].toLowerCase();
        List<String> steps = Punish.getPunishRegistry().getStep(stepName);
        if (steps == null || steps.isEmpty()) {
            sender.sendMessage(color(plugin.getMessage("punish_action_not_found").replace("{action}", stepName)));
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String step : steps) {
                String[] parts = step.split(" ");
                if (parts.length == 0) {
                    continue;
                }

                String actionName = parts[0].toLowerCase();
                PunishAction action = PunishAPI.getPunishActionRegistry().getAction(actionName);
                if (action == null) {
                    sender.sendMessage(color(plugin.getMessage("punish_action_not_found").replace("{action}", actionName)));
                    continue;
                }

                String[] actionArgs = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];
                sender.sendMessage(color(plugin.getMessage("punish_executing_action").replace("{action}", actionName)));
                action.onExecute(Bukkit.getConsoleSender(), target.getName(), target.getUniqueId(), actionArgs);
            }
            Punish.instance.broadcastPunish(stepName, target);
            sender.sendMessage(color(
                    plugin.getMessage("punish_success")
                            .replace("{player}", target.getName())
                            .replace("{punish}", stepName)
            ));
        });
    }

    @Override
    protected List<String> getTabCompletions(@NonNull CommandSender sender, String @NonNull [] args) {
        return List.of();
    }
}
