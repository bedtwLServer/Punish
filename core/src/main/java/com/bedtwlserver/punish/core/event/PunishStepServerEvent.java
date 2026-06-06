package com.bedtwlserver.punish.core.event;

import com.bedtwlserver.punish.api.event.ServerEvent;
import com.google.gson.JsonObject;

import java.util.UUID;

/**
 * 通用懲罰事件
 * 用於執行配置中的懲罰步驟
 */
public record PunishStepServerEvent(long id, String sourceServer, String stepName, UUID playerUUID, String playerName,
                                    String executor, long timestamp) implements ServerEvent {

    public PunishStepServerEvent(String sourceServer, String stepName, UUID playerUUID,
                                 String playerName, String executor) {
        this(0, sourceServer, stepName, playerUUID, playerName, executor, System.currentTimeMillis());
    }

    @Override
    public String getEventType() {
        return "punish_step";
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
