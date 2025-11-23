package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.example.entity.BaseData;

import java.time.LocalDateTime;

@Data
@TableName("db_notification")
public class Notification implements BaseData {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String versionId;
    private String title;
    private String content;
    private Integer readNum;
    private String adminName;
    private LocalDateTime createTime;
}
