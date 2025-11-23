package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.dto.BaseInfo;
import org.example.entity.vo.response.BaseInfoVO;
import org.example.mapper.BaseInfoMapper;
import org.example.service.BaseInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BaseInfoServiceImpl extends ServiceImpl<BaseInfoMapper, BaseInfo> implements BaseInfoService {
    @Override
    public BaseInfoVO getBaseInfo() {
        // 获取数据库中的基础信息记录（假设只有一条记录，id为1）
        BaseInfo baseInfo = this.getById(1);

        // 如果没有记录，则创建一条默认记录
        if (baseInfo == null) {
            baseInfo = new BaseInfo();
            baseInfo.setId(1);
            baseInfo.setStartRuntime(java.time.LocalDateTime.now());
            baseInfo.setTotalVisits(0);
            baseInfo.setCurrentOnline(1);
            baseInfo.setTotalArticles(0);
            baseInfo.setUpdateRuntime(java.time.LocalDateTime.now());
            this.save(baseInfo);
        }

        // 转换为VO对象
        BaseInfoVO baseInfoVO = new BaseInfoVO();
        BeanUtils.copyProperties(baseInfo, baseInfoVO);

        return baseInfoVO;
    }

    @Override
    public void incrementTotalVisits() {
        // 使用Lambda更新器增加总访问量
        this.lambdaUpdate()
                .setSql("total_visits = total_visits + 1")
                .update();
    }

    @Override
    public Integer getCurrentOnline() {
        BaseInfo baseInfo = this.getById(1);
        return baseInfo != null ? baseInfo.getCurrentOnline() : 0;
    }

    @Override
    public void updateCurrentOnline(Integer count) {
        this.lambdaUpdate()
                .eq(BaseInfo::getId, 1)
                .set(BaseInfo::getCurrentOnline, count)
                .update();
    }
}
