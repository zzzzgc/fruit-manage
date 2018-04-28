package genmodel;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

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
//        Optional accResult = Stream.of("a", "b", null, "d")
//                .reduce((acc, item) -> {
//                    System.out.println("acc : "  + acc);
//                    acc += item;
//                    System.out.println("item: " + item);
//                    System.out.println("acc+ : "  + acc);
//                    System.out.println("--------");
//                    return acc;
//                });
//        System.out.println("accResult: " + accResult.get());
//        System.out.println("--------");
//        String errorRowStr2 = Arrays.stream(new String[]{"a","b",null,"c"}).map(Object::toString).reduce("", "");
//        System.out.println(errorRowStr2);
        String errorRowStr = Arrays.stream(new Object[]{"a","b",null,"d"}).map(obj -> {
            if (obj!=null) {
                return obj.toString();
            }
            return null;
        }).reduce("", (acc, str) -> acc += str);
        System.out.println(errorRowStr);
    }

}
