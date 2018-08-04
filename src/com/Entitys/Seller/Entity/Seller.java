package com.Entitys.Seller.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.LoginableBasicInfo;
import com.Entitys.User.Entity.LoginInfo;

@Entity
@Table(name="seller")
public class Seller extends LoginableBasicInfo {
	@Column(name="manname", length=20)
	@Valid(varType = VarType.String, maxLength = 20)
	private String manname;
	@Column(name="address")
	@Valid(needValid = false)
	private String address;
	public Seller() {
		super();
		USERTYPE = LoginInfo.TYPE_SELLER;
	}
	public String getManname() {
		return manname;
	}
	public void setManname(String manname) {
		this.manname = manname;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	@Override
	public boolean WritePremission(String key, int role) {
		switch (key) {
		case "sid":
		case "createdat":
		case "createdid":
		case "updatedat":
		case "updatedid":
			return role >= LoginableBasicInfo.ROLE_SYSTEM;
		case "corporationsid":
			return role >= LoginableBasicInfo.ROLE_SUPER;
		case "username":
		case "password":
		case "phone":
		case "name":
		case "manname":
		case "address":
			return role >= LoginableBasicInfo.ROLE_USER;
		default:
			return false;
		}
	}
	@Override
	public boolean ReadPremission(String key, int role) {
		switch (key) {
		case "sid":
			return role >= LoginableBasicInfo.ROLE_ADMIN;
		case "createdat":
		case "createdid":
		case "updatedat":
		case "updatedid":
		case "corporationsid":
		case "username":
		case "password":
		case "phone":
		case "name":
		case "manname":
		case "address":
			return role >= LoginableBasicInfo.ROLE_USER;
		default:
			return false;
		}
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
//			this.phone_fe_11 = phone;
//		}
//	}
}
