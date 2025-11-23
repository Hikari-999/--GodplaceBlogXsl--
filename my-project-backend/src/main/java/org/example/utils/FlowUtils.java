package org.example.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 限流通用工具
 * 针对于不同的情况进行限流操作，支持限流升级
 */
@Slf4j
@Component
public class FlowUtils {

    @Resource
    StringRedisTemplate template;

    /**
     * 针对于单次频率限制，请求成功后，在冷却时间内不得再次进行请求，如3秒内不能再次发起请求
     * @param key 键
     * @param blockTime 限制时间
     * @return 是否通过限流检查
     */
    public boolean limitOnceCheck(String key, int blockTime){
        return this.internalCheck(key, 1, blockTime, (overclock) -> false);
    }

    /**
     * 针对于单次频率限制，请求成功后，在冷却时间内不得再次进行请求
     * 如3秒内不能再次发起请求，如果不听劝阻继续发起请求，将限制更长时间
     * @param key 键
     * @param frequency 请求频率
     * @param baseTime 基础限制时间
     * @param upgradeTime 升级限制时间
     * @return 是否通过限流检查
     */
    public boolean limitOnceUpgradeCheck(String key, int frequency, int baseTime, int upgradeTime){
        return this.internalCheck(key, frequency, baseTime, (overclock) -> {
            if (overclock)
                template.opsForValue().set(key, "1", upgradeTime, TimeUnit.SECONDS);
            return false;
        });
    }

    /**
     * 针对于在时间段内多次请求限制，如3秒内限制请求20次，超出频率则封禁一段时间
     * @param counterKey 计数键
     * @param blockKey 封禁键
     * @param blockTime 封禁时间
     * @param frequency 请求频率
     * @param period 计数周期
     * @return 是否通过限流检查
     */
    public boolean limitPeriodCheck(String counterKey, String blockKey, int blockTime, int frequency, int period){
        return this.internalCheck(counterKey, frequency, period, (overclock) -> {
            if (overclock)
                template.opsForValue().set(blockKey, "", blockTime, TimeUnit.SECONDS);
            return !overclock;
        });
    }

    /**
     * 内部使用请求限制主要逻辑
     * @param key 计数键
     * @param frequency 请求频率
     * @param period 计数周期
     * @param action 限制行为与策略
     * @return 是否通过限流检查
     */
    private boolean internalCheck(String key, int frequency, int period, LimitAction action){
        String count = template.opsForValue().get(key);
        if (count != null) {
            long value = Optional.ofNullable(template.opsForValue().increment(key)).orElse(0L);//运行到这里如果键值过期了，那么返回1
            int c = Integer.parseInt(count);
            if(value != c + 1) //1！=当前count+1
                template.expire(key, period, TimeUnit.SECONDS);
            return action.run(value > frequency);
        } else {
            template.opsForValue().set(key, "1", period, TimeUnit.SECONDS);//重设逻辑为 60内执行1次
            return true;
        }
    }
/*    第一次调用的完整流程：
    // 1. 调用 limitPeriodCheck
    limitPeriodCheck("user:123:counter", "user:123:block", 300, 5, 60);
    // 2. 进入 internalCheck 方法
    private boolean internalCheck(String key, int frequency, int period, LimitAction action){
        String count = template.opsForValue().get(key);
        // 3. 第一次调用时，count 为 null
        if (count != null) {
            // 这里面的代码不会执行
        ...
        } else {
            // 4. 执行这里的代码
            template.opsForValue().set(key, "1", period, TimeUnit.SECONDS);
            return true;  // 5. 直接返回true，不执行Action！
        }
    }
    // 1. 再次调用 limitPeriodCheck
    limitPeriodCheck("user:123:counter", "user:123:block", 300, 5, 60);
    // 2. 进入 internalCheck 方法
    private boolean internalCheck(String key, int frequency, int period, LimitAction action){
        String count = template.opsForValue().get(key);
        // 3. 此时 count 不为 null（之前已创建）
        if (count != null) {
            // 4. 执行这里的代码
            long value = Optional.ofNullable(template.opsForValue().increment(key)).orElse(0L);
            int c = Integer.parseInt(count);
            if(value != c + 1)
                template.expire(key, period, TimeUnit.SECONDS);
            // 5. 关键：此时才执行传入的Action！
            return action.run(value > frequency);  // 这里才执行Action中的代码
        }
    }*/


    /**
     * 内部使用，限制行为与策略
     */
    private interface LimitAction {
        boolean run(boolean overclock);
    }
}
