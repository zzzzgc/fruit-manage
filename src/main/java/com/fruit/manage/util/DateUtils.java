package com.fruit.manage.util;

import com.jfinal.ext.kit.DateKit;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * 获取业务需要的时间
 * @Author: ZGC
 * @Date Created in 14:30 2018/5/30
 */
public class DateUtils {

    /**
     * 根据时间获取该时间段的订单周期日期
     */
    public static Date getOrderCycleDate (Date date) {
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        if (calendar.get(Calendar.HOUR_OF_DAY)>=12) {
            // 超過11:59:59算明天的訂單
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return calendar.getTime();
    }

    /**
     * 获取指定时间的首日订单周期时间区间
     * @param OrderCycleDate 订单周期日期! 日期! 日期! 会忽略时间.允许为null
     * @return yyyy-MM-dd HH:mm:ss格式的时间的数组(两个值,时间区间来的)
     */
    public static String[] getOrderCycleDateStrings (Date OrderCycleDate) {
//        Date createTime = getParaToDate("createTime");

        String[] createTimes = new String[2];

        if (OrderCycleDate != null) {
            // 使用指定时间导出采购计划
            String createTimeStr = DateAndStringFormat.getStringDateShort(OrderCycleDate);
            createTimes[0] = DateAndStringFormat.getNextDay(createTimeStr, "-1") + " 12:00:00";
            createTimes[1] = createTimeStr + " 11:59:59";
        } else {
            // 使用当前时间导出采购计划(当天首日订单周期的)
            Calendar nowCalendar = Calendar.getInstance();
            nowCalendar.add(Calendar.DAY_OF_MONTH, -1);
            createTimes[0] = DateFormatUtils.format(nowCalendar.getTime(), "yyyy-MM-dd") + " 12:00:00";
            nowCalendar.add(Calendar.DAY_OF_MONTH, 1);
            createTimes[1] = DateFormatUtils.format(nowCalendar.getTime(), "yyyy-MM-dd") + " 11:59:59";
        }
        return createTimes;
    }

    public static void main(String[] args) {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DAY_OF_MONTH,-1);
        for (String s : DateUtils.getOrderCycleDateStrings(instance.getTime())) {
            System.out.println(s);
        }
    }
}
