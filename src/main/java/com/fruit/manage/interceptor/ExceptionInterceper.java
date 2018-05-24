package com.fruit.manage.interceptor;

import com.fruit.manage.util.MailMessage;
import com.fruit.manage.util.SendEmailUtil;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

/**
 * @author partner
 * @date 2018/5/24 20:05
 */
public class ExceptionInterceper implements Interceptor {
    private static SendEmailUtil sendEmailUtil =null;
    @Override
    public void intercept(Invocation inv) {
        try {
            inv.invoke();
        } catch (Exception e) {
            e.printStackTrace();
            if (sendEmailUtil == null) {
                sendEmailUtil=new SendEmailUtil();
            }

            // 调用通知
            MailMessage mailMessage = new MailMessage();
            String[] reiveveMessage = new String[2];
            reiveveMessage[0] = "1098766713@qq.com";
            reiveveMessage[1] = "1142287959@qq.com";

            mailMessage.setSubject("我是异常");
            mailMessage.setReceiveAddress(reiveveMessage);
            mailMessage.setContent(e.getMessage());

            try {
                sendEmailUtil.sendEmail(mailMessage);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
