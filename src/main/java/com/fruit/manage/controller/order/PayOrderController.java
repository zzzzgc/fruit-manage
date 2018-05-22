package com.fruit.manage.controller.order;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.model.Order;
import com.fruit.manage.model.PayOrderInfo;
import com.fruit.manage.util.Constant;
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
        //这代码有毒
        Date currentTime = new Date();
        PayOrderInfo payOrderInfo = getModel(PayOrderInfo.class, "", true);
        Integer operationId = getSessionAttr(Constant.SESSION_UID);
        payOrderInfo.setOperationId(operationId);
        payOrderInfo.setOperationType(UserTypeConstant.A_USER.getValue());
        payOrderInfo.setCreateTime(currentTime);
        payOrderInfo.setPayOfTime(currentTime);
        System.out.println("有毒，支付价格是："+payOrderInfo.getPayTheMoney());
        payOrderInfo.save();

        // 修改订单已支付金额
        Order order = Order.dao.getOrder(payOrderInfo.getOrderId());
        order.setPayTotalMoney(order.getPayTotalMoney()==null?payOrderInfo.getPayTheMoney():order.getPayTotalMoney().add(payOrderInfo.getPayTheMoney()));
        order.update();
        renderNull();
    }
}
