package com.fruit.manage.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseOrder<M extends BaseOrder<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}

	public java.lang.Integer getId() {
		return getInt("id");
	}

	public void setUId(java.lang.Integer uId) {
		set("u_id", uId);
	}

	public java.lang.Integer getUId() {
		return getInt("u_id");
	}

	public void setOrderId(java.lang.String orderId) {
		set("order_id", orderId);
	}

	public java.lang.String getOrderId() {
		return getStr("order_id");
	}

	public void setPayId(java.lang.String payId) {
		set("pay_id", payId);
	}

	public java.lang.String getPayId() {
		return getStr("pay_id");
	}

	public void setPayStatus(java.lang.Integer payStatus) {
		set("pay_status", payStatus);
	}

	public java.lang.Integer getPayStatus() {
		return getInt("pay_status");
	}

	public void setPayNeedMoney(java.math.BigDecimal payNeedMoney) {
		set("pay_need_money", payNeedMoney);
	}

	public java.math.BigDecimal getPayNeedMoney() {
		return get("pay_need_money");
	}

	public void setPayTotalMoney(java.math.BigDecimal payTotalMoney) {
		set("pay_total_money", payTotalMoney);
	}

	public java.math.BigDecimal getPayTotalMoney() {
		return get("pay_total_money");
	}

	public void setPayCallback(java.lang.String payCallback) {
		set("pay_callback", payCallback);
	}

	public java.lang.String getPayCallback() {
		return getStr("pay_callback");
	}

	public void setPaySuccess(java.lang.Integer paySuccess) {
		set("pay_success", paySuccess);
	}

	public java.lang.Integer getPaySuccess() {
		return getInt("pay_success");
	}

	public void setOrderStatus(java.lang.Integer orderStatus) {
		set("order_status", orderStatus);
	}

	public java.lang.Integer getOrderStatus() {
		return getInt("order_status");
	}

	public void setPayTime(java.util.Date payTime) {
		set("pay_time", payTime);
	}

	public java.util.Date getPayTime() {
		return get("pay_time");
	}

	public void setRefundStatus(java.lang.Integer refundStatus) {
		set("refund_status", refundStatus);
	}

	public java.lang.Integer getRefundStatus() {
		return getInt("refund_status");
	}

	public void setRefundTime(java.util.Date refundTime) {
		set("refund_time", refundTime);
	}

	public java.util.Date getRefundTime() {
		return get("refund_time");
	}

	public void setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
	}

	public java.util.Date getCreateTime() {
		return get("create_time");
	}

	public void setUpdateTime(java.util.Date updateTime) {
		set("update_time", updateTime);
	}

	public java.util.Date getUpdateTime() {
		return get("update_time");
	}

}
