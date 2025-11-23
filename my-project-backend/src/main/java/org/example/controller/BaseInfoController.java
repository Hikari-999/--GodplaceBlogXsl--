package org.example.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.RestBean;
import org.example.entity.vo.response.BaseInfoVO;
import org.example.service.BaseInfoService;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/base-info")
public class BaseInfoController {

    @Resource
    private BaseInfoService baseInfoService;

    /**
     * 获取网站基础信息
     * @return BaseInfoVO 基础信息
     */
    @GetMapping("/get-info")
    public RestBean<BaseInfoVO> getBaseInfo() {
        try {
            BaseInfoVO baseInfoVO = baseInfoService.getBaseInfo();
            return RestBean.success(baseInfoVO);
        } catch (Exception e) {
            log.error("获取基础信息失败", e);
            return RestBean.failure(500, "获取基础信息失败");
        }
    }

    /**
     * 增加访问量（当有用户访问网站时调用）
     * @return RestBean
     */
    @PostMapping("/increment-visits")
    public RestBean<Void> incrementVisits() {
        try {
            baseInfoService.incrementTotalVisits();
            return RestBean.success();
        } catch (Exception e) {
            log.error("增加访问量失败", e);
            return RestBean.failure(500, "增加访问量失败");
        }
    }

    /**
     * 获取当前在线人数
     * @return 当前在线人数
     */
    @GetMapping("/current-online")
    public RestBean<Integer> getCurrentOnline() {
        try {
            Integer currentOnline = baseInfoService.getCurrentOnline();
            return RestBean.success(currentOnline);
        } catch (Exception e) {
            log.error("获取当前在线人数失败", e);
            return RestBean.failure(500, "获取当前在线人数失败");
        }
    }
}
