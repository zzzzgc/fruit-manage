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
	 * @param orderTime
	 * @param pageNum
	 * @param pageSize
	 * @param orderBy
	 * @param isASC
	 * @return
	 */
	public Page<Order> getData(Order order, String[] orderTime, int pageNum, int pageSize, String orderBy, boolean isASC){
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("from b_order where 1=1 ");
		if(StrKit.notBlank(order.getOrderId())){
			sql.append("and order_id = ? ");
			params.add(order.getOrderId());
		}
		if(StrKit.notBlank(order.getProductId())){
			sql.append("and product_id = ? ");
			params.add(order.getProductId());
		}
		if(order.getBuyuserUid() != null && order.getBuyuserUid() > 0){
			sql.append("and buyuser_uid = ? ");
			params.add(order.getBuyuserUid());
		}
		if(order.getOrderStatus() != null){
			sql.append("and order_status = ? ");
			params.add(order.getOrderStatus());
		}
		
		if(ArrayUtils.isNotEmpty(orderTime) && orderTime.length == 2){
			sql.append("and order_time BETWEEN ? and ? ");
			params.add(orderTime[0] + " 00:00:00");
			params.add(orderTime[1] + " 23:59:59");
		}
		sql.append("and order_status >= 0 ");
		orderBy = StrKit.isBlank(orderBy)?"order_time":orderBy;
		sql.append("order by "+orderBy+" "+(isASC?"":"desc "));
		
		return paginate(pageNum, pageSize, "select * ", sql.toString(), params.toArray());
	}
}
