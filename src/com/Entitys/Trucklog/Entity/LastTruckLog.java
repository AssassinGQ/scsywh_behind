package com.Entitys.Trucklog.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;
import com.Common.Enums.SnowFlake_DataID;
import com.Common.Utils.SnowFlake;

@Entity
@Table(name = "lasttrucklog")
public class LastTruckLog extends Bean {
	public final static int HASWARN_NO = 0;

	public final static int HASWARN_YES = 1;
	@Column(name = "trucklogsid", length = 20)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 99999999999999999999D)
	private Long trucklogsid;
	@Column(name = "trucksid", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long trucksid;
	@Column(name = "ordersid", length = 20)
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
	private String battery; // jsonarray
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
	private Integer haswarn; // 0无异常1有异常
	@Column(name = "warnsid", length = 20)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 99999999999999999999D)
	private Long warnsid;

	public LastTruckLog() {
		super();
	}

	public LastTruckLog(TruckLog truckLog) {
		this.trucklogsid = truckLog.getSid();
		this.trucksid = truckLog.getTrucksid();
		this.ordersid = truckLog.getOrdersid();
		this.corporationsid = truckLog.getCorporationsid();
		this.datastatus = truckLog.getDatastatus();
		this.time = truckLog.getTime();
		this.distance = truckLog.getDistance();
		this.gpsx = truckLog.getGpsx();
		this.gpsy = truckLog.getGpsy();
		this.speed = truckLog.getSpeed();
		this.battery = truckLog.getBattery();
		this.fuelvol = truckLog.getFuelvol();
		this.lefttirepressure = truckLog.getLefttirepressure();
		this.righttirepressure = truckLog.getRighttirepressure();
		this.lefttiretemp = truckLog.getLefttiretemp();
		this.righttiretemp = truckLog.getRighttiretemp();
		this.lock = truckLog.getLock();
		this.posture = truckLog.getPosture();
		this.haswarn = truckLog.getHaswarn();
		this.warnsid = truckLog.getWarnsid();
		this.createdat = truckLog.getCreatedat();
		this.createdid = truckLog.getCreatedid();
		this.updatedat = truckLog.getUpdatedat();
		this.updatedid = truckLog.getUpdatedid();
		SnowFlake snowFlake = new SnowFlake(0, SnowFlake_DataID.getEnum("LastTruckLog").getDataId());
		this.sid = snowFlake.nextId();
	}
	
	public void updateFromTruckLog(TruckLog truckLog){
		this.trucklogsid = truckLog.getSid();
		this.trucksid = truckLog.getTrucksid();
		this.ordersid = truckLog.getOrdersid();
		this.corporationsid = truckLog.getCorporationsid();
		this.datastatus = truckLog.getDatastatus();
		this.time = truckLog.getTime();
		this.distance = truckLog.getDistance();
		this.gpsx = truckLog.getGpsx();
		this.gpsy = truckLog.getGpsy();
		this.speed = truckLog.getSpeed();
		this.battery = truckLog.getBattery();
		this.fuelvol = truckLog.getFuelvol();
		this.lefttirepressure = truckLog.getLefttirepressure();
		this.righttirepressure = truckLog.getRighttirepressure();
		this.lefttiretemp = truckLog.getLefttiretemp();
		this.righttiretemp = truckLog.getRighttiretemp();
		this.lock = truckLog.getLock();
		this.posture = truckLog.getPosture();
		this.haswarn = truckLog.getHaswarn();
		this.warnsid = truckLog.getWarnsid();
		this.createdat = truckLog.getCreatedat();
		this.createdid = truckLog.getCreatedid();
		this.updatedat = truckLog.getUpdatedat();
		this.updatedid = truckLog.getUpdatedid();
	}
	
	public TruckLog getTruckLog(){
		TruckLog truckLog = new TruckLog();
		truckLog.setSid(this.getTrucklogsid());
		truckLog.setTrucksid(this.getTrucksid());
		truckLog.setOrdersid(this.ordersid);
		truckLog.setCorporationsid(this.getCorporationsid());
		truckLog.setDatastatus(this.getDatastatus());
		truckLog.setTime(this.getTime());
		truckLog.setDistance(this.getDistance());
		truckLog.setGpsx(this.getGpsx());
		truckLog.setGpsy(this.getGpsy());
		truckLog.setSpeed(this.getSpeed());
		truckLog.setBattery(this.getBattery());
		truckLog.setFuelvol(this.getFuelvol());
		truckLog.setLefttirepressure(this.getLefttirepressure());
		truckLog.setRighttirepressure(this.getRighttirepressure());
		truckLog.setLefttiretemp(this.getLefttiretemp());
		truckLog.setRighttiretemp(this.getRighttiretemp());
		truckLog.setLock(this.getLock());
		truckLog.setPosture(this.getPosture());
		truckLog.setHaswarn(this.getHaswarn());
		truckLog.setWarnsid(this.getWarnsid());
		truckLog.setCreatedat(this.getCreatedat());
		truckLog.setCreatedid(this.getCreatedid());
		truckLog.setUpdatedat(this.getUpdatedat());
		truckLog.setUpdatedid(this.getUpdatedid());
		return truckLog;
	}

	public Long getTrucklogsid() {
		return trucklogsid;
	}

	public void setTrucklogsid(Long trucklogsid) {
		this.trucklogsid = trucklogsid;
	}

	public Long getTrucksid() {
		return trucksid;
	}

	public void setTrucksid(Long trucksid) {
		this.trucksid = trucksid;
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
