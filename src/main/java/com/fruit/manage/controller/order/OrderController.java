package com.fruit.manage.controller.order;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.OrderConstant;
import com.fruit.manage.constant.OrderStatusCode;
import com.fruit.manage.model.*;
import com.fruit.manage.util.Constant;
import com.jfinal.aop.Before;
import com.jfinal.json.FastJson;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZGC
 * @date Created in 17:59 2018/3/16
 */
public class OrderController extends BaseController {

    private Logger log = Logger.getLogger(getClass());

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
        Map paramMap=new HashMap();
        paramMap.put("searchProvince",getPara("search_province")); // 省份
        paramMap.put("searchCity",getPara("search_city")); // 城市
        paramMap.put("customerName",getPara("customer_name")); // 客户名称
        paramMap.put("customerPhone",getPara("customer_phone")); // 客户电话
        paramMap.put("customerID",getPara("customer_id")); //客户编号
        paramMap.put("productName",getPara("product_name")); // 商品名称
        paramMap.put("productID",getPara("product_id")); //商品编号
        paramMap.put("standardName",getPara("standard_name")); //规格名称
        paramMap.put("standardID",getPara("standard_id")); //规格编号
        paramMap.put("createTime",getParaValues("format_create_time"));//开始时间和结束时间
        paramMap.put("businessInfoName",getPara("businessInfo_name")); //商铺名称
        paramMap.put("businessInfoID",getPara("businessInfo_id")); // 商铺ID

        String orderStatus = "0";

        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));

        Page<Order> orderPage = Order.dao.getOtherData(orderStatus, pageNum, pageSize, orderBy, isASC,paramMap);

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
        order.put("products",orderDetails);
        renderJson(order);
    }

    /**
     * 保存添加的新订单
     */
    public void save () {
        Order order = getModel(Order.class, "", true);
        Object[] products = (Object[])getParaValues("products");

        OrderDetail[] products1 = JsonKit.parse(getPara("products"), OrderDetail[].class);
        // TODO 未完成
        for (Object product : products) {

        }
        if (order.getOrderId() != null) {

            // 编辑 TODO 需要重新计算金额
            order.update();
        } else {
            //添加
            // 生成订单号
            //
        }
        Map<String, String[]> paraMap = getParaMap();
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
                "\tu.id,\n" +
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
     * 服务器查询商品信息,并返回供用户选择的商品信息[value字段](编辑)
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

}
