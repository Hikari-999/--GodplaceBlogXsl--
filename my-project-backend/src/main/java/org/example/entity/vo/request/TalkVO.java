package org.example.entity.vo.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TalkVO {
    private Integer id;

    private String content;

    private String images;

    private List<String> imageList;

    private Integer userId;

    private LocalDateTime createTime;

    private Integer likes;

    private Integer comments;

    private Integer views;

    private String username;

    private String avatar;

    private List<String> tags;

    private List<TalkCommentVO> commentList;
}
