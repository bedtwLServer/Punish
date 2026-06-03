package com.bedtwlserver.punish.core.model;

import java.util.UUID;

public record PunishEvent(long id, String stepName, UUID playerUUID, String playerName, String processedBy) {
}
