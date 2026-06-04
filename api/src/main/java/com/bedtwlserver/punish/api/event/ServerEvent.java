package com.bedtwlserver.punish.api.event;

import java.util.UUID;

/**
 * 通用的跨伺服器事件
 * 可用於任何伺服器間通訊，不限於懲罰系統
 */
public interface ServerEvent {
    
    /**
     * 獲取事件類型
     */
    String getEventType();
    
    /**
     * 獲取事件來源伺服器 ID
     */
    String getSourceServer();
    
    /**
     * 獲取涉及的玩家 UUID（可選，如果不相關則返回 null）
     */
    UUID getPlayerUUID();
    
    /**
     * 獲取事件創建時間（時間戳）
     */
    long getTimestamp();
    
    /**
     * 序列化事件為 JSON 字符串（用於存儲和傳輸）
     */
    String toJson();
}
