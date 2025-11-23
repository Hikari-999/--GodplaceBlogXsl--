package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.entity.dto.Tag;
import org.example.entity.dto.Talk;
import org.example.entity.dto.TalkComment;
import org.example.entity.dto.TalkTag;
import org.example.entity.vo.request.TalkCommentVO;
import org.example.entity.vo.request.TalkVO;
import org.example.mapper.TagMapper;
import org.example.mapper.TalkCommentMapper;
import org.example.mapper.TalkMapper;
import org.example.mapper.TalkTagMapper;
import org.example.service.TalkService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Base64;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

@Service
public class TalkServiceImpl extends ServiceImpl<TalkMapper, Talk> implements TalkService {

    @Resource
    private TagMapper tagMapper;

    @Resource
    private TalkTagMapper talkTagMapper;
    @Resource
    private TalkCommentMapper talkCommentMapper;

    @Override
    public List<TalkVO> getTalkList() {
        List<Talk> talks = this.list(new QueryWrapper<Talk>().orderByDesc("create_time"));
        return talks.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public Page<TalkVO> getTalkPage(Integer pageNum, Integer pageSize) {
        Page<Talk> page = new Page<>(pageNum, pageSize);
        Page<Talk> talkPage = this.page(page, new QueryWrapper<Talk>().orderByDesc("create_time"));

        Page<TalkVO> voPage = new Page<>(talkPage.getCurrent(), talkPage.getSize(), talkPage.getTotal());
        List<TalkVO> talkVOList = talkPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(talkVOList);

        return voPage;
    }

    @Override
    @Transactional
    public void publishTalk(TalkVO talkVO) {
        Talk talk = new Talk();
        BeanUtils.copyProperties(talkVO, talk);
        talk.setCreateTime(LocalDateTime.now());
        talk.setLikes(0);
        talk.setComments(0);
        talk.setViews(0);

        // 处理图片列表 - 将图片路径列表转换为逗号分隔的字符串
        if (talkVO.getImageList() != null && !talkVO.getImageList().isEmpty()) {
            talk.setImages(String.join(",", talkVO.getImageList()));
        }

        this.save(talk);

        // 处理标签
        handleTalkTags(talk.getId(), talkVO.getTags());
    }

    @Override
    @Transactional
    public void updateTalk(TalkVO talkVO) {
        Talk talk = new Talk();
        BeanUtils.copyProperties(talkVO, talk);

        // 处理图片列表 - 将图片路径列表转换为逗号分隔的字符串
        if (talkVO.getImageList() != null && !talkVO.getImageList().isEmpty()) {
            talk.setImages(String.join(",", talkVO.getImageList()));
        } else {
            talk.setImages(null);
        }

        this.updateById(talk);

        // 处理标签
        handleTalkTags(talk.getId(), talkVO.getTags());
    }

    @Override
    @Transactional
    public void deleteTalk(Integer id) {
        // 先删除关联的标签记录
        talkTagMapper.delete(new QueryWrapper<TalkTag>().eq("talk_id", id));

        // 再删除随笔本身
        this.removeById(id);
    }

    /**
     * 压缩Base64图片数据
     * @param base64Image 原始Base64图片数据
     * @return 压缩后的Base64图片数据
     */
    private String compressImage(String base64Image) {
        if (!StringUtils.hasText(base64Image) || !base64Image.startsWith("data:image/")) {
            return base64Image;
        }

        try {
            // 提取图片格式
            String imageFormat = base64Image.substring(11, base64Image.indexOf(";"));
            if (!"jpeg".equals(imageFormat) && !"jpg".equals(imageFormat) && !"png".equals(imageFormat)) {
                return base64Image; // 不是支持的格式，不压缩
            }

            // 解码Base64数据
            String base64Data = base64Image.substring(base64Image.indexOf(",") + 1);
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // 读取图片
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage originalImage = ImageIO.read(bis);

            // 如果图片尺寸已经很小，不压缩
            if (originalImage.getWidth() <= 800 && originalImage.getHeight() <= 800) {
                return base64Image;
            }

            // 计算压缩后的尺寸，保持宽高比
            int targetWidth, targetHeight;
            double ratio = (double) originalImage.getWidth() / originalImage.getHeight();

            if (originalImage.getWidth() > originalImage.getHeight()) {
                targetWidth = Math.min(800, originalImage.getWidth());
                targetHeight = (int) (targetWidth / ratio);
            } else {
                targetHeight = Math.min(800, originalImage.getHeight());
                targetWidth = (int) (targetHeight * ratio);
            }

            // 创建压缩后的图片
            Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            BufferedImage compressedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = compressedImage.createGraphics();
            g.drawImage(scaledImage, 0, 0, null);
            g.dispose();

            // 转换为Base64
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(compressedImage, imageFormat, bos);
            byte[] compressedBytes = bos.toByteArray();
            String compressedBase64 = Base64.getEncoder().encodeToString(compressedBytes);

            return "data:image/" + imageFormat + ";base64," + compressedBase64;
        } catch (Exception e) {
            // 压缩失败，返回原始数据
            return base64Image;
        }
    }

    /**
     * 处理随笔标签关联
     * @param talkId 随笔ID
     * @param tags 标签列表
     */
    private void handleTalkTags(Integer talkId, List<String> tags) {
        if (tags != null && !tags.isEmpty()) {
            // 先删除原有的标签关联
            talkTagMapper.delete(new QueryWrapper<TalkTag>().eq("talk_id", talkId));

            // 添加新的标签关联
            for (String tagName : tags) {
                Tag tag = tagMapper.selectOne(new QueryWrapper<Tag>().eq("tag_name", tagName));
                if (tag == null) {
                    // 如果标签不存在，创建新标签
                    tag = new Tag();
                    tag.setTagName(tagName);
                    tag.setCreatedTime(LocalDateTime.now());
                    tagMapper.insert(tag);
                }

                // 创建随笔和标签的关联
                TalkTag talkTag = new TalkTag();
                talkTag.setTalkId(talkId);
                talkTag.setTagId(tag.getTagId());
                talkTagMapper.insert(talkTag);
            }
        } else {
            // 如果没有标签，删除所有关联
            talkTagMapper.delete(new QueryWrapper<TalkTag>().eq("talk_id", talkId));
        }
    }

    /**
     * 将Talk实体转换为TalkVO
     * @param talk Talk实体
     * @return TalkVO对象
     */
    private TalkVO convertToVO(Talk talk) {
        TalkVO vo = new TalkVO();
        BeanUtils.copyProperties(talk, vo);

        // 处理图片列表 - 将逗号分隔的图片路径转换为列表
        if (StringUtils.hasText(talk.getImages())) {
            String[] imageArray = talk.getImages().split(",");
            List<String> imageList = new ArrayList<>();
            for (String image : imageArray) {
                String trimmedImage = image.trim();
                if (StringUtils.hasText(trimmedImage)) {
                    imageList.add(trimmedImage);
                }
            }
            vo.setImageList(imageList);
        }

        // 获取标签信息
        List<Tag> tags = tagMapper.selectList(
                new QueryWrapper<Tag>()
                        .inSql("tag_id",
                                "SELECT tag_id FROM db_talk_tag WHERE talk_id = " + talk.getId())
        );

        List<String> tagNames = tags.stream()
                .map(Tag::getTagName)
                .collect(Collectors.toList());
        vo.setTags(tagNames);

        // 设置评论数
        vo.setComments(Math.toIntExact(talkCommentMapper.selectCount(
                new QueryWrapper<TalkComment>()
                        .eq("talk_id", talk.getId())
        )));

        // 获取评论信息
        List<TalkComment> comments = talkCommentMapper.selectList(
                new QueryWrapper<TalkComment>()
                        .eq("talk_id", talk.getId())
                        .orderByAsc("create_time")
        );

        List<TalkCommentVO> commentVOs = new ArrayList<>();
        for (TalkComment comment : comments) {
            TalkCommentVO commentVO = new TalkCommentVO();
            commentVO.setId(comment.getId());
            commentVO.setTalkId(comment.getTalkId());
            commentVO.setContent(comment.getContent());
            commentVO.setUserId(comment.getUserId());
            commentVO.setUsername(comment.getUsername());
            commentVO.setCreateTime(comment.getCreateTime());
            commentVOs.add(commentVO);
        }
        vo.setCommentList(commentVOs);

        return vo;
    }


}
