package genmodel;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;
import com.jfinal.plugin.activerecord.generator.MetaBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * 如果希望仅生成指定的表的bean，则使用使用该类构造Generator
 *
 * @author Administrator
 */
public class MyMetaBuilder extends MetaBuilder {
    protected Set<String> includedTables = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

    public MyMetaBuilder(DataSource dataSource, String... includedTableNames) {
        super(dataSource);
        for (String tableName : includedTableNames) {
            includedTables.add(tableName);
        }
    }

    @Override
    protected boolean isSkipTable(String tableName) {
        System.err.println(tableName);
        if (includedTables.contains(tableName)) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        try {
            List<Object[]> excel = ExcelCommon.excelRd(new File("C:\\Users\\Administrator\\Desktop\\test.xlsx"), 9, 1, new ExcelRdTypeEnum[]{
                    ExcelRdTypeEnum.STRING,
                    ExcelRdTypeEnum.STRING,
                    ExcelRdTypeEnum.STRING,
                    ExcelRdTypeEnum.STRING,
                    ExcelRdTypeEnum.STRING,

                    ExcelRdTypeEnum.STRING,
                    ExcelRdTypeEnum.INTEGER,
                    ExcelRdTypeEnum.INTEGER,
                    ExcelRdTypeEnum.INTEGER,
                    ExcelRdTypeEnum.INTEGER,

                    ExcelRdTypeEnum.STRING,
                    ExcelRdTypeEnum.STRING
            });

            for (Object[] rows : excel) {
                Integer productId = 0;
                Integer productStandardId = 0;

                String productIdInfo = (String) rows[0];
                if (StringUtils.isNotBlank(productIdInfo)) {
                    String[] idInfo = productIdInfo.split("-");
                    productId = Integer.parseInt(idInfo[0]);
                    productStandardId = Integer.parseInt(idInfo[1]);
                }

                for (Object cell : rows) {
                    System.out.print(cell + "   ");
                }
                System.out.println();


                String typeName = (String)rows[1];
                String productName = (String)rows[2];
                if (StringUtils.isNotBlank(productName)) {
                    for (Object cell : rows) {
                        System.out.print(cell + "   ");
                    }
                    System.out.println();
                    continue;
                }
                String productStandardName = (String)rows[3];
                String subhead = (String)rows[4];
                String ProcurementCode = (String)rows[5];
                Integer sendNum = Integer.parseInt(rows[6]+"");
                Integer OrderSendNum = Integer.parseInt(rows[7]+"");
                Integer sellPrice = Integer.parseInt(rows[8]+"");
                Integer price = Integer.parseInt(rows[9]+"");
                String profit = (String)rows[10];
                String title = (String)rows[11];


            }




        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
