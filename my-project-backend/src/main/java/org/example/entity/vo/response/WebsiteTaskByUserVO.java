package org.example.entity.vo.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WebsiteTaskByUserVO {
    @NotNull(message = "ID不能为空")
    Integer id;
    @NotBlank(message = "Task标题不能为空")
    String titleByuser;
    @NotBlank(message = "描述不能为空")
    String descriptionByuser;
    @NotBlank(message = "姓名不能为空")
    String username;
}
