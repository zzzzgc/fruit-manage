package com.fruit.manage.model;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.model.base.BaseProductStandard;
import com.fruit.manage.util.Common;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class ProductStandard extends BaseProductStandard<ProductStandard> {
    public static final ProductStandard dao = new ProductStandard().dao();
    public static final int STATUS_USE = 1;
    public static final int STATUS_UNUSED = 0;
    public static final int STATUS_DELETE = -1;

    public List<ProductStandard> list(int productId, String prop, boolean desc) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id,name,original_price,sell_price,");
        sql.append("weight_price,shipping_fee,purchase_quantity_min,purchase_quantity_max,");
        sql.append("buy_start_time,buy_end_time,status,is_default ");
        sql.append("FROM b_product_standard WHERE product_id=? and status!=-1 ");
        sql.append("order by ");
        sql.append(prop);
        if (desc) {
            sql.append(" desc ");
        }
        return find(sql.toString(), productId);
    }

    public boolean changeStatus(Integer[] productIds, int status) {
        Db.update("update b_product_standard set status=? where product_id in (" + Common.arrayToSqlIn(productIds) + ") ", status);
        return true;
    }

    public boolean changeStatusOne(int productId, Integer[] ids, int status) {
        return Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                Db.update("update b_product_standard set status=? where id in (" + Common.arrayToSqlIn(ids) + ")", status);
                if (status == STATUS_USE) {// 如果为启用并且商品为不启用状态，则设置商品为启用状态
                    Db.update("update b_product set status=?,update_time=now() where id=? and status!=? "
                            , Product.STATUS_USE, productId, Product.STATUS_USE);
                } else {// 如果规格没有上线状态的，则设置商品为下架状态
                    StringBuilder sql = new StringBuilder();
                    sql.append("UPDATE b_product p LEFT JOIN b_product_standard ps ON (ps.status=? AND p.id=ps.product_id) ")
                            .append("SET p.status=?,p.update_time=now() ")
                            .append("WHERE p.id=? AND p.status=? AND ps.id IS NULL ");
                    Db.update(sql.toString(), STATUS_USE, Product.STATUS_UNUSED, productId, Product.STATUS_USE);
                }
                return true;
            }
        });

    }

    public boolean save(ProductStandard productStandard) {
        return Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                productStandard.setUpdateTime(new Date());
                if (productStandard.getIsDefault() == 1) {// 将其他默认规格去除
                    Db.update("update b_product_standard set is_default=0 where product_id=?", productStandard.getProductId());
                }
                boolean result = false;
                if (productStandard.getId() != null) {
                    result = productStandard.update();
                } else {
                    productStandard.setCreateTime(new Date());
                    result = productStandard.save();
                }
                if (!result) {
                    return false;
                }
                Product product = Product.dao.getById(productStandard.getProductId());
                product.setUpdateTime(new Date());// 用于微信前端显示商品最近更新时间
                if (productStandard.getStatus() == STATUS_USE && product.getStatus() != Product.STATUS_USE) {// 如果为启用并且商品为不启用状态，则设置商品为启用状态
                    product.setStatus(Product.STATUS_USE);
                }
                return product.update();
            }
        });
    }

    /**
     * 获取商品的必要的规格信息
     *
     * @param productId
     * @return
     */
    public List<ProductStandard> getProductIdStandardsInfo(String productId) {
        String sql = "SELECT\n" +
                "\tps.gross_weight,\n" +
                "\tps.sell_price,\n" +
                "\tps.sell_price AS original_price,\n" +
                "\tps.id AS product_standard_id,\n" +
                "\tps.`name` AS product_standard_name,\n" +
                "\tps.product_id,\n" +
                "\tp.measure_unit,\n" +
                "\tp.`name` AS product_name,\n" +
                "\tp.brand\n" +
                "FROM\n" +
                "\tb_product_standard AS ps\n" +
                "INNER JOIN b_product AS p ON ps.product_id = p.id\n" +
                "AND p.id = ? ";
        return dao.find(sql, productId);
    }

    public List<ProductStandard> getProductStandardAllInfo() {
        return find("SELECT\n" +
                "\tp.`name` AS product_name,\n" +
                "\tp.id AS product_id,\n" +
                "\tps.`name` AS product_standard_name,\n" +
                "\tps.id AS product_standard_id\n" +
                "FROM\n" +
                "\tb_product_standard AS ps\n" +
                "INNER JOIN b_product AS p ON ps.product_id = p.id");
    }


    /**
     * 根据商品规格ID获取规格信息
     * @param productStandardId 规格编号
     * @return 规格商品
     */
    public ProductStandard getProductStandardById(Integer productStandardId){
        StringBuilder sql =new StringBuilder() ;
        sql.append("SELECT  ps.id,ps.product_id,ps.`name`,ps.sub_title,ps.original_price,ps.sell_price, ");
        sql.append("ps.weight_price,ps.cost_price,ps.shipping_fee,ps.carton_weight,ps.fruit_weight, ");
        sql.append("ps.gross_weight,ps.purchase_quantity_min,ps.purchase_quantity_max,ps.buy_start_time, ");
        sql.append("ps.buy_end_time,ps.sort_purchase,ps.sort_sold_out,ps.sort_split,ps.stock,ps.`status`,ps.is_default, ");
        sql.append("ps.purchaser_uid,ps.create_time,ps.update_time ");
        sql.append("from b_product_standard ps where ps.id = ? ");
        return findFirst(sql.toString(),productStandardId);
    }

    /**
     * 仓库（warehouse）的构造函数的封装
     * @param type
     * @param userId
     * @param productStandardId
     * @param changeNum
     * @return
     */
    public WarehouseLog getWarehouseLog(UserTypeConstant type,Integer userId,Integer productStandardId,Integer changeNum){
        WarehouseLog warehouseLog=new WarehouseLog();
        warehouseLog.setUserId(userId);
        warehouseLog.setUserType(type.getValue());
        warehouseLog.setProductStandardId(productStandardId);
        warehouseLog.setChangeNum(changeNum);
        warehouseLog.setCreateTime(new Date());
        return warehouseLog;
    }


    @Before(Tx.class)
    public boolean delete(UserTypeConstant type,Integer userId) {
        super.delete();
        return getWarehouseLog(type,userId,super.getId(),~super.getStock()+1).save();
    }

    @Before(Tx.class)
    public boolean save(UserTypeConstant type,Integer userId) {
        super.save();
        return getWarehouseLog(type,userId,super.getId(),super.getStock()).save();
    }

    @Before(Tx.class)
    public boolean update(UserTypeConstant type,Integer userId,Integer afterNum,Integer beforeNum) {
        super.update();
        return getWarehouseLog(type,userId,super.getId(),afterNum-beforeNum).save();
    }

    // 修改完状态，刷新商品列表
    // 保存完也刷新列表

}
