package com.fruit.manage.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author partner
 * @date 2018/3/22 11:42
 */
public class SerialNumUtil {

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

    public static void main(String[] args) {
        System.out.println(getProrementPlanId());
    }
}
