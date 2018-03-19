package com.fruit.manage.constant;

import java.util.LinkedHashMap;

public class ShipmentConstant {

    /**
     * LinkedHashMap可以按存入顺序输出是获取下一个状态码的关键.   声明规范:该流程必须按流程顺序排列,且尽量从不去更改
     * 订单状态常量集合
     */
    public static LinkedHashMap<Integer,String> SHIPMENT_TYPE;
    static {
        SHIPMENT_TYPE=new LinkedHashMap<>();

        // 物流方式 0-市场车， 1-物流， 2-自提
        SHIPMENT_TYPE.put(0,"市场车");
        SHIPMENT_TYPE.put(1,"物流");
        SHIPMENT_TYPE.put(2,"自提");
    }
    /**
     * 获取下一个流程的状态
     * @return 下一个流程的状态码
     */
    public static Integer nextStatus(Integer status) {
        boolean isNextData = false;
        for (Integer integer : SHIPMENT_TYPE.keySet()) {
            if (isNextData) {
                return integer;
            }
            if (integer.equals(status)) {
                isNextData = true;
            }
        }
        if (isNextData) {
            throw new RuntimeException("这是最后一个订单状态码了");

        } else {
            throw new RuntimeException("订单状态码不存在");
        }
    }

    public static void main(String[] args) {
        System.out.println(nextStatus(1));
    }
}
