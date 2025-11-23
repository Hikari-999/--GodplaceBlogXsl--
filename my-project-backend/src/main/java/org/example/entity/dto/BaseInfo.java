package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.example.entity.BaseData;

import java.time.LocalDateTime;

@Data
@TableName("db_baseinfo")
public class BaseInfo implements BaseData {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 总访问量
     */
    private Integer totalVisits;

    /**
     * 当前在线人数
     */
    private Integer currentOnline;

    /**
     * 网站启动时间
     */
    private LocalDateTime startRuntime;

    /**
     * 网站更新时间
     */
    private LocalDateTime updateRuntime;

    /**
     * 文章总数
     */
    private Integer totalArticles;
}
