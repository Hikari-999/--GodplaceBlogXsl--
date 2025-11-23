package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.RestBean;
import org.example.entity.dto.ArticleBaseInfo;
import org.example.entity.dto.ArticleTag;
import org.example.entity.dto.Tag;
import org.example.entity.vo.request.ArticleBaseInfoRequestVO;
import org.example.entity.vo.response.ArticleBaseInfoVO;
import org.example.entity.vo.response.BlogListBaseInfoVO;
import org.example.mapper.ArticleBaseInfoMapper;
import org.example.mapper.ArticleTagMapper;
import org.example.mapper.TagMapper;
import org.example.service.ArticleBaseInfoService;
import org.example.service.TagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleBaseInfoServiceImpl extends ServiceImpl<ArticleBaseInfoMapper, ArticleBaseInfo> implements ArticleBaseInfoService {
    @Resource
    private TagService tagService;

    @Resource
    private ArticleTagMapper articleTagMapper;

    @Resource
    private TagMapper tagMapper;
    @Override
    public ArticleBaseInfoVO getArticleBaseInfoById(String id) {
        // 只查询 del_flag 为 0 的数据
        ArticleBaseInfo articleBaseInfo = this.lambdaQuery()
                .eq(ArticleBaseInfo::getId, id)
                .eq(ArticleBaseInfo::getDelFlag, 0)
                .one();

        if (articleBaseInfo == null) {
            return null;
        }

        ArticleBaseInfoVO articleBaseInfoVO = new ArticleBaseInfoVO();
        BeanUtils.copyProperties(articleBaseInfo, articleBaseInfoVO);

        // 获取文章标签信息
        Map<Integer, List<String>> articleTagsMap = tagService.getArticleTagsMap();
        List<String> tags = articleTagsMap.getOrDefault(articleBaseInfo.getArticleId(), List.of());
        articleBaseInfoVO.setTags(tags);

        return articleBaseInfoVO;
    }

    @Override
    public RestBean<Void> modifyReadOrStar(String type, Integer articleId) {
        if ("read".equals(type)){
            this.lambdaUpdate()
                    .eq(ArticleBaseInfo::getArticleId, articleId)
                    .setSql("read_num = read_num + 1")
                    .update();
        } else if ("star".equals(type)){
            this.lambdaUpdate()
                    .eq(ArticleBaseInfo::getArticleId, articleId)
                    .setSql("star = star + 1")
                    .update();
        }
        return RestBean.success();
    }

    @Override
    public List<BlogListBaseInfoVO> getArticleList() {
        // 获取所有文章基本信息
        List<ArticleBaseInfo> articleBaseInfoList = this.lambdaQuery()
                .eq(ArticleBaseInfo::getDelFlag, 0)
                .list();

        // 获取文章标签映射
        Map<Integer, List<String>> articleTagsMap = tagService.getArticleTagsMap();

        // 转换为VO对象并设置标签
        return articleBaseInfoList.stream()
                .map(articleBaseInfo -> {
                    BlogListBaseInfoVO vo = articleBaseInfo.asViewObject(BlogListBaseInfoVO.class);
                    vo.setTags(articleTagsMap.getOrDefault(articleBaseInfo.getArticleId(), List.of()));
                    return vo;
                })
                .collect(Collectors.toList());
    }


    @Override
    public Integer getArticleCount() {
        return Math.toIntExact(this.lambdaQuery()
                .eq(ArticleBaseInfo::getDelFlag, 0)
                .count());
    }

    @Override
    @Transactional
    public void addArticle(ArticleBaseInfoVO vo) {
        // 保存文章基本信息
        ArticleBaseInfo articleBaseInfo = new ArticleBaseInfo();
        BeanUtils.copyProperties(vo, articleBaseInfo);
        articleBaseInfo.setDelFlag(0);
        articleBaseInfo.setStartTime(LocalDateTime.now());
        articleBaseInfo.setUpdateTime(LocalDateTime.now());
        articleBaseInfo.setStar(0);
        articleBaseInfo.setReadNum(0);
        articleBaseInfo.setTotalReadTime(LocalDateTime.now());

        // 如果articleId未设置，则使用当前时间戳作为默认值
        if (articleBaseInfo.getArticleId() == null) {
            articleBaseInfo.setArticleId((int) (System.currentTimeMillis() / 1000));
        }

        this.save(articleBaseInfo);

        // 处理标签
        if (vo.getTags() != null && !vo.getTags().isEmpty()) {
            processTagsForArticle(articleBaseInfo.getArticleId(), vo.getTags());
        }
    }

    @Override
    @Transactional
    public void updateArticle(ArticleBaseInfoRequestVO vo) {
        // 直接根据 articleId 更新文章基本信息
        ArticleBaseInfo articleToUpdate = new ArticleBaseInfo();
        articleToUpdate.setTitle(vo.getTitle());
        articleToUpdate.setDescription(vo.getDescription());
        articleToUpdate.setUpdateTime(LocalDateTime.now());

        this.lambdaUpdate()
                .eq(ArticleBaseInfo::getArticleId, vo.getArticleId())
                .eq(ArticleBaseInfo::getDelFlag, 0)
                .update(articleToUpdate);

        // 处理标签
        if (vo.getTags() != null) {
            // 先删除原有的标签关联
            articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                    .eq(ArticleTag::getArticleId, vo.getArticleId()));
            // 再添加新的标签关联
            if (!vo.getTags().isEmpty()) {
                processTagsForArticle(vo.getArticleId(), vo.getTags());
            }
        }
    }


    @Override
    public void deleteArticle(Integer id) {
        // 软删除，将del_flag设置为1
        this.lambdaUpdate()
                .eq(ArticleBaseInfo::getArticleId, id)
                .set(ArticleBaseInfo::getDelFlag, 1)
                .update();
    }

    @Override
    public Integer getLatestArticleId() {
        ArticleBaseInfo article = this.lambdaQuery()
                .eq(ArticleBaseInfo::getDelFlag, 0)
                .orderByDesc(ArticleBaseInfo::getArticleId)
                .last("LIMIT 1")
                .one();
        return article != null ? article.getArticleId() : 1;
    }

    /**
     * 处理文章标签关联
     * @param articleId 文章ID
     * @param tags 标签列表
     */
    private void processTagsForArticle(Integer articleId, List<String> tags) {
        for (String tagName : tags) {
            // 查找标签是否已存在
            Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getTagName, tagName));

            Integer tagId;
            if (tag == null) {
                // 标签不存在，创建新标签
                tag = new Tag();
                tag.setTagName(tagName);
                tagMapper.insert(tag);
                tagId = tag.getTagId();
            } else {
                // 标签已存在，使用现有标签ID
                tagId = tag.getTagId();
            }

            // 创建文章和标签的关联
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTagMapper.insert(articleTag);
        }
    }
}
