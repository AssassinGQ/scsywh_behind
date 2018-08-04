package com.Entitys.Admin.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.LoginableBasicInfo;
import com.Entitys.User.Entity.LoginInfo;

@Entity
@Table(name="admin")
public class Admin extends LoginableBasicInfo {
	public final static int DEPT_STORAGE = 0;//仓储
	public final static int DEPT_CHEMICAL = 1;//危化品
	public final static int DEPT_FINANCE = 2;//财务
	public final static int DEPT_INFORMATION = 3;//信息
	public final static int DEPT_MANAGER = 4;//经理
	@Column(name="dept", length=4)
	@Valid(varType = VarType.Number, minValue = 1, maxValue = 9999)
	private Integer dept;
	@Column(name="email", length=30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String email;
	public Admin() {
		super();
		USERTYPE = LoginInfo.TYPE_ADMIN;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Integer getDept() {
		return dept;
	}
	public void setDept(Integer dept) {
		if(dept < DEPT_STORAGE || dept > DEPT_MANAGER)
			dept = DEPT_MANAGER;
		this.dept = dept;
	}
	@Override
	public boolean WritePremission(String key, int role) {
		if(role == LoginableBasicInfo.ROLE_SYSTEM)
			return true;
		switch (key) {
		case "createdat":
		case "createdid":
		case "updatedat":
		case "updatedid":
			return role > LoginableBasicInfo.ROLE_SYSTEM;
		case "corporationsid":
			return role >= LoginableBasicInfo.ROLE_SUPER;
		case "sid":
		case "dept":
			return role >= LoginableBasicInfo.ROLE_CORPORATION;
		case "username":
		case "password":
		case "phone":
		case "name":
		case "email":
			return role >= LoginableBasicInfo.ROLE_ADMIN;
		default:
			return false;
		}
	}
	@Override
	public boolean ReadPremission(String key, int role) {
		switch (key) {
		case "createdat":
		case "createdid":
		case "updatedat":
		case "updatedid":
			return role >= LoginableBasicInfo.ROLE_CORPORATION;
		case "sid":
		case "dept":
		case "corporationsid":
		case "username":
		case "password":
		case "phone":
		case "name":
		case "email":
			return role >= LoginableBasicInfo.ROLE_ADMIN;
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
//			this.phone_te_11 = phone;
//		}
//	}
}
