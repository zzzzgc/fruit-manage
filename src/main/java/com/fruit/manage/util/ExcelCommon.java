package com.fruit.manage.util;

import com.fruit.manage.model.Order;
import com.fruit.manage.util.excel.Excel;
import com.fruit.manage.util.excel.ExcelException;
import com.fruit.manage.util.excel.ExcelRow;
import com.fruit.manage.util.excel.ExcelStyle;
import com.fruit.manage.util.excelRd.ExcelRd;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdRow;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;
import com.jfinal.kit.StrKit;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Excel导入、导出、生成模板 公共类
 *
 * @author ZGC AND CCZ
 * @date Created in 21:10 2018/3/23
 */
public class ExcelCommon {
    /**
     * @param map path:保存的路径,fileName：文件名，title：标题,createBy：创建者,header[] 行头,listData 要导出的数据（可选填）
     *            必须严格按照指定的列名传入，作用于导出模板和导出数据
     */
    public static String createExcelModul(Map map) throws ExcelException {
        String path = (String) map.get("path");
        String fileName = (String) map.get("fileName");
        //判断文件夹是否存在
        if (!new File(path).exists() || !new File(path).isDirectory()) {
            new File(path).mkdir();
        }
        String title = (String) map.get("title");
        String createBy = (String) map.get("createBy");
        String[] header = (String[]) map.get("header");
        List<Object[]> listData = (List<Object[]>) map.get("listData");

        return createExcelModul(path, fileName, title, createBy, header, listData);
    }

    /**
     * @param path     保存的路径+文件名
     * @param fileName 保存的路径+文件名
     * @param title    标题
     * @param createBy 创建者
     * @param header   行头
     * @param listData 要导出的数据
     * @return 保存地址
     * @throws ExcelException 异常
     */
    public static String createExcelModul(String path, String fileName, String title, String createBy, String[] header, List<Object[]> listData) throws ExcelException {
        //判断文件夹是否存在
        if (!new File(path).exists() || !new File(path).isDirectory()) {
            new File(path).mkdirs();
        }
        String savePath = path + File.separator + fileName;

        Excel excel = new Excel();
        if (StrKit.notBlank(title)) {
            excel.setTitle(title);
        }
        if (!StrKit.notBlank(savePath)) {
            throw new ExcelException("保存路径不能为空");
        } else {
            excel.setSavePath(savePath);
        }
        if (StrKit.notBlank(createBy)) {
            excel.setCreateBy(createBy);
        }
        if (StrKit.notBlank(header)) {
            excel.setHeader(header);
        }
        for (Object[] dataRow : listData) {
            ExcelRow row = excel.createRow();
            for (Object dataCell : dataRow) {
                row.addCell(dataCell);
            }
        }
        try {
            return excel.CreateXlsx();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 简单的专门返回数据的excel信息
     *
     * @param pathFile    目录对象,支持url路径,支持绝对和抽象路径
     * @param startRowNum 指定起始行，从1开始
     * @param startColNum 指定起始列，从1开始
     * @throws IOException
     * @throws ExcelRdException
     */
    public static List<Object[]> excelRd(File pathFile, Integer startRowNum, Integer startColNum, ExcelRdTypeEnum[] types) throws IOException, ExcelRdException {
        List<Object[]> data = new ArrayList<>();

        ExcelRd excelRd = new ExcelRd(pathFile);
        excelRd.setStartRow(startRowNum - 1);
        excelRd.setStartCol(startColNum - 1);
        // 指定每列的类型
        excelRd.setTypes(types);

        List<ExcelRdRow> rows = excelRd.analysisXlsx();

        Iterator<ExcelRdRow> iterator = rows.iterator();
        while (iterator.hasNext()) {
            ExcelRdRow next = iterator.next();
            List<Object> row = next.getRow();
            data.add(row.toArray());
        }
        return data;
    }

    public static List<List<Object[]>> excelRdList(String pathFile,Integer startRowNum,Integer startColNum,ExcelRdTypeEnum [] typeEnums){
        ExcelRd excelRd = new ExcelRd(pathFile);
        excelRd.setStartRow(startRowNum);
        excelRd.setStartCol(startColNum);
        excelRd.setTypes(typeEnums);
        List<List<Object[]>> listObjects = new ArrayList<>();
        List<List<ExcelRdRow>> lists=null;
        try {
            lists=excelRd.analysisXlsxMultiTable();
            for (int i = 0; i < lists.size(); i++) {
                List<Object[]> objectArray = new ArrayList<>();
                Iterator<ExcelRdRow> iterator = lists.get(i).iterator();
                while (iterator.hasNext()) {
                    ExcelRdRow excelRdRow =iterator.next();
                    List<Object> objects = excelRdRow.getRow();
                    objectArray.add(objects.toArray());
                }
                listObjects.add(objectArray);
            }
        } catch (ExcelRdException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listObjects;
    }

    public static void main(String[] args) {
        int rowCount = 0;

        // 创建Excel
        XSSFWorkbook wb = new XSSFWorkbook();

        // 创建表
        XSSFSheet sheet = wb.createSheet("商家出货单");
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

        /**
         * 避免混乱,行和单元格都按照这个规范
         * rowName = r + rowIndex(start by 1)
         * cellName = c + row(start by 1) + endColumn(start by 1)
         * endColumn < 10
         */

        // 1 line
        XSSFRow row = sheet.createRow(rowCount++);
        mergedRegionNowRow(sheet, row, 1, 9);
        c9 = row.createCell(0);
        c9.setCellStyle(titleTwo);
        c9.setCellValue(DateFormatUtils.format(now, "yyyy-MM-dd") + "广州嘻果出货单" + now.getTime());

        // 2 line
        row = sheet.createRow(rowCount++);
        mergedRegionNowRow(sheet, row, 1, 3);
        mergedRegionNowRow(sheet, row, 4, 6);
        mergedRegionNowRow(sheet, row, 7, 9);
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
        mergedRegionNowRow(sheet, row, 1, 9);
        c9 = row.createCell(0);
        c9.setCellStyle(textOne);
        c9.setCellValue("商家地址: 梧州发指定车到梧州");

        // 4 line
        row = sheet.createRow(rowCount++);
        mergedRegionNowRow(sheet, row, 1, 3);
        mergedRegionNowRow(sheet, row, 4, 6);
        mergedRegionNowRow(sheet, row, 7, 9);
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
        mergedRegionNowRow(sheet, row, 1, 9);
        c9 = row.createCell(0);
        c9.setCellStyle(textOne);
        c9.setCellValue("配货点：运城");


        // 表格

        // bottom 1 line
        row = sheet.createRow(rowCount++);
        mergedRegionNowRow(sheet, row, 1, 9);
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


//        //1.创建Excel工作薄对象
//        HSSFWorkbook wb = new HSSFWorkbook();
//        //2.创建Excel工作表对象
//        HSSFSheet sheet = wb.createSheet("商家出货单");
//        //3.创建Excel工作表的行
//        HSSFRow row = sheet.createRow(6);
//        //4.创建单元格样式
////        CellStyle cellStyle =wb.createCellStyle();
//
//        // 设置这些样式
////        cellStyle.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
////        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
////        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
////        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
////        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
////        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
////        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
//
//
//
//        //5.创建Excel工作表指定行的单元格
////        row.createCell(0).setCellStyle(cellStyle);
//        //6.设置Excel工作表的值
////        row.createCell(0).setCellValue("aaaa");
//
////        row.createCell(1).setCellStyle(cellStyle);
////        row.createCell(1).setCellValue("bbbb");
//
//
//        //设置sheet名称和单元格内容
//        wb.setSheetName(0,"第一张工作表");
//        //设置单元格内容   cell.setCellValue("单元格内容");
//
//        // 最后一步，将文件存到指定位置
//        try
//        {
//            FileOutputStream fout = new FileOutputStream("G:/students.xls");
//            wb.write(fout);
//            fout.close();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }

    }

    private static void mergedRegionNowRow(XSSFSheet sheet, XSSFRow row, int firstCol, int lastCol) {
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), firstCol - 1, lastCol - 1));
    }


//    public static void main(String[] args) {
//        try {
//            List<Object[]> list = excelRd(new File("C:\\Users\\Administrator\\Downloads\\商品库信息大全.xlsx"), 3, 1, new ExcelRdTypeEnum[]{
//                    ExcelRdTypeEnum.STRING,
//                    ExcelRdTypeEnum.INTEGER,
//                    ExcelRdTypeEnum.STRING,
//                    ExcelRdTypeEnum.INTEGER,
//                    ExcelRdTypeEnum.STRING,
//                    ExcelRdTypeEnum.INTEGER
//            });
//            for (Object[] objects : list) {
//                for (Object object : objects) {
//                    System.out.print(object + "-");
//                }
//                System.out.println("");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
