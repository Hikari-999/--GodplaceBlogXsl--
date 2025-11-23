package org.example.config;

import org.example.handler.AiWebSocketHandler;
import org.example.handler.OnlineUserWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器，处理 /ws/online 路径的连接
        registry.addHandler(new OnlineUserWebSocketHandler(), "/ws/online")
                .setAllowedOrigins("*"); // 允许跨域

        // 注册AI对话WebSocket处理器
        registry.addHandler(new AiWebSocketHandler(), "/api/ws/ai")
                .setAllowedOrigins("*"); // 允许跨域
    }
}
