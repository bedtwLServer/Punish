package com.bedtwlserver.punish.core.event;

import com.bedtwlserver.punish.api.event.ServerEvent;
import com.bedtwlserver.punish.api.event.ServerEventListener;
import com.bedtwlserver.punish.core.Punish;
import org.bukkit.entity.Player;

/**
 * Mute 事件監聽器
 * 處理來自其他伺服器的 Mute 事件
 */
public class MuteEventListener implements ServerEventListener {

    @Override
    public void onEvent(ServerEvent event) {
        if (!(event instanceof MuteServerEvent muteEvent)) {
            return;
        }

        Player player = org.bukkit.Bukkit.getPlayer(muteEvent.getPlayerUUID());
        if (player != null && player.isOnline()) {
            player.sendMessage(Punish.instance.color(
                    Punish.instance.getMessage("denied_muted")
                            .replace("{expireAt}", muteEvent.getExpireTime() == -1L ?
                                    Punish.instance.getMessage("permanent") :
                                    String.valueOf(muteEvent.getExpireTime()))
                            .replace("{executor}", muteEvent.getExecutor())
                            .replace("{reason}", muteEvent.getReason())
            ));
        }
    }

    @Override
    public String getEventType() {
        return "mute";
    }
}
