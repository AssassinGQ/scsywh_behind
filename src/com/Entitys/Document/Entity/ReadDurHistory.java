package com.Entitys.Document.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;

@Entity
@Table(name="readdurhistory")
public class ReadDurHistory extends Bean {
	@Column(name="driver_sid", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long driver_sid;
	@Column(name="doc_sid", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long doc_sid;
	@Column(name="drivername", length=10)
	@Valid(varType = VarType.String, maxLength = 30)
	private String drivername;
	@Column(name="docname", length=10)
	@Valid(varType = VarType.String, maxLength = 30)
	private String docname;
	@Column(name="dur", length=13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long dur;
	public ReadDurHistory() {
		super();
	}
	public ReadDurHistory(Long driver_sid, Long doc_sid, Long dur) {
		super();
		this.driver_sid = driver_sid;
		this.doc_sid = doc_sid;
		this.dur = dur;
	}
	public Long getDriver_sid() {
		return driver_sid;
	}
	public void setDriver_sid(Long driver_sid) {
		this.driver_sid = driver_sid;
	}
	public Long getDoc_sid() {
		return doc_sid;
	}
	public void setDoc_sid(Long doc_sid) {
		this.doc_sid = doc_sid;
	}
	public Long getDur() {
		return dur;
	}
	public void setDur(Long dur) {
		this.dur = dur;
	}

	public String getDrivername() {
		return drivername;
	}

	public void setDrivername(String drivername) {
		this.drivername = drivername;
	}

	public String getDocname() {
		return docname;
	}

	public void setDocname(String docname) {
		this.docname = docname;
	}
}
