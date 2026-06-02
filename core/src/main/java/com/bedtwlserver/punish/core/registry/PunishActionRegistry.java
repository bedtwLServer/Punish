package com.bedtwlserver.punish.core.registry;

import com.bedtwlserver.punish.api.PunishAction;

import java.util.HashMap;
import java.util.Map;

public class PunishActionRegistry implements com.bedtwlserver.punish.api.PunishActionRegistry {
    private final Map<String, PunishAction> actions = new HashMap<>();

    @Override
    public PunishAction getAction(String name) {
        return actions.get(name.toLowerCase());
    }

    @Override
    public void registerAction(String name, PunishAction action) {
        actions.putIfAbsent(name.toLowerCase(), action);
    }

    @Override
    public void unregisterAction(String name) {
        actions.remove(name.toLowerCase());
    }
}
