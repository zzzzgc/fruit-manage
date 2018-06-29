package com.fruit.manage.interceptor;

import com.fruit.manage.util.ZihoMailMessage;
import com.fruit.manage.util.ZhioSendEmailUtil;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

/**
 * @author partner
 * @date 2018/5/24 20:05
 */
public class ExceptionInterceper implements Interceptor {
    private static ZhioSendEmailUtil sendEmailUtil =null;
    @Override
    public void intercept(Invocation inv) {
        try {
            inv.invoke();
        } catch (Exception e) {
            e.printStackTrace();
            if (sendEmailUtil == null) {
                sendEmailUtil=new ZhioSendEmailUtil();
            }

            // 调用通知
            ZihoMailMessage zihoMailMessage = new ZihoMailMessage();
            String[] reiveveMessage = new String[4];
            reiveveMessage[0] = "zguocong@52xiguo.com";
            reiveveMessage[1] = "1142287959@qq.com";
            reiveveMessage[2] = "zguocong@52xiguo.com";
            reiveveMessage[3] = "1098766713@qq.com";

            zihoMailMessage.setSubject("我是异常");
            zihoMailMessage.setReceiveAddress(reiveveMessage);
            zihoMailMessage.setContent(e.getMessage());

            try {
                sendEmailUtil.sendEmail(zihoMailMessage);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
