package org.example.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.RestBean;
import org.example.entity.vo.request.NotificationModifyVO;
import org.example.entity.vo.response.NotificationVO;
import org.example.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@Slf4j
@RequestMapping("/notification")
public class NotificationController {
    @Resource
    private NotificationService notificationService;
    @GetMapping("/show")
    public RestBean<List<NotificationVO>> notification() {
        List<NotificationVO> list = notificationService.getNotification();
        return RestBean.success(list);
    }

    @PostMapping("/modify")
    public RestBean<Void> modify(@RequestBody NotificationModifyVO vo) {
        if (Objects.equals(vo.getRole(), "admin")){
        notificationService.modify(vo);
        return RestBean.success();}
        else return RestBean.failure(403, "无权限");
    }
    @PostMapping("/update")
    public RestBean<Void> update(@RequestParam Integer id, @RequestBody NotificationModifyVO vo) {
        if (Objects.equals(vo.getRole(), "admin")) {
            notificationService.updateNotification(id, vo);
            return RestBean.success();
        } else {
            return RestBean.failure(403, "无权限");
        }
    }

    @PostMapping("/delete")
    public RestBean<Void> delete(@RequestParam Integer id, @RequestParam String role) {
        if (Objects.equals(role, "admin")) {
            notificationService.deleteNotification(id);
            return RestBean.success();
        } else {
            return RestBean.failure(403, "无权限");
        }
    }
}
