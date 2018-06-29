package com.fruit.manage.controller.statement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.OrderDetail;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.ExcelException;
import com.jfinal.kit.StrKit;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * @author partner
 * @Description 4.产品毛利排行报表
 * @date 2018/6/2 13:56
 */
public class ProcurementMarginController extends BaseController {
    private List list = new ArrayList();

    public void getData() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        String orderBy = getPara("prop");
        boolean isASC = "ascending".equals(getPara("order"));
        Integer productId = getParaToInt("product_id");
        String productName = getPara("product_name");
        Integer productStandardId = getParaToInt("product_standard_id");
        String productStandardName = getPara("product_standard_name");
        String[] createTimes = getParaValues("order_cycle_date");
        String select = _getSqlSelect();
        String selectExcept = _getSqlSelectExcept(list, productId, productName, productStandardId, productStandardName, createTimes);
        System.out.println("-------------产品毛利排行报表SQL START--------------");
        System.out.println(select + selectExcept);
        for (int i = 0; i < list.size(); i++) {
            System.out.println("list index:" + i + ",list value:" + list.get(i));
        }
        System.out.println("-------------产品毛利排行报表SQL END--------------");
        renderJson(OrderDetail.dao.paginate(pageNum, pageSize, select, selectExcept, list.toArray()));
    }

    public void exportExcel() {
        Integer productId = getParaToInt("product_id");
        String productName = getPara("product_name");
        Integer productStandardId = getParaToInt("product_standard_id");
        String productStandardName = getPara("product_standard_name");
        String[] createTimes = getParaValues("order_cycle_date");
        String select = _getSqlSelect();
        String selectExcept = _getSqlSelectExcept(list, productId, productName, productStandardId, productStandardName, createTimes);
        System.out.println("-------------产品销售排行版报表SQL START--------------");
        System.out.println(select + selectExcept);
        for (int i = 0; i < list.size(); i++) {
            System.out.println("list index:" + i + ",list value:" + list.get(i));
        }
        System.out.println("-------------产品销售排行版报表SQL END--------------");


        // TODO 设置headers
        String[] headers = {
               "商品",
                "商品规格",
                "规格编号",
                " 采购人",
                " 产品销售均价",
                "产品成本均价",
                "产品平均利润",
                "实际发货总数",
                "产品总利润",
                " 实际发货平摊单价",
                "产品毛利"
        };

        List<OrderDetail> orderDetails = OrderDetail.dao.find(select + selectExcept, list.toArray());
        if (orderDetails != null && orderDetails.size() > 0) {
            List<String[]> lists = new ArrayList<>();
                for (OrderDetail orderDetail : orderDetails) {
                    String[] strings = new String[headers.length];
                    strings[0] = orderDetail.get("productName");
                    strings[1] = orderDetail.get("productStandardName");
                    strings[2] = orderDetail.get("productStandardId");
                    strings[3] = orderDetail.get("nick_name");
                    strings[4] = orderDetail.get("sell_price")+"";
                    strings[5] = orderDetail.get("costPrice")+"";
                    strings[6] = orderDetail.get("average_margin_price")+"";
                    strings[7] = orderDetail.get("totalActualNum")+"";
                    strings[8] = orderDetail.get("total_margin_price")+"";
                    strings[9] = orderDetail.get("avgActualSendGoosNum")+"";
                    strings[10] = orderDetail.get("margin")+"";
                    lists.add(strings);
                }

            Integer uid = getSessionAttr(Constant.SESSION_UID);
            User user = User.dao.findById(uid);
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".xlsx";
            String path = CommonController.FILE_PATH;
            String title = "产品利润报表";
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
                renderErrorText("导出库存统计错误!");
            }
        } else {
            renderErrorText("该库存统计没有记录!");
        }

    }


    private String _getSqlSelect() {
        String select =
//                "-- 商品 商品规格 规格编号  采购人   销售数量  成本价  销售总价  产品总利润  产品毛利 \n" +
                "SELECT  " +
                        "  GROUP_CONCAT(DISTINCT p.`name`) AS productName,  " +
                        "  GROUP_CONCAT(DISTINCT ps.`name`) AS productStandardName,  " +
                        "  GROUP_CONCAT(DISTINCT ps.id) AS productStandardId,  " +
                        "  auser.nick_name,  " +
                        "  AVG(ppd.sell_price) AS sell_price,  " +
                        "  AVG(pwPrice.put_average_price) AS costPrice,  " +
                        "  (  " +
                        "    AVG(ppd.sell_price) - AVG(pwPrice.put_average_price)  " +
                        "  ) AS average_margin_price,  " +
                        "  orderInfo.totalActualNum,  " +
                        "  (  " +
                        "    AVG(ppd.sell_price) - AVG(pwPrice.put_average_price)  " +
                        "  ) * orderInfo.totalActualNum AS total_margin_price,  " +
                        "  orderInfo.avgActualSendGoosNum,  " +
                        "  (  " +
                        "    (  " +
                        "      AVG(ppd.sell_price) - AVG(pwPrice.put_average_price)  " +
                        "    ) / (  " +
                        "      AVG(ppd.sell_price) + orderInfo.avgActualSendGoosNum  " +
                        "    ) * 100  " +
                        "  ) AS margin ";
        return select;
    }

    private String _getSqlSelectExcept(List list, Integer productId, String productName, Integer productStandardId, String productStandardName, String[] createTiems) {
        StringBuffer selectExcept = new StringBuffer();
        selectExcept.append("FROM  " +
                "  b_procurement_plan pp  " +
                "INNER JOIN b_procurement_plan_detail ppd ON ppd.procurement_plan_id = pp.id  " +
                "INNER JOIN b_product p ON p.id = ppd.product_id   " +
                "INNER JOIN b_product_standard ps ON ps.id = ppd.product_standard_id  " +
                "INNER JOIN a_user auser ON auser.id = ppd.procurement_id  " +
                "LEFT JOIN (  " +
                "  SELECT  " +
                "    pw.order_cycle_date,  " +
                "    pwd.product_standard_id,  " +
                "    pwd.put_average_price,  " +
                "    pwd.procurement_id  " +
                "  FROM  " +
                "    b_put_warehouse pw  " +
                "  INNER JOIN b_put_warehouse_detail pwd ON pwd.put_id = pw.id  " +
                ") AS pwPrice ON pwPrice.order_cycle_date = pp.order_cycle_date  " +
                "AND pwPrice.product_standard_id = ppd.product_standard_id   " +
                "AND pwPrice.procurement_id = ppd.procurement_id  " +
                "AND pwPrice.put_average_price <> 0  " +
                "LEFT JOIN (  " +
                "  SELECT  " +
                "    SUM(od.actual_send_goods_num) AS actualNum,  " +
                "    o.order_cycle_date,  " +
                "    od.product_standard_id,  " +
                "    ood.totalActualNum,  " +
                "    od.actual_send_goods_num,  " +
                "    SUM(  " +
                "      od.actual_send_goods_num / ood.totalActualNum * o.pay_logistics_money  " +
                "    ) AS avgActualSendGoosNum  " +
                "  FROM  " +
                "    b_order o  " +
                "  JOIN b_order_detail od ON od.order_id = o.order_id  " +
                "  JOIN (  " +
                "    SELECT  " +
                "      order_id,  " +
                "      SUM(actual_send_goods_num) AS totalActualNum  " +
                "    FROM  " +
                "      b_order_detail  " +
                "    GROUP BY  " +
                "      order_id  " +
                "  ) AS ood ON ood.order_id = od.order_id  " +
                "  GROUP BY  " +
                "    o.order_cycle_date,od.product_standard_id  " +
                ") AS orderInfo ON orderInfo.order_cycle_date = pp.order_cycle_date  " +
                "AND ppd.product_standard_id = orderInfo.product_standard_id ");

        selectExcept.append("WHERE  1 = 1 ");

        if (productId != null && productId > 0) {
            selectExcept.append("  and ppd.product_id like  ?  ");
            list.add("%" + productId + "%");
        }
        if (StrKit.notBlank(productName)) {

            selectExcept.append("  and ppd.product_name like ?  ");
            list.add("%" + productName + "%");
        }
        if (productStandardId != null && productStandardId > 0) {

            selectExcept.append("  and ppd.product_standard_id LIKE ?  ");
            list.add("%" + productStandardId + "%");
        }
        if (StrKit.notBlank(productStandardName)) {
            selectExcept.append("  and ppd.product_standard_name LIKE ?  ");
            list.add("%" + productStandardName + "%");
        }
        if (ArrayUtils.isNotEmpty(createTiems) && createTiems.length == 2) {
            selectExcept.append(" AND pp.order_cycle_date BETWEEN ? AND ? ");
            list.add(createTiems[0]);
            list.add(createTiems[1]);
        }

        selectExcept.append("GROUP BY  " +
                "  ppd.product_standard_id,  " +
                "  ppd.procurement_id  " +
                "ORDER BY  " +
                "  margin DESC ");
        return selectExcept.toString();
    }

}
