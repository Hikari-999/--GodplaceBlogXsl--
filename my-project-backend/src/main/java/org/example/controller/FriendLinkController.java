package org.example.controller;

import jakarta.annotation.Resource;
import org.example.entity.RestBean;
import org.example.entity.dto.FriendLink;
import org.example.entity.vo.request.FriendLinkVO;
import org.example.service.FriendLinkService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/friend-link")
public class FriendLinkController {

    @Resource
    private FriendLinkService friendLinkService;

    /**
     * 获取所有友情链接
     */
    @GetMapping("/list")
    public RestBean<List<FriendLinkVO>> getAllFriendLinks() {
        List<FriendLink> friendLinks = friendLinkService.getAllFriendLinks();
        return RestBean.success(friendLinks.stream().map(FriendLink::toVO).toList());
    }

    @GetMapping("/list-by-admin")
    public RestBean<List<FriendLinkVO>> getAllFriendLinksByAdmin() {
        List<FriendLink> friendLinks = friendLinkService.getAllFriendLinksByAdmin();
        return RestBean.success(friendLinks.stream().map(FriendLink::toVO).toList());
    }

    /**
     * 添加友情链接（支持文件上传）
     */
    @PostMapping("/add")
    public RestBean<Void> addFriendLink(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile avatar) {

        try {
            FriendLink friendLink = new FriendLink();
            friendLink.setName(name);
            friendLink.setUrl(url);
            friendLink.setDescription(description);

            // 如果有上传头像文件，则转换为Base64存储
            if (avatar != null && !avatar.isEmpty()) {
                // 检查文件类型
                String contentType = avatar.getContentType();
                if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/jpg"))) {
                    return RestBean.failure(400, "只支持JPG/PNG格式的图片");
                }

                // 检查文件大小（限制为2MB）
                if (avatar.getSize() > 2 * 1024 * 1024) {
                    return RestBean.failure(400, "图片大小不能超过2MB");
                }

                // 将图片转换为Base64编码
                byte[] bytes = avatar.getBytes();
                String base64Encoded = Base64.getEncoder().encodeToString(bytes);

                // 构造data URL
                String dataUrl = "data:" + contentType + ";base64," + base64Encoded;
                friendLink.setAvatar(dataUrl);
            }

            if (friendLinkService.addFriendLink(friendLink)) {
                return RestBean.success();
            } else {
                return RestBean.failure(400, "友情链接添加失败");
            }
        } catch (Exception e) {
            return RestBean.failure(500, "添加友情链接失败: " + e.getMessage());
        }
    }

    /**
     * 更新友情链接
     */
    @PutMapping("/update/{id}")
    public RestBean<Void> updateFriendLink(@PathVariable Integer id, @RequestBody FriendLink friendLink) {
        if (friendLinkService.updateFriendLink(id, friendLink)) {
            return RestBean.success();
        } else {
            return RestBean.failure(400, "友情链接更新失败");
        }
    }

    /**
     * 删除友情链接
     */
    @DeleteMapping("/delete/{id}")
    public RestBean<Void> deleteFriendLink(@PathVariable Integer id) {
        if (friendLinkService.deleteFriendLink(id)) {
            return RestBean.success();
        } else {
            return RestBean.failure(400, "友情链接删除失败");
        }
    }
}
