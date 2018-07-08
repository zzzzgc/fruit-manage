package com.fruit.manage.model;

import com.fruit.manage.model.base.BasePutWarehouse;
import com.jfinal.ext.kit.DateKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class PutWarehouse extends BasePutWarehouse<PutWarehouse> {
	public static final PutWarehouse dao = new PutWarehouse().dao();

	/**
	 * 根据map传进来的时间数组对数据进行分页查询
	 * @param pageNum 当前第几页
	 * @param pageSize 每页显示几行
	 * @param orderBy 根据什么字段排序
	 * @param isASC 是否升序
	 * @param orderCycleDates 业务周期时间
	 * @return 返回一个带条件的分页数据集合
	 */
	public Page<PutWarehouse> getAllInfo(int pageNum, int pageSize, String orderBy, boolean isASC, String[] orderCycleDates){
		ArrayList<Object> params = new ArrayList<Object>();
		String selectStr = "select pw.id,pw.put_num,pw.put_type_num,pw.put_total_price,pw.put_type,pw.warehouse_address,pw.create_time,pw.put_time,pw.order_cycle_date ";
		StringBuilder sql=new StringBuilder();
		sql.append("from b_put_warehouse pw where 1=1 ");
		if (orderCycleDates != null && orderCycleDates.length == 2) {
			sql.append("and pw.order_cycle_date BETWEEN ? and ? ");
			params.add(orderCycleDates[0]);
			params.add(orderCycleDates[1]);
		}
		orderBy = StrKit.isBlank(orderBy) ? "pw.order_cycle_date" : orderBy;
		sql.append("order by " + orderBy + " " + (isASC ? "" : "desc "));
		return paginate(pageNum, pageSize, selectStr, sql.toString(), params.toArray());
	}

	/**
	 * 根据入库编号获取入库信息
	 * @param putId 入库编号
	 * @return 入库信息
	 */
	public PutWarehouse getPutWarehouseById(Integer putId) {
		String sql="select * from b_put_warehouse where id = ? ";
		return findFirst(sql, putId);
	}

	/**
	 * 根据订单周期时间获取入库信息
	 * @param orderCycleDate 入库时间
	 * @return 入库信息
	 */
	public PutWarehouse getPutWarehouseByOrderCycleDate(Date orderCycleDate) {
		String sql="select * from b_put_warehouse where order_cycle_date = ? ";
		return findFirst(sql, DateKit.toStr(orderCycleDate,"yyyy-MM-dd"));
	}


	/**
	 * 新增入库单
	 * @return
	 */
	public PutWarehouse addPutWarehouse(String warehouseAddress, Integer putType, Integer putTypeNum, Date putTime, Integer putNum, Date orderCycleDate, BigDecimal putTotalPrice) {
		PutWarehouse putWarehouse = new PutWarehouse();
		putWarehouse.setWarehouseAddress(warehouseAddress);
		putWarehouse.setOrderCycleDate(orderCycleDate);
		putWarehouse.setPutTotalPrice(putTotalPrice);
		putWarehouse.setPutType(putType);
		putWarehouse.setPutTime(putTime);
		putWarehouse.setPutTypeNum(putTypeNum);
		putWarehouse.setPutNum(putNum);
		putWarehouse.setCreateTime(new Date());
		putWarehouse.setUpdateTime(new Date());
		putWarehouse.save();
		return putWarehouse;
	}
}
