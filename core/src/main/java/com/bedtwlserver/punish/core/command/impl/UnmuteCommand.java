package com.bedtwlserver.punish.core.command.impl;

import com.bedtwlserver.punish.core.Punish;
import com.bedtwlserver.punish.core.cache.CacheManager;
import com.bedtwlserver.punish.core.command.CommandBase;
import com.bedtwlserver.punish.core.event.CacheUpdateServerEvent;
import com.bedtwlserver.punish.core.model.MojangProfile;
import com.bedtwlserver.punish.core.util.MojangApiUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class UnmuteCommand extends CommandBase {
    @Override
    protected void execute(@NonNull CommandSender sender, @NonNull String label, String @NonNull [] args) {
        if (!sender.hasPermission("punish.unmute")) {
            sender.sendMessage(color(plugin.getMessage("no_permission")));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(color(plugin.getMessage("unmute_command_usage")));
            return;
        }

        String targetName = args[0];
        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            Punish.getStorage().removeMute(online.getUniqueId());
            CacheManager.removeMute(online.getUniqueId());

            CacheUpdateServerEvent cacheEvent = new CacheUpdateServerEvent(
                    Punish.getServerId(),
                    CacheUpdateServerEvent.Action.REMOVE_MUTE,
                    online.getUniqueId(), online.getName(), "", "", -1L
            );
            Punish.getStorage().addServerEvent(cacheEvent);

            sender.sendMessage(color(
                    plugin.getMessage("unmute_success").replace("{player}", online.getName())
            ));
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

            Bukkit.getScheduler().runTask(plugin, () -> {
                Punish.getStorage().removeMute(profile.uuid());
                CacheManager.removeMute(profile.uuid());

                CacheUpdateServerEvent cacheEvent = new CacheUpdateServerEvent(
                        Punish.getServerId(),
                        CacheUpdateServerEvent.Action.REMOVE_MUTE,
                        profile.uuid(), profile.name(), "", "", -1L
                );
                Punish.getStorage().addServerEvent(cacheEvent);

                sender.sendMessage(color(
                        plugin.getMessage("unmute_success").replace("{player}", profile.name())
                ));
            });
        });
    }

    @Override
    protected List<String> getTabCompletions(@NonNull CommandSender sender, String @NonNull [] args) {
        if (args.length == 1) {
            return null;
        }
        return List.of();
    }
}
