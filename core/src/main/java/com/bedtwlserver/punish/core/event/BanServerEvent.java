package com.bedtwlserver.punish.core.event;

import com.bedtwlserver.punish.api.event.ServerEvent;
import com.google.gson.JsonObject;

import java.util.UUID;

/**
 * Ban 事件實現
 * 當一個玩家被 ban 時觸發
 */
public class BanServerEvent implements ServerEvent {
    
    private final String sourceServer;
    private final UUID playerUUID;
    private final String playerName;
    private final String executor;
    private final String reason;
    private final long expireTime;
    private final long timestamp;
    
    public BanServerEvent(String sourceServer, UUID playerUUID, String playerName, 
                         String executor, String reason, long expireTime) {
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
    public String getSourceServer() {
        return sourceServer;
    }
    
    @Override
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public String getExecutor() {
        return executor;
    }
    
    public String getReason() {
        return reason;
    }
    
    public long getExpireTime() {
        return expireTime;
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
