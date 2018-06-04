package com.fruit.manage.controller.statement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.RoleKeyCode;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.ProcurementPlanDetail;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.ExcelException;
import com.jfinal.plugin.activerecord.Page;

import java.math.BigDecimal;
import java.util.*;

/**
 * @Description 2.采购缺货报表
 * @author partner
 * @date 2018/5/25 13:39
 */
public class ProcurementStoreoutController extends BaseController {
    /**
     * 获取采购缺货数据列表
     */
    public void getData(){
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        String orderBy = getPara("prop");
        boolean isASC = "ascending".equals(getPara("order"));
        String[] createTimes = new String[2];
        String procurementId = getPara("procurement_id");
        String productName = getPara("product_name");
        String productStandardName = getPara("product_standard_name");
        String[] createTimes2 = getParaValues("format_create_time");
        String createStartTime = getPara("createStartTime");
        String createEndTime = getPara("createEndTime");
        createTimes[0] = createStartTime;
        createTimes[1] = createEndTime;
        Page<ProcurementPlanDetail> procurementPlanDetailPage= ProcurementPlanDetail.dao.getProcuementStoreout(pageNum,pageSize,orderBy,isASC,procurementId,productName,productStandardName,createTimes2);
        renderJson(procurementPlanDetailPage);
    }



    /**
     * 导出采购缺货数据
     */
    public void exportStoreoutExcel() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        String orderBy = getPara("prop");
        boolean isASC = "ascending".equals(getPara("order"));
        String[] createTimes = new String[2];
        String procurementId = getPara("procurement_id");
        String productName = getPara("product_name");
        String productStandardName = getPara("product_standard_name");
        String[] createTimes2 = getParaValues("format_create_time");
//        Page<ProcurementPlanDetail> procurementPlanDetailPage = ProcurementPlanDetail.dao.getProcuementStoreout(pageNum, pageSize, orderBy, isASC, procurementId, productName, productStandardName, createTimes2);
        List<ProcurementPlanDetail> procurementPlanDetailPage = ProcurementPlanDetail.dao.getProcuementStoreout(procurementId, productName, productStandardName, createTimes2);
        if (procurementPlanDetailPage != null && procurementPlanDetailPage.size() >0) {
            List<String[]> lists = new ArrayList<>();
            for (int i = 0; i < procurementPlanDetailPage.size(); i++) {
                String[] strings = new String[15];
                ProcurementPlanDetail procurementPlanDetail = procurementPlanDetailPage.get(i);
                strings[0] = procurementPlanDetail.getProductName();
                strings[1] = procurementPlanDetail.getProductStandardName();
                strings[2] = procurementPlanDetail.getProductStandardId() + "";
                strings[3] = procurementPlanDetail.getProductStandardNum() + "";
                strings[4] = procurementPlanDetail.getProcurementNum() + "";

                strings[5] = procurementPlanDetail.getInventoryNum() + "";
                strings[6] = procurementPlanDetail.get("storeoutNum")+"";
                strings[7] = "";
                try {
                    BigDecimal storeoutNum = new BigDecimal(procurementPlanDetail.get("storeoutNum")+"");
                    BigDecimal orderDetailTotalNum =new BigDecimal(procurementPlanDetail.get("orderDetailTotalNum")+"");
                    strings[7] = ((storeoutNum).divide(orderDetailTotalNum,2,2)).multiply(new BigDecimal(100)) + "%";
                } catch (Exception e) {
                    e.printStackTrace();
                    strings[7] = "";
                }
                strings[8] = procurementPlanDetail.get("procurement_name");
                strings[9] = procurementPlanDetail.get("costPrice")+"";

                strings[10] = procurementPlanDetail.getSellPrice() + "";
                strings[11] = (procurementPlanDetail.getProductStandardNum() - procurementPlanDetail.getProcurementNum() - procurementPlanDetail.getInventoryNum()) > 0 ? procurementPlanDetail.getProductStandardNum() - procurementPlanDetail.getProcurementNum() - procurementPlanDetail.getInventoryNum() + "": "不缺货" ;
                strings[12] = (procurementPlanDetail.getProductStandardNum() - procurementPlanDetail.getProcurementNum() - procurementPlanDetail.getInventoryNum()) > 0 ? new BigDecimal((procurementPlanDetail.getProductStandardNum() - procurementPlanDetail.getProcurementNum() - procurementPlanDetail.getInventoryNum())).multiply(procurementPlanDetail.getProcurementNeedPrice()) + "" : "0";
                if (procurementPlanDetail.getProcurementTotalPrice()!=null  && procurementPlanDetail.getProcurementTotalPrice().intValue()>0){
                    strings[13] = (procurementPlanDetail.getProductStandardNum() - procurementPlanDetail.getProcurementNum() - procurementPlanDetail.getInventoryNum()) > 0 ? (new BigDecimal((procurementPlanDetail.getProductStandardNum() - procurementPlanDetail.getProcurementNum() - procurementPlanDetail.getInventoryNum())).multiply(procurementPlanDetail.getProcurementNeedPrice()).divide(procurementPlanDetail.getProcurementTotalPrice(),2,2)).multiply(new BigDecimal(100)) + "%" : "0";
                }else {
                    strings[13] = "100%";
                }
                strings[14] = procurementPlanDetail.get("create_time")+"";
                lists.add(strings);
            }

            Integer uid = getSessionAttr(Constant.SESSION_UID);
            User user = User.dao.findById(uid);
            String[] headers = {"商品名称", "商品规格", "规格编号", "下单数", "采购数量",
                    "库存量", "缺货订单详细数", "订单详细缺货率", "采购人", "成本价",
                    "销售价", "缺货数量", "缺货总额", "缺货占比", "采购创建时间"};
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".xlsx";
            String path = CommonController.FILE_PATH;
            String title = "采购缺货报表";
            Map map = new HashMap();
            map.put("path", path);
            map.put("fileName", fileName);
            map.put("title", title);
            map.put("createBy", user.getNickName());
            map.put("header", headers);
            map.put("listData", lists);
            try {
                ExcelCommon.createExcelModul(map);
                List<String> list = new ArrayList<>();
                list.add(fileName);
                renderJson(list);
            } catch (ExcelException e) {
                e.printStackTrace();
                renderErrorText("导出采购缺货单错误!");
            }
        } else {
            renderErrorText("该采购缺货单没有记录!");
        }
    }

    /**
     * 获取所有的采购人
     */
    public void getAllProcurementUser() {
        List<User> userList =User.dao.getUserNickNameAndUId(RoleKeyCode.PROCUREMENT);
        renderJson(userList);
    }
}
