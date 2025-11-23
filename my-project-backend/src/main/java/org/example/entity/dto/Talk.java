package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("db_talk")
public class Talk {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String content;

    private String images; // 存储图片文件路径，用逗号分隔

    private Integer userId;

    private LocalDateTime createTime;

    private Integer likes;

    private Integer comments;

    private Integer views;
}
