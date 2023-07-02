package com.example.community;

import com.example.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {
    //注入发送邮件的工具类
    @Autowired
    private MailClient mailClient;

    //注入模板引擎
    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 发送文本内容邮件
     */
    @Test
    public void testTextMail(){
        mailClient.sendMail("1724789330@qq.com","TEST","Welcome");
    }

    /**
     * 发送html类型文件
     */
    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","铭哥");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);
        mailClient.sendMail("1724789330@qq.com","HTML",content);
    }

}
