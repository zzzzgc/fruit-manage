package com.fruit.manage.model;

import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.model.base.BaseOrderDetail;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
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
        // 实发数量默认等于应发数量,再由用户修改
        String sql = "SELECT\n" +
                "\to.product_name,\n" +
                "\to.id,\n" +
                "\to.product_standard_name,\n" +
                "\to.num,\n" +
                "\to.original_price,\n" +
                "\to.sell_price,\n" +
                "\to.actual_deliver_num,\n" +
                " b_o.pay_reality_need_money," +
                " ps.stock, " +
                " o.product_standard_id, " +
                "\to.actual_send_goods_num AS actual_send_goods_num,\n" +
                "\tp.brand,\n" +

                //商户规格副标题
                " ps.sub_title, " +
                " o.buy_remark, " +

                "\tps.gross_weight\n" +
                "FROM\n" +
                "\tb_order_detail AS o\n" +
                "INNER JOIN b_order AS b_o ON o.order_id= b_o.order_id\n" +
                "INNER JOIN b_product AS p ON o.product_id = p.id\n" +
                "INNER JOIN b_product_standard AS ps ON o.product_standard_id = ps.id\n" +
                "WHERE\n" +
                "\to.order_id = ?  ";
        return dao.find(sql, orderid);
    }

    /**
     * 根据订单编号获取订单详情单个表操作
     *
     * @param orderId 订单ID
     * @return 订单详情集合
     */
    public List<OrderDetail> getOrderDetailSingleTable(String orderId) {
        String sql = "select od.id,od.product_standard_id,od.actual_send_goods_num from b_order_detail od where od.order_id = ? ";
        return find(sql, orderId);
    }

    private OrderLog getOrderLog(UserTypeConstant type, Integer uid, String orderId, Integer productId, Integer productStandardId, Integer changeNum, Date orderCreateTime) {
        OrderLog orderLog = new OrderLog();
        orderLog.setUId(uid);
        orderLog.setUserType(type.getValue());
        orderLog.setOrderId(orderId);
        orderLog.setProductId(productId);
        orderLog.setProductStandardId(productStandardId);
        orderLog.setChangeNum(changeNum);
        orderLog.setOrderCreateTime(orderCreateTime);
        orderLog.setCreateTime(new Date());
        return orderLog;
    }

    /**
     * 根据订单编号获取总额=（子订单=售价*下单量）的和
     *
     * @param orderId
     * @return
     */
    public BigDecimal getOrderTotalCost(String orderId) {
        String sql = "select sum(od.total_pay) from b_order_detail od where od.order_id= ? ";
        return Db.queryBigDecimal(sql, orderId);
    }

    /**
     * 根据订单编号获取实际需要支付的订单总金额 = (子订单=售价*实际发货数量)的和
     *
     * @param orderId
     * @return
     */
    public BigDecimal getOrderPayRealityNeedMoneyByOrderID(String orderId) {
//         2018-05-28 zgc 应收金额应该是pay_all_money(总货款=订单总金额+物流费用) 而不是 pay_reality_need_money(订单总金额)
//        String sql = "select o.pay_reality_need_money from b_order  o where o.order_id = ?";
        String sql = "select o.pay_all_money from b_order  o where o.order_id = ?";
        return Db.queryBigDecimal(sql, orderId);
    }

    /**
     * 根据订单详细编号获取订单详细
     *
     * @param id 订单详细编号
     * @return
     */
    public OrderDetail getOrderDetailById(Integer id) {
        String sql = "SELECT * from b_order_detail where id = ? ";
        return findFirst(sql, id);
    }

    private OrderLog getOrderLog(String orderId, Integer productId, Integer productStandardId, Integer changeNum, Date orderCreateTime) {
        OrderLog orderLog = new OrderLog();
        // 未知用户
        orderLog.setUserType(UserTypeConstant.UNKNOWN_USER.getValue());
        orderLog.setOrderId(orderId);
        orderLog.setProductId(productId);
        orderLog.setProductStandardId(productStandardId);
        orderLog.setChangeNum(changeNum);
        orderLog.setOrderCreateTime(orderCreateTime);
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
        String sql = " SELECT o.create_time from b_order o INNER JOIN b_order_detail od on o.order_id=od.order_id where od.id = ? ";
        Date orderCreateTime = Db.queryDate(sql, super.getOrderId());
        super.delete();
        return getOrderLog(super.getOrderId(), super.getProductId(), super.getProductStandardId(), ~super.getNum() + 1, orderCreateTime).save();
    }

    @Deprecated
    @Override
    @Before(Tx.class)
    public boolean update() {
        OrderDetail orderDetail = OrderDetail.dao.findById(super.getId());
        String sql = " SELECT o.create_time from b_order o INNER JOIN b_order_detail od on o.order_id=od.order_id where od.id = ? ";
        Date orderCreateTime = Db.queryDate(sql, super.getOrderId());
        super.update();
        return getOrderLog(orderDetail.getOrderId(), orderDetail.getProductId(), orderDetail.getProductStandardId(), super.getNum() - orderDetail.getNum(), orderCreateTime).save();
    }


    /**
     * 推荐
     */
    @Before(Tx.class)
    public boolean save(UserTypeConstant type, Integer uid, Date orderCreateTime) {
        super.save();
        // 新增商家购买数量
        Product.dao.updateSellNum(super.getProductId(),1);
        return getOrderLog(type, uid, super.getOrderId(), super.getProductId(), super.getProductStandardId(), super.getNum(), orderCreateTime).save();
    }

    @Before(Tx.class)
    public boolean delete(UserTypeConstant type, Integer uid, Date orderCreateTime) {
        super.delete();
        return getOrderLog(type, uid, super.getOrderId(), super.getProductId(), super.getProductStandardId(), ~super.getNum() + 1, orderCreateTime).save();
    }

    @Before(Tx.class)
    public boolean update(UserTypeConstant type, Integer uid, Date orderCreateTime) {
        OrderDetail orderDetail = OrderDetail.dao.findById(super.getId());
        super.update();
        return getOrderLog(type, uid, orderDetail.getOrderId(), orderDetail.getProductId(), orderDetail.getProductStandardId(), super.getNum() - orderDetail.getNum(), orderCreateTime).save();
    }


    @Before(Tx.class)
    public boolean save(UserTypeConstant type, Integer uid, String orderId, Integer productId, Integer productStandardId, Integer num, Date orderCreateTime) {
        super.save();
        return getOrderLog(type, uid, orderId, productId, productStandardId, num, orderCreateTime).save();
    }

    /**
     * 推荐
     */
    @Before(Tx.class)
    public boolean delete(UserTypeConstant type, Integer uid, String orderId, Integer productId, Integer productStandardId, Integer num, Date orderCreateTime) {
        super.delete();
        return getOrderLog(type, uid, orderId, productId, productStandardId, ~num + 1, orderCreateTime).save();
    }

    /**
     * 推荐
     */
    @Before(Tx.class)
    public boolean update(UserTypeConstant type, Integer uid, String orderId, Integer productId, Integer productStandardId, Integer beforeNum, Integer afterNum, Date orderCreateTime) {
        super.setUpdateTime(new Date());
        super.update();
        return getOrderLog(type, uid, orderId, productId, productStandardId, afterNum - beforeNum, orderCreateTime).save();
    }

}
