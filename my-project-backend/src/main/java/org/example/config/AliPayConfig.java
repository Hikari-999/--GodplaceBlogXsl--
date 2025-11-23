package org.example.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置类
 * @author: Godplace
 * @date: 2025/8/8 22:18
 *
 **/
@Configuration
@Component
@Data
@ConfigurationProperties(prefix = "alipay")
public class AliPayConfig {
    //请求协议
    private String protocol;
    //请求网关
    private String gatewayHost;
    //签名类型
    private String signType;
    //appid
    private String appId;
    //私钥
    private String merchantPrivateKey;
    //应用公钥证书文件路径
    private String merchantCertPath;
    //支付宝公钥证书文件路径
    private String alipayCertPath;
    //支付宝根证书文件路径
    private String alipayRootCertPath;
    //异步通知地址
    private String notifyUrl;
    //设置AES密钥，调用AES加密相关接口时需要 请填写你的AES密钥
    private String encryptKey;
}
