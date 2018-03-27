package com.fruit.manage.quartz;


import org.apache.log4j.Logger;

/**
 * 每天3点执行一次的定时调度
 *
 * @author ZGC AND CCZ
 * @date 2018-03-27 12:12
 **/
public class LeisureJob implements Runnable{
    private static Logger log = Logger.getLogger(LeisureJob.class);

    @Override
    public void run() {
        start();
        test ();
        end();
    }

    /**
     * 开始输出时间和声明开始调度
     */
    public static void start() {
//        log.info("-------------开始-> 每天3点的定时调度------------------");

    }

    public static void test () {
//        System.out.println("现在时间:" + DateFormatUtils.format(new Date(),"yyyy-MM-dd hh:mm:ss"));
    }

    /**
     *结束输出时间和声明开始调度
     */
    public static void end() {
//        log.info("-------------结束-> 每天3点的定时调度------------------");

    }
}
