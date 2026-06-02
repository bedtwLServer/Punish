package com.bedtwlserver.punish.core.action;

import com.bedtwlserver.punish.api.PunishAction;
import com.bedtwlserver.punish.core.Punish;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public class BanAction implements PunishAction {
    @Override
    public void onExecute(CommandSender executor, String name, UUID uuid, String[] args) {
        if (args.length < 1) {
            return;
        }
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : Punish.instance.getMessage("no_reason");
        String executorName = executor instanceof Player ? executor.getName() : Punish.instance.getMessage("console");
        Punish.getStorage().addBan(uuid, name, executorName, reason, -1L);
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            online.kickPlayer(Punish.instance.color(Punish.instance.getMessage("denied_banned")
                    .replace("{expireAt}", Punish.instance.getMessage("permanent"))
                    .replace("{executor}", executorName)
                    .replace("{reason}", reason)));
        }
    }
}
