package com.fruit.manage.controller.procurement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.RoleKeyCode;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.*;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.ZhioDateUtils;
import com.fruit.manage.util.excelRd.ExcelRd;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdRow;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author partner
 * @date 2018/3/23 15:07
 */
public class PlanDetailController extends BaseController {
    public static Logger logger = Logger.getLogger(PlanController.class);

    /**
     * 获取所有的采购计划
     */
    public void getAllPPlanDetail() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        Map map = new HashMap();
        String orderBy = getPara("prop");
        String procurementPlanId = getPara("procurementPlanId");
        map.put("procurementPlanId", procurementPlanId);
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));

        Date createTime = getParaToDate("order_cycle_date");
        String createTimeStr = getPara("order_cycle_date");
        String[] createTimes = ZhioDateUtils.getOrderCycleDateStrings(createTime);

        String userName = getPara("user_name");
        String userPhone = getPara("user_phone");
        String userID = getPara("user_id");
        String productName = getPara("product_name");
        String productID = getPara("product_id");
        String productStandardName = getPara("product_standard_name");
        String productStandardID = getPara("product_standard_id");
        map.put("createTimes", createTimes);
        map.put("userName", userName);
        map.put("userPhone", userPhone);
        map.put("userID", userID);
        map.put("productName", productName);
        map.put("productID", productID);
        map.put("productStandardName", productStandardName);
        map.put("productStandardID", productStandardID);
        Page<ProcurementPlanDetail> pPlanDetailPage = ProcurementPlanDetail.dao.getPPlanDetail(pageNum, pageSize, orderBy, isASC, map);
        Record procurementPlan = ProcurementPlan.dao.getWaitStatisticsOrderTotal(createTimeStr);
        if (pPlanDetailPage.getList() != null && pPlanDetailPage.getList().size() > 0) {
            pPlanDetailPage.getList().get(0).put("waitStatisticsOrderTotal", procurementPlan.get("wait_statistics_order_total"));
        }
        renderJson(pPlanDetailPage);
    }

    /**
     * 根据时间字段更新采购计划
     */
    @Before(Tx.class)
    public void updatePPlanDetail() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        // 获取当前操作用户
        User user = User.dao.findById(uid);


        String procurementPlanId = getPara("procurementPlanId");


        Date orderCycleDate = getParaToDate("order_cycle_date");
        String orderCycleDateStr = getPara("order_cycle_date");
        String[] orderCycleDates = ZhioDateUtils.getOrderCycleDateStrings(orderCycleDate);

        // 获取要导出数据
        List<ProcurementPlan> planList = ProcurementPlan.dao.getExportDataByPPlanID(orderCycleDates);
        // 先执行删除操作
//        ProcurementPlanDetail.dao.delPPlanDetail(orderCycleDates);
        for (ProcurementPlan procurementPlan : planList) {
            ProcurementPlanDetail procurementPlanDetail = ProcurementPlanDetail.dao.getPPlanDetailByPSID(procurementPlan.get("productStandardID"), procurementPlanId, null);
            // 如果采购计划不存在就新增
            if (procurementPlanDetail == null) {
                Integer productId = procurementPlan.get("productId");
                procurementPlanDetail = new ProcurementPlanDetail();
                procurementPlanDetail.setProductId(productId);
                procurementPlanDetail.setProductStandardId(procurementPlan.get("productStandardID"));
                procurementPlanDetail.setProcurementId(procurementPlan.get("procurement_id"));
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
                procurementPlanDetail.setProcurementPlanId(procurementPlanId);
                procurementPlanDetail.setCreateTime(orderCycleDate);
                procurementPlanDetail.setUpdateTime(new Date());
                procurementPlanDetail.setProcurementPlanId(procurementPlanId);
                procurementPlanDetail.save();
            } else {
                procurementPlanDetail.setProductStandardNum(Integer.parseInt(procurementPlan.get("productStandardNum") + ""));
                procurementPlanDetail.setUpdateTime(new Date());
                procurementPlanDetail.update();
            }
        }
        // 订单日志修改为1（被统计过）
        ProcurementPlan.dao.updateOrderLog(orderCycleDateStr);
        // 修改采购计划的数据
        Record orderLogInfo = ProcurementPlan.dao.getOrderLogByPPlan(orderCycleDates);
        ProcurementPlan procurementPlan = ProcurementPlan.dao.getProcurementPlanByOrderCycleDate(orderCycleDateStr);

        procurementPlan.setNum(Integer.parseInt(orderLogInfo.get("num") + ""));
        procurementPlan.setOrderTotal(Integer.parseInt(orderLogInfo.get("order_total") + ""));
        procurementPlan.setProductStandardNum(Integer.parseInt(orderLogInfo.get("product_standard_num") + ""));
        procurementPlan.setWaitStatisticsOrderTotal(0);
        procurementPlan.setProcurementId(getSessionAttr(Constant.SESSION_UID));
        procurementPlan.update();
        renderNull();
    }

    /**
     * 根据采购计划编码获取采购计划信息
     */
    public void getPPlanDetailByID() {
        Integer id = getParaToInt("id");
        renderJson(ProcurementPlanDetail.dao.getPPlanDetailByID(id));
    }

    /**
     * 获取所有的采购人员
     */
    public void getAllProcurementUser() {
        // 获取所有的采购人员
        List<User> userList = User.dao.getUserNickNameAndUId(RoleKeyCode.PROCUREMENT);
        renderJson(userList);
    }

    /**
     * 修改采购计划
     */
    public void updatePPlanDetailTwo() {
        ProcurementPlanDetail procurementPlanDetail = getModel(ProcurementPlanDetail.class, "", true);
        procurementPlanDetail.update();
        renderJson(new ArrayList<>(1).add(0));
    }

    /**
     * 读取Excel多表
     *
     * @param filePath
     * @return
     */
    public List<List<ExcelRdRow>> readExcelMultiTable(String filePath) {
        ExcelRdTypeEnum[] types = {
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.STRING
        };
        ExcelRd excelRd = new ExcelRd(filePath);
        excelRd.setStartRow(0);
        excelRd.setStartCol(0);
        excelRd.setTypes(types);
        List<List<ExcelRdRow>> lists = null;
        try {
            lists = excelRd.analysisXlsxMultiTable();
        } catch (ExcelRdException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lists;
    }

    public static List<ExcelRdRow> readExcel(String filePath) {
        ExcelRd excelRd = new ExcelRd(filePath);
        excelRd.setStartRow(1);
        excelRd.setStartCol(0);
        //0-商品名，1-规格名，2-规格编码，3-重量(斤)，4-报价，5-下单量，6-库存量，7-采购量，8-采购单价，9-下单备注
        ExcelRdTypeEnum[] types = {
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.DOUBLE,
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

    @Before(Tx.class)
    public void uploaderExcelThree() {
        Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                int tableIndex = 0;
                int rowIndex = 1;
                try {
                    String procurementPlanId = getPara("procurementPlanId");

                    String fileName = getPara("fileName");

                    Date orderCycleDate = getParaToDate("order_cycle_date");

                    String filePath = CommonController.FILE_PATH + File.separator + fileName;

                    // 获取目前的采购计划报表
                    List<ProcurementPlanDetail> oldPlanDetailList = ProcurementPlanDetail.dao.getAllPPlanDetail(procurementPlanId);
                    // 基于采购人和商品规格分类
                    Map<String, ProcurementPlanDetail> ppDsMapBypps = oldPlanDetailList.stream().collect(Collectors.toMap(p -> p.getProcurementId() + "-" + p.getProductStandardId(), Function.identity()));
                    // 基于商品规格分类
                    Map<Integer, ProcurementPlanDetail> ppDsMapByp = oldPlanDetailList.stream().collect(Collectors.toMap(ProcurementPlanDetail::getProductStandardId, Function.identity()));
                    // 删除所有采购计划报表
                    ProcurementPlanDetail.dao.delAllPlanDetailByPPlanId(procurementPlanId);

                    List<List<ExcelRdRow>> listExcelRdRows = readExcelMultiTable(filePath);
                    for (int i = 0; i < listExcelRdRows.size(); i++) {
                        List<ExcelRdRow> excelRdRows = listExcelRdRows.get(i);

                        String procurementName = (String)excelRdRows.get(1).getRow().get(3);
                        Integer procurementId = User.dao.getUserIdByName(( procurementName).split(":")[1]);
                        if (procurementId == null) {
                            continue;
                        }

                        // 数据在第三行
                        for (rowIndex = 3; rowIndex < excelRdRows.size(); rowIndex++) {
                            //0-商品名，1-规格名，2-规格编码，3-重量(斤)，4-报价，5-下单量，6-库存量，7-采购量，8-采购单价，9-下单备注
                            //0-规格编码，1-商品名，2-规格名，3-采购数量，4-采购单价，5-采购人，6-采购备注
                            List<Object> row = excelRdRows.get(rowIndex).getRow();
                            String productName = row.get(0) + "";
                            String productStandardName = row.get(1) + "";
                            Integer productStandardId = Integer.parseInt(row.get(2) + "");
                            Integer procurementNum = Integer.parseInt(row.get(7) + "");
                            BigDecimal procurementNeedPrice = new BigDecimal(Double.parseDouble(row.get(8) + ""));
                            String procurementRemark = row.get(9) + "";

                            ProcurementPlanDetail ppd = ppDsMapBypps.get(procurementId + "-" + productStandardId);
                            if (ppd != null) {
                                // 覆盖

                                BigDecimal pNum = new BigDecimal(procurementNum);
                                ppd.setId(null);
                                ppd.setProductStandardId(productStandardId);
                                ppd.setProductName(productName);
                                ppd.setProductStandardName(productStandardName);
                                ppd.setProcurementNum(procurementNum);
                                ppd.setProcurementNeedPrice(procurementNeedPrice);
                                ppd.setProcurementId(procurementId);
                                ppd.setProcurementRemark(procurementRemark);
                                ppd.setProcurementTotalPrice(procurementNeedPrice.multiply(pNum));
                                ppd.setUpdateTime(new Date());
                                ppd.setProcurementPlanId(procurementPlanId);
                                ppd.save();
                            } else {
                                // 新增
                                Product product = Product.dao.getProductIDByPSID(productStandardId);

                                ProcurementPlanDetail newPpd = new ProcurementPlanDetail();
                                newPpd.setProductStandardId(productStandardId);
                                newPpd.setProductName(productName);
                                newPpd.setSellPrice(product.get("sell_price"));
                                newPpd.setProductStandardNum(0);
                                newPpd.setProductId(product.getId());
                                newPpd.setProductStandardName(productStandardName);
                                newPpd.setProcurementNum(procurementNum);
                                newPpd.setProcurementNeedPrice(procurementNeedPrice);
                                newPpd.setProcurementId(procurementId);
                                newPpd.setProcurementRemark(procurementRemark);
                                BigDecimal pNum = new BigDecimal(procurementNum);
                                newPpd.setProcurementTotalPrice(procurementNeedPrice.multiply(pNum));
                                Date date = new Date();
                                newPpd.setCreateTime(date);
                                newPpd.setUpdateTime(date);
                                newPpd.setProcurementPlanId(procurementPlanId);
                                newPpd.save();
                            }
                        }
                    }
                    renderNull();
                    return true;
                } catch (Exception e) {
                    // 输出错误信息
                    excelRenderErrorInfo(tableIndex, rowIndex, e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            }
        });
    }

    /**
     * 输出Excel的错误信息
     *
     * @param tableIndex
     * @param rowIndex
     * @param errorMsg
     */
    public void excelRenderErrorInfo(Integer tableIndex, Integer rowIndex, String errorMsg) {
        if (tableIndex == 0) {
            renderErrorText(errorMsg);
        } else {
            renderErrorText("第" + tableIndex + "张表，第" + rowIndex + "行数据出现异常\n异常信息是：" + errorMsg);
        }
    }

}
