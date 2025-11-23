package org.example.entity.vo.request;

import lombok.Data;

import java.util.List;

@Data
public class ArticleBaseInfoRequestVO {
    private Integer articleId;
    private String title;
    private String description;
    private List<String> tags;
    private String role; // 用于权限验证
}

