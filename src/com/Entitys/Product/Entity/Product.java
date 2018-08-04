package com.Entitys.Product.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.BasicInfo;
import com.Common.Entitys.LoginableBasicInfo;

@Entity
@Table(name="product")
public class Product extends BasicInfo {
	@Column(name="name", length=20)
	@Valid(varType = VarType.String, maxLength = 20)
	private String name;
	@Column(name="remark")
	@Valid(needValid = false)
	private String remark;
	@Column(name="unnumber", length=20)
	@Valid(varType = VarType.String, maxLength = 20)
	private String unnumber;
	@Column(name="type", length=4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer type;
	@Column(name="packettype", length=4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer packettype;
	@Column(name="packetrank", length=4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer packetrank;
	public Product() {
		super();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getUnnumber() {
		return unnumber;
	}
	public void setUnnumber(String unnumber) {
		this.unnumber = unnumber;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getPackettype() {
		return packettype;
	}
	public void setPackettype(Integer packettype) {
		this.packettype = packettype;
	}
	public Integer getPacketrank() {
		return packetrank;
	}
	public void setPacketrank(Integer packetrank) {
		this.packetrank = packetrank;
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
		case "name":
		case "remark":
		case "unnumber":
		case "type":
		case "packettype":
		case "packetrank":
			return role >= LoginableBasicInfo.ROLE_ADMIN;
		default:
			return false;
		}
	}
	@Override
	public boolean ReadPremission(String key, int role) {
		// TODO Auto-generated method stub
		return true;
	}
}
