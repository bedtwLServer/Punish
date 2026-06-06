package com.bedtwlserver.punish.core.event;

import com.bedtwlserver.punish.api.event.ServerEvent;
import com.bedtwlserver.punish.api.event.ServerEventListener;
import com.bedtwlserver.punish.core.Punish;
import com.bedtwlserver.punish.core.cache.CacheManager;
import com.bedtwlserver.punish.core.model.PunishData;

import java.util.UUID;

/**
 * 快取更新事件監聽器
 * 接收來自其他伺服器的快取更新通知，同步本地快取
 */
public class CacheUpdateEventListener implements ServerEventListener {

    @Override
    public void onEvent(ServerEvent event) {
        if (!(event instanceof CacheUpdateServerEvent cacheEvent)) {
            return;
        }

        UUID uuid = cacheEvent.getPlayerUUID();

        switch (cacheEvent.getAction()) {
            case ADD_BAN -> CacheManager.putBan(uuid, new PunishData(
                    cacheEvent.getPlayerName(), uuid,
                    cacheEvent.getReason(), cacheEvent.getExecutor(),
                    cacheEvent.getExpireTime()
            ));
            case REMOVE_BAN -> CacheManager.removeBan(uuid);
            case ADD_MUTE -> CacheManager.putMute(uuid, new PunishData(
                    cacheEvent.getPlayerName(), uuid,
                    cacheEvent.getReason(), cacheEvent.getExecutor(),
                    cacheEvent.getExpireTime()
            ));
            case REMOVE_MUTE -> CacheManager.removeMute(uuid);
        }

        Punish.instance.getLogger().info("快取已同步: " + cacheEvent.getAction() + " " + cacheEvent.getPlayerName());
    }

    @Override
    public String getEventType() {
        return "cache_update";
    }
}
