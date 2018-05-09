package com.fruit.manage.constant;

/**
 * @author partner
 * @date 2018/5/8 17:37
 */
public enum PayOfTypeStatusCode {
    //0:微信支付，1：支付宝支付，2：银行转账
    WECHAT_PAY(0,"微信支付"),
    ALIPAY(1,"支付宝支付"),
    BLANK_PAY(2,"银行转账"),;



    Integer status;
    String str;

    PayOfTypeStatusCode(Integer status, String str) {
        this.status = status;
        this.str = str;
    }

    public static String getPayType (Integer status) {
        for (PayOfTypeStatusCode payOfType : PayOfTypeStatusCode.values()) {
            if (payOfType.getStatus().equals(status)) {
                return payOfType.getStr();
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getStr() {
        return str;
    }
}
