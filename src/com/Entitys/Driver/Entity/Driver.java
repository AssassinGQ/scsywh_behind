package com.Entitys.Driver.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.BasicInfo;
import com.Common.Entitys.LoginableBasicInfo;
import com.Entitys.Escort.Entity.Escort;
import com.Entitys.User.Entity.LoginInfo;

@Entity
@Table(name="driver")
public class Driver extends LoginableBasicInfo {
	
	public final static int STATUS_IDLE = 0;
	public final static int STATUS_TASK = 1;
	public final static int STATUS_INVAILD = 2;
	@Column(name="QCtype", length=4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer QCtype;
	@Column(name="QCnumber", length=20)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String QCnumber;
	@Column(name="QCddl", length=13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long QCddl;
	@Column(name="QCorganization", length=20)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String QCorganization;
	@Column(name="joinjobtime", length=13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long joinjobtime;
	@Column(name="status", length=4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer status;
	@Column(name="labourcontractddl", length=13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long labourcontractddl;
	public Driver() {
		super();
		this.status = Escort.STATUS_IDLE;
		USERTYPE = LoginInfo.TYPE_DRIVER;
	}
	public Integer getQCtype() {
		return QCtype;
	}
	public void setQCtype(Integer qCtype) {
		QCtype = qCtype;
	}
	public String getQCnumber() {
		return QCnumber;
	}
	public void setQCnumber(String qCnumber) {
		QCnumber = qCnumber;
	}
	public Long getQCddl() {
		return QCddl;
	}
	public void setQCddl(Long qCddl) {
		QCddl = qCddl;
	}
	public String getQCorganization() {
		return QCorganization;
	}
	public void setQCorganization(String qCorganization) {
		QCorganization = qCorganization;
	}
	public Long getJoinjobtime() {
		return joinjobtime;
	}
	public void setJoinjobtime(Long joinjobtime) {
		this.joinjobtime = joinjobtime;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Long getLabourcontractddl() {
		return labourcontractddl;
	}
	public void setLabourcontractddl(Long labourcontractddl) {
		this.labourcontractddl = labourcontractddl;
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
		case "status":
		case "joinjobtime":
		case "labourcontractddl":
		case "QCtype":
		case "QCnumber":
		case "QCddl":
		case "QCorganization":
			return role >= LoginableBasicInfo.ROLE_ADMIN;
		case "username":
		case "password":
		case "name":
		case "phone":
			return role >= LoginableBasicInfo.ROLE_USER;
		default:
			return false;
		}
	}
	@Override
	public boolean ReadPremission(String key, int role) {
		if(role == BasicInfo.ROLE_GOVERNMENT)
			return true;
		switch (key) {
		case "sid":
		case "createdat":
		case "createdid":
		case "updatedat":
		case "updatedid":
			return role >= LoginableBasicInfo.ROLE_ADMIN;
		case "corporationsid":
		case "status":
		case "joinjobtime":
		case "labourcontractddl":
		case "QCtype":
		case "QCnumber":
		case "QCddl":
		case "QCorganization":
		case "username":
		case "password":
		case "name":
		case "phone":
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
//			this.phone_te_11 = phone;
//		}
//	}
}
