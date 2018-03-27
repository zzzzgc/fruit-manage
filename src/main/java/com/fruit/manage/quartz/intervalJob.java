package com.fruit.manage.quartz;


import com.fruit.manage.model.Order;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.log4j.Logger;

/**
 * 每三分钟执行一次的定时调度
 *
 * @author ZGC AND CCZ
 * @date 2018-03-27 12:12
 **/
public class intervalJob implements Runnable{
    private static Logger log = Logger.getLogger(intervalJob.class);

    @Override
    public void run() {
        start();
        changeDeliveryOrder();
        end();
    }

    /**
     * 开始输出时间和声明开始调度
     */
    public static void start() {
//        log.info("-------------开始->每三分钟一次的定时调度------------------");

    }

    /**
     * 自动改delivery(配送)状态
     *  三天后  已配送 -> 已送达
     */
    @Before(Tx.class)
    public static void changeDeliveryOrder() {
        Db.update("UPDATE b_order AS o\n" +
                "INNER JOIN b_logistics_info AS li ON o.order_id = li.order_id\n" +
                "SET \n" +
                "\to.order_status = 20,\n" +
                "\tli.take_goods_time = NOW()\n" +
                "\n" +
                "WHERE\n" +
                "\to.order_status = 15\n" +
                "AND DATEDIFF(NOW(), li.send_goods_time) > 3");
    }

    /**
     *结束输出时间和声明开始调度
     */
    public static void end() {
//        log.info("-------------结束->每三分钟一次的定时调度------------------");

    }
}
