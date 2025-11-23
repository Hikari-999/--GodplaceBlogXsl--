package org.example.entity.vo.request;

import lombok.Data;

@Data
public class NotificationModifyVO {
    private String versionId;
    private String title;
    private String content;
    private String adminName;
    private String role;
}
