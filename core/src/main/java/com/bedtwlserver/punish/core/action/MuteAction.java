package com.bedtwlserver.punish.core.action;

import com.bedtwlserver.punish.api.PunishAction;
import com.bedtwlserver.punish.core.Punish;
import com.bedtwlserver.punish.core.cache.CacheManager;
import com.bedtwlserver.punish.core.event.CacheUpdateServerEvent;
import com.bedtwlserver.punish.core.event.MuteServerEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public class MuteAction implements PunishAction {
    @Override
    public void onExecute(CommandSender executor, String name, UUID uuid, String[] args) {
        if (args.length < 1) {
            return;
        }
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : Punish.instance.getMessage("no_reason");
        String executorName = executor instanceof Player ? executor.getName() : Punish.instance.getMessage("console");

        // 添加到資料庫
        Punish.getStorage().addMute(uuid, name, executorName, reason, -1L);

        // 更新快取
        CacheManager.putMute(uuid, new com.bedtwlserver.punish.core.model.PunishData(name, uuid, reason, executorName, -1L));

        // 通知其他伺服器更新快取
        CacheUpdateServerEvent cacheEvent = new CacheUpdateServerEvent(
                Punish.getServerId(),
                CacheUpdateServerEvent.Action.ADD_MUTE,
                uuid, name, executorName, reason, -1L
        );
        Punish.getStorage().addServerEvent(cacheEvent);

        // 觸發跨服 Mute 事件
        MuteServerEvent muteEvent = new MuteServerEvent(
                Punish.getServerId(),
                uuid,
                name,
                executorName,
                reason,
                -1L
        );
        com.bedtwlserver.punish.api.PunishAPI.getServerEventRegistry().fireEvent(muteEvent);
    }
}
