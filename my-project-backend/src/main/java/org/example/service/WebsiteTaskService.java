package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.WebsiteTask;
import org.example.entity.vo.request.WebsiteTaskVO;
import org.example.entity.vo.response.WebsiteTaskByAdminVO;
import org.example.entity.vo.response.WebsiteTaskByUserVO;

import java.util.List;


public interface WebsiteTaskService extends IService<WebsiteTask> {
    //管理员获取任务接口
    List<WebsiteTaskByAdminVO> getTask();
    //管理员新增任务接口
    void setTask(WebsiteTaskVO vo);
    //游客获取任务接口
    List<WebsiteTaskByUserVO> getTaskByUser();
    //游客新增任务接口
    void setTaskByUser(WebsiteTaskVO vo, String ip);


    // 管理员更新任务接口
    void updateTask(WebsiteTaskVO vo);

    // 管理员删除任务接口
    void deleteTask(Integer id);
}
