package com.fruit.manage.controller.order;

import com.alibaba.fastjson.JSON;
import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.*;
import com.fruit.manage.model.*;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.DateAndStringFormat;
import com.fruit.manage.util.ZhioDateUtils;
import com.fruit.manage.util.ZhioIdUtil;
import com.jfinal.aop.Before;
import com.jfinal.ext.kit.DateKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author ZGC
 * @date Created in 17:59 2018/3/16
 */
public class OrderController extends BaseController {

    private Logger log = Logger.getLogger(getClass());
    /**
     * 删除订单序列自增
     */
    public static int delOrderCount = 0;

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

        paramMap.put("orderId", getPara("order_id"));

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
            System.out.println("orderId:" + orderId + ",原来的状态是:" + orderStatus + ",后来的状态是:" + nextStatus);
            if (nextStatus.equals(OrderStatusCode.DELETED.getStatus())) {
                renderErrorText("数据有问题或接口异常丶没有下一个状态了");
                return;
            }
            order.setOrderStatus(nextStatus);
            order.update();
        } else {
            // 删除
            order.setOrderStatus(OrderStatusCode.DELETED.getStatus());
            order.delete();
            LogisticsInfo.dao.delLogisticsInfoByOrderID(orderId);
            List<OrderDetail> orderDetails = OrderDetail.dao.getOrderDetails(orderId);
            orderId = delOrderCount++ + "-" + order.getOrderId();
            order.setOrderId(orderId);
            order.save();
            for (OrderDetail orderDetail : orderDetails) {
                // ccz 2018-5-18 orderCreateTime
                orderDetail.delete(UserTypeConstant.A_USER, uid, orderId, orderDetail.getProductId(), orderDetail.getProductStandardId(), orderDetail.getNum(), order.getCreateTime());
                orderDetail.setOrderId(orderId);
                // 避免写入订单日志被算成一笔订单,所以这里不调用带有订单统计的save
                orderDetail.save();
                // 商家购买数量去除一个,但是由于周订单每周清除一次,所以会导致不准确.这里就不删除了
//                Product.dao.updateSellNum(orderDetail.getProductId(),-1);
            }
        }
        renderNull();
    }

    /**
     * 修改订单状态(比如 5 -> 0 )
     */
    @Before(Tx.class)
    public void setRollbackStatus() {
        String orderId = getPara("orderId");
        Order order = Order.dao.getOrder(orderId);
        Integer orderStatus = order.getOrderStatus();
        // 判断是否为已配货
       /* if(OrderStatusCode.WAIT_DISTRIBUTION.getStatus().equals(orderStatus)){
            List<OrderDetail> details = OrderDetail.dao.getOrderDetailSingleTable(orderId);
            for (OrderDetail detail : details) {
                ProductStandard productStandard = ProductStandard.dao.getProductStandardById(detail.getProductStandardId());
                // 修改库存数量=把现有的库存加上实际发货的数量
                productStandard.setStock(productStandard.getStock()+detail.getActualSendGoodsNum());
                productStandard.update();
            }
        }*/
        Integer rollbackStatus = OrderConstant.rollbackStatus(orderStatus);
        order.setOrderStatus(rollbackStatus);
        order.update();
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
     * 根据订单号获取订单详情里必要信息
     */
    public void getOrderDetailElse() {
        String orderId = getPara("orderId");
        String sql = "select pay_of_type,pay_of_time from b_pay_order_info where 1=1 and order_id = ? order by pay_of_time DESC";
        PayOrderInfo payOrderInfo = PayOrderInfo.dao.findFirst(sql, orderId);
        String dateStr = "暂无";
        String payOfType = "暂无";
        if (payOrderInfo != null && payOrderInfo.getPayOfTime() != null) {
            dateStr = DateAndStringFormat.getStringDateShort(payOrderInfo.getPayOfTime());
            payOfType = PayOfTypeStatusCode.getPayType(payOrderInfo.getPayOfType());
        }
        Map<String, Object> map = new HashMap<>(10);
        map.put("payOfTime", dateStr);
        map.put("payOfType", payOfType);
        renderJson(map);
    }

    /**
     * 更新报损表
     */
    @Before(Tx.class)
    public void updateBreakage() {
        Date date = new Date();
        OrderBreakage orderBreakage = getModel(OrderBreakage.class, "", true);
        String order_id = getPara("order_id");
        String[] imgs = getParaValues("imgs");
        orderBreakage.setAudit(0);
        orderBreakage.setOrderId(order_id);
        // 如果报损单的编号不为空并且大于0，则编辑
        if (orderBreakage.getId() != null && orderBreakage.getId() > 0) {
            orderBreakage.update();
        } else {
            orderBreakage.setCreateTime(date);
            orderBreakage.save();
        }
        System.out.println(orderBreakage.get("id").toString());
        // 根据报损编号删除报损关联图片
        OrderBreakageImg.dao.delOrderBreakageImgByBreakageId(orderBreakage.getId());
        OrderBreakageImg orderBreakageImg = null;
        if (imgs != null && !("").equals(imgs) && imgs.length > 0) {
            for (int i = 0; i < imgs.length; i++) {
                orderBreakageImg = new OrderBreakageImg();
                orderBreakageImg.setBreakageId(orderBreakage.getId());
                orderBreakageImg.setImgUrl(imgs[i]);
                orderBreakageImg.setSort(i + 1);
                orderBreakageImg.setCreateTime(date);
                orderBreakageImg.save();
            }
        }
        renderNull();
    }

    /**
     * 过去订单报损数据
     */
    public void getBreakageInfo() {
        String orderId = getPara("orderId");
        Integer psId = getParaToInt("productStandardId");
        Map<String, Object> map = new HashMap<>(2);
        OrderBreakage orderBreakage = OrderBreakage.dao.getOrderBreakageInfoByOrderIdAndPSId(orderId, psId);
        map.put("orderBreakage", orderBreakage);
        if (orderBreakage != null) {
            map.put("orderBreakageImg", OrderBreakageImg.dao.getOrderBreakageImgByBreakageId(orderBreakage.getId()));
        }
        renderJson(map);
    }

    public void updateOrderDetailElse() {
        OrderDetail orderDetail = getModel(OrderDetail.class, "", true);
        OrderDetail orderDetail2 = OrderDetail.dao.getOrderDetailById(orderDetail.getId());
        if (orderDetail2 != null && orderDetail2.getActualSendGoodsNum() != null) {
            Order order = Order.dao.getOrder(orderDetail2.getOrderId());
            if (order.getPayAllMoney() != null) {

                // 报价价格差值
                BigDecimal diffPrice = orderDetail.getSellPrice().subtract(orderDetail2.getSellPrice());
                // 该订单详细的总价格差值save
                BigDecimal totalMoneyBefore = orderDetail.getSellPrice().multiply(new BigDecimal(orderDetail2.getActualSendGoodsNum()));
                BigDecimal totalMoneyAfter = orderDetail2.getSellPrice().multiply(new BigDecimal(orderDetail2.getActualSendGoodsNum()));
                // 改之前减改之后
                BigDecimal totalMoneyDiff = totalMoneyAfter.subtract(totalMoneyBefore);

                orderDetail2.setSellPrice(orderDetail.getSellPrice());
                orderDetail2.setActualDeliverNum(orderDetail.getActualDeliverNum());
                Integer userId = getSessionAttr(Constant.SESSION_UID);
                // 订单数量未改变
                orderDetail2.update(UserTypeConstant.B_USER, (Integer) getSessionAttr(Constant.SESSION_UID), orderDetail2.getOrderId(), orderDetail2.getProductId(), orderDetail2.getProductStandardId(), orderDetail2.getNum(), orderDetail2.getNum(), order.getCreateTime());


                // 修改订单实际需要支付的总总金额
                order.setPayRealityNeedMoney(order.getPayRealityNeedMoney().subtract(totalMoneyDiff));
                order.setPayAllMoney(order.getPayAllMoney().subtract(totalMoneyDiff));
//            order.setPayNeedMoney(order.getPayNeedMoney().subtract(totalMoneyDiff));
                order.update();
                // 修改订单总价格
                renderNull();
            } else {
                renderErrorText("订单必须为配送状态!");
            }
        } else {
            renderErrorText("订单必须为配送状态!");
        }
    }

    /**
     * 保存添加的新订单
     */
    @Before(Tx.class)
    public void save() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        Order order = getModel(Order.class, "", true);
        String products = getPara("products");
        Integer businessUserId = getParaToInt("business_user_id");
        Date orderCycle = StringUtils.isNotBlank(getPara("first_day_order_cycle")) ? DateKit.toDate(getPara("first_day_order_cycle")) : new Date();
        List<OrderDetail> orderDetails = JSON.parseArray(products, OrderDetail.class);
        BigDecimal payNeedMoney = new BigDecimal(0);
        BigDecimal payRealityNeedMoney = new BigDecimal(0);
        Date now = new Date();

        if (order.getOrderId() != null) {
            //编辑

            for (OrderDetail orderDetail : orderDetails) {
                Integer num = orderDetail.getNum();
                BigDecimal sellPrice = orderDetail.getSellPrice();
                BigDecimal totalPay = sellPrice.multiply(new BigDecimal(num));
                Integer actualSendGoodsNum = orderDetail.getActualSendGoodsNum();
                // 有实际发货的数量,在配货的时候减库存
                if (actualSendGoodsNum != null && actualSendGoodsNum != 0) {
                    /*//根据商品规格编号获取商品规格信息
                    ProductStandard productStandard=ProductStandard.dao.findById(orderDetail.getProductStandardId());
                    //判断仓库数量是否小于实发数数量
                    if(productStandard!=null && productStandard.getStock()>=actualSendGoodsNum) {
                        // 执行出库操作：库存量-发货量
                        productStandard.setStock(productStandard.getStock()-actualSendGoodsNum);
                        productStandard.update();
                    }else{
                        throw new RuntimeException("");
                    }*/
                    // 实际需要支付金额 = （所有子订单=销售价*实际发货数量）
                    payRealityNeedMoney = payRealityNeedMoney.add(sellPrice.multiply(new BigDecimal(actualSendGoodsNum)));
                    orderDetail.setActualDeliverNum(actualSendGoodsNum);
                }
                orderDetail.setTotalPay(totalPay);
                payNeedMoney = payNeedMoney.add(totalPay);
                if (orderDetail.getId() != null) {
                    OrderDetail oldOrderDetail = OrderDetail.dao.findById(orderDetail.getId());
                    // ccz 2018-5-18 orderCreateTime
                    orderDetail.update(UserTypeConstant.A_USER, uid, order.getOrderId(), orderDetail.getProductId(), orderDetail.getProductStandardId(), oldOrderDetail.getNum(), orderDetail.getNum(), order.getCreateTime());
                } else {
                    orderDetail.setUId(businessUserId);
                    orderDetail.setOrderId(order.getOrderId());
                    orderDetail.setCreateTime(now);
                    orderDetail.setUpdateTime(now);
                    // ccz 2018-5-18 orderCreateTime
                    orderDetail.save(UserTypeConstant.A_USER, uid, order.getCreateTime());
                }
            }
            order.setPayNeedMoney(payNeedMoney);
            // 设置实际支付需要的金额
            order.setPayRealityNeedMoney(payRealityNeedMoney);
            order.setUpdateTime(now);
//            order.setOrderCycleDate(ZhioDateUtils.getOrderCycleDate(now));
            // 统计订单总货款pay_all_price
            Order oldOrder = Order.dao.findById(order.getId());
            if (oldOrder.getPayLogisticsMoney() != null) {
                // 订单总货款pay_all_money = 总订单物流费用+总订单实际支付金额
                order.setPayAllMoney(oldOrder.getPayLogisticsMoney().add(payRealityNeedMoney));
            }
            order.update();
        } else {
            //添加,并校验是否存在相同订单周期的订单.这时候物流信息还没出来,不能统计订单总货款 pay_all_price

            // 订单锁拦截器
            BusinessUser user = BusinessUser.dao.findById(businessUserId);
            Integer lock = user.getLock();
            if (lock == 1) {
                renderErrorText("用户下单功能已被锁，请联系运营或销售解锁");
                return;
            }

            String orderId = ZhioIdUtil.getOrderId(orderCycle, businessUserId);
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
                    orderDetail.setUId(businessUserId);
                    orderDetail.setOrderId(order.getOrderId());
                    orderDetail.setCreateTime(orderCycle);
                    orderDetail.setUpdateTime(now);
                    // ccz 2018-5-18 orderCreateTime
                    orderDetail.save(UserTypeConstant.A_USER, uid, orderCycle);
                }

                order.setPayNeedMoney(payNeedMoney);
                order.setPayTotalMoney(new BigDecimal(0));
                order.setUId(businessUserId);
                order.setOrderCycleDate(ZhioDateUtils.getOrderCycleDate(orderCycle));
                order.setUpdateTime(now);
                order.setCreateTime(orderCycle);
                order.save();

                BusinessInfo info = BusinessInfo.dao.getBusinessInfoByUId(businessUserId);
                LogisticsInfo logisticsInfo = new LogisticsInfo();
                logisticsInfo.setUId(uid);
                logisticsInfo.setOrderId(orderId);
                logisticsInfo.setBuyAddress(info.getAddressProvince() + info.getAddressCity() + info.getAddressDetail());
                logisticsInfo.setBuyPhone(info.getPhone());
                logisticsInfo.setBuyUserName(info.getBusinessName());
                logisticsInfo.setDeliveryType(info.getShipmentsType());
                logisticsInfo.setUpdateTime(now);
                logisticsInfo.setCreateTime(orderCycle);
                logisticsInfo.save();
            } else {
                // 如果有相同订单周期的订单,就叠加商品.这次添加视为补充商品.因为想修改商品应该去编辑才对,而不是添加.
                // 具有完整字段的对象,用于作为更新模板和核对是否叠加
                List<OrderDetail> nowOrderDetails = OrderDetail.dao.getOrderDetails(orderId);

                // 导入的订单
                OrderDetailFor:
                for (OrderDetail oldOrderDetail : orderDetails) {
                    BigDecimal sellPrice = oldOrderDetail.getSellPrice();
                    BigDecimal num = new BigDecimal(oldOrderDetail.getNum());

                    for (OrderDetail nowOrderDetail : nowOrderDetails) {
                        if (nowOrderDetail.getProductStandardId().equals(oldOrderDetail.getProductStandardId())) {
                            // 与数据库中的订单匹配的商品
                            int nowNum = nowOrderDetail.getNum() + oldOrderDetail.getNum();
                            BigDecimal totalPrice = sellPrice.multiply(BigDecimal.valueOf(nowNum));
                            payNeedMoney = payNeedMoney.add(totalPrice);

                            nowOrderDetail.setNum(nowNum);
                            nowOrderDetail.setTotalPay(totalPrice);
                            nowOrderDetail.setUpdateTime(now);
                            // ccz 2018-5-18 orderCreateTime
                            nowOrderDetail.update(UserTypeConstant.A_USER, uid, orderId, nowOrderDetail.getProductId(), nowOrderDetail.getProductStandardId(), oldOrderDetail.getNum(), nowOrderDetail.getNum(), order.getCreateTime());
                            continue OrderDetailFor;
                        }
                    }

                    // 不和数据库中的订单匹配的新增商品
                    BigDecimal totalPrice = sellPrice.multiply(num);
                    payNeedMoney = payNeedMoney.add(totalPrice);

                    oldOrderDetail.setTotalPay(totalPrice);
                    oldOrderDetail.setOrderId(orderId);
                    oldOrderDetail.setUId(businessUserId);
                    oldOrderDetail.setUpdateTime(now);
                    oldOrderDetail.setCreateTime(now);
                    // ccz 2018-5-18 orderCreateTime
                    oldOrderDetail.save(UserTypeConstant.A_USER, uid, order.getCreateTime());
                }
                nowOrder.setPayNeedMoney(nowOrder.getPayNeedMoney().add(payNeedMoney));
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
        String sql = "SELECT " +
                "bu.`name`, " +
                "bu.id, " +
                "bu.phone, " +
                "bu.nick_name, " +
                "bu.a_user_sales_id, " +
                "binfo.business_name " +
                "FROM " +
                "a_user AS au " +
                "INNER JOIN a_user_role aur ON aur.user_id = au.id " +
                "INNER JOIN b_business_user AS bu ON au.id = bu.a_user_sales_id " +
                "INNER JOIN b_business_info AS binfo ON binfo.u_id = bu.id " +
                "WHERE 1=1 " +

                // 给运营角色所有客户可见
                "and ( " +
                "case  " +
                "when " +
                "(SELECT ur2.user_id  " +
                "from a_user_role ur2   " +
                "INNER JOIN a_user u2 on ur2.user_id=u2.id " +
                "INNER JOIN a_role r2 on ur2.role_id = r2.id " +
                // 需要给哪个角色赋权查看所有商户时，需要role_key的范围值
                "where r2.role_key = 'operator' " +
                "and u2.id =?) is not NULL " +
                "then 0 else 1 end = 0 or bu.a_user_sales_id = ? " +
                ") " +

                //ccz 2018-5-25 添加未有认证的商户不能被下单
                " and (SELECT ba.id from b_business_auth ba where ba.u_id = bu.id) is not null " +
//                  "bu.a_user_sales_id = ? " +
                // 因为User的角色可以多个，所以查询的数据有多条重复的商家，只有给商家编号分组，就能达到去重
                " group by bu.id ";
        // 销售只能获取自己的客户
        renderJson(BusinessUser.dao.find(sql, uid, uid));
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
        String sql = "SELECT o.create_time from b_order o where o.order_id = ? ";
        Date orderCreateTime = Db.queryDate(sql, orderDetail.getOrderId());
        // ccz 2018-5-18 orderCreateTime
        orderDetail.delete(UserTypeConstant.A_USER, uid, orderDetail.getOrderId(), orderDetail.getProductId(), orderDetail.getProductStandardId(), orderDetail.getNum(), orderCreateTime);
        renderNull();
    }

    /**
     * 保存配送信息
     */
    @Before(Tx.class)
    public void saveLogisticInfo() {
        try {
            LogisticsInfo logisticsInfo = getModel(LogisticsInfo.class, "", true);
            Integer businessUserID = getParaToInt("business_user_id");
            Integer businessInfoID = getParaToInt("business_info_id");
            String orderId = getPara("order_id");
            BusinessInfo businessInfo = BusinessInfo.dao.getBusinessInfoByID(businessInfoID);
            BusinessUser businessUser = BusinessUser.dao.getBusinessUserByID(businessUserID);
            LogisticsInfo logisticsInfoUpdate = LogisticsInfo.dao.getLogisticsDetailInfoByOrderID(orderId);
            if (logisticsInfo != null && logisticsInfoUpdate != null) {

                //发货总费用(send_goods_total_cost)：
                // 运费（freight_cost） +
                // 装车费（embarkationCost） +
                // 中转费/短途费（transshipment_cost）+
                // 三轮车费用（tricycle_cost）+
                // 打包费（package_cost
                BigDecimal freightCost = logisticsInfo.getFreightCost() == null ? new BigDecimal(0) : logisticsInfo.getFreightCost();
                BigDecimal embarkationCost = logisticsInfo.getEmbarkationCost() == null ? new BigDecimal(0) : logisticsInfo.getEmbarkationCost();
                BigDecimal transshipmnetCost = logisticsInfo.getTransshipmentCost() == null ? new BigDecimal(0) : logisticsInfo.getTransshipmentCost();
                BigDecimal tricycleCost = logisticsInfo.getTricycleCost() == null ? new BigDecimal(0) : logisticsInfo.getTricycleCost();
                BigDecimal packageCost = logisticsInfo.getPackageCost() == null ? new BigDecimal(0) : logisticsInfo.getPackageCost();
                // 计算发货总费用
                BigDecimal sendGoodsTotalCost = freightCost.add(embarkationCost).add(transshipmnetCost).add(tricycleCost).add(packageCost);
                // 设置发货总费用
                logisticsInfoUpdate.setFreightCost(freightCost);
                logisticsInfoUpdate.setEmbarkationCost(embarkationCost);
                logisticsInfoUpdate.setTricycleCost(tricycleCost);
                logisticsInfoUpdate.setTransshipmentCost(transshipmnetCost);
                logisticsInfoUpdate.setPackageCost(packageCost);
                logisticsInfoUpdate.setAbstract(logisticsInfo.getAbstract());
                logisticsInfoUpdate.setDeliveryInfo(logisticsInfo.getDeliveryInfo());
                logisticsInfoUpdate.setLicensePlateNumber(logisticsInfo.getLicensePlateNumber());

                logisticsInfoUpdate.setPackageNum(logisticsInfo.getPackageNum());
                logisticsInfoUpdate.setSendGoodsTotalCost(sendGoodsTotalCost);
                // 获取并设置实际发货总数量
                logisticsInfoUpdate.setRealitySendNum(LogisticsInfo.dao.getActualSendGoodsNum(orderId));
                logisticsInfoUpdate.setDeliveryInfo(logisticsInfo.getDeliveryInfo());
                logisticsInfoUpdate.setLicensePlateNumber(logisticsInfo.getLicensePlateNumber());
                logisticsInfoUpdate.setSendGoodsTime(new Date());

                logisticsInfoUpdate.setUpdateTime(new Date());
                logisticsInfoUpdate.update();

                Order order = Order.dao.getOrder(orderId);
                order.setPayLogisticsMoney(sendGoodsTotalCost);
                //修改订单产生的总费用 = 订单费用+发货物流费用
                order.setPayAllMoney(order.getPayRealityNeedMoney().add(sendGoodsTotalCost));
                order.update();
            }
            renderNull();
        } catch (Exception e) {
            renderErrorText("发货失败!");
        }
    }

    /**
     * 根据订单编号获取并最终算出要支付的总价格
     */
    public void getLogisticsCost() {
        String orderId = getPara("orderId");
        Integer userId = getParaToInt("businessUserId");
        LogisticsInfo logisticsInfo = LogisticsInfo.dao.getLogisticeInfoByOrderID(orderId);
        Integer orderStatus = Order.dao.getOrderStatusByOrderId(orderId);
        if (orderStatus < 15) {
            renderErrorText("请先对订单号为" + orderId + "进行配送！");
            return;
        }
        if (logisticsInfo != null) {
            BigDecimal orderPayRealityNeedMoney = OrderDetail.dao.getOrderPayRealityNeedMoneyByOrderID(orderId);
            if (logisticsInfo == null) {
                logisticsInfo = new LogisticsInfo();
            }
            if (logisticsInfo.getSendGoodsTotalCost() == null) {
                logisticsInfo.setSendGoodsTotalCost(new BigDecimal(0));
            }
            BigDecimal allTotalCost = orderPayRealityNeedMoney.add(logisticsInfo.getSendGoodsTotalCost());
            // 根据用户编号获取商户商铺信息
            BusinessInfo businessInfo = BusinessInfo.dao.getBusinessInfoByUId(userId);
            // 根据用户编号获取获取的手机号码和用户名称f
            BusinessUser businessUser = BusinessUser.dao.getBusinessUserByID(userId);
            // 根据订单编号获取支付的订单记录的总金额
//            Double reallyPayMoney = PayOrderInfo.dao.getReallyPayMoney(orderId);
            Double reallyPayMoney = PayOrderInfo.dao.getPayAllMoney(orderId);
            // 根据订单编号获取订单支付的记录
            List<PayOrderInfo> payOrderInfos = PayOrderInfo.dao.getPayOrderInfoByOrderId(orderId);
            List list = new ArrayList<>();
            list.add(allTotalCost);
            list.add(businessInfo);
            list.add(businessUser);
            list.add(reallyPayMoney);
            list.add(payOrderInfos);
            renderJson(list);
        } else {
            renderErrorText("物流信息为空!");
//            List<String> list = new ArrayList<>();
//            list.add("error");
//            renderJson(list);
        }
    }

    /**
     * 修改订单为支付状态
     */
    public void updatePayStatus() {
        try {
            String orderId = null;
            orderId = getPara("orderId");
            LogisticsInfo logisticsInfo = LogisticsInfo.dao.getLogisticeInfoByOrderID(orderId);
//            BigDecimal orderTotalPay = OrderDetail.dao.getOrderTotalCost(orderId);
            BigDecimal payRealityNeedMoney = OrderDetail.dao.getOrderPayRealityNeedMoneyByOrderID(orderId);
            if (logisticsInfo == null) {
                logisticsInfo = new LogisticsInfo();
            }
            if (logisticsInfo.getSendGoodsTotalCost() == null) {
                logisticsInfo.setSendGoodsTotalCost(new BigDecimal(0));
            }
            BigDecimal allTotalCost = payRealityNeedMoney.add(logisticsInfo.getSendGoodsTotalCost());
            //根据订单编号修改订单状态
            Order.dao.updateOrderPayStatus(5, allTotalCost, orderId);
            //如果是送达并确认付款，那就修改订单状态为已完成订单
            Order.dao.updateOrderStatus(orderId);
            renderNull();
        } catch (Exception e) {
            renderErrorText("确认支付失败!");
        }
    }

    /**
     * 重写并
     * 通过订单ID修改订单状态
     */
    public void updatePayStatusByOrderId() {
        try {
            String orderId = getPara("orderId");
            LogisticsInfo logisticsInfo = LogisticsInfo.dao.getLogisticeInfoByOrderID(orderId);
            BigDecimal payRealityNeedMoney = OrderDetail.dao.getOrderPayRealityNeedMoneyByOrderID(orderId);
            if (logisticsInfo == null) {
                logisticsInfo = new LogisticsInfo();
            }
            if (logisticsInfo.getSendGoodsTotalCost() == null) {
                logisticsInfo.setSendGoodsTotalCost(new BigDecimal(0));
            }
            BigDecimal allTotalCost = payRealityNeedMoney.add(logisticsInfo.getSendGoodsTotalCost());
            // 根据订单编号获取支付的订单记录的总金额
//            Double reallyPayMoney = PayOrderInfo.dao.getReallyPayMoney(orderId);
            Double reallyPayMoney = PayOrderInfo.dao.getPayAllMoney(orderId);
            if (allTotalCost.doubleValue() == reallyPayMoney) {
                //根据订单编号修改订单状态
                Order.dao.updateOrderPayStatus(5, allTotalCost, orderId);
                //如果是送达并确认付款，那就修改订单状态为已完成订单
                Order.dao.updateOrderStatus(orderId);
                renderNull();
            } else {
                renderErrorText("支付失败!");
            }
        } catch (Exception e) {
            renderErrorText("支付失败!");
        }
    }

    /**
     * 根据订单编号删除物流信息
     */
    public void delLogisticsInfo() {
        String orderId = getPara("orderID");
        try {
//            LogisticsInfo.dao.delLogisticsInfoByOrderID(orderId);
            LogisticsInfo logisticsInfo = LogisticsInfo.dao.getLogisticeInfoByOrderID(orderId);
            logisticsInfo.setSendGoodsTotalCost(new BigDecimal(0));
            logisticsInfo.setPackageNum(0);
            logisticsInfo.setTricycleCost(new BigDecimal(0));
            logisticsInfo.setFreightCost(new BigDecimal(0));
            logisticsInfo.setTransshipmentCost(new BigDecimal(0));
            logisticsInfo.setPackageCost(new BigDecimal(0));
            logisticsInfo.setRealitySendNum(0);
            logisticsInfo.setLicensePlateNumber(null);
            // 暂时不需要
//            logisticsInfo.setDeliveryInfo(null);
            logisticsInfo.setUpdateTime(new Date());
            logisticsInfo.update();
            renderNull();
        } catch (Exception e) {
        }
    }

    /**
     * 根据客户id获取客户的所有未支付的订单
     */
    public void getCustomerOrderInfo() {
        Integer customerId = getParaToInt("customerId");
        renderJson(Order.dao.getCustomerOrderInfo(customerId));
    }

    /**
     * 获取该商户未支付的所有订单
     */
    public void getCustomerPayOrderInfo() {
        Integer customerId = getParaToInt("customerId");
        renderJson(Order.dao.getCustomerPayOrderInfo(customerId));
    }

    /**
     * 单支付方式多订单的分摊
     */
    @Before(Tx.class)
    public void saveApportionInfo() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        Integer customerId = getParaToInt("customerId");
        BigDecimal payMoney = BigDecimal.valueOf(Double.valueOf(getPara("payMoney")));
        Integer payType = getParaToInt("payType");
        Date payDate = getParaToDate("payDate");
        List<Order> apportionOrders = Order.dao.getCustomerPayOrderInfo2(customerId);
        for (Order apportionOrder : apportionOrders) {
            // 总需支付金额(实发金额+所有物流费用)
            BigDecimal payAllMoney = apportionOrder.getPayAllMoney();
            // 用户已支付金额
            BigDecimal payTotalMoney = apportionOrder.getPayTotalMoney();
            //订单欠款金额
            BigDecimal arrearageMoney = payAllMoney.subtract(payTotalMoney);
            // 分摊后余额
            payMoney = payMoney.subtract(arrearageMoney);

            if (payMoney.compareTo(BigDecimal.ZERO) >= 0) {
                //余额大于等于0,完整分摊一笔(已支付)
                apportionOrder.setPayTotalMoney(apportionOrder.getPayAllMoney());
                apportionOrder.setPayStatus(OrderPayStatusCode.IS_OK.getStatus());
                apportionOrder.update();

                PayOrderInfo payOrderInfo = new PayOrderInfo();
                payOrderInfo.setUserId(apportionOrder.getUId());
                payOrderInfo.setOperationId(uid);
                payOrderInfo.setOperationType(UserTypeConstant.A_USER.getValue());
                payOrderInfo.setPayReallyTotalMoney(apportionOrder.getPayAllMoney());
                payOrderInfo.setPayTheMoney(payAllMoney);
                payOrderInfo.setSaleId(apportionOrder.get("a_user_sales_id"));
                payOrderInfo.setOrderId(apportionOrder.getOrderId());
                payOrderInfo.setPayOfType(payType);
                payOrderInfo.setPayOfTime(payDate);
                payOrderInfo.setCreateTime(new Date());
                payOrderInfo.save();
            } else {
                //余额小于0,不能完整扣完(订单还不能是已支付状态).把余额全给他即可

                // 还原余额
                payMoney = payMoney.add(arrearageMoney);

                apportionOrder.setPayTotalMoney(payTotalMoney.add(payMoney));
                apportionOrder.update();

                PayOrderInfo payOrderInfo = new PayOrderInfo();
                payOrderInfo.setUserId(uid);
                payOrderInfo.setOperationId(uid);
                payOrderInfo.setOperationType(UserTypeConstant.A_USER.getValue());
                payOrderInfo.setPayReallyTotalMoney(apportionOrder.getPayAllMoney());
                payOrderInfo.setPayTheMoney(payMoney);
                payOrderInfo.setSaleId(apportionOrder.get("a_user_sales_id"));
                payOrderInfo.setOrderId(apportionOrder.getOrderId());
                payOrderInfo.setPayOfType(payType);
                payOrderInfo.setPayOfTime(payDate);
                payOrderInfo.setCreateTime(new Date());
                payOrderInfo.save();
                renderNull();
                return;
            }
        }

        // 添加剩余余额到用户余额中
        BusinessUser customer = BusinessUser.dao.findById(customerId);
        customer.setMoney(customer.getMoney().add(payMoney));
        customer.update();
        renderNull();
    }
}
