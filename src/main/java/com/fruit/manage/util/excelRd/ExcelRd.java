package com.fruit.manage.util.excelRd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;

public class ExcelRd extends ExcelRdContent {

    private boolean xls = false;
    private List<XSSFSheet> sheetList =new ArrayList<>();
    private List<HSSFSheet> sheet03List =new ArrayList<>();

    public ExcelRd(String xlsxPath) {
        super();
        this.file = new File(xlsxPath);
    }

    public ExcelRd(File file) {
        super();
        this.file = file;
    }

    public List<ExcelRdRow> analysisXlsx() throws ExcelRdException, IOException {
        String xlsxPath = file.getPath();

        if (!(xlsxPath.endsWith(".xlsx") || xlsxPath.endsWith(".xls")))
            throw new ExcelRdException("Excel can only be xlsx or xls!");

        // 03版本的excel要特别标明
        if (xlsxPath.endsWith(".xls"))
            xls = true;

        if (!file.exists())
            throw new ExcelRdException("Excel path is not correct");
        if (!file.isFile())
            throw new ExcelRdException("Excel path is not a file");

        List<ExcelRdTypeEnum> types = getTypes();
        if (types == null || types.size() == 0)
            throw new ExcelRdException("Types of the data must be set");

        is = new FileInputStream(file);

        if (xls)
            workbook03 = new HSSFWorkbook(is);
        else
            workbook = new XSSFWorkbook(is);


        // 当前只考虑识别一个 sheet
        if (xls)
            sheet03 = workbook03.getSheetAt(0);
        else
            sheet = workbook.getSheetAt(0);

        // 循环所有【右边的边界】
        int right = getStartCol() + types.size();
        int rowThreshold = 0;    // 阈值【当连续取到三个空行，或者连续取到 3 * size 个空 cell 时，将会退出检测】
        int colThreshold = 0;    // 阈值【当连续取到三个空行，或者连续取到 3 * size 个空 cell 时，将会退出检测】

        for (int i = getStartRow(); ; i++) {

            // 阈值【当连续取到三个空行，或者连续取到 3 * size 个空 cell 时，将会退出检测】
            if (rowThreshold >= 3 || colThreshold >= 3 * types.size())
                break;

            if (xls)
                row03 = sheet03.getRow(i);
            else
                row = sheet.getRow(i);

            if (row03 == null && row == null) {
                rowThreshold++;
                continue;
            }
            rowThreshold = 0;

            ExcelRdRow excelRdRow = new ExcelRdRow();
            for (int j = getStartCol(); j < right; j++) {

                if (xls)
                    cell03 = row03.getCell(j);
                else
                    cell = row.getCell(j);

                if (cell03 == null && cell == null) {
                    colThreshold++;
                    excelRdRow.addCell("");
                } else {
                    colThreshold = 0;
                    Object cellValue;

                    if (xls)
                        cellValue = ExcelRdUtil.getCellValue(cell03, types.get(j - getStartCol()));
                    else
                        cellValue = ExcelRdUtil.getCellValue(cell, types.get(j - getStartCol()));

                    excelRdRow.addCell(cellValue);
                }
            }

            // 如果row全部为null，将不加入结果
            List<Object> rtRow = excelRdRow.getRow();
            int size = rtRow.size();
            for (Object object : rtRow) {
                if (object == null || "".equals(object.toString().trim()))
                    size--;
            }
            if (size != 0)
                addRow(excelRdRow);
        }
        return getRows();
    }

    public List<List<ExcelRdRow>> analysisXlsxMultiTable() throws ExcelRdException, IOException {
        String xlsxPath = file.getPath();

        if (!(xlsxPath.endsWith(".xlsx") || xlsxPath.endsWith(".xls"))) {
            throw new ExcelRdException("Excel can only be xlsx or xls!");
        }

        // 03版本的excel要特别标明
        if (xlsxPath.endsWith(".xls")) {
            xls = true;
        }

        if (!file.exists()) {
            throw new ExcelRdException("Excel path is not correct");
        }
        if (!file.isFile()) {
            throw new ExcelRdException("Excel path is not a file");
        }

        List<ExcelRdTypeEnum> types = getTypes();
        if (types == null || types.size() == 0) {
            throw new ExcelRdException("Types of the data must be set");
        }

        is = new FileInputStream(file);

        if (xls) {
            workbook03 = new HSSFWorkbook(is);
        } else {
            workbook = new XSSFWorkbook(is);
        }

        // 当前只考虑识别一个 sheet
        if (xls) {
            for (int i = 0; i < workbook03.getNumberOfSheets(); i++) {
                sheet03 = workbook03.getSheetAt(i);
                sheet03List.add(sheet03);
            }
        } else {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheet = workbook.getSheetAt(i);
                sheetList.add(sheet);
            }
        }

        // 循环所有【右边的边界】
        int right = getStartCol() + types.size();

        List<List<ExcelRdRow>> listExcelRdRows = new ArrayList<>();
        if (xls) {
            for (int k = 0; k < sheet03List.size(); k++) {
                HSSFSheet sheet03 = sheet03List.get(k);
                List<ExcelRdRow> excelRdRows = new ArrayList<>();
                int rowThreshold = 0;
                int colThreshold = 0;
                for (int i = getStartRow(); ; i++) {

                    // 阈值【当连续取到三个空行，或者连续取到 3 * size 个空 cell 时，将会退出检测】
                    if (rowThreshold >= 3 || colThreshold >= 3 * types.size()) {
                        break;
                    }

                    HSSFRow row03 = sheet03.getRow(i);

                    if (row03 == null) {
                        rowThreshold++;
                        continue;
                    }
                    rowThreshold = 0;

                    ExcelRdRow excelRdRow = new ExcelRdRow();
                    for (int j = getStartCol(); j < right; j++) {

                        HSSFCell cell03 = row03.getCell(j);

                        if (cell03 == null) {
                            colThreshold++;
                            excelRdRow.addCell("");
                        } else {
                            colThreshold = 0;
                            Object cellValue;
                            cellValue = ExcelRdUtil.getCellValue(cell03, types.get(j - getStartCol()));
                            excelRdRow.addCell(cellValue);
                        }
                    }
                    // 如果row全部为null，将不加入结果
                    List<Object> rtRow = excelRdRow.getRow();
                    int size = rtRow.size();
                    for (Object object : rtRow) {
                        if (object == null || "".equals(object.toString().trim())) {
                            size--;
                        }
                    }
                    if (size != 0) {
                        excelRdRows.add(excelRdRow);
                    }
                }
                listExcelRdRows.add(excelRdRows);
            }
        }else {
            for (int k = 0; k < sheetList.size(); k++) {
                XSSFSheet sheet = sheetList.get(k);
                List<ExcelRdRow> excelRdRows = new ArrayList<>();
                int rowThreshold = 0;
                int colThreshold = 0;
                for (int i = getStartRow(); ; i++) {
                    ExcelRdRow excelRdRow = new ExcelRdRow();
                    if (rowThreshold >= 3 || colThreshold >= 3 * types.size()) {
                        break;
                    }
                    XSSFRow row = sheet.getRow(i);

                    if (row == null) {
                        rowThreshold++;
                        continue;
                    }
                    rowThreshold = 0;

                    for (int j = getStartCol(); j < right; j++) {
                    XSSFCell cell = row.getCell(j);

                        if (cell == null) {
                            colThreshold++;
                            excelRdRow.addCell("");
                        } else {
                            colThreshold = 0;
                            Object cellValue;
                            cellValue = ExcelRdUtil.getCellValue(cell, types.get(j - getStartCol()));

                            excelRdRow.addCell(cellValue);
                        }
                    }
                    // 如果row全部为null，将不加入结果
                    List<Object> rtRow = excelRdRow.getRow();
                    int size = rtRow.size();
                    for (Object object : rtRow) {
                        if (object == null || "".equals(object.toString().trim())) {
                            size--;
                        }
                    }
                    if (size != 0) {
                        excelRdRows.add(excelRdRow);
                    }
                }

                listExcelRdRows.add(excelRdRows);
            }
        }
        return listExcelRdRows;
    }
}
