package com.fruit.manage.controller.warehouse.out;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.*;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.ExcelException;
import com.jfinal.aop.Before;
import com.jfinal.ext.kit.DateKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.omg.CORBA.INTERNAL;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 仓库出库
 *
 * @Author: ZGC
 * @Date Created in 14:15 2018/4/1
 */
public class WarehouseOutContrller extends BaseController {

    private static Logger log = Logger.getLogger(WarehouseOutContrller.class);

    /**
     * 获取出库表数据
     */
    public void getData() {

        OutWarehouse outWarehouse = getModel(OutWarehouse.class, "", true);
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);

        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));
        String orderBy = getPara("prop");

        // 下单时间
        String[] order_cycle_date = getParaValues("order_cycle_date");

        renderJson(OutWarehouse.dao.getData(pageNum, pageSize, orderBy, isASC, order_cycle_date, outWarehouse));
    }

    /**
     * 添加出库表
     */
    public void save() {
        Date createTime = getParaToDate("create_Time");
        Integer outType = getParaToInt("out_type");
        if (outType == null) {
            outType = 1;
        }
        OutWarehouse outWarehouse = new OutWarehouse();
        outWarehouse.setOutTime(createTime);
        outWarehouse.setOrderCycleDate(createTime);
        outWarehouse.setOutType(outType);
        outWarehouse.setUpdateTime(new Date());
        outWarehouse.setCreateTime(new Date());
        outWarehouse.setWarehouseAddress("运城");
        outWarehouse.save();
        renderNull();
    }

    /**
     * 刪除出库单,并删除子订单
     */
    @Before(Tx.class)
    public void delete() {
        Integer id = getParaToInt("id");
        OutWarehouse.dao.deleteById(id);
        List<OutWarehouseDetail> outWarehouseDetail = OutWarehouseDetail.dao.getOutWarehouseDetail(id);
        for (OutWarehouseDetail warehouseDetail : outWarehouseDetail) {
            updateProductStandardStore(warehouseDetail.getProductStandardId(), warehouseDetail.getOutNum(), warehouseDetail.getProductStandardName(), warehouseDetail.getProductId(), warehouseDetail.getProductName());
        }
        renderNull();
    }

    /**
     * 导出出库详细
     */
    public void exportExcelOutWarehouse() {
        Integer id = getParaToInt("id");
        String sql = "SELECT  " +
                "owd.id, " +
                "owd.product_name, " +
                "owd.product_id, " +
                "owd.product_standard_name, " +
                "owd.product_standard_id, " +
                "owd.product_weight, " +
                "owd.out_num, " +
                "owd.out_price, " +
                "owd.out_total_price, " +
                "owd.out_average_price, " +
                "owd.user_name, " +
                "owd.user_id, " +
                "owd.order_num, " +
                "owd.order_time, " +
                "owd.out_type, " +
                "owd.approver_name, " +
                "owd.out_remark, " +
                "owd.create_time, " +
                "owd.update_time, " +
                "owd.out_Id " +
                "FROM " +
                "b_out_warehouse_detail AS owd " +
                "WHERE " +
                "owd.out_Id = ? ";
        System.out.println(sql);
        List<OutWarehouseDetail> outWarehouseDetails = OutWarehouseDetail.dao.find(sql, id);
        if (outWarehouseDetails == null || outWarehouseDetails.size() < 1) {
            renderErrorText("没有可以导出的记录");
            return;
        }

        String[] headers = {
                " 商品名",
                " 规格名",
                " 规格编号",
                " 重量（斤",
                " 出库单价",
                " 出库数量",
                " 出库总额",
                " 客户姓名",
                " 客户编号",
                " 订单数量",
                " 订单时间",
                " 商品备注"
        };

        ArrayList<Object[]> tableData = new ArrayList<>();

        outWarehouseDetails.forEach(
                detail -> {
                    System.out.println(detail);
                    Object[] column = new Object[12];
                    column[0] = detail.getProductName();
                    column[1] = detail.getProductStandardName();
                    column[2] = detail.getProductStandardId();
                    column[3] = detail.getProductWeight();
                    column[4] = detail.getOutPrice();
                    column[5] = detail.getOutNum();
                    column[6] = detail.getOutTotalPrice();
                    column[7] = detail.getUserName();
                    column[8] = detail.getUserId();
                    column[9] = detail.getOrderNum();
                    column[10] = detail.getOrderTime();
                    column[11] = detail.getOutRemark();
                    tableData.add(column);
                }
        );

        String path = CommonController.FILE_PATH;
        String fileName = System.currentTimeMillis() + "出库单.xlsx";
        try {
            String excelModul = ExcelCommon.createExcelModul(path, fileName, DateFormatUtils.format(new Date(), "yyyy年MM月dd日") + "出库单", null, headers, tableData);
        } catch (ExcelException e) {
            e.printStackTrace();
        }
        HashMap<String, String> returnMap = new HashMap<>(1);
        returnMap.put("fileName", fileName);
        renderJson(returnMap);
    }

    /**
     * 导入商家出库单,前提是用户已导入入库单.
     * <p>
     * 出库后记录当天库存总量.注意:暂时不兼容招待出库
     */
    public void importExcelOutWarehouse() throws IOException, InvalidFormatException {
        // TODO 未取到前端传来的owId
        Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                try {
                    String fileName = getPara("fileName");
                    Integer outId = getParaToInt("out_id");

                    File file = new File(CommonController.FILE_PATH + File.separator + fileName);
                    XSSFWorkbook sheets = new XSSFWorkbook(file);

                    Integer colTotalNum = 7;

                    // 获取出库详细的历史数据
                    List<OutWarehouseDetail> oldOutWarehouseDetail = OutWarehouseDetail.dao.getOutWarehouseDetail(outId);
                    Map<Integer, OutWarehouseDetail> oldOutWarehouseDetailMap = oldOutWarehouseDetail == null || oldOutWarehouseDetail.size() < 1 ? new HashMap<>(1) : oldOutWarehouseDetail.stream().collect(Collectors.toMap(OutWarehouseDetail::getOrderDetailId, Function.identity()));

                    // 不止一张表
                    for (int sheetCount = 0; sheetCount < sheets.getNumberOfSheets(); sheetCount++) {

                        try {
                            Sheet sheet = sheets.getSheetAt(sheetCount);

                            // 获取用户名
                            String userName = sheet.getRow(1).getCell(0).getStringCellValue();
                            userName = userName.substring(userName.indexOf(":") + 1).trim();

                            // 订单号
                            String orderId = sheet.getRow(5).getCell(0).getStringCellValue();
                            orderId = orderId.substring(orderId.indexOf(":") + 1).trim();

                            // 获取所有订单详细
                            List<OrderDetail> orderDetails = OrderDetail.dao.getOrderDetails(orderId);
                            Map<Integer, OrderDetail> orderDetailMap = orderDetails.stream().collect(Collectors.toMap(OrderDetail::getId, Function.identity()));

                            // 列表在7行开始,8倒数第3行结束 (+1-2 = -1)
                            for (int j = 7; j < sheet.getLastRowNum() - 1; j++) {
                                // 0        1         2      3         4     5       6    7
                                // 订单详细编号  商品名称,规格名称,规格编码,重量（斤,下单数量,实发数量,商品备注

                                try {
                                    Row row = sheet.getRow(j);

                                    // 一共有8列
                                    Integer orderDetailId = Integer.valueOf(row.getCell(0) + "");
                                    String productName = row.getCell(1) + "";
                                    Integer productStandardId = (int) row.getCell(3).getNumericCellValue();
                                    String productWeight = row.getCell(4) + "";
                                    Integer outNum = Double.valueOf((row.getCell(6) + "")).intValue();
                                    String outRemark = row.getCell(7) + "";

                                    OrderDetail orderDetail = orderDetailMap.get(orderDetailId);
                                    if (orderDetail == null) {
                                        excelExceptionRender(sheetCount, j, "订单详细Id:" + orderDetailId);
                                        return false;
                                    }

                                    ProductStandard productStandard = ProductStandard.dao.findById(productStandardId);
                                    Integer stock = productStandard.getStock();
                                    if (stock < outNum) {
                                        excelExceptionRender(sheetCount, j, "用户" + userName + "的出货单的商品" + productName + "的规格" + productStandard.getName() + "库存为" + stock + "出货数量为" + outNum);
                                        return false;
                                    }

                                    Integer uid = getSessionAttr(Constant.SESSION_UID);

                                    productStandard.setStock(stock - outNum);
                                    // 0入 1出
                                    boolean update = productStandard.update(UserTypeConstant.A_USER, uid, stock, stock + outNum, "1", productStandard.getName(), productStandard.getProductId(), orderDetail.getProductName());

                                    BigDecimal sellPrice = orderDetail.getSellPrice();
                                    BigDecimal totalPrice = sellPrice.multiply(new BigDecimal(outNum));

                                    OutWarehouseDetail outWarehouseDetail = oldOutWarehouseDetailMap.get(orderDetailId);

                                    if (outWarehouseDetail == null) {
                                        OutWarehouseDetail owd = new OutWarehouseDetail();
                                        owd.setProductId(orderDetail.getProductId());
                                        owd.setProductName(orderDetail.getProductName());
                                        owd.setProductStandardId(orderDetail.getProductStandardId());
                                        owd.setProductStandardName(orderDetail.getProductStandardName());
                                        owd.setProductWeight(productStandard.getSubTitle());
                                        owd.setOutTotalPrice(totalPrice);
                                        owd.setOutId(outId);
                                        owd.setOutAveragePrice(sellPrice);
                                        // 1 商家出货
                                        owd.setOutType(1);
                                        owd.setOutNum(outNum);
                                        owd.setOutPrice(sellPrice);
                                        owd.setOutRemark(outRemark);
                                        owd.setUserId(orderDetail.getUId());
                                        owd.setUserName(userName);
                                        owd.setOrderDetailId(orderDetail.getId());
                                        owd.setOrderNum(orderDetail.getNum());
                                        owd.setOrderTime(orderDetail.getCreateTime());
                                        owd.setUpdateTime(new Date());
                                        owd.setCreateTime(new Date());
                                        // TODO 临时
                                        owd.save();
//                                        productTotalNum += outNum;
//                                        ++productStandardTotalNum;
//                                        allTotalPrice = allTotalPrice.add(totalPrice);
                                        // 避免重复添加
                                        oldOutWarehouseDetailMap.put(orderDetail.getId(), owd);
                                        // 用来统计计算
                                        oldOutWarehouseDetail.add(owd);
                                    } else {
                                        // 只需要更新 库户名 出库数量 重量
                                        outWarehouseDetail.setUserName(userName);
//                                        productTotalNum += outNum;
                                        outWarehouseDetail.setOutNum(outNum);
                                        outWarehouseDetail.setProductWeight(productWeight);
                                        outWarehouseDetail.update();
//                                        allTotalPrice = allTotalPrice.add(totalPrice.subtract(outWarehouseDetail.getOutTotalPrice()));
//                                        if (outNum == 0) {
//                                            --productStandardTotalNum;
//                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    excelExceptionRender(sheetCount, j, e.getMessage());
                                    return false;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            excelExceptionRender(sheetCount, null, e.getMessage());
                            return false;
                        }
                    }

                    // 商品数量
                    Integer productTotalNum = 0;

                    // 商品品类数量
                    Integer productStandardTotalNum = 0;
                    Map<Integer, Integer> productStandardTotalMap = new HashMap<>(10000);

                    // 出库总额
                    BigDecimal allTotalPrice = new BigDecimal(0.00);

                    for (OutWarehouseDetail outWarehouseDetail : oldOutWarehouseDetail) {
                        productTotalNum += outWarehouseDetail.getOutNum();
                        Integer productStandardId = productStandardTotalMap.get(outWarehouseDetail.getProductStandardId());
                        if (productStandardId == null) {
                            productStandardTotalMap.put(outWarehouseDetail.getProductStandardId(), outWarehouseDetail.getProductStandardId());
                        }

                        allTotalPrice = allTotalPrice.add(outWarehouseDetail.getOutTotalPrice());
                    }

                    productStandardTotalNum = productStandardTotalMap.size();

                    OutWarehouse ow = new OutWarehouse();
                    ow.setId(outId);
                    ow.setOutNum(productTotalNum);
                    ow.setOutTypeNum(productStandardTotalNum);
                    ow.setOutTotalPrice(allTotalPrice);
                    ow.update();

                    // 记录库存


                    renderNull();
                    return true;
                } catch (Exception e) {
                    renderErrorText("导入失败");
                    e.printStackTrace();
                    return false;
                }
            }
        });
    }

    private void excelExceptionRender(Integer sheetCount, Integer rowCount, String ErrorMsg) {
        StringBuilder sb = new StringBuilder();
        sb.append("第" + (sheetCount + 1) + "个表");
        if (rowCount != null) {
            sb.append("的第" + (rowCount + 1) + "行");
        }
        sb.append("出现异常").append(ErrorMsg);
        renderErrorText(sb.toString());
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
