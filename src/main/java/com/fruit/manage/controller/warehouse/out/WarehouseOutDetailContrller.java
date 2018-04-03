package com.fruit.manage.controller.warehouse.out;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.model.OutWarehouseDetail;
import com.fruit.manage.model.ProductStandard;
import com.fruit.manage.util.Constant;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: ZGC
 * @Date Created in 17:57 2018/4/3
 */
public class WarehouseOutDetailContrller extends BaseController {

    /**
     * 获取数据
     */
    public void getData() {
        OutWarehouseDetail outWarehouseDetail = getModel(OutWarehouseDetail.class, "", true);
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);

        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));
        String orderBy = getPara("prop");

        // 下单时间
        String[] create_time = getParaValues("create_time");

        renderJson(OutWarehouseDetail.dao.getData(pageNum, pageSize, orderBy, isASC, create_time, outWarehouseDetail));
    }

    /**
     * 获取订单详细
     */
    public void getWarehouseDetailInfo() {
        renderJson(OutWarehouseDetail.dao.findById(getParaToInt("id")));
    }

    /**
     * 编辑和添加订单详细
     */
    public void editWarehouseDetailInfo() {
        OutWarehouseDetail outWarehouseDetail = getModel(OutWarehouseDetail.class, "", true);
        if (outWarehouseDetail.getId() != null) {
            OutWarehouseDetail oldOutWarehouseDetail = OutWarehouseDetail.dao.findById(outWarehouseDetail.getId());
            outWarehouseDetail.update();
            updateProductStandardStore(outWarehouseDetail.getProductStandardId(),outWarehouseDetail.getOutNum() - oldOutWarehouseDetail.getOutNum());
        } else {
            outWarehouseDetail.setOutPrice(new BigDecimal(0) );
            outWarehouseDetail.setOutType(0);
            outWarehouseDetail.setOutAveragePrice(new BigDecimal(0) );
            outWarehouseDetail.setOutTotalPrice(new BigDecimal(0));
            outWarehouseDetail.setUpdateTime(new Date());
            outWarehouseDetail.setCreateTime(new Date());
            outWarehouseDetail.save();
            updateProductStandardStore(outWarehouseDetail.getProductStandardId(),outWarehouseDetail.getOutNum());
        }
        renderNull();
    }

    /**
     * 根据商品规格编号（product_standard_id）修改商品规格的库存
     * @param psId
     * @param putNum
     * @return
     */
    public boolean updateProductStandardStore(Integer psId,Integer putNum){
        ProductStandard productStandard = ProductStandard.dao.getProductStandardById(psId);
        productStandard.setStock(productStandard.getStock() + putNum);
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        return productStandard.update(UserTypeConstant.A_USER,uid,productStandard.getStock()+putNum,productStandard.getStock(),"0");
    }


}
