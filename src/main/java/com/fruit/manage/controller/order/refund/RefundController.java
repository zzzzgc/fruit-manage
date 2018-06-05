package com.fruit.manage.controller.order.refund;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.RefundOrderInfo;

/**
 * @author partner
 * @date 2018/6/4 18:36
 */
public class RefundController extends BaseController {
    public void getData() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        String orderBy = getPara("prop");
        boolean isASC = "ascending".equals(getPara("order"));
        String businessName = getPara("business_name");
        String orderID = getPara("order_id");
        String salesName = getPara("sales_name");
        String[] createTimes = getParaValues("format_create_time");
        renderJson(RefundOrderInfo.dao.getRefundOrderInfoPages(pageNum,pageSize,orderBy,isASC,businessName,salesName,orderID,createTimes));
    }

    public void addRefundRecord() {

    }
}
