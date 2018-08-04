package com.Common.Entitys;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;

@MappedSuperclass
public abstract class LoginableBasicInfo extends BasicInfo {
//	public final static int ROLE_SYSTEM = 5;
//	public final static int ROLE_SUPER = 4;
//	public final static int ROLE_CORPORATION = 3;
//	public final static int ROLE_ADMIN = 2;
//	public final static int ROLE_USER = 1;
//	public final static int ROLE_GOVERNMENT = 0;
	@Column(name="username", length=30)
	@Valid(varType = VarType.String, maxLength = 30)
	protected String username;
//	@Column(name="password", length=10)
//	protected String password;
	@Column(name="phone", length=11)
	@Valid(varType = VarType.String, minLength = 11, maxLength = 11)
	protected String phone;
	@Column(name="name", length=20)
	@Valid(varType = VarType.String, maxLength = 30)
	protected String name;
	public LoginableBasicInfo() {
		super();
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
//	public String getPassword() {
//		return password;
//	}
//	public void setPassword(String password) {
//		this.password = password;
//	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
