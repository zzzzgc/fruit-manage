package com.fruit.manage.util;

import com.jfinal.ext2.kit.DateTimeKit;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author partner
 * @date 2018/3/22 11:42
 */
public class IdUtil {

    /**
     * 获取采购订单编号
     * @return 返回采购订单编号
     */
    public static String getProrementPlanId(){
        String prorementPlanId = "";
        if (Integer.parseInt(DateAndStringFormat.getHour())>=12){
            prorementPlanId=DateAndStringFormat.getNextDay(DateAndStringFormat.getStringDateShort(new Date()),"1").replaceAll("-","")+DateAndStringFormat.getHour()+DateAndStringFormat.getTime();
        }else {
            prorementPlanId=DateAndStringFormat.getStringDateLong();
        }

        return prorementPlanId;
    }

    /**
     * 创建订单周期订单号(b_order的order_id)
     * @param business_user_id 一般是商户(b_business_user)的用户id
     * @return 订单周期订单号
     */
    public static String createOrderId(Integer business_user_id) {
        // 当前的24小时制的小时
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR_OF_DAY)>=12) {
            // 超過11:59:59算明天的訂單
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return DateTimeKit.formatDateToStyle("yyyyMMdd", calendar.getTime()) + "-" + business_user_id;
    }

    /**
     * 获取指定的订单周期订单号
     * @param date 订单周期日期
     * @param business_user_id 一般是商户(b_business_user)的用户id
     * @return 订单周期订单号
     */
    public static String getOrderId (Date date,Integer business_user_id) {
        return DateTimeKit.formatDateToStyle("yyMMdd", date) + "-" + business_user_id;
    }

    public static void main(String[] args) {
        System.out.println(getProrementPlanId());
    }
}
