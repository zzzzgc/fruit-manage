package com.fruit.manage.model;

import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.model.base.BaseOrderDetail;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class OrderDetail extends BaseOrderDetail<OrderDetail> {
    public static final OrderDetail dao = new OrderDetail().dao();

    public List<OrderDetail> getOrderDetails(String orderId) {
        return dao.find("SELECT * FROM b_order_detail od WHERE od.order_id = ? ", orderId);
    }

    /**
     * 获取订单信息展示的部分 商品信息和商品规格
     *
     * @param orderid
     * @return
     */
    public List<OrderDetail> getOtherOrderDetail(String orderid) {
        String sql = "SELECT\n" +
                "\to.product_name,\n" +
                "\to.id,\n" +
                "\to.product_standard_name,\n" +
                "\to.num,\n" +
                "\to.original_price,\n" +
                "\to.sell_price,\n" +
                "\to.sell_price,\n" +

                "\to.actual_send_goods_num,\n" +

                "\tp.brand,\n" +
                "\tps.gross_weight\n" +
                "FROM\n" +
                "\tb_order_detail AS o\n" +
                "INNER JOIN b_product AS p ON o.product_id = p.id\n" +
                "INNER JOIN b_product_standard AS ps ON o.product_standard_id = ps.id\n" +
                "WHERE\n" +
                "\to.order_id = ?  ";
        return dao.find(sql, orderid);
    }

    private OrderLog getOrderLog(UserTypeConstant type, Integer uid, String orderId, Integer productId, Integer productStandardId, Integer changeNum) {
        OrderLog orderLog = new OrderLog();
        orderLog.setUId(uid);
        orderLog.setUserType(type.getValue());
        orderLog.setOrderId(orderId);
        orderLog.setProductId(productId);
        orderLog.setProductStandardId(productStandardId);
        orderLog.setChangeNum(changeNum);
        orderLog.setCreateTime(new Date());
        return orderLog;
    }

    /**
     * 根据订单编号获取总额
     * @param orderId
     * @return
     */
    public BigDecimal getOrderTotalCost(String orderId){
        String sql="select sum(od.total_pay) from b_order_detail od where od.order_id= ? ";
        return Db.queryBigDecimal(sql,orderId);
    }

    private OrderLog getOrderLog(String orderId, Integer productId, Integer productStandardId, Integer changeNum) {
        OrderLog orderLog = new OrderLog();
        // 未知用户
        orderLog.setUserType(UserTypeConstant.UNKNOWN_USER.getValue());
        orderLog.setOrderId(orderId);
        orderLog.setProductId(productId);
        orderLog.setProductStandardId(productStandardId);
        orderLog.setChangeNum(changeNum);
        orderLog.setCreateTime(new Date());
        return orderLog;
    }

    @Before(Tx.class)
    public boolean save(OrderLog orderLog) {
        super.save();
        return orderLog.save();
    }
    @Before(Tx.class)
    public boolean delete(OrderLog orderLog) {
        super.delete();
        return orderLog.save();
    }
    @Before(Tx.class)
    public boolean update(OrderLog orderLog) {
        super.update();
        return orderLog.save();
    }

    @Deprecated
    @Override
    @Before(Tx.class)
    public boolean save() {
        // 删除并添加删除的 x + orderId的订单的时候需要使用不计入日志的方法
        return super.save();
    }
    @Deprecated
    @Override
    @Before(Tx.class)
    public boolean delete() {
        super.delete();
        return getOrderLog(super.getOrderId(), super.getProductId(), super.getProductStandardId(), ~super.getNum() + 1).save();
    }
    @Deprecated
    @Override
    @Before(Tx.class)
    public boolean update() {
        OrderDetail orderDetail = OrderDetail.dao.findById(super.getId());
        super.update();
        return getOrderLog(orderDetail.getOrderId(), orderDetail.getProductId(), orderDetail.getProductStandardId(), super.getNum() - orderDetail.getNum()).save();
    }


    /**
     * 推荐
     */
    @Before(Tx.class)
    public boolean save(UserTypeConstant type, Integer uid) {
        super.save();
        return getOrderLog(type, uid, super.getOrderId(), super.getProductId(), super.getProductStandardId(), super.getNum()).save();
    }
    @Before(Tx.class)
    public boolean delete(UserTypeConstant type, Integer uid) {
        super.delete();
        return getOrderLog(type, uid, super.getOrderId(), super.getProductId(), super.getProductStandardId(), ~super.getNum() + 1).save();
    }
    @Before(Tx.class)
    public boolean update(UserTypeConstant type, Integer uid) {
        OrderDetail orderDetail = OrderDetail.dao.findById(super.getId());
        super.update();
        return getOrderLog(type, uid, orderDetail.getOrderId(), orderDetail.getProductId(), orderDetail.getProductStandardId(), super.getNum() - orderDetail.getNum()).save();
    }



    @Before(Tx.class)
    public boolean save(UserTypeConstant type, Integer uid, String orderId, Integer productId, Integer productStandardId, Integer num) {
        super.save();
        return getOrderLog(type, uid, orderId, productId, productStandardId, num).save();
    }
    /**
     * 推荐
     */
    @Before(Tx.class)
    public boolean delete(UserTypeConstant type, Integer uid, String orderId, Integer productId, Integer productStandardId, Integer num) {
        super.delete();
        return getOrderLog(type, uid, orderId, productId, productStandardId, ~num + 1).save();
    }
    /**
     * 推荐
     */
    @Before(Tx.class)
    public boolean update(UserTypeConstant type, Integer uid, String orderId, Integer productId, Integer productStandardId, Integer beforeNum, Integer afterNum) {
        super.setUpdateTime(new Date());
        super.update();
        return getOrderLog(type, uid, orderId, productId, productStandardId, afterNum - beforeNum).save();
    }
















}
