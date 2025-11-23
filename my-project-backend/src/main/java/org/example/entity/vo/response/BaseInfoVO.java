package org.example.entity.vo.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseInfoVO {
    /**
     * 网站启动时间
     */
    private LocalDateTime startRuntime;
    /**
     * 网站更新时间
     */
    private LocalDateTime updateRuntime;

    /**
     * 总访问量
     */
    private Integer totalVisits;

    /**
     * 当前在线人数
     */
    private Integer currentOnline;

    /**
     * 文章总数
     */
    private Integer totalArticles;
}
