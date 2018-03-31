package com.fruit.manage.constant;

import com.fruit.manage.model.ProductStandard;

/**
 * @author partner
 * @date 2018/3/30 14:33
 */
public enum  ProductStandardConstant {
    // 仓库操作类型: 0 入库 10 出库

    /**
     * 入库 0
     */
    PUT_IN_STORE(0),
    /**
     * 出库 10
     */
    OUTPUT_FROM_STORE(10),;

    Integer status;

    ProductStandardConstant(Integer status){this.status = status;}

    public Integer getStatus() {return status;}
}
