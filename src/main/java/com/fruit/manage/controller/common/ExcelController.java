package com.fruit.manage.controller.common;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.ShipmentConstant;
import com.fruit.manage.model.*;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.DateAndStringFormat;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.ExcelStyle;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;
import com.jfinal.aop.Before;
import com.jfinal.ext2.kit.DateTimeKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * excel导入导出集合
 * <p>
 * 1.可以使用通用的excel文件导入导出(提高复用率)
 * 2.各个模块的excel文件导入导出(方便整理各个模块的excel)
 * 3.使用相同文件路径的excel导入导出(统一规范保存路径)
 * <p>
 * 文件上传/文件下载 路径,
 * 在JFConfig的
 * me.setBaseUploadPath(baseUploadPath) / me.setBaseDownloadPath(baseDownloadPath)
 * 设置setBaseDownloadPath即可将基础路径指向项目根径之外，方便单机多实例部署。当该路径参数设置为相对路径时，则是以项目根为基础的相对路径。
 * 即当renderFlie('/xxx/xx')的时候   '/'  =  baseDownloadPath
 * '/xxx/xx' = baseDownloadPath/xxx/xx
 *
 * @author ZGC
 * @date 2018-03-23 11:54
 **/
public class ExcelController extends BaseController {
    private static Logger log = Logger.getLogger(ExcelController.class);

    /**
     * baseDownloadPath = static
     * so 目前的BASE_PATH = 项目根目录/WEB-INF/excel
     * 所有目录存放目录
     */
    private static final String BASE_PATH = CommonController.FILE_PATH;

    /**
     * 下载文件Demo
     * 这是Demo ,使用的时候请修改修饰符为public
     */
    protected void test() {
        String fileName = "商品库信息.txt";
        System.out.println("BASE_PATH:" + BASE_PATH);
        File file = new File(BASE_PATH + File.separator + fileName);
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new RuntimeException(fileName + "文件创建失败");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        renderFile(file);
    }

    /**
     * 采购:导出商品库列表,用于导入采购配额的
     */
    public void getProductStandardAllInfoExcel() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        String fileName = "商品库信息大全.xlsx";
        String[] headers = new String[6];
        // xls表头顺序：商品名，规格名，规格编码，采购姓名，采购人id
        headers[0] = "商品名";
        headers[1] = "商品编码";
        headers[2] = "规格名";
        headers[3] = "规格编码";
        headers[4] = "采购姓名";
        headers[5] = "采购人编码";

        List<ProductStandard> list = ProductStandard.dao.getProductStandardAllInfo();
        List<Object[]> tables = new ArrayList<>();
        for (ProductStandard productStandard : list) {
            Object[] info = new Object[6];
            info[0] = productStandard.get("product_name");
            info[1] = productStandard.get("product_id");
            info[2] = productStandard.get("product_standard_name");
            info[3] = productStandard.get("product_standard_id");
            tables.add(info);
        }
        File file = null;
        try {
            String filePath = ExcelCommon.createExcelModul(BASE_PATH, fileName, "商品库信息大全", User.dao.findById(uid).getName(), headers, tables);

            file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("BASE_PATH:" + BASE_PATH + ",fileName:" + fileName + "  文件不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        renderFile(file);
    }

    /**
     * 采购:导入采购配额
     */
    public void setProcurementQuota() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        String name = User.dao.findById(uid).getName();
        String fileName = getPara("fileName");
        File file = new File(BASE_PATH + File.separator + fileName);
        try {
            // xls表头顺序：商品名,，规格名，规格编码，采购姓名，采购人编码
            List<Object[]> excelData = ExcelCommon.excelRd(file, 4, 1, new ExcelRdTypeEnum[]{
                    ExcelRdTypeEnum.STRING,
                    ExcelRdTypeEnum.INTEGER,
                    ExcelRdTypeEnum.STRING,
                    ExcelRdTypeEnum.INTEGER,
                    ExcelRdTypeEnum.STRING,
                    ExcelRdTypeEnum.INTEGER
            });

            // 所有的商品库信息
            List<ProductStandard> productStandardList = ProductStandard.dao.getProductStandardAllInfo();

            List<ProcurementQuota> ProcurementQuotas = ProcurementQuota.dao.getProcurementQuotaAllInfo();
            Map<Object, ProcurementQuota> productStandardId = ProcurementQuotas.stream().collect(Collectors.toMap(quota -> quota.get("product_standard_id"), Function.identity()));

            List<User> users = User.dao.getAllUser();
            Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));

            for (Object[] excelRow : excelData) {
                for (ProductStandard productStandard : productStandardList) {
                    if (productStandard.get("product_standard_id") != null && productStandard.get("product_standard_id").equals(excelRow[3])) {
                        Integer procurementId = (Integer) excelRow[5];
                        User user = userMap.get(procurementId);
                        String procurementName = user.getName();

                        ProcurementQuota quota = productStandardId.get(productStandard.get("product_standard_id"));
                        if (quota == null) {
                            if (StringUtils.isNotBlank(procurementName) && procurementId != null) {
                                ProcurementQuota procurementQuota = new ProcurementQuota();
                                procurementQuota.setProductId(productStandard.get("product_id"));
                                procurementQuota.setProductName(productStandard.get("product_name"));
                                procurementQuota.setProductStandardName(productStandard.get("product_standard_name"));
                                procurementQuota.setProductStandardId(productStandard.get("product_standard_id"));
                                procurementQuota.setProcurementName(procurementName);
                                procurementQuota.setProcurementId(procurementId);
                                procurementQuota.setCreateUserId(uid);
                                procurementQuota.setCreateUserName(name);
                                procurementQuota.setUpdateTime(new Date());
                                procurementQuota.setCreateTime(new Date());
                                procurementQuota.save();
                            }
                            break;
                        } else {
                            quota.setProductId(productStandard.get("product_id"));
                            quota.setProductName(productStandard.get("product_name"));
                            quota.setProductStandardName(productStandard.get("product_standard_name"));
                            quota.setProductStandardId(productStandard.get("product_standard_id"));
                            quota.setProcurementName(procurementName);
                            quota.setProcurementId(procurementId);
                            quota.setCreateUserId(uid);
                            quota.setCreateUserName(name);
                            quota.setUpdateTime(new Date());
                            quota.update();
                        }
                    }
                }

            }
            renderNull();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExcelRdException e) {
            e.printStackTrace();
        }
        renderErrorText("导入失败,请联系技术修复");
    }


    /**
     * 订单:生成商家出货单
     * <p>
     * 避免混乱,行和单元格都按照这个规范
     * row对象只创建一次,
     * 每次都sheet.createRow(rowCount++)创建获取下一行并指向row引用
     * cellName = c + endColumn(start by 1)
     * <p>
     * excel格式
     * 信息展示(没有固定宽度,默认为三列为一个单元,不够就加单元)
     * 数据(每一列只占一列)
     * 信息展示(没有固定宽度,默认为三列为一个单元,不够就加)
     * <p>
     * 一般数据会比信息展示需要更多的列,所以大概的按3:1来放置
     * <p>
     * 信息展示默认以三列为一个单元
     */
    @Before(Tx.class)
    public void getBusinessSendGoodsBilling() {
        try {
            int rowCount;

            // 创建Excel
            XSSFWorkbook wb = new XSSFWorkbook();

            Calendar calendar = Calendar.getInstance();
            if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
                // 超過11:59:59算明天的訂單
                calendar.add(Calendar.DAY_OF_MONTH, -1);
            }
            String startDateStr = DateTimeKit.formatDateToStyle("yyyy-MM-dd", calendar.getTime()) + " 12:00:00";
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            String endDateStr = DateTimeKit.formatDateToStyle("yyyy-MM-dd", calendar.getTime()) + " 12:00:00";
            String sql = "SELECT " +
                    "o.order_id, " +
                    "bu.`name` AS business_user_name, " +
                    "linfo.buy_phone, " +
                    "linfo.buy_address, " +
                    "linfo.buy_user_name, " +
                    "linfo.delivery_type, " +
                    "au.`name` AS sales_name, " +
                    "au.phone AS sales_phone " +
                    "FROM " +
                    "b_order AS o " +
                    "INNER JOIN b_business_user AS bu ON o.u_id = bu.id " +
                    "INNER JOIN b_business_info AS info ON bu.id = info.u_id " +
                    "INNER JOIN a_user AS au ON bu.a_user_sales_id = au.id " +
                    "LEFT JOIN b_logistics_info AS linfo ON linfo.order_id = o.order_id " +
                    "WHERE " +
                    "o.order_status in (5) " +
                    "AND o.create_time BETWEEN ? " +
                    "AND ? ";
            List<Order> orders = Order.dao.find(sql, startDateStr, endDateStr);
            Date now = new Date();

            if (orders.size() < 1) {
                renderText("没有订单出货,请稍后操作");
                return;
            }
            for (Order order : orders) {
                rowCount = 0;
                String orderId = order.get("order_id");
                String businessUserName = order.get("business_user_name");
                String buyPhone = order.get("buy_phone");
                String buyAddress = order.get("buy_address");
                String buyUserName = order.get("buy_user_name");
                Integer deliveryType = order.get("delivery_type");
                String salesName = order.get("sales_name");
                String salesPhone = order.get("sales_phone");

                // 创建表
                XSSFSheet sheet = wb.createSheet(businessUserName + "_商家出货单");
                // 去除网格线
                sheet.setDisplayGridlines(false);
                sheet.setDefaultRowHeight((short) (512));

                // 标题样式
                XSSFCellStyle styleTitle = ExcelStyle.getStyleTitle(wb, 1);

                // 文本样式
                XSSFCellStyle styleText = ExcelStyle.getStyleText(wb, 3);
                int textHeight = 20;

                // 表样式
                XSSFCellStyle styleTable = ExcelStyle.getStyleTableByTwo(wb, 3);
                int tableHeight = 30;

                // 规范: 设置为1-3合并 ?-6合并 ?-9合并的单元格名称.
                XSSFCell c3;
                XSSFCell c6;
                XSSFCell c9;


                // 1 line
                XSSFRow row = sheet.createRow(rowCount++);
                row.setHeightInPoints(textHeight);
                _mergedRegionNowRow(sheet, row, 1, 9);
                c9 = row.createCell(0);
                c9.setCellStyle(styleTitle);
                c9.setCellValue(DateFormatUtils.format(now, "yyyy-MM-dd") + "广州嘻果出货单" + now.getTime());

                // 2 line
                row = sheet.createRow(rowCount++);
                row.setHeightInPoints(textHeight);
                _mergedRegionNowRow(sheet, row, 1, 3);
                _mergedRegionNowRow(sheet, row, 4, 6);
                _mergedRegionNowRow(sheet, row, 7, 9);
                c3 = row.createCell(0);
                c6 = row.createCell(3);
                c9 = row.createCell(6);
                c3.setCellStyle(styleText);
                c6.setCellStyle(styleText);
                c9.setCellStyle(styleText);
                c3.setCellValue("商家名称:" + businessUserName);
                c6.setCellValue("联系人:" + buyUserName);
                c9.setCellValue("送货电话:" + buyPhone);

                // 3 line
                row = sheet.createRow(rowCount++);
                row.setHeightInPoints(textHeight);
                _mergedRegionNowRow(sheet, row, 1, 9);
                c9 = row.createCell(0);
                c9.setCellStyle(styleText);
                c9.setCellValue("商家地址:" + buyAddress);

                // 4 line
                row = sheet.createRow(rowCount++);
                row.setHeightInPoints(textHeight);
                _mergedRegionNowRow(sheet, row, 1, 3);
                _mergedRegionNowRow(sheet, row, 4, 6);
                _mergedRegionNowRow(sheet, row, 7, 9);
                c3 = row.createCell(0);
                c6 = row.createCell(3);
                c9 = row.createCell(6);
                c3.setCellStyle(styleText);
                c6.setCellStyle(styleText);
                c9.setCellStyle(styleText);
                c3.setCellValue("发车类型:" + ShipmentConstant.SHIPMENT_TYPE.get(deliveryType));
                c6.setCellValue("负责销售:" + salesName);
                c9.setCellValue("联系电话:" + salesPhone);

                // 5 line
                row = sheet.createRow(rowCount++);
                row.setHeightInPoints(textHeight);
                _mergedRegionNowRow(sheet, row, 1, 9);
                c9 = row.createCell(0);
                c9.setCellStyle(styleText);
                c9.setCellValue("配货点：广州江南市场");

                // 6 line
                row = sheet.createRow(rowCount++);
                row.setHeightInPoints(textHeight);
                _mergedRegionNowRow(sheet, row, 1, 3);
                c3 = row.createCell(0);
                c3.setCellStyle(styleText);
                c3.setCellValue("订单号: " + orderId);

                XSSFCell c1;
                XSSFCell c2;
                XSSFCell c4;
                XSSFCell c5;
                XSSFCell c7;

                // data
                row = sheet.createRow(rowCount++);
                row.setHeightInPoints(textHeight);
                c1 = row.createCell(0);
                c2 = row.createCell(1);
                c3 = row.createCell(2);
                c4 = row.createCell(3);
                c5 = row.createCell(4);
                c6 = row.createCell(5);
                c7 = row.createCell(6);
                c1.setCellStyle(styleTable);
                c2.setCellStyle(styleTable);
                c3.setCellStyle(styleTable);
                c4.setCellStyle(styleTable);
                c5.setCellStyle(styleTable);
                c6.setCellStyle(styleTable);
                c7.setCellStyle(styleTable);

                c1.setCellValue("商品名称");
                c2.setCellValue("规格名称");
                c3.setCellValue("规格编号");
                c4.setCellValue("重量（斤）");
                c5.setCellValue("下单数量");
                c6.setCellValue("实发数量");
                c7.setCellValue("商品备注");

                sql = "SELECT " +
                        "od.product_name, " +
                        "od.product_standard_name, " +
                        "od.product_standard_id, " +
                        "ps.weight_price, " +
                        "od.num, " +
                        "od.actual_send_goods_num, " +
                        "od.buy_remark " +
                        "FROM " +
                        "b_order AS o " +
                        "INNER JOIN b_order_detail AS od ON o.order_id = od.order_id " +
                        "INNER JOIN b_product_standard AS ps ON od.product_standard_id = ps.id " +
                        "WHERE o.order_id = ? ";
                List<OrderDetail> orderDetails = OrderDetail.dao.find(sql, orderId);
                for (OrderDetail orderDetail : orderDetails) {
                    row = sheet.createRow(rowCount++);
                    row.setHeightInPoints(tableHeight);
                    c1 = row.createCell(0);
                    c2 = row.createCell(1);
                    c3 = row.createCell(2);
                    c4 = row.createCell(3);
                    c5 = row.createCell(4);
                    c6 = row.createCell(5);
                    c7 = row.createCell(6);
                    c1.setCellStyle(styleTable);
                    c2.setCellStyle(styleTable);
                    c3.setCellStyle(styleTable);
                    c4.setCellStyle(styleTable);
                    c5.setCellStyle(styleTable);
                    c6.setCellStyle(styleTable);
                    c7.setCellStyle(styleTable);

                    c3.setCellType(CellType.NUMERIC);
                    c4.setCellType(CellType.NUMERIC);
                    c5.setCellType(CellType.NUMERIC);
                    c6.setCellType(CellType.NUMERIC);

                    c1.setCellValue(orderDetail.get("product_name").toString());
                    c2.setCellValue(orderDetail.get("product_standard_name").toString());
                    c3.setCellValue((Integer) orderDetail.get("product_standard_id"));
                    c4.setCellValue(((BigDecimal) orderDetail.get("weight_price")).doubleValue());
                    c5.setCellValue((Integer) orderDetail.get("num"));
                    c6.setCellValue(0);
                    c7.setCellValue(orderDetail.get("buy_remark") != null ? orderDetail.get("buy_remark").toString() : null);

                }
                // 添加三行空行
//            sheet.createRow(rowCount++).setRowStyle(styleTable);
//            sheet.createRow(rowCount++).setRowStyle(styleTable);
//            sheet.createRow(rowCount++).setRowStyle(styleTable);

                // bottom 1 line
                row = sheet.createRow(rowCount++);
                row.setHeightInPoints(textHeight);
                _mergedRegionNowRow(sheet, row, 1, 9);
                c9 = row.createCell(0);
                c9.setCellStyle(styleText);
                c9.setCellValue("温馨提示：运费和装车费、三轮车费、包装费、短途费/中转费，均按实际产生费用收取");

                // bottom 2 line
                row = sheet.createRow(rowCount++);
                row.setHeightInPoints(textHeight);
                _mergedRegionNowRow(sheet, row, 1, 3);
                _mergedRegionNowRow(sheet, row, 4, 6);
                _mergedRegionNowRow(sheet, row, 7, 9);
                c3 = row.createCell(0);
                c6 = row.createCell(3);
                c9 = row.createCell(6);
                c3.setCellStyle(styleText);
                c6.setCellStyle(styleText);
                c9.setCellStyle(styleText);
                c3.setCellValue("点单:");
                c6.setCellValue("核单:");
                c9.setCellValue("打泡:");

            }

            HttpServletResponse response = getResponse();
            OutputStream output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(DateFormatUtils.format(now, "yyyy年MM月dd日") + "商家出货单.xlsx", "UTF-8"));
            response.setContentType("application/excel");
            wb.write(output);
            output.flush();
            output.close();
        } catch (Exception e) {
            renderErrorText("导出失败,出现未知异常,请联系技术,时间为:" + DateFormatUtils.format(new Date(), "yyyy-MM-dd hh:ss:mm"));
            e.printStackTrace();
        }
    }


    /**
     * 订单:生成商家收款单
     * <p>
     * 避免混乱,行和单元格都按照这个规范
     * row对象只创建一次,
     * 每次都sheet.createRow(rowCount++)创建获取下一行并指向row引用
     * cellName = c + endColumn(start by 1)
     * <p>
     * excel格式
     * 信息展示(没有固定宽度,默认为三列为一个单元,不够就加单元)
     * 数据(每一列只占一列)
     * 信息展示(没有固定宽度,默认为三列为一个单元,不够就加)
     * <p>
     * 一般数据会比信息展示需要更多的列,所以大概的按3:1来放置
     * <p>
     * 信息展示默认以三列为一个单元
     */
    @Before(Tx.class)
    public void getBusinessCollectionBilling() {
        int rowCount;

        // 创建Excel
        XSSFWorkbook wb = new XSSFWorkbook();

        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
            // 超過11:59:59算明天的訂單
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        String startDateStr = DateTimeKit.formatDateToStyle("yyyy-MM-dd", calendar.getTime()) + " 12:00:00";
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        String endDateStr = DateTimeKit.formatDateToStyle("yyyy-MM-dd", calendar.getTime()) + " 12:00:00";
        String sql = "SELECT " +
                "o.order_id, " +
                "bu.`name` AS business_user_name, " +
                "linfo.buy_phone, " +
                "linfo.buy_address, " +
                "linfo.buy_user_name, " +
                "linfo.delivery_type, " +
                "au.`name` AS sales_name, " +
                "au.phone AS sales_phone " +
                "FROM " +
                "b_order AS o " +
                "INNER JOIN b_business_user AS bu ON o.u_id = bu.id " +
                "INNER JOIN b_business_info AS info ON bu.id = info.u_id " +
                "INNER JOIN a_user AS au ON bu.a_user_sales_id = au.id " +
                "INNER JOIN b_logistics_info AS linfo ON linfo.order_id = o.order_id " +
                "WHERE " +
                "o.order_status in (15,20,25,30) " +
                "AND o.create_time BETWEEN ? " +
                "AND ? ";
        System.out.println(sql);
        System.out.println(startDateStr);
        System.out.println(endDateStr);
        List<Order> orders = Order.dao.find(sql, startDateStr, endDateStr);
        Date now = new Date();

        if (orders.size() < 1) {
            renderText("没有订单出货,请稍后操作");
            return;
        }
        for (Order order : orders) {
            rowCount = 0;
            String orderId = order.get("order_id");
            String businessUserName = order.get("business_user_name");
            String buyPhone = order.get("buy_phone");
            String buyAddress = order.get("buy_address");
            String buyUserName = order.get("buy_user_name");
            Integer deliveryType = order.get("delivery_type");
            String salesName = order.get("sales_name");
            String salesPhone = order.get("sales_phone");

            // 创建表
            XSSFSheet sheet = wb.createSheet(businessUserName + "_商家出货单");
            // 去除网格线
            sheet.setDisplayGridlines(false);
            sheet.setDefaultRowHeight((short) (512));

            // 标题样式
            XSSFCellStyle styleTitle = ExcelStyle.getStyleTitle(wb, 2);

            // 文本样式
            XSSFCellStyle styleText = ExcelStyle.getStyleText(wb, 3);
            int textHeight = 20;

            // 表样式
            XSSFCellStyle styleTable = ExcelStyle.getStyleTableByOne(wb, 3);
            int tableHeight = 30;

            // 规范: 设置为1-3合并 ?-6合并 ?-9合并的单元格名称.
            XSSFCell c3;
            XSSFCell c6;
            XSSFCell c9;


            // 1 line
            XSSFRow row = sheet.createRow(rowCount++);
            row.setHeightInPoints(textHeight);
            _mergedRegionNowRow(sheet, row, 1, 9);
            c9 = row.createCell(0);
            c9.setCellStyle(styleTitle);
            c9.setCellValue(DateFormatUtils.format(now, "yyyy-MM-dd") + "广州嘻果出货单" + now.getTime());

            // 2 line
            row = sheet.createRow(rowCount++);
            row.setHeightInPoints(textHeight);
            row.setHeightInPoints(textHeight);
            _mergedRegionNowRow(sheet, row, 1, 3);
            _mergedRegionNowRow(sheet, row, 4, 6);
            _mergedRegionNowRow(sheet, row, 7, 9);
            c3 = row.createCell(0);
            c6 = row.createCell(3);
            c9 = row.createCell(6);
            c3.setCellStyle(styleText);
            c6.setCellStyle(styleText);
            c9.setCellStyle(styleText);
            c3.setCellValue("商家名称:" + businessUserName);
            c6.setCellValue("联系人:" + buyUserName);
            c9.setCellValue("送货电话:" + buyPhone);

            // 3 line
            row = sheet.createRow(rowCount++);
            row.setHeightInPoints(textHeight);
            _mergedRegionNowRow(sheet, row, 1, 9);
            c9 = row.createCell(0);
            c9.setCellStyle(styleText);
            c9.setCellValue("商家地址:" + buyAddress);

            // 4 line
            row = sheet.createRow(rowCount++);
            row.setHeightInPoints(textHeight);
            _mergedRegionNowRow(sheet, row, 1, 3);
            _mergedRegionNowRow(sheet, row, 4, 6);
            _mergedRegionNowRow(sheet, row, 7, 9);
            c3 = row.createCell(0);
            c6 = row.createCell(3);
            c9 = row.createCell(6);
            c3.setCellStyle(styleText);
            c6.setCellStyle(styleText);
            c9.setCellStyle(styleText);
            c3.setCellValue("发车类型:" + ShipmentConstant.SHIPMENT_TYPE.get(deliveryType));
            c6.setCellValue("负责销售:" + salesName);
            c9.setCellValue("联系电话:" + salesPhone);

            // 5 line
            row = sheet.createRow(rowCount++);
            row.setHeightInPoints(textHeight);
            _mergedRegionNowRow(sheet, row, 1, 9);
            c9 = row.createCell(0);
            c9.setCellStyle(styleText);
            c9.setCellValue("配货点：广州江南市场");

            XSSFCell c1;
            XSSFCell c2;
            XSSFCell c4;
            XSSFCell c5;
            XSSFCell c7;
            XSSFCell c8;

            // data
            row = sheet.createRow(rowCount++);
            row.setHeightInPoints(tableHeight);
            c1 = row.createCell(0);
            c2 = row.createCell(1);
            c3 = row.createCell(2);
            c4 = row.createCell(3);
            c5 = row.createCell(4);
            c6 = row.createCell(5);
            c7 = row.createCell(6);
            c8 = row.createCell(7);
            c1.setCellStyle(styleTable);
            c2.setCellStyle(styleTable);
            c3.setCellStyle(styleTable);
            c4.setCellStyle(styleTable);
            c5.setCellStyle(styleTable);
            c6.setCellStyle(styleTable);
            c7.setCellStyle(styleTable);
            c8.setCellStyle(styleTable);

            c1.setCellValue("商品名称");
            c2.setCellValue("规格名称");
            c3.setCellValue("重量（斤）");
            c4.setCellValue("下单数量");
            c5.setCellValue("实发数量");
            c6.setCellValue("单价");
            c7.setCellValue("总额");
            c8.setCellValue("商品备注");

            sql = "SELECT " +
                    "od.product_name, " +
                    "od.product_standard_name, " +
                    "ps.weight_price, " +
                    "od.num, " +
                    "od.actual_send_goods_num, " +
                    "od.sell_price, " +
                    "o.pay_reality_need_money, " +
                    "od.buy_remark " +
                    "FROM " +
                    "b_order AS o " +
                    "INNER JOIN b_order_detail AS od ON o.order_id = od.order_id " +
                    "INNER JOIN b_product_standard AS ps ON od.product_standard_id = ps.id " +
                    "WHERE o.order_id = ? ";
            List<OrderDetail> orderDetails = OrderDetail.dao.find(sql, orderId);
            for (OrderDetail orderDetail : orderDetails) {
                row = sheet.createRow(rowCount++);
                row.setHeightInPoints(textHeight);
                c1 = row.createCell(0);
                c2 = row.createCell(1);
                c3 = row.createCell(2);
                c4 = row.createCell(3);
                c5 = row.createCell(4);
                c6 = row.createCell(5);
                c7 = row.createCell(6);
                c8 = row.createCell(7);
                c1.setCellStyle(styleTable);
                c2.setCellStyle(styleTable);
                c3.setCellStyle(styleTable);
                c4.setCellStyle(styleTable);
                c5.setCellStyle(styleTable);
                c6.setCellStyle(styleTable);
                c7.setCellStyle(styleTable);
                c8.setCellStyle(styleTable);

                c1.setCellValue(orderDetail.get("product_name").toString());
                c2.setCellValue(orderDetail.get("product_standard_name").toString());
                c3.setCellValue(orderDetail.get("weight_price").toString());
                c4.setCellValue(orderDetail.get("num").toString());
                c5.setCellValue(orderDetail.get("actual_send_goods_num").toString());
                c6.setCellValue(orderDetail.get("sell_price").toString());
                c7.setCellValue(orderDetail.get("pay_reality_need_money") != null ? orderDetail.get("pay_reality_need_money").toString() : null);
                c8.setCellValue(orderDetail.get("buy_remark") != null ? orderDetail.get("buy_remark").toString() : null);
            }
        }

        try {
            HttpServletResponse response = getResponse();
            OutputStream output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(DateFormatUtils.format(now, "yyyy年MM月dd日") + "商家收款单.xlsx", "UTF-8"));
            response.setContentType("application/excel");
            wb.write(output);
            output.flush();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        renderNull();
    }


    private static void _mergedRegionNowRow(XSSFSheet sheet, XSSFRow row, int firstCol, int lastCol) {
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), firstCol - 1, lastCol - 1));
    }


    /**
     * 采购:根据采购计划ID导出采购计划单
     */
    public void getProcurementPlanBilling() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);

        User user = User.dao.findById(uid);

        Date createTime = getParaToDate("createTime");

        String[] createTimes = new String[2];

        if (createTime != null) {
            // 使用指定时间导出采购计划
            String createTimeStr = DateAndStringFormat.getStringDateShort(createTime);
            createTimes[0] = DateAndStringFormat.getNextDay(createTimeStr, "-1") + " 12:00:00";
            createTimes[1] = createTimeStr + " 11:59:59";
        } else {
            // 使用当前时间导出采购计划
            Calendar nowCalendar = Calendar.getInstance();
            if (nowCalendar.get(Calendar.HOUR_OF_DAY) < 12) {
                nowCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            createTimes[0] = DateFormatUtils.format(nowCalendar.getTime(), "yyyy-MM-dd") + " 12:00:00";
            nowCalendar.add(Calendar.DAY_OF_MONTH, 1);
            createTimes[1] = DateFormatUtils.format(nowCalendar.getTime(), "yyyy-MM-dd") + " 12:00:00";
            createTime = nowCalendar.getTime();
        }

        // 获取要导出数据
        List<ProcurementPlan> planList = ProcurementPlan.dao.getExportDataByPPlanID(createTimes);

        if (planList.size() < 1) {
            renderText("没有可导出的采购计划");
            return;
        }

        Map<String, List<ProcurementPlan>> procurementPlanGroup = planList.stream().collect(
                Collectors.groupingBy(
                        plan -> plan.get("procurement_name")
                )
        );
        String[] headers = {"商品名", "规格名", "规格编码", "重量(斤)", "报价", "下单量", "库存量", "采购量", "采购单价", "下单备注"};

        XSSFWorkbook wb = new XSSFWorkbook();

        // 标题样式
        XSSFCellStyle styleTitle = ExcelStyle.getStyleTitle(wb, 2);

        // 文本样式
        XSSFCellStyle styleText = ExcelStyle.getStyleText(wb, 3);
        int textHeight = 20;

        // 表样式
        XSSFCellStyle styleTable = ExcelStyle.getStyleTableByOne(wb, 3);
        int tableHeight = 30;


        procurementPlanGroup.forEach(
                (productStandardName, procurementPlanList) -> {
                    try {
                        XSSFSheet sheet = wb.createSheet();
                        sheet.setDisplayGridlines(false);
                        sheet.setDefaultRowHeight((short) (512));

                        int rowCount = 0;
                        XSSFRow row;

                        // 标题
                        row = sheet.createRow(rowCount++);
                        _mergedRegionNowRow(sheet, row, 1, headers.length);
                        XSSFCell titleCell = row.createCell(0);
                        titleCell.setCellStyle(styleTitle);
                        titleCell.setCellValue("采购计划表");

                        // 其他信息
                        row = sheet.createRow(rowCount++);
                        _mergedRegionNowRow(sheet, row, 1, 3);
                        _mergedRegionNowRow(sheet, row, 4, 7);
                        XSSFCell nowTime = row.createCell(0);
                        XSSFCell createBy = row.createCell(3);
                        nowTime.setCellStyle(styleText);
                        nowTime.setCellValue("创建时间: " + DateFormatUtils.format(new Date(), "yyyy-MM-dd hh:ss:mm"));
                        createBy.setCellStyle(styleText);
                        createBy.setCellValue("采购人:" + productStandardName);


                        row = sheet.createRow(rowCount++);
                        int cellCount = 0;
                        for (String header : headers) {
                            XSSFCell cell = row.createCell(cellCount++);
                            cell.setCellStyle(styleTable);
                            cell.setCellValue(header);
                        }

                        for (ProcurementPlan procurementPlan : procurementPlanList) {
                            cellCount = 0;
                            row = sheet.createRow(rowCount++);
                            XSSFCell c1 = row.createCell(cellCount++);
                            XSSFCell c2 = row.createCell(cellCount++);
                            XSSFCell c3 = row.createCell(cellCount++);
                            XSSFCell c4 = row.createCell(cellCount++);
                            XSSFCell c5 = row.createCell(cellCount++);
                            XSSFCell c6 = row.createCell(cellCount++);
                            XSSFCell c7 = row.createCell(cellCount++);
                            XSSFCell c8 = row.createCell(cellCount++);
                            XSSFCell c9 = row.createCell(cellCount++);
                            XSSFCell c10 = row.createCell(cellCount++);
                            c1.setCellStyle(styleTable);
                            c2.setCellStyle(styleTable);
                            c3.setCellStyle(styleTable);
                            c4.setCellStyle(styleTable);
                            c5.setCellStyle(styleTable);
                            c6.setCellStyle(styleTable);
                            c7.setCellStyle(styleTable);
                            c8.setCellStyle(styleTable);
                            c9.setCellStyle(styleTable);
                            c10.setCellStyle(styleTable);
                            c3.setCellType(CellType.NUMERIC);
                            c4.setCellType(CellType.NUMERIC);
                            c5.setCellType(CellType.NUMERIC);
                            c6.setCellType(CellType.NUMERIC);
                            c7.setCellType(CellType.NUMERIC);
                            c8.setCellType(CellType.NUMERIC);
                            c9.setCellType(CellType.NUMERIC);
                            c1.setCellValue(procurementPlan.get("productName") + "");
                            c2.setCellValue(procurementPlan.get("productStandardName") + "");
                            c3.setCellValue(procurementPlan.get("productStandardID") + "");
                            c4.setCellValue(procurementPlan.get("fruitWeight") + "");
                            c5.setCellValue(procurementPlan.get("sellPrice") + "");
                            c6.setCellValue(procurementPlan.get("purchaseNum") + "");
                            c7.setCellValue(procurementPlan.get("inventoryNum") + "");
                            c8.setCellValue(procurementPlan.get("procurementNum") + "");
                            c9.setCellValue(procurementPlan.get("procurementPrice") + "");
                            c10.setCellValue(procurementPlan.get("procurementRemark") + "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );

        try {
            HttpServletResponse response = getResponse();
            OutputStream output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(DateFormatUtils.format(createTime, "yyyy年MM月dd日") + "采购计划.xlsx", "UTF-8"));
            response.setContentType("application/excel");
            wb.write(output);
            output.flush();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        renderNull();
    }

    /**
     * 导入商品规格信息
     */
    public void importProduct() {
        try {
            ArrayList<ExcelRdTypeEnum> types = new ArrayList<ExcelRdTypeEnum>();
            //1
            types.add(ExcelRdTypeEnum.INTEGER);
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.STRING);
            //5
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.DOUBLE);
            types.add(ExcelRdTypeEnum.DOUBLE);
            //10
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.STRING);
            //15
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.INTEGER);
            types.add(ExcelRdTypeEnum.STRING);
            //20
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.STRING);
            types.add(ExcelRdTypeEnum.INTEGER);
            types.add(ExcelRdTypeEnum.INTEGER);
            // 25
            types.add(ExcelRdTypeEnum.DATE);
            types.add(ExcelRdTypeEnum.DATE);
            List<Object[]> excel = ExcelCommon.excelRd(new File("C:\\Users\\Administrator\\Desktop\\import (1).xlsx"), 3, 1, types.toArray(new ExcelRdTypeEnum[27]));

            ArrayList<String[]> errorRow = new ArrayList<>();

            // key 商品名称  value 保存的商品id ,用于取商品id
            HashMap<String, Integer> productInfos = new HashMap<>();

            HashMap<String, ArrayList<String>> productStandardIds = new HashMap<>();

            ArrayList<String> fruitTypes = new ArrayList<>();

            int totalCount = 0;
            int count = 0;

            for (Object[] row : excel) {
//                System.out.print("这是第"+totalCount+"条数据");
//                for (Object o : row) {
//                    System.out.print(o+"  ");
//                }
//                System.out.println();
                totalCount++;
                Db.tx(new IAtom() {
                    @Override
                    public boolean run() throws SQLException {
                        //是否开启
                        Integer status = (int) (Double.parseDouble(row[16] + ""));
                        // 逻辑(换算和整合)
                        if (status == 0) {
                            return false;
                        }
                        //商品编号
//                        String productId = (String) row[0];
                        //商品名称
                        String productName = (String) row[1];
                        //规格
                        String productStandardName = (String) row[2];
                        //采购人
                        String procurementName = (String) row[3];
                        //商品规格编号
//                        String productStandardId = (String) row[4];
                        //建议零售价 ------------------
                        String xx6 = (String) row[5];
                        //平台价
                        Double sellPrice = Double.parseDouble(row[6] + "");
                        //成本价
                        Double theirPrice = Double.parseDouble(row[7] + "");
                        //水果描述
                        String fruit_des = (String) row[8];
                        //保存方式
                        String storeWay = (String) row[9];
                        //水果类型 fruit_type
                        String fruit_type = (String) row[10];
                        //国家  -------------
                        String country = (String) row[11];
                        //地区  -------------
                        String province = (String) row[12];
                        //权重 -------------
                        String sort = (String) row[13];
                        //品牌  -------------
                        String brand = (String) row[14];
                        //计量单位 -------------
                        String measure_unit = (String) row[15];

                        //保鲜时间 -------------
                        String fresh_expire_time = row[17] + "";
                        //图片 img
                        String img = "http://www.atool.org/placeholder.png?size=300x150&bg=868686&fg=fff";
                        //store_way -------------
                        String xx21 = (String) row[19];
                        //total_sell_num
                        Integer total_sell_num = 0;
                        //week_sell_num
                        Integer week_sell_num = 0;
                        //创建时间
//                        String create_time = (String) row[22];
                        //更新时间
//                        String update_time = (String) row[23];


                        // 添加
                        try {
                            Integer productId = productInfos.get(productName);
                            if (productId == null) {
                                Product product = new Product();
                                product.setImg(img);
                                product.setName(productName);
                                product.setCountry(country);
                                if (StringUtils.isNotBlank(province)) {
                                    product.setProvince(province);
                                }
                                product.setSort(10L);
                                measure_unit = StringUtils.isNotBlank(measure_unit) ? measure_unit : "件";
                                product.setMeasureUnit(measure_unit);
                                brand = (StringUtils.isNoneBlank(brand)) ? brand : "暂无";
                                product.setBrand(brand);
                                product.setStatus(status);
                                product.setImg(img);
                                product.setFruitType(fruit_type);
                                product.setTotalSellNum(0);
                                product.setWeekSellNum(0);
                                product.setFruitDes(fruit_des);
                                product.setStoreWay(storeWay);
                                product.setCreateTime(new Date());
                                product.setUpdateTime(new Date());
                                Product.dao.save(product, new String[]{img}, new String[]{fruit_type}, new Integer[]{28}, new Integer[]{1, 2, 3, 4, 5});

                                productId = product.getId();
                                productInfos.put(productName, productId);
                            }

//                            System.out.print("这是第"+totalCount+"条数据");
                            for (Object o : row) {
                                System.out.print(o + "  ");
                            }
                            System.out.println();

                            /**
                             * id
                             * product_id
                             * name
                             * sub_title
                             * original_price
                             * sell_price
                             * weight_price
                             * cost_price
                             * shipping_fee
                             * carton_weight
                             *
                             * fruit_weight
                             * gross_weight
                             * purchase_quantity_min
                             * purchase_quantity_max
                             * buy_start_time
                             * buy_end_time
                             * sort_purchase
                             * sort_sold_out
                             * sort_split
                             * stock
                             * status
                             * is_default
                             * purchaser_uid
                             * create_time
                             * update_time
                             */

                            ProductStandard productStandard = new ProductStandard();
                            productStandard.setProductId(productId);
                            productStandard.setName(productStandardName);
                            productStandard.setSubTitle("");
                            productStandard.setOriginalPrice(new BigDecimal(sellPrice).add(new BigDecimal(sellPrice).multiply(new BigDecimal(0.2))));
                            productStandard.setSellPrice(new BigDecimal(sellPrice));
                            productStandard.setWeightPrice(new BigDecimal(0));
                            productStandard.setCostPrice(new BigDecimal(theirPrice));
                            productStandard.setShippingFee(new BigDecimal(0));
                            productStandard.setCartonWeight(0d);
                            productStandard.setFruitWeight(0d);
                            productStandard.setGrossWeight(0d);
                            productStandard.setPurchaseQuantityMin(1);
                            productStandard.setPurchaseQuantityMax(999);
                            productStandard.setSortPurchase(50);
                            productStandard.setSortSoldOut(50);
                            productStandard.setSortSplit(50);
                            productStandard.setStock(0);
                            productStandard.setStatus(status);
                            productStandard.setIsDefault(0);
                            productStandard.setPurchaserUid(103);
                            productStandard.setCreateTime(new Date());
                            productStandard.setUpdateTime(new Date());
                            productStandard.save();

                            /**
                             * id
                             * product_id
                             * product_name
                             * product_standard_id  *
                             * product_standard_name
                             * procurement_id   *
                             * procurement_name
                             * procurement_phone
                             * create_user_id
                             * create_user_name
                             * create_time   *
                             * update_time
                             */


                            ProcurementQuota procurementQuota = new ProcurementQuota();
                            procurementQuota.setProductId(productId);
                            procurementQuota.setProductName(productName);
                            procurementQuota.setProductStandardId(productStandard.getId());
                            procurementQuota.setProductStandardName(productStandardName);
                            switch (procurementName) {
                                case "A":
                                    procurementName = "钟华";
                                    break;
                                case "B":
                                    procurementName = "潘建雄";
                                    break;
                                case "C":
                                    procurementName = "曹雄斌";
                                    break;
                                case "D":
                                    procurementName = "林镇全";
                                    break;
                                default:
                                    break;
                            }
                            User user = User.dao.findFirst("select * from a_user where nick_name LIKE ? ", "%" + procurementName + "%");
                            procurementQuota.setProcurementName(procurementName);
                            procurementQuota.setProcurementId(user.getId());
                            procurementQuota.setProcurementPhone(user.getPhone());
                            procurementQuota.setCreateUserId(user.getId());
                            procurementQuota.setCreateUserName(user.getNickName());
                            procurementQuota.setUpdateTime(new Date());
                            procurementQuota.setCreateTime(new Date());
                            procurementQuota.save();

                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                });
                // 数据


            }
            System.out.println("总数据源" + totalCount + ",一共添加了" + totalCount + "条数据");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 导入商品图片
     */
    public void importProductImg() {
        File imgFolders = new File("C:\\Users\\Administrator\\Desktop\\图片库");
        File[] Folders = imgFolders.listFiles();
        for (File folder : Folders) {
            String folderName = folder.getName();
            List<Product> products = Product.dao.find("SELECT * FROM b_product ");
            Map<String, Product> productMap = products.stream().collect(Collectors.toMap(Product::getName, Function.identity()));
            Product product = productMap.get(folderName);
            if (product !=null){
                File[] files = folder.listFiles();
                ArrayList<String> imgUrls = new ArrayList<>();
                int imgCount = 0;
                for (File file : files) {
                    if (file.getName().contains("jpg")||file.getName().contains("png")){
                        String imgUrl = "http://192.168.3.123:8080/upload/file/" + folderName + "/" + file.getName();
                        System.out.println(imgUrl);
                        imgUrls.add(imgUrl);
                        imgCount++;
                    }
                }
                product.setImg("http://192.168.3.123:8080/upload/file/" + folderName + "/01.jpg");
                product.update();
                String[] imgs = imgUrls.toArray(new String[imgCount]);
                boolean b = ProductImg.dao.saveProductImg(true, product.getId(), 1, imgs);
                if (!b) {
                    System.out.println("存在导入失败的图片");
                }
            }else {
                System.out.println("不存在改这个商品" + folderName);
            }
        }
    }


}
