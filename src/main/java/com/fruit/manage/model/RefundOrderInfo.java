package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseRefundOrderInfo;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class RefundOrderInfo extends BaseRefundOrderInfo<RefundOrderInfo> {
	public static final RefundOrderInfo dao = new RefundOrderInfo().dao();

	public Page<RefundOrderInfo> getRefundOrderInfoPages(int pageNum, int pageSize, String orderBy, boolean isASC,String businessName,String salesName,String orderId,String [] createTimes) {
		String sqlSelect = "SELECT roi.id as roiId,bu.`name` as businessName,u.nick_name as saleName,roi.refund_the_money as refundTheMoney,\n" +
				"\troi.refund_of_time as refundOfTime,roi.refund_of_evidence as refundOfEvidence ";
		List list = new ArrayList();
		StringBuilder sqlSelectExcept = new StringBuilder();
		sqlSelectExcept.append("from b_refund_order_info roi\n" +
				"\tINNER JOIN b_business_user bu on bu.id = roi.user_id\n" +
				"\tINNER JOIN a_user u on u.id = roi.sale_id");
		if (StrKit.notBlank(businessName)) {
			sqlSelectExcept.append(" and bu.`name` like ? ");
			list.add("%"+businessName+"%");
		}
		if (StrKit.notBlank(salesName)) {
			sqlSelectExcept.append(" and u.nick_name like ? ");
			list.add("%"+salesName+"%");
		}
		if (StrKit.notBlank(orderId)) {
			sqlSelectExcept.append(" and roi.order_id like ? ");
			list.add("%"+orderId+"%");
		}
		if (ArrayUtils.isNotEmpty(createTimes) && createTimes.length == 2) {
			sqlSelectExcept.append(" \tand roi.refund_of_time BETWEEN ? and ? ");
			list.add(createTimes[0]+ " 00:00:00");
			list.add(createTimes[1]+" 23:59:59");
		}
		return paginate(pageNum, pageSize, sqlSelect, sqlSelectExcept.toString(), list.toArray());
	}
}
