package com.bedtwlserver.punish.core.event;

import com.bedtwlserver.punish.api.event.ServerEvent;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

/**
 * Ban 事件實現
 * 當一個玩家被 ban 時觸發
 */
public class BanServerEvent implements ServerEvent {

    @Getter
    private final long id;
    @Getter
    private final String sourceServer;
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

    public BanServerEvent(String sourceServer, UUID playerUUID, String playerName,
                         String executor, String reason, long expireTime) {
        this(0, sourceServer, playerUUID, playerName, executor, reason, expireTime);
    }

    public BanServerEvent(long id, String sourceServer, UUID playerUUID, String playerName,
                         String executor, String reason, long expireTime) {
        this.id = id;
        this.sourceServer = sourceServer;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.executor = executor;
        this.reason = reason;
        this.expireTime = expireTime;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String getEventType() {
        return "ban";
    }

    @Override
    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "ban");
        json.addProperty("source_server", sourceServer);
        json.addProperty("player_uuid", playerUUID.toString());
        json.addProperty("player_name", playerName);
        json.addProperty("executor", executor);
        json.addProperty("reason", reason);
        json.addProperty("expire_time", expireTime);
        json.addProperty("timestamp", timestamp);
        return json.toString();
    }
}
