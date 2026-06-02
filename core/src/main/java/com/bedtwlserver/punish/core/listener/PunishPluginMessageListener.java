package com.bedtwlserver.punish.core.listener;

import com.bedtwlserver.punish.api.PunishAction;
import com.bedtwlserver.punish.api.PunishAPI;
import com.bedtwlserver.punish.core.Punish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class PunishPluginMessageListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!"BungeeCord".equals(channel)) {
            return;
        }

        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(message))) {
            String subChannel = input.readUTF();
            if (!"PunishExecute".equals(subChannel)) {
                return;
            }

            String actionName = input.readUTF();
            String targetName = input.readUTF();
            UUID targetUuid = UUID.fromString(input.readUTF());
            int argsLength = input.readInt();
            String[] args = new String[argsLength];
            for (int i = 0; i < argsLength; i++) {
                args[i] = input.readUTF();
            }

            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                return;
            }

            PunishAction action = PunishAPI.getPunishActionRegistry().getAction(actionName);
            if (action == null) {
                return;
            }

            action.onExecute(Bukkit.getConsoleSender(), target.getName(), targetUuid, args);
        } catch (IOException ignored) {
        }
    }
}
