package org.example.entity.vo.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleBaseInfoVO {
    private Integer articleId;
    private LocalDateTime startTime;
    private LocalDateTime updateTime;
    private String title;
    private String description;
    private Integer star;
    private Integer readNum;
    private LocalDateTime totalReadTime;
    private List<String> tags;
    private String role;
}
