package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.example.entity.BaseData;

import java.time.LocalDateTime;

@Data
@TableName("db_article_baseinfo")
public class ArticleBaseInfo implements BaseData {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer articleId;

    private String title;

    private LocalDateTime startTime;

    private LocalDateTime updateTime;

    private String description;

    private Integer star;

    private Integer readNum;

    private LocalDateTime totalReadTime;

    private Integer delFlag;

}
