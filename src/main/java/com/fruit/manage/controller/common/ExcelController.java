package com.fruit.manage.controller.common;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Order;
import com.fruit.manage.model.ProcurementQuota;
import com.fruit.manage.model.ProductStandard;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.ExcelStyle;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     * 获取系统中的所有商品规格信息
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
     * 导入采购配额
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

            List<ProductStandard> productStandardList = ProductStandard.dao.getProductStandardAllInfo();

            for (Object[] excelRow : excelData) {
                for (ProductStandard productStandard : productStandardList) {
                    if (productStandard.get("product_standard_id") != null && productStandard.get("product_standard_id").equals(excelRow[3])) {
                        if (StringUtils.isNotBlank((String) excelRow[4]) && excelRow[5] != null) {
                            ProcurementQuota procurementQuota = new ProcurementQuota();
                            procurementQuota.setProductId(productStandard.get("product_id"));
                            procurementQuota.setProductName(productStandard.get("product_name"));
                            procurementQuota.setProductStandardName(productStandard.get("product_standard_name"));
                            procurementQuota.setProductStandardId(productStandard.get("product_standard_id"));
                            procurementQuota.setProcurementName((String) excelRow[4]);
                            procurementQuota.setProcurementId((Integer) excelRow[5]);
                            procurementQuota.setCreateTime(new Date());
                            procurementQuota.setCreateUserId(uid);
                            procurementQuota.setCreateUserName(name);
                            procurementQuota.save();
                        }
                        break;
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
     * 生成商家出货单
     *
     * 避免混乱,行和单元格都按照这个规范
     *  row对象只创建一次,
     *  每次都sheet.createRow(rowCount++)创建获取下一行并指向row引用
     *  cellName = c + endColumn(start by 1)
     *
     *  excel格式
     *  信息展示(没有固定宽度,默认为三列为一个单元,不够就加单元)
     *  数据(每一列只占一列)
     *  信息展示(没有固定宽度,默认为三列为一个单元,不够就加)
     *
     *  一般数据会比信息展示需要更多的列,所以大概的按3:1来放置
     *
     *  信息展示默认以三列为一个单元
     */
    public void getBusinessSendGoodsBilling() {
        List<Order> orders = Order.dao.find("");

        int rowCount = 0;

        // 创建Excel
        XSSFWorkbook wb = new XSSFWorkbook();

        // 创建表
        XSSFSheet sheet = wb.createSheet("_商家出货单");
        // 去除网格线
        sheet.setDisplayGridlines(false);

        // 获取样式
        XSSFCellStyle titleOne = ExcelStyle.getStyleTitle(wb, 1);
        XSSFCellStyle titleTwo = ExcelStyle.getStyleTitle(wb, 2);

        XSSFCellStyle textOne = ExcelStyle.getStyleText(wb, 3);
        XSSFCellStyle textTwo = ExcelStyle.getStyleText(wb, 4);

        // 规范: 设置为1-3合并 ?-6合并 ?-9合并的单元格名称.
        XSSFCell c3;
        XSSFCell c6;
        XSSFCell c9;

        Date now = new Date();

        // 1 line
        XSSFRow row = sheet.createRow(rowCount++);
        _mergedRegionNowRow(sheet, row, 1, 9);
        c9 = row.createCell(0);
        c9.setCellStyle(titleTwo);
        c9.setCellValue(DateFormatUtils.format(now, "yyyy-MM-dd") + "广州嘻果出货单" + now.getTime());

        // 2 line
        row = sheet.createRow(rowCount++);
        _mergedRegionNowRow(sheet, row, 1, 3);
        _mergedRegionNowRow(sheet, row, 4, 6);
        _mergedRegionNowRow(sheet, row, 7, 9);
        c3 = row.createCell(0);
        c6 = row.createCell(3);
        c9 = row.createCell(6);
        c3.setCellStyle(textOne);
        c6.setCellStyle(textOne);
        c9.setCellStyle(textOne);
        c3.setCellValue("商家名称:");
        c6.setCellValue("联系人:");
        c9.setCellValue("送货电话:");

        // 3 line
        row = sheet.createRow(rowCount++);
        _mergedRegionNowRow(sheet, row, 1, 9);
        c9 = row.createCell(0);
        c9.setCellStyle(textOne);
        c9.setCellValue("商家地址: 梧州发指定车到梧州");

        // 4 line
        row = sheet.createRow(rowCount++);
        _mergedRegionNowRow(sheet, row, 1, 3);
        _mergedRegionNowRow(sheet, row, 4, 6);
        _mergedRegionNowRow(sheet, row, 7, 9);
        c3 = row.createCell(0);
        c6 = row.createCell(3);
        c9 = row.createCell(6);
        c3.setCellStyle(textOne);
        c6.setCellStyle(textOne);
        c9.setCellStyle(textOne);
        c3.setCellValue("发车类型: 市场车");
        c6.setCellValue("负责销售: 老李");
        c9.setCellValue("联系电话: 18819960688");

        // 5 line
        row = sheet.createRow(rowCount++);
        _mergedRegionNowRow(sheet, row, 1, 9);
        c9 = row.createCell(0);
        c9.setCellStyle(textOne);
        c9.setCellValue("配货点：运城");


        // data





        // bottom 1 line
        row = sheet.createRow(rowCount++);
        _mergedRegionNowRow(sheet, row, 1, 9);
        c9 = row.createCell(0);
        c9.setCellStyle(textOne);
        c9.setCellValue("温馨提示：运费和装车费、三轮车费、包装费、短途费/中转费，均按实际产生费用收取");

        // bottom 2 line


        try {
            FileOutputStream fout = new FileOutputStream("G:/students.xls");
            wb.write(fout);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void _mergedRegionNowRow(XSSFSheet sheet, XSSFRow row, int firstCol, int lastCol) {
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), firstCol - 1, lastCol - 1));
    }


}
