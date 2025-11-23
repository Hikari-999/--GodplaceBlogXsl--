package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.dto.Message;
import org.example.mapper.MessageMapper;
import org.example.service.MessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public List<Message> getAllMessages() {
        return this.list();
    }

    @Override
    public void addMessage(Message message) {
        message.setCreateTime(LocalDateTime.now());
        this.save(message);
    }

    @Override
    public void deleteMessageById(Integer id) {
        this.removeById(id);
    }
}
