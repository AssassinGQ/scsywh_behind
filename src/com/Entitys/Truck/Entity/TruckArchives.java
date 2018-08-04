package com.Entitys.Truck.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.BasicInfo;
import com.Common.Utils.Utils;

//查改
@Entity
@Table(name = "truckarchives")
public class TruckArchives extends BasicInfo {
	public final static int STATUS_VALID = 0;
	public final static int STATUS_INVALID = 1;
	@Column(name = "status", length = 4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer status; // 状态，0有效，1失效
	@Column(name = "trucknumber", length = 30)
	@Valid(varType = VarType.String, maxLength = 30)
	private String trucknumber; // 车牌号
	@Column(name = "validtime", length = 13)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 13)
	private String validtime; // 有效期，时间戳
	@Column(name = "manname", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String manname; // 联系人名字
	@Column(name = "manphone", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String manphone; // 联系人手机
	@Column(name = "dlyszh", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String dlyszh; // 道路运输证号
	@Column(name = "jyfw", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String jyfw; // 经营范围
	@Column(name = "cpys", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String cpys; // 车牌颜色
	@Column(name = "ygjg", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String ygjg; // 运管机构
	@Column(name = "cx", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String cx; // 车型
	@Column(name = "cxfl", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String cxfl; // 车型分类
	@Column(name = "clfl", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String clfl; // 车辆分类
	@Column(name = "cllx", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String cllx; // 车辆类型
	@Column(name = "cz", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String cz; // 车种
	@Column(name = "csys", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String csys; // 车身颜色
	@Column(name = "dph", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String dph; // 底盘号
	@Column(name = "cp", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String cp; // 厂牌
	@Column(name = "fdjh", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String fdjh; // 发动机号
	@Column(name = "cljsdj", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String cljsdj; // 发动机号

	@Column(name = "lastannualtime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long lastannualtime;
	@Column(name = "lastsecondtime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long lastsecondtime;
	@Column(name = "lastsecondcontent", length = 100)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 100)
	private String lastsecondcontent_fm_100;
	@Column(name = "annalduration", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long annalduration;
	@Column(name = "secondduration", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long secondduration;
	@Column(name = "nextannualtime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long nextannualtime;
	@Column(name = "nextsecondtime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long nextsecondtime;

	public TruckArchives() {
		super();
		this.status = STATUS_VALID;
	}

	public void setNextannualtime(Long nextannualtime) {
		this.nextannualtime = nextannualtime;
	}

	public void setNextsecondtime(Long nextsecondtime) {
		this.nextsecondtime = nextsecondtime;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getTrucknumber() {
		return trucknumber;
	}

	public void setTrucknumber(String trucknumber) {
		this.trucknumber = trucknumber;
	}

	public String getValidtime() {
		return validtime;
	}

	public void setValidtime(String validtime) {
		this.validtime = validtime;
	}

	public String getManname() {
		return manname;
	}

	public void setManname(String manname) {
		this.manname = manname;
	}

	public String getManphone() {
		return manphone;
	}

	public void setManphone(String manphone) {
		this.manphone = manphone;
	}

	public String getDlyszh() {
		return dlyszh;
	}

	public void setDlyszh(String dlyszh) {
		this.dlyszh = dlyszh;
	}

	public String getJyfw() {
		return jyfw;
	}

	public void setJyfw(String jyfw) {
		this.jyfw = jyfw;
	}

	public String getCpys() {
		return cpys;
	}

	public void setCpys(String cpys) {
		this.cpys = cpys;
	}

	public String getYgjg() {
		return ygjg;
	}

	public void setYgjg(String ygjg) {
		this.ygjg = ygjg;
	}

	public String getCx() {
		return cx;
	}

	public void setCx(String cx) {
		this.cx = cx;
	}

	public String getCxfl() {
		return cxfl;
	}

	public void setCxfl(String cxfl) {
		this.cxfl = cxfl;
	}

	public String getClfl() {
		return clfl;
	}

	public void setClfl(String clfl) {
		this.clfl = clfl;
	}

	public String getCllx() {
		return cllx;
	}

	public void setCllx(String cllx) {
		this.cllx = cllx;
	}

	public String getCz() {
		return cz;
	}

	public void setCz(String cz) {
		this.cz = cz;
	}

	public String getCsys() {
		return csys;
	}

	public void setCsys(String csys) {
		this.csys = csys;
	}

	public String getDph() {
		return dph;
	}

	public void setDph(String dph) {
		this.dph = dph;
	}

	public String getCp() {
		return cp;
	}

	public void setCp(String cp) {
		this.cp = cp;
	}

	public String getFdjh() {
		return fdjh;
	}

	public void setFdjh(String fdjh) {
		this.fdjh = fdjh;
	}

	public String getCljsdj() {
		return cljsdj;
	}

	public void setCljsdj(String cljsdj) {
		this.cljsdj = cljsdj;
	}

	public Long getLastannualtime() {
		return lastannualtime;
	}

	public void setLastannualtime(Long lastannualtime) {
		this.lastannualtime = lastannualtime;
		if (this.lastannualtime != null && this.annalduration != null)
			this.nextannualtime = this.lastannualtime + this.annalduration;
	}

	public Long getLastsecondtime() {
		return lastsecondtime;
	}

	public void setLastsecondtime(Long lastsecondtime) {
		this.lastsecondtime = lastsecondtime;
		if (this.lastsecondtime != null && this.secondduration != null)
			this.nextsecondtime = this.lastsecondtime + this.secondduration;
	}

	public String getLastsecondcontent_fm_100() {
		return lastsecondcontent_fm_100;
	}

	public void setLastsecondcontent_fm_100(String lastsecondcontent_fm_100) {
		this.lastsecondcontent_fm_100 = lastsecondcontent_fm_100;
	}

	public Long getAnnalduration() {
		return annalduration;
	}

	public void setAnnalduration(Long annalduration) {
		this.annalduration = annalduration;
		if (this.lastannualtime != null && this.annalduration != null)
			this.nextannualtime = this.lastannualtime + this.annalduration;
	}

	public Long getSecondduration() {
		return secondduration;
	}

	public void setSecondduration(Long secondduration) {
		this.secondduration = secondduration;
		if (this.lastsecondtime != null && this.secondduration != null)
			this.nextsecondtime = this.lastsecondtime + this.secondduration;
	}

	public Long getNextannualtime() {
		return nextannualtime;
	}

	public Long getNextsecondtime() {
		return nextsecondtime;
	}
	
	public boolean needannualcheck(){
		if(nextannualtime == null)
			return true;
		else
			return Utils.getCurrenttimeMills() >= this.nextannualtime;
	}
	public boolean needsecondcheck(){
		if(nextsecondtime == null)
			return true;
		else
			return Utils.getCurrenttimeMills() >= this.nextsecondtime;
	}

	@Override
	public boolean WritePremission(String key, int role) {
		switch (key) {
		case "lastannualtime":
		case "lastsecondtime":
		case "lastsecondcontent":
		case "annalduration":
		case "secondduration":
		case "nextannualtime":
		case "nextsecondtime":
			return false;
		case "sid":
		case "createdid":
		case "updatedid":
		case "createdat":
		case "updatedat":
		case "corporationsid":
			return role >= BasicInfo.ROLE_SUPER;
		default:
			return role >= BasicInfo.ROLE_ADMIN;
		}
	}

	@Override
	public boolean ReadPremission(String key, int role) {
		return true;
	}
}
