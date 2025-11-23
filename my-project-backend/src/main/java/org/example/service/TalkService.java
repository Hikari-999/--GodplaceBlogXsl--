package org.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.Talk;
import org.example.entity.vo.request.TalkVO;


import java.util.List;

public interface TalkService extends IService<Talk> {
    List<TalkVO> getTalkList();

    Page<TalkVO> getTalkPage(Integer pageNum, Integer pageSize);

    void publishTalk(TalkVO talkVO);

    void deleteTalk(Integer id);

    void updateTalk(TalkVO talkVO);
}
