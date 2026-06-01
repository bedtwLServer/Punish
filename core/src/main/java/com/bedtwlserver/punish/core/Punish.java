package com.bedtwlserver.punish.core;

import com.bedtwlserver.punish.api.PunishAPI;
import com.bedtwlserver.punish.core.command.CommandBase;
import com.bedtwlserver.punish.core.command.impl.BanCommand;
import com.bedtwlserver.punish.core.command.impl.MuteCommand;
import com.bedtwlserver.punish.core.command.impl.UnbanCommand;
import com.bedtwlserver.punish.core.command.impl.UnmuteCommand;
import com.bedtwlserver.punish.core.listener.PlayerEvent;
import com.bedtwlserver.punish.core.registry.PunishActionRegistry;
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

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        PunishAPI.setPunishActionRegistry(new PunishActionRegistry());
        instance = this;
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
        registerCommand("ban", new BanCommand());
        registerCommand("unban", new UnbanCommand());
        registerCommand("mute", new MuteCommand());
        registerCommand("unmute", new UnmuteCommand());

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

}
