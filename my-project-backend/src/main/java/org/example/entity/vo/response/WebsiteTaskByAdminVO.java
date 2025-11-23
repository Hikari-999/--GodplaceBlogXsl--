package org.example.entity.vo.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WebsiteTaskByAdminVO {
    @NotNull(message = "ID不能为空")
    Integer id;
    @NotBlank(message = "Task标题不能为空")
    String title;
    @NotBlank(message = "描述不能为空")
    String description;
    @NotBlank(message = "状态不能为空")
    Integer status;
}
