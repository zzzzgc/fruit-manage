package genmodel;


import javax.sql.DataSource;

import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.hikaricp.HikariCpPlugin;

/**
 * GeneratorDemo
 */
public class GeneratorDemo {
	
	public static DataSource getDataSource() {
		PropKit.use("setting.properties");
		// 配置数据库连接池插件
//		DruidPlugin dataSource = new DruidPlugin(PropKit.get("db.url"), PropKit.get("db.user"), 
//				PropKit.get("db.password"), PropKit.get("db.driver"));
//		// StatFilter提供JDBC层的统计信息
//		dataSource.addFilter(new StatFilter());
//		// WallFilter的功能是防御SQL注入攻击
//		WallFilter wallDefault = new WallFilter();
//		wallDefault.setDbType(JdbcConstants.MYSQL);
//		dataSource.addFilter(wallDefault);		
//		dataSource.setInitialSize(PropKit.getInt("db.poolInitialSize"));
//		dataSource.setMaxPoolPreparedStatementPerConnectionSize(PropKit.getInt("db.poolMaxSize"));
//		dataSource.setTimeBetweenConnectErrorMillis(PropKit.getInt("db.connectionTimeoutMillis"));
		HikariCpPlugin dataSource = new HikariCpPlugin(PropKit.get("db.url"), PropKit.get("db.user"), PropKit.get("db.password"), PropKit.get("db.driver"));
		dataSource.setMaximumPoolSize(PropKit.getInt("db.poolMaxSize"));
		dataSource.setConnectionTimeout(PropKit.getInt("db.connectionTimeoutMillis"));
		dataSource.start();
		return dataSource.getDataSource();
	}
	
	public static void main(String[] args) {
		// base model 所使用的包名
		// model 所使用的包名 (MappingKit 默认使用的包名)
		String modelPackageName = "com.fruit.manage.model";
		String baseModelPackageName = modelPackageName+".base";
		// base model 文件保存路径
		String baseModelOutputDir = PathKit.getWebRootPath() + "/src/main/java/"+(baseModelPackageName.replaceAll("\\.", "/"));
		
		// model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
		String modelOutputDir = baseModelOutputDir + "/..";
		
		// 创建生成器
		DataSource dataSource = getDataSource();
		Generator gernerator = new Generator(dataSource, baseModelPackageName, baseModelOutputDir, modelPackageName, modelOutputDir);		
//		MetaBuilder metaBuilder = new MyMetaBuilder(dataSource,"game_account", "game_8868", "game_money");
//		gernerator.setMetaBuilder(metaBuilder);
		// 设置数据库方言
		gernerator.setDialect(new MysqlDialect());
		// 添加不需要生成的表名
		String tables = "old-b_classify";
		gernerator.addExcludedTable(tables.split(","));
		
		// 设置是否在 Model 中生成 dao 对象
		gernerator.setGenerateDaoInModel(true);
		// 设置是否生成字典文件
		gernerator.setGenerateDataDictionary(false);
		// 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为 "User"而非 OscUser
		gernerator.setRemovedTableNamePrefixes("b_", "a_");
		// 生成
		gernerator.generate();
	}
}




