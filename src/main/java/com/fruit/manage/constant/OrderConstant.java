package com.fruit.manage.constant;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author ZGC and LZZ
 * @date Created in 16:38 2018/3/16
 */
public class OrderConstant {

    /**
     * LinkedHashMap可以按存入顺序输出是获取下一个状态码的关键.   声明规范:该流程必须按流程顺序排列,且尽量从不去更改
     * 订单状态常量集合
     */
    public static LinkedHashMap<Integer, String> ORDER_STATUS_MAP;

    static {
        ORDER_STATUS_MAP = new LinkedHashMap<>();

        /**
         商品状态：
         0-待确认；
         5-已确认；
         10-未配送（已配货）；
         15-已配送；
         20-已送达；
         25-待付款；
         30-已完成(已配送 + 支付状态为=>已付款);
         40-已退款;
         50已删除
         */
        ORDER_STATUS_MAP.put(0, "待确认");
        ORDER_STATUS_MAP.put(5, "已确认");
        ORDER_STATUS_MAP.put(10, "已配货");
        ORDER_STATUS_MAP.put(15, "已配送");
        ORDER_STATUS_MAP.put(20, "已送达");
        ORDER_STATUS_MAP.put(25, "待付款");
        ORDER_STATUS_MAP.put(30, "已完成");
        ORDER_STATUS_MAP.put(40, "已退款");
        ORDER_STATUS_MAP.put(50, "已删除");
    }

    /**
     * 获取下一个流程的状态
     *
     * @return 下一个流程的状态码
     */
    public static Integer nextStatus(Integer status) {
        boolean isNextData = false;
        for (Integer localStatus : ORDER_STATUS_MAP.keySet()) {
            if (isNextData) {
                return localStatus;
            }
            if (localStatus.equals(status)) {
                isNextData = true;
            }
        }
        if (isNextData) {
            throw new RuntimeException("这是最后一个订单状态码了");
        } else {
            throw new RuntimeException("订单状态码不存在");
        }
    }

    /**
     * 获取上一个流程的状态
     *
     * @param orderStatus
     * @return
     */
    public static Integer rollbackStatus(Integer orderStatus) {
        Set<Integer> statusSet = ORDER_STATUS_MAP.keySet();
        Iterator<Integer> iterator = statusSet.iterator();
        Integer rollbackTemp = -1;
        while (iterator.hasNext()) {
            Integer next = iterator.next();
            if (next.equals(orderStatus)) {
                if (rollbackTemp != -1) {
                    return rollbackTemp;
                }
                throw new RuntimeException("不能再退了这是最初的状态了");
            }
            rollbackTemp = next;
        }
        throw new RuntimeException("不存在的状态码");
    }

    public static void main(String[] args) {
        System.out.println(nextStatus(15));
        System.out.println(rollbackStatus(15));
    }

}
