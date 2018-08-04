package com.Entitys.Trucklog.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;

@Entity
@Table(name = "trucklog")
public class TruckLog extends Bean {
	public final static int HASWARN_NO = 0;
	
	public final static int HASWARN_YES = 1;
	@Column(name = "trucksid", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long trucksid;
	@Column(name="ordersid", length=20)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 99999999999999999999D)
	private Long ordersid;
	@Column(name = "time", length = 13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long time;
	@Column(name = "distance", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double distance;
	@Column(name = "gpsx", length = 13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Double gpsx;
	@Column(name = "gpsy", length = 13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Double gpsy;
	@Column(name = "speed", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double speed;
	@Column(name = "battery", length = 10)
	@Valid(varType = VarType.String, maxLength = 50)
	private String battery;	//	jsonarray
	@Column(name = "fuelvol", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double fuelvol;
	@Column(name = "lefttirepressure", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double lefttirepressure;
	@Column(name = "righttirepressure", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double righttirepressure;
	@Column(name = "lefttiretemp", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double lefttiretemp;
	@Column(name = "righttiretemp", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double righttiretemp;
	@Column(name = "lock", length = 10)
	@Valid(varType = VarType.String, maxLength = 50)
	private String lock;
	@Column(name = "posture", length = 200)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 200)
	private String posture;
	@Column(name = "haswarn", length = 4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer haswarn;		//0无异常1有异常
	@Column(name = "warnsid", length = 20)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 99999999999999999999D)
	private Long warnsid;
	public TruckLog() {
		super();
	}
	public Long getTrucksid() {
		return trucksid;
	}
	public void setTrucksid(Long trucksid) {
		this.trucksid = trucksid;
	}
	public Long getCorporationsid() {
		return corporationsid;
	}
	public void setCorporationsid(Long corporationsid) {
		this.corporationsid = corporationsid;
	}
	public Long getOrdersid() {
		return ordersid;
	}

	public void setOrdersid(Long ordersid) {
		this.ordersid = ordersid;
	}

	public Double getDistance() {
		return distance;
	}
	public void setDistance(Double distance) {
		this.distance = distance;
	}
	public String getBattery() {
		return battery;
	}
	public void setBattery(String battery) {
		this.battery = battery;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public Double getGpsx() {
		return gpsx;
	}
	public void setGpsx(Double gpsx) {
		this.gpsx = gpsx;
	}
	public Double getGpsy() {
		return gpsy;
	}
	public void setGpsy(Double gpsy) {
		this.gpsy = gpsy;
	}
	public Double getSpeed() {
		return speed;
	}
	public void setSpeed(Double speed) {
		this.speed = speed;
	}
	public Double getFuelvol() {
		return fuelvol;
	}
	public void setFuelvol(Double fuelvol) {
		this.fuelvol = fuelvol;
	}
	public Double getLefttirepressure() {
		return lefttirepressure;
	}
	public void setLefttirepressure(Double lefttirepressure) {
		this.lefttirepressure = lefttirepressure;
	}
	public Double getRighttirepressure() {
		return righttirepressure;
	}
	public void setRighttirepressure(Double righttirepressure) {
		this.righttirepressure = righttirepressure;
	}
	public Double getLefttiretemp() {
		return lefttiretemp;
	}
	public void setLefttiretemp(Double lefttiretemp) {
		this.lefttiretemp = lefttiretemp;
	}
	public Double getRighttiretemp() {
		return righttiretemp;
	}
	public void setRighttiretemp(Double righttiretemp) {
		this.righttiretemp = righttiretemp;
	}
	public String getLock() {
		return lock;
	}
	public void setLock(String lock) {
		this.lock = lock;
	}
	public String getPosture() {
		return posture;
	}
	public void setPosture(String posture) {
		this.posture = posture;
	}
	public Integer getHaswarn() {
		return haswarn;
	}
	public void setHaswarn(Integer haswarn) {
		this.haswarn = haswarn;
	}
	public Long getWarnsid() {
		return warnsid;
	}
	public void setWarnsid(Long warnsid) {
		this.warnsid = warnsid;
	}
}
