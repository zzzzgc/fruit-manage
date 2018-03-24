package com.fruit.manage.controller.procurement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.ProcurementPlan;
import com.fruit.manage.model.ProcurementPlanDetail;
import com.fruit.manage.model.Product;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.DateAndStringFormat;
import com.fruit.manage.util.excelRd.ExcelRd;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdRow;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;
import com.jfinal.plugin.activerecord.Page;
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
     * TODO 缺少事务
     */
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
                ProcurementPlanDetail procurementPlanDetail2=ProcurementPlanDetail.dao.getPPlanDetailByPSID(procurementPlan.get("productStandardID"),createTimes);
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
                    ProcurementPlanDetail procurementPlanDetail= ProcurementPlanDetail.dao.getPPlanDetailByPSID((Integer) row.get(0),createTimes);
                    if(procurementPlanDetail!=null){
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
                    for (Object t : row) {
                        System.out.print(t+"\t");
                    }
                    System.out.println();
                }
            }
        }catch (Exception e){
            renderJson(new ArrayList<>().add(1));
        }

        renderJson(new ArrayList<>().add(0));
    }
}
