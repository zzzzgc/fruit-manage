package com.fruit.manage.service;

import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.model.ProductStandard;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * @author partner
 * @date 2018/3/30 14:28
 */
public class WarehouseService {

    /**
     * 修改库存
     *
     * @param type     UserTypeConstant 枚举类型
     * @param userId   用户编号
     * @param afterNum 修改后的库存
     * @return 返回0（成功获取到数据返回） 、 1（获取到null数据）、 -1（出异常了）
     */
    public Integer update(Integer productStandardId, UserTypeConstant type, Integer userId, Integer afterNum, String changeType, String productStandardName, Integer productId, String productName) {
        try {
            // 根据商品规格编号获取规格信息
            ProductStandard productStandard = ProductStandard.dao.getProductStandardById(productStandardId);
            // 如果存在该商品则修改
            if (productStandard != null) {
                productStandard.update(type, userId, afterNum, productStandard.getStock(), changeType, productStandardName, productId, productName);
                return 0;
            }
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 获取历史库存
     */
    public Record getStock(String orderCycleDate,Integer productStandardId) {
        return Db.findFirst("SELECT   " +
                "  ps.id," +
                "  (  " +
                "    IFNULL(pwd.put_num, 0) - IFNULL(SUM(owd.out_num), 0) + IFNULL(cid.check_inventory_num, 0)  " +
                "  ) AS stock  " +
                "FROM  " +
                "  b_product_standard ps  " +
                "LEFT JOIN b_out_warehouse ow ON ow.order_cycle_date = ?  AND ps.id =  ?  " +
                "LEFT JOIN b_out_warehouse_detail owd ON owd.out_Id = ow.id AND owd.product_standard_id = ps.id  " +
                "LEFT JOIN b_put_warehouse pw ON pw.order_cycle_date = ow.order_cycle_date   " +
                "LEFT JOIN b_put_warehouse_detail pwd ON pwd.put_id = pw.id AND pwd.product_standard_id = ps.id  " +
                "right JOIN b_check_inventory ci ON DATE_ADD(  " +
                "    ci.order_cycle_date,  " +
                "    INTERVAL 1 DAY  " +
                "  ) = ow.order_cycle_date  " +
                "LEFT JOIN b_check_inventory_detail cid ON cid.check_inventory_id = ci.id AND  cid.product_standard_id = ps.id  " +
                "GROUP BY ps.id  " +
                "HAVING stock > 0  " +
                "ORDER BY stock desc", orderCycleDate,productStandardId);
    }
}
