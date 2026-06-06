package com.bedtwlserver.punish.core.command.impl;

import com.bedtwlserver.punish.core.Punish;
import com.bedtwlserver.punish.core.cache.CacheManager;
import com.bedtwlserver.punish.core.command.CommandBase;
import com.bedtwlserver.punish.core.event.CacheUpdateServerEvent;
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
                // 從資料庫移除
                Punish.getStorage().removeBan(profile.uuid());

                // 更新快取
                CacheManager.removeBan(profile.uuid());

                // 通知其他伺服器更新快取
                CacheUpdateServerEvent cacheEvent = new CacheUpdateServerEvent(
                        Punish.getServerId(),
                        CacheUpdateServerEvent.Action.REMOVE_BAN,
                        profile.uuid(), profile.name(), "", "", -1L
                );
                Punish.getStorage().addServerEvent(cacheEvent);

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
