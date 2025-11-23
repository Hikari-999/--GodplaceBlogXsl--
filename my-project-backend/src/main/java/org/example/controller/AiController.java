package org.example.controller;

import jakarta.annotation.Resource;
import org.example.entity.RestBean;
import org.example.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Resource
    private AiService aiService;

    @PostMapping("/chat")
    public RestBean<String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return RestBean.failure(400, "消息不能为空");
        }
        try {
            String response = aiService.sendMessage(message);
            return RestBean.success(response);
        } catch (Exception e) {
            return RestBean.failure(500, "AI服务调用失败: " + e.getMessage());
        }
    }
    @PostMapping("/chat-with-memory")
    public RestBean<String> chatWithMemory(@RequestBody Map<String, String> request) {
        String conversationId = request.get("conversationId");
        String message = request.get("message");

        if (conversationId == null || conversationId.trim().isEmpty()) {
            return RestBean.failure(400, "会话ID不能为空");
        }

        if (message == null || message.trim().isEmpty()) {
            return RestBean.failure(400, "消息不能为空");
        }

        try {
            String response = aiService.sendMessageWithMemory(conversationId, message);
            return RestBean.success(response);
        } catch (Exception e) {
            return RestBean.failure(500, "AI服务调用失败: " + e.getMessage());
        }
    }

    @PostMapping("/generate-article")
    public RestBean<String> generateArticle(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            return RestBean.failure(400, "提示词不能为空");
        }
        try {
            String article = aiService.generateArticle(prompt);
            return RestBean.success(article);
        } catch (Exception e) {
            return RestBean.failure(500, "文章生成失败: " + e.getMessage());
        }
    }

}
