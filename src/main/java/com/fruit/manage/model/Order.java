package com.fruit.manage.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.fruit.manage.model.base.BaseOrder;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class Order extends BaseOrder<Order> {
	public static final Order dao = new Order().dao();

	/**
	 * 分页获取数据
	 * @param order
	 * @param createTime
	 * @param pageNum
	 * @param pageSize
	 * @param orderBy
	 * @param isASC
	 * @return
	 */
	public Page<Order> getData(Order order, String[] createTime, int pageNum, int pageSize, String orderBy, boolean isASC){
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("FROM b_order_detail AS od INNER JOIN b_order AS o ON od.order_id = o.order_id WHERE 1 = 1 ");
		if(StrKit.notBlank(order.getOrderId())){
			sql.append("and o.order_id = ? ");
			params.add(order.getOrderId());
		}
		if(order.get("product_id")!=null && StrKit.notBlank(order.get("product_id").toString())){
			sql.append("and od.product_id = ? ");
			params.add(order.get("productId"));
		}
		if(order.getUId() != null && order.getUId() > 0){
			sql.append("and od.buy_uid = ? ");
			params.add(order.getUId());
		}
		if(order.getOrderStatus() != null && order.getOrderStatus() >= 0){
			sql.append("and o.order_status  = ? ");
			params.add(order.getOrderStatus());
		}
		
		if(ArrayUtils.isNotEmpty(createTime) && createTime.length == 2){
			sql.append("and o.create_time BETWEEN ? and ? ");
			params.add(createTime[0] + " 00:00:00");
			params.add(createTime[1] + " 23:59:59");
		}
		orderBy = StrKit.isBlank(orderBy)?"o.create_time":orderBy;
		sql.append("order by "+orderBy+" "+(isASC?"":"desc "));

		System.out.println("select * "+sql.toString());
		
		return paginate(pageNum, pageSize, "select * ", sql.toString(), params.toArray());
	}
}