package com.fruit.manage.controller.order;

import com.alibaba.fastjson.JSON;
import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.OrderConstant;
import com.fruit.manage.constant.OrderStatusCode;
import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.model.*;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.IdUtil;
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

    public void test() {
        System.out.println(new Date());
    }

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
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        Integer isNext = getParaToInt("isNext");
        String orderId = getPara("orderId");
        Order order = Order.dao.getOrder(orderId);
        if (isNext.equals(1)) {
            // 进入下一个流程
            Integer orderStatus = order.getOrderStatus();

            Integer nextStatus = OrderConstant.nextStatus(orderStatus);
            if (nextStatus.equals(OrderStatusCode.DELETED.getStatus())) {
                renderErrorText("数据有问题或接口异常丶没有下一个状态了");
                return;
            }
            order.setOrderStatus(nextStatus);
            order.update();
        } else {
            // 删除
            order.setOrderStatus(OrderStatusCode.DELETED.getStatus());
            order.update();
            List<OrderDetail> orderDetails = OrderDetail.dao.getOrderDetails(orderId);
            for (OrderDetail orderDetail : orderDetails) {
                orderDetail.delete(UserTypeConstant.A_USER, uid);
            }
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
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        Order order = getModel(Order.class, "", true);
        String products = getPara("products");
        Integer business_user_id = getParaToInt("business_user_id");
        List<OrderDetail> orderDetails = JSON.parseArray(products, OrderDetail.class);
        BigDecimal payNeedMoney = new BigDecimal(0);
        Date now = new Date();

        if (order.getOrderId() != null) {
            //编辑

            for (OrderDetail orderDetail : orderDetails) {
                Integer num = orderDetail.getNum();
                BigDecimal sellPrice = orderDetail.getSellPrice();
                BigDecimal totalPay = sellPrice.multiply(new BigDecimal(num));
                orderDetail.setTotalPay(totalPay);
                payNeedMoney = payNeedMoney.add(totalPay);
                if (orderDetail.getId() != null) {
                    orderDetail.update(UserTypeConstant.A_USER, uid);
                } else {
                    orderDetail.setUId(business_user_id);
                    orderDetail.setOrderId(order.getOrderId());
                    orderDetail.setCreateTime(now);
                    orderDetail.setUpdateTime(now);
                    orderDetail.save(UserTypeConstant.A_USER, uid);
                }
            }
            order.setPayNeedMoney(payNeedMoney);
            order.setUpdateTime(now);
            order.update();
        } else {
            //添加,并校验是否存在相同订单周期的订单

            String orderId = IdUtil.createOrderId(business_user_id);
            Order nowOrder = Order.dao.getOrder(orderId);
            // 区分该订单周期的订单是否已经被创建
            if (nowOrder == null) {
                order.setOrderId(orderId);

                for (OrderDetail orderDetail : orderDetails) {
                    Integer num = orderDetail.getNum();
                    BigDecimal sellPrice = orderDetail.getSellPrice();
                    BigDecimal totalPay = sellPrice.multiply(new BigDecimal(num));
                    payNeedMoney = payNeedMoney.add(totalPay);

                    orderDetail.setTotalPay(totalPay);
                    orderDetail.setCreateTime(now);
                    orderDetail.setUId(business_user_id);
                    orderDetail.setOrderId(order.getOrderId());
                    orderDetail.setUpdateTime(now);
                    orderDetail.save(UserTypeConstant.A_USER, uid);
                }

                order.setPayNeedMoney(payNeedMoney);
                order.setPayTotalMoney(new BigDecimal(0));
                order.setUId(business_user_id);
                order.setUpdateTime(now);
                order.setCreateTime(now);
                order.save();
            } else {
                // 如果有相同订单周期的订单,就叠加商品.这次添加视为补充商品.因为想修改商品应该去编辑才对,而不是添加.

                // 具有完整字段的对象,用于作为更新模板和核对是否叠加
                List<OrderDetail> nowOrderDetails = OrderDetail.dao.getOrderDetails(orderId);
                // 导入的订单
                OrderDetailFor:
                for (OrderDetail orderDetail : orderDetails) {
                    BigDecimal sellPrice = orderDetail.getSellPrice();
                    BigDecimal num = new BigDecimal(orderDetail.getNum());

                    // 过滤与数据库中的订单匹配的商品
                    for (OrderDetail nowOrderDetail : nowOrderDetails) {
                        if (nowOrderDetail.getProductStandardId().equals(orderDetail.getProductStandardId())) {
                            int nowNum = nowOrderDetail.getNum() + orderDetail.getNum();
                            BigDecimal totalPrice = sellPrice.multiply(num);
                            payNeedMoney = payNeedMoney.add(totalPrice);

                            nowOrderDetail.setNum(nowNum);
                            nowOrderDetail.setTotalPay(totalPrice);
                            nowOrderDetail.setUpdateTime(now);
                            nowOrderDetail.update(UserTypeConstant.A_USER, uid, orderId, nowOrderDetail.getProductId(), nowOrderDetail.getProductStandardId(), nowOrderDetail.getNum(), nowNum);
                            continue OrderDetailFor;
                        }
                    }

                    // 不和数据库中的订单匹配的新增商品
                    BigDecimal totalPrice = sellPrice.multiply(num);
                    payNeedMoney = payNeedMoney.add(totalPrice);

                    orderDetail.setTotalPay(totalPrice);
                    orderDetail.setOrderId(orderId);
                    orderDetail.setUId(business_user_id);
                    orderDetail.setUpdateTime(now);
                    orderDetail.setCreateTime(now);
                    orderDetail.save(UserTypeConstant.A_USER,uid);
                }
                nowOrder.setPayNeedMoney(payNeedMoney);
                nowOrder.setUpdateTime(now);
                nowOrder.update();

            }

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
     * 编辑订单的时候,删除商品(修改)
     */
    public void deleteProductForEdit() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        String orderDetailId = getPara("orderDetailId");
        OrderDetail orderDetail = OrderDetail.dao.findById(orderDetailId);
        orderDetail.delete(UserTypeConstant.A_USER, uid);
        renderNull();
    }
}
