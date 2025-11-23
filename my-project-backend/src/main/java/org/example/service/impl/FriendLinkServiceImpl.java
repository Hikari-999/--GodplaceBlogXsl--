package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.dto.FriendLink;
import org.example.mapper.FriendLinkMapper;
import org.example.service.FriendLinkService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FriendLinkServiceImpl extends ServiceImpl<FriendLinkMapper, FriendLink> implements FriendLinkService {

    @Override
    public List<FriendLink> getAllFriendLinks() {
        return this
                .lambdaQuery()
                .eq(FriendLink::getIsDefault, 1)
                .list();
    }

    @Override
    public List<FriendLink> getAllFriendLinksByAdmin() {
        return this
                .list();
    }

    @Override
    public boolean addFriendLink(FriendLink friendLink) {
        friendLink.setIsDefault(0);
        friendLink.setCreateTime(LocalDateTime.now());
        return this.save(friendLink);
    }

    @Override
    public boolean updateFriendLink(Integer id, FriendLink friendLink) {
        friendLink.setId(id);
        return this.updateById(friendLink);
    }

    @Override
    public boolean deleteFriendLink(Integer id) {
        return this.removeById(id);
    }
}
