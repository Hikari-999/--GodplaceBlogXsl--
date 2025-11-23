package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.dto.Message;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
