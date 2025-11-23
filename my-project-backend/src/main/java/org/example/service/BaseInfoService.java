package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.BaseInfo;
import org.example.entity.vo.response.BaseInfoVO;

public interface BaseInfoService extends IService<BaseInfo> {
    /**
     * 获取网站基础信息
     * @return BaseInfoVO 基础信息视图对象
     */
    BaseInfoVO getBaseInfo();

    /**
     * 更新总访问量（增加1）
     */
    void incrementTotalVisits();

    /**
     * 获取当前在线人数
     * @return 当前在线人数
     */
    Integer getCurrentOnline();

    /**
     * 更新当前在线人数
     * @param count 在线人数
     */
    void updateCurrentOnline(Integer count);
}
