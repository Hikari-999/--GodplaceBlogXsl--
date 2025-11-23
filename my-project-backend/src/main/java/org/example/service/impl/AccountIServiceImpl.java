package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.dto.Account;
import org.example.entity.vo.request.ConfirmResetVO;
import org.example.entity.vo.request.EmailRegisterVO;
import org.example.entity.vo.request.EmailResetVO;
import org.example.mapper.AccountMapper;
import org.example.service.AccountService;
import org.example.utils.Const;
import org.example.utils.FlowUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.Query;
import java.util.*;
import java.util.concurrent.TimeUnit;
@Slf4j
@Service
public class AccountIServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    FlowUtils flowUtils;
    @Resource
    AmqpTemplate amqpTemplate;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    PasswordEncoder passwordEncoder;

    //验证邮件发送冷却时间限制，秒为单位
    @Value("${spring.web.verify.mail-limit}")
    int verifyLimit;

    //用户登录
    @Override
    public UserDetails loadUserByUsername(String test) throws UsernameNotFoundException {
        Account account =this.findAccountByNameOrEmail(test);
        if (account == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return User
                .withUsername(test)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }
    public Account findAccountByNameOrEmail(String test) {
        return this.query()
                .eq("username", test).or()
                .eq("email",test)
                .one();

    }

    //注册创建邮件
    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) { //流量锁 防止同一时间内大量请求同时访问改接口
            if (!this.verifyLimit(ip)) {  // 流量限制 针对统一ip的用户 单线程防止多请求
                return "请求频繁，请稍后再试";
            }
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            Map<String, Object> date = Map.of("type", type, "email", email, "code", code);//type 注册或者重置或者修改..
            amqpTemplate.convertAndSend("mail", date);
            stringRedisTemplate.opsForValue().
                    set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        }
    }

    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        String email = vo.getEmail();
        String redisKey = Const.VERIFY_EMAIL_DATA + email;
        String username = vo.getUsername();
        String code = stringRedisTemplate.opsForValue().get(redisKey);//将用户获取验证码时存入redis的验证码与用户自己填写的验证码啊作对比
        if (code == null) return "请先获取验证码"+vo.getCode();
        if(!code.equals(vo.getCode())) return "验证码错误";
        if(this.existsAccountByEmail(email)) return "此邮件已被注册";
        if(this.existsAccountByUsername(username)) return "此用户名已被注册";
        String password = passwordEncoder.encode(vo.getPassword());
        Account account = new Account(null,username,password,email,"user",new Date(),"0");
        if (this.save(account)) {
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
            return  null;
        }else return "内部错误，请联系管理员";

    }


    /**
     * 邮件验证码重置密码操作，需要检查验证码是否正确
     * @param info 重置基本信息
     * @return 操作结果，null表示正常，否则为错误原因
     */

    @Override
    public String resetEmailAccountPassword(EmailResetVO info) {
        String email = info.getEmail();
        String verify = this.resetConfirm(new ConfirmResetVO(email,info.getCode()));
        if (verify!=null) return verify;
        String password = passwordEncoder.encode(info.getPassword());
        boolean update = this.update()
                .eq("email",email)
                .set("password",password).update();
        if (update) {
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
        }
        return null;
    }

    /**
     * 执行密码重置确认，检查验证码是否正确
     * @param info 密码重置信息
     * @return 是否操作成功
     */
    @Override
    public String resetConfirm(ConfirmResetVO info) {
        String email = info.getEmail();
        String redisKey = Const.VERIFY_EMAIL_DATA + email;
        String code = stringRedisTemplate.opsForValue().get(redisKey);
        if(code == null) return "请先获取验证码";
        if(!code.equals(info.getCode())) return "验证码错误";
        return null;
    }

    @Override
    public String verifyRole(String username) {
        log.info("查询用户角色，用户名: {}", username);

        if (username == null || username.isEmpty()) {
            log.warn("用户名为空");
            return "user";
        }

        try {
            // 查询完整对象而不是只查询role字段
            Account account = this.getOne(
                    new QueryWrapper<Account>()
                            .eq("username", username)
            );

            log.info("查询结果 - account: {}", account);

            if (account != null && account.getRole() != null) {
                log.info("用户角色: {}", account.getRole());
                return account.getRole();
            } else {
                log.warn("未找到用户名为 {} 的账户或角色为空，返回默认角色", username);
                return "user";
            }
        } catch (Exception e) {
            log.error("查询用户角色时发生异常", e);
            return "user";
        }
    }




    //针对ip地址来对邮件验证码进行限流操作
    private boolean verifyLimit(String ip) {
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return flowUtils.limitOnceCheck(key,verifyLimit);
    }

    /**
     * 查询指定邮箱的用户是否已经存在
     * @param email 邮箱
     * @return 是否存在
     */
    private boolean existsAccountByEmail(String email){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email", email));
    }

    /**
     * 查询指定用户名的用户是否已经存在
     * @param username 用户名
     * @return 是否存在
     */
    private boolean existsAccountByUsername(String username){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username", username));
    }

}
