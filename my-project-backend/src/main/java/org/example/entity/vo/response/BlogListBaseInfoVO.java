package org.example.entity.vo.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BlogListBaseInfoVO {
    private Integer articleId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime updateTime;
    private String description;
    private Integer star;
    private Integer readNum;
    private LocalDateTime totalReadTime;
    private List<String> tags; // 添加标签字段
}
