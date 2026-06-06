package com.bedtwlserver.punish.core.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PunishRegistry {
    private final Map<String, List<String>> steps = new HashMap<>();

    public void registerStep(String name, List<String> actions) {
        steps.put(name.toLowerCase(), actions);
    }

    public void unregisterStep(String name) {
        steps.remove(name.toLowerCase());
    }

    public List<String> getStep(String name) {
        return steps.get(name.toLowerCase());
    }

    public java.util.Set<String> getStepNames() {
        return steps.keySet();
    }
}
