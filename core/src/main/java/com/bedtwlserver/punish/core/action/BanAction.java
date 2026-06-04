package com.bedtwlserver.punish.core.action;

import com.bedtwlserver.punish.api.PunishAction;
import com.bedtwlserver.punish.api.PunishAPI;
import com.bedtwlserver.punish.core.Punish;
import com.bedtwlserver.punish.core.event.BanServerEvent;
import org.bukkit.Bukkit;
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
        
        // 創建並觸發 Ban 事件
        BanServerEvent banEvent = new BanServerEvent(
                Punish.instance.getServerId(),
                uuid,
                name,
                executorName,
                reason,
                -1L
        );
        
        // 觸發事件（本地監聽器會處理踢出邏輯）
        if (PunishAPI.getServerEventRegistry() != null) {
            PunishAPI.getServerEventRegistry().fireEvent(banEvent);
        }
        
        // 如果玩家在線，踢出（作為備用）
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            online.kickPlayer(Punish.instance.color(Punish.instance.getMessage("denied_banned")
                    .replace("{expireAt}", Punish.instance.getMessage("permanent"))
                    .replace("{executor}", executorName)
                    .replace("{reason}", reason)));
        }
    }
}
