package org.example.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.RestBean;
import org.example.entity.vo.request.ArticleBaseInfoRequestVO;
import org.example.entity.vo.response.ArticleBaseInfoVO;
import org.example.entity.vo.response.BlogListBaseInfoVO;
import org.example.service.ArticleBaseInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@RestController
@Slf4j
@RequestMapping("/article-base-info")
public class ArticleBaseInfoController {

    @Resource
    private ArticleBaseInfoService articleBaseInfoService;

    // 从配置文件中获取前端文章目录路径
    @Value("${frontend.articles.path:F:/xihua/JAVA/project/GodplaceBlog/my-project-frontend/src/assets/articles}")
    private String articlesPath;

    @GetMapping("/getArticleInfoById")
    public RestBean<ArticleBaseInfoVO> getArticleBaseInfoById(@RequestParam String id) {
        return RestBean.success(articleBaseInfoService.getArticleBaseInfoById(id));
    }

    @GetMapping("/getArticleList")
    public RestBean<List<BlogListBaseInfoVO>> getArticleList() {
        return RestBean.success(articleBaseInfoService.getArticleList());
    }

    @PostMapping("/modifyReadOrStar")
    public RestBean<Void> modifyReadOrStar(@RequestParam String type,
                                           @RequestParam int id){
        return articleBaseInfoService.modifyReadOrStar(type,id);
    }

    @PostMapping("/add")
    public RestBean<Void> addArticle(@RequestBody ArticleBaseInfoVO vo) {
        if (Objects.equals(vo.getRole(), "admin")) {
            articleBaseInfoService.addArticle(vo);
            return RestBean.success();
        } else {
            return RestBean.failure(403, "无权限");
        }
    }

    @PostMapping("/update")
    public RestBean<Void> updateArticle(@RequestBody ArticleBaseInfoRequestVO vo) {
        log.info("接收到的更新请求数据: {}", vo);
        if (Objects.equals(vo.getRole(), "admin")) {
            articleBaseInfoService.updateArticle(vo);
            return RestBean.success();
        } else {
            return RestBean.failure(403, "无权限");
        }
    }

    @PostMapping("/delete")
    public RestBean<Void> deleteArticle(@RequestParam Integer id, @RequestParam String role) {
        if (Objects.equals(role, "admin")) {
            articleBaseInfoService.deleteArticle(id);
            return RestBean.success();
        } else {
            return RestBean.failure(403, "无权限");
        }
    }

    /**
     * 新增文章并上传Markdown文件
     */
    @PostMapping("/addWithFile")
    public RestBean<Void> addArticleWithFile(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String tags,
            @RequestParam String role,
            @RequestParam("file") MultipartFile file) {
        if (!Objects.equals(role, "admin")) {
            return RestBean.failure(403, "无权限");
        }

        if (file.isEmpty()) {
            return RestBean.failure(400, "文件不能为空");
        }

        // 解析标签
        List<String> tagList = List.of(tags.replace("[", "").replace("]", "").replace("\"", "").split(","));
        tagList = tagList.stream().map(String::trim).filter(tag -> !tag.isEmpty()).toList();

        try {
            // 保存文章基本信息到数据库
            ArticleBaseInfoVO vo = new ArticleBaseInfoVO();
            vo.setTitle(title);
            vo.setDescription(description);
            vo.setTags(tagList);
            vo.setRole(role);
            articleBaseInfoService.addArticle(vo);

            // 获取最新添加的文章ID
            Integer articleId = articleBaseInfoService.getLatestArticleId();

            // 构建正确的文件保存路径
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            Path articlesDir = projectDir.resolve(articlesPath);
            String fileName = "article" + articleId + ".md";
            Path filePath = articlesDir.resolve(fileName);

            // 确保目录存在
            Files.createDirectories(filePath.getParent());

            // 保存文件
            file.transferTo(filePath.toFile());

            return RestBean.success();
        } catch (IOException e) {
            log.error("文件保存失败", e);
            return RestBean.failure(500, "文件保存失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("新增文章失败", e);
            return RestBean.failure(500, "新增文章失败: " + e.getMessage());
        }
    }
}
