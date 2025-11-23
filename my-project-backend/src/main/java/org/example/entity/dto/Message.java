package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("db_messages")
public class Message {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String content;

    private String author;

    private LocalDateTime createTime;
}
