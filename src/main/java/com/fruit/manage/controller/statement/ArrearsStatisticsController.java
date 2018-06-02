package com.fruit.manage.controller.statement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.Excel;
import com.fruit.manage.util.excel.ExcelException;
import com.fruit.manage.util.excel.ExcelRow;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.terracotta.statistics.Time;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户欠款统计报表
 *
 * @Author: ZGC
 * @Date Created in 16:32 2018/6/1
 */
public class ArrearsStatisticsController extends BaseController {
    /**
     * 获取客户欠款统计详细表
     */
    public void getData() {
        // 分页参数
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        String groupStr = getPara("groupStr");
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("orderBy"));

        // 数据参数
        String nick_name = getPara("nick_name");
        String business_name = getPara("business_name");
        String[] order_cycle_times = getParaValues("order_cycle_time");

        String select = _getSelect();
        StringBuilder sqlExceptSelect = new StringBuilder();
        sqlExceptSelect.append(_getSqlExceptSelect());


//        String nick_name = getPara("nick_name");
        List<BigDecimal> bigDecimalsArrearage = new ArrayList<>();
        List<BigDecimal> bigDecimalsArrearageTotal = new ArrayList<>();
        Map map = new HashMap<>();
        Page<Record> paginate = Db.paginate(pageNum, pageSize, select, sqlExceptSelect.toString());
        paginate.getList().stream().forEach(record -> {
                    bigDecimalsArrearageTotal.add(record.getBigDecimal("arrearage_total"));
                    bigDecimalsArrearage.add(record.getBigDecimal("arrearage"));
                }
        );
        BigDecimal arrearageCount = new BigDecimal(0);
        BigDecimal arrearageTotalCount  = new BigDecimal(0);
        for (int i = 0; i < bigDecimalsArrearage.size(); i++) {
            arrearageCount = arrearageCount.add(bigDecimalsArrearage.get(i));
            arrearageTotalCount = arrearageTotalCount.add(bigDecimalsArrearageTotal.get(i));
        }
        map.put("pageData", paginate);
        map.put("arrearageCount",arrearageCount);
        map.put("arrearageTotalCount", arrearageTotalCount);
        renderJson(map);
    }

    /**
     * 导出客户欠款统计详细excel表
     */
    public void exportExcel() {
        String select = _getSelect();


        File file = null;
        StringBuilder sqlExceptSelect = new StringBuilder();
        sqlExceptSelect.append(_getSqlExceptSelect());
        List<Record> records = Db.find(select + sqlExceptSelect);
        for (Record record : records) {
            String nick_name = record.getStr("nick_name");
            String business_name = record.getStr("business_name");
            String order_id = record.getStr("order_id");
            BigDecimal arrearage = record.getBigDecimal("arrearage");
            BigDecimal arrearage_total = record.getBigDecimal("arrearage_total");
        }

        ArrayList<Object[]> tableData = new ArrayList<>();

        String[] headers = {
                "商家名称",
                "订单编号",
                "销售名称",
                "订单欠款",
                "总欠款",
        };
        Object[] end = new Object[1];
        List<BigDecimal> bigDecimals = new ArrayList<>();
        BigDecimal totalCount = new BigDecimal(0);
        records.stream().forEach(
                record -> {
                    Object[] column = {
                            record.getStr("business_name"),
                            record.getStr("order_id"),
                            record.getStr("nick_name"),
                            record.getBigDecimal("arrearage"),
                            record.getBigDecimal("arrearage_total"),
                    };
                    bigDecimals.add(record.getBigDecimal("arrearage_total"));
                    tableData.add(column);
                }
        );
        for (BigDecimal bigDecimal : bigDecimals) {
            totalCount = totalCount.add(bigDecimal);
        }
        String fileName = Time.time() + "客户欠款统计报表.xlsx";

        if (!new File(CommonController.FILE_PATH).exists() || !new File(CommonController.FILE_PATH).isDirectory()) {
            new File(CommonController.FILE_PATH).mkdirs();
        }
        String savePath = CommonController.FILE_PATH + File.separator + fileName;

        Excel excel = new Excel();
        excel.setTitle("客户欠款统计报表");
        if (!StrKit.notBlank(savePath)) {
            renderErrorText("保存路径不能为空");
        } else {
            excel.setSavePath(savePath);
        }
        excel.setCreateBy("");
        if (StrKit.notBlank(headers)) {
            excel.setHeader(headers);
        }
        for (Object[] dataRow : tableData) {
            ExcelRow row = excel.createRow();
            for (Object dataCell : dataRow) {
                row.addCell(dataCell);
            }
        }
        ExcelRow row = excel.createRow();
        row.addCell("总共总欠款：" + totalCount.doubleValue(), false);
        try {
            String excelFileUrl = excel.CreateXlsx();
            file = new File(excelFileUrl);
            if (!file.exists()) {
                throw new RuntimeException("BASE_PATH:" + CommonController.FILE_PATH + ",fileName:" + fileName + "  文件不存在 ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        renderFile(file);

        //        try {
//            String excelFileUrl = ExcelCommon.createExcelModul(CommonController.FILE_PATH, fileName, "客户欠款统计报表", "", headers, tableData);
//            file = new File(excelFileUrl);
//            if (!file.exists()) {
//                throw new RuntimeException("BASE_PATH:" + CommonController.FILE_PATH + ",fileName:" + fileName + "  文件不存在 ");
//            }
//        } catch (ExcelException e) {
//            e.printStackTrace();
//        }

    }

    private String _getSqlExceptSelect() {
        return "FROM  " +
                "  b_order AS o  " +
                "INNER JOIN b_business_user AS bu ON o.u_id = bu.id  " +
                "INNER JOIN a_user ON bu.a_user_sales_id = a_user.id  " +
                "INNER JOIN b_business_info ON bu.id = b_business_info.u_id  " +
                "INNER JOIN (  " +
                "  SELECT  " +
                "    o.u_id,  " +
                "    SUM(  " +
                "      o.pay_all_money - o.pay_total_money  " +
                "    ) AS arrearage_total  " +
                "  FROM  " +
                "    b_order o  " +
                "  WHERE  " +
                "    o.pay_all_money > o.pay_total_money  " +
                "  GROUP BY  " +
                "    o.u_id  " +
                ") AS arrearage_orderId ON arrearage_orderId.u_id = bu.id  " +
                "WHERE  " +
                "  o.pay_all_money > o.pay_total_money  " +
                "ORDER BY  " +
                "  arrearage_orderId.arrearage_total DESC,  " +
                "  bu.`name`,  " +
                "  o.order_id DESC ";
    }

    private String _getSelect() {
        return "SELECT  " +
                "  a_user.nick_name,  " +
                "  b_business_info.business_name,  " +
                "  o.order_id,  " +
                "  o.pay_all_money - o.pay_total_money as arrearage,  " +
                "  arrearage_orderId.arrearage_total ";
    }

    /**
     * 获取汇总信息
     */
    public void getTotalInfo() {

    }


}
