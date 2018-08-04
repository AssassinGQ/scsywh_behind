package com.Entitys.Statistics;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;

@Entity
@Table(name = "orderyearstatistics")
public class OrderYearStatistics  extends Bean {
	@Column(name="year", length=4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private String year;
	@Column(name="objectsid", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long objectsid;
	@Column(name="objecttype", length=4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer objecttype;
	@Column(name="objectname", length=30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String objectname;
	@Column(name="orderamount", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long orderamount;
	@Column(name="fuelused", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double fuelused;
	@Column(name="distance", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double distance;
	@Column(name="output", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double output;
	public OrderYearStatistics() {
		super();
	}
	public OrderYearStatistics(Long corporationsid, String year, Long objectsid, Integer objecttype) {
		super();
		this.corporationsid = corporationsid;
		this.year = year;
		this.objectsid = objectsid;
		this.objecttype = objecttype;
		this.orderamount = Long.parseLong("0");
		this.fuelused = Double.parseDouble("0");
		this.distance = Double.parseDouble("0");
		this.output = Double.parseDouble("0");
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
	public Long getOrderamount() {
		return orderamount;
	}
	public void setOrderamount(Long orderamount) {
		this.orderamount = orderamount;
	}
	public Double getFuelused() {
		return fuelused;
	}
	public void setFuelused(Double fuelused) {
		this.fuelused = fuelused;
	}
	public Double getDistance() {
		return distance;
	}
	public void setDistance(Double distance) {
		this.distance = distance;
	}
	public Double getOutput() {
		return output;
	}
	public void setOutput(Double output) {
		this.output = output;
	}
	public void addOrderAmount(int i){
		this.orderamount += i;
	}
	public void addOrderAmount(){
		addOrderAmount(1);
	}
	public void addFuelUsed(Double fuelused){
		this.fuelused += fuelused;
	}
	public void addDistance(Double distance){
		this.distance += distance;
	}
	public void addOutput(Double output){
		this.output += output;
	}
}
