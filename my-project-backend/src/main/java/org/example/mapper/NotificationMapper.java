package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.dto.Notification;
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
