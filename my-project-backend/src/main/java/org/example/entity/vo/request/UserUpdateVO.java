package org.example.entity.vo.request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

/**
 * 用户信息更新表单信息
 */
@Data
public class UserUpdateVO {
    private Integer id;

    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$")
    @Length(min = 1, max = 10)
    private String username;

    @Email
    @Length(min = 4)
    private String email;

    @Length(min = 6, max = 20)
    private String password;

    private String role;
}
