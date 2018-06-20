package com.fruit.manage.controller.statement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.OrderStatusCode;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.OrderDetail;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.ExcelException;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang3.StringUtils;
import org.terracotta.statistics.Time;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 1.销售毛利报表
 *
 * @Author: ZGC
 * @Date Created in 18:26 2018/5/25
 */
public class SalesMarginController extends BaseController {

    private String fileName;

    /**
     * 获取数据
     */
    public void getData() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        String groupStr = getPara("groupStr");
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("orderBy"));

        String nick_name = getPara("nick_name");
        String business_name = getPara("business_name");
        String order_cycle_date = getPara("order_cycle_date");
        String select = _getSelect();
        List<Object> params = new ArrayList<>();
        String sqlExceptSelect = _getSqlExceptSelect(params, nick_name, business_name, order_cycle_date);

        renderJson(Db.paginate(pageNum, pageSize, select, sqlExceptSelect, params.toArray()));
    }

    /**
     * 导出Excel
     */
    public void exportExcel() {
        String nick_name = getPara("nick_name");
        String business_name = getPara("business_name");
        String order_cycle_date = getPara("order_cycle_date");
        String[] headers = {
                "订单号",
                "销售名称",
                "商户名称",
                "订单总货款",
                "利润总额",
                "毛利率"
        };
        String select = _getSelect();
        List<Object> params = new ArrayList<>();
        String sqlExceptSelect = _getSqlExceptSelect(params, nick_name, business_name, order_cycle_date);
        List<Record> salesMarginList = Db.find(select + sqlExceptSelect, params.toArray());
        ArrayList<Object[]> tableData = new ArrayList<>();
        salesMarginList.stream().forEach(
                record -> {
                    Object[] column = {
                            record.get("order_id"),
                            record.get("nick_name"),
                            record.get("business_name"),
                            record.get("pay_all_money"),
                            record.get("total_gross_margin"),
                            record.get("gross_margin"),
                    };
                    tableData.add(column);
                }
        );

        File file = null;
        fileName = "销售毛利报表" + Time.time() + ".xlsx";
        try {
            String filePath = ExcelCommon.createExcelModul(CommonController.FILE_PATH, fileName, "销售毛利报表", "", headers, tableData);
            file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("BASE_PATH:" + CommonController.FILE_PATH + ",fileName:" + fileName + "  文件不存在");
            }
            List<String> list = new ArrayList<>(1);
            list.add(fileName);
            renderJson(list);
        } catch (ExcelException e) {
            e.printStackTrace();
            renderErrorText("导出销售毛利报表失败！");
        }
//        renderFile(file);
    }

    /**
     * 获取汇总信息
     */
    public void getTotalInfo() {
        // 临时获取

        String nick_name = getPara("nick_name");
        String business_name = getPara("business_name");
        String order_cycle_date = getPara("order_cycle_date");
        String select = _getSelect();
        List<Object> params = new ArrayList<>();
        String sqlExceptSelect = _getSqlExceptSelect(params, nick_name, business_name, order_cycle_date);
        List<Record> salesMarginList = Db.find(select + sqlExceptSelect, params.toArray());

        BigDecimal total_gross_margin = new BigDecimal(0);
        BigDecimal gross_margin = new BigDecimal(0);
        int orderCount = 0;

        for (Record record : salesMarginList) {
            // 订单总利润
            BigDecimal tgm = record.get("total_gross_margin");
            // 订单总毛利率
            BigDecimal gm = record.get("gross_margin");
            if (tgm != null && gm != null) {
                orderCount++;
                total_gross_margin = total_gross_margin.add(tgm);
                gross_margin = gross_margin.add(gm);
            }
        }

        HashMap<Object, Object> map = new HashMap<>(2);
        map.put("total_gross_margin", total_gross_margin);
        map.put("gross_margin", gross_margin.divide(new BigDecimal(orderCount), 5));
        renderJson(map);
    }

    private String _getSqlExceptSelect(List<Object> params, String nick_name, String business_name, String order_cycle_date) {
        StringBuffer sql = new StringBuffer();
        sql.append(" FROM  " +
                "  b_order AS o  " +
                "INNER JOIN b_order_detail AS od ON o.order_id = od.order_id  " +
                "INNER JOIN b_business_user AS bu ON o.u_id = bu.id  " +
                "INNER JOIN a_user AS au ON bu.a_user_sales_id = au.id  " +
                "INNER JOIN b_business_info AS bi ON bi.u_id = bu.id  " +
                "LEFT JOIN b_check_inventory AS ci ON ci.order_cycle_date  = o.order_cycle_date   " +
                "LEFT JOIN b_check_inventory_detail cid ON cid.check_inventory_id = ci.id AND cid.product_standard_id = od.product_standard_id  " +
                "LEFT JOIN b_put_warehouse as pw ON pw.order_cycle_date = o.order_cycle_date  " +
                "LEFT JOIN b_put_warehouse_detail pwd ON pwd.put_id = pw.id AND pwd.product_standard_id = od.product_standard_id  " +
                "WHERE 1 = 1 " +
                "AND  o.order_status != " + OrderStatusCode.DELETED.getStatus() + " ");
        if (StringUtils.isNotBlank(nick_name)) {
            sql.append("AND au.nick_name = ? ");
            params.add(nick_name);
        }

        if (StringUtils.isNotBlank(business_name)) {
            sql.append("AND bi.business_name = ? ");
            params.add(business_name);
        }

        sql.append("AND ifnull(cid.inventory_price, pwd.put_average_price) GROUP BY o.order_id ORDER BY o.order_id DESC");
        return sql.toString();
    }

    private String _getSelect() {
        return "SELECT  " +
                "  o.order_id,  " +
                "  au.nick_name,  " +
                "  bi.business_name,  " +
                "  o.pay_all_money,  " +
                "  SUM(  " +
                "    (  " +
                "      od.sell_price - ifnull(cid.inventory_price, pwd.put_average_price)  " +
                "    ) * od.actual_send_goods_num  " +
                "  ) AS total_gross_margin,  " +
                "  SUM(  " +
                "    (  " +
                "      od.sell_price - ifnull(cid.inventory_price, pwd.put_average_price)  " +
                "    ) * od.actual_send_goods_num  " +
                "  ) / o.pay_all_money * 100 AS gross_margin ";
    }

}
