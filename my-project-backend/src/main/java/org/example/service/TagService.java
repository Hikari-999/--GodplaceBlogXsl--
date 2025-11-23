// TagService.java
package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.Tag;

import java.util.List;
import java.util.Map;

public interface TagService extends IService<Tag> {
    Map<Integer, List<String>> getArticleTagsMap();
}
