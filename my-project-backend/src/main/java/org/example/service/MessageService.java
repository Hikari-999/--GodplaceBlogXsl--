package org.example.service;

import org.example.entity.dto.Message;

import java.util.List;

public interface MessageService {
    List<Message> getAllMessages();

    void addMessage(Message message);

    void deleteMessageById(Integer id);
}

