package com.fruit.manage.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BasePayOrderInfo<M extends BasePayOrderInfo<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}

	public java.lang.Integer getId() {
		return getInt("id");
	}

	public void setPayOfType(java.lang.Integer payOfType) {
		set("pay_of_type", payOfType);
	}

	public java.lang.Integer getPayOfType() {
		return getInt("pay_of_type");
	}

	public void setPayOfTime(java.util.Date payOfTime) {
		set("pay_of_time", payOfTime);
	}

	public java.util.Date getPayOfTime() {
		return get("pay_of_time");
	}

	public void setPayReallyTotalMoney(java.lang.Double payReallyTotalMoney) {
		set("pay_really_total_money", payReallyTotalMoney);
	}

	public java.lang.Double getPayReallyTotalMoney() {
		return getDouble("pay_really_total_money");
	}

	public void setPayTheMoney(java.lang.Double payTheMoney) {
		set("pay_the_money", payTheMoney);
	}

	public java.lang.Double getPayTheMoney() {
		return getDouble("pay_the_money");
	}

	public void setPayOfEvidence(java.lang.String payOfEvidence) {
		set("pay_of_evidence", payOfEvidence);
	}

	public java.lang.String getPayOfEvidence() {
		return getStr("pay_of_evidence");
	}

	public void setUserId(java.lang.Integer userId) {
		set("user_id", userId);
	}

	public java.lang.Integer getUserId() {
		return getInt("user_id");
	}

	public void setSaleId(java.lang.Integer saleId) {
		set("sale_id", saleId);
	}

	public java.lang.Integer getSaleId() {
		return getInt("sale_id");
	}

	public void setOrderId(java.lang.String orderId) {
		set("order_id", orderId);
	}

	public java.lang.String getOrderId() {
		return getStr("order_id");
	}

	public void setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
	}

	public java.util.Date getCreateTime() {
		return get("create_time");
	}

}