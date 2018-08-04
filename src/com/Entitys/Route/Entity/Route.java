package com.Entitys.Route.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.BasicInfo;
import com.Common.Entitys.LoginableBasicInfo;

@Entity
@Table(name="route")
public class Route extends BasicInfo {
	@Column(name="name", length=20)
	@Valid(varType = VarType.String, maxLength = 20)
	private String name;
	@Column(name="transportsrc", length=20)
	@Valid(varType = VarType.String, maxLength = 20)
	private String transportsrc;
	@Column(name="transportdst", length=20)
	@Valid(varType = VarType.String, maxLength = 20)
	private String transportdst;
	@Column(name="transportdistance", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double transportdistance;
	@Column(name="viaprovince")
	@Valid(needValid = false)
	private String viaprovince;
	@Column(name="price", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double price;
	@Column(name="remark")
	@Valid(needValid = false)
	private String remark;
	public Route() {
		super();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTransportsrc() {
		return transportsrc;
	}
	public void setTransportsrc(String transportsrc) {
		this.transportsrc = transportsrc;
	}
	public String getTransportdst() {
		return transportdst;
	}
	public void setTransportdst(String transportdst) {
		this.transportdst = transportdst;
	}
	public Double getTransportdistance() {
		return transportdistance;
	}
	public void setTransportdistance(Double transportdistance) {
		this.transportdistance = transportdistance;
	}
	public String getViaprovince() {
		return viaprovince;
	}
	public void setViaprovince(String viaprovince) {
		this.viaprovince = viaprovince;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
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
		case "transportsrc":
		case "transportdst":
		case "transportdistance":
		case "viaprovince":
		case "remark":
		case "price":
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
