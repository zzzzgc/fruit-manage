package com.fruit.manage.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseRefundOrderInfo<M extends BaseRefundOrderInfo<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}

	public java.lang.Integer getId() {
		return getInt("id");
	}

	public void setOperationId(java.lang.Integer operationId) {
		set("operation_id", operationId);
	}

	public java.lang.Integer getOperationId() {
		return getInt("operation_id");
	}

	public void setUserId(java.lang.Integer userId) {
		set("user_id", userId);
	}

	public java.lang.Integer getUserId() {
		return getInt("user_id");
	}

	public void setOrderId(java.lang.String orderId) {
		set("order_id", orderId);
	}

	public java.lang.String getOrderId() {
		return getStr("order_id");
	}

	public void setSaleId(java.lang.Integer saleId) {
		set("sale_id", saleId);
	}

	public java.lang.Integer getSaleId() {
		return getInt("sale_id");
	}

	public void setOperationType(java.lang.Integer operationType) {
		set("operation_type", operationType);
	}

	public java.lang.Integer getOperationType() {
		return getInt("operation_type");
	}

	public void setRefundOfType(java.lang.Integer refundOfType) {
		set("refund_of_type", refundOfType);
	}

	public java.lang.Integer getRefundOfType() {
		return getInt("refund_of_type");
	}

	public void setRefundTheMoney(java.math.BigDecimal refundTheMoney) {
		set("refund_the_money", refundTheMoney);
	}

	public java.math.BigDecimal getRefundTheMoney() {
		return get("refund_the_money");
	}

	public void setRefundOfEvidence(java.lang.String refundOfEvidence) {
		set("refund_of_evidence", refundOfEvidence);
	}

	public java.lang.String getRefundOfEvidence() {
		return getStr("refund_of_evidence");
	}

	public void setRefundOfTime(java.util.Date refundOfTime) {
		set("refund_of_time", refundOfTime);
	}

	public java.util.Date getRefundOfTime() {
		return get("refund_of_time");
	}

	public void setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
	}

	public java.util.Date getCreateTime() {
		return get("create_time");
	}

}
