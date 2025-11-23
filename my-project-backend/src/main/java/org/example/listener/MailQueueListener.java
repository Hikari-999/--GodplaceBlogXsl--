package org.example.listener;

import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用于处理邮件发送的消息队列监听器
 */
@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {

    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;

    String senderName = "Godplace";

    /**
     * 处理邮件发送
     * @param data 邮件信息
     */
    @RabbitHandler
    public void sendMailMessage(Map<String, Object> data) {
        try {
            String email = data.get("email").toString();
            Integer code = (Integer) data.get("code");
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String templatePath = "";
            String subject = "";

            switch (data.get("type").toString()) {
                case "register":
                    templatePath = "mail/register.html";
                    subject = "欢迎注册我们的网站";
                    break;
                case "reset":
                    templatePath = "mail/reset.html";
                    subject = "您的密码重置邮件";
                    break;
                default:
                    return;
            }

            String content = loadEmailTemplate(templatePath, code);

            helper.setSubject(subject);
            helper.setText(content, true); // true indicates HTML content
            helper.setTo(email);
            helper.setFrom(new InternetAddress(username, senderName, "UTF-8"));

            sender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载邮件模板并替换验证码
     * @param templatePath 模板路径
     * @param code 验证码
     * @return 渲染后的HTML内容
     */
    private String loadEmailTemplate(String templatePath, Integer code) throws Exception {
        ClassPathResource resource = new ClassPathResource(templatePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String content = reader.lines().collect(Collectors.joining("\n"));
            return content.replace("{{code}}", code.toString());
        }
    }
}
