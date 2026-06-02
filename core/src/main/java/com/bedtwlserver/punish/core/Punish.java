package com.bedtwlserver.punish.core;

import com.bedtwlserver.punish.api.PunishAPI;
import com.bedtwlserver.punish.core.action.BanAction;
import com.bedtwlserver.punish.core.action.MuteAction;
import com.bedtwlserver.punish.core.command.CommandBase;
import com.bedtwlserver.punish.core.command.impl.BanCommand;
import com.bedtwlserver.punish.core.command.impl.PunishCommand;
import com.bedtwlserver.punish.core.command.impl.MuteCommand;
import com.bedtwlserver.punish.core.command.impl.UnbanCommand;
import com.bedtwlserver.punish.core.command.impl.UnmuteCommand;
import com.bedtwlserver.punish.core.listener.PlayerEvent;
import com.bedtwlserver.punish.core.listener.PunishPluginMessageListener;
import com.bedtwlserver.punish.core.registry.PunishActionRegistry;
import com.bedtwlserver.punish.core.registry.PunishRegistry;
import com.bedtwlserver.punish.core.storage.Storage;
import com.bedtwlserver.punish.core.storage.impl.MySQLStorage;
import com.bedtwlserver.punish.core.storage.impl.SQLiteStorage;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Punish extends JavaPlugin {

    public static Punish instance;

    @Getter
    private static Storage storage;

    @Getter
    private static final PunishRegistry punishRegistry = new PunishRegistry();

    @Override
    public void onLoad() {
        saveDefaultConfig();
        PunishAPI.setPunishActionRegistry(new PunishActionRegistry());
        PunishAPI.getPunishActionRegistry().registerAction("ban", new BanAction());
        PunishAPI.getPunishActionRegistry().registerAction("mute", new MuteAction());
        loadPunishActions();
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PunishPluginMessageListener());
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

    public void broadcastPunish(String actionName, Player target) {
        try {
            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream data = new java.io.DataOutputStream(output);
            data.writeUTF("PunishExecute");
            data.writeUTF(actionName);
            data.writeUTF(target.getName());
            data.writeUTF(target.getUniqueId().toString());
            Bukkit.getServer().sendPluginMessage(this, "BungeeCord", output.toByteArray());
        } catch (Exception e) {
            getLogger().warning("無法廣播 punish: " + e.getMessage());
        }
    }

}
