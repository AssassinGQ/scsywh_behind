package com.Entitys.Truck.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.BasicInfo;
import com.Common.Entitys.LoginableBasicInfo;

@Entity
@Table(name="truck")
public class Truck extends BasicInfo {
	public final static int TRUCKSTATUS_IDLE = 0;
	public final static int TRUCKSTATUS_TASK = 1;
	public final static int TRUCKSTATUS_INVAILD = 2;
//	private static int LOCKSTATUS_OPEN = 0;
//	private static int LOCKSTATUS_LOCK = 1;
//	private static String LOCKPOSITION_UPFRONT = "up_front";
//	private static String LOCKPOSITION_UPMIDDLE = "up_middle";
//	private static String LOCKPOSITION_UPBACK = "up_back";
//	private static String LOCKPOSITION_DOWNLEFT = "down_left";
//	private static String LOCKPOSITION_DOWNRIGHT = "down_right";
	@Column(name="trucknumber", length=20)
	@Valid(varType = VarType.String, maxLength = 20)
	private String trucknumber;
	@Column(name="truckstatus", length=4)	//0空闲1任务中2失效
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer truckstatus;
	@Column(name="trucktype", length=4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer trucktype;
	@Column(name="model", length=20)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String model;
	@Column(name="weight", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double weight;
	@Column(name="vol", length=10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double vol;
//	@Column(name="lock", length=100)
//	private String lock;
	@Column(name="cid", length=32)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 32)
	private String cid;
	
	@Column(name="RTCnumber", length=20)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String RTCnumber;
	@Column(name="RTCtime", length=13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long RTCtime;
	@Column(name="RTCddl", length=13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long RTCddl;
	@Column(name="RTCorganization", length=20)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 20)
	private String RTCorganization;
	@Column(name="insurancemoney", length=10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double insurancemoney;
	@Column(name="insuranceddl", length=13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long insuranceddl;
	
	@Column(name="driversid", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long driversid;
	@Column(name="escortsid", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long escortsid;
	@Column(name="trailersid", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long trailersid;
	
	@Column(name="truckarchivessid", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long truckarchivessid;
	@Column(name="truckmaintainstatisticssid", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long truckmaintainstatisticssid;

	public Truck() {
		super();
		this.truckstatus = Truck.TRUCKSTATUS_IDLE;
//		try {
//			JSONObject lockObject = new JSONObject();
//			lockObject.put(LOCKPOSITION_UPFRONT, LOCKSTATUS_LOCK);
//			lockObject.put(LOCKPOSITION_UPMIDDLE, LOCKSTATUS_LOCK);
//			lockObject.put(LOCKPOSITION_UPBACK, LOCKSTATUS_LOCK);
//			lockObject.put(LOCKPOSITION_DOWNLEFT, LOCKSTATUS_LOCK);
//			lockObject.put(LOCKPOSITION_DOWNRIGHT, LOCKSTATUS_LOCK);
//			this.lock = lockObject.toString();
//		} catch (Exception e) {
//			System.err.println("In Truck Construct Method, JSON error");
//		}
	}
	public String getTrucknumber() {
		return trucknumber;
	}
	public void setTrucknumber(String trucknumber) {
		this.trucknumber = trucknumber;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public Integer getTruckstatus() {
		return truckstatus;
	}
	public void setTruckstatus(Integer truckstatus) {
		this.truckstatus = truckstatus;
	}
	public Integer getTrucktype() {
		return trucktype;
	}
	public void setTrucktype(Integer trucktype) {
		this.trucktype = trucktype;
	}
	public Double getWeight() {
		return weight;
	}
	public void setWeight(Double weight) {
		this.weight = weight;
	}
	public Double getVol() {
		return vol;
	}
	public void setVol(Double vol) {
		this.vol = vol;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
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
	public Double getInsurancemoney() {
		return insurancemoney;
	}
	public void setInsurancemoney(Double insurancemoney) {
		this.insurancemoney = insurancemoney;
	}
	public Long getInsuranceddl() {
		return insuranceddl;
	}
	public void setInsuranceddl(Long insuranceddl) {
		this.insuranceddl = insuranceddl;
	}

	public Long getDriversid() {
		return driversid;
	}

	public void setDriversid(Long driversid) {
		this.driversid = driversid;
	}

	public Long getEscortsid() {
		return escortsid;
	}

	public void setEscortsid(Long escortsid) {
		this.escortsid = escortsid;
	}

	public Long getTrailersid() {
		return trailersid;
	}

	public void setTrailersid(Long trailersid) {
		this.trailersid = trailersid;
	}

	public Long getTruckarchivessid() {
		return truckarchivessid;
	}
	public void setTruckarchivessid(Long truckarchivessid) {
		this.truckarchivessid = truckarchivessid;
	}
	public Long getTruckmaintainstatisticssid() {
		return truckmaintainstatisticssid;
	}
	public void setTruckmaintainstatisticssid(Long truckmaintainstatisticssid) {
		this.truckmaintainstatisticssid = truckmaintainstatisticssid;
	}
	@Override
	public boolean WritePremission(String key, int role) {
		switch (key) {
		case "sid":
		case "cid":
		case "truckstatus":
		case "createdat":
		case "createdid":
		case "updatedat":
		case "updatedid":
		case "truckarchivessid":
		case "truckmaintainstatisticssid":
			return role >= LoginableBasicInfo.ROLE_SYSTEM;
		case "corporationsid":
			return role >= LoginableBasicInfo.ROLE_SUPER;
		case "trucknumber":
		case "trucktype":
		case "model":
		case "weight":
		case "vol":
		case "RTCnumber":
		case "RTCtime":
		case "RTCddl":
		case "RTCorganization":
		case "insurancemoney":
		case "insuranceddl":
		case "driversid":
		case "escortsid":
		case "trailersid":
			return role >= LoginableBasicInfo.ROLE_ADMIN;
		default:
			return false;
		}
	}
	@Override
	public boolean ReadPremission(String key, int role) {
		return true;
//		switch (key) {
//		case "sid":
//		case "createdat":
//		case "createdid":
//		case "updatedat":
//		case "updatedid":
//			return role >= LoginableBasicInfo.ROLE_ADMIN;
//		case "corporationsid":
//		case "username":
//		case "password":
//		case "phone":
//		case "name":
//		case "manname":
//		case "address":
//			return true;
//		default:
//			return false;
//		}
	}
	public boolean setLock(String lock){
		return false;
//		if(lock == null)
//			return false;
//		String[] locks = null;
//		try {
//			locks = lock.split("-");
//		} catch (Exception e) {
//			return false;
//		}
//		if(locks == null)
//			return false;
//		if(locks.length != 5)
//			return false;
//		int[] intlocks = new int[5]; 
//		for(int i = 0; i < 5; i++){
//			try {
//				int tmp = Integer.parseInt(locks[i]);
//				if(tmp == LOCKSTATUS_LOCK)
//					intlocks[i] = LOCKSTATUS_LOCK;
//				else if(tmp == LOCKSTATUS_OPEN)
//					intlocks[i] = LOCKSTATUS_OPEN;
//				else
//					return false;
//			} catch (Exception e) {
//				return false;
//			}
//		}
//		JSONObject lockObject = new JSONObject();
//		try {
//			lockObject.put(LOCKPOSITION_UPFRONT, intlocks[0]);
//			lockObject.put(LOCKPOSITION_UPMIDDLE, intlocks[1]);
//			lockObject.put(LOCKPOSITION_UPBACK, intlocks[2]);
//			lockObject.put(LOCKPOSITION_DOWNLEFT, intlocks[3]);
//			lockObject.put(LOCKPOSITION_DOWNRIGHT, intlocks[4]);
//			this.lock = lockObject.toString();
//			return true;
//		} catch (Exception e) {
//			return false;
//		}
		
	}
}
