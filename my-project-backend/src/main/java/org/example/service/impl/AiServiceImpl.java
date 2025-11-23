package org.example.service.impl;

import org.example.service.AiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;

    // 定义系统提示词
    private static final String SYSTEM_PROMPT = """
        你是一个个人博客网站的AI智能客服，你的主要任务是帮助用户解答关于博客网站的问题。
        
        博客网站功能包括：
        1. 版本信息迭代界面 - 用户可以查看网站的更新日志和版本信息
        2. 博客文章页面 - 用户可以浏览、阅读博客文章，文章有标签分类
        3. 待开发内容 - 网站还在持续开发中，有任务列表展示未来功能规划
        
        网站技术栈：
        - 前端：Vue3 + Element Plus
        - 后端：Spring Boot
        - 数据库相关操作使用MyBatis Plus
        - AI功能集成
        
        回答用户问题时，请遵循以下原则：
        1. 准确性：基于博客网站的实际功能进行回答
        2. 友好性：语气亲切，乐于助人
        3. 清晰性：回答简洁明了，避免使用过于专业的术语
        4. 引导性：对于复杂操作，提供清晰的步骤指导
        5. 诚实性：对于待开发功能，明确告知用户当前状态
        
        如果用户询问的问题与博客网站无关，请礼貌地将话题引导回博客网站相关的内容。
        """;

    @Autowired
    public AiServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String sendMessage(String message) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .options(OpenAiChatOptions.builder()
                        .withModel("qwen-plus")
                        .build())
                .call()
                .content();
    }

    @Override
    public String generateArticle(String prompt) {
        String fullPrompt = "请根据以下提示生成一篇文章:\n\n" + prompt + "\n\n文章内容:";
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(fullPrompt)
                .options(OpenAiChatOptions.builder()
                        .withModel("qwen-plus")
                        .build())
                .call()
                .content();
    }

    @Override
    public String sendMessageWithMemory(String conversationId, String message) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .options(OpenAiChatOptions.builder()
                        .withModel("qwen-plus")
                        .build())
                .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .content();
    }

    @Override
    public Flux<ChatResponse> streamMessage(String message) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .options(OpenAiChatOptions.builder()
                        .withModel("qwen-plus")
                        .build())
                .stream().chatResponse();
    }

    @Override
    public Flux<ChatResponse> streamMessageWithMemory(String conversationId, String message) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .options(OpenAiChatOptions.builder()
                        .withModel("qwen-plus")
                        .build())
                .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream().chatResponse();
    }
}

