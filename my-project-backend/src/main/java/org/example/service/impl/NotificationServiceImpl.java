package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.dto.Notification;
import org.example.entity.vo.request.NotificationModifyVO;
import org.example.entity.vo.response.NotificationVO;
import org.example.mapper.NotificationMapper;
import org.example.service.NotificationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    @Override
    public List<NotificationVO> getNotification() {
        return this.list().stream()
                .map(notification -> notification.asViewObject(NotificationVO.class)).toList();
    }

    @Override
    public void modify(NotificationModifyVO vo) {
        Notification notification = new Notification();
        BeanUtils.copyProperties(vo, notification);
        notification.setCreateTime(LocalDateTime.now());
        this.save(notification);
    }

    @Override
    public void updateNotification(Integer id, NotificationModifyVO vo) {
        Notification notification = new Notification();
        BeanUtils.copyProperties(vo, notification);
        notification.setId(id);
        notification.setCreateTime(LocalDateTime.now());
        this.updateById(notification);
    }

    @Override
    public void deleteNotification(Integer id) {
        this.removeById(id);
    }
}
