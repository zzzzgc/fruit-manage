package com.fruit.manage.constant;

import java.lang.reflect.Field;

public enum OrderStatusCode {

    // 商品状态：0-待确认；5-已确认；10-未配送（已配货）；15-已配送；20-已送达；25-待付款；30-已完成(已配送 + 支付状态为=>已付款);40-已退款; 50已删除

    /**
     * 待确认
     */
    WAIT_AFFIRM(0),
    /**
     * 已确认
     */
    AFFIRM(5),
    /**
     * 未配送(已配货)
     */
    WAIT_DISTRIBUTION(10),
    /**
     * 已配送
     */
    DISTRIBUTION(15),
    /**
     * 已送达
     */
    TAKE_DISTRIBUTION(20),
    /**
     * 待付款
     */
    WAIT_PAYMENT(25),
    /**
     * 已完成
     */
    IS_OK(30),
    /**
     * 已退款
     */
    REFUND(40),
    /**
     * 已删除
     */
    DELETED(50),
    ;

    Integer status;

    OrderStatusCode(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
