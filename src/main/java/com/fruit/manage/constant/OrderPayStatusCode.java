package com.fruit.manage.constant;

public enum OrderPayStatusCode {
    // 支付状态，0-未付款；5-已支付；10-已退款，状态变更时这里可能需要修改

    /**
     * 未付款
     */
    WAIT_PAYMENT(0),
    /**
     * 已支付
     */
    IS_OK(5),
    /**
     * 已退款
     */
    REFUND(10),
    ;

    Integer status;

    OrderPayStatusCode(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
