package com.fruit.manage.constant;

import java.lang.reflect.Field;

public enum OrderStatusCode {

    // 商品状态，0-待确认；5-已确认；10-未配送；15-已配送；20-待付款；25-已完成(已配送 + 支付状态为=>已付款); 30-已退款; 40-已删除

    /**
     * 待确认
     */
    WAIT_AFFIRM(0),
    /**
     * 已确认
     */
    AFFIRM(5),
    /**
     * 未配送
     */
    WAIT_DISTRIBUTION(10),
    /**
     * 已配送
     */
    DISTRIBUTION(15),
    /**
     * 待付款
     */
    WAIT_PAYMENT(20),
    /**
     * 已完成
     */
    IS_OK(25),
    /**
     * 已退款
     */
    REFUND(30),
    /**
     * 已删除
     */
    DELETED(40),
    ;

    Integer status;

    OrderStatusCode(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
