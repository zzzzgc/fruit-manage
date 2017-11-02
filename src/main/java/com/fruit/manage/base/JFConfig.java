package com.fruit.manage.base;

import com.fruit.manage.controller.IndexController;
import com.fruit.manage.controller.ProductController;
import com.fruit.manage.controller.ProductStandardController;
import com.fruit.manage.interceptor.AllowCrossDomain;
import com.fruit.manage.model._MappingKit;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.route.AutoBindRoutes;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.plugin.hikaricp.HikariCpPlugin;
import com.jfinal.template.Engine;

/**
 * API引导式配置
 */
public class JFConfig extends JFinalConfig {
	/**
	 * 配置常量
	 */
	public void configConstant(Constants me) {
		PropKit.use("setting.properties");
		me.setDevMode(PropKit.getBoolean("devMode", false));
//		me.setFreeMarkerTemplateUpdateDelay(0);
//		me.setError404View("index.html");
	}
	
	/**
	 * 配置路由
	 */
	public void configRoute(Routes me) {
		me.add("/", IndexController.class);
		me.add("/product", ProductController.class);
		AutoBindRoutes routeBind = new AutoBindRoutes();
		routeBind.autoScan(false);
		me.add(routeBind);
		me.add("/productStandard", ProductStandardController.class);
//		AutoBindRoutes routeBind = new AutoBindRoutes();
//		routeBind.autoScan(false);
//		me.add(routeBind);
	}
	
	public void configEngine(Engine me) {
		//me.addSharedFunction("/common/_layout.html");
		//me.addSharedFunction("/common/_paginate.html");
	}
	
	/**
	 * 配置插件
	 */
	public void configPlugin(Plugins me) {
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
//		me.add(dataSource);
		
		// 配置数据库连接池
		HikariCpPlugin dataSource = new HikariCpPlugin(PropKit.get("db.url"), PropKit.get("db.user"), PropKit.get("db.password"), PropKit.get("db.driver"));
		dataSource.setMaximumPoolSize(PropKit.getInt("db.poolMaxSize"));
		dataSource.setConnectionTimeout(PropKit.getInt("db.connectionTimeoutMillis"));
		me.add(dataSource);
		ActiveRecordPlugin arp = new ActiveRecordPlugin(dataSource);
		// 所有映射在 MappingKit 中自动化搞定
		_MappingKit.mapping(arp);

		me.add(arp);
		
		me.add(new EhCachePlugin());// 初始化应用缓存插件
//		me.add(new Cron4jPlugin("job.properties"));// 初始化定时任务插件
	}
	
	/**
	 * 配置全局拦截器
	 */
	public void configInterceptor(Interceptors me) {
		//me.add(new LoginInterceptor());
		me.add(new AllowCrossDomain());
	}
	
	/**
	 * 配置处理器
	 */
	public void configHandler(Handlers me) {
//		me.add(new PageHandler());
	}
}
