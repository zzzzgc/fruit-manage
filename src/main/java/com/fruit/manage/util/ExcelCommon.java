package com.fruit.manage.util;

import com.fruit.manage.util.excel.Excel;
import com.fruit.manage.util.excel.ExcelException;
import com.fruit.manage.util.excel.ExcelRow;
import com.fruit.manage.util.excelRd.ExcelRd;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdRow;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;
import com.jfinal.kit.StrKit;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Excel导入、导出、生成模板 公共类
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
        String savePath = path + "/" + fileName;
        String title = (String) map.get("title");
        String createBy = (String) map.get("createBy");
        String[] header = (String[]) map.get("header");
        List<String[]> listData = (List<String[]>) map.get("listData");
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
    public static String createExcelModul(String path, String fileName, String title, String createBy, String[] header, List<String[]> listData) throws ExcelException {
        //判断文件夹是否存在
        if (!new File(path).exists() || !new File(path).isDirectory()) {
            new File(path).mkdir();
        }
        String savePath = path + "/" + fileName;

        Excel excel = new Excel();
        excel.setWidth(1024);
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
        if (listData != null && listData.size() > 0) {
            // 循环行数
            for (int i = 0; i < listData.size(); i++) {
                if (listData.get(i) != null && listData.get(i).length > 0) {
                    // 创建新的一行
                    ExcelRow row = excel.createRow();
                    // 循环列数
                    for (int j = 0; j < listData.get(i).length; j++) {
                        // 添加数据
                        row.addCell(listData.get(i)[j]);
                    }
                }
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
     * @param readPath
     * @param startRowNum 指定起始行，从1开始
     * @param startColNum 指定起始列，从1开始
     * @throws IOException
     * @throws ExcelRdException
     */
    public static List<Object[]> excelRd(String readPath, Integer startRowNum, Integer startColNum) throws IOException, ExcelRdException {
        List<Object[]> data = new ArrayList<>();

        ExcelRd excelRd = new ExcelRd(readPath);
        excelRd.setStartRow(startRowNum-1);
        excelRd.setStartCol(startColNum-1);
        ExcelRdTypeEnum[] types = {
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.DATETIME,
                ExcelRdTypeEnum.DATE,
                ExcelRdTypeEnum.STRING
        };
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

    public static void main(String[] args) {
        try {
            List<Object[]> list = excelRd("C:\\Users\\Administrator\\Downloads\\商品库信息大全 (2).xlsx", 3, 1);
            for (Object[] objects : list) {
                for (Object object : objects) {
                    System.out.print(object + "-");
                }
                System.out.println("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExcelRdException e) {
            e.printStackTrace();
        }
    }
/*    public static void main(String[] args) {
        Map map =new HashMap();
        map.put("savePath","C:\\Users\\Administrator\\Desktop\\test.xlsx");
        map.put("title","标题");
        String[] header = {"序号","日期","时间","数字"};
        map.put("header",header);
        map.put("createBy","partner");
        List<String[]> listData=new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String [] row ={"序号"+i,"日期"+i,"时间"+i,"数字"+i};
            listData.add(row);
        }
        map.put("listData",listData);
        try {
            createExcelModul(map);
        } catch (ExcelException e) {
            e.printStackTrace();
        }
    }*/
}
