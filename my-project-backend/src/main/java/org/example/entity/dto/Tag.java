// Tag.java
package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("db_tag")
public class Tag {
    @TableId(type = IdType.AUTO)
    private Integer tagId;

    private String tagName;

    private LocalDateTime createdTime;
}
