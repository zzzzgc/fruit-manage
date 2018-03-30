package com.fruit.manage.controller.procurement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.ProcurementPlan;
import com.fruit.manage.model.ProcurementPlanDetail;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.DateAndStringFormat;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.IdUtil;
import com.fruit.manage.util.excel.ExcelException;
import com.jfinal.ext.kit.DateKit;
import com.jfinal.plugin.activerecord.Page;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        if (pPlanPage.getList() != null && pPlanPage.getList().size() > 0) {
            for (int i = 0; i < pPlanPage.getList().size(); i++) {
                ProcurementPlan procurementPlan = pPlanPage.getList().get(i);
                String createTime = DateAndStringFormat.getStringDateShort(procurementPlan.getCreateTime());
                String[] createTimes = new String[2];
                createTimes[0] = DateAndStringFormat.getNextDay(createTime, "-1") + " 12:00:00";
                createTimes[1] = createTime + " 11:59:59";
                ProcurementPlan procurementPlan2 = ProcurementPlan.dao.getWaitStatisticsOrderTotal(createTimes, createTime);
                procurementPlan.setWaitStatisticsOrderTotal(procurementPlan2.getWaitStatisticsOrderTotal());
            }
        }
        renderJson(pPlanPage);
    }

    /**
     * 删除采购计划
     */
    public void delPPlan() {
        int pPlanId = getParaToInt("pPlanId");
        renderResult(ProcurementPlan.dao.deleteById(pPlanId));
    }

    /**
     * 添加采购计划
     */
    public void addPlan() {
        String[] create_time = new String[2];
        String nowDateStr = DateAndStringFormat.getStringDateShort(new Date());
        if (Integer.parseInt(DateAndStringFormat.getHour()) >= 12) {
            //当前时间大于12小时的情况（包括12小时）
            create_time[0] = DateAndStringFormat.getStringDateShort(new Date()) + " 12:00:00";
            create_time[1] = DateAndStringFormat.getNextDay(DateAndStringFormat.getStringDateShort(new Date()), "1") + " 11:59:59";
            nowDateStr = DateAndStringFormat.getNextDay(DateAndStringFormat.getStringDateShort(new Date()), "1");
        } else {
            //当前时间小于12小时的情况（不包括12小时）
            create_time[0] = DateAndStringFormat.getNextDay(DateAndStringFormat.getStringDateShort(new Date()), "-1") + " 12:00:00";
            create_time[1] = DateAndStringFormat.getStringDateShort(new Date()) + " 11:59:59";
        }
        ProcurementPlan procurementPlan = ProcurementPlan.dao.getPPlan(create_time);
        List<Integer> list = new ArrayList<>();
        if (procurementPlan.getProductStandardNum() != null && procurementPlan.getProductStandardNum() != 0) {
            try {
                ProcurementPlan procurementPlan2 = ProcurementPlan.dao.getPPlanCreateTime(nowDateStr);
                if (procurementPlan2 != null && procurementPlan2.getId() != null) {
                    procurementPlan2.setNum(procurementPlan.getNum());
                    procurementPlan2.setOrderTotal(procurementPlan.getOrderTotal());
                    procurementPlan2.setProductStandardNum(procurementPlan.getProductStandardNum());
                    procurementPlan2.setWaitStatisticsOrderTotal(procurementPlan.getWaitStatisticsOrderTotal());
                    procurementPlan2.setProcurementId(getSessionAttr(Constant.SESSION_UID));
                    procurementPlan2.update();
                    // 此段日期已经有人有人下单，并重新覆盖
                    list.add(0);
                } else {
                    procurementPlan.setId(IdUtil.getProrementPlanId());
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
        Date createTime = getParaToDate("createTime");
        String createTimeStr = DateAndStringFormat.getStringDateShort(createTime);
        String[] create_time = new String[2];
        create_time[0] = DateAndStringFormat.getNextDay(createTimeStr, "-1") + " 12:00:00";
        create_time[1] = createTimeStr + " 11:59:59";
        ProcurementPlan procurementPlan = ProcurementPlan.dao.getPPlan(create_time);
        ProcurementPlan procurementPlan2 = ProcurementPlan.dao.getPPlanCreateTime(createTimeStr);
        if (procurementPlan != null && procurementPlan2 != null) {
            procurementPlan2.setNum(procurementPlan.getNum());
            procurementPlan2.setOrderTotal(procurementPlan.getOrderTotal());
            procurementPlan2.setProductStandardNum(procurementPlan.getProductStandardNum());
            procurementPlan2.setWaitStatisticsOrderTotal(procurementPlan.getWaitStatisticsOrderTotal());
            procurementPlan2.setProcurementId(getSessionAttr(Constant.SESSION_UID));
            procurementPlan2.update();
        }
        // 订单日志修改为1（被统计过）
        ProcurementPlan.dao.updateOrderLog(create_time);
        renderJson(new ArrayList<>().add(0));
    }

    /**
     * 根据采购计划ID导出采购计划单
     */
    public void exportPPlan() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        // 获取当前操作用户
        User user = User.dao.findById(uid);

        Date createTime = getParaToDate("createTime");
        String createTimeStr = DateAndStringFormat.getStringDateShort(createTime);
        String[] createTimes = new String[2];
        createTimes[0] = DateAndStringFormat.getNextDay(createTimeStr, "-1") + " 12:00:00";
        createTimes[1] = createTimeStr + " 11:59:59";
        // 获取要导出数据
        List<ProcurementPlan> planList = ProcurementPlan.dao.getExportDataByPPlanID(createTimes);

        // 行头
        String[] header = {"商品名", "规格名", "规格编码", "重量(斤)", "报价", "下单量", "库存量", "采购量", "采购单价", "下单备注"};
        // 先执行删除操作
        ProcurementPlanDetail.dao.delPPlanDetail(createTimes);

        // excel表格信息
        HashMap<Integer, Map<String, Object>> excelInfoList = new HashMap<>(5);


//        List<String[]> listData = new ArrayList<String[]>();
        String zipFileName = createTimeStr+"订单周期的采购计划表.zip";
        String zipFolder = CommonController.FILE_PATH + File.separator + zipFileName;
        File zipFolderFile = new File(zipFolder);
        if (zipFolderFile.exists()) {
            zipFolderFile.mkdirs();
        }
        if (planList.size() <1) {
            renderErrorText("一条订单都没有");
            return;
        }
        for (ProcurementPlan procurementPlan : planList) {
            // 根据采购人分别保存信息,用来区分不同的采购采购的东西
            Map<String, Object> excelInfo = excelInfoList.get(procurementPlan.get("procurement_id"));
            List<Object[]> listData = null;
            if (excelInfo == null) {
                excelInfo = new HashMap<>(20);
                excelInfoList.put(procurementPlan.get("procurement_id"),excelInfo);
                excelInfo.put("path", zipFolder);
                excelInfo.put("fileName", "file_" + UUID.randomUUID().toString().replaceAll("-", "") + ".xlsx");
                excelInfo.put("title", "采购计划表");
                excelInfo.put("createBy", procurementPlan.get("procurement_name"));
                excelInfo.put("header", header);
                excelInfo.put("listData", new ArrayList<Object[]>());
                listData = (List<Object[]>) excelInfo.get("listData");
            } else {
                listData = (List<Object[]>) excelInfo.get("listData");
            }
            Object[] str = new Object[header.length];
            Integer productId = procurementPlan.get("productId");
            // 商品名
            str[0] = procurementPlan.get("productName");
            // 规格名
            str[1] = procurementPlan.get("productStandardName");
            // 规格编号
            str[2] = procurementPlan.get("productStandardID");
            // 水果重量
            str[3] = procurementPlan.get("fruitWeight");
            // 报价
            str[4] = procurementPlan.get("sellPrice");
            str[5] = procurementPlan.get("purchaseNum");
            str[6] = procurementPlan.get("inventoryNum");
            str[7] = procurementPlan.get("procurementNum");
            str[8] = procurementPlan.get("procurementPrice");
            // TODO 需要改成订单备注
            str[9] = procurementPlan.get("procurementRemark");
            listData.add(str);
            ProcurementPlanDetail procurementPlanDetail = new ProcurementPlanDetail();
            procurementPlanDetail.setProductId(productId);
            procurementPlanDetail.setProductStandardId(procurementPlan.get("productStandardID"));
            procurementPlanDetail.setProcurementId(uid);
            procurementPlanDetail.setProductName(procurementPlan.get("productName"));
            procurementPlanDetail.setProductStandardName(procurementPlan.get("productStandardName"));
            procurementPlanDetail.setSellPrice(procurementPlan.get("sellPrice"));
            procurementPlanDetail.setInventoryNum(Integer.parseInt(procurementPlan.get("inventoryNum") + ""));
            procurementPlanDetail.setProcurementNum(Integer.parseInt(procurementPlan.get("procurementNum") + ""));
            procurementPlanDetail.setProductStandardNum(Integer.parseInt(procurementPlan.get("productStandardNum") + ""));
            procurementPlanDetail.setProcurementNeedPrice(BigDecimal.valueOf(procurementPlan.get("procurementNeedPrice")));
            procurementPlanDetail.setProcurementTotalPrice(BigDecimal.valueOf(procurementPlan.get("procurementTotalPrice")));
            procurementPlanDetail.setOrderRemark(procurementPlan.get("orderRemark"));
            procurementPlanDetail.setProcurementRemark(procurementPlan.get("procurementRemark"));
            procurementPlanDetail.setCreateTime(createTime);
            procurementPlanDetail.setUpdateTime(new Date());
            procurementPlanDetail.save();
        }

        //保存路径
//        String savePath = getRequest().getSession().getServletContext().getRealPath("static/excel");
//        System.out.println("\n" + savePath);
//        String fpath = getSession().getServletContext().getRealPath("static/excel");
//        String fileName = "file_" + UUID.randomUUID().toString().replaceAll("-", "") + ".xlsx";
//        System.out.println(fpath + "\n");
//        Map map = new HashMap(12);
//        map.put("path", savePath);
//        map.put("fileName", fileName);
//        map.put("title", "采购计划表");
//        map.put("createBy", user.getName());
//        map.put("header", header);
//        map.put("listData", listData);
        ArrayList<File> files = new ArrayList<>();
        for (Integer integer : excelInfoList.keySet()) {
            Map<String, Object> stringObjectMap = excelInfoList.get(integer);
            String file = null;
            try {
                file = ExcelCommon.createExcelModul(stringObjectMap);
            } catch (ExcelException e) {
                renderErrorText(e.getMessage());
            }
            files.add(new File(file));
        }
        String zipName  = zipFileName + ".zip";
        boolean b = fileToZip(zipFolder, CommonController.FILE_PATH, zipFileName);
        HashMap<Object, Object> objectObjectHashMap = new HashMap<>(1);
        objectObjectHashMap.put("zipName",zipName);
        renderJson(objectObjectHashMap);
    }

    /**
     * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下
     * @param sourceFilePath :待压缩的文件路径
     * @param zipFilePath :压缩后存放路径
     * @param fileName :压缩后文件的名称
     * @return
     */
    public static boolean fileToZip(String sourceFilePath,String zipFilePath,String fileName){
        boolean flag = false;
        File sourceFile = new File(sourceFilePath);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;

        if(sourceFile.exists() == false){
            System.out.println("待压缩的文件目录："+sourceFilePath+"不存在.");
        }else{
            try {
                File zipFile = new File(zipFilePath + "/" + fileName +".zip");
                if(zipFile.exists()){
                    System.out.println(zipFilePath + "目录下存在名字为:" + fileName +".zip" +"打包文件.");
                }else{
                    File[] sourceFiles = sourceFile.listFiles();
                    if(null == sourceFiles || sourceFiles.length<1){
                        System.out.println("待压缩的文件目录：" + sourceFilePath + "里面不存在文件，无需压缩.");
                    }else{
                        fos = new FileOutputStream(zipFile);
                        zos = new ZipOutputStream(new BufferedOutputStream(fos));
                        byte[] bufs = new byte[1024*10];
                        for(int i=0;i<sourceFiles.length;i++){
                            //创建ZIP实体，并添加进压缩包
                            ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
                            zos.putNextEntry(zipEntry);
                            //读取待压缩的文件并写进压缩包里
                            fis = new FileInputStream(sourceFiles[i]);
                            bis = new BufferedInputStream(fis, 1024*10);
                            int read = 0;
                            while((read=bis.read(bufs, 0, 1024*10)) != -1){
                                zos.write(bufs,0,read);
                            }
                        }
                        flag = true;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally{
                //关闭流
                try {
                    if(null != bis) bis.close();
                    if(null != zos) zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
        return flag;
    }

    public static Integer testid = 1;

    public void download() {
        String path = getPara("path", testid + 1 + "");
        File file = new File(path);
        if (file.exists()) {
            renderFile(file);
        } else {
            renderJson();
        }
    }
}
