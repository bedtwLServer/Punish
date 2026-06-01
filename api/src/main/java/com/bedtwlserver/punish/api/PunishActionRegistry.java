package com.bedtwlserver.punish.api;


public interface PunishActionRegistry {
    Runnable getAction(String name);
    void registerAction(String name, Runnable action);
    void unregisterAction(String name);
}
