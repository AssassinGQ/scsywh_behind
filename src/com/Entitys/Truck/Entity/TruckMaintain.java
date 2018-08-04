package com.Entitys.Truck.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.BasicInfo;

//增删查改记录
@Entity
@Table(name = "truckmaintain")
public class TruckMaintain extends BasicInfo {
	@Column(name = "trucknumber", length = 30)
	@Valid(varType = VarType.String, maxLength = 30)
	private String trucknumber;
	@Column(name = "wxdh", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String wxdh;	//维修单号
	@Column(name = "cpys", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String cpys;	//车牌颜色
	@Column(name = "cllx", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String cllx;	//车辆类型
	@Column(name = "dph", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String dph;	//底盘号
	@Column(name = "txf", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String txf;	//托修方
	@Column(name = "txfdh", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String txfdh;	//托修方电话
	@Column(name = "wxnr", length = 100)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String wxnr;	//维修内容
	@Column(name = "fyhj", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double fyhj;	//费用合计
	//private Integer ccts_fm_4;	//出厂天数，前端自己计算
	@Column(name = "bxq", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long bxq;	//保修期
	public TruckMaintain() {
		super();
	}
	public String getTrucknumber() {
		return trucknumber;
	}
	public void setTrucknumber(String trucknumber) {
		this.trucknumber = trucknumber;
	}
	public String getWxdh() {
		return wxdh;
	}
	public void setWxdh(String wxdh) {
		this.wxdh = wxdh;
	}
	public String getCpys() {
		return cpys;
	}
	public void setCpys(String cpys) {
		this.cpys = cpys;
	}
	public String getCllx() {
		return cllx;
	}
	public void setCllx(String cllx) {
		this.cllx = cllx;
	}
	public String getDph() {
		return dph;
	}
	public void setDph(String dph) {
		this.dph = dph;
	}
	public String getTxf() {
		return txf;
	}
	public void setTxf(String txf) {
		this.txf = txf;
	}
	public String getTxfdh() {
		return txfdh;
	}
	public void setTxfdh(String txfdh) {
		this.txfdh = txfdh;
	}
	public String getWxnr() {
		return wxnr;
	}
	public void setWxnr(String wxnr0) {
		this.wxnr = wxnr0;
	}
	public Double getFyhj() {
		return fyhj;
	}
	public void setFyhj(Double fyhj) {
		this.fyhj = fyhj;
	}
	public Long getBxq() {
		return bxq;
	}
	public void setBxq(Long bxq) {
		this.bxq = bxq;
	}
	@Override
	public boolean WritePremission(String key, int role) {
		switch (key) {
		case "sid":
		case "createdid":
		case "createdat":
		case "updatedid":
		case "updatedat":
		case "corporationsid":
		case "trucknumber":
			return role >= BasicInfo.ROLE_SYSTEM;
		case "wxdh":
		case "cpys":
		case "cllx":
		case "dph":
		case "txf":
		case "txfdh":
		case "wxnr":
		case "fyhj":
		case "bxq":
			return role >= BasicInfo.ROLE_ADMIN;
		default:
			return false;
		}
	}
	@Override
	public boolean ReadPremission(String key, int role) {
		// TODO Auto-generated method stub
		return false;
	}
}
