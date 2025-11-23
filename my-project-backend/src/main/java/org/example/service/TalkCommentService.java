package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.TalkComment;

import java.util.List;

public interface TalkCommentService extends IService<TalkComment> {
    List<TalkComment> getCommentsByTalkId(Integer talkId);

    void addComment(TalkComment comment);

    void deleteComment(Integer id);

    int countCommentsByTalkId(Integer talkId);
}
