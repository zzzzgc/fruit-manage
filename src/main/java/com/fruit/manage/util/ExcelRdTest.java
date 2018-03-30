package com.fruit.manage.util;

import com.fruit.manage.util.excelRd.ExcelRd;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdRow;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelRdTest {
	private class Article{
		private final String title;
		private final String author;
		private final List<String> tags;

		private Article(String title, String author, List<String> tags) {
			this.title = title;
			this.author = author;
			this.tags = tags;
		}

		public String getTitle() {
			return title;
		}

		public String getAuthor() {
			return author;
		}

		public List<String> getTags() {
			return tags;
		}

		@Override
		public String toString() {
			return super.toString();
		}
	}
	public Article getFirstJavaArticle() {
		List<String> list=new ArrayList<>(5);
		list.add("Java");
		list.add("C#");
		list.add("Python");
		Article article=new Article("title","author",list);
		List<Article> articles=new ArrayList<>();
		articles.add(article);
		for (Article article2 : articles) {
			if (article2.getTags().contains("Java")) {
				return article2;
			}
		}

		return null;
	}


	public static void main(String[] args) throws IOException, ExcelRdException {
		test01();
		//		excelRd();
	}

	public static void test01(){
		testBreak:
		for (int i = 0; i < 10; i++) {
			System.out.println("我是i:"+i);
			for (int j = 0; j < 10; j++) {
				System.out.println("我是j:"+j);
				if(j==7){
					break testBreak;
				}
			}
		}
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
