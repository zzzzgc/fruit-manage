package com.fruit.manage.base;

import com.fruit.manage.model._MappingKit;
import com.fruit.manage.plugin.shiro.ShiroInterceptor;
import com.fruit.manage.plugin.shiro.ShiroPlugin;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.activerecord.tx.TxByActionKeyRegex;
import com.jfinal.plugin.cron4j.Cron4jPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.plugin.hikaricp.HikariCpPlugin;
import com.jfinal.template.Engine;

/**
 * API引导式配置
 */
public class JFConfig extends JFinalConfig {

    Routes routes = null;

    /**
     * 配置常量
     */
    @Override
    public void configConstant(Constants me) {
        PropKit.use("setting.properties");
        me.setDevMode(PropKit.getBoolean("devMode", false));
        //没有身份认证
        me.setErrorView(401, "/login");
        // 上传下载路径使用默认值  upload/file
//        me.setBaseUploadPath(PropKit.get("file.baseUploadPath"));
//        me.setBaseDownloadPath(PropKit.get("file.baseDownloadPath"));

//		me.setFreeMarkerTemplateUpdateDelay(0);
        //没有权限
//		me.setErrorView(403, "/login");
//		me.setError404View("/404.html");
//		me.setError500View("/500.html");
    }

    /**
     * 配置路由
     */
    @Override
    public void configRoute(Routes me) {
        this.routes = new BaseRoutesConfig();
        me.add(routes);
//		AutoBindRoutes routeBind = new AutoBindRoutes();
//		routeBind.autoScan(false);
//		me.add(routeBind);
    }

    @Override
    public void configEngine(Engine me) {
        //me.addSharedFunction("/common/_layout.html");
        //me.addSharedFunction("/common/_paginate.html");
    }

    /**
     * 配置插件
     */
    @Override
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
		me.add(new Cron4jPlugin("job.properties"));// 初始化定时任务插件
        ShiroPlugin shiroPlugin = new ShiroPlugin(routes);//权限控制插件
        shiroPlugin.setLoginUrl("/login");
        shiroPlugin.setSuccessUrl("/");
        shiroPlugin.setUnauthorizedUrl("");//没有权限提示页
        me.add(shiroPlugin);
    }

    /**
     * 配置全局拦截器
     */
    @Override
    public void configInterceptor(Interceptors me) {
        //me.add(new LoginInterceptor());
//		me.add(new AllowCrossDomain());
//		me.add(new UrlShiroInterceptor());
        me.add(new ShiroInterceptor());
//		me.add(new Tx());
    }

    /**
     * 配置处理器
     */
    @Override
    public void configHandler(Handlers me) {
//		me.add(new PageHandler());
    }
}
