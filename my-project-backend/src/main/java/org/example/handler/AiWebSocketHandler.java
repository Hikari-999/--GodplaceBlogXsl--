package org.example.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.service.AiService;
import org.example.utils.SpringContextUtil;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AiWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicBoolean> sessionCancelled = new ConcurrentHashMap<>();//会话的连接状态 默认为false
    private AiService aiService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        sessionCancelled.put(session.getId(), new AtomicBoolean(false));
        log.info("AI WebSocket连接已建立，会话ID: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            // 解析客户端发送的消息
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            String conversationId = jsonNode.get("conversationId").asText();
            String userMessage = jsonNode.get("message").asText();
            boolean stream = jsonNode.has("stream") && jsonNode.get("stream").asBoolean(false);

            log.info("收到AI对话请求，会话ID: {}, 消息: {}, 流式: {}", conversationId, userMessage, stream);

            // 获取AiService实例
            if (aiService == null) {
                aiService = SpringContextUtil.getBean(AiService.class);
            }

            if (stream) {
                // 流式处理
                handleStreamMessage(session, conversationId, userMessage);
            } else {
                // 非流式处理
                handleNonStreamMessage(session, conversationId, userMessage);
            }

        } catch (Exception e) {
            log.error("处理AI对话消息时发生错误", e);
            trySendMessage(session, new TextMessage(objectMapper.writeValueAsString(
                    new AiResponse("error", "抱歉，处理您的消息时发生了错误: " + e.getMessage())
            )));
        }
    }

    /**
     * 处理非流式消息
     */
    private void handleNonStreamMessage(WebSocketSession session, String conversationId, String userMessage) throws Exception {
        // 调用AI服务获取回复
        String aiResponse = aiService.sendMessageWithMemory(conversationId, userMessage);

        // 构造回复消息
        String responseJson = objectMapper.writeValueAsString(new AiResponse("complete", aiResponse));

        // 发送回复给客户端
        trySendMessage(session, new TextMessage(responseJson));
    }

    /**
     * 处理流式消息
     */
    private void handleStreamMessage(WebSocketSession session, String conversationId, String userMessage) throws Exception {
        String sessionId = session.getId();
        AtomicBoolean cancelled = sessionCancelled.get(sessionId);

        // 重置取消状态
        cancelled.set(false);

        // 调用AI服务获取流式回复
        Flux<ChatResponse> responseFlux =
                aiService.streamMessageWithMemory(conversationId, userMessage);

        responseFlux
                .subscribeOn(Schedulers.boundedElastic())
                .doOnComplete(() -> {
                    try {
                        if (!cancelled.get() && session.isOpen()) {
                            trySendMessage(session, new TextMessage(objectMapper.writeValueAsString(
                                    new AiResponse("done", "[DONE]")
                            )));
                        }
                    } catch (Exception e) {
                        log.error("发送完成消息时出错", e);
                    }
                })
                .doOnError(throwable -> {
                    try {
                        if (!cancelled.get() && session.isOpen()) {
                            log.error("流式处理出错", throwable);
                            trySendMessage(session, new TextMessage(objectMapper.writeValueAsString(
                                    new AiResponse("error", "错误: " + throwable.getMessage())
                            )));
                        }
                    } catch (Exception e) {
                        log.error("发送错误消息时出错", e);
                    }
                })
                .subscribe(chatResponse -> {
                    try {
                        if (!cancelled.get() && session.isOpen()) {
                            String content = chatResponse.getResult().getOutput().getContent();
                            if (!content.isEmpty()) {
                                trySendMessage(session, new TextMessage(objectMapper.writeValueAsString(
                                        new AiResponse("stream", content)
                                )));
                            }
                        }
                    } catch (Exception e) {
                        log.error("发送流式消息时出错", e);
                    }
                });
    }

    /**
     * 安全地发送消息，避免ClosedChannelException
     * @param session WebSocket会话
     * @param message 要发送的消息
     */
    private void trySendMessage(WebSocketSession session, TextMessage message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(message);
            } else {
                log.debug("WebSocket会话已关闭，无法发送消息，会话ID: {}", session.getId());
            }
        } catch (ClosedChannelException e) {
            log.warn("WebSocket连接已关闭，无法发送消息，会话ID: {}", session.getId());
            // 从会话列表中移除已关闭的连接
            removeSession(session);
        } catch (IOException e) {
            log.error("发送WebSocket消息时发生IO异常，会话ID: {}", session.getId(), e);
            // 从会话列表中移除出错的连接
            removeSession(session);
        } catch (Exception e) {
            log.error("发送WebSocket消息时发生未知异常，会话ID: {}", session.getId(), e);
        }
    }

    /**
     * 从会话列表中移除指定会话
     * @param session WebSocket会话
     */
    private void removeSession(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        AtomicBoolean cancelled = sessionCancelled.remove(sessionId);
        if (cancelled != null) {
            cancelled.set(true);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        AtomicBoolean cancelled = sessionCancelled.remove(sessionId);
        if (cancelled != null) {
            cancelled.set(true);
        }
        log.info("AI WebSocket连接已关闭，会话ID: {}, 状态: {}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        if (exception instanceof ClosedChannelException) {
            log.warn("AI WebSocket传输错误（连接已关闭），会话ID: {}", sessionId);
        } else {
            log.error("AI WebSocket传输错误，会话ID: {}", sessionId, exception);
        }
        sessions.remove(sessionId);
        AtomicBoolean cancelled = sessionCancelled.get(sessionId);
        if (cancelled != null) {
            cancelled.set(true);
        }
    }

    /**
     * AI回复消息类
     */
    private static class AiResponse {
        private String type;
        private String message;

        public AiResponse() {}

        public AiResponse(String message) {
            this.type = "complete";
            this.message = message;
        }

        public AiResponse(String type, String message) {
            this.type = type;
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
