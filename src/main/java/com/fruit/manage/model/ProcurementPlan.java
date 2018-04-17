package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseProcurementPlan;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.sun.tools.javac.util.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class ProcurementPlan extends BaseProcurementPlan<ProcurementPlan> {
    public static final ProcurementPlan dao = new ProcurementPlan().dao();

    /**
     * 获取所有的采购计划
     *
     * @param pageNum  当前第几页
     * @param pageSize 每页显示几行
     * @param orderBy  排序
     * @param isASC    是否升序
     * @param map      多条件查询参数
     * @return 返回一个带分页的数据集合
     */
    public Page<ProcurementPlan> getAllProcurementPlan(int pageNum, int pageSize, String orderBy, boolean isASC, Map map) {
        ArrayList<Object> params = new ArrayList<Object>();
        String selectStr = "select pp.id,pp.procurement_id,pp.product_standard_num,pp.num,pp.wait_statistics_order_total,pp.order_total,pp.create_time \n";
        StringBuilder sql = new StringBuilder();
        sql.append("\tfrom b_procurement_plan pp where 1=1 \n");
        if (org.apache.commons.lang3.ArrayUtils.isNotEmpty((String[]) map.get("createTime")) && ((String[]) map.get("createTime")).length == 2) {
            sql.append("and pp.create_time BETWEEN ? and ? ");
            String startDate = ((String[]) map.get("createTime"))[0] + " 00:00:00";
            String endDate = ((String[]) map.get("createTime"))[1] + " 23:59:59";
            params.add(startDate);
            params.add(endDate);
        }
        orderBy = StrKit.isBlank(orderBy) ? "pp.create_time" : orderBy;
        sql.append("order by " + orderBy + " " + (isASC ? "" : "desc "));
        return paginate(pageNum, pageSize, selectStr, sql.toString(), params.toArray());
    }

    // 获取未统计的商品总数
    public ProcurementPlan getWaitStatisticsOrderTotal(String[] createTimes, String createTime) {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(DISTINCT ol.order_id) AS wait_statistics_order_total, ");
        sql.append("'" + createTime + "'");
        sql.append(" as create_time ");
        sql.append("from b_order_log ol ");
        sql.append("where ol.is_statistical=0 ");
        sql.append("and ol.create_time BETWEEN ? and ? ");
        List<String> list = new ArrayList<>();
        list.add(createTimes[0]);
        list.add(createTimes[1]);
        return findFirst(sql.toString(), list.toArray());
    }

    public void updateOrderLog(String[] createTimes) {
        String sql = "update b_order_log ol set ol.is_statistical=1 where ol.create_time BETWEEN ? and ? ";
        Db.update(sql, createTimes[0], createTimes[1]);
    }

    /**
     * 根据采购计划ID获取要导出的采购数据
     *
     * @param createTime
     * @return
     */
    public List<ProcurementPlan> getExportDataByPPlanID(String[] createTime) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT " +
                "p.`name` AS productName, " +
                "pq.procurement_id, " +
                "pq.procurement_name, " +
                "ps.`name` AS productStandardName, " +
                "ps.id AS productStandardID, " +
                "ps.fruit_weight AS fruitWeight, " +
                "ps.sell_price AS sellPrice, " +
                "( " +
                "SELECT " +
                "sum(od2.num) " +
                "FROM " +
                "b_order_detail od2, " +
                "b_order o " +
                "WHERE " +
                "od2.order_id = o.order_id " +
                "AND od2.product_standard_id = ol.product_standard_id " +
                "GROUP BY " +
                "od2.product_standard_id " +
                ") AS purchaseNum, " +
                "ps.stock AS inventoryNum, " +
                "(SELECT 0) AS procurementNum, " +
                "(select 0) as procurementPrice, " +
                "(select '') as procurementRemark, " +
                "p.id as productId, " +
                "( " +
                "SELECT " +
                "SUM(ol2.change_num) " +
                "from " +
                "b_order_log ol2 " +
                "where " +
                "ol2.product_standard_id = ol.product_standard_id " +
                ") as productStandardNum, " +
                "(SELECT 0) as procurementNeedPrice, " +
                "(select 0) as procurementTotalPrice, " +
                "(select '') as orderRemark " +
                "from " +
                "b_order_log ol, " +
                "b_product p, " +
                "b_product_standard ps, " +
                "b_procurement_quota pq " +
                "where " +
                "1 = 1 " +
                "and pq.product_standard_id = ol.product_standard_id " +
                "and ol.product_id = p.id " +
                "and ol.product_standard_id = ps.id " +
                // 订单采购订单导出计划
//                "and ol.is_statistical = 0 " +
                "and ol.create_time BETWEEN ? " +
                "and ? " +
                "GROUP BY " +
                "ol.product_standard_id " +
                "HAVING " +
                "purchaseNum > 0 " +
                "order by " +
                "purchaseNum desc, " +
                "ps.sell_price desc");
//		sql.append("select p.`name` as productName, ");
//		sql.append("pq.procurement_id, ");
//		sql.append("pq.procurement_name, ");
//		sql.append("ps.`name` as productStandardName, ");
//		sql.append("ps.id as productStandardID, ");
//		sql.append("ps.fruit_weight as fruitWeight, ");
//		sql.append("ps.sell_price as sellPrice,( ");
//		sql.append("SELECT sum(od2.num) from b_order_detail od2,b_order o ");
//		sql.append("where od2.order_id=o.order_id ");
//		sql.append("and od2.product_standard_id=ol.product_standard_id ");
//		sql.append("group by od2.product_standard_id ");
//		sql.append(") as purchaseNum, ");
//		sql.append("(select 0) as inventoryNum, ");
//		sql.append("(select 0) as procurementNum, ");
//		sql.append("(select 0) as procurementPrice, ");
//		sql.append("(select '') as procurementRemark, ");
//		sql.append("p.id as productId, ");
//		sql.append("(SELECT SUM(ol2.change_num) from b_order_log ol2 where ol2.product_standard_id=ol.product_standard_id) as productStandardNum, ");
//		sql.append("(SELECT 0) as procurementNeedPrice, ");
//		sql.append("(select 0) as procurementTotalPrice, ");
//		sql.append("(select '') as orderRemark ");
//		sql.append("from b_order_log ol,b_product p,");
//		sql.append("b_product_standard ps,b_procurement_quota pq ");
//		sql.append("where 1=1 ");
//		sql.append("and pq.product_standard_id = ol.product_standard_id ");
//		sql.append("and ol.product_id=p.id ");
//		sql.append("and ol.product_standard_id=ps.id ");
//		sql.append("and ol.create_time BETWEEN ? and ? ");
//		sql.append("GROUP BY ol.product_standard_id ");
//		sql.append("HAVING purchaseNum > 0 ");
//		// 按采购量和售价降序排序
//		sql.append("order by purchaseNum desc,ps.sell_price desc ");
        List<String> list = new ArrayList<>();
        list.add(createTime[0]);
        list.add(createTime[1]);
        return find(sql.toString(), list.toArray());
    }

    /**
     * 获取采购计划要添加的参数
     *
     * @param createTime 开始时间和结束时间
     * @return 返回一个采购计划的数据
     */
    public ProcurementPlan getPPlan(String[] createTime) {
        ArrayList<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("select (count(DISTINCT ol.product_standard_id)) as product_standard_num, ");
        sql.append("SUM(ol.change_num) as num, ");
        sql.append("sum(0) as wait_statistics_order_total, ");
        sql.append("(count(DISTINCT ol.order_id)) as order_total ");
        sql.append("from b_order_log ol ");
        sql.append("where 1=1 ");
        // sql.append("and ol.is_statistical = 0 ");
        if (org.apache.commons.lang3.ArrayUtils.isNotEmpty(createTime) && createTime.length == 2) {
            sql.append("and ol.create_time BETWEEN ? and ? ");
            params.add(createTime[0]);
            params.add(createTime[1]);
        }
        return findFirst(sql.toString(), params.toArray());
    }

    public ProcurementPlan getPPlanCreateTime(String createTime) {
        String sql = "select pp.id,pp.procurement_id,pp.product_standard_num,pp.num,pp.wait_statistics_order_total,pp.order_total,pp.create_time " +
                " from b_procurement_plan pp where 1=1 and pp.create_time = ? ";
        return findFirst(sql, createTime);
    }

    /**
     * 根据采购计划编号获取采购计划信息
     * @param pPId 采购计划编号
     * @return 采购计划信息
     */
    public ProcurementPlan getPPlanById(String pPId) {
        String sql = "SELECT * from b_procurement_plan where id = ? ";
        return findFirst(sql, pPId);
    }
}
