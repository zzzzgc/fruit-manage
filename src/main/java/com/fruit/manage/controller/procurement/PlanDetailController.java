package com.fruit.manage.controller.procurement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.*;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.DateAndStringFormat;
import com.fruit.manage.util.excelRd.ExcelRd;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdRow;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author partner
 * @date 2018/3/23 15:07
 */
public class PlanDetailController extends BaseController{
    public static Logger logger = Logger.getLogger(PlanController.class);

    /**
     * 获取所有的采购计划
     */
    public void getAllPPlanDetail(){
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        Map map = new HashMap();
        String orderBy = getPara("prop");
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));
        Date createTime=getParaToDate("create_time");
        String createTimeStr= DateAndStringFormat.getStringDateShort(createTime);
        String [] createTimes =new String[2];
        createTimes[0] = DateAndStringFormat.getNextDay(createTimeStr,"-1")+" 12:00:00";
        createTimes[1] = createTimeStr+" 11:59:59";

        String userName =getPara("user_name");
        String userPhone=getPara("user_phone");
        String userID = getPara("user_id");
        String productName=getPara("product_name");
        String productID = getPara("product_id");
        String productStandardName = getPara("product_standard_name");
        String productStandardID = getPara("product_standard_id");
        map.put("createTimes",createTimes);
        map.put("userName",userName);
        map.put("userPhone",userPhone);
        map.put("userID",userID);
        map.put("productName",productName);
        map.put("productID",productID);
        map.put("productStandardName",productStandardName);
        map.put("productStandardID",productStandardID);
        Page<ProcurementPlanDetail> pPlanDetailPage= ProcurementPlanDetail.dao.getPPlanDetail(pageNum,pageSize,orderBy,isASC,map);
        ProcurementPlan procurementPlan = ProcurementPlan.dao.getWaitStatisticsOrderTotal(createTimes,createTimeStr);
        if(pPlanDetailPage.getList()!=null && pPlanDetailPage.getList().size()>0){
            pPlanDetailPage.getList().get(0).put("waitStatisticsOrderTotal",procurementPlan.getWaitStatisticsOrderTotal());
        }
        renderJson(pPlanDetailPage);
    }

    /**
     * 根据时间字段更新采购计划
     */
    @Before(Tx.class)
    public void updatePPlanDetail(){
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        // 获取当前操作用户
        User user = User.dao.findById(uid);
        Date createTime=getParaToDate("createTime");
        String createTimeStr= DateAndStringFormat.getStringDateShort(createTime);
        String [] createTimes =new String[2];
        createTimes[0] = DateAndStringFormat.getNextDay(createTimeStr,"-1")+" 12:00:00";
        createTimes[1] = createTimeStr+" 11:59:59";
        // 获取要导出数据
        List<ProcurementPlan> planList = ProcurementPlan.dao.getExportDataByPPlanID(createTimes);
        // 先执行删除操作
//        ProcurementPlanDetail.dao.delPPlanDetail(createTimes);
        List<String> list = new ArrayList<>();
        try {
            for (ProcurementPlan procurementPlan : planList) {
                ProcurementPlanDetail procurementPlanDetail2=ProcurementPlanDetail.dao.getPPlanDetailByPSID(procurementPlan.get("productStandardID"),createTimes,null);
                // 如果采购计划不存在就新增
                if(procurementPlanDetail2==null){
                    Integer productId =procurementPlan.get("productId");
                   ProcurementPlanDetail procurementPlanDetail =new ProcurementPlanDetail();
                    procurementPlanDetail.setProductId(productId);
                    procurementPlanDetail.setProductStandardId(procurementPlan.get("productStandardID"));
                    procurementPlanDetail.setProcurementId(uid);
                    procurementPlanDetail.setProductName( procurementPlan.get("productName"));
                    procurementPlanDetail.setProductStandardName(procurementPlan.get("productStandardName"));
                    procurementPlanDetail.setSellPrice(procurementPlan.get("sellPrice"));
                    procurementPlanDetail.setInventoryNum(Integer.parseInt (procurementPlan.get("inventoryNum")+""));
                    procurementPlanDetail.setProcurementNum(Integer.parseInt(procurementPlan.get("procurementNum")+""));
                    procurementPlanDetail.setProductStandardNum(Integer.parseInt(procurementPlan.get("productStandardNum")+""));
                    procurementPlanDetail.setProcurementNeedPrice(BigDecimal.valueOf(procurementPlan.get("procurementNeedPrice")));
                    procurementPlanDetail.setProcurementTotalPrice(BigDecimal.valueOf(procurementPlan.get("procurementTotalPrice")));
                    procurementPlanDetail.setOrderRemark(procurementPlan.get("orderRemark"));
                    procurementPlanDetail.setProcurementRemark(procurementPlan.get("procurementRemark"));
                    procurementPlanDetail.setCreateTime(createTime);
                    procurementPlanDetail.setUpdateTime(new Date());
                    procurementPlanDetail.save();
                }else {
                    procurementPlanDetail2.setProductStandardNum(Integer.parseInt(procurementPlan.get("productStandardNum")+""));
                    procurementPlanDetail2.setUpdateTime(new Date());
                    procurementPlanDetail2.update();
                }
            }
            // 订单日志修改为1（被统计过）
            ProcurementPlan.dao.updateOrderLog(createTimes);
            list.add("0");
        }catch (Exception e){
            list.add("1");
        }
        renderJson(list);
    }

    /**
     * 根据采购计划编码获取采购计划信息
     */
    public void getPPlanDetailByID(){
        Integer id=getParaToInt("id");
        renderJson(ProcurementPlanDetail.dao.getPPlanDetailByID(id));
    }

    /**
     * 获取所有的采购人员
     */
    public void getAllProcurementUser(){
        renderJson(User.dao.getAllUser());
    }

    /**
     * 修改采购计划
     */
    public void updatePPlanDetailTwo(){
        ProcurementPlanDetail procurementPlanDetail=getModel(ProcurementPlanDetail.class,"",true);
        procurementPlanDetail.update();
        renderJson(new ArrayList<>(1).add(0));
    }

    public void uploaderExcel(){
//        UploadFile uploadFile=getFile("file");
        try{
            String fileName=getPara("fileName");
            String filePath = CommonController.FILE_PATH + File.separator + fileName;
            ExcelRd excelRd = new ExcelRd(filePath);
            excelRd.setStartRow(1);
            excelRd.setStartCol(0);
            ExcelRdTypeEnum[] types = {
                    ExcelRdTypeEnum.INTEGER,
                    ExcelRdTypeEnum.STRING,
                    ExcelRdTypeEnum.STRING,
                    ExcelRdTypeEnum.INTEGER,
                    ExcelRdTypeEnum.DOUBLE,
                    ExcelRdTypeEnum.INTEGER,
                    ExcelRdTypeEnum.STRING
            };
            // 指定每列的类型
            excelRd.setTypes(types);

            List<ExcelRdRow> rows = null;
            try {
                rows = excelRd.analysisXlsx();
            } catch (ExcelRdException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Iterator<ExcelRdRow> iterator = rows.iterator();
            Integer count=0;
            String [] createTimes =new String[2];
            Map<Integer,List<Integer>> map=new HashMap<>(12);
            while (iterator.hasNext()) {
                ExcelRdRow next = iterator.next();
                List<Object> row = next.getRow();
                count ++;
                if(count ==1){
                    String createTimeStr= ((String)row.get(0)).split(" ")[0].substring(5);
                    createTimes[0] = DateAndStringFormat.getNextDay(createTimeStr,"-1")+" 12:00:00";
                    createTimes[1] = createTimeStr+" 11:59:59";
                }
                if(count>2){
                    boolean flag = false;
                    ProcurementPlanDetail procurementPlanDetail= ProcurementPlanDetail.dao.getPPlanDetailByPSID((Integer) row.get(0),createTimes,null);
                    if(procurementPlanDetail!=null){
                        List<Integer> userIDs=map.get(row.get(0));
                        if(userIDs!=null){
                            for (int i = 0;i < userIDs.size(); i++) {
                                Integer userID=userIDs.get(i);
                                if(userID!=null && userID!=(int)row.get(5)){
                                    flag = true;
                                    userIDs.add((int)row.get(5));
                                    map.put((int)row.get(0),userIDs);
                                    break;
                                }else {
                                    userIDs.add((int)row.get(5));
                                    map.put((int)row.get(0),userIDs);
                                }
                            }
                        }else {
                            if(userIDs==null){
                                userIDs=new ArrayList<>();
                            }
                            userIDs.add((int)row.get(5));
                            map.put((int)row.get(0),userIDs);
                        }
                        if(flag){
                            procurementPlanDetail.setId(null);
                            procurementPlanDetail.setProductStandardId((int)row.get(0));
                            procurementPlanDetail.setProductName((String)row.get(1));
                            procurementPlanDetail.setProductStandardName((String)row.get(2));
                            procurementPlanDetail.setProcurementNum((int)row.get(3));
                            procurementPlanDetail.setProcurementNeedPrice(new BigDecimal((Double) row.get(4)));
                            procurementPlanDetail.setProcurementId((int)row.get(5));
                            procurementPlanDetail.setProcurementRemark((String)row.get(6));
                            BigDecimal pNeedPrice=new BigDecimal((Double) row.get(4));
                            BigDecimal pNum =new BigDecimal((int)row.get(3));
                            procurementPlanDetail.setProcurementTotalPrice(pNeedPrice.multiply(pNum));
                            procurementPlanDetail.setUpdateTime(new Date());
                            procurementPlanDetail.save();
                        }else {
                            procurementPlanDetail.setProductStandardId((int)row.get(0));
                            procurementPlanDetail.setProductName((String)row.get(1));
                            procurementPlanDetail.setProductStandardName((String)row.get(2));
                            procurementPlanDetail.setProcurementNum((int)row.get(3));
                            procurementPlanDetail.setProcurementNeedPrice(new BigDecimal((Double) row.get(4)));
                            procurementPlanDetail.setProcurementId((int)row.get(5));
                            procurementPlanDetail.setProcurementRemark((String)row.get(6));
                            BigDecimal pNeedPrice=new BigDecimal((Double) row.get(4));
                            BigDecimal pNum =new BigDecimal((int)row.get(3));
                            procurementPlanDetail.setProcurementTotalPrice(pNeedPrice.multiply(pNum));
                            procurementPlanDetail.setUpdateTime(new Date());
                            procurementPlanDetail.update();
                        }
                    }
                }
            }
            // 进行删除操作
            List<ProcurementPlanDetail> procurementPlanDetails=ProcurementPlanDetail.dao.getPSIDAndPSCount(createTimes);
                for (int i = 0; i < procurementPlanDetails.size(); i++) {
                ProcurementPlanDetail procurementPlanDetail= procurementPlanDetails.get(i);
                Integer psID=procurementPlanDetail.getProductStandardId();
                Integer psCount= Integer.parseInt(procurementPlanDetail.get("pscount")+"");
                List<Integer> list =map.get(psID);
                // 判断导入的用户是否少了
                if( list!=null && list.size()<psCount){
                    List<ProcurementPlanDetail> procurementPlanDetailList= ProcurementPlanDetail.dao.getPPDIDAndProcurementID(createTimes, psID);
                    for (int j = 0; j < procurementPlanDetailList.size(); j++) {
                        ProcurementPlanDetail procurementPlanDetail2= procurementPlanDetailList.get(j);
                        Integer procurementId= procurementPlanDetail2.getProcurementId();
                        Integer id=procurementPlanDetail2.getId();
                        boolean isExist=false;
                        for (int k = 0; k < list.size(); k++) {
                            if(procurementId!=null && procurementId.equals(list.get(k))){
                                isExist = true;
                            }
                        }
                        if(!isExist){
                            procurementPlanDetail2.deleteById(id);
                        }
                    }
                }
            }
            renderJson(new ArrayList<>().add(0));
        }catch (Exception e){
            renderJson(new ArrayList<>().add(1));
        }

    }








    public static List<ExcelRdRow> readExcel(String filePath){
        ExcelRd excelRd = new ExcelRd(filePath);
        excelRd.setStartRow(1);
        excelRd.setStartCol(0);
        ExcelRdTypeEnum[] types = {
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.STRING
        };
        // 指定每列的类型
        excelRd.setTypes(types);

        List<ExcelRdRow> rows = null;
        try {
            rows = excelRd.analysisXlsx();
        } catch (ExcelRdException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }



    public void uploaderExcelTwo(){
        try{

            String fileName=getPara("fileName");
            String filePath = CommonController.FILE_PATH + File.separator + fileName;
            Iterator<ExcelRdRow> iterator = PlanDetailController.readExcel(filePath).iterator();
            Integer count=0;
            Map<String,ProcurementPlanDetail> mapKeyOne =new HashMap<>();
            Map<String,ProcurementPlanDetail> mapKeyTwo =new HashMap<>();
            String [] createTimes =new String[2];
            // 循环Excel行
            while (iterator.hasNext()) {
                ExcelRdRow next = iterator.next();
                List<Object> row = next.getRow();
                count ++;
                if(count ==1){
                    String createTimeStr= ((String)row.get(0)).split(" ")[1];
                    createTimes[0] = DateAndStringFormat.getNextDay(createTimeStr,"-1")+" 12:00:00";
                    createTimes[1] = createTimeStr+" 11:59:59";
                    // 根据时间获取所有的采购计划
                    List<ProcurementPlanDetail> procurementPlanDetailList= ProcurementPlanDetail.dao.getAllPPlanDetail(createTimes);
                    if(procurementPlanDetailList!=null && procurementPlanDetailList.size()>0) {
                        //循环所有的采购计划数据到Map临时数据
                        for (ProcurementPlanDetail pPlanDetail : procurementPlanDetailList) {
                            // key为productStandardId,value为ProcurementPlanDetail
                            mapKeyOne.put(pPlanDetail.getProductStandardId()+"",pPlanDetail);
                            // key为productStandardId+"-"+procurementId,value为ProcurementPlanDetail
                            mapKeyTwo.put(pPlanDetail.getProductStandardId() + "-"+pPlanDetail.getProcurementId(),pPlanDetail);
                        }
                    }
                }
                if(count>2){
                    if(count==3){
                        // 根据时间删除所有的采购计划
                        ProcurementPlanDetail.dao.delAllPPlanDetailByTime(createTimes);
                    }
                    ProcurementPlanDetail pPDtailTwo = mapKeyTwo.get((Integer) row.get(0) + "-" + (Integer) row.get(5));
                    if(pPDtailTwo!=null){
                        pPDtailTwo.setId(null);
                        pPDtailTwo.setProductStandardId((int)row.get(0));
                        pPDtailTwo.setProductName((String)row.get(1));
                        //执行入库操作
                        // putInStore((Integer)row.get(0),(Integer)row.get(3));
                        pPDtailTwo.setProductStandardName((String)row.get(2));
                        pPDtailTwo.setProcurementNum((int)row.get(3));
                        pPDtailTwo.setProcurementNeedPrice(new BigDecimal((Double) row.get(4)));
                        pPDtailTwo.setProcurementId((int)row.get(5));
                        pPDtailTwo.setProcurementRemark((String)row.get(6));
                        BigDecimal pNeedPrice=new BigDecimal((Double) row.get(4));
                        BigDecimal pNum =new BigDecimal((int)row.get(3));
                        pPDtailTwo.setProcurementTotalPrice(pNeedPrice.multiply(pNum));
                        pPDtailTwo.setUpdateTime(new Date());
                        pPDtailTwo.save();
                    }else {
                        ProcurementPlanDetail pPDtailOne = mapKeyOne.get(row.get(0)+"");
                        if(pPDtailOne!=null){
                            pPDtailOne.setId(null);
                            pPDtailOne.setProductStandardId((int)row.get(0));
                            //执行入库操作
//                            putInStore((Integer)row.get(0),(Integer)row.get(3));
                            pPDtailOne.setProductName((String)row.get(1));
                            pPDtailOne.setProductStandardName((String)row.get(2));
                            pPDtailOne.setProcurementNum((int)row.get(3));
                            pPDtailOne.setProcurementNeedPrice(new BigDecimal((Double) row.get(4)));
                            pPDtailOne.setProcurementId((int)row.get(5));
                            pPDtailOne.setProcurementRemark((String)row.get(6));
                            BigDecimal pNeedPrice=new BigDecimal((Double) row.get(4));
                            BigDecimal pNum =new BigDecimal((int)row.get(3));
                            pPDtailOne.setProcurementTotalPrice(pNeedPrice.multiply(pNum));
                            pPDtailOne.setUpdateTime(new Date());
                            pPDtailOne.save();
                        }
                    }
                }
            }

            renderJson(new ArrayList<>().add(0));
        }catch (Exception e){
            renderJson(new ArrayList<>().add(1));
        }
    }

    public void putInStore(Integer productStandardId,Integer changeNum){
        ProductStandard productStandard =ProductStandard.dao.getProductStandardById(productStandardId);
        if(productStandard!=null){
            productStandard.setStock(productStandard.getStock()+changeNum);
            productStandard.update();
        }
    }

}
