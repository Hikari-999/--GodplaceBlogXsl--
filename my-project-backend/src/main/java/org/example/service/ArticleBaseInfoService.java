package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.RestBean;
import org.example.entity.dto.ArticleBaseInfo;
import org.example.entity.vo.request.ArticleBaseInfoRequestVO;
import org.example.entity.vo.response.ArticleBaseInfoVO;
import org.example.entity.vo.response.BlogListBaseInfoVO;

import java.util.List;

public interface ArticleBaseInfoService extends IService<ArticleBaseInfo> {
    ArticleBaseInfoVO getArticleBaseInfoById(String id);

    RestBean<Void> modifyReadOrStar(String type,Integer id);

    List<BlogListBaseInfoVO> getArticleList();
    // 可以在这里添加自定义的业务方法声明

    /**
     * 获取文章总数
     * @return 文章总数
     */
    Integer getArticleCount();
    /**
     * 新增文章
     * @param vo 文章信息
     */
    void addArticle(ArticleBaseInfoVO vo);

    /**
     * 更新文章
     * @param vo 文章信息
     */
    void updateArticle(ArticleBaseInfoRequestVO vo);

    /**
     * 删除文章（软删除）
     * @param id 文章ID
     */
    void deleteArticle(Integer id);

    /**
     * 获取最新的文章ID
     * @return 最新文章ID
     */
    Integer getLatestArticleId();
}
