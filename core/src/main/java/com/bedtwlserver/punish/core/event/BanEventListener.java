package com.bedtwlserver.punish.core.event;

import com.bedtwlserver.punish.api.event.ServerEvent;
import com.bedtwlserver.punish.api.event.ServerEventListener;
import com.bedtwlserver.punish.core.Punish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Ban 事件監聽器
 * 處理來自其他伺服器的 Ban 事件
 */
public class BanEventListener implements ServerEventListener {

    @Override
    public void onEvent(ServerEvent event) {
        if (!(event instanceof BanServerEvent banEvent)) {
            return;
        }

        // 如果玩家在本伺服器在線，立即踢出
        Player player = Bukkit.getPlayer(banEvent.getPlayerUUID());
        if (player != null && player.isOnline()) {
            String kickMessage = Punish.instance.color(
                    Punish.instance.getMessage("denied_banned")
                            .replace("{expireAt}", banEvent.getExpireTime() == -1L ?
                                    Punish.instance.getMessage("permanent") :
                                    String.valueOf(banEvent.getExpireTime()))
                            .replace("{executor}", banEvent.getExecutor())
                            .replace("{reason}", banEvent.getReason())
            );
            player.kickPlayer(kickMessage);
            Punish.instance.getLogger().info("玩家 " + banEvent.getPlayerName() + " 被踢出 (Ban 事件)");
        }
    }

    @Override
    public String getEventType() {
        return "ban";
    }
}
