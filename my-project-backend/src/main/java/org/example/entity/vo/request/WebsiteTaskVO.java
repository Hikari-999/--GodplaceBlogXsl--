package org.example.entity.vo.request;

import lombok.Data;

@Data
public class WebsiteTaskVO {
    Integer id;
    String title;
    String description;
    Integer status;
    String titleByuser;
    String descriptionByuser;
    String username;
    String role;
}
