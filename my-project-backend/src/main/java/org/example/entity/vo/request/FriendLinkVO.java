package org.example.entity.vo.request;

import lombok.Data;

@Data
public class FriendLinkVO {
    private Integer id;
    private String name;
    private String url;
    private String description;
    private String avatar;
    private Integer isDefault;
}
