package com.fruit.manage.controller.statement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.OrderDetail;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.ExcelException;
import org.terracotta.statistics.Time;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 销售毛利报表
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

        renderJson(OrderDetail.dao.getSalesMarginList(pageNum, pageSize, groupStr, isASC, nick_name, business_name, order_cycle_date));
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
        List<OrderDetail> salesMarginList = OrderDetail.dao.getSalesMarginList(nick_name, business_name, order_cycle_date);
        ArrayList<Object[]> tableData = new ArrayList<>();
        salesMarginList.stream().forEach(
                orderDetail -> {
                    Object[] column = {
                            orderDetail.get("order_id"),
                            orderDetail.get("nick_name"),
                            orderDetail.get("business_name"),
                            orderDetail.get("pay_all_money"),
                            orderDetail.get("total_gross_margin"),
                            orderDetail.get("gross_margin"),
                    };
                    tableData.add(column);
                }
        );

        File file = null;
        fileName = Time.time() + "销售毛利报表.xlsx";
        try {
            String filePath = ExcelCommon.createExcelModul(CommonController.FILE_PATH, fileName, "销售毛利报表", "", headers, tableData);
            file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("BASE_PATH:" + CommonController.FILE_PATH + ",fileName:" + fileName + "  文件不存在");
            }
        } catch (ExcelException e) {
            e.printStackTrace();
        }
        renderFile(file);
    }

    /**
     * 获取汇总信息
     */
    public void getTotalInfo () {
        String nick_name = getPara("nick_name");
        String business_name = getPara("business_name");
        String order_cycle_date = getPara("order_cycle_date");
        List<OrderDetail> salesMarginList = OrderDetail.dao.getSalesMarginList(nick_name, business_name, order_cycle_date);

        BigDecimal total_gross_margin =new BigDecimal(0);
        BigDecimal gross_margin  =new BigDecimal(0);
        int orderCount = 0;

        for (OrderDetail orderDetail : salesMarginList) {
                // 订单总利润
                BigDecimal tgm = orderDetail.get("total_gross_margin");
                // 订单总毛利率
                BigDecimal gm = orderDetail.get("gross_margin");
                if (tgm!=null&&gm !=null) {
                    orderCount ++;
                    total_gross_margin = total_gross_margin.add(tgm);
                    gross_margin = gross_margin.add(gm);
                }
        }

        HashMap<Object, Object> map = new HashMap<>(2);
        map.put("total_gross_margin",total_gross_margin);
        map.put("gross_margin",gross_margin.divide(new BigDecimal(orderCount),5));
        renderJson(map);
    }

}
