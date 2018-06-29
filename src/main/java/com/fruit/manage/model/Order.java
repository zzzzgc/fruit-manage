package com.fruit.manage.model;

import com.fruit.manage.constant.OrderPayStatusCode;
import com.fruit.manage.constant.OrderStatusCode;
import com.fruit.manage.model.base.BaseOrder;
import com.fruit.manage.util.ZhioDateUtils;
import com.fruit.manage.util.ZhioIdUtil;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class Order extends BaseOrder<Order> {
    public static final Order dao = new Order().dao();


    /**
     * 获取订单
     *
     * @param orderId
     * @return
     */
    public Order getOrder(String orderId) {
        String sql = "SELECT * FROM b_order o WHERE o.order_id = ? ";
        return dao.findFirst(sql, orderId);
    }

    /**
     * 分页获取数据
     *
     * @param order
     * @param createTime
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @param isASC
     * @return
     */
    public Page<Order> getData(Order order, String[] createTime, int pageNum, int pageSize, String orderBy, boolean isASC) {
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append("FROM b_order_detail AS od INNER JOIN b_order AS o ON od.order_id = o.order_id WHERE 1 = 1 ");
        if (StrKit.notBlank(order.getOrderId())) {
            sql.append("and o.order_id = ? ");
            params.add(order.getOrderId());
        }
        if (order.get("product_id") != null && StrKit.notBlank(order.get("product_id").toString())) {
            sql.append("and od.product_id = ? ");
            params.add(order.get("productId"));
        }
        if (order.getUId() != null && order.getUId() > 0) {
            sql.append("and od.buy_uid = ? ");
            params.add(order.getUId());
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() >= 0) {
            sql.append("and o.order_status  = ? ");
            params.add(order.getOrderStatus());
        }
        if (ArrayUtils.isNotEmpty(createTime) && createTime.length == 2) {
            sql.append("and o.create_time BETWEEN ? and ? ");
            params.add(createTime[0] + " 00:00:00");
            params.add(createTime[1] + " 23:59:59");
        }
        orderBy = StrKit.isBlank(orderBy) ? "o.create_time" : orderBy;
        sql.append("order by " + orderBy + " " + (isASC ? "" : "desc "));

        System.out.println("select * " + sql.toString());
        return paginate(pageNum, pageSize, "select * ", sql.toString(), params.toArray());
    }

    /**
     * 不同状态订单的展示
     *
     * @param orderStatus
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @param isASC
     * @return
     */
    public Page<Order> getOtherData(String orderStatus, int pageNum, int pageSize, String orderBy, boolean isASC, Map map) {
        ArrayList<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        String selectStr = "SELECT\n" +
                "\to.order_status,\n" +
                "\to.pay_need_money,\n" +
                "\to.pay_total_money,\n" +
                "\to.pay_all_money,\n" +
                "\to.pay_status,\n" +
                "\to.pay_success,\n" +
                "\to.create_time,\n" +
                "\to.update_time,\n" +
                "\to.order_id,\n" +
                "\tau.nick_name AS a_user_sales,\n" +
                "\tinfo.business_name,\n" +
                "\tinfo.id AS business_info_id,\n" +
                "\tu.id AS business_user_id,\n" +
                "\tCONCAT(\n" +
                "\t\tinfo.address_province,\n" +
                "\t\tinfo.address_city,\n" +
                "\t\tinfo.address_detail\n" +
                "\t) AS address, " +
                " o.pay_reality_need_money ";
        sql.append("FROM\n" +
                "\tb_order AS o\n" +
                "INNER JOIN b_business_user AS u ON o.u_id = u.id\n" +
                "INNER JOIN b_business_info AS info ON u.id = info.u_id\n" +
                "INNER JOIN a_user AS au ON u.a_user_sales_id = au.id " +
                "INNER JOIN b_order_detail as od on od.order_id = o.order_id " +
                "INNER JOIN b_product as p on p.id = od.product_id " +
                "inner join b_product_standard ps  on ps.product_id=p.id " +
                "WHERE 1 = 1 ");
        // 判断是否为待付款 25:为待付款
        if ("25".equals(orderStatus)) {
            sql.append("AND o.pay_status = 0 and o.order_status <> 50 ");
        } else {
            // 必须添加订单订单状态
            sql.append("AND o.order_status = ? ");
            params.add(orderStatus);
        }

        String noStr = "全部";
        if (StrKit.notBlank((String) map.get("searchProvince")) && !noStr.equals(map.get("searchProvince"))) {
            sql.append("and info.address_province LIKE ? ");
            params.add("%" + map.get("searchProvince") + "%");
        }
        if (StrKit.notBlank((String) map.get("searchCity")) && !noStr.equals(map.get("searchCity"))) {
            sql.append("and info.address_city LIKE ? ");
            params.add("%" + map.get("searchCity") + "%");
        }
        if (StrKit.notBlank((String) map.get("customerName"))) {
            sql.append("and u.`name` LIKE ? ");
            params.add("%" + map.get("customerName") + "%");
        }
        if (StrKit.notBlank((String) map.get("customerPhone"))) {
            sql.append("and u.phone LIKE ? ");
            params.add("%" + map.get("customerPhone") + "%");
        }

        if (StrKit.notBlank((String) map.get("customerID"))) {
            sql.append("and u.id LIKE ? ");
            params.add("%" + map.get("customerID") + "%");
        }
        if (StrKit.notBlank((String) map.get("productName"))) {
            sql.append("and p.`name` LIKE ? ");
            params.add("%" + map.get("productName") + "%");
        }
        if (StrKit.notBlank((String) map.get("productID"))) {
            sql.append("and p.id LIKE ? ");
            params.add("%" + map.get("productID") + "%");
        }
        if (StrKit.notBlank((String) map.get("standardName"))) {
            sql.append("and ps.`name` LIKE ? ");
            params.add("%" + map.get("standardName") + "%");
        }
        if (StrKit.notBlank((String) map.get("standardID"))) {
            sql.append("and ps.id LIKE ? ");
            params.add("%" + map.get("standardID") + "%");
        }
        if (StrKit.notBlank((String) map.get("businessInfoName"))) {
            sql.append("and info.business_name like ? ");
            params.add("%" + map.get("businessInfoName") + "%");
        }
        if (StrKit.notBlank((String) map.get("businessInfoID"))) {
            sql.append("and info.id like ? ");
            params.add("%" + map.get("businessInfoID") + "%");
        }
        if (ArrayUtils.isNotEmpty((String[]) map.get("createTime")) && ((String[]) map.get("createTime")).length == 2) {
            sql.append("and o.create_time BETWEEN ? and ? ");
            String startDate = ((String[]) map.get("createTime"))[0] + " 00:00:00";
            String endDate = ((String[]) map.get("createTime"))[1] + " 23:59:59";
            params.add(startDate);
            params.add(endDate);
        }
        if (StrKit.notBlank((String) map.get("orderId"))) {
            sql.append(" and o.order_id like ? ");
            params.add("%" + map.get("orderId") + "%");
        }
        sql.append("GROUP BY o.order_id ");
        orderBy = StrKit.isBlank(orderBy) ? "o.create_time" : orderBy;
        sql.append("order by " + orderBy + " " + (isASC ? "" : "desc "));
        System.out.println(selectStr + sql.toString());
        return paginate(pageNum, pageSize, selectStr, sql.toString(), params.toArray());
    }

    /**
     * 订单编辑或订单详细
     *
     * @param orderId
     * @return
     */
    public Order getOtherDataInfo(String orderId) {
        String selectStr = "SELECT\n" +
                "\tau.id AS business_user_id,\n" +
                "\tau.`name` AS a_user_sales,\n" +
                "\to.id,\n" +
                "\to.u_id,\n" +
                "\to.order_id,\n" +
                "\to.order_status,\n" +
                "\to.create_time,\n" +
                "\to.pay_need_money,\n" +
                " o.pay_reality_need_money," +
                " o.pay_all_money,"+
                "\tinfo.business_name,\n" +
                "\tli.buy_address,\n" +
                "\tli.buy_phone,\n" +
                "\tli.buy_user_name,\n" +

                //查询实际支付总额
                " o.pay_total_money, " +
                " li.delivery_info, " +
                " li.license_plate_number, " +
                " info.business_contacts, " +
                " info.phone as contactPhone, " +

                "\tli.delivery_type ";

        StringBuffer sql = new StringBuffer();
        sql.append("FROM\n" +
                "\tb_order AS o \n" +
                "INNER JOIN b_business_user AS u ON o.u_id = u.id\n" +
                // 不能用userId关联两表,而是要用order_id关联两表
                "LEFT JOIN b_logistics_info AS li ON o.order_id = li.order_id\n" +
//                "LEFT JOIN b_logistics_info AS li ON o.u_id = li.u_id\n" +

                "INNER JOIN b_business_info AS info ON u.id = info.u_id\n" +
                "INNER JOIN a_user AS au ON u.a_user_sales_id = au.id\n" +
                "WHERE\n" +
                "\t1 = 1 ");
        sql.append("AND o.order_id = ? ");
        System.out.println(selectStr + sql.toString());
        return dao.findFirst(selectStr + sql, orderId);
    }

    /**
     * 根据订单编号修改支付状态
     *
     * @param payStatus
     * @param orderId
     * @return
     */
    public boolean updateOrderPayStatus(Integer payStatus, BigDecimal payTotalMoney, String orderId) {
        String sql = "update b_order set pay_status=?,pay_total_money=? where order_id = ?";
        if (Db.update(sql, payStatus, payTotalMoney, orderId) > 0) {
            return true;
        }
        return false;
    }

    /**
     * 根据订单修改订单状态
     *
     * @param orderId
     * @return
     */
    public boolean updateOrderStatus(String orderId){
        String sql="update b_order o set order_status = "+OrderStatusCode.IS_OK.getStatus()+" where o.order_id=? and o.order_status = "+OrderStatusCode.TAKE_DISTRIBUTION.getStatus();
        if(Db.update(sql,orderId)>0)
            return true;
        return false;
    }

    /**
     * 获取客户的所有未付款(包含部分付款)订单信息和客户信息
     */
    public List<Order> getCustomerOrderInfo(Integer customerId) {
        String sql = "SELECT " +
                "CONCAT( " +
                "bi.address_province, " +
                "bi.address_city, " +
                "bi.address_detail " +
                ") AS address, " +
                "bu.nick_name AS bName, " +
                "au.nick_name AS aName, " +
                "bu.phone " +
                "FROM " +
                "b_business_user bu " +
                "INNER JOIN a_user au ON bu.a_user_sales_id = au.id " +
                "INNER JOIN b_business_info bi ON bi.u_id = bu.id " +
                "WHERE " +
                "bu.id = ? ";
        return Order.dao.find(sql, customerId);
    }

    /**
     * 根据订单编号获取订单的状态
     *
     * @param orderId 订单编号
     * @return
     */
    public Integer getOrderStatusByOrderId(String orderId) {
        String sql = "select order_status from b_order where order_id = ?";
        return Db.queryInt(sql, orderId);
    }

    /**
     * 获取用户的信息
     *
     * @param customerId 用户id
     * @return
     */
    public List<Order> getCustomerPayOrderInfo(Integer customerId) {
        String sql = "SELECT " +
                "o.create_time, " +
                "au.nick_name, " +
                "o.order_id, " +
                "o.order_status, " +
                "o.pay_all_money, " +
                "o.pay_total_money, " +
                "o.pay_all_money - o.pay_total_money as arrearage " +
                "FROM " +
                "b_business_user bu " +
                "INNER JOIN a_user au ON bu.a_user_sales_id = au.id " +
                "INNER JOIN b_order o ON o.u_id = bu.id " +
                "WHERE " +
                "o.order_status IN (15, 20, 25) " +
                "AND o.pay_status = " + OrderPayStatusCode.WAIT_PAYMENT.getStatus() + " " +
                "AND bu.id = ? " +
                "ORDER BY o.create_time ASC";
        return dao.find(sql, customerId);
    }

    /**
     * 获取用户的信息
     *
     * @param customerId 用户id
     * @return
     */
    public List<Order> getCustomerPayOrderInfo2(Integer customerId) {
        String sql = "SELECT " +
                "o.*," +
                "bu.a_user_sales_id " +
                "FROM " +
                "b_business_user bu " +
                "INNER JOIN b_order o ON o.u_id = bu.id " +
                "WHERE " +
                "o.order_status IN (15, 20, 25) " +
                "AND bu.id = ? " +
                "AND o.pay_status = " + OrderPayStatusCode.WAIT_PAYMENT.getStatus() + " " +
                "ORDER BY o.create_time ASC";
        return dao.find(sql, customerId);
    }

    /**
     * 封装添加订单实体
     *
     * @return
     */
    public Order addOrder(Integer bUserId, Date CreateTime) {
        Order order = new Order();
        order.setUId(bUserId);
        order.setOrderId(ZhioIdUtil.getOrderId(CreateTime,bUserId));
        order.setPayNeedMoney(new BigDecimal(0));
        order.setPayTotalMoney(new BigDecimal(0));
        order.setOrderCycleDate(ZhioDateUtils.getOrderCycleDate(CreateTime));
        order.setCreateTime(CreateTime);
        order.setUpdateTime(new Date());
        order.save();

//        order.setPayStatus(payStatus);
//        order.setPayNeedMoney(PayNeedMoney);
//        order.setPayTotalMoney(PayTotalMoney);
//        order.setPaySuccess(PaySuccess);
//        order.setpay_id(uid);
//        order.setpay_callback(uid);
//        order.setrefund_status(uid);
//        order.setpay_time(uid);
//        order.setrefund_time(uid);
//        order.setpay_reality_need_money(uid);
//        order.setpay_logistics_money(uid);
//        order.setpay_all_money(uid);
        return order;
    }
}
