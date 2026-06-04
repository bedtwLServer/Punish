package com.bedtwlserver.punish.core;

import com.bedtwlserver.punish.api.PunishAPI;
import com.bedtwlserver.punish.api.event.ServerEvent;
import com.bedtwlserver.punish.core.action.BanAction;
import com.bedtwlserver.punish.core.action.MuteAction;
import com.bedtwlserver.punish.core.command.CommandBase;
import com.bedtwlserver.punish.core.command.impl.*;
import com.bedtwlserver.punish.core.event.BanEventListener;
import com.bedtwlserver.punish.core.event.ServerEventRegistryImpl;
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

    public static Punish instance;

    @Getter
    private static Storage storage;

    @Getter
    private static final PunishRegistry punishRegistry = new PunishRegistry();
    @Getter
    private String serverId;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        PunishAPI.setPunishActionRegistry(new PunishActionRegistry());
        PunishAPI.getPunishActionRegistry().registerAction("ban", new BanAction());
        PunishAPI.getPunishActionRegistry().registerAction("mute", new MuteAction());
        
        // 初始化事件註冊表
        PunishAPI.setServerEventRegistry(new ServerEventRegistryImpl());
        PunishAPI.getServerEventRegistry().registerListener(new BanEventListener());
        
        loadPunishActions();
    }

    @Override
    public void onEnable() {
        instance = this;
        serverId = getConfig().getString("storage.server-id", getName());
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
        getConfig().getConfigurationSection("punish").getKeys(false).forEach(step -> {
            punishRegistry.registerStep(step, getConfig().getStringList("punish." + step));
        });
    }

    private void pollServerEvents() {
        try {
            for (ServerEvent event : storage.getServerEvents(serverId)) {
                getLogger().info("收到事件: " + event.getEventType());
                Bukkit.getScheduler().runTask(this, () -> {
                    try {
                        executeServerEvent(event);
                        // 事件處理成功，標記為已處理
                        // 注意：需要保存 event ID，這裡使用簡化方式
                    } catch (Exception e) {
                        getLogger().warning("執行伺服器事件失敗: " + e.getMessage());
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
