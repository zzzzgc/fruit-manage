package com.fruit.manage.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseOrderBreakage<M extends BaseOrderBreakage<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}

	public java.lang.Integer getId() {
		return getInt("id");
	}

	public void setOrderId(java.lang.String orderId) {
		set("order_id", orderId);
	}

	public java.lang.String getOrderId() {
		return getStr("order_id");
	}

	public void setBreakageImgId(java.lang.String breakageImgId) {
		set("breakage_img_id", breakageImgId);
	}

	public java.lang.String getBreakageImgId() {
		return getStr("breakage_img_id");
	}

	public void setProductId(java.lang.Integer productId) {
		set("product_id", productId);
	}

	public java.lang.Integer getProductId() {
		return getInt("product_id");
	}

	public void setProductStandardId(java.lang.Integer productStandardId) {
		set("product_standard_id", productStandardId);
	}

	public java.lang.Integer getProductStandardId() {
		return getInt("product_standard_id");
	}

	public void setProductName(java.lang.String productName) {
		set("product_name", productName);
	}

	public java.lang.String getProductName() {
		return getStr("product_name");
	}

	public void setProductStandardName(java.lang.String productStandardName) {
		set("product_standard_name", productStandardName);
	}

	public java.lang.String getProductStandardName() {
		return getStr("product_standard_name");
	}

	public void setBreakageNum(java.lang.Integer breakageNum) {
		set("breakage_num", breakageNum);
	}

	public java.lang.Integer getBreakageNum() {
		return getInt("breakage_num");
	}

	public void setReferenceBreakagePrice(java.math.BigDecimal referenceBreakagePrice) {
		set("reference_breakage_price", referenceBreakagePrice);
	}

	public java.math.BigDecimal getReferenceBreakagePrice() {
		return get("reference_breakage_price");
	}

	public void setBreakagePrice(java.math.BigDecimal breakagePrice) {
		set("breakage_price", breakagePrice);
	}

	public java.math.BigDecimal getBreakagePrice() {
		return get("breakage_price");
	}

	public void setBreakageDescribe(java.lang.String breakageDescribe) {
		set("breakage_describe", breakageDescribe);
	}

	public java.lang.String getBreakageDescribe() {
		return getStr("breakage_describe");
	}

	public void setAudit(java.lang.Integer audit) {
		set("audit", audit);
	}

	public java.lang.Integer getAudit() {
		return getInt("audit");
	}

	public void setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
	}

	public java.util.Date getCreateTime() {
		return get("create_time");
	}

}
