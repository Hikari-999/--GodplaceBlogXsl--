package org.example.task;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.dto.BaseInfo;
import org.example.service.ArticleBaseInfoService;
import org.example.service.BaseInfoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 网站基础信息定时更新任务
 */
@Slf4j
@Component
public class BaseInfoUpdateTask {

    @Resource
    private BaseInfoService baseInfoService;

    @Resource
    private ArticleBaseInfoService articleBaseInfoService;

    /**
     * 每分钟更新一次网站基础信息
     * 更新内容包括：
     * 1. 更新update_time字段为当前时间
     * 2. 更新文章总数
     */
    @Scheduled(cron = "0 * * * * ?")
    public void updateBaseInfo() {
        try {
            log.info("开始执行定时任务：更新网站基础信息");

            // 获取当前基础信息（假设只有一条记录，ID为1）
            BaseInfo baseInfo = baseInfoService.getById(1);

            // 如果不存在基础信息记录，则创建一条
            if (baseInfo == null) {
                baseInfo = new BaseInfo();
                baseInfo.setId(1);
                baseInfo.setStartRuntime(LocalDateTime.now());
                baseInfo.setTotalVisits(0);
                baseInfo.setCurrentOnline(0);
                baseInfo.setTotalArticles(0);
                baseInfo.setUpdateRuntime(LocalDateTime.now());
                baseInfoService.save(baseInfo);
                log.info("创建新的基础信息记录");
            } else {
                // 更新update_time字段和文章总数
                Integer articleCount = articleBaseInfoService.getArticleCount();

                baseInfo.setUpdateRuntime(LocalDateTime.now());
                baseInfo.setTotalArticles(articleCount);

                baseInfoService.updateById(baseInfo);
                log.info("更新基础信息成功：更新时间={}，文章总数={}",
                        baseInfo.getUpdateRuntime(), baseInfo.getTotalArticles());
            }
        } catch (Exception e) {
            log.error("执行定时任务更新基础信息时发生错误", e);
        }
    }
}
