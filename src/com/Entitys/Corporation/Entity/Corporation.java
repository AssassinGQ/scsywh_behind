package com.Entitys.Corporation.Entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Entitys.LoginableBasicInfo;

@Entity
@Table(name="corporation")
public class Corporation extends LoginableBasicInfo {
	public Corporation() {
		super();
	}
//	@Override
//	public void UpdateLoginInfo(Map<String, String> params) {
//		String username = params.get("username");
//		if(username != null){//全部role都可以修改
//			this.username_tm_30 = username;
//		}
//		String password = params.get("password");
//		if(password != null){
//			this.password_te_6 = password;
//		}
//		String phone = params.get("phone");
//		if(phone != null){
//			this.phone_te_11 = phone;
//		}
//	}
}
