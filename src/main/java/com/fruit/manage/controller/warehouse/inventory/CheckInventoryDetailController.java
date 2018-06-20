package com.fruit.manage.controller.warehouse.inventory;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.*;
import com.fruit.manage.service.WarehouseService;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.DateUtils;
import com.fruit.manage.util.IdUtil;
import com.fruit.manage.util.excel.ExcelRow;
import com.fruit.manage.util.excelRd.ExcelRd;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdRow;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;
import com.jfinal.aop.Before;
import com.jfinal.ext.kit.DateKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
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
    public void importExcelInfo() {
        Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                Integer rowIndex = 1;
                try {
                    String fileName = getPara("fileName");
                    String checkInventoryId = getPara("checkInventoryId");
                    String orderCycleDate = getPara("order_cycle_date");

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(DateKit.toDate(orderCycleDate));
                    calendar.add(Calendar.DAY_OF_MONTH,-1);
                    String yesterdayOrderCycleDate = DateKit.toStr(calendar.getTime(), "yyyy-MM-dd");

                    String filePath = CommonController.FILE_PATH + File.separator + fileName;
                    Iterator<ExcelRdRow> iterable = readExcel(filePath).iterator();
                    Integer count = 0;
                    Date currentTime = new Date();
                    String nullInfo = "";
                    while (iterable.hasNext()) {
                        rowIndex++;
                        count++;
                        ExcelRdRow excelRdRow = iterable.next();
                        List<Object> list = excelRdRow.getRow();
                        if (count == 1) {
//                            String creatTimeStr = ((String) list.get(0)).split(" ")[0].substring(5);
//                            startTime = creatTimeStr + " 00:00:00";
//                            endTime = creatTimeStr + " 23:59:59";
                        } else if (count > 2) {

                            // 参数获取
                            String productName = list.get(0) + "";
                            String productStandardName = list.get(1) + "";
                            Integer productStandardId = Integer.parseInt(list.get(2) + "");
                            String productWeight = list.get(3) + "";
                            String userName = list.get(6) + "";
                            Integer checkInventoryNum = list.get(7) == null ? 0 : Integer.parseInt(list.get(7) + "");
                            String remark = list.get(8) == null ? "" : list.get(8) + "";

                            CheckInventoryDetail nowCID = CheckInventoryDetail.dao.getCheckInventoryDetail(productStandardId, orderCycleDate);
                            if (nowCID != null) {
                                // 已存在,需要新增
                                Record stockRecord = new WarehouseService().getStock(orderCycleDate,productStandardId);
                                if (stockRecord == null) {
                                    nullInfo += "商品规格编号" + productStandardId + "的库存为空,";
                                    continue;
                                }
                                productStandardId = stockRecord.getInt("id");
                                Integer stock = stockRecord.getInt("stock");
//                                ProductStandard productStandard = ProductStandard.dao.getProductStandardById(productStandardId);
                                // 根据ID获取盘点单未被修改之前的盘点单详细数据
                                CheckInventoryDetail oldCID = CheckInventoryDetail.dao.getCheckInventoryDetailById(nowCID.getId());
                                // 修改之后的数量
//                                Integer inventoryNumUpdate = nowCID.getInventoryNum();
                                Integer inventoryNumUpdate = checkInventoryNum;
                                // 初始的库存数量
                                Integer inventoryNumPrimeval = oldCID.getInventoryNum();
                                Integer inventoryNumDiffer = inventoryNumUpdate - inventoryNumPrimeval;

                                //被修改后的期末库存总额=未被修改期末库存总额 - (库存数量更改后的差值*库存单价)
                                BigDecimal inventoryTotalPriceUpdate = oldCID.getInventoryTotalPrice().add((new BigDecimal(inventoryNumDiffer).multiply(oldCID.getInventoryPrice())));
                                // 被修改后的库存总额差值
                                BigDecimal inventoryTotalPriceDiffer = inventoryTotalPriceUpdate.subtract(oldCID.getInventoryTotalPrice());
                                // 根据存库单价*库存数量=期末库存总额 ，库存数量减少，期末库存总额也随之减少，则单价不变，所以不需要修改
                                oldCID.setInventoryTotalPrice(inventoryTotalPriceUpdate);
//                                oldCID.setInventoryNum(inventoryNumUpdate);
                                oldCID.setInventoryNum(stock);
//                                oldCID.setInventoryNum(productStandard.getStock());
                                oldCID.setCheckInventoryNum(inventoryNumUpdate);
                                oldCID.setUpdateTime(currentTime);
                                oldCID.setInventoryRemark(nowCID.getInventoryRemark());
//                                oldCID.setCheckInventoryNum(nowCID.getCheckInventoryNum());
                                oldCID.setUserName(nowCID.getUserName());
                                oldCID.setInventoryRemark(remark);
                                oldCID.update();
                                // 修改商品规格库存
//                                updateProductStandardStore(oldCID.getProductStandardId(), inventoryNumDiffer, oldCID.getProductStandardName(), oldCID.getProductId(), oldCID.getProductName());
                                // 根据盘点编号获取盘点信息
                                CheckInventory checkInventory = CheckInventory.dao.getCheckInventoryById(nowCID.getCheckInventoryId());
                                checkInventory.setUpdateTime(currentTime);
                                // 修改盘点单的商品数量
                                checkInventory.setProductCount(checkInventory.getProductCount() + inventoryNumDiffer);
                                checkInventory.setProductTotalPrice((new BigDecimal(checkInventory.getProductTotalPrice()).add(inventoryTotalPriceDiffer)).doubleValue());
                                checkInventory.update();
                            } else {
                                // 根据商品规格编号获取商品编号
                                Integer productId = ProductStandard.dao.getProductIdByPSId(productStandardId);

//                                ProductStandard productStandard = ProductStandard.dao.getProductStandardById(productStandardId);
                                Record stockRecord = new WarehouseService().getStock(orderCycleDate,productStandardId);
                                if (stockRecord == null) {
                                    nullInfo += "商品规格编号" + productStandardId + "的库存为空,";
                                    continue;
                                }
                                productStandardId = stockRecord.getInt("id");
                                Integer stock = stockRecord.getInt("stock");

                                nowCID = new CheckInventoryDetail();
                                nowCID.setId(IdUtil.getCheckInventoryDetailId(DateKit.toDate(orderCycleDate), productStandardId));
                                nowCID.setCheckInventoryId(checkInventoryId);
                                nowCID.setProductId(productId);
                                nowCID.setProductName(productName);
                                nowCID.setProductStandardName(productStandardName);
                                nowCID.setProductStandardId(productStandardId);
                                nowCID.setProductWeight(productWeight);

                                // 获取前一天的
//                                CheckInventoryDetail yesterdayCID = CheckInventoryDetail.dao.getCheckInventoryDetail(productStandardId, startTime, endTime);
                                CheckInventoryDetail yesterdayCID = CheckInventoryDetail.dao.getCheckInventoryDetail(productStandardId, yesterdayOrderCycleDate);
                                if (yesterdayCID == null) {
                                    yesterdayCID = new CheckInventoryDetail();
                                    yesterdayCID.setInventoryPrice(new BigDecimal(0));
                                    yesterdayCID.setInventoryTotalPrice(new BigDecimal(0));
                                    yesterdayCID.setInventoryNum(0);
                                }
                                // 期中入库数量
                                Integer putInNum = WarehouseLog.dao.getCountInventorySum(0, orderCycleDate, productStandardId);
                                if (putInNum == null) {
                                    putInNum = 0;
                                }
                                // 期中出库数量
                                Integer outPutNum = WarehouseLog.dao.getCountInventorySum(0, orderCycleDate, productStandardId);
                                if (outPutNum == null) {
                                    outPutNum = 0;
                                }
                                // 获取入库单价
                                BigDecimal average = PutWarehouseDetail.dao.getAveragePriceByPsIdAndTime(productStandardId, orderCycleDate);
                                if (average == null) {
                                    average = new BigDecimal(0);
                                }
                                if (stock == null || stock == 0) {
                                    nullInfo += "商品规格编号" + productStandardId + "的库存为空,";
                                    continue;
                                }
                                // zgc 2018-06-16
                                // 最高总价  期初库存总额 + 期中入库总额     期中入库总额 = 期中入库数量 * 期中入库单价
                                BigDecimal nowTotalPrice = yesterdayCID.getInventoryTotalPrice().add(new BigDecimal(putInNum).multiply(average));
                                // 最高数量  期初库存 + 期中入库
                                BigDecimal nowTotalNum = new BigDecimal(yesterdayCID.getInventoryNum()).add(new BigDecimal(putInNum));
                                // 期末单价  最高总价 / 最高数量
                                BigDecimal inventoryAveragePrice = nowTotalPrice.divide(nowTotalNum,2);
                                // 期末库存总额  最高总价 - 期末单价*期中出库数量
                                BigDecimal inventoryTotalPrice = nowTotalPrice.subtract(inventoryAveragePrice.multiply(new BigDecimal(outPutNum)));
                                // 期末库存数量 期初库存 + 期中差异值
                                Integer nowStock = stock + (putInNum - outPutNum);

                                // 库存单价=【期初库存总额+期中入库单价*(期中入库数量-期中出库数量)】/期末库存数量
//                                BigDecimal inventoryAveragePrice = (yesterdayCID.getInventoryTotalPrice().add(average.multiply(new BigDecimal(putInNum).subtract(new BigDecimal(-outPutNum))))).divide(new BigDecimal(stock), 2, BigDecimal.ROUND_HALF_DOWN);
                                // 期末库存总额 = 期初库存总额+期中入库单价*(期中入库数量-期中出库数量)
//                                BigDecimal inventoryTotalPrice = yesterdayCID.getInventoryTotalPrice().add(average.multiply(new BigDecimal(putInNum).subtract(new BigDecimal(-outPutNum))));
//                                BigDecimal inventoryTotalPrice = yesterdayCID.getInventoryTotalPrice().add(average.multiply(new BigDecimal(checkInventoryNum)));
                                nowCID.setInventoryPrice(inventoryAveragePrice);
                                nowCID.setInventoryTotalPrice(inventoryTotalPrice);
                                nowCID.setUserName(userName);
                                nowCID.setInventoryNum(nowStock);
                                nowCID.setCheckInventoryNum(checkInventoryNum);
                                nowCID.setInventoryRemark(remark);
                                nowCID.setCreateTime(currentTime);
                                nowCID.save();

                                // 根据盘点单的编号获取盘点信息
                                CheckInventory checkInventory = CheckInventory.dao.getCheckInventoryById(checkInventoryId);
                                checkInventory.setUpdateTime(currentTime);
                                checkInventory.setProductTotalPrice((new BigDecimal(checkInventory.getProductTotalPrice()).add(inventoryTotalPrice)).doubleValue());
                                checkInventory.setProductCount(checkInventory.getProductCount() + stock);
                                checkInventory.setCheckInventoryTime(currentTime);
                                checkInventory.update();
                            }
                        }
                    }
                    if (nullInfo != "") {
                        System.out.println("-------------导入盘点单 START-------------");
                        System.out.println(nullInfo);
                        renderErrorText(nullInfo);
                        System.out.println("-------------导入盘点单 END-------------");
                    }
                    renderNull();
                    return true;
                } catch (Exception e) {
                    // 显示报错信息
                    excelRenderErrorInfo(rowIndex, e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            }
        });
    }

    /**
     * 输出Excel的错误信息
     *
     * @param rowIndex
     * @param errorMsg
     */
    public void excelRenderErrorInfo(Integer rowIndex, String errorMsg) {
        renderErrorText("盘点单第" + rowIndex + "行数据出现异常\n异常信息是：" + errorMsg);
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
    public void getCheckInventoryById() {
        String id = getPara("id");
        CheckInventoryDetail checkInventoryDetail = CheckInventoryDetail.dao.getCheckInventoryDetailById(id);
        renderJson(checkInventoryDetail);
    }

    /**
     * 修改盘点单详细信息
     */
    @Before(Tx.class)
    public void updateInventoryDetail() {
        CheckInventoryDetail checkInventoryDetail = getModel(CheckInventoryDetail.class, "", true);
        if (checkInventoryDetail != null) {
            Date currentTime = new Date();
            // 根据ID获取盘点单未被修改之前的盘点单详细数据
            CheckInventoryDetail checkInventoryDetail2 = CheckInventoryDetail.dao.getCheckInventoryDetailById(checkInventoryDetail.getId());
            // 修改之后的数量
//            Integer inventoryNumUpdate = checkInventoryDetail.getInventoryNum();
            Integer inventoryNumUpdate = checkInventoryDetail.getCheckInventoryNum();
            // 初始的库存数量
//            Integer inventoryNumPrimeval = checkInventoryDetail2.getInventoryNum();
            Integer inventoryNumPrimeval = checkInventoryDetail2.getCheckInventoryNum();
            Integer inventoryNumDiffer = inventoryNumUpdate - inventoryNumPrimeval;

            //被修改后的期末库存总额=未被修改期末库存总额 - (库存数量更改后的差值*库存单价)
            BigDecimal inventoryTotalPriceUpdate = checkInventoryDetail2.getInventoryTotalPrice().add((new BigDecimal(inventoryNumDiffer).multiply(checkInventoryDetail2.getInventoryPrice())));
            // 被修改后的库存总额差值
            BigDecimal inventoryTotalPriceDiffer = inventoryTotalPriceUpdate.subtract(checkInventoryDetail2.getInventoryTotalPrice());
            // 根据存库单价*库存数量=期末库存总额 ，库存数量减少，期末库存总额也随之减少，则单价不变，所以不需要修改
            checkInventoryDetail2.setInventoryTotalPrice(inventoryTotalPriceUpdate);
//            checkInventoryDetail2.setInventoryNum(inventoryNumUpdate);
            checkInventoryDetail2.setUpdateTime(currentTime);
            checkInventoryDetail2.setInventoryRemark(checkInventoryDetail.getInventoryRemark());
            checkInventoryDetail2.setUserName(checkInventoryDetail.getUserName());
            checkInventoryDetail2.setCheckInventoryNum(checkInventoryDetail.getCheckInventoryNum());
            checkInventoryDetail2.update();
//            // 修改商品规格库存
//            updateProductStandardStore(checkInventoryDetail2.getProductStandardId(), inventoryNumDiffer, checkInventoryDetail2.getProductStandardName(), checkInventoryDetail2.getProductId(), checkInventoryDetail2.getProductName());
            // 根据盘点编号获取盘点信息
            CheckInventory checkInventory = CheckInventory.dao.getCheckInventoryById(checkInventoryDetail.getCheckInventoryId());
            checkInventory.setUpdateTime(currentTime);
            // 修改盘点单的商品数量
            checkInventory.setProductCount(checkInventory.getProductCount() + inventoryNumDiffer);
            checkInventory.setProductTotalPrice((new BigDecimal(checkInventory.getProductTotalPrice()).add(inventoryTotalPriceDiffer)).doubleValue());
            checkInventory.update();
        }
    }

    /**
     * 根据商品规格编号（product_standard_id）修改商品规格的库存
     *
     * @param psId
     * @param putNum
     * @return
     */
    public boolean updateProductStandardStore(Integer psId, Integer putNum, String productStandardName, Integer productId, String productName) {
        ProductStandard productStandard = ProductStandard.dao.getProductStandardById(psId);
        productStandard.setStock(productStandard.getStock() + putNum);
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        return productStandard.update(UserTypeConstant.A_USER, uid, productStandard.getStock() + putNum, productStandard.getStock(), "0", productStandardName, productId, productName);
    }
}
