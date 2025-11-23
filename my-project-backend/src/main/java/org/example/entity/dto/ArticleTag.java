// ArticleTag.java
package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("db_article_tag")
public class ArticleTag {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer articleId;

    private Integer tagId;
}
