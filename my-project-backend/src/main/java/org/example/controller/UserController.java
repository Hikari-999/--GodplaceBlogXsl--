package org.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.RestBean;
import org.example.entity.dto.Account;
import org.example.entity.vo.request.AdminAddUserVO;
import org.example.entity.vo.request.UserUpdateVO;
import org.example.service.AccountService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/admin/user")
public class UserController {

    @Resource
    private AccountService accountService;

    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 获取所有用户列表（过滤掉管理员账户）
     * @return 用户列表
     */
    @GetMapping("/list")
    public RestBean<List<Account>> getUserList() {
        try {
            List<Account> users = accountService.list();
            // 过滤掉管理员账户，只返回普通用户
            List<Account> filteredUsers = users.stream()
                    //.filter(user -> !"admin".equals(user.getRole()))
                    .filter(user -> !"1".equals(user.getDelFlag()))
                    .collect(Collectors.toList());

            // 清除密码信息，不返回给前端
            filteredUsers.forEach(user -> user.setPassword(null));
            return RestBean.success(filteredUsers);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return RestBean.failure(500, "获取用户列表失败");
        }
    }

    /**
     * 根据ID删除用户
     * @param id 用户ID
     * @return 操作结果
     */
    @PostMapping("/delete/{id}")
    public RestBean<Void> deleteUser(@PathVariable Integer id) {
        try {
            // 检查要删除的用户是否是管理员
            Account account = accountService.getById(id);
            if (account == null) {
                return RestBean.failure(400, "用户不存在");
            }

            if ("admin".equals(account.getRole())) {
                return RestBean.failure(400, "不能删除管理员账户");
            }

            boolean result = accountService.removeById(id);
            if (result) {
                return RestBean.success();
            } else {
                return RestBean.failure(400, "删除用户失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return RestBean.failure(500, "删除用户失败");
        }
    }

    /**
     * 更新用户角色
     * @param id 用户ID
     * @param role 新角色
     * @return 操作结果
     */
    @PostMapping("/update-role/{id}")
    public RestBean<Void> updateUserRole(@PathVariable Integer id, @RequestParam String role) {
        try {
            Account account = accountService.getById(id);
            if (account == null) {
                return RestBean.failure(400, "用户不存在");
            }

            // 检查是否试图将用户设置为管理员
            if ("admin".equals(role)) {
                return RestBean.failure(400, "不能将用户设置为管理员");
            }

            account.setRole(role);
            boolean result = accountService.updateById(account);
            if (result) {
                return RestBean.success();
            } else {
                return RestBean.failure(400, "更新用户角色失败");
            }
        } catch (Exception e) {
            log.error("更新用户角色失败", e);
            return RestBean.failure(500, "更新用户角色失败");
        }
    }

    /**
     * 更新用户信息（用户名、邮箱、密码、角色）
     * @param vo 用户更新信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public RestBean<Void> updateUser(@RequestBody UserUpdateVO vo) {
        try {
            Account account = accountService.getById(vo.getId());
            if (account == null) {
                return RestBean.failure(400, "用户不存在");
            }

            // 检查用户名是否已被其他用户使用
            if (vo.getUsername() != null && !vo.getUsername().equals(account.getUsername())) {
                QueryWrapper<Account> usernameQuery = new QueryWrapper<>();
                usernameQuery.eq("username", vo.getUsername());
                if (accountService.getOne(usernameQuery) != null) {
                    return RestBean.failure(400, "用户名已被使用");
                }
                account.setUsername(vo.getUsername());
            }

            // 检查邮箱是否已被其他用户使用
            if (vo.getEmail() != null && !vo.getEmail().equals(account.getEmail())) {
                QueryWrapper<Account> emailQuery = new QueryWrapper<>();
                emailQuery.eq("email", vo.getEmail());
                if (accountService.getOne(emailQuery) != null) {
                    return RestBean.failure(400, "邮箱已被使用");
                }
                account.setEmail(vo.getEmail());
            }

            // 更新密码（如果提供了新密码）
            if (vo.getPassword() != null && !vo.getPassword().isEmpty()) {
                account.setPassword(passwordEncoder.encode(vo.getPassword()));
            }

            // 更新角色（如果提供了新角色）
            if (vo.getRole() != null && !vo.getRole().isEmpty()) {
                // 检查是否试图将用户设置为管理员
                if ("admin".equals(vo.getRole())) {
                    return RestBean.failure(400, "不能将用户设置为管理员");
                }
                account.setRole(vo.getRole());
            }

            boolean result = accountService.updateById(account);
            if (result) {
                return RestBean.success();
            } else {
                return RestBean.failure(400, "更新用户信息失败");
            }
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return RestBean.failure(500, "更新用户信息失败");
        }
    }
    /**
     * 管理员添加新用户
     * @param vo 用户信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public RestBean<Void> addUser(@RequestBody @Valid AdminAddUserVO vo) {
        try {
            // 检查用户名是否已被使用
            QueryWrapper<Account> usernameQuery = new QueryWrapper<>();
            usernameQuery.eq("username", vo.getUsername());
            if (accountService.getOne(usernameQuery) != null) {
                return RestBean.failure(400, "用户名已被使用");
            }

            // 检查邮箱是否已被使用
            QueryWrapper<Account> emailQuery = new QueryWrapper<>();
            emailQuery.eq("email", vo.getEmail());
            if (accountService.getOne(emailQuery) != null) {
                return RestBean.failure(400, "邮箱已被使用");
            }

            // 创建新用户
            Account account = new Account(null,vo.getUsername(),passwordEncoder.encode(vo.getPassword()),vo.getEmail(),null,new Date(),"0");
            if(Objects.equals(vo.getRole(), "admin")) return RestBean.failure(400, "不能将用户设置为管理员");
            account.setRole(vo.getRole() != null ? vo.getRole() : "user");
            account.setDelFlag("0");

            boolean result = accountService.save(account);
            if (result) {
                return RestBean.success();
            } else {
                return RestBean.failure(400, "添加用户失败");
            }
        } catch (Exception e) {
            log.error("添加用户失败", e);
            return RestBean.failure(500, "添加用户失败");
        }
    }
}
