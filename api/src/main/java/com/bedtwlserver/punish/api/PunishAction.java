package com.bedtwlserver.punish.api;

import org.bukkit.command.CommandSender;

import java.util.UUID;

public interface PunishAction {

    void onExecute(CommandSender executor, String name, UUID uuid, String[] args);
}
