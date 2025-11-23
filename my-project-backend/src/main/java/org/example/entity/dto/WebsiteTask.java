package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.entity.BaseData;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("db_tasks")
public class WebsiteTask implements BaseData {
    // 主键
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 开发任务标题
    private String title;

    // 开发任务描述
    private String description;

    // 开发任务状态 (0:为开始, 1:开发中, 2:已完成)
    private Integer status;

    // 游客提出的开发标题
    private String titleByuser;

    // 游客提出的开发描述
    private String descriptionByuser;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime createTimeByuser;

    // 删除标志 0未删除 1删除
    private Integer delFlag;

    // 用户名（外键关联 db_account 表）
    private String username;
}
