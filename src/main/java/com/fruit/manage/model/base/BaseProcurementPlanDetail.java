package com.fruit.manage.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseProcurementPlanDetail<M extends BaseProcurementPlanDetail<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}

	public java.lang.Integer getId() {
		return getInt("id");
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

	public void setProcurementId(java.lang.Integer procurementId) {
		set("procurement_id", procurementId);
	}

	public java.lang.Integer getProcurementId() {
		return getInt("procurement_id");
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

	public void setSellPrice(java.math.BigDecimal sellPrice) {
		set("sell_price", sellPrice);
	}

	public java.math.BigDecimal getSellPrice() {
		return get("sell_price");
	}

	public void setInventoryNum(java.lang.Integer inventoryNum) {
		set("inventory_num", inventoryNum);
	}

	public java.lang.Integer getInventoryNum() {
		return getInt("inventory_num");
	}

	public void setProcurementNum(java.lang.Integer procurementNum) {
		set("procurement_num", procurementNum);
	}

	public java.lang.Integer getProcurementNum() {
		return getInt("procurement_num");
	}

	public void setProductStandardNum(java.lang.Integer productStandardNum) {
		set("product_standard_num", productStandardNum);
	}

	public java.lang.Integer getProductStandardNum() {
		return getInt("product_standard_num");
	}

	public void setProcurementNeedPrice(java.math.BigDecimal procurementNeedPrice) {
		set("procurement_need_price", procurementNeedPrice);
	}

	public java.math.BigDecimal getProcurementNeedPrice() {
		return get("procurement_need_price");
	}

	public void setProcurementTotalPrice(java.math.BigDecimal procurementTotalPrice) {
		set("procurement_total_price", procurementTotalPrice);
	}

	public java.math.BigDecimal getProcurementTotalPrice() {
		return get("procurement_total_price");
	}

	public void setOrderRemark(java.lang.String orderRemark) {
		set("order_remark", orderRemark);
	}

	public java.lang.String getOrderRemark() {
		return getStr("order_remark");
	}

	public void setProcurementRemark(java.lang.String procurementRemark) {
		set("procurement_remark", procurementRemark);
	}

	public java.lang.String getProcurementRemark() {
		return getStr("procurement_remark");
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
