package org.example.controller;

import jakarta.annotation.Resource;
import org.example.entity.RestBean;
import org.example.entity.dto.Message;
import org.example.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    @GetMapping("/list")
    public RestBean<List<Message>> getAllMessages() {
        List<Message> messages = messageService.getAllMessages();
        return RestBean.success(messages);
    }

    @PostMapping("/add")
    public RestBean<Void> addMessage(@RequestBody Message message) {
        messageService.addMessage(message);
        return RestBean.success();
    }

    @PostMapping("/delete/{id}")
    public RestBean<Void> deleteMessage(@PathVariable Integer id) {
        messageService.deleteMessageById(id);
        return RestBean.success();
    }
}
