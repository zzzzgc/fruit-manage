package com.fruit.manage.util;

import com.fruit.manage.util.excelRd.ExcelRd;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdRow;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelRdTest {

	public static void main(String[] args) throws IOException, ExcelRdException {
		excelRd();
	}

	private static void excelRd() throws IOException, ExcelRdException {

		String path = "C:\\Users\\Administrator\\Desktop\\test.xlsx";
		ExcelRd excelRd = new ExcelRd(path);
		excelRd.setStartRow(1);	// 指定起始行，从0开始
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
                System.out.print(t);
            }
            System.out.println("\n");
			
			plans[i] = plan;
		}
	}

}
