package com.fruit.manage.constant;

public enum UserTypeConstant {
    /**
     * 0后台(a_user)
     */
    A_USER(0),
    /**
     * 1商户(b_business_user)
     */
    B_USER(1),
    /**
     * 2会员(c_user)
     */
    C_USER(2),
    /**
     * 3未知异常(待定)
     */
    UNKNOWN_USER(3),

    ;

    Integer status;

    UserTypeConstant(Integer status) {
        this.status = status;
    }

    public Integer getValue() {
        return status;
    }
}
