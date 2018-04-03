package com.fruit.manage.service;

import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.model.ProductStandard;

/**
 * @author partner
 * @date 2018/3/30 14:28
 */
public class WarehouseService {

    /**
     * 修改库存
     * @param type UserTypeConstant 枚举类型
     * @param userId 用户编号
     * @param afterNum 修改后的库存
     * @return 返回0（成功获取到数据返回） 、 1（获取到null数据）、 -1（出异常了）
     */
    public Integer update(Integer productStandardId,UserTypeConstant type,Integer userId,Integer afterNum,String changeType){
        try {
            // 根据商品规格编号获取规格信息
            ProductStandard productStandard = ProductStandard.dao.getProductStandardById(productStandardId);
            // 如果存在该商品则修改
            if(productStandard!=null){
                productStandard.update(type,userId,afterNum,productStandard.getStock(),changeType);
                return 0;
            }
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }
}
