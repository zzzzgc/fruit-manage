package com.fruit.manage.controller.order;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.model.BusinessUser;
import com.fruit.manage.model.Order;
import com.fruit.manage.model.PayOrderInfo;
import com.fruit.manage.util.Constant;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

import java.math.BigDecimal;
import java.sql.SQLException;
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
        Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                Date currentTime = new Date();
                PayOrderInfo payOrderInfo = getModel(PayOrderInfo.class, "", true);
                Integer operationId = getSessionAttr(Constant.SESSION_UID);
                payOrderInfo.setOperationId(operationId);
                payOrderInfo.setOperationType(UserTypeConstant.A_USER.getValue());
                payOrderInfo.setCreateTime(currentTime);
                payOrderInfo.setPayOfTime(currentTime);
                System.out.println("有毒，支付价格是：" + payOrderInfo.getPayTheMoney());
                payOrderInfo.save();

                // 修改订单已支付金额
                Order order = Order.dao.getOrder(payOrderInfo.getOrderId());
                // 判断订单支付的金额是否大于需支付的金额
                BigDecimal elsePay = order.getPayAllMoney().subtract(order.getPayTotalMoney() == null ? new BigDecimal(0) : order.getPayTotalMoney());
                if (payOrderInfo.getPayTheMoney().compareTo(elsePay) > 0) {
                    String businessBalanceSql = getBusinessBalanceSql();
                    BusinessUser businessUser = BusinessUser.dao.findFirst(businessBalanceSql, order.getUId());

                    BigDecimal balance = payOrderInfo.getPayTheMoney().subtract(elsePay);
                    businessUser.setMoney(businessUser.getMoney() == null ? balance : businessUser.getMoney().add(balance));
                    businessUser.update();
                    order.setPayTotalMoney(order.getPayAllMoney());
                } else {
                    order.setPayTotalMoney(order.getPayTotalMoney() == null ? payOrderInfo.getPayTheMoney() : order.getPayTotalMoney().add(payOrderInfo.getPayTheMoney()));
                }
                order.update();
                renderNull();
                return true;
            }
        });
    }

    /**
     * 获取用户数据
     *
     * @return
     */
    private String getBusinessBalanceSql() {
        String sql = "SELECT bu.id,bu.money,bu.phone,bu.a_user_sales_id,(SELECT u.nick_name from a_user u where u.id = bu.a_user_sales_id) as nick_name from b_business_user bu where bu.id = ? ";
        return sql;
    }
}
