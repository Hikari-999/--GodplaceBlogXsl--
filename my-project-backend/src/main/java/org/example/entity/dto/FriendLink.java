package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.example.entity.BaseData;
import org.example.entity.vo.request.FriendLinkVO;

import java.time.LocalDateTime;


@Data
@TableName("db_friend_link")
public class FriendLink implements BaseData {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String url;
    private String description;
    private String avatar;
    private Integer isDefault;
    private LocalDateTime createTime;

    public FriendLinkVO toVO() {
        return this.asViewObject(FriendLinkVO.class);
    }
}
