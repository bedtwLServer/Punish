package com.bedtwlserver.punish.core.command.impl;

import com.bedtwlserver.punish.core.Punish;
import com.bedtwlserver.punish.api.PunishAPI;
import com.bedtwlserver.punish.core.cache.CacheManager;
import com.bedtwlserver.punish.core.command.CommandBase;
import com.bedtwlserver.punish.core.event.BanServerEvent;
import com.bedtwlserver.punish.core.event.CacheUpdateServerEvent;
import com.bedtwlserver.punish.core.model.MojangProfile;
import com.bedtwlserver.punish.core.util.MojangApiUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

public class BanCommand extends CommandBase {
    @Override
    protected void execute(@NonNull CommandSender sender, @NonNull String label, String @NonNull [] args) {
        if (!sender.hasPermission("punish.ban")) {
            sender.sendMessage(color(plugin.getMessage("no_permission")));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(color(plugin.getMessage("ban_command_usage")));
            return;
        }

        String targetName = args[0];
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : plugin.getMessage("no_reason");
        String executor = sender.getName();
        Player online = Bukkit.getPlayerExact(targetName);

        if (online != null) {
            applyBan(sender, online.getName(), online.getUniqueId(), executor, reason);
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

            Bukkit.getScheduler().runTask(plugin, () -> applyBan(sender, profile.name(), profile.uuid(), executor, reason));
        });
    }

    private void applyBan(CommandSender sender, String playerName, java.util.UUID uuid, String executor, String reason) {
        // 寫入資料庫
        Punish.getStorage().addBan(uuid, playerName, executor, reason, -1L);

        // 更新快取
        CacheManager.putBan(uuid, new com.bedtwlserver.punish.core.model.PunishData(playerName, uuid, reason, executor, -1L));

        // 通知其他伺服器更新快取
        CacheUpdateServerEvent cacheEvent = new CacheUpdateServerEvent(
                Punish.instance.getServerId(),
                CacheUpdateServerEvent.Action.ADD_BAN,
                uuid, playerName, executor, reason, -1L
        );
        Punish.getStorage().addServerEvent(cacheEvent);

        // 觸發跨服 Ban 事件（BanEventListener 會處理踢出）
        BanServerEvent banEvent = new BanServerEvent(
                Punish.instance.getServerId(),
                uuid,
                playerName,
                executor,
                reason,
                -1L
        );
        if (PunishAPI.getServerEventRegistry() != null) {
            PunishAPI.getServerEventRegistry().fireEvent(banEvent);
        }

        sender.sendMessage(color(
                plugin.getMessage("ban_success").replace("{player}", playerName)
        ));
    }

    @Override
    protected List<String> getTabCompletions(@NonNull CommandSender sender, String @NonNull [] args) {
        if (args.length == 1) {
            return null;
        }
        return List.of();
    }
}
