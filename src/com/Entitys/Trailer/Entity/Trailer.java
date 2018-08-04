package com.Entitys.Trailer.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.BasicInfo;
import com.Common.Entitys.LoginableBasicInfo;

@Entity
@Table(name="trailer")
public class Trailer extends BasicInfo {
	public final static int STATUS_IDLE = 0;
	public final static int STATUS_TASK = 1;
	public final static int STATUS_INVAILD = 2;
	@Column(name="trailernumber", length=20)
	@Valid(varType = VarType.String, maxLength = 20)
	private String trailernumber;
	@Column(name="trailerstatus", length=4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer trailerstatus;
	@Column(name="model", length=20)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String model;
	@Column(name="weight", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double weight;
	@Column(name="vol", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double vol;

	@Column(name="RTCnumber", length=20)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String RTCnumber;
	@Column(name="RTCtime", length=13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long RTCtime;
	@Column(name="RTCddl", length=13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long RTCddl;
	@Column(name="RTCorganization", length=50)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 50)
	private String RTCorganization;
	@Column(name="businessscope")
	@Valid(nullAble = true, varType = VarType.String, maxLength = 100)
	private String businessscope;
	@Column(name="nextapprovingtime", length=13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long nextapprovingtime;
	public Trailer() {
		super();
		this.trailerstatus = Trailer.STATUS_IDLE;
	}
	public Double getVol() {
		return vol;
	}
	public void setVol(Double vol) {
		this.vol = vol;
	}
	public String getTrailernumber() {
		return trailernumber;
	}
	public void setTrailernumber(String trailernumber) {
		this.trailernumber = trailernumber;
	}
	public Integer getTrailerstatus() {
		return trailerstatus;
	}
	public void setTrailerstatus(Integer trailerstatus) {
		this.trailerstatus = trailerstatus;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public Double getWeight() {
		return weight;
	}
	public void setWeight(Double weight) {
		this.weight = weight;
	}
	public String getRTCnumber() {
		return RTCnumber;
	}
	public void setRTCnumber(String rTCnumber) {
		RTCnumber = rTCnumber;
	}
	public Long getRTCtime() {
		return RTCtime;
	}
	public void setRTCtime(Long rTCtime) {
		RTCtime = rTCtime;
	}
	public Long getRTCddl() {
		return RTCddl;
	}
	public void setRTCddl(Long rTCddl) {
		RTCddl = rTCddl;
	}
	public String getRTCorganization() {
		return RTCorganization;
	}
	public void setRTCorganization(String rTCorganization) {
		RTCorganization = rTCorganization;
	}
	public String getBusinessscope() {
		return businessscope;
	}
	public void setBusinessscope(String businessscope) {
		this.businessscope = businessscope;
	}
	public Long getNextapprovingtime() {
		return nextapprovingtime;
	}
	public void setNextapprovingtime(Long nextapprovingtime) {
		this.nextapprovingtime = nextapprovingtime;
	}
	@Override
	public boolean WritePremission(String key, int role) {
		switch (key) {
		case "sid":
		case "trailerstatus":
		case "createdat":
		case "createdid":
		case "updatedat":
		case "updatedid":
			return role >= LoginableBasicInfo.ROLE_SYSTEM;
		case "corporationsid":
			return role >= LoginableBasicInfo.ROLE_SUPER;
		case "trailernumber":
		case "model":
		case "weight":
		case "vol":
		case "RTCnumber":
		case "RTCtime":
		case "RTCddl":
		case "RTCorganization":
		case "businessscope":
		case "nextapprovingtime":
			return role >= LoginableBasicInfo.ROLE_ADMIN;
		default:
			return false;
		}
	}
	@Override
	public boolean ReadPremission(String key, int role) {
		return true;
	}
}
