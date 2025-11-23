package org.example.service;

import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * AI服务接口
 */
public interface AiService {

    /**
     * 发送消息给AI并获取回复
     * @param message 用户消息
     * @return AI回复
     */
    String sendMessage(String message);

    /**
     * 根据提示生成文章
     * @param prompt 提示词
     * @return 生成的文章内容
     */
    String generateArticle(String prompt);
    /**
     * 发送消息给AI并获取回复(带会话ID)
     * @param conversationId 会话ID
     * @param message 用户消息
     * @return AI回复
     */
    String sendMessageWithMemory(String conversationId, String message);

    /**
     * 发送消息并流式获取回复
     * @param message 用户消息
     * @return 流式回复
     */
    Flux<ChatResponse> streamMessage(String message);

    /**
     * 发送带记忆功能的消息并流式获取回复
     * @param conversationId 会话ID
     * @param message 用户消息
     * @return 流式回复
     */
    Flux<ChatResponse> streamMessageWithMemory(String conversationId, String message);
}

