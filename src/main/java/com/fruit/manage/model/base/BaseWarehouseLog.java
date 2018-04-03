package com.fruit.manage.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseWarehouseLog<M extends BaseWarehouseLog<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}

	public java.lang.Integer getId() {
		return getInt("id");
	}

	public void setUserId(java.lang.Integer userId) {
		set("user_id", userId);
	}

	public java.lang.Integer getUserId() {
		return getInt("user_id");
	}

	public void setProductStandardId(java.lang.Integer productStandardId) {
		set("product_standard_id", productStandardId);
	}

	public java.lang.Integer getProductStandardId() {
		return getInt("product_standard_id");
	}

	public void setUserType(java.lang.Integer userType) {
		set("user_type", userType);
	}

	public java.lang.Integer getUserType() {
		return getInt("user_type");
	}

	public void setChangeNum(java.lang.Integer changeNum) {
		set("change_num", changeNum);
	}

	public java.lang.Integer getChangeNum() {
		return getInt("change_num");
	}

	public void setChangeType(java.lang.String changeType) {
		set("change_type", changeType);
	}

	public java.lang.String getChangeType() {
		return getStr("change_type");
	}

	public void setIsStatistical(java.lang.Integer isStatistical) {
		set("is_statistical", isStatistical);
	}

	public java.lang.Integer getIsStatistical() {
		return getInt("is_statistical");
	}

	public void setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
	}

	public java.util.Date getCreateTime() {
		return get("create_time");
	}

	public void setProductId(java.lang.Integer productId) {
		set("product_id", productId);
	}

	public java.lang.Integer getProductId() {
		return getInt("product_id");
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

}
