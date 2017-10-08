package genmodel;

import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import com.jfinal.plugin.activerecord.generator.MetaBuilder;

/**
 * 如果希望仅生成指定的表的bean，则使用使用该类构造Generator
 * @author Administrator
 *
 */
public class MyMetaBuilder extends MetaBuilder {
	protected Set<String> includedTables = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

	public MyMetaBuilder(DataSource dataSource, String... includedTableNames) {
		super(dataSource);
		for(String tableName : includedTableNames){
			includedTables.add(tableName);
		}
	}
	
	@Override
	protected boolean isSkipTable(String tableName) {
		System.err.println(tableName);
		if(includedTables.contains(tableName)){
			return false;
		}
		return true;
	}

}
