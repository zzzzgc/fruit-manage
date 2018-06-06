package com.fruit.manage.controller.order.refund;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.model.BusinessUser;
import com.fruit.manage.model.Order;
import com.fruit.manage.model.RefundOrderInfo;
import com.fruit.manage.util.Constant;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

import java.sql.SQLException;
import java.util.*;

/**
 * @author partner
 * @date 2018/6/4 18:36
 */
public class RefundController extends BaseController {
    private List list;

    public void getData() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        String orderBy = getPara("prop");
        boolean isASC = "ascending".equals(getPara("order"));
        String businessName = getPara("business_name");
        String orderID = getPara("order_id");
        String salesName = getPara("sales_name");
        String[] createTimes = getParaValues("format_create_time");
        renderJson(RefundOrderInfo.dao.getRefundOrderInfoPages(pageNum, pageSize, orderBy, isASC, businessName, salesName, orderID, createTimes));
    }

    /**
     * 添加退款记录
     */
    public void addRefundRecord() {
        Date date = new Date();
        Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {

                RefundOrderInfo refundOrderInfo = getModel(RefundOrderInfo.class, "", true);
                String sql = getBusinessBalanceSql();
                BusinessUser businessUser = BusinessUser.dao.findFirst(sql, refundOrderInfo.getUserId());
                if (businessUser.getMoney().compareTo(refundOrderInfo.getRefundTheMoney()) >= 0) {
                    Integer userId = getSessionAttr(Constant.SESSION_UID);
                    refundOrderInfo.setOperationType(UserTypeConstant.A_USER.getValue());
                    refundOrderInfo.setOperationId(userId);
                    refundOrderInfo.setRefundOfTime(date);
                    refundOrderInfo.setCreateTime(date);
                    refundOrderInfo.save();
                    businessUser.setMoney(businessUser.getMoney().subtract(refundOrderInfo.getRefundTheMoney()));
                    businessUser.update();
                } else {
                    renderErrorText("您的个人余额不足以退款！");
                }
                renderNull();
                return true;
            }
        });

    }

    public void getRefundAllInfo() {
        String userId = getPara("userId");
        String sqlBusiness = getBusinessBalanceSql();
        // 获取用户余额数据
        BusinessUser businessUser = BusinessUser.dao.findFirst(sqlBusiness, userId);
        Map<String, Object> map = new HashMap<>();
        map.put("businessUser", businessUser);

        String sqlOrderInfo = getOrderInfoSql();
        List<Order> orders = Order.dao.find(sqlOrderInfo, userId);
        map.put("orders", orders);
        renderJson(map);
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

    private String getOrderInfoSql() {
        String sql = " SELECT o.id,o.order_id,o.u_id from b_order o where o.u_id = ? ";
        return sql;
    }
}
