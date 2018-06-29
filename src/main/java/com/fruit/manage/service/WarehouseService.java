package com.fruit.manage.service;

import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.model.CheckInventory;
import com.fruit.manage.model.CheckInventoryDetail;
import com.fruit.manage.model.ProductStandard;
import com.jfinal.aop.Before;
import com.jfinal.ext.kit.DateKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Integer getStock(String orderCycleDate, Integer productStandardId) {
        Record first = Db.findFirst("SELECT   " +
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
                "ORDER BY stock desc", orderCycleDate, productStandardId);
        if (first == null) {
            return null;
        }
        return first == null ? null : first.getInt("stock");
    }

    /**
     * 更新盘点单详细表
     * 无库存商品也需要存在盘点单详细表上。便于统计完全出库并包含昨日库存的商品单价
     * 新增盘点单表的时候调用
     *
     * @param orderCycleDateStr 运营周期时间
     */
    @Before(Tx.class)
    public void updateCheckInventoryDetail(String orderCycleDateStr,String userName) {
        // 需要包含昨日库存和今日入库的商品(可能又会有新品库存)

        // 今日盘点表
        CheckInventory nowCheckInventory = CheckInventory.dao.getCheckInventoryByOrderCycleDate(orderCycleDateStr);

        Calendar instance = Calendar.getInstance();
        instance.setTime(DateKit.toDate(orderCycleDateStr));
        instance.add(Calendar.HOUR_OF_DAY, -1);
        Date yesterdayOrderCycleDate = instance.getTime();

        // 昨日业务周期
        String yesterdayOrderCycleDateStr = DateKit.toStr(yesterdayOrderCycleDate, "yyyy-MM-dd");

        // 昨日库存商品
        List<Record> records = Db.find("SELECT  " +
                "  cid.product_standard_id,  " +
                "  cid.product_standard_name,  " +
                "  cid.product_id,  " +
                "  cid.product_name,  " +
                "  cid.product_weight as product_weight,  " +
                "  cid.user_name,  " +
                "  cid.inventory_remark  " +
                "FROM  " +
                "  b_check_inventory ci  " +
                "INNER JOIN b_check_inventory_detail cid ON ci.id = cid.check_inventory_id  " +
                "WHERE  " +
                "  ci.order_cycle_date = ?", yesterdayOrderCycleDateStr);
        Set<Record> collect = records.stream().collect(Collectors.toSet());

        // 今日入库商品
        List<Record> records2 = Db.find("SELECT  " +
                "  pwd.product_standard_id,  " +
                "  pwd.product_id,  " +
                "  pwd.product_standard_name,  " +
                "  pwd.product_name,  " +
                "  pwd.product_weight  " +
                "FROM  " +
                "  b_put_warehouse pw  " +
                "INNER JOIN b_put_warehouse_detail pwd ON pwd.put_id = pw.id  " +
                "WHERE  " +
                "  pw.order_cycle_date = ?", orderCycleDateStr);
        Set<Record> collect2 = records2.stream().collect(Collectors.toSet());

        collect.addAll(collect2);

        if (collect != null && collect.size() > 0) {
            for (Record record : collect) {
                Integer product_standard_id = record.get("product_standard_id");
                String product_standard_name = record.get("product_standard_name");
                Integer product_id = record.get("product_id");
                String product_name = record.get("product_name");
                String productWeight = record.get("product_weight");

                String remark = record.get("inventory_remark");
                CheckInventoryDetail.dao.addOrUpdateCheckInventoryDetail(
                        nowCheckInventory.getId(),
                        orderCycleDateStr,
                        yesterdayOrderCycleDateStr,
                        product_name,
                        product_standard_name,
                        product_standard_id,
                        product_id,
                        productWeight,
                        userName,
                        0,
                        remark
                );
            }
        }

    }
}
