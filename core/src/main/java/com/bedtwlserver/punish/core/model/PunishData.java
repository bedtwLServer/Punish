package com.bedtwlserver.punish.core.model;

import java.util.UUID;

public record PunishData(String playerName, UUID playerUUID, String reason, String executor, long expireAt) {
}
