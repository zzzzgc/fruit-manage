package com.fruit.manage.controller.statement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.RoleKeyCode;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.ProcurementPlanDetail;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.ExcelException;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author partner
 * @Description 2.采购缺货报表
 * @date 2018/5/25 13:39
 */
public class ProcurementStoreoutController extends BaseController {
    /**
     * 获取采购缺货数据列表
     */
    public void getData() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        String orderBy = getPara("prop");
        boolean isASC = "ascending".equals(getPara("order"));
//        String[] createTimes = new String[2];
        String procurementId = getPara("procurement_id");
        String productName = getPara("product_name");
        String productStandardName = getPara("product_standard_name");
        String[] createTimes = getParaValues("format_create_time");
//        String createStartTime = getPara("createStartTime");
//        String createEndTime = getPara("createEndTime");
//        createTimes[0] = createStartTime;
//        createTimes[1] = createEndTime;
        List<Object> prarms = new ArrayList<>();
        String sql = _getSelect();
        String sqlExceptSelect = _getSqlExceptSelect(prarms, procurementId, productName, productStandardName, createTimes);
//        Page<ProcurementPlanDetail> procurementPlanDetailPage= ProcurementPlanDetail.dao.getProcuementStoreout(pageNum,pageSize,orderBy,isASC,procurementId,productName,productStandardName,createTimes2);
        renderJson(Db.paginate(pageNum, pageSize, sql, sqlExceptSelect.toString(), prarms.toArray()));
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
        List<Object> prarms = new ArrayList<>();
        String sql = _getSelect();
        String sqlExceptSelect = _getSqlExceptSelect(prarms, procurementId, productName, productStandardName, createTimes);
//        Page<ProcurementPlanDetail> procurementPlanDetailPage= ProcurementPlanDetail.dao.getProcuementStoreout(pageNum,pageSize,orderBy,isASC,procurementId,productName,productStandardName,createTimes2);
        List<Record> procurementPlanDetailPage = Db.find(sql + sqlExceptSelect.toString(), prarms.toArray());
        if (procurementPlanDetailPage != null && procurementPlanDetailPage.size() > 0) {
            List<String[]> lists = new ArrayList<>();
            for (int i = 0; i < procurementPlanDetailPage.size(); i++) {
                String[] strings = new String[15];
                Record procurementPlanDetail = procurementPlanDetailPage.get(i);
                BigDecimal productStandardNum = procurementPlanDetail.get("product_standard_num");
                BigDecimal inventoryNum = procurementPlanDetail.get("inventory_num");
                BigDecimal procurementNeedPrice = procurementPlanDetail.get("procurement_need_price");
                BigDecimal sell_price = procurementPlanDetail.get("sell_price");
                BigDecimal procurement_num = procurementPlanDetail.get("procurement_num");
                int qNum = productStandardNum.intValue() - procurement_num.intValue() - inventoryNum.intValue();

                strings[0] = procurementPlanDetail.get("product_name");
                strings[1] = procurementPlanDetail.get("product_standard_name");
                strings[2] = procurementPlanDetail.get("product_standard_id") + "";
                strings[3] = productStandardNum + "";
                strings[4] = inventoryNum + "";
                strings[5] = procurement_num + "";
                strings[6] = qNum > 0 ? qNum + "" : "不缺货";

                // 商品缺货率
                strings[7] = qNum > 0 ? new BigDecimal(qNum).divide(productStandardNum.subtract(inventoryNum),2,BigDecimal.ROUND_CEILING).multiply(new BigDecimal(100)) + "%" : "0";

                // 平台平均报价
                strings[8] = sell_price + "";
                // 缺货总额，临时按照平台报价来算
                strings[9] = qNum > 0 ? new BigDecimal(qNum).multiply(sell_price) + "" : "0";
                // 采购人
                strings[10] = procurementPlanDetail.get("procurement_name");
                // 订单详细数量
                strings[11] = procurementPlanDetail.get("orderDetailTotalNum") + "";
                // 订单详细缺货数量
                strings[12] = procurementPlanDetail.get("storeoutNum") + "";
                //订单详细缺货率
                strings[13] = "";
                try {
                    BigDecimal storeoutNum = new BigDecimal(procurementPlanDetail.get("storeoutNum") + "");
                    BigDecimal orderDetailTotalNum = new BigDecimal(procurementPlanDetail.get("orderDetailTotalNum") + "");
                    strings[13] = ((storeoutNum).divide(orderDetailTotalNum, 2, 2)).multiply(new BigDecimal(100)) + "%";
                } catch (Exception e) {
                    e.printStackTrace();
                    strings[13] = "";
                }
                lists.add(strings);
            }

            Integer uid = getSessionAttr(Constant.SESSION_UID);
            User user = User.dao.findById(uid);
            String[] headers = {
                    "商品名称",
                    "商品规格",
                    "规格编号",
                    "下单数量",
                    "历史累加库存数量",
                    "采购数量",
                    "采购取货数量",
                    "采购商品缺货率",
                    "平均平台报价",
                    "缺货总额",
                    "采购人",
                    "订单详细数量",
                    "订单详细缺货数量",
                    "订单缺货率"
            };
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
        List<User> userList = User.dao.getUserNickNameAndUId(RoleKeyCode.PROCUREMENT);
        renderJson(userList);
    }


    private String _getSqlExceptSelect(List<Object> params, String procurementId, String productName, String productStandardName, String[] createTimes) {
        StringBuffer sql = new StringBuffer();
        sql.append(" FROM b_procurement_plan_detail ppd  " +
                " INNER join b_procurement_plan pd on ppd.procurement_plan_id=pd.id  " +
                " INNER JOIN a_user u on u.id = ppd.procurement_id ");
        if (StrKit.notBlank(procurementId)) {
            sql.append("\tand ppd.procurement_id = ?\n");
            params.add(procurementId);
        }
        if (StrKit.notBlank(productName)) {
            sql.append("\tand ppd.product_name like ?\n");
            params.add("%" + productName + "%");
        }
        if (StrKit.notBlank(productStandardName)) {
            sql.append("\tand ppd.product_standard_name like ?\n");
            params.add("%" + productStandardName + "%");
        }
        if (createTimes != null && !"".equals(createTimes) && StrKit.notBlank(createTimes[0]) && StrKit.notBlank(createTimes[1]) && createTimes.length == 2) {
            sql.append("\tand pd.create_time between ? and ?\n");
            params.add(createTimes[0]);
            params.add(createTimes[1]);
        }

        sql.append("GROUP BY ppd.product_standard_id,ppd.procurement_id");
        return sql.toString();
    }

    private String _getSelect() {
        return "SELECT  " +
                "  ppd.product_name,  " +
                "  ppd.product_standard_name,  " +
                "  ppd.product_standard_id,  " +
                "  SUM( ppd.product_standard_num ) AS product_standard_num,  " +
                "  SUM( ppd.procurement_num ) AS procurement_num,  " +
                "  Avg( ppd.procurement_need_price ) AS procurement_need_price,  " +
                "  AVG( ppd.sell_price ) AS sell_price,  " +
                "  SUM( ppd.inventory_num ) AS inventory_num,  " +
                "  (select count(1) from b_order_detail od INNER JOIN b_order o on o.order_id  = od.order_id  where ppd.product_standard_id = od.product_standard_id and o.order_status != 50) as orderDetailTotalNum,  " +
                "  (select count(1) from b_order_detail od INNER JOIN b_order o on o.order_id  = od.order_id where (od.actual_send_goods_num <= 0 or od.actual_send_goods_num is NULL) and ppd.product_standard_id = od.product_standard_id and o.order_status != 50) as storeoutNum,    " +
                "  GROUP_CONCAT(distinct  u.nick_name) as procurement_name,  " +
                "  SUM(ppd.procurement_total_price) as procurement_total_price ";
    }
}
