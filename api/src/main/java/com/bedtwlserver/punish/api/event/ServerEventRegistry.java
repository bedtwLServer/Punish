package com.bedtwlserver.punish.api.event;

import java.util.List;

/**
 * 伺服器事件註冊表
 * 管理事件監聽器的註冊和觸發
 */
public interface ServerEventRegistry {

    /**
     * 註冊事件監聽器
     *
     * @param listener 事件監聽器
     */
    void registerListener(ServerEventListener listener);

    /**
     * 註銷事件監聽器
     *
     * @param listener 事件監聽器
     */
    void unregisterListener(ServerEventListener listener);

    /**
     * 觸發事件（廣播到所有相關監聽器）
     *
     * @param event 要觸發的事件
     */
    void fireEvent(ServerEvent event);

    /**
     * 獲取所有監聽器
     */
    List<ServerEventListener> getListeners();

    /**
     * 獲取特定事件類型的監聽器
     *
     * @param eventType 事件類型
     */
    List<ServerEventListener> getListeners(String eventType);
}
