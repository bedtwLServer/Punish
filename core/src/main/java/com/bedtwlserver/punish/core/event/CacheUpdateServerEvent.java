package com.bedtwlserver.punish.core.event;

import com.bedtwlserver.punish.api.event.ServerEvent;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

/**
 * 快取更新事件
 * 當某個伺服器新增/移除 ban/mute 時，通知所有其他伺服器更新快取
 */
public class CacheUpdateServerEvent implements ServerEvent {

    @Getter
    private final long id;
    @Getter
    private final String sourceServer;
    @Getter
    private final Action action;
    @Getter
    private final UUID playerUUID;
    @Getter
    private final String playerName;
    @Getter
    private final String executor;
    @Getter
    private final String reason;
    @Getter
    private final long expireTime;
    @Getter
    private final long timestamp;

    public CacheUpdateServerEvent(String sourceServer, Action action, UUID playerUUID,
                                   String playerName, String executor, String reason, long expireTime) {
        this(0, sourceServer, action, playerUUID, playerName, executor, reason, expireTime);
    }

    public CacheUpdateServerEvent(long id, String sourceServer, Action action, UUID playerUUID,
                                   String playerName, String executor, String reason, long expireTime) {
        this.id = id;
        this.sourceServer = sourceServer;
        this.action = action;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.executor = executor;
        this.reason = reason;
        this.expireTime = expireTime;
        this.timestamp = System.currentTimeMillis();
    }

    public static CacheUpdateServerEvent fromJson(long id, String sourceServer, UUID playerUUID, JsonObject json) {
        Action action = Action.valueOf(json.get("action").getAsString());
        String playerName = json.get("player_name").getAsString();
        String executor = json.get("executor").getAsString();
        String reason = json.get("reason").getAsString();
        long expireTime = json.get("expire_time").getAsLong();
        return new CacheUpdateServerEvent(id, sourceServer, action, playerUUID, playerName, executor, reason, expireTime);
    }

    @Override
    public String getEventType() {
        return "cache_update";
    }

    @Override
    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "cache_update");
        json.addProperty("action", action.name());
        json.addProperty("source_server", sourceServer);
        json.addProperty("player_uuid", playerUUID.toString());
        json.addProperty("player_name", playerName);
        json.addProperty("executor", executor);
        json.addProperty("reason", reason);
        json.addProperty("expire_time", expireTime);
        json.addProperty("timestamp", timestamp);
        return json.toString();
    }

    public enum Action {
        ADD_BAN, REMOVE_BAN, ADD_MUTE, REMOVE_MUTE
    }
}
