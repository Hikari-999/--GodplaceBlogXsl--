package org.example.entity.vo.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Integer id;
    private String versionId;
    private String title;
    private String content;
    private Integer readNum;
    private String adminName;
    private LocalDateTime createTime;
}
