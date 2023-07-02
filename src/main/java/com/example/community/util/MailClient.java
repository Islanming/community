package com.example.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 邮件发送
 * 类似邮件发送功能的客户端
 * @author Lenovo
 */
@Component
public class MailClient {
    //打印日志
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    //注入JavaMailSender
    @Autowired
    private JavaMailSender mailSender;

    //将发件人导入，即之前注册的邮箱
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 提供一个public的方法，供外界调用
     * @param to 收件人
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public void sendMail(String to,String subject,String content){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            //用帮助类帮助构建message
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            //第二个参数表示发送的邮件是html格式
            helper.setText(content,true);
            mailSender.send(helper.getMimeMessage());
        }catch (MessagingException e){
            //用日志记录异常
            logger.error("发送邮件失败："+e.getMessage());
        }
    }
}
