package com.bedtwlserver.punish.core.event;

import com.bedtwlserver.punish.api.event.ServerEvent;
import com.bedtwlserver.punish.api.event.ServerEventListener;
import com.bedtwlserver.punish.api.event.ServerEventRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 伺服器事件註冊表實現
 */
public class ServerEventRegistryImpl implements ServerEventRegistry {
    
    private final List<ServerEventListener> listeners = new ArrayList<>();
    
    @Override
    public void registerListener(ServerEventListener listener) {
        for (ServerEventListener existing : listeners) {
            if (existing.getClass().equals(listener.getClass())) {
                return;
            }
        }
        listeners.add(listener);
    }
    
    @Override
    public void unregisterListener(ServerEventListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public void fireEvent(ServerEvent event) {
        List<ServerEventListener> targetListeners = getListeners(event.getEventType());
        for (ServerEventListener listener : targetListeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public List<ServerEventListener> getListeners() {
        return new ArrayList<>(listeners);
    }
    
    @Override
    public List<ServerEventListener> getListeners(String eventType) {
        return listeners.stream()
                .filter(listener -> "*".equals(listener.getEventType()) || eventType.equals(listener.getEventType()))
                .collect(Collectors.toList());
    }
}
