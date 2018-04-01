package com.fruit.manage.controller.warehouse.put;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.PutWarehouseDetail;
import com.fruit.manage.util.DateAndStringFormat;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Page;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author partner
 * @date 2018/4/1 16:24
 */
public class WarehousePutDetailController extends BaseController {
    private Logger logger = Logger.getLogger(WarehousePutDetailController.class);

    /**
     * 根据条件获取带分页的数据集合
     */
    public void getAllInfo() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        Map map = new HashMap();
        String orderBy = getPara("prop");
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));
        String productName = getPara("product_name");
        String productId = getPara("product_id");
        String productStandardName = getPara("product_standard_name");
        String productStandardId = getPara("product_standard_id");
        String warehousePutDetailId =getPara("put_id");
        map.put("productName", productName);
        map.put("productId", productId);
        map.put("productStandardName", productStandardName);
        map.put("productStandardId", productStandardId);
        map.put("warehousePutDetailId",warehousePutDetailId);
        Page<PutWarehouseDetail> lists=PutWarehouseDetail.dao.getAllInfo(pageNum,pageSize,orderBy,isASC,map);
        renderJson(lists);
    }
}
