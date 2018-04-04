package com.fruit.manage.controller.warehouse.out;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.model.OutWarehouse;
import com.fruit.manage.model.OutWarehouseDetail;
import com.fruit.manage.model.ProductStandard;
import com.fruit.manage.util.Constant;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        OutWarehouseDetail imporOwd = getModel(OutWarehouseDetail.class, "", true);

        // 用于重新核对账本
        Integer outId = imporOwd.getOutId();
        List<OutWarehouseDetail> owd = OutWarehouseDetail.dao.getOutWarehouseDetail(outId);
        Map<Integer, OutWarehouseDetail> owdMap = owd.stream().collect(Collectors.toMap(OutWarehouseDetail::getProductStandardId, Function.identity()));

        if (imporOwd.getId() != null) {
            // 编辑

            OutWarehouseDetail oldOutWarehouseDetail = OutWarehouseDetail.dao.findById(imporOwd.getId());
            switch (imporOwd.getOutType()) {
                case 0:
                    // 销售出库
                    // 出库总金额 = 出库数量 * 订单报价金额
                    imporOwd.setOutTotalPrice(imporOwd.getOutPrice().multiply(new BigDecimal(imporOwd.getOutNum())));
                    break;
                case 1:
                    // 款待出库
                    // 出库总金额  = 0
                    break;
                default:
            }
            imporOwd.update();
            updateProductStandardStore(imporOwd.getProductStandardId(), imporOwd.getOutNum() - oldOutWarehouseDetail.getOutNum(), imporOwd.getProductStandardName(), imporOwd.getProductId(), imporOwd.getProductName());
        } else {
            // 添加(可能已存在该规格,直接覆盖[除旧迎新])

            OutWarehouseDetail outWarehouseDetail = owdMap.get(imporOwd.getProductStandardId());
            Integer changeNum = imporOwd.getOutNum();

            if (outWarehouseDetail != null) {
                // 存在相同规格的出库
                updateProductStandardStore(imporOwd.getProductStandardId(), ~outWarehouseDetail.getOutNum() + 1, imporOwd.getProductStandardName(), imporOwd.getProductId(), imporOwd.getProductName());
                outWarehouseDetail.delete();
            } else {
                // 不存在相同规格的出库
            }

            switch (imporOwd.getOutType()) {
                case 0:
                    // 销售出库
                    // 出库总金额 = 出库数量 * 订单报价金额
                    imporOwd.setOutTotalPrice(imporOwd.getOutPrice().multiply(new BigDecimal(imporOwd.getOutNum())));
                    break;
                case 1:
                    // 款待出库
                    // 出库总金额  = 0
                    break;
                default:
            }

            imporOwd.setOutPrice(new BigDecimal(0));
            imporOwd.setOutType(0);
            imporOwd.setOutAveragePrice(new BigDecimal(0));
            imporOwd.setOutTotalPrice(new BigDecimal(0));
            imporOwd.setUpdateTime(new Date());
            imporOwd.setCreateTime(new Date());
            imporOwd.save();
            updateProductStandardStore(imporOwd.getProductStandardId(), imporOwd.getOutNum(), imporOwd.getProductStandardName(), imporOwd.getProductId(), imporOwd.getProductName());
        }

        // 重新核对总账()


        // 商品数量
        Integer productTotalNum = 0;

        // 商品品类数量
        Integer productStandardTotalNum = 0;

        // 出库总额
        BigDecimal allTotalPrice = new BigDecimal(0.00);


        for (OutWarehouseDetail warehouseDetail : owd) {
            productTotalNum += warehouseDetail.getOutNum();
            ++productStandardTotalNum;
            allTotalPrice.add(warehouseDetail.getOutTotalPrice());
        }

        OutWarehouse ow = new OutWarehouse();
        ow.setId(outId);
        ow.setOutNum(productTotalNum);
        ow.setOutTypeNum(productStandardTotalNum);
        ow.setOutTotalPrice(allTotalPrice);
        ow.update();

        renderNull();
    }

    /**
     * 根据商品规格编号（product_standard_id）修改商品规格的库存
     *
     * @param psId
     * @param changeNum
     * @return
     */
    public boolean updateProductStandardStore(Integer psId, Integer changeNum, String productStandardName, Integer productId, String productName) {
        ProductStandard productStandard = ProductStandard.dao.getProductStandardById(psId);
        productStandard.setStock(productStandard.getStock() + changeNum);
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        // 0入库 1出库
        return productStandard.update(UserTypeConstant.A_USER, uid, productStandard.getStock() + changeNum, productStandard.getStock(), "1", productStandardName, productId, productName);
    }


}
