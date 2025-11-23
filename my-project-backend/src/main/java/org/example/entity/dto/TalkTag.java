package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("db_talk_tag")
public class TalkTag {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer talkId;

    private Integer tagId;
}
