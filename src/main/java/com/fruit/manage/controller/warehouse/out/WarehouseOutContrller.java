package com.fruit.manage.controller.warehouse.out;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.OrderDetail;
import com.fruit.manage.model.OutWarehouse;
import com.fruit.manage.model.OutWarehouseDetail;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.ExcelException;
import com.jfinal.ext.kit.DateKit;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.math.BigDecimal;
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
        String[] createTime = getParaValues("create_time");

        renderJson(OutWarehouse.dao.getData(pageNum, pageSize, orderBy, isASC, createTime,outWarehouse));
    }

    /**
     * 添加出库表
     */
    public void save() {
        String CreateTimeStr = getPara("create_Time");
        Integer out_type = getParaToInt("out_type");
        Date createTime = DateKit.toDate(CreateTimeStr);
        System.out.println(createTime);
        OutWarehouse outWarehouse = new OutWarehouse();
        outWarehouse.setOutTime(createTime);
        outWarehouse.setOutType(out_type);
        outWarehouse.setUpdateTime(new Date());
        outWarehouse.setCreateTime(new Date());
        outWarehouse.setWarehouseAddress("运城");
        outWarehouse.save();
        renderNull();
    }

    /**
     * 刪除
     */
    public void delete() {
        Integer id = getParaToInt("id");
        OutWarehouse outWarehouse = new OutWarehouse();
        outWarehouse.setId(id);
        boolean delete = outWarehouse.delete();
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
     * 导入商家出库单
     */
    public void importExcelOutWarehouse() {

        try {
            // TODO 未取到前端传来的owId
            String fileName = getPara("fileName");
            Integer outId = getParaToInt("out_id");

            File file = new File(CommonController.FILE_PATH + File.separator + fileName);
            XSSFWorkbook sheets = new XSSFWorkbook(file);

            Integer colTotalNum = 7;

            // 商品数量
            Integer productTotalNum = 0;

            // 商品品类数量
            Integer productStandardTotalNum = 0;

            // 出库总额
            BigDecimal allTotalPrice = new BigDecimal(0.00);

            // 获取出库详细的历史数据
            List<OutWarehouseDetail> oldOutWarehouseDetail = OutWarehouseDetail.dao.getOutWarehouseDetail(outId);
            Map<Integer, OutWarehouseDetail> oldOutWarehouseDetailMap = oldOutWarehouseDetail == null? new HashMap<>(1):oldOutWarehouseDetail.stream().collect(Collectors.toMap(OutWarehouseDetail::getProductStandardId, Function.identity()));

            for (OutWarehouseDetail outWarehouseDetail : oldOutWarehouseDetail) {
                productTotalNum += outWarehouseDetail.getOutNum();
                ++productStandardTotalNum;
                allTotalPrice = allTotalPrice.add(outWarehouseDetail.getOutTotalPrice());
            }

            // 不止一张表
            for (Sheet sheet : sheets) {

                // 获取用户名
                String userName = sheet.getRow(1).getCell(0).getStringCellValue();
                userName = userName.substring(userName.indexOf(":") + 1);

                // 订单号
                String orderId = sheet.getRow(5).getCell(0).getStringCellValue();
                orderId = orderId.substring(orderId.indexOf(":") + 1);

                // 获取所有订单详细
                List<OrderDetail> orderDetails = OrderDetail.dao.getOrderDetails(orderId);
                Map<Integer, OrderDetail> orderDetailMap = orderDetails.stream().collect(Collectors.toMap(OrderDetail::getProductStandardId, Function.identity()));

                // 列表在7行开始,8倒数第3行结束 (+1-2 = -1)
                for (int j = 7; j < sheet.getLastRowNum() - 1; j++) {
                    // 0        1         2      3         4     5       6
                    // 商品名称,规格名称,规格编码,重量（斤,下单数量,实发数量,商品备注

                    Row row = sheet.getRow(j);

                    // 一共有7列
                    Integer productStandardId = Integer.valueOf(row.getCell(2).getStringCellValue());
                    BigDecimal productWeight = new BigDecimal(row.getCell(3).getStringCellValue());
                    Integer outNum = Integer.valueOf(row.getCell(5).getStringCellValue());
                    String outRemark = row.getCell(6).getStringCellValue();


                    OrderDetail orderDetail = orderDetailMap.get(productStandardId);
                    if (orderDetail == null) {
                        System.out.println("orderId:" + orderId + " 不存在productStandardId:" + productStandardId);
                        continue;
                    }

                    BigDecimal sellPrice = orderDetail.getSellPrice();
                    BigDecimal totalPrice = sellPrice.multiply(new BigDecimal(outNum));

                    OutWarehouseDetail outWarehouseDetail = oldOutWarehouseDetailMap.get(productStandardId);

                    if (outWarehouseDetail == null) {
                        OutWarehouseDetail owd = new OutWarehouseDetail();
                        owd.setProductId(orderDetail.getProductId());
                        owd.setProductName(orderDetail.getProductName());
                        owd.setProductStandardId(orderDetail.getProductStandardId());
                        owd.setProductStandardName(orderDetail.getProductStandardName());
                        owd.setProductWeight(productWeight);
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
                        owd.setOrderNum(orderDetail.getNum());
                        owd.setOrderTime(orderDetail.getCreateTime());
                        owd.setUpdateTime(new Date());
                        owd.setCreateTime(new Date());
                        // TODO 临时
                        owd.save();
                        productTotalNum += outNum;
                        ++productStandardTotalNum;
                        allTotalPrice = allTotalPrice.add(totalPrice);
                        // 避免重复添加
                        oldOutWarehouseDetailMap.put(orderDetail.getProductStandardId(),owd);
                    } else {
                        // 只需要更新 库户名 出库数量 重量
                        outWarehouseDetail.setUserName(userName);
                        productTotalNum += outNum - outWarehouseDetail.getOutNum();
                        outWarehouseDetail.setOutNum(outNum);
                        outWarehouseDetail.setProductWeight(productWeight);
                        allTotalPrice = allTotalPrice.add(totalPrice.subtract(outWarehouseDetail.getOutTotalPrice()));
                        if (outNum == 0) {
                            --productStandardTotalNum;
                        }
                    }
                }
            }

            OutWarehouse ow = new OutWarehouse();
            ow.setId(outId);
            ow.setOutNum(productTotalNum);
            ow.setOutTypeNum(productStandardTotalNum);
            ow.setOutTotalPrice(allTotalPrice);
            ow.update();

            renderNull();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
