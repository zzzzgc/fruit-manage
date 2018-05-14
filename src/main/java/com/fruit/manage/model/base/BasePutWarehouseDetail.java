package com.fruit.manage.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BasePutWarehouseDetail<M extends BasePutWarehouseDetail<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}

	public java.lang.Integer getId() {
		return getInt("id");
	}

	public void setProductName(java.lang.String productName) {
		set("product_name", productName);
	}

	public java.lang.String getProductName() {
		return getStr("product_name");
	}

	public void setProcurementId(java.lang.Integer procurementId) {
		set("procurement_id", procurementId);
	}

	public java.lang.Integer getProcurementId() {
		return getInt("procurement_id");
	}

	public void setProductId(java.lang.Integer productId) {
		set("product_id", productId);
	}

	public java.lang.Integer getProductId() {
		return getInt("product_id");
	}

	public void setProductStandardName(java.lang.String productStandardName) {
		set("product_standard_name", productStandardName);
	}

	public java.lang.String getProductStandardName() {
		return getStr("product_standard_name");
	}

	public void setProductStandardId(java.lang.Integer productStandardId) {
		set("product_standard_id", productStandardId);
	}

	public java.lang.Integer getProductStandardId() {
		return getInt("product_standard_id");
	}

	public void setProductWeight(java.lang.String productWeight) {
		set("product_weight", productWeight);
	}

	public java.lang.String getProductWeight() {
		return getStr("product_weight");
	}

	public void setProcurementPrice(java.math.BigDecimal procurementPrice) {
		set("procurement_price", procurementPrice);
	}

	public java.math.BigDecimal getProcurementPrice() {
		return get("procurement_price");
	}

	public void setProcurementTotalPrice(java.math.BigDecimal procurementTotalPrice) {
		set("procurement_total_price", procurementTotalPrice);
	}

	public java.math.BigDecimal getProcurementTotalPrice() {
		return get("procurement_total_price");
	}

	public void setBoothCost(java.math.BigDecimal boothCost) {
		set("booth_cost", boothCost);
	}

	public java.math.BigDecimal getBoothCost() {
		return get("booth_cost");
	}

	public void setPutNum(java.lang.Integer putNum) {
		set("put_num", putNum);
	}

	public java.lang.Integer getPutNum() {
		return getInt("put_num");
	}

	public void setPutAveragePrice(java.math.BigDecimal putAveragePrice) {
		set("put_average_price", putAveragePrice);
	}

	public java.math.BigDecimal getPutAveragePrice() {
		return get("put_average_price");
	}

	public void setProcurementName(java.lang.String procurementName) {
		set("procurement_name", procurementName);
	}

	public java.lang.String getProcurementName() {
		return getStr("procurement_name");
	}

	public void setPutRemark(java.lang.String putRemark) {
		set("put_remark", putRemark);
	}

	public java.lang.String getPutRemark() {
		return getStr("put_remark");
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

	public void setPutId(java.lang.Integer putId) {
		set("put_id", putId);
	}

	public java.lang.Integer getPutId() {
		return getInt("put_id");
	}

}
