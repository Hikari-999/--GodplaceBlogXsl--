package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.dto.Account;
import org.example.entity.dto.WebsiteTask;

@Mapper
public interface WebsiteTaskMapper extends BaseMapper<WebsiteTask> {
}