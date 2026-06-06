package com.bedtwlserver.punish.core.storage;

import com.bedtwlserver.punish.api.event.ServerEvent;
import com.bedtwlserver.punish.core.model.PunishData;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public abstract Map<UUID, PunishData> loadAllBans();

    public abstract Map<UUID, PunishData> loadAllMutes();

    public abstract void addServerEvent(ServerEvent event);

    public abstract List<ServerEvent> getServerEvents(String serverId);

    public abstract void markServerEventProcessed(long id, String serverId);

    public abstract void disconnect();
}
