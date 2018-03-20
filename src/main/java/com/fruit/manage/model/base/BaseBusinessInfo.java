package com.fruit.manage.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseBusinessInfo<M extends BaseBusinessInfo<M>> extends Model<M> implements IBean {

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

	public void setBusinessName(java.lang.String businessName) {
		set("business_name", businessName);
	}

	public java.lang.String getBusinessName() {
		return getStr("business_name");
	}

	public void setBusinessContacts(java.lang.String businessContacts) {
		set("business_contacts", businessContacts);
	}

	public java.lang.String getBusinessContacts() {
		return getStr("business_contacts");
	}

	public void setPhone(java.lang.String phone) {
		set("phone", phone);
	}

	public java.lang.String getPhone() {
		return getStr("phone");
	}

	public void setAddressProvince(java.lang.String addressProvince) {
		set("address_province", addressProvince);
	}

	public java.lang.String getAddressProvince() {
		return getStr("address_province");
	}

	public void setAddressCity(java.lang.String addressCity) {
		set("address_city", addressCity);
	}

	public java.lang.String getAddressCity() {
		return getStr("address_city");
	}

	public void setAddressDetail(java.lang.String addressDetail) {
		set("address_detail", addressDetail);
	}

	public java.lang.String getAddressDetail() {
		return getStr("address_detail");
	}

	public void setAddressShop(java.lang.String addressShop) {
		set("address_shop", addressShop);
	}

	public java.lang.String getAddressShop() {
		return getStr("address_shop");
	}

	public void setShipmentsType(java.lang.Integer shipmentsType) {
		set("shipments_type", shipmentsType);
	}

	public java.lang.Integer getShipmentsType() {
		return getInt("shipments_type");
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
