package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseProcurementPlan;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;

import java.util.Map;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class ProcurementPlan extends BaseProcurementPlan<ProcurementPlan> {
	public static final ProcurementPlan dao = new ProcurementPlan().dao();

	/**
	 * 获取所有的采购计划
	 * @param pageNum 当前第几页
	 * @param pageSize 每页显示几行
	 * @param orderBy 排序
	 * @param isASC 是否升序
	 * @param map 多条件查询参数
	 * @return 返回一个带分页的数据集合
	 */
	public Page<ProcurementPlan> getAllProcurementPlan(int pageNum, int pageSize, String orderBy, boolean isASC,Map map){
		String selectStr="select pp.id,pp.procurement_id,pp.product_standard_num,pp.num,pp.wait_statistics_order_total,pp.order_total,pp.create_time \n";
		StringBuilder sql=new StringBuilder();
		sql.append("\tfrom b_procurement_plan pp \n");
		orderBy = StrKit.isBlank(orderBy) ? "pp.create_time" : orderBy;
		sql.append("order by " + orderBy + " " + (isASC ? "" : "desc "));
		return paginate(pageNum,pageSize,selectStr,sql.toString(),map);
	}
}
