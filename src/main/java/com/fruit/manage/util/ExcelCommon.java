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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel导入、导出、生成模板 公共类
 */
public class ExcelCommon {
    /**
     *
     * @param map path:保存的路径,fileName：文件名，title：标题,createBy：创建者,header[] 行头,listData 要导出的数据（可选填）
     *            必须严格按照指定的列名传入，作用于导出模板和导出数据
     */
    public static String createExcelModul(Map map) throws ExcelException{
        Excel excel =new Excel();
        String path =(String) map.get("path");
        if(!new File(path).exists() || !new File(path).isDirectory()){ //判断文件夹是否存在
            new File(path).mkdir();
        }
        String fileName =(String) map.get("fileName");
        String savePath = path + "/"+fileName;
        String title =(String)map.get("title");
        String createBy = (String)map.get("createBy");
        String [] header=(String[])map.get("header");
        List<String[]> listData=(List<String[]>)map.get("listData");
        excel.setWidth(1024);
        if(StrKit.notBlank(title)){
            excel.setTitle(title);
        }
        if(!StrKit.notBlank(savePath)){
            throw new ExcelException("保存路径不能为空");
        }else {
            excel.setSavePath(savePath);
        }
        if(StrKit.notBlank(createBy)){
            excel.setCreateBy(createBy);
        }
        if(StrKit.notBlank(header)){
            excel.setHeader(header);
        }
        if(listData!=null && listData.size()>0){
            for (int i = 0; i < listData.size(); i++) { // 循环行数
                if(listData.get(i)!=null && listData.get(i).length>0){
                    ExcelRow row=excel.createRow(); // 创建新的一行
                    for (int j = 0; j < listData.get(i).length; j++) { // 循环列数
                        row.addCell(listData.get(i)[j]); // 添加数据
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

    private static void excelRd(String readPath) throws IOException, ExcelRdException {
        ExcelRd excelRd = new ExcelRd(readPath);
        excelRd.setStartRow(1);	// 指定起始行，从1开始
        excelRd.setStartCol(0);	// 指定起始列，从0开始
        ExcelRdTypeEnum[] types = {
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.DATETIME,
                ExcelRdTypeEnum.DATE,
                ExcelRdTypeEnum.STRING
        };
        excelRd.setTypes(types);	// 指定每列的类型

        List<ExcelRdRow> rows = excelRd.analysisXlsx();
        Map<String, Object>[] plans = new HashMap[rows.size()];

        int size = rows.size();
        for (int i = 0; i < size; i++) {

            ExcelRdRow excelRdRow = rows.get(i);
            List<Object> row = excelRdRow.getRow();
            HashMap<String, Object> plan = new HashMap<String, Object>();

            for (Object t : row) {
                System.out.print(t +"\t*");
            }
            System.out.println("\n");

            plans[i] = plan;
        }
    }

    public static void main(String[] args) {
        try {
            excelRd("C:\\Users\\Administrator\\Desktop\\test.xlsx");
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
