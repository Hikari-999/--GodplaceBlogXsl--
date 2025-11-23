package org.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.entity.RestBean;
import org.example.entity.dto.Account;
import org.example.entity.dto.TalkComment;
import org.example.entity.vo.request.TalkCommentVO;
import org.example.entity.vo.request.TalkVO;
import org.example.service.TalkCommentService;
import org.example.service.TalkService;
import org.example.service.AccountService;
import org.example.service.impl.AccountIServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/talk")
public class TalkController {

    @Resource
    private TalkService talkService;

    @Resource
    private TalkCommentService talkCommentService;

    @Resource
    private AccountService accountService;

    @Resource
    AccountIServiceImpl service;
    @GetMapping("/list")
    public RestBean<List<TalkVO>> getTalkList() {
        return RestBean.success(talkService.getTalkList());
    }

    @GetMapping("/page")
    public RestBean<Page<TalkVO>> getTalkPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return RestBean.success(talkService.getTalkPage(pageNum, pageSize));
    }

    @PostMapping("/publish")
    public RestBean<Void> publishTalk(@RequestBody TalkVO talkVO) {
        talkService.publishTalk(talkVO);
        return RestBean.success();
    }

    @PutMapping("/update")
    public RestBean<Void> updateTalk(@RequestBody TalkVO talkVO) {
        talkService.updateTalk(talkVO);
        return RestBean.success();
    }

    @DeleteMapping("/{id}")
    public RestBean<Void> deleteTalk(@PathVariable Integer id) {
        talkService.deleteTalk(id);
        return RestBean.success();
    }

    @PostMapping("/upload-images")
    public RestBean<List<String>> uploadImages(@RequestParam("images") MultipartFile[] images) {
        try {
            List<String> imagePaths = new ArrayList<>();
            // 获取当前日期作为文件夹名
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String uploadDir = System.getProperty("user.dir") + "/uploads/talks/" + datePath;

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String originalFilename = image.getOriginalFilename();
                    String fileExtension = Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf("."));
                    String newFilename = UUID.randomUUID() + fileExtension;
                    String filePath = uploadDir + "/" + newFilename;

                    image.transferTo(new File(filePath));
                    // 保存相对路径到数据库
                    String relativePath = "/uploads/talks/" + datePath + "/" + newFilename;
                    imagePaths.add(relativePath);
                }
            }

            return RestBean.success(imagePaths);
        } catch (IOException e) {
            return RestBean.failure(400, "图片上传失败");
        }
    }

    // 获取随笔评论列表
    @GetMapping("/{talkId}/comments")
    public RestBean<List<TalkCommentVO>> getTalkComments(@PathVariable Integer talkId) {
        List<TalkComment> comments = talkCommentService.getCommentsByTalkId(talkId);
        List<TalkCommentVO> commentVOs = new ArrayList<>();
        for (TalkComment comment : comments) {
            TalkCommentVO vo = new TalkCommentVO();
            vo.setId(comment.getId());
            vo.setTalkId(comment.getTalkId());
            vo.setContent(comment.getContent());
            vo.setUserId(comment.getUserId());
            vo.setUsername(comment.getUsername());
            vo.setCreateTime(comment.getCreateTime());
            commentVOs.add(vo);
        }
        return RestBean.success(commentVOs);
    }

    // 添加评论
    @PostMapping("/{talkId}/comment")
    public RestBean<Void> addComment(@PathVariable Integer talkId, @RequestBody TalkCommentVO commentVO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 检查用户是否已认证
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return RestBean.failure(401, "用户未登录");
        }

        Object principal = authentication.getPrincipal();

        String username;
        if (principal instanceof User) {
            username = ((User) principal).getUsername();
        } else {
            username = principal.toString();
        }

        Account account = service.findAccountByNameOrEmail(username);

        if (account == null) {
            return RestBean.failure(400, "用户不存在");
        }

        TalkComment comment = new TalkComment();
        comment.setTalkId(talkId);
        comment.setContent(commentVO.getContent());
        comment.setUserId(account.getId()); // 使用数据库中的用户ID
        comment.setUsername(account.getUsername()); // 使用认证用户的用户名
        comment.setCreateTime(new Date().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        talkCommentService.addComment(comment);
        return RestBean.success();
    }


    // 删除评论
    @DeleteMapping("/comment/{commentId}")
    public RestBean<Void> deleteComment(@PathVariable Integer commentId) {
        // 获取当前认证用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 检查用户是否已认证
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return RestBean.failure(401, "用户未登录");
        }

        Object principal = authentication.getPrincipal();

        String username;
        if (principal instanceof User) {
            username = ((User) principal).getUsername();
        } else if (principal instanceof String) {
            // 如果principal是字符串，检查是否为anonymousUser
            if ("anonymousUser".equals(principal)) {
                return RestBean.failure(401, "用户未登录");
            }
            username = (String) principal;
        } else {
            return RestBean.failure(401, "用户身份信息异常");
        }

        // 检查是否有权限删除评论（评论作者或管理员）
        TalkComment comment = talkCommentService.getById(commentId);
        if (comment != null) {
            Account account = service.findAccountByNameOrEmail(username);
            if (account == null) {
                return RestBean.failure(400, "用户不存在");
            }

            if (comment.getUsername().equals(username) || "admin".equals(account.getRole())) {
                talkCommentService.deleteComment(commentId);
                return RestBean.success();
            } else {
                return RestBean.failure(403, "无权限删除该评论");
            }
        } else {
            return RestBean.failure(404, "评论不存在");
        }
    }

}
