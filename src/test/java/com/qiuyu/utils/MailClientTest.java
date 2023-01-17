package com.qiuyu.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MailClientTest {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testSendMail() {
        String to = "2448567284@qq.com";
        String subject = "测试邮件";
        String content = "测试邮件内容";
        mailClient.sendMail(to, subject, content);
    }

    @Test
    public void testSendHtmlMail() {
        String to = "2448567284@qq.com";
        String subject = "测试邮件";

        //创建数据
        Context context = new Context();
        context.setVariable("username", "qiuyu");

        //根据模板,放入数据
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        //发送
        mailClient.sendMail(to, subject, content);
    }
}
