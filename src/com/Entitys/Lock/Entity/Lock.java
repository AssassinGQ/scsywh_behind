package com.Entitys.Lock.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;

@Entity
@Table(name="lockinfo")
public class Lock  extends Bean {
	public final static int STATUS_REQUESTED = 0;
	public final static int STATUS_RESPONSED = 1;
	//public final static int STATUS_PUSHED = 2;
	public final static int STATUS_OPERATED = 2;
	@Column(name="corporationname", length=30)
	@Valid(varType = VarType.String, maxLength = 20)
	private String corporationname;
	@Column(name="requestat", length=13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long requestat;
	@Column(name="requestfrom", length=10)//请求的司机的sid
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long requestfrom;
	@Column(name="drivername", length=30)
	@Valid(varType = VarType.String, maxLength = 30)
	private String drivername;
	@Column(name="trucknumber", length=30)
	@Valid(varType = VarType.String, maxLength = 30)
	private String trucknumber;
	@Column(name="request", length=4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer request;
	@Column(name="requestdesc", length=200)
	@Valid(varType = VarType.String, maxLength = 200)
	private String requestdesc;
	@Column(name="responseat", length=13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long responseat;
	@Column(name="response", length=4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer response;
	@Column(name="operatedat", length=13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long operatedat;
	@Column(name="operate", length=4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer operate;
	@Column(name="status", length=4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer status;
	public Lock() {
		super();
		this.status = STATUS_REQUESTED;
	}
	public String getCorporationname() {
		return corporationname;
	}
	public void setCorporationname(String corporationname) {
		this.corporationname = corporationname;
	}
	public String getDrivername() {
		return drivername;
	}
	public void setDrivername(String drivername) {
		this.drivername = drivername;
	}
	public String getTrucknumber() {
		return trucknumber;
	}
	public void setTrucknumber(String trucknumber) {
		this.trucknumber = trucknumber;
	}
	public String getRequestdesc() {
		return requestdesc;
	}
	public void setRequestdesc(String requestdesc) {
		this.requestdesc = requestdesc;
	}
	public Long getRequestat() {
		return requestat;
	}
	public void setRequestat(Long requestat) {
		this.requestat = requestat;
	}
	public Long getRequestfrom() {
		return requestfrom;
	}
	public void setRequestfrom(Long requestfrom) {
		this.requestfrom = requestfrom;
	}
	public Integer getRequest() {
		return request;
	}
	public void setRequest(Integer request) {
		this.request = request;
	}
	public Long getResponseat() {
		return responseat;
	}
	public void setResponseat(Long responseat) {
		this.responseat = responseat;
	}
	public Integer getResponse() {
		return response;
	}
	public void setResponse(Integer response) {
		this.response = response;
	}
	public Long getOperatedat() {
		return operatedat;
	}
	public void setOperatedat(Long operatedat) {
		this.operatedat = operatedat;
	}
	public Integer getOperate() {
		return operate;
	}
	public void setOperate(Integer operate) {
		this.operate = operate;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
}
