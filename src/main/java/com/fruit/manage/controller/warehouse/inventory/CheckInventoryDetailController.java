package com.fruit.manage.controller.warehouse.inventory;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.*;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.IdUtil;
import com.fruit.manage.util.excel.ExcelRow;
import com.fruit.manage.util.excelRd.ExcelRd;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdRow;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;
import com.jfinal.aop.Before;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


/**
 * @author partner
 * @date 2018/4/3 22:53
 */
public class CheckInventoryDetailController extends BaseController {
    private Logger logger = Logger.getLogger(CheckInventoryDetailController.class);

    /**
     * 根据条件获取带分页的盘点单详细数据
     */
    public void getAllInfo(){
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
        String checkInventoryId = getPara("check_inventory_id");
        String checkInventoryTime = getPara("check_inventory_time");
        map.put("productName", productName);
        map.put("productId", productId);
        map.put("productStandardName", productStandardName);
        map.put("productStandardId", productStandardId);
        map.put("checkInventoryId", checkInventoryId);
        map.put("createTime", checkInventoryTime);
        Page<CheckInventoryDetail> lists = CheckInventoryDetail.dao.getAllInfo(pageNum, pageSize, orderBy, isASC, map);
        renderJson(lists);
    }

    /**
     * 导入Excel
     */
    @Before(Tx.class)
    public void importExcelInfo(){
        String fileName = getPara("fileName");
        String checkInventoryId=getPara("checkInventoryId");
        String filePath = CommonController.FILE_PATH + File.separator + fileName;
        Iterator<ExcelRdRow> iterable = readExcel(filePath).iterator();
        Integer count=0;
        Date currentTime = new Date();
        String startTime = null;
        String endTime = null;
        while (iterable.hasNext()) {
            count++;
            ExcelRdRow excelRdRow =iterable.next();
            List<Object> list= excelRdRow.getRow();
            if (count == 1) {
                String creatTimeStr = ((String) list.get(0)).split(" ")[0].substring(5);
                startTime = creatTimeStr + " 00:00:00";
                endTime = creatTimeStr + " 23:59:59";
            } else if (count > 2) {
                CheckInventoryDetail checkInventoryDetail =CheckInventoryDetail.dao.getCheckInventoryDetail((Integer)list.get(2),checkInventoryId,startTime,endTime);
                if (checkInventoryDetail != null) {
                    // 根据ID获取盘点单未被修改之前的盘点单详细数据
                    CheckInventoryDetail checkInventoryDetail2 = CheckInventoryDetail.dao.getCheckInventoryDetailById(checkInventoryDetail.getId());
                    // 修改之后的数量
                    Integer inventoryNumUpdate=checkInventoryDetail.getInventoryNum();
                    // 初始的库存数量
                    Integer inventoryNumPrimeval =checkInventoryDetail2.getInventoryNum();
                    Integer inventoryNumDiffer=inventoryNumUpdate-inventoryNumPrimeval;

                    //被修改后的期末库存总额=未被修改期末库存总额 - (库存数量更改后的差值*库存单价)
                    BigDecimal inventoryTotalPriceUpdate=checkInventoryDetail2.getInventoryTotalPrice().add((new BigDecimal(inventoryNumDiffer).multiply(checkInventoryDetail2.getInventoryPrice())));
                    // 被修改后的库存总额差值
                    BigDecimal inventoryTotalPriceDiffer = inventoryTotalPriceUpdate.subtract(checkInventoryDetail2.getInventoryTotalPrice());
                    // 根据存库单价*库存数量=期末库存总额 ，库存数量减少，期末库存总额也随之减少，则单价不变，所以不需要修改
                    checkInventoryDetail2.setInventoryTotalPrice(inventoryTotalPriceUpdate);
                    checkInventoryDetail2.setInventoryNum(inventoryNumUpdate);
                    checkInventoryDetail2.setUpdateTime(currentTime);
                    checkInventoryDetail2.setInventoryRemark(checkInventoryDetail.getInventoryRemark());
                    checkInventoryDetail2.setUserName(checkInventoryDetail.getUserName());
                    checkInventoryDetail2.update();
                    // 修改商品规格库存
                    updateProductStandardStore(checkInventoryDetail2.getProductStandardId(),inventoryNumDiffer,checkInventoryDetail2.getProductStandardName(),checkInventoryDetail2.getProductId(),checkInventoryDetail2.getProductName());
                    // 根据盘点编号获取盘点信息
                    CheckInventory checkInventory = CheckInventory.dao.getCheckInventoryById(checkInventoryDetail.getCheckInventoryId());
                    checkInventory.setUpdateTime(currentTime);
                    // 修改盘点单的商品数量
                    checkInventory.setProductCount(checkInventory.getProductCount()+inventoryNumDiffer);
                    checkInventory.setProductTotalPrice((new BigDecimal(checkInventory.getProductTotalPrice()).add(inventoryTotalPriceDiffer)).doubleValue());
                    checkInventory.update();
                }else {
                    checkInventoryDetail=new CheckInventoryDetail();
                    checkInventoryDetail.setId(IdUtil.getCheckInventoryDetailId(currentTime,(Integer)list.get(2)));
                    checkInventoryDetail.setCheckInventoryId(checkInventoryId);
                    // 根据商品规格编号获取商品编号
                    Integer productId =ProductStandard.dao.getProductIdByPSId((Integer)list.get(2));
                    ProductStandard productStandard = ProductStandard.dao.getProductStandardById((Integer) list.get(2));
                    checkInventoryDetail.setProductId(productId);
                    checkInventoryDetail.setProductName((String) list.get(0));
                    checkInventoryDetail.setProductStandardName((String) list.get(1));
                    checkInventoryDetail.setProductStandardId((Integer) list.get(2));
                    checkInventoryDetail.setProductWeight((Double) list.get(3));

                    //获取前一天的
                    CheckInventoryDetail checkInventoryDetail2=CheckInventoryDetail.dao.getCheckInventoryDetail((Integer)list.get(2),startTime,endTime);
                    if (checkInventoryDetail2 == null) {
                        checkInventoryDetail2=new CheckInventoryDetail();
                        checkInventoryDetail2.setInventoryPrice(new BigDecimal(0));
                        checkInventoryDetail2.setInventoryTotalPrice(new BigDecimal(0));
                    }
                    // 期中入库数量
                    Integer putInNum = WarehouseLog.dao.getCountInventorySum("0", startTime, endTime);
                    if (putInNum == null) {
                        putInNum = 0;
                    }
                    // 期中出库数量
                    Integer outPutNum = WarehouseLog.dao.getCountInventorySum("1", startTime, endTime);
                    if (outPutNum == null) {
                        outPutNum = 0;
                    }
                    // 获取入库单价
                    BigDecimal average = PutWarehouseDetail.dao.getAveragePriceByPsIdAndTime((Integer) list.get(2), startTime, endTime);
                    if (average == null) {
                        average = new BigDecimal(0);
                    }
                     // 库存单价=【期初库存总额+期中入库单价*(期中入库数量-期中出库数量)】/期末库存数量
                    BigDecimal inventoryAveragePrice=(checkInventoryDetail2.getInventoryTotalPrice().add(average.multiply(new BigDecimal(putInNum).subtract(new BigDecimal(outPutNum))))).divide(new BigDecimal(productStandard.getStock()),2,BigDecimal.ROUND_HALF_DOWN);
                    // 期末库存总额 = 期初库存总额+期中入库单价*(期中入库数量-期中出库数量)
                    BigDecimal inventoryTotalPrice = checkInventoryDetail2.getInventoryTotalPrice().add(average.multiply(new BigDecimal(putInNum).subtract(new BigDecimal(outPutNum))));
                    checkInventoryDetail.setInventoryPrice(inventoryAveragePrice);
                    checkInventoryDetail.setInventoryTotalPrice(inventoryTotalPrice);
                    checkInventoryDetail.setUserName((String) list.get(6));
                    checkInventoryDetail.setInventoryNum(productStandard.getStock());
                    checkInventoryDetail.setCheckInventoryNum((Integer) list.get(7));
                    checkInventoryDetail.setInventoryRemark((String)list.get(8));
                    checkInventoryDetail.setCreateTime(currentTime);
                    checkInventoryDetail.save();

                    // 根据盘点单的编号获取盘点信息
                    CheckInventory checkInventory = CheckInventory.dao.getCheckInventoryById(checkInventoryId);
                    checkInventory.setUpdateTime(currentTime);
                    checkInventory.setProductTotalPrice((new BigDecimal(checkInventory.getProductTotalPrice()).add(inventoryTotalPrice)).doubleValue());
                    checkInventory.setProductCount(checkInventory.getProductCount()+productStandard.getStock());
                    checkInventory.setCheckInventoryTime(currentTime);
                    checkInventory.update();
                }
            }
        }
        renderNull();
    }
    private List<ExcelRdRow> readExcel(String filePath) {
        ExcelRd excelRd = new ExcelRd(filePath);
        excelRd.setStartRow(1);
        excelRd.setStartCol(0);
        ExcelRdTypeEnum[] types = {
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.STRING
        };
        // 指定每列的类型
        excelRd.setTypes(types);

        List<ExcelRdRow> rows = null;
        try {
            rows = excelRd.analysisXlsx();
        } catch (ExcelRdException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    /**
     * 根据ID获取盘点单详细信息
     */
    public void getCheckInventoryById(){
        String id = getPara("id");
        CheckInventoryDetail checkInventoryDetail = CheckInventoryDetail.dao.getCheckInventoryDetailById(id);
        renderJson(checkInventoryDetail);
    }

    /**
     * 修改盘点单详细信息
     */
    @Before(Tx.class)
    public void updateInventoryDetail(){
        CheckInventoryDetail checkInventoryDetail = getModel(CheckInventoryDetail.class, "", true);
        if (checkInventoryDetail != null) {
            Date currentTime=new Date();
            // 根据ID获取盘点单未被修改之前的盘点单详细数据
            CheckInventoryDetail checkInventoryDetail2 = CheckInventoryDetail.dao.getCheckInventoryDetailById(checkInventoryDetail.getId());
            // 修改之后的数量
            Integer inventoryNumUpdate=checkInventoryDetail.getInventoryNum();
            // 初始的库存数量
            Integer inventoryNumPrimeval =checkInventoryDetail2.getInventoryNum();
            Integer inventoryNumDiffer=inventoryNumUpdate-inventoryNumPrimeval;

            //被修改后的期末库存总额=未被修改期末库存总额 - (库存数量更改后的差值*库存单价)
            BigDecimal inventoryTotalPriceUpdate=checkInventoryDetail2.getInventoryTotalPrice().add((new BigDecimal(inventoryNumDiffer).multiply(checkInventoryDetail2.getInventoryPrice())));
            // 被修改后的库存总额差值
            BigDecimal inventoryTotalPriceDiffer = inventoryTotalPriceUpdate.subtract(checkInventoryDetail2.getInventoryTotalPrice());
            // 根据存库单价*库存数量=期末库存总额 ，库存数量减少，期末库存总额也随之减少，则单价不变，所以不需要修改
            checkInventoryDetail2.setInventoryTotalPrice(inventoryTotalPriceUpdate);
            checkInventoryDetail2.setInventoryNum(inventoryNumUpdate);
            checkInventoryDetail2.setUpdateTime(currentTime);
            checkInventoryDetail2.setInventoryRemark(checkInventoryDetail.getInventoryRemark());
            checkInventoryDetail2.setUserName(checkInventoryDetail.getUserName());
            checkInventoryDetail2.update();
            // 修改商品规格库存
            updateProductStandardStore(checkInventoryDetail2.getProductStandardId(),inventoryNumDiffer,checkInventoryDetail2.getProductStandardName(),checkInventoryDetail2.getProductId(),checkInventoryDetail2.getProductName());
            // 根据盘点编号获取盘点信息
            CheckInventory checkInventory = CheckInventory.dao.getCheckInventoryById(checkInventoryDetail.getCheckInventoryId());
            checkInventory.setUpdateTime(currentTime);
            // 修改盘点单的商品数量
            checkInventory.setProductCount(checkInventory.getProductCount()+inventoryNumDiffer);
            checkInventory.setProductTotalPrice((new BigDecimal(checkInventory.getProductTotalPrice()).add(inventoryTotalPriceDiffer)).doubleValue());
            checkInventory.update();
        }
    }

    /**
     * 根据商品规格编号（product_standard_id）修改商品规格的库存
     * @param psId
     * @param putNum
     * @return
     */
    public boolean updateProductStandardStore(Integer psId,Integer putNum,String productStandardName,Integer productId,String productName){
        ProductStandard productStandard = ProductStandard.dao.getProductStandardById(psId);
        productStandard.setStock(productStandard.getStock() + putNum);
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        return productStandard.update(UserTypeConstant.A_USER,uid,productStandard.getStock()+putNum,productStandard.getStock(),"0",productStandardName,productId,productName);
    }
}
