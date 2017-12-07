package com.fruit.manage.constant;

import java.util.HashMap;
import java.util.Map;

public class OrderConstant {

	public static Map<Integer, String> ORDER_STATUS_MAP;
	static {
		ORDER_STATUS_MAP = new HashMap<Integer, String>();
//		ORDER_STATUS_MAP.put(-1, "已删除");
		ORDER_STATUS_MAP.put(0, "等待付款");
		ORDER_STATUS_MAP.put(1, "审核中");
		ORDER_STATUS_MAP.put(2, "已审核");
		ORDER_STATUS_MAP.put(3, "等待发货");
		ORDER_STATUS_MAP.put(4, "待确认收货");
		ORDER_STATUS_MAP.put(5, "交易成功");
		ORDER_STATUS_MAP.put(6, "交易取消");
		ORDER_STATUS_MAP.put(7, "已退费");
	}
	
}
