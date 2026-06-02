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
import java.util.List;
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

            String stepName = input.readUTF();
            String targetName = input.readUTF();
            UUID targetUuid = UUID.fromString(input.readUTF());

            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                return;
            }

            List<String> steps = Punish.getPunishRegistry().getStep(stepName);
            if (steps == null || steps.isEmpty()) {
                return;
            }

            Bukkit.getScheduler().runTask(Punish.instance, () -> {
                for (String step : steps) {
                    String[] parts = step.split(" ");
                    if (parts.length == 0) {
                        continue;
                    }

                    String actionName = parts[0].toLowerCase();
                    PunishAction action = PunishAPI.getPunishActionRegistry().getAction(actionName);
                    if (action == null) {
                        continue;
                    }

                    String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];
                    action.onExecute(Bukkit.getConsoleSender(), target.getName(), targetUuid, args);
                }
            });
        } catch (IOException ignored) {
        }
    }
}
