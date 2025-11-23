// TagMapper.java
package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.dto.Tag;
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
