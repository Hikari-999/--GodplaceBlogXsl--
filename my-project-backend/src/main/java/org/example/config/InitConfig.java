package org.example.config;

import cn.hutool.core.io.resource.ClassPathResource;
import com.alipay.easysdk.kernel.Config;
import lombok.extern.slf4j.Slf4j;
import org.example.utils.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Godpalce
 * @date 2025/8/8
 * @description 全局初始化配置
 */
@Configuration
@Slf4j
public class InitConfig implements InitializingBean {
    private final AliPayConfig aliPayConfig;
    public InitConfig(AliPayConfig aliPayConfig) {
        this.aliPayConfig = aliPayConfig;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("支付宝全局配置开始");
        Config config = new Config();
        config.protocol = aliPayConfig.getProtocol();
        config.gatewayHost = aliPayConfig.getGatewayHost();
        config.signType = aliPayConfig.getSignType();
        config.appId = aliPayConfig.getAppId();
        config.merchantPrivateKey = StringUtils.readFileOfTxt(new ClassPathResource(aliPayConfig.getMerchantPrivateKey()).getPath());
        config.merchantCertPath = aliPayConfig.getMerchantCertPath();
        config.alipayCertPath = aliPayConfig.getAlipayCertPath();
        config.alipayRootCertPath = aliPayConfig.getAlipayRootCertPath();
        config.notifyUrl = aliPayConfig.getNotifyUrl();
        log.info("支付宝全局配置结束");
    }

}
