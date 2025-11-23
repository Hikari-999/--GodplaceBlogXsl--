package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.dto.TalkComment;
import org.example.mapper.TalkCommentMapper;
import org.example.service.TalkCommentService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class TalkCommentServiceImpl extends ServiceImpl<TalkCommentMapper, TalkComment> implements TalkCommentService {

    @Resource
    private TalkCommentMapper talkCommentMapper;

    @Override
    public List<TalkComment> getCommentsByTalkId(Integer talkId) {
        return talkCommentMapper.selectList(
                new QueryWrapper<TalkComment>()
                        .eq("talk_id", talkId)
                        .orderByAsc("create_time")
        );
    }

    @Override
    public void addComment(TalkComment comment) {
        talkCommentMapper.insert(comment);
    }

    @Override
    public void deleteComment(Integer id) {
        talkCommentMapper.deleteById(id);
    }

    @Override
    public int countCommentsByTalkId(Integer talkId) {
        return Math.toIntExact(talkCommentMapper.selectCount(
                new QueryWrapper<TalkComment>()
                        .eq("talk_id", talkId)
        ));
    }
}
