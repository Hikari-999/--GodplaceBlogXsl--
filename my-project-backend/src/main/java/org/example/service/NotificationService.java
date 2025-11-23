package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.Notification;
import org.example.entity.vo.request.NotificationModifyVO;
import org.example.entity.vo.response.NotificationVO;

import java.util.List;

public interface NotificationService extends IService<Notification>{
    List<NotificationVO> getNotification();

    void modify(NotificationModifyVO vo);

    // 添加更新公告接口
    void updateNotification(Integer id, NotificationModifyVO vo);

    // 添加删除公告接口
    void deleteNotification(Integer id);
}
