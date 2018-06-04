package com.fruit.manage.controller.statement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.OrderDetail;
import com.fruit.manage.model.Product;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.DateAndStringFormat;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.Excel;
import com.fruit.manage.util.excel.ExcelException;
import com.fruit.manage.util.excel.ExcelRow;
import com.jfinal.kit.StrKit;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Ref;
import java.util.*;

/**
 * @Description 3.产品销售排行版
 * @author partner
 * @date 2018/5/31 17:37
 */
public class ProductSaleRankListController extends BaseController {
    private List list = new ArrayList();

    /**
     * 根据查询条件进行分页查询数据
     */
    public void getData() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        String orderBy = getPara("prop");
        boolean isASC = "ascending".equals(getPara("order"));
        Integer productId = getParaToInt("product_id");
        String productName = getPara("product_name");
        Integer productStandardId = getParaToInt("product_standard_id");
        String productStandardName = getPara("product_standard_name");
        String[] createTimes = getParaValues("format_create_time");
        String select = getSqlSelect();
        String selectExcept = getSqlSelectExcept(list, productId, productName, productStandardId, productStandardName,createTimes);
        System.out.println("-------------产品销售排行版报表SQL START--------------");
        System.out.println(select + selectExcept);
        for (int i = 0; i < list.size(); i++) {
            System.out.println("list index:"+i+",list value:"+list.get(i));
        }
        System.out.println("-------------产品销售排行版报表SQL END--------------");
        renderJson(OrderDetail.dao.paginate(pageNum,pageSize,select,selectExcept,list.toArray()));
    }


    /**
     * 导出商品销售排行报表
     */
    public void exportPSRankListExcel() {
        Integer productId = getParaToInt("product_id");
        String productName = getPara("product_name");
        Integer productStandardId = getParaToInt("product_standard_id");
        String productStandardName = getPara("product_standard_name");
        String[] createTimes = getParaValues("format_create_time");
        String select = getSqlSelect();
        String selectExcept = getSqlSelectExcept(list, productId, productName, productStandardId, productStandardName,createTimes);
        System.out.println("-------------产品销售排行版报表SQL START--------------");
        System.out.println(select + selectExcept);
        for (int i = 0; i < list.size(); i++) {
            System.out.println("list index:"+i+",list value:"+list.get(i));
        }
        System.out.println("-------------产品销售排行版报表SQL END--------------");
        List<OrderDetail> orderDetails = OrderDetail.dao.find(select + selectExcept, list.toArray());
        Map<Integer, Integer> mapCount = new HashMap<>();
        if (orderDetails != null && orderDetails.size() >0) {

            Integer uid = getSessionAttr(Constant.SESSION_UID);
            User user = User.dao.findById(uid);
            String[] headers = {"商品名称","规格名称","规格编号","商品规格数量","商品数量","平均报价","总销量金额","平均毛利"};
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".xlsx";
            String path = CommonController.FILE_PATH;
            String title = "商品销售排行报表";
//            Map map = new HashMap();
//            map.put("path", path);
//            map.put("fileName", fileName);
//            map.put("title", title);
//            map.put("createBy", user.getNickName());
//            map.put("header", headers);
//            map.put("listData", lists);
            //判断文件夹是否存在
            if (!new File(path).exists() || !new File(path).isDirectory()) {
                new File(path).mkdirs();
            }
            String savePath = path + File.separator + fileName;

            Excel excel = new Excel();
            if (StrKit.notBlank(title)) {
                excel.setTitle(title);
            }
            if (!StrKit.notBlank(savePath)) {
                renderErrorText("保存路径不能为空");
            } else {
                excel.setSavePath(savePath);
            }
            if (StrKit.notBlank(user.getNickName())) {
                excel.setCreateBy(user.getNickName());
            }
            if (StrKit.notBlank(headers)) {
                excel.setHeader(headers);
            }
            // 循环获取要合并的行数
            for (int i = 0; i < orderDetails.size(); i++) {
                OrderDetail orderDetail = orderDetails.get(i);
                Integer productIdCount = Integer.parseInt(orderDetail.get("productId") + "");
                if (mapCount.containsKey(productIdCount)) {
                    Integer mapValue = mapCount.get(productIdCount);
                    mapCount.put(productIdCount, mapValue + 1);
                }else {
                    mapCount.put(productIdCount, 1);
                }
            }
            for (int i = 0; i < orderDetails.size(); i++) {
                String[] strings = new String[7];
                OrderDetail orderDetail = orderDetails.get(i);
//                strings[0] = orderDetail.get("productName")+"";
//                strings[1] = orderDetail.get("productStandardName")+"";
//                strings[2] = orderDetail.get("productStandardId") + "";
//                strings[3] = orderDetail.get("productStandardNum")+"";
//                strings[4] = orderDetail.get("avgSellPrice") + "";
//
//                strings[5] = orderDetail.get("deliverNumTotalPrice") + "";
//                strings[6] = "";
                ExcelRow row=excel.createRow();
                row.addCell(orderDetail.get("productName")+"");
                row.addCell(orderDetail.get("productStandardName")+"");
                row.addCell(orderDetail.get("productStandardId") + "");
                row.addCell(orderDetail.get("productStandardNum")+"");
                if (mapCount.get(orderDetail.get("productId"))!=null && mapCount.get(orderDetail.get("productId"))>0) {
                    row.addCell(orderDetail.get("productNum")+"",1,mapCount.get(orderDetail.get("productId")));
                    mapCount.remove(orderDetail.get("productId"));
                }
                row.addCell(orderDetail.get("avgSellPrice") + "");
                row.addCell(orderDetail.get("deliverNumTotalPrice") + "");
                row.addCell(orderDetail.get("grossProfitRate") ==null || "".equals(orderDetail.get("grossProfitRate")) ? "" :orderDetail.get("grossProfitRate")+"%");

            }
//            for (Object[] dataRow : lists) {
//                ExcelRow row = excel.createRow();
//                for (Object dataCell : dataRow) {
//                    row.addCell(dataCell);
//                }
//            }
            try {
                 excel.CreateXlsx();
                List<String> list = new ArrayList<>();
                list.add(fileName);
                renderJson(list);
            } catch (Exception e) {
                renderErrorText("生成报表失败！");
            }
//            try {
//                ExcelCommon.createExcelModul(map);
//                List<String> list = new ArrayList<>();
//                list.add(fileName);
//                renderJson(list);
//            } catch (ExcelException e) {
//                e.printStackTrace();
//                renderErrorText("导出商品销售排行错误!");
//            }
        } else {
            renderErrorText("该商品销售排行没有记录!");
        }
    }

    /**
     * 获取要查询的sql列表
     * @return
     */
    private String getSqlSelect() {
        String sql = "SELECT  od.product_name as productName,od.product_standard_name as productStandardName,od.product_standard_id as productStandardId,od.product_id as productId,\n" +
                "\tsum(od.num) as productStandardNum  ,(SELECT SUM(od2.num) from b_order_detail od2 where 1=1 and od2.product_id = od.product_id ) as productNum,od.sell_price,\n" +
                "\tFORMAT((SUM(od.sell_price*od.num)/sum(od.num)),2) as avgSellPrice,(od.sell_price*IFNULL(od.actual_deliver_num,0))as deliverNumTotalPrice,od.actual_deliver_num,\tod.create_time as createTime,\n" +
                "\tFORMAT(avg(( od.sell_price - ifnull(cid.inventory_price, pwd.put_average_price) ) * od.actual_send_goods_num / (od.sell_price * od.actual_send_goods_num) * 100),2) as  grossProfitRate\n";


        return sql;
    }

    /**
     * 获取要查询的sql条件
     * @param list
     * @param productId
     * @param productName
     * @param productStandardId
     * @param productStandardName
     * @param createTiems
     * @return
     */
    private String getSqlSelectExcept(List list, Integer productId, String productName, Integer productStandardId, String productStandardName,String [] createTiems) {
        String sql = "\tfrom b_order o\n" +
                "\tINNER JOIN b_order_detail od on o.order_id = od.order_id\n" +
                "\tINNER JOIN b_product p on p.id = od.product_id\n" +
                "\tINNER JOIN b_product_standard ps on p.id = ps.product_id and ps.id = od.product_standard_id\n" +

                "LEFT  JOIN b_check_inventory AS ci ON ci.create_time LIKE CONCAT(\n" +
                "\t(\n" +
                "\t\tCASE\n" +
                "\t\tWHEN timediff(\n" +
                "\t\t\tTIME(o.create_time),\n" +
                "\t\t\tstr_to_date('12:00:00', '%H:%i:%s')\n" +
                "\t\t) < 0 THEN\n" +
                "\t\t\tDATE(o.create_time)\n" +
                "\t\tELSE\n" +
                "\t\t\tdate_add(\n" +
                "\t\t\t\tDATE(o.create_time),\n" +
                "\t\t\t\tINTERVAL 1 DAY\n" +
                "\t\t\t)\n" +
                "\t\tEND\n" +
                "\t),\n" +
                "\t'%'\n" +
                ")\n" +
                "LEFT JOIN b_check_inventory_detail cid ON cid.check_inventory_id = ci.id\n" +
                "LEFT JOIN b_put_warehouse as pw ON pw.create_time LIKE CONCAT(\n" +
                "\tCASE\n" +
                "\tWHEN timediff(\n" +
                "\t\tTIME(o.create_time),\n" +
                "\t\tstr_to_date('12:00:00', '%H:%i:%s')\n" +
                "\t) < 0 THEN\n" +
                "\t\tDATE(o.create_time)\n" +
                "\tELSE\n" +
                "\t\tdate_add(\n" +
                "\t\t\tDATE(o.create_time),\n" +
                "\t\t\tINTERVAL 1 DAY\n" +
                "\t\t)\n" +
                "\tEND,\n" +
                "\t'%'\n" +
                ")\n" +
                "LEFT JOIN b_put_warehouse_detail pwd ON pwd.put_id = pw.id AND pwd.product_standard_id = od.product_standard_id"+


                "\twhere 1=1\n";
        if (productId != null && productId > 0) {
            sql += "\tand od.product_id like  ?\n";
            list.add("%" + productId + "%");
        }
        if (StrKit.notBlank(productName)) {

            sql += "\tand od.product_name like ?\n";
            list.add("%" + productName + "%");
        }
        if (productStandardId != null && productStandardId > 0) {

            sql += "\tand od.product_standard_id LIKE ?\n";
            list.add("%" + productStandardId + "%");
        }
        if (StrKit.notBlank(productStandardName)) {
            sql += "\tand od.product_standard_name LIKE ?\n";
            list.add("%" + productStandardName + "%");
        }
        if (ArrayUtils.isNotEmpty(createTiems) && createTiems.length == 2) {
            sql += " AND od.create_time BETWEEN ? AND ? ";
            list.add(createTiems[0]);
            list.add(createTiems[1]);
        }
        sql += "\tgroup by od.product_standard_id\n" +
                "\tORDER BY productNum DESC,productId DESC,productStandardNum DESC";
        return sql;
    }
}
