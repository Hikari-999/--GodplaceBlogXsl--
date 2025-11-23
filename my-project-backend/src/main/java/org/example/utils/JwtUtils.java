package org.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {

    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    //为用户生成Jwt令牌的冷却时间，防止刷接口频繁登录生成令牌，以秒为单位
    @Value("${spring.security.jwt.limit.base}")
    private int limit_base;
    //用户如果继续恶意刷令牌，更严厉的封禁时间
    @Value("${spring.security.jwt.limit.upgrade}")
    private int limit_upgrade;
    //判定用户在冷却时间内，继续恶意刷令牌的次数
    @Value("${spring.security.jwt.limit.frequency}")
    private int limit_frequency;


    @Resource
    StringRedisTemplate template;

    @Resource
    FlowUtils utils;
    //解析JWT信息
    public DecodedJWT resolve(String headerToken) {
        String token = this.converToken(headerToken);
        if (token == null) {
            return null;
        }
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            if(this.isInvalidToken(decodedJWT.getId()))//如果token已经失效了
                return null;
            Date expiresAt = decodedJWT.getExpiresAt(); //判断jwt是否过期
            return new Date().after(expiresAt) ? null : decodedJWT;
        } catch (JWTVerificationException exception) {
            return null;
        }
    }


    public String createJwt(UserDetails details, int id, String name) {
        if(this.frequencyCheck(id)) {
            Algorithm algorithm = Algorithm.HMAC256(key);
            Date expire = this.expireTime();//定义过期时间
            return JWT.create()
                    .withJWTId(UUID.randomUUID().toString())
                    .withClaim("id", id)
                    .withClaim("name", name)
                    .withClaim("authorities", details.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority).toList())
                    .withExpiresAt(expire)//设置过期时间
                    .withIssuedAt(new Date())//根据当前时间设置初始令牌时间
                    .sign(algorithm);
        }
        else return null;
    }


    private boolean frequencyCheck(int userId){
        String key = Const.JWT_FREQUENCY + userId;
        return utils.limitOnceUpgradeCheck(key, limit_frequency, limit_base, limit_upgrade);
    }
    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire * 24);
        return calendar.getTime();
    }

    private String converToken(String headerToken) {//判断token是否合法
        if (headerToken == null || !headerToken.startsWith("Bearer "))
            return null;
        return headerToken.substring(7);
    }

    public Integer toId(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }

    //解析用户令牌的方法
    public UserDetails toUser(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("*****")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    //让令牌失效
    public boolean invalidateJwt(String headerToken) {
        String token = this.converToken(headerToken);
        if (token == null) {
            return false;
        }
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = verifier.verify(token);
            String id = jwt.getId();
            return deleteToken(id, jwt.getExpiresAt());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    //删除token
    private boolean deleteToken(String uuid,Date time) {
        if(this.isInvalidToken(uuid)) {   //判断当前token是否失效
            return false;
        }
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(),0);
        template.opsForValue().set(Const.JWT_BLACK_LIST+uuid, "", expire, TimeUnit.MILLISECONDS);
        return true;
    }
    private boolean isInvalidToken(String uuid) {
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }
}
