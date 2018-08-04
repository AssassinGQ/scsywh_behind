package com.Entitys.Warn.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.json.JSONArray;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;

@Entity
@Table(name="warn")
public class Warn extends Bean {
	public final static int STATUS_CREATED = 0;
	public final static int STATUS_PUSHED = 1;
	public final static int STATUS_RECEIVED = 2;//司机已经收到
	public final static int STATUS_HANDLED = 3;//司机已经上传处理结果
	
	public final static int WARNTYPE_LOCK = 1;
	public final static int WARNTYPE_LEAK = 2;
	public final static int WARNTYPE_TIRE = 3;
	public final static int WARNTYPE_FUEL = 4;
	public final static int WARNTYPE_OVERSPEED = 5;
	public final static int WARNTYPE_PARK = 6;
	public final static int WARNTYPE_FATIGUEDRIVING = 7;
	public final static int WARNTYPE_SUDDENBRAKE = 8;
	public final static int WARNTYPE_SUDDENACCELERATE = 9;
	public final static int WARNTYPE_ACCIDENT = 10;
	public final static int WARNTYPE_OVERLOAD = 11;
	public final static int WARNTYPE_LOWBATTERY = 12;
	
	@Column(name="corporationname", length=30)
	@Valid(varType = VarType.String, maxLength = 30)
	private String corporationname;
	@Column(name="trucksid", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long trucksid;
	@Column(name="trucknumber", length=30)
	@Valid(varType = VarType.String, maxLength = 30)
	private String trucknumber;
	@Column(name="driversid", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long driversid;
	@Column(name="drivername", length=30)
	@Valid(varType = VarType.String, maxLength = 30)
	private String drivername;
	@Column(name="ordersid", length=20)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 99999999999999999999D)
	private Long ordersid;
	@Column(name="status", length=4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer status;
	@Column(name="warntype", length=4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private	Integer warntype;
	@Column(name="warntime", length=13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long warntime;
	@Column(name="gpsx", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double gpsx;
	@Column(name="gpsy", length=13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Double gpsy;
	@Column(name="warnvalue", length=30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String warnvalue;
	@Column(name = "warnimages")
	@Valid(needValid = false)
	private String warnimages; // 上传的图像的sid
	
	@Column(name="warndriverresp")
	@Valid(needValid = false)
	private String warndriverresp;
	@Column(name="warndriverimages")
	@Valid(needValid = false)
	private String warndriverimages;
	
	public Warn() {
		super();
	}
	public String getCorporationname() {
		return corporationname;
	}
	public void setCorporationname(String corporationname) {
		this.corporationname = corporationname;
	}
	public Long getTrucksid() {
		return trucksid;
	}
	public void setTrucksid(Long trucksid) {
		this.trucksid = trucksid;
	}
	public String getTrucknumber() {
		return trucknumber;
	}
	public void setTrucknumber(String trucknumber) {
		this.trucknumber = trucknumber;
	}
	public String getDrivername() {
		return drivername;
	}
	public void setDrivername(String drivername) {
		this.drivername = drivername;
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
	public String getWarnvalue() {
		return warnvalue;
	}
	public void setWarnvalue(String warnvalue) {
		this.warnvalue = warnvalue;
	}
	public Long getDriversid() {
		return driversid;
	}
	public void setDriversid(Long driversid) {
		this.driversid = driversid;
	}
	public Long getOrdersid() {
		return ordersid;
	}
	public void setOrdersid(Long ordersid) {
		this.ordersid = ordersid;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getWarntype() {
		return warntype;
	}
	public void setWarntype(Integer warntype) {
		this.warntype = warntype;
	}
	public Long getWarntime() {
		return warntime;
	}
	public void setWarntime(Long warntime) {
		this.warntime = warntime;
	}
	public String getWarndriverresp() {
		return warndriverresp;
	}
	public void setWarndriverresp(String warndriverresp) {
		this.warndriverresp = warndriverresp;
	}
	public String getWarnimages() {
		return warnimages;
	}
	public void setWarnimages(String warnimages) {
		this.warnimages = warnimages;
	}
	public String getWarndriverimages() {
		return warndriverimages;
	}
	public void setWarndriverimages(String warndriverimages) {
		this.warndriverimages = warndriverimages;
	}
	public boolean adduploadimage(Long sid){
		JSONArray jsonArray = null;
		if(this.warnimages == null)
			jsonArray = new JSONArray();
		else {
			try {
				jsonArray = new JSONArray(this.warnimages);
			} catch (Exception e) {
				return false;
			}
		}
		jsonArray.put(sid);
		this.warnimages = jsonArray.toString();
		return true;
	}
	public boolean adddriverimage(Long sid){
		JSONArray jsonArray = null;
		if(this.warndriverimages == null)
			jsonArray = new JSONArray();
		else {
			try {
				jsonArray = new JSONArray(this.warndriverimages);
			} catch (Exception e) {
				return false;
			}
		}
		jsonArray.put(sid);
		this.warndriverimages = jsonArray.toString();
		return true;
	}
	public boolean removedriverimage(Long sid){
		if(sid == null)
			return false;
		if(this.warndriverimages == null)
			return false;
		else {
			try {
				JSONArray jsonArray = new JSONArray(this.warndriverimages);
				Integer index = null;
				for(int i = 0; i < jsonArray.length(); i++){
					if(sid.equals(jsonArray.getLong(i))){
						index = i;
						break;
					}
				}
				if(index == null)
					return false;
				else{
					jsonArray.remove(index);
					this.warndriverimages = jsonArray.toString();
					return true;
				}
			} catch (Exception e) {
				return false;
			}
		}
	}
	
}
