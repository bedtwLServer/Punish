package com.bedtwlserver.punish.core.event;

import com.bedtwlserver.punish.api.event.ServerEvent;
import com.google.gson.JsonObject;

import java.util.UUID;

/**
 * 通用懲罰事件
 * 用於執行配置中的懲罰步驟
 */
public class PunishStepServerEvent implements ServerEvent {
    
    private final long id;
    private final String sourceServer;
    private final String stepName;
    private final UUID playerUUID;
    private final String playerName;
    private final String executor;
    private final long timestamp;
    
    public PunishStepServerEvent(String sourceServer, String stepName, UUID playerUUID, 
                                 String playerName, String executor) {
        this(0, sourceServer, stepName, playerUUID, playerName, executor, System.currentTimeMillis());
    }
    
    public PunishStepServerEvent(long id, String sourceServer, String stepName, UUID playerUUID,
                                 String playerName, String executor, long timestamp) {
        this.id = id;
        this.sourceServer = sourceServer;
        this.stepName = stepName;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.executor = executor;
        this.timestamp = timestamp;
    }
    
    @Override
    public String getEventType() {
        return "punish_step";
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
    
    @Override
    public long getId() {
        return id;
    }
    
    public String getStepName() {
        return stepName;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public String getExecutor() {
        return executor;
    }
    
    @Override
    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "punish_step");
        json.addProperty("source_server", sourceServer);
        json.addProperty("step_name", stepName);
        json.addProperty("player_uuid", playerUUID.toString());
        json.addProperty("player_name", playerName);
        json.addProperty("executor", executor);
        json.addProperty("timestamp", timestamp);
        return json.toString();
    }
}
