package com.fruit.manage.model;

import com.fruit.manage.model.base.BasePutWarehouseDetail;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class PutWarehouseDetail extends BasePutWarehouseDetail<PutWarehouseDetail> {
    public static final PutWarehouseDetail dao = new PutWarehouseDetail().dao();

    /**
     * 根据条件进行分页查询
     *
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @param isASC
     * @param map
     * @return
     */
    public Page<PutWarehouseDetail> getAllInfo(Integer pageNum, Integer pageSize, String orderBy, boolean isASC, Map map) {
        ArrayList<Object> params = new ArrayList<Object>();
        String selectStr = "select pwd.id,pwd.product_name,pwd.procurement_id,pwd.product_id,pwd.product_standard_name, " +
                "pwd.product_standard_id,pwd.product_weight,pwd.procurement_price,pwd.procurement_total_price, " +
                "pwd.booth_cost,pwd.put_num,pwd.put_average_price,pwd.procurement_name,pwd.put_remark, " +
                "pwd.create_time,pwd.update_time,pwd.put_id ";
        StringBuilder sql = new StringBuilder();
        sql.append("from b_put_warehouse_detail pwd ");
        sql.append("where 1=1 ");
        if (StrKit.notBlank((String) map.get("putId"))) {
            sql.append("and pwd.put_id like ? ");
            params.add("%" + (String) map.get("putId") + "%");
        }
        if (StrKit.notBlank((String) map.get("productName"))) {
            sql.append("and pwd.product_name like ? ");
            params.add("%" + map.get("productName") + "%");
        }
        if (StrKit.notBlank((String) map.get("productId"))) {
            sql.append("and pwd.product_id like ? ");
            params.add("%" + map.get("productId") + "%");
        }
        if (StrKit.notBlank((String) map.get("productStandardName"))) {
            sql.append("and pwd.product_standard_name like ? ");
            params.add("%" + map.get("productStandardName") + "%");
        }
        if (StrKit.notBlank((String) map.get("productStandardId"))) {
            sql.append("and pwd.product_standard_id like ? ");
            params.add("%" + map.get("productStandardId") + "%");
        }
        orderBy = StrKit.isBlank(orderBy) ? "ppd.create_time" : orderBy;
        sql.append("order by " + orderBy + " " + (isASC ? "" : "desc "));
        return paginate(pageNum, pageSize, selectStr, sql.toString(), params.toArray());
    }

    /**
     * 根据入库编号删除入库单所有详细信息
     *
     * @param putId 入库单编号
     * @return 返回是否删除成功
     */
    public boolean delWarehousePutDetailByWPId(Integer putId) {
        String sql = "delete from b_put_warehouse_detail where put_id=?";
        if (Db.update(sql, putId) > 0) {
            return true;
        }
        return false;
    }

    /**
     * 根据入库单编号获取入库单详细信息数据的集合
     *
     * @param putId
     * @return
     */
    public List<PutWarehouseDetail> getAllInfoByPutId(Integer putId) {
        String sql = "SELECT * from b_put_warehouse_detail pwd where pwd.put_id = ?";
        return find(sql, putId);
    }

    /**
     * 根据ID获取入库详细表
     *
     * @param id 仓库详细编号
     * @return
     */
    public PutWarehouseDetail getPutDetailById(Integer id) {
        String sql = "select * from b_put_warehouse_detail where id = ?";
        return findFirst(sql, id);
    }

    /**
     * 根据规格编号和采购编号获取发货信息
     *
     * @param psId
     * @param procurementId
     * @return
     */
    public PutWarehouseDetail getPutDetailByPSIDAndProcurementId(Integer psId, Integer procurementId, String startTime, String endTime, Integer putId) {
        StringBuilder sql = new StringBuilder();
        sql.append("select pwd.id,pwd.product_name,pwd.procurement_id,pwd.product_id,pwd.product_standard_name, ");
        sql.append("pwd.product_standard_id,pwd.product_weight,pwd.procurement_price,pwd.procurement_total_price, ");
        sql.append("pwd.booth_cost,pwd.booth_cost,pwd.put_num,pwd.put_average_price,pwd.procurement_name, ");
        sql.append("pwd.put_remark,pwd.create_time,pwd.update_time,pwd.put_id ");
        sql.append("from b_put_warehouse_detail pwd  ");
        sql.append("where pwd.product_standard_id= ? and pwd.procurement_id = ?  and pwd.put_id = ? ");
        // sql.append("and pwd.create_time BETWEEN ? and ? ");
        return findFirst(sql.toString(), psId, procurementId, putId);
    }

    /**
     * 根据putId获取总共有几种商品规格
     *
     * @param putId
     * @return
     */
    public Integer getAllTypeCountByPutId(Integer putId) {
        String sql = "SELECT sum(temp.typeCount) as allTypeCount from ( " +
                "SELECT COUNT(pwd.product_standard_id) as typeCount,pwd.product_standard_id from b_put_warehouse_detail pwd " +
                "where 1=1 " +
                "group by pwd.product_standard_id and pwd.put_id = ? " +
                ") temp";
        return Db.queryInt(sql, putId);
    }

    /**
     * 根据规格编号和时间获取入库单价
     *
     * @param psId
     * @param startTime
     * @param endTime
     * @return
     */
    public BigDecimal getAveragePriceByPsIdAndTime(Integer psId, String startTime, String endTime) {
        String sql = "SELECT pwd.put_average_price " +
                "from b_put_warehouse_detail pwd " +
                "WHERE 1=1 " +
                "and pwd.product_standard_id = ? " +
                "and pwd.create_time BETWEEN ? and ? ";
        return Db.queryBigDecimal(sql, psId, startTime, endTime);
    }


    /**
     * 根据规格编号和时间获取入库单价
     *
     * @param psId
     * @param orderCycleDate
     * @return
     */
    public BigDecimal getAveragePriceByPsIdAndTime(Integer psId, String orderCycleDate) {
        String sql = "SELECT " +
                "  pwd.put_average_price  " +
                "FROM  " +
                "  b_put_warehouse pw  " +
                "JOIN b_put_warehouse_detail pwd ON pwd.put_id = pw.id  " +
                "WHERE  " +
                "  1 = 1  " +
                "AND pw.order_cycle_date = ?  " +
                "AND pwd.product_standard_id = ? ";
        return Db.queryBigDecimal(sql, orderCycleDate, psId);
    }
}
