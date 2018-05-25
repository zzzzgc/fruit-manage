package com.fruit.manage.util;

import com.jfinal.kit.PropKit;

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * @author partner
 * @date 2018/5/24 19:42
 */
public class SendEmailUtil {

    public void sendEmail(MailMessage mailMessage) throws Exception {
        // 参数配置
        Properties props = new Properties();
        // 使用的协议（JavaMail规范要求）
        props.setProperty("mail.transport.protocol", "smtp");
        // 发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.host", PropKit.get("mail.server.host"));
        // 需要请求认证
        props.setProperty("mail.smtp.auth", PropKit.get("mail.smtp.auth"));
        props.setProperty("mail.smtp.port", PropKit.get("mail.server.port"));
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", PropKit.get("mail.smtp.socketFactory.fallback"));
        props.setProperty("mail.smtp.socketFactory.port", PropKit.get("mail.server.port"));

        // 2. 根据配置创建会话对象, 用于和邮件服务器交互
        Session session = Session.getInstance(props);
        // 设置为debug模式, 可以查看详细的发送 log
        session.setDebug(true);

        // 3. 创建一封邮件
        MimeMessage message = createMimeMessage(session, PropKit.get("mail.from.address"), mailMessage);

        // 4. 根据 Session 获取邮件传输对象
        Transport transport = session.getTransport();

        //    PS_03: 仔细看log, 认真看log, 看懂log, 错误原因都在log已说明。
        transport.connect(PropKit.get("mail.from.address"), PropKit.get("mail.password"));

        // 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(message, message.getAllRecipients());

        // 7. 关闭连接
        transport.close();
    }


    /**
     * 创建一封只包含文本的简单邮件
     *
     * @param session 和服务器交互的会话
     * @return
     * @throws Exception
     */
    public static MimeMessage createMimeMessage(Session session,String sendAddress, MailMessage mailMessage) throws Exception {
        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);

        // 2. From: 发件人（昵称有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改昵称）
        message.setFrom(new InternetAddress(sendAddress, PropKit.get("mail.send.user.name"), "UTF-8"));

        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        if(mailMessage.getReceiveAddress()!=null && mailMessage.getReceiveAddress().length>0){
            for (int i = 0; i < mailMessage.getReceiveAddress().length; i++) {
                message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(mailMessage.getReceiveAddress()[i], "指猴研发大佬", "UTF-8"));
            }
        }

        // 4. Subject: 邮件主题（标题有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改标题）
        message.setSubject(mailMessage.getSubject());

        // 5. Content: 邮件正文（可以使用html标签）（内容有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改发送内容）
        message.setContent(mailMessage.getContent(), "text/html;charset=UTF-8");

        // 6. 设置发件时间
        message.setSentDate(new Date());

        // 7. 保存设置
        message.saveChanges();
        return message;
    }
}
