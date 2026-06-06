package com.bedtwlserver.punish.core.action;

import com.bedtwlserver.punish.api.PunishAPI;
import com.bedtwlserver.punish.api.PunishAction;
import com.bedtwlserver.punish.core.Punish;
import com.bedtwlserver.punish.core.cache.CacheManager;
import com.bedtwlserver.punish.core.event.BanServerEvent;
import com.bedtwlserver.punish.core.event.CacheUpdateServerEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public class BanAction implements PunishAction {
    @Override
    public void onExecute(CommandSender executor, String name, UUID uuid, String[] args) {
        if (args.length < 1) {
            return;
        }
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : Punish.instance.getMessage("no_reason");
        String executorName = executor instanceof Player ? executor.getName() : Punish.instance.getMessage("console");

        // 添加到資料庫
        Punish.getStorage().addBan(uuid, name, executorName, reason, -1L);

        // 更新快取
        CacheManager.putBan(uuid, new com.bedtwlserver.punish.core.model.PunishData(name, uuid, reason, executorName, -1L));

        // 通知其他伺服器更新快取
        CacheUpdateServerEvent cacheEvent = new CacheUpdateServerEvent(
                Punish.getServerId(),
                CacheUpdateServerEvent.Action.ADD_BAN,
                uuid, name, executorName, reason, -1L
        );
        Punish.getStorage().addServerEvent(cacheEvent);

        // 創建並觸發 Ban 事件（BanEventListener 會處理踢出）
        BanServerEvent banEvent = new BanServerEvent(
                Punish.getServerId(),
                uuid,
                name,
                executorName,
                reason,
                -1L
        );
        if (PunishAPI.getServerEventRegistry() != null) {
            PunishAPI.getServerEventRegistry().fireEvent(banEvent);
        }
    }
}
