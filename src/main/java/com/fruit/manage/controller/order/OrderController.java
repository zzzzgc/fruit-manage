package com.fruit.manage.controller.order;

import com.alibaba.fastjson.JSON;
import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.OrderConstant;
import com.fruit.manage.constant.OrderStatusCode;
import com.fruit.manage.model.*;
import com.fruit.manage.util.Constant;
import com.jfinal.aop.Before;
import com.jfinal.ext2.kit.DateTimeKit;
import com.jfinal.ext2.kit.RandomKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZGC
 * @date Created in 17:59 2018/3/16
 */
public class OrderController extends BaseController {

    private Logger log = Logger.getLogger(getClass());

    private static long PAY_COUNT = 10000L;

    /**
     * 获取所有订单的列表数据
     */
    @RequiresPermissions("order:query")
    public void getData() {
        Order order = getModel(Order.class, "", true);
        log.info("订单搜索参数order=" + order);
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);

        String orderBy = getPara("prop");

        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));

        // 下单时间
        String[] orderTime = getParaValues("order_time");

        renderJson(Order.dao.getData(order, orderTime, pageNum, pageSize, orderBy, isASC));
    }

    /**
     * 获取订单的列表数据
     */
    @RequiresPermissions("order:query")
    public void getOtherData() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);

        String orderBy = getPara("prop");
        Map paramMap = new HashMap(20);
        // 省份
        paramMap.put("searchProvince", getPara("search_province"));
        // 城市
        paramMap.put("searchCity", getPara("search_city"));
        // 客户名称
        paramMap.put("customerName", getPara("customer_name"));
        // 客户电话
        paramMap.put("customerPhone", getPara("customer_phone"));
        //客户编号
        paramMap.put("customerID", getPara("customer_id"));
        // 商品名称
        paramMap.put("productName", getPara("product_name"));
        //商品编号
        paramMap.put("productID", getPara("product_id"));
        //规格名称
        paramMap.put("standardName", getPara("standard_name"));
        //规格编号
        paramMap.put("standardID", getPara("standard_id"));
        //开始时间和结束时间
        paramMap.put("createTime", getParaValues("format_create_time"));
        //商铺名称
        paramMap.put("businessInfoName", getPara("businessInfo_name"));
        // 商铺ID
        paramMap.put("businessInfoID", getPara("businessInfo_id"));

        String orderStatus = getPara("order_status");

        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));

        Page<Order> orderPage = Order.dao.getOtherData(orderStatus, pageNum, pageSize, orderBy, isASC, paramMap);

        for (Order order : orderPage.getList()) {
            String orderId = order.getOrderId();
            List<OrderDetail> products = OrderDetail.dao.getOrderDetails(orderId);
            order.put("products", products);
            // 格式化订单状态
            order.put("order_status", OrderConstant.ORDER_STATUS_MAP.get(order.getOrderStatus()));
        }
        renderJson(orderPage);
    }

    /**
     * 修改订单状态(0删除 1进入下一个订单流程)
     */
    @Before(Tx.class)
    public void setStatus() {
        Integer isNext = getParaToInt("isNext");
        String orderId = getPara("orderId");
        Order order = Order.dao.getOrder(orderId);
        if (isNext.equals(1)) {
            // 进入下一个流程
            Integer orderStatus = order.getOrderStatus();
            order.setOrderStatus(OrderConstant.nextStatus(orderStatus));
            order.update();
        } else {
            // 删除
            order.setOrderStatus(OrderStatusCode.DELETED.getStatus());
            order.update();
        }
        renderNull();
    }

    /**
     * 获取单笔订单信息(编辑订单)
     */
    public void getOtherDataInfo() {
        String orderId = getPara("orderId");
        Order order = Order.dao.getOtherDataInfo(orderId);
        List<OrderDetail> orderDetails = OrderDetail.dao.getOtherOrderDetail(orderId);
        order.put("products", orderDetails);
        renderJson(order);
    }

    /**
     * 保存添加的新订单
     */
    @Before(Tx.class)
    public void save() {
        Order order = getModel(Order.class, "", true);
        String products = getPara("products");
        Integer business_user_id = getParaToInt("business_user_id");
        List<OrderDetail> orderDetails = JSON.parseArray(products, OrderDetail.class);
        BigDecimal payNeedMoney = new BigDecimal(0);

        if (order.getOrderId() != null) {
            for (OrderDetail orderDetail : orderDetails) {
                Integer num = orderDetail.getNum();
                BigDecimal sellPrice = orderDetail.getSellPrice();
                // 该商品的真实支付总价
                BigDecimal totalPay = sellPrice.multiply(new BigDecimal(num));
                orderDetail.setTotalPay(totalPay);
                payNeedMoney = payNeedMoney.add(totalPay);
                if (orderDetail.getId() != null) {
                    orderDetail.update();
                } else {
                    orderDetail.setOrderId(order.getOrderId());
                    orderDetail.save();
                }
            }
            order.setPayNeedMoney(payNeedMoney);
            order.update();
        } else {
            //添加
            order.setOrderId(getNewOrderId());
            Date now = new Date();
            for (OrderDetail orderDetail : orderDetails) {
                Integer num = orderDetail.getNum();
                BigDecimal sellPrice = orderDetail.getSellPrice();
                // 该商品的真实支付总价
                BigDecimal totalPay = sellPrice.multiply(new BigDecimal(num));
                orderDetail.setTotalPay(totalPay);
                payNeedMoney = payNeedMoney.add(totalPay);
                orderDetail.setUpdateTime(now);
                orderDetail.setCreateTime(now);
                orderDetail.setBuyUid(business_user_id);
                orderDetail.setOrderId(order.getOrderId());
                orderDetail.save();
            }
            order.setUpdateTime(now);
            order.setCreateTime(now);
            order.setPayNeedMoney(payNeedMoney);
            order.setPayTotalMoney(new BigDecimal(0));
            order.setUId(business_user_id);
            order.save();
        }
        // 成功
        renderNull();
    }

    public void setStatusAll() {
        renderErrorText("目前不做该功能,避免出现没有订单操作的情况");
    }


    /**
     * 获取商户信息(添加订单)
     */
    public void getCustomers() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        // 销售只能获取自己的客户,其他人能获取全部,但是其他人除了超级管理员都不能调用这个方法,因为权限控制.
        // name必须为value ,是获取该值的关键(前端)
        String sql = "SELECT\n" +
                "\tu.`name` AS value,\n" +
                "\tu.id AS business_user_id,\n" +
                "\tu.phone,\n" +
                "\tu.nick_name,\n" +
                "\tu.a_user_sales_id AS sales_id\n" +
                "FROM\n" +
                "\tb_business_user AS u ";
        if (!User.dao.isSales(uid)) {
            renderJson(BusinessUser.dao.find(sql));
            return;
        }
        // 销售只能获取自己的客户
        renderJson(BusinessUser.dao.find(sql + " WHERE u.a_user_sales_id = ? ", uid));
        return;
    }

    /**
     * 获取商户信息(添加订单)
     */
    public void getCustomerInfo() {
        String customerId = getPara("customerId");
        renderJson(BusinessUser.dao.getCustomerInfo(customerId));
    }

    /**
     * 服务器查询商品信息,并返回供用户选择的商品信息(编辑)
     */
    public void getProductInfoByQuery() {
        String queryString = getPara("queryString");
        renderJson(Product.dao.getProductNameByQueryString(queryString));
    }

    /**
     * 获取商品信息(添加/修改订单)
     */
    public void getProductInfo() {
        String businessUserName = getPara("businessUserName");
        renderJson(BusinessUser.dao.getCustomerInfo(businessUserName));
    }

    /**
     * 获取商品规格信息(添加/修改订单)
     */
    public void getProductIdStandardsInfo() {
        String productId = getPara("productId");
        renderJson(ProductStandard.dao.getProductIdStandardsInfo(productId));
    }

    /**
     * 支付id生成规则
     *
     * @return 新的订单id
     */
    private String getNewOrderId() {
        String orderId;
        synchronized (OrderController.class) {
            orderId = DateTimeKit.formatDateToStyle("yyMMddhhmmss", new Date()) + "-" + PAY_COUNT++ + RandomKit.random(1000, 9999);
        }
        return orderId;
    }


}
