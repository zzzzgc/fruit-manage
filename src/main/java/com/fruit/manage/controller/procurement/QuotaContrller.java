package com.fruit.manage.controller.procurement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Order;
import org.apache.log4j.Logger;

/**
 * 采购配额
 *
 * @author ZGC
 * @date 2018-03-21 16:29
 **/
public class QuotaContrller extends BaseController {
    private org.apache.log4j.Logger log = Logger.getLogger(getClass());


//    @RequiresPermissions("order:query")
    /**
     *
     * 获取所有订单的列表数据
     */
    public void getData() {
        log.info("获取数据:" + getParaMap());
        Order order = getModel(Order.class, "", true);
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);

        String orderBy = getPara("prop");

        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));

        // 下单时间
        String[] orderTime = getParaValues("order_time");

//        renderJson(null);
    }
}
