package com.bedtwlserver.punish.core.cache;

import com.bedtwlserver.punish.core.model.PunishData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 記憶體快取管理器
 * 儲存所有 ban/mute 資料，避免每次查詢都存取資料庫
 */
public final class CacheManager {

    private static final Map<UUID, PunishData> bans = new ConcurrentHashMap<>();
    private static final Map<UUID, PunishData> mutes = new ConcurrentHashMap<>();

    private CacheManager() {
    }

    // ── Bans ───────────────────────────────────────────────────────────

    public static void putBan(UUID uuid, PunishData data) {
        bans.put(uuid, data);
    }

    public static void removeBan(UUID uuid) {
        bans.remove(uuid);
    }

    public static PunishData getBan(UUID uuid) {
        return bans.get(uuid);
    }

    public static boolean isBanned(UUID uuid) {
        return bans.containsKey(uuid);
    }

    public static Map<UUID, PunishData> getAllBans() {
        return bans;
    }

    // ── Mutes ──────────────────────────────────────────────────────────

    public static void putMute(UUID uuid, PunishData data) {
        mutes.put(uuid, data);
    }

    public static void removeMute(UUID uuid) {
        mutes.remove(uuid);
    }

    public static PunishData getMute(UUID uuid) {
        return mutes.get(uuid);
    }

    public static boolean isMuted(UUID uuid) {
        return mutes.containsKey(uuid);
    }

    public static Map<UUID, PunishData> getAllMutes() {
        return mutes;
    }

    // ── Bulk load ──────────────────────────────────────────────────────

    public static void loadBans(Map<UUID, PunishData> allBans) {
        bans.clear();
        bans.putAll(allBans);
    }

    public static void loadMutes(Map<UUID, PunishData> allMutes) {
        mutes.clear();
        mutes.putAll(allMutes);
    }

    // ── Stats ──────────────────────────────────────────────────────────

    public static int banCount() {
        return bans.size();
    }

    public static int muteCount() {
        return mutes.size();
    }
}
