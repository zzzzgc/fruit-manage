package com.fruit.manage.controller.procurement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.ProcurementPlan;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.DateAndStringFormat;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.SerialNumUtil;
import com.fruit.manage.util.excel.ExcelException;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.TxConfig;
import com.microsoft.schemas.office.office.STInsetMode;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

public class PlanController extends BaseController {
    public static Logger logger = Logger.getLogger(PlanController.class);

    public void getPlan() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        Map map = new HashMap();
        String orderBy = getPara("prop");
        map.put("createTime", getParaValues("format_create_time"));
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));
        Page<ProcurementPlan> pPlanPage = ProcurementPlan.dao.getAllProcurementPlan(pageNum, pageSize, orderBy, isASC, map);
        if(pPlanPage.getList()!=null && pPlanPage.getList().size()>0){
            for (int i = 0; i < pPlanPage.getList().size(); i++) {
                ProcurementPlan procurementPlan= pPlanPage.getList().get(i);
                String createTime = DateAndStringFormat.getStringDateShort(procurementPlan.getCreateTime());
                String [] createTimes =new String[2];
                createTimes[0]=DateAndStringFormat.getNextDay(createTime,"-1")+" 12:00:00";
                createTimes[1]=createTime+" 11:59:59";
                ProcurementPlan procurementPlan2=ProcurementPlan.dao.getWaitStatisticsOrderTotal(createTimes,createTime);
                procurementPlan.setWaitStatisticsOrderTotal(procurementPlan2.getWaitStatisticsOrderTotal());
            }
        }
        renderJson(pPlanPage);
    }

    /**
     *删除采购计划
     */
    public void delPPlan() {
        int pPlanId = getParaToInt("pPlanId");
        renderResult(ProcurementPlan.dao.deleteById(pPlanId));
    }

    /**
     * 添加采购计划
     */
    public void addPlan() {
        String[] create_time = new String [2];
        String nowDateStr = DateAndStringFormat.getStringDateShort(new Date());
        if (Integer.parseInt(DateAndStringFormat.getHour())>=12){
            //当前时间大于12小时的情况（包括12小时）
            create_time[0]=DateAndStringFormat.getStringDateShort(new Date())+" 12:00:00";
            create_time[1]=DateAndStringFormat.getNextDay(DateAndStringFormat.getStringDateShort(new Date()),"1")+" 11:59:59";
            nowDateStr=DateAndStringFormat.getNextDay(DateAndStringFormat.getStringDateShort(new Date()),"1");
        }else{
            //当前时间小于12小时的情况（不包括12小时）
            create_time[0]=DateAndStringFormat.getNextDay(DateAndStringFormat.getStringDateShort(new Date()),"-1")+" 12:00:00";
            create_time[1]=DateAndStringFormat.getStringDateShort(new Date())+" 11:59:59";
        }
        ProcurementPlan procurementPlan = ProcurementPlan.dao.getPPlan(create_time);
        List<Integer> list=new ArrayList<>();
        if(procurementPlan.getProductStandardNum() != null && procurementPlan.getProductStandardNum() != 0){
            try {
                ProcurementPlan procurementPlan2=ProcurementPlan.dao.getPPlanCreateTime(nowDateStr);
                if(procurementPlan2!=null && procurementPlan2.getId()!=null){
                    procurementPlan2.setNum(procurementPlan.getNum());
                    procurementPlan2.setOrderTotal(procurementPlan.getOrderTotal());
                    procurementPlan2.setProductStandardNum(procurementPlan.getProductStandardNum());
                    procurementPlan2.setWaitStatisticsOrderTotal(procurementPlan.getWaitStatisticsOrderTotal());
                    procurementPlan2.setProcurementId(getSessionAttr(Constant.SESSION_UID));
                    procurementPlan2.update();
                    // 此段日期已经有人有人下单，并重新覆盖
                    list.add(0);
                }else{
                    procurementPlan.setId(SerialNumUtil.getProrementPlanId());
                    procurementPlan.setProcurementId(getSessionAttr(Constant.SESSION_UID));
                    procurementPlan.setCreateTime(DateAndStringFormat.strToDate(nowDateStr));
                    procurementPlan.save();
                    // 此段日期已经有人有人下单，新添加
                    list.add(1);
                }
                // 订单日志修改为1（被统计过）
                ProcurementPlan.dao.updateOrderLog(create_time);
            } catch (Exception e) {
                // 异常
                list.add(-1);
            }
        }else {
            // 此段日期无人下单
            list.add(2);
        }
        renderJson(list);
    }

    /**
     * 更新采购计划
     */
    public void updatePPlan(){
        Date createTime=getParaToDate("createTime");
        String createTimeStr= DateAndStringFormat.getStringDateShort(createTime);
        String [] create_time =new String [2];
        create_time[0]=DateAndStringFormat.getNextDay(createTimeStr,"-1")+" 12:00:00";
        create_time[1]=createTimeStr+" 11:59:59";
        ProcurementPlan procurementPlan=ProcurementPlan.dao.getPPlan(create_time);
        ProcurementPlan procurementPlan2 = ProcurementPlan.dao.getPPlanCreateTime(createTimeStr);
        if(procurementPlan!=null && procurementPlan2!=null){
            procurementPlan2.setNum(procurementPlan.getNum());
            procurementPlan2.setOrderTotal(procurementPlan.getOrderTotal());
            procurementPlan2.setProductStandardNum(procurementPlan.getProductStandardNum());
            procurementPlan2.setWaitStatisticsOrderTotal(procurementPlan.getWaitStatisticsOrderTotal());
            procurementPlan2.setProcurementId(getSessionAttr(Constant.SESSION_UID));
            procurementPlan2.update();
        }
        renderJson(new ArrayList<>().add(0));
    }

    /**
     * 根据采购计划ID导出采购计划单
     */
    public void exportPPlan() {
        int pPlanId = getParaToInt("pPlanId");
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        // 获取当前操作用户
        User user = User.dao.findById(uid);
        // 获取要导出数据
        List<ProcurementPlan> planList = ProcurementPlan.dao.getExportDataByPPlanID(pPlanId);
        //行头
        String[] header = {"商品名", "规格名", "规格编码", "重量(斤)", "报价", "下单量", "库存量", "采购量", "采购单价", "下单备注"};
        List<String[]> listData = new ArrayList<String[]>();
        for (ProcurementPlan procurementPlan : planList) {
            String[] str = new String[header.length];
            // 商品名
            str[0] = procurementPlan.get("pName");
            // 规格名
            str[1] = procurementPlan.get("psName");
            // 规格编号
            str[2] = procurementPlan.get("psID");
            // 水果重量
            str[3] = procurementPlan.get("fruitWeight");
            // 报价
            str[4] = procurementPlan.get("sellPrice");
            str[5] = procurementPlan.get("orderCount");
            str[6] = procurementPlan.get("inventoryCount");
            str[7] = procurementPlan.get("procurementCount");
            str[8] = procurementPlan.get("procurementPrice");
            str[9] = procurementPlan.get("orderRemark");
            listData.add(str);
        }
        //保存路径
        String savePath = getRequest().getSession().getServletContext().getRealPath("static/excel");
        System.out.println("\n" + savePath);
        String fpath = getSession().getServletContext().getRealPath("static/excel");
        System.out.println(fpath + "\n");
        Map map = new HashMap(12);
        map.put("path", savePath);
        map.put("fileName", UUID.randomUUID().toString().replaceAll("-", "") + ".xlsx");
        map.put("title", "采购计划表");
        map.put("createBy", user.getName());
        map.put("header", header);
        map.put("listData", listData);
        try {
            String path = ExcelCommon.createExcelModul(map);
            List<String> list = new ArrayList<>();
            list.add(path);
            renderJson(list);
        } catch (ExcelException e) {
            renderErrorText(e.getMessage());
        }
    }

    public void download() {
        String path = getPara("path");
        File file = new File(path);
        if (file.exists()) {
            renderFile(file);
        } else {
            renderJson();
        }
    }
}
