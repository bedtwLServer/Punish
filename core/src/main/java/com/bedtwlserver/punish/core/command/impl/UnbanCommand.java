package com.bedtwlserver.punish.core.command.impl;

import com.bedtwlserver.punish.core.Punish;
import com.bedtwlserver.punish.core.command.CommandBase;
import com.bedtwlserver.punish.core.model.MojangProfile;
import com.bedtwlserver.punish.core.util.MojangApiUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class UnbanCommand extends CommandBase {
    @Override
    protected void execute(@NonNull CommandSender sender, @NonNull String label, String @NonNull [] args) {
        if (!sender.hasPermission("punish.unban")) {
            sender.sendMessage(color(plugin.getMessage("no_permission")));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(color(plugin.getMessage("unban_command_usage")));
            return;
        }
        sender.sendMessage(color(plugin.getMessage("requesting_mojang_api")));
        String targetName = args[0];
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            MojangProfile profile = MojangApiUtil.resolveProfile(targetName);
            if (profile == null) {
                Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(color(
                        plugin.getMessage("player_not_found").replace("{player}", targetName)
                )));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                Punish.getStorage().removeBan(profile.uuid());
                sender.sendMessage(color(
                        plugin.getMessage("unban_success").replace("{player}", profile.name())
                ));
            });
        });
    }

    @Override
    protected List<String> getTabCompletions(@NonNull CommandSender sender, String @NonNull [] args) {
        return List.of();
    }
}
