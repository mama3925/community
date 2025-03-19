package com.qiuyu.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author QiuYuSY
 * @create 2023-01-17 16:35
 * 用于发送邮件
 */
@Component
public class MailClient {
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * 从yml中读取发件人
     */
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送邮件
     * @param to
     * @param subject
     * @param content
     */
    public void sendMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            // 设置收发件人
            helper.setFrom(from);
            helper.setTo(to);

            // 设置标题和内容
            helper.setSubject(subject);
            helper.setText(content, true);

            // 发送
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败，{}", e.getMessage());
        }
    }
}
