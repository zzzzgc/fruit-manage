package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseBusinessAuth;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class BusinessAuth extends BaseBusinessAuth<BusinessAuth> {
	public static final BusinessAuth dao = new BusinessAuth().dao();

    /**
     * 根据商户信息的ID获取商户认证的信息
     * @param businessInfoID
     * @return
     */
	public BusinessAuth getBusinessAuthByBusinessInfoID(Integer businessInfoID){
		StringBuilder sql=new StringBuilder();
		sql.append("select ba.id,ba.legal_person_name,ba.identity,ba.bank_account, ");
		sql.append("ba.business_license,ba.auth_type,ba.img_identity_front, ");
		sql.append("ba.img_identity_reverse,ba.img_license,ba.img_online_shop ");
		sql.append("from b_business_auth ba ");
		sql.append("where ba.u_id = (select bi.u_id from b_business_info bi where bi.id=?) ");
		return findFirst(sql.toString(),businessInfoID);
	}
}
