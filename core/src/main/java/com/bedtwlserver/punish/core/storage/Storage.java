package com.bedtwlserver.punish.core.storage;

import com.bedtwlserver.punish.core.model.PunishData;

import java.util.UUID;
import java.util.List;
import com.bedtwlserver.punish.core.model.PunishEvent;

public abstract class Storage {
    public abstract void connect();

    public abstract void addBan(UUID uuid, String playerName, String executor, String reason, long expireTime);
    public abstract void addMute(UUID uuid, String playerName, String executor, String reason, long expireTime);

    public abstract boolean isBanned(UUID uuid);
    public abstract boolean isMuted(UUID uuid);

    public abstract void removeBan(UUID uuid);
    public abstract void removeMute(UUID uuid);

    public abstract PunishData getBan(UUID uuid);
    public abstract PunishData getMute(UUID uuid);

    public abstract void addPunishEvent(String stepName, UUID uuid, String playerName);
    public abstract List<PunishEvent> getPunishEvents(String serverId);
    public abstract void markPunishEventProcessed(long id, String serverId);

    public abstract void disconnect();
}
