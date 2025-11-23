package org.example.service;


import org.example.entity.dto.FriendLink;

import java.util.List;

public interface FriendLinkService {
    /**
     * 获取所有友情链接
     * @return 友情链接列表
     */
    List<FriendLink> getAllFriendLinks();
    /**
     * 获取所有友情链接
     * @return 友情链接列表
     */
    List<FriendLink>getAllFriendLinksByAdmin();
    /**
     * 添加友情链接
     * @param friendLink 友情链接信息
     * @return 是否添加成功
     */
    boolean addFriendLink(FriendLink friendLink);

    /**
     * 更新友情链接
     * @param id 友情链接ID
     * @param friendLink 友情链接信息
     * @return 是否更新成功
     */
    boolean updateFriendLink(Integer id, FriendLink friendLink);

    /**
     * 删除友情链接
     * @param id 友情链接ID
     * @return 是否删除成功
     */
    boolean deleteFriendLink(Integer id);
}
