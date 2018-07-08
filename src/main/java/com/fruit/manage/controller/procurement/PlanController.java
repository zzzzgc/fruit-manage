package com.fruit.manage.controller.procurement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.OrderLog;
import com.fruit.manage.model.ProcurementPlan;
import com.fruit.manage.model.ProcurementPlanDetail;
import com.fruit.manage.util.*;
import com.jfinal.aop.Before;
import com.jfinal.ext.kit.DateKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PlanController extends BaseController {
    public static Logger logger = Logger.getLogger(PlanController.class);

    public void getPlan() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        String orderBy = getPara("prop");
        String[] orderCycleDates = getParaValues("order_cycle_date");
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));
        Page<ProcurementPlan> pPlanPage = ProcurementPlan.dao.getAllProcurementPlan(pageNum, pageSize, orderBy, isASC, orderCycleDates);
        List<ProcurementPlan> list = pPlanPage.getList();
        if (list != null && list.size() > 0) {
            // 重算未统计信息的数量
            for (int i = 0; i < list.size(); i++) {
                ProcurementPlan procurementPlan = list.get(i);
                Date orderCycleDate = procurementPlan.getOrderCycleDate();
                String orderCycleDateStr = DateKit.toStr(orderCycleDate, "yyyy-MM-dd");
                Record orderLog = ProcurementPlan.dao.getWaitStatisticsOrderTotal(orderCycleDateStr);
                procurementPlan.setWaitStatisticsOrderTotal(Integer.parseInt(orderLog.get("wait_statistics_order_total") + ""));
            }
        }
        renderJson(pPlanPage);
    }

    /**
     * 删除采购计划
     */
    @Before(Tx.class)
    public void delPPlan() {
        String pPlanId = getPara("pPlanId");
        ProcurementPlan procurementPlan = ProcurementPlan.dao.getPPlanById(pPlanId);
        if (procurementPlan != null && procurementPlan.getCreateTime() != null) {
            String[] createTimes = ZhioDateUtils.getOrderCycleDateStrings(procurementPlan.getCreateTime());
            ProcurementPlanDetail.dao.delAllPlanDetailByPPlanId(procurementPlan.getId());
            // 根据采购生成时间修改订单日志统计状态为未统计（0）
            OrderLog.dao.updateOrderLog(createTimes);
            renderResult(ProcurementPlan.dao.deleteById(pPlanId));
        } else {
            renderErrorText("删除失败！");
        }
    }

    /**
     * 添加采购计划
     */
    public void addPlan() {

        Date orderCycleDate = getParaToDate("order_cycle_date");
        String orderCycleDateStr = getPara("order_cycle_date");
        if (orderCycleDate == null) {
            orderCycleDate = ZhioDateUtils.getOrderCycleDate(new Date());
            orderCycleDateStr = DateKit.toStr(orderCycleDate,"yyyy-MM-dd");
        }
        String[] orderCycleDates = ZhioDateUtils.getOrderCycleDateStrings(orderCycleDate);


        Record orderLog = ProcurementPlan.dao.getOrderLogByPPlan(orderCycleDates);
        List<Integer> list = new ArrayList<>();
        if (orderLog.get("product_standard_num") != null && Integer.parseInt(orderLog.get("product_standard_num") + "") != 0) {
            try {
                ProcurementPlan procurementPlan = ProcurementPlan.dao.getProcurementPlanByOrderCycleDate(orderCycleDateStr);
                if (procurementPlan != null && procurementPlan.getId() != null) {
                    procurementPlan.setNum(orderLog.get("num"));
                    procurementPlan.setOrderTotal(orderLog.get("order_total"));
                    procurementPlan.setProductStandardNum(orderLog.get("product_standard_num"));
                    procurementPlan.setWaitStatisticsOrderTotal(orderLog.get("wait_statistics_order_total"));
                    procurementPlan.setProcurementId(getSessionAttr(Constant.SESSION_UID));
                    procurementPlan.update();
                    // 此段日期已经有人有人下单，并重新覆盖
                    list.add(0);
                } else {
                    procurementPlan = new ProcurementPlan();
                    procurementPlan.setId(ZhioIdUtil.getProrementPlanId(orderCycleDate));
                    procurementPlan.setProcurementId(getSessionAttr(Constant.SESSION_UID));
                    procurementPlan.setOrderCycleDate(orderCycleDate);
                    procurementPlan.setNum(Integer.parseInt(orderLog.get("num") + ""));
                    procurementPlan.setOrderTotal(Integer.parseInt(orderLog.get("order_total") + ""));
                    procurementPlan.setProductStandardNum(Integer.parseInt(orderLog.get("product_standard_num") + ""));
                    procurementPlan.setWaitStatisticsOrderTotal(Integer.parseInt(orderLog.get("wait_statistics_order_total") + ""));
                    procurementPlan.setCreateTime(new Date());
                    procurementPlan.save();
                    // 此段日期已经有人有人下单，新添加
                    list.add(1);
                }
                // 订单日志修改为1（被统计过）
                ProcurementPlan.dao.updateOrderLog(orderCycleDateStr);
            } catch (Exception e) {
                // 异常
                list.add(-1);
                e.printStackTrace();
            }
        } else {
            // 此段日期无人下单
            list.add(2);
        }
        renderJson(list);
    }

    /**
     * 更新采购计划
     */
    public void updatePPlan() {

        try {
            Date orderCycleDate = getParaToDate("order_cycle_date");
            String orderCycleDateStr = getPara("order_cycle_date");
            String[] orderCycleDates = ZhioDateUtils.getOrderCycleDateStrings(orderCycleDate);

            Record procurementPlan = ProcurementPlan.dao.getOrderLogByPPlan(orderCycleDates);
            ProcurementPlan procurementPlan2 = ProcurementPlan.dao.getProcurementPlanByOrderCycleDate(orderCycleDateStr);
            if (procurementPlan != null && procurementPlan2 != null) {
                procurementPlan2.setNum(procurementPlan.get("num") == null ? 0 : procurementPlan.get("num"));
                procurementPlan2.setOrderTotal(procurementPlan.get("order_total"));
                procurementPlan2.setProductStandardNum(procurementPlan.get("product_standard_num"));
                procurementPlan2.setWaitStatisticsOrderTotal(procurementPlan.get("wait_statistics_order_total") == null ? 0 : procurementPlan.get("wait_statistics_order_total"));
                procurementPlan2.setProcurementId(getSessionAttr(Constant.SESSION_UID));
                procurementPlan2.update();
            }
            // 订单日志修改为1（被统计过）
            // ProcurementPlan.dao.updateOrderLog(create_time);
            renderNull();
        } catch (Exception e) {
            renderErrorText("更新采购计划失败！");
        }
    }

}
