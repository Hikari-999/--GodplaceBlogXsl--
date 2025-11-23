package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.dto.WebsiteTask;
import org.example.entity.vo.request.WebsiteTaskVO;
import org.example.entity.vo.response.WebsiteTaskByAdminVO;
import org.example.entity.vo.response.WebsiteTaskByUserVO;
import org.example.mapper.WebsiteTaskMapper;
import org.example.service.WebsiteTaskService;
import org.example.utils.Const;
import org.example.utils.FlowUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WebsiteTaskServiceImpl extends ServiceImpl<WebsiteTaskMapper, WebsiteTask> implements WebsiteTaskService {
    @Resource
    private WebsiteTaskMapper websiteTaskMapper;

    @Resource
    private FlowUtils flowUtils;

    //用户发送网站开发建议冷却时间限制，秒为单位
    @Value("${spring.web.verify.web-task}")
    int verifyLimit;
    @Override
    public List<WebsiteTaskByAdminVO> getTask() {
        // 构建查询条件：title 不为 null
        List<WebsiteTask> tasks = websiteTaskMapper.selectList(new LambdaQueryWrapper<WebsiteTask>()
                .isNotNull(WebsiteTask::getTitle)
                .isNotNull(WebsiteTask::getDescription)
                .eq(WebsiteTask::getDelFlag, 0)
                .orderByDesc(WebsiteTask::getCreateTime)
                .last("LIMIT 100"));

        // 日志记录：查询到的任务数量
        log.info("查询到 {} 条任务数据", tasks.size());

        // 如果查询结果为空，返回空列表
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("开始将任务数据映射为VO对象");


        List<WebsiteTaskByAdminVO> voList = tasks.stream()
                .map(task -> task.asViewObject(WebsiteTaskByAdminVO.class))
                .collect(Collectors.toList());

        log.info("转换后的VO数据: {}", voList.toString());
        return voList;
    }

    @Override
    public void setTask(WebsiteTaskVO vo) {
        WebsiteTask task = new WebsiteTask();
        BeanUtils.copyProperties(vo, task);
        task.setCreateTime(LocalDateTime.now()); // 设置当前时间戳到 create_time 字段
        task.setDelFlag(0);
        // 如果 vo 中的 username 为空，则设置默认值为 "游客+4位随机数"
        String name;
        if (vo.getUsername() == null || vo.getUsername().isEmpty()) {
            name = "游客" + setRandomNumber();
        } else {
            name = vo.getUsername();
        }
        task.setUsername(name);
        websiteTaskMapper.insert(task);
    }

    @Override
    public List<WebsiteTaskByUserVO> getTaskByUser() {
        // 构建查询条件：title 不为 null
        List<WebsiteTask> tasks = websiteTaskMapper.selectList(new LambdaQueryWrapper<WebsiteTask>()
                .isNotNull(WebsiteTask::getTitleByuser)
                .isNotNull(WebsiteTask::getDescriptionByuser)
                .eq(WebsiteTask::getDelFlag, 0)
                .orderByDesc(WebsiteTask::getCreateTimeByuser)
                .last("LIMIT 100"));

        // 日志记录：查询到的任务数量
        log.info("查询到 {} 条任务数据", tasks.size());

        // 如果查询结果为空，返回空列表
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("开始将任务数据映射为VO对象");


        List<WebsiteTaskByUserVO> voList = tasks.stream()
                .map(task -> task.asViewObject(WebsiteTaskByUserVO.class))
                .collect(Collectors.toList());

        log.info("转换后的VO数据: {}", voList);
        return voList;
    }

    //针对ip地址来对邮件验证码进行限流操作
    private boolean verifyLimit(String ip) {
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return flowUtils.limitOnceCheck(key,verifyLimit);
    }
    @Override
    public void setTaskByUser(WebsiteTaskVO vo, String ip) {
        synchronized (ip.intern()) {//流量锁 防止同一时间内大量请求同时访问该接口
            if (!this.verifyLimit(ip)) {  // 如果限流检查不通过
                throw new RuntimeException("请求频率限制为60s，再等一等");
            }

            WebsiteTask task = new WebsiteTask();
            BeanUtils.copyProperties(vo, task);
            task.setCreateTimeByuser(LocalDateTime.now()); // 设置当前时间戳到 create_time 字段
            task.setDelFlag(0);
            task.setStatus(0);
            // 如果 vo 中的 username 为空，则设置默认值为 "游客+4位随机数"
            String name;
            if (vo.getUsername() == null || vo.getUsername().isEmpty()) {
                name = "游客" + setRandomNumber();
            } else {
                name = vo.getUsername();
            }
            task.setUsername(name);
            websiteTaskMapper.insert(task);
        }
    }
    @Override
    public void updateTask(WebsiteTaskVO vo) {
        WebsiteTask task = new WebsiteTask();
        BeanUtils.copyProperties(vo, task);
        task.setCreateTime(LocalDateTime.now());

        // 如果 vo 中的 username 为空，则设置默认值为 "游客+4位随机数"
        String name;
        if (vo.getUsername() == null || vo.getUsername().isEmpty()) {
            name = "游客" + setRandomNumber();
        } else {
            name = vo.getUsername();
        }
        task.setUsername(name);

        // 根据ID更新任务
        websiteTaskMapper.updateById(task);
    }

    @Override
    public void deleteTask(Integer id) {
        WebsiteTask task = new WebsiteTask();
        task.setId(id);
        task.setDelFlag(1); // 软删除，设置删除标志为1
        websiteTaskMapper.updateById(task);
    }

    public Integer setRandomNumber() {
        Random random = new Random();
        // 生成 1000~9999 的随机数
        return 1000 + random.nextInt(9000);
    }
}
