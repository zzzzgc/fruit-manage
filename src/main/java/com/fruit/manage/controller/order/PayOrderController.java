package com.fruit.manage.controller.order;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Order;
import com.fruit.manage.model.PayOrderInfo;
import com.jfinal.log.Logger;

import java.math.BigDecimal;
import java.util.Date;


/**
 * @author partner
 * @date 2018/4/4 15:45
 */
public class PayOrderController extends BaseController {
    private Logger logger = Logger.getLogger(PayOrderController.class);

    /**
     * 添加支付订单信息
     */
    public void addPayOrder() {
        Date currentTime = new Date();
        PayOrderInfo payOrderInfo = getModel(PayOrderInfo.class, "", true);
        payOrderInfo.setCreateTime(currentTime);
        payOrderInfo.setPayOfTime(currentTime);
        payOrderInfo.save();

        // 修改订单已支付金额
        Order order = Order.dao.getOrder(payOrderInfo.getOrderId());
        order.setPayTotalMoney(order.getPayTotalMoney()==null?new BigDecimal(payOrderInfo.getPayTheMoney()):order.getPayTotalMoney().add(new BigDecimal(payOrderInfo.getPayTheMoney())));
        order.update();
        renderNull();
    }
}
