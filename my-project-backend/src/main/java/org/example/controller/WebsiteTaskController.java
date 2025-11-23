package org.example.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.RestBean;
import org.example.entity.vo.request.WebsiteTaskVO;
import org.example.entity.vo.response.WebsiteTaskByAdminVO;
import org.example.entity.vo.response.WebsiteTaskByUserVO;
import org.example.service.WebsiteTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/blog")
public class WebsiteTaskController {

    @Resource
    private WebsiteTaskService websiteTaskService;
   @PostMapping("/getTask")
    public RestBean<List<WebsiteTaskByAdminVO>> getTask() {
        return RestBean.success(websiteTaskService.getTask());
    }

   @PostMapping("/setTask")
    public RestBean<Void> setTask(@RequestBody WebsiteTaskVO vo) {
       if (Objects.equals(vo.getRole(), "admin")){
       websiteTaskService.setTask(vo);
       return RestBean.success();}
       else return RestBean.failure(403, "无权限");
    }

    @PostMapping("/setTaskByUser")
    public RestBean<Void> setTaskByUser(@RequestBody WebsiteTaskVO vo, HttpServletRequest request) {
        try {
            websiteTaskService.setTaskByUser(vo, request.getRemoteAddr());
            return RestBean.success();
        } catch (RuntimeException e) {
            return RestBean.failure(429, e.getMessage());
        }
    }

    @PostMapping("/getTaskByUser")
    public RestBean<List<WebsiteTaskByUserVO>> getTaskByUser() {
        return RestBean.success(websiteTaskService.getTaskByUser());
    }
    // 添加更新任务接口
    @PostMapping("/updateTask")
    public RestBean<Void> updateTask(@RequestBody WebsiteTaskVO vo) {
        if (Objects.equals(vo.getRole(), "admin")) {
            websiteTaskService.updateTask(vo);
            return RestBean.success();
        } else {
            return RestBean.failure(403, "无权限");
        }
    }

    // 添加删除任务接口
    @PostMapping("/deleteTask")
    public RestBean<Void> deleteTask(@RequestParam Integer id, @RequestParam String role) {
        if (Objects.equals(role, "admin")) {
            websiteTaskService.deleteTask(id);
            return RestBean.success();
        } else {
            return RestBean.failure(403, "无权限");
        }
    }
}

