package com.bedtwlserver.punish.core;

import com.bedtwlserver.punish.api.PunishAPI;
import com.bedtwlserver.punish.api.event.ServerEvent;
import com.bedtwlserver.punish.core.action.BanAction;
import com.bedtwlserver.punish.core.action.MuteAction;
import com.bedtwlserver.punish.core.cache.CacheManager;
import com.bedtwlserver.punish.core.command.CommandBase;
import com.bedtwlserver.punish.core.command.impl.*;
import com.bedtwlserver.punish.core.event.*;
import com.bedtwlserver.punish.core.listener.PlayerEvent;
import com.bedtwlserver.punish.core.registry.PunishActionRegistry;
import com.bedtwlserver.punish.core.registry.PunishRegistry;
import com.bedtwlserver.punish.core.storage.Storage;
import com.bedtwlserver.punish.core.storage.impl.MySQLStorage;
import com.bedtwlserver.punish.core.storage.impl.SQLiteStorage;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Punish extends JavaPlugin {

    // Static accessor for PunishRegistry (Lombok @Getter on static field doesn't produce static getter)
    @Getter
    private static final PunishRegistry punishRegistry = new PunishRegistry();
    public static Punish instance;
    @Getter
    private static Storage storage;
    private String serverId;

    // Static accessor for serverId (instance field, needs instance reference)
    public static String getServerId() {
        return instance != null ? instance.serverId : null;
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();
        PunishAPI.setPunishActionRegistry(new PunishActionRegistry());
        PunishAPI.getPunishActionRegistry().registerAction("ban", new BanAction());
        PunishAPI.getPunishActionRegistry().registerAction("mute", new MuteAction());

        // 初始化事件註冊表
        PunishAPI.setServerEventRegistry(new ServerEventRegistryImpl());
        PunishAPI.getServerEventRegistry().registerListener(new BanEventListener());
        PunishAPI.getServerEventRegistry().registerListener(new PunishStepEventListener());
        PunishAPI.getServerEventRegistry().registerListener(new MuteEventListener());
        PunishAPI.getServerEventRegistry().registerListener(new CacheUpdateEventListener());

        loadPunishActions();
    }

    @Override
    public void onEnable() {
        instance = this;
        serverId = getConfig().getString("storage.server-id", null);
        if (serverId == null || serverId.isEmpty()) {
            serverId = java.util.UUID.randomUUID().toString().substring(0, 8);
            getConfig().set("storage.server-id", serverId);
            saveConfig();
            getLogger().info("已生成隨機 server-id: " + serverId);
        }
        try {
            String use = getConfig().getString("storage.use", "sqlite").toLowerCase();
            switch (use) {
                case "mysql" -> storage = new MySQLStorage(
                        getConfig().getString("storage.mysql.address", "127.0.0.1"),
                        getConfig().getInt("storage.mysql.port", 3306),
                        getConfig().getString("storage.mysql.database", "punish"),
                        getConfig().getString("storage.mysql.username", "root"),
                        getConfig().getString("storage.mysql.password", "")
                );
                case "sqlite" -> storage = new SQLiteStorage(
                        getConfig().getString("storage.sqlite.file", "database.db")
                );
                default -> throw new IllegalArgumentException("無效的 storage.use: " + use);
            }

            storage.connect();
            getLogger().info("已啟用資料庫: " + use);
        } catch (Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }

        // 載入所有 ban/mute 到記憶體快取
        try {
            CacheManager.loadBans(storage.loadAllBans());
            CacheManager.loadMutes(storage.loadAllMutes());
            getLogger().info("快取載入完成: " + CacheManager.banCount() + " bans, " + CacheManager.muteCount() + " mutes");
        } catch (Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerListener(new PlayerEvent());
        registerCommand("punish", new PunishCommand());
        registerCommand("ban", new BanCommand());
        registerCommand("unban", new UnbanCommand());
        registerCommand("mute", new MuteCommand());
        registerCommand("unmute", new UnmuteCommand());
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::pollServerEvents, 20L, 20L);

    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.disconnect();
        }
    }

    public void disablePlugin() {
        getPluginLoader().disablePlugin(this);
    }

    private void registerListener(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    private void registerCommand(String name, CommandBase commandBase) {
        getCommand(name).setExecutor(commandBase);
        getCommand(name).setTabCompleter(commandBase);
    }

    public String getMessage(String key) {
        return getConfig().getString("message." + key, key);
    }

    public String color(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private void loadPunishActions() {
        if (getConfig().getConfigurationSection("punish") == null) {
            return;
        }
        getConfig().getConfigurationSection("punish").getKeys(false).forEach(step -> punishRegistry.registerStep(step, getConfig().getStringList("punish." + step)));
    }

    private void pollServerEvents() {
        try {
            for (ServerEvent event : storage.getServerEvents(serverId)) {
                getLogger().info("收到事件: " + event.getEventType() + " (ID: " + event.id() + ")");
                Bukkit.getScheduler().runTask(this, () -> {
                    try {
                        executeServerEvent(event);
                        // 標記事件為已處理
                        if (event.id() > 0) {
                            storage.markServerEventProcessed(event.id(), serverId);
                            getLogger().info("已標記事件為已處理 (ID: " + event.id() + ")");
                        }
                    } catch (Exception e) {
                        getLogger().warning("執行伺服器事件失敗: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            getLogger().warning("輪詢伺服器事件失敗: " + e.getMessage());
        }
    }

    private void executeServerEvent(ServerEvent event) {
        getLogger().info("執行伺服器事件: " + event.getEventType());
        // 觸發事件監聽器
        if (PunishAPI.getServerEventRegistry() != null) {
            PunishAPI.getServerEventRegistry().fireEvent(event);
        }
    }

}
