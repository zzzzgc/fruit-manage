package com.fruit.manage.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseCheckInventoryDetail<M extends BaseCheckInventoryDetail<M>> extends Model<M> implements IBean {

	public void setId(java.lang.String id) {
		set("id", id);
	}

	public java.lang.String getId() {
		return getStr("id");
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

	public void setProductStandardId(java.lang.Integer productStandardId) {
		set("product_standard_id", productStandardId);
	}

	public java.lang.Integer getProductStandardId() {
		return getInt("product_standard_id");
	}

	public void setProductStandardName(java.lang.String productStandardName) {
		set("product_standard_name", productStandardName);
	}

	public java.lang.String getProductStandardName() {
		return getStr("product_standard_name");
	}

	public void setCheckInventoryId(java.lang.String checkInventoryId) {
		set("check_inventory_id", checkInventoryId);
	}

	public java.lang.String getCheckInventoryId() {
		return getStr("check_inventory_id");
	}

	public void setProductWeight(java.lang.Double productWeight) {
		set("product_weight", productWeight);
	}

	public java.lang.Double getProductWeight() {
		return getDouble("product_weight");
	}

	public void setInventoryPrice(java.math.BigDecimal inventoryPrice) {
		set("inventory_price", inventoryPrice);
	}

	public java.math.BigDecimal getInventoryPrice() {
		return get("inventory_price");
	}

	public void setInventoryNum(java.lang.Integer inventoryNum) {
		set("inventory_num", inventoryNum);
	}

	public java.lang.Integer getInventoryNum() {
		return getInt("inventory_num");
	}

	public void setCheckInventoryNum(java.lang.Integer checkInventoryNum) {
		set("check_inventory_num", checkInventoryNum);
	}

	public java.lang.Integer getCheckInventoryNum() {
		return getInt("check_inventory_num");
	}

	public void setInventoryTotalPrice(java.math.BigDecimal inventoryTotalPrice) {
		set("inventory_total_price", inventoryTotalPrice);
	}

	public java.math.BigDecimal getInventoryTotalPrice() {
		return get("inventory_total_price");
	}

	public void setUserId(java.lang.Integer userId) {
		set("user_id", userId);
	}

	public java.lang.Integer getUserId() {
		return getInt("user_id");
	}

	public void setUserName(java.lang.String userName) {
		set("user_name", userName);
	}

	public java.lang.String getUserName() {
		return getStr("user_name");
	}

	public void setInventoryRemark(java.lang.String inventoryRemark) {
		set("inventory_remark", inventoryRemark);
	}

	public java.lang.String getInventoryRemark() {
		return getStr("inventory_remark");
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
