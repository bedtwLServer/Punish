package com.bedtwlserver.punish.api.event;

/**
 * 伺服器事件監聽器
 * 用於監聽和處理來自其他伺服器的事件
 */
public interface ServerEventListener {

    /**
     * 處理事件
     *
     * @param event 接收到的事件
     */
    void onEvent(ServerEvent event);

    /**
     * 獲取該監聽器支持的事件類型
     *
     * @return 支持的事件類型（如 "ban", "mute" 等），或 "*" 表示監聽所有事件
     */
    String getEventType();
}
