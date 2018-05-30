package com.fruit.manage.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseOutWarehouse<M extends BaseOutWarehouse<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}

	public java.lang.Integer getId() {
		return getInt("id");
	}

	public void setOutNum(java.lang.Integer outNum) {
		set("out_num", outNum);
	}

	public java.lang.Integer getOutNum() {
		return getInt("out_num");
	}

	public void setOutTypeNum(java.lang.Integer outTypeNum) {
		set("out_type_num", outTypeNum);
	}

	public java.lang.Integer getOutTypeNum() {
		return getInt("out_type_num");
	}

	public void setOutTotalPrice(java.math.BigDecimal outTotalPrice) {
		set("out_total_price", outTotalPrice);
	}

	public java.math.BigDecimal getOutTotalPrice() {
		return get("out_total_price");
	}

	public void setOutType(java.lang.Integer outType) {
		set("out_type", outType);
	}

	public java.lang.Integer getOutType() {
		return getInt("out_type");
	}

	public void setWarehouseAddress(java.lang.String warehouseAddress) {
		set("warehouse_address", warehouseAddress);
	}

	public java.lang.String getWarehouseAddress() {
		return getStr("warehouse_address");
	}

	public void setOrderCycleDate(java.util.Date orderCycleDate) {
		set("order_cycle_date", orderCycleDate);
	}

	public java.util.Date getOrderCycleDate() {
		return get("order_cycle_date");
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
