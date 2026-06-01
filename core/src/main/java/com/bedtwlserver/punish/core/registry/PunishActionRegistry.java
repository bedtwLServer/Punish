package com.bedtwlserver.punish.core.registry;

import java.util.Map;

public class PunishActionRegistry implements com.bedtwlserver.punish.api.PunishActionRegistry {

    private Map<String, Runnable> actions;

    @Override
    public Runnable getAction(String name) {
        return actions.get(name);
    }

    @Override
    public void registerAction(String name, Runnable action) {
        if (actions.containsKey(name)) return;
        actions.put(name, action);
    }

    @Override
    public void unregisterAction(String name) {
        if (!actions.containsKey(name)) return;
        actions.remove(name);
    }
}
