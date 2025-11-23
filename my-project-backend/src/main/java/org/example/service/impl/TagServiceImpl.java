// TagServiceImpl.java
package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.entity.dto.ArticleTag;
import org.example.entity.dto.Tag;
import org.example.mapper.ArticleTagMapper;
import org.example.mapper.TagMapper;
import org.example.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Resource
    private ArticleTagMapper articleTagMapper;

    @Override
    public Map<Integer, List<String>> getArticleTagsMap() {
        // 查询所有文章标签关联信息
        List<ArticleTag> articleTags = articleTagMapper.selectList(null);

        // 查询所有标签信息
        List<Tag> tags = this.list();

        // 构建 tagId -> tagName 的映射
        Map<Integer, String> tagMap = tags.stream()
                .collect(Collectors.toMap(Tag::getTagId, Tag::getTagName));

        // 构建 articleId -> tagNames 的映射
        return articleTags.stream()
                .collect(Collectors.groupingBy(ArticleTag::getArticleId,
                        Collectors.mapping(at -> tagMap.get(at.getTagId()),
                                Collectors.toList())));
    }
}
