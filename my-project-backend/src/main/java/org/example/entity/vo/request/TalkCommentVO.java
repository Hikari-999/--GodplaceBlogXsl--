package org.example.entity.vo.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TalkCommentVO {
    private Integer id;

    private Integer talkId;

    private String content;

    private Integer userId;

    private String username;

    private LocalDateTime createTime;
}
