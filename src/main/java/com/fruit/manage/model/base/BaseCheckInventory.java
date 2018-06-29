package com.fruit.manage.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseCheckInventory<M extends BaseCheckInventory<M>> extends Model<M> implements IBean {

	public void setId(java.lang.String id) {
		set("id", id);
	}

	public java.lang.String getId() {
		return getStr("id");
	}

	public void setProductCount(java.lang.Integer productCount) {
		set("product_count", productCount);
	}

	public java.lang.Integer getProductCount() {
		return getInt("product_count");
	}

	public void setProductTotalPrice(java.math.BigDecimal productTotalPrice) {
		set("product_total_price", productTotalPrice);
	}

	public java.math.BigDecimal getProductTotalPrice() {
		return get("product_total_price");
	}

	public void setWarehouseId(java.lang.Integer warehouseId) {
		set("warehouse_id", warehouseId);
	}

	public java.lang.Integer getWarehouseId() {
		return getInt("warehouse_id");
	}

	public void setWarehouseName(java.lang.String warehouseName) {
		set("warehouse_name", warehouseName);
	}

	public java.lang.String getWarehouseName() {
		return getStr("warehouse_name");
	}

	public void setOrderCycleDate(java.util.Date orderCycleDate) {
		set("order_cycle_date", orderCycleDate);
	}

	public java.util.Date getOrderCycleDate() {
		return get("order_cycle_date");
	}

	public void setCheckInventoryTime(java.util.Date checkInventoryTime) {
		set("check_inventory_time", checkInventoryTime);
	}

	public java.util.Date getCheckInventoryTime() {
		return get("check_inventory_time");
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
