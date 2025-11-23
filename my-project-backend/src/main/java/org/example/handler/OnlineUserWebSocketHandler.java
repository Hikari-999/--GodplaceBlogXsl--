package org.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.service.BaseInfoService;
import org.example.utils.SpringContextUtil;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class OnlineUserWebSocketHandler extends TextWebSocketHandler {

    // 记录当前在线人数
    private static final AtomicInteger onlineCount = new AtomicInteger(0);

    // 存储所有连接的会话
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        int currentOnline = onlineCount.incrementAndGet();
        log.info("有新连接加入！当前在线人数为: {}", currentOnline);

        // 更新数据库中的在线人数
        updateOnlineCountInDatabase(currentOnline);

        // 广播给所有客户端当前在线人数
        broadcastOnlineCount(currentOnline);
    }

    /**
     * 处理收到的文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("来自客户端的消息: {}", message.getPayload());
        // 可以在这里处理客户端发送的消息
    }

    /**
     * 连接关闭调用的方法
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        int currentOnline = onlineCount.decrementAndGet();
        log.info("有一连接关闭！当前在线人数为: {}，关闭状态: {}", currentOnline, status);

        // 更新数据库中的在线人数
        updateOnlineCountInDatabase(currentOnline);

        // 广播给所有客户端当前在线人数
        broadcastOnlineCount(currentOnline);
    }

    /**
     * 发生错误时调用
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket发生错误，会话ID: {}", session.getId(), exception);
        sessions.remove(session.getId());
        int currentOnline = onlineCount.get();
        updateOnlineCountInDatabase(currentOnline);
        broadcastOnlineCount(currentOnline);
    }

    /**
     * 广播在线人数给所有连接的客户端
     * @param onlineCount 在线人数
     */
    private void broadcastOnlineCount(int onlineCount) {
        for (WebSocketSession session : sessions.values()) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(String.valueOf(onlineCount)));
                }
            } catch (Exception e) {
                log.error("发送消息失败", e);
            }
        }
    }

    /**
     * 更新数据库中的在线人数
     * @param count 当前在线人数
     */
    private void updateOnlineCountInDatabase(int count) {
        try {
            BaseInfoService baseInfoService = SpringContextUtil.getBean(BaseInfoService.class);
            if (baseInfoService != null) {
                baseInfoService.updateCurrentOnline(count);
            }
        } catch (Exception e) {
            log.error("更新数据库在线人数失败", e);
        }
    }

    /**
     * 获取当前在线人数
     * @return 当前在线人数
     */
    public static int getCurrentOnlineCount() {
        return onlineCount.get();
    }
}
