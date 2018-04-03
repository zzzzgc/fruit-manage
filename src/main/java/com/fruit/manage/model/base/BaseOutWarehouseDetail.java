package com.fruit.manage.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseOutWarehouseDetail<M extends BaseOutWarehouseDetail<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}

	public java.lang.Integer getId() {
		return getInt("id");
	}

	public void setOutId(java.lang.Integer outId) {
		set("out_Id", outId);
	}

	public java.lang.Integer getOutId() {
		return getInt("out_Id");
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

	public void setUserId(java.lang.Integer userId) {
		set("user_id", userId);
	}

	public java.lang.Integer getUserId() {
		return getInt("user_id");
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

	public void setProductWeight(java.math.BigDecimal productWeight) {
		set("product_weight", productWeight);
	}

	public java.math.BigDecimal getProductWeight() {
		return get("product_weight");
	}

	public void setOutNum(java.lang.Integer outNum) {
		set("out_num", outNum);
	}

	public java.lang.Integer getOutNum() {
		return getInt("out_num");
	}

	public void setOutPrice(java.math.BigDecimal outPrice) {
		set("out_price", outPrice);
	}

	public java.math.BigDecimal getOutPrice() {
		return get("out_price");
	}

	public void setOutTotalPrice(java.math.BigDecimal outTotalPrice) {
		set("out_total_price", outTotalPrice);
	}

	public java.math.BigDecimal getOutTotalPrice() {
		return get("out_total_price");
	}

	public void setOutAveragePrice(java.math.BigDecimal outAveragePrice) {
		set("out_average_price", outAveragePrice);
	}

	public java.math.BigDecimal getOutAveragePrice() {
		return get("out_average_price");
	}

	public void setUserName(java.lang.String userName) {
		set("user_name", userName);
	}

	public java.lang.String getUserName() {
		return getStr("user_name");
	}

	public void setOrderNum(java.lang.Integer orderNum) {
		set("order_num", orderNum);
	}

	public java.lang.Integer getOrderNum() {
		return getInt("order_num");
	}

	public void setOrderTime(java.util.Date orderTime) {
		set("order_time", orderTime);
	}

	public java.util.Date getOrderTime() {
		return get("order_time");
	}

	public void setOutType(java.lang.Integer outType) {
		set("out_type", outType);
	}

	public java.lang.Integer getOutType() {
		return getInt("out_type");
	}

	public void setApproverName(java.lang.String approverName) {
		set("approver_name", approverName);
	}

	public java.lang.String getApproverName() {
		return getStr("approver_name");
	}

	public void setOutRemark(java.lang.String outRemark) {
		set("out_remark", outRemark);
	}

	public java.lang.String getOutRemark() {
		return getStr("out_remark");
	}

	public void setOutTime(java.util.Date outTime) {
		set("out_time", outTime);
	}

	public java.util.Date getOutTime() {
		return get("out_time");
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
