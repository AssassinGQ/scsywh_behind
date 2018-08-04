package com.Entitys.Statistics;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;

//加减次数，查询
@Entity
@Table(name = "truckmaintainstatistics")
public class TruckMaintainStatistics  extends Bean {
	@Column(name = "trucknumber", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String trucknumber;
	@Column(name = "maintaintimes", length = 4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer maintaintimes;
	@Column(name = "zjjcrq", length = 13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long zjjcrq;	//最近进厂日期
	@Column(name = "zjccrq", length = 13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long zjccrq;	//最近出厂日期
	public TruckMaintainStatistics() {
		super();
		this.maintaintimes = 0;
	}
	public String getTrucknumber() {
		return trucknumber;
	}
	public void setTrucknumber(String trucknumber) {
		this.trucknumber = trucknumber;
	}
	public Integer getMaintaintimes() {
		return maintaintimes;
	}
	public void setMaintaintimes(Integer maintaintimes) {
		this.maintaintimes = maintaintimes;
	}
	public Long getZjjcrq() {
		return zjjcrq;
	}
	public void setZjjcrq(Long zjjcrq) {
		this.zjjcrq = zjjcrq;
	}
	public Long getZjccrq() {
		return zjccrq;
	}
	public void setZjccrq(Long zjccrq) {
		this.zjccrq = zjccrq;
	}
	public void addmaintain(int n){
		this.maintaintimes += n;
	}
	public void submaintain(int n){
		this.maintaintimes -= n;
		if(this.maintaintimes < 0)
			this.maintaintimes = 0;
	}
	public void addmaintain(){
		addmaintain(1);
	}
	public void submaintain(){
		submaintain(1);
	}
}
