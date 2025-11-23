package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("db_talk_comment")
public class TalkComment {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer talkId;

    private String content;

    private Integer userId;

    private String username;

    private LocalDateTime createTime;
}
