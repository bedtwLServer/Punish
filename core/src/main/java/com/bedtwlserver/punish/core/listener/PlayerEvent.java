package com.bedtwlserver.punish.core.listener;

import com.bedtwlserver.punish.core.Punish;
import com.bedtwlserver.punish.core.model.PunishData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PlayerEvent implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void asyncPreLogin(AsyncPlayerPreLoginEvent event) {
        PunishData punishData = Punish.getStorage().getBan(event.getUniqueId());
        if (punishData == null) return;
        if (punishData.expireAt() != -1 && punishData.expireAt() < System.currentTimeMillis()) {
            Bukkit.getScheduler().runTaskAsynchronously(Punish.instance, () -> Punish.getStorage().removeBan(event.getUniqueId()));
            return;
        }
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Punish.instance.color(Punish.instance.getMessage("denied_banned")
                .replace("{duration}", String.valueOf(punishData.expireAt()))
                .replace("{executor}", punishData.executor())
                .replace("{reason}", punishData.reason()))
                .replace("{expireAt}", punishData.expireAt() >= 0 ? String.valueOf(punishData.expireAt()) : Punish.instance.getMessage("permanent")));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void asyncChat(AsyncPlayerChatEvent event) {
        PunishData punishData = Punish.getStorage().getMute(event.getPlayer().getUniqueId());
        if (punishData == null) return;
        if (punishData.expireAt() != -1 && punishData.expireAt() < System.currentTimeMillis()) {
            Bukkit.getScheduler().runTaskAsynchronously(Punish.instance, () -> Punish.getStorage().removeBan(event.getPlayer().getUniqueId()));
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(Punish.instance.color(Punish.instance.getMessage("denied_muted")
                .replace("{duration}", String.valueOf(punishData.expireAt()))
                .replace("{executor}", punishData.executor())
                .replace("{reason}", punishData.reason()))
                .replace("{expireAt}", punishData.expireAt() < 1 ? String.valueOf(punishData.expireAt()) : Punish.instance.getMessage("permanent")));

    }
}
