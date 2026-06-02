package com.bedtwlserver.punish.api;


public interface PunishActionRegistry {
    PunishAction getAction(String name);
    void registerAction(String name, PunishAction action);
    void unregisterAction(String name);
}
