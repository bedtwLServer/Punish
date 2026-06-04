package com.bedtwlserver.punish.core.event;

import com.bedtwlserver.punish.api.event.ServerEvent;
import com.bedtwlserver.punish.api.event.ServerEventListener;
import com.bedtwlserver.punish.api.PunishAPI;
import com.bedtwlserver.punish.core.Punish;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;

/**
 * 懲罰步驟事件監聽器
 * 執行配置中的懲罰步驟
 */
public class PunishStepEventListener implements ServerEventListener {
    
    @Override
    public void onEvent(ServerEvent event) {
        if (!(event instanceof PunishStepServerEvent punishEvent)) {
            return;
        }
        
        Punish.instance.getLogger().info("執行懲罰步驟: " + punishEvent.getStepName());
        
        List<String> steps = Punish.getPunishRegistry().getStep(punishEvent.getStepName());
        if (steps == null || steps.isEmpty()) {
            Punish.instance.getLogger().warning("未找到懲罰步驟: " + punishEvent.getStepName());
            return;
        }
        
        for (String step : steps) {
            String[] parts = step.split(" ");
            if (parts.length == 0) continue;
            
            String actionName = parts[0].toLowerCase();
            com.bedtwlserver.punish.api.PunishAction action = 
                    PunishAPI.getPunishActionRegistry().getAction(actionName);
            
            if (action == null) {
                Punish.instance.getLogger().warning("未找到懲罰動作: " + actionName);
                continue;
            }
            
            String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];
            
            try {
                action.onExecute(Bukkit.getConsoleSender(), punishEvent.getPlayerName(), 
                        punishEvent.getPlayerUUID(), args);
                Punish.instance.getLogger().info("已執行動作: " + actionName);
            } catch (Exception e) {
                Punish.instance.getLogger().warning("執行動作失敗: " + actionName + " - " + e.getMessage());
            }
        }
    }
    
    @Override
    public String getEventType() {
        return "punish_step";
    }
}
