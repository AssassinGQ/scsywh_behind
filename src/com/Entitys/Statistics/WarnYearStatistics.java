package com.Entitys.Statistics;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;
import com.Entitys.Warn.Entity.Warn;

@Entity
@Table(name = "warnyearstatistics")
public class WarnYearStatistics  extends Bean {
	@Column(name = "year", length = 4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private String year;
	@Column(name = "objectsid", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long objectsid;
	@Column(name = "objecttype", length = 4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer objecttype;
	@Column(name="objectname", length=30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String objectname;
	@Column(name = "lockamount", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long lockamount;
	@Column(name = "leakamount", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long leakamount;
	@Column(name = "tireamount", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long tireamount;
	@Column(name = "fuelamount", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long fuelamount;
	@Column(name = "overspeedamount", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long overspeedamount;
	@Column(name = "parkamount", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long parkamount;
	@Column(name = "fatiguedrivingamount", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long fatiguedrivingamount;
	@Column(name = "suddenbrakeamount", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long suddenbrakeamount;
	@Column(name = "suddenaccelamount", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long suddenaccelamount;
	@Column(name = "accidentamount", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long accidentamount;
	@Column(name = "overloadamount", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long overloadamount;

	public WarnYearStatistics() {
		super();
	}

	public WarnYearStatistics(Long corporationsid, String year, Long objectsid, Integer objecttype) {
		super();
		this.corporationsid = corporationsid;
		this.year = year;
		this.objectsid = objectsid;
		this.objecttype = objecttype;
		this.lockamount = Long.parseLong("0");
		this.leakamount = Long.parseLong("0");
		this.tireamount = Long.parseLong("0");
		this.fuelamount = Long.parseLong("0");
		this.overspeedamount = Long.parseLong("0");
		this.parkamount = Long.parseLong("0");
		this.fatiguedrivingamount = Long.parseLong("0");
		this.suddenbrakeamount = Long.parseLong("0");
		this.suddenaccelamount = Long.parseLong("0");
		this.accidentamount = Long.parseLong("0");
		this.overloadamount = Long.parseLong("0");
	}

	public String getObjectname() {
		return objectname;
	}

	public void setObjectname(String objectname) {
		this.objectname = objectname;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public Long getObjectsid() {
		return objectsid;
	}

	public void setObjectsid(Long objectsid) {
		this.objectsid = objectsid;
	}

	public Integer getObjecttype() {
		return objecttype;
	}

	public void setObjecttype(Integer objecttype) {
		this.objecttype = objecttype;
	}

	public Long getLockamount() {
		return lockamount;
	}

	public void setLockamount(Long lockamount) {
		this.lockamount = lockamount;
	}

	public Long getLeakamount() {
		return leakamount;
	}

	public void setLeakamount(Long leakamount) {
		this.leakamount = leakamount;
	}

	public Long getTireamount() {
		return tireamount;
	}

	public void setTireamount(Long tireamount) {
		this.tireamount = tireamount;
	}

	public Long getFuelamount() {
		return fuelamount;
	}

	public void setFuelamount(Long fuelamount) {
		this.fuelamount = fuelamount;
	}

	public Long getOverspeedamount() {
		return overspeedamount;
	}

	public void setOverspeedamount(Long overspeedamount) {
		this.overspeedamount = overspeedamount;
	}

	public Long getParkamount() {
		return parkamount;
	}

	public void setParkamount(Long parkamount) {
		this.parkamount = parkamount;
	}

	public Long getFatiguedrivingamount() {
		return fatiguedrivingamount;
	}

	public void setFatiguedrivingamount(Long fatiguedrivingamount) {
		this.fatiguedrivingamount = fatiguedrivingamount;
	}

	public Long getSuddenbrakeamount() {
		return suddenbrakeamount;
	}

	public void setSuddenbrakeamount(Long suddenbrakeamount) {
		this.suddenbrakeamount = suddenbrakeamount;
	}

	public Long getSuddenaccelamount() {
		return suddenaccelamount;
	}

	public void setSuddenaccelamount(Long suddenaccelamount) {
		this.suddenaccelamount = suddenaccelamount;
	}

	public Long getAccidentamount() {
		return accidentamount;
	}

	public void setAccidentamount(Long accidentamount) {
		this.accidentamount = accidentamount;
	}

	public Long getOverloadamount() {
		return overloadamount;
	}

	public void setOverloadamount(Long overloadamount) {
		this.overloadamount = overloadamount;
	}

	public void addWarn(int warntype, int amount) {
		switch (warntype) {
		case Warn.WARNTYPE_LOCK:
			this.lockamount += amount;
			break;
		case Warn.WARNTYPE_LEAK:
			this.leakamount += amount;
			break;
		case Warn.WARNTYPE_TIRE:
			this.tireamount += amount;
			break;
		case Warn.WARNTYPE_FUEL:
			this.fuelamount += amount;
			break;
		case Warn.WARNTYPE_OVERSPEED:
			this.overspeedamount += amount;
			break;
		case Warn.WARNTYPE_PARK:
			this.parkamount += amount;
			break;
		case Warn.WARNTYPE_FATIGUEDRIVING:
			this.fatiguedrivingamount += amount;
			break;
		case Warn.WARNTYPE_SUDDENBRAKE:
			this.suddenbrakeamount += amount;
			break;
		case Warn.WARNTYPE_SUDDENACCELERATE:
			this.suddenaccelamount += amount;
			break;
		case Warn.WARNTYPE_ACCIDENT:
			this.accidentamount += amount;
			break;
		case Warn.WARNTYPE_OVERLOAD:
			this.overloadamount += amount;
			break;
		default:
			break;
		}
	}

	public void addWarn(int warntype) {
		addWarn(warntype, 1);
	}
}
