package com.Entitys.Document.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;

@Entity
@Table(name="totaldur")
public class TotalDur extends Bean {
	@Column(name="driver_sid", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long driver_sid;
	@Column(name="totaldur", length=13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long totaldur;
	public TotalDur() {
		super();
	}
	public TotalDur(Long driver_sid, Long totaldur) {
		super();
		this.driver_sid = driver_sid;
		this.totaldur = totaldur;
	}
	public Long getDriver_sid() {
		return driver_sid;
	}
	public void setDriver_sid(Long driver_sid) {
		this.driver_sid = driver_sid;
	}
	public Long getTotaldur() {
		return totaldur;
	}
	public void setTotaldur(Long totaldur) {
		this.totaldur = totaldur;
	}
	public void AddTotalDur(Long dur){
		this.totaldur += dur;
	}
}
