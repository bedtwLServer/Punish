package com.bedtwlserver.punish.core.action;

import com.bedtwlserver.punish.api.PunishAction;
import com.bedtwlserver.punish.core.Punish;
import com.bedtwlserver.punish.core.cache.CacheManager;
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

        // 寫入資料庫
        Punish.getStorage().addBan(uuid, name, executorName, reason, -1L);

        // 更新本地快取
        CacheManager.putBan(uuid, new com.bedtwlserver.punish.core.model.PunishData(name, uuid, reason, executorName, -1L));

        // 注意：不在此處觸發 cache_update 或 ban 事件
        // 這些事件應由發出指令的伺服器（BanCommand/MuteCommand）統一觸發
        // 避免跨伺服器懲罰步驟（punish_step）觸發重複事件
    }
}
