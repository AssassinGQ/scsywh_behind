package com.Entitys.Trucklog.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.Common.Entitys.Bean;

@Entity
@Table(name="trucklogs")
public class TruckLogs extends Bean {		//维护各车的日志信息对应的数据库表名和日志信息是否在使用
	public final static int STATUS_USING = 0;
	public final static int STATUS_COMPLETE = 1;
	public final static int STATUS_INVAILD = 2;
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="sid", length=10)
	private Long sid_nm_10;
	@Column(name="datastatus", length=4)
	private Integer datastatus_fm_4;
	@Column(name="trucksid", length=10)
	private Long trucksid_fm_10;
	@Column(name="trucknumber", length=10)
	private String trucknumber_fm_30;
	@Column(name="corporationsid", length=5)
	private Long corporationsid_fm_5;
	@Column(name="ordersid", length=10)
	private Long ordersid_fm_10;
	@Column(name="tablename", length=30)	//数据表名（"trucklog_"+trucksid+"_"+ordersid）
	private String tablename_nm_30;
	@Column(name="status", length=4)		//0使用中1已完成（运单完成）2失效（因为车辆信息被删除或运单取消）
	private Integer status_nm_4;
	@Column(name="createdat", length=13)
	private Long createdat_nm_13;
	@Column(name="createdid", length=10)
	private Long createdid_nm_10;
	@Column(name="updatedat", length=13)
	private Long updatedat_nm_13;
	@Column(name="updatedid", length=10)
	private Long updatedid_nm_10;
	public TruckLogs() {
		super();
		this.datastatus_fm_4 = Bean.CREATED;
	}
	public Long getSid_nm_10() {
		return sid_nm_10;
	}
	public void setSid_nm_10(Long sid_nm_10) {
		this.sid_nm_10 = sid_nm_10;
	}
	public Long getTrucksid_fm_10() {
		return trucksid_fm_10;
	}
	public void setTrucksid_fm_10(Long trucksid_fm_10) {
		this.trucksid_fm_10 = trucksid_fm_10;
	}
	public String getTrucknumber_fm_30() {
		return trucknumber_fm_30;
	}
	public void setTrucknumber_fm_30(String trucknumber_fm_30) {
		this.trucknumber_fm_30 = trucknumber_fm_30;
	}
	public Long getCorporationsid_fm_5() {
		return corporationsid_fm_5;
	}
	public void setCorporationsid_fm_5(Long corporationsid_fm_5) {
		this.corporationsid_fm_5 = corporationsid_fm_5;
	}
	public Long getOrdersid_fm_10() {
		return ordersid_fm_10;
	}
	public void setOrdersid_fm_10(Long ordersid_fm_10) {
		this.ordersid_fm_10 = ordersid_fm_10;
	}
	public String getTablename_nm_30() {
		return tablename_nm_30;
	}
	public void setTablename_nm_30(String tablename_nm_30) {
		this.tablename_nm_30 = tablename_nm_30;
	}
	public Integer getStatus_nm_4() {
		return status_nm_4;
	}
	public void setStatus_nm_4(Integer status_nm_4) {
		this.status_nm_4 = status_nm_4;
	}
	public Integer getDatastatus_fm_4() {
		return datastatus_fm_4;
	}
	public void setDatastatus_fm_4(Integer datastatus_fm_4) {
		this.datastatus_fm_4 = datastatus_fm_4;
	}
	public Long getCreatedat_nm_13() {
		return createdat_nm_13;
	}
	public void setCreatedat_nm_13(Long createdat_nm_13) {
		this.createdat_nm_13 = createdat_nm_13;
	}
	public Long getCreatedid_nm_10() {
		return createdid_nm_10;
	}
	public void setCreatedid_nm_10(Long createdid_nm_10) {
		this.createdid_nm_10 = createdid_nm_10;
	}
	public Long getUpdatedat_nm_13() {
		return updatedat_nm_13;
	}
	public void setUpdatedat_nm_13(Long updatedat_nm_13) {
		this.updatedat_nm_13 = updatedat_nm_13;
	}
	public Long getUpdatedid_nm_10() {
		return updatedid_nm_10;
	}
	public void setUpdatedid_nm_10(Long updatedid_nm_10) {
		this.updatedid_nm_10 = updatedid_nm_10;
	}
	@Override
	public boolean IsDeleted() {
		return this.datastatus_fm_4 == Bean.DELETED;
	}
	@Override
	public void SetDeleted() {
		this.datastatus_fm_4 = Bean.DELETED;
	}
	@Override
	public void setSid(Long sid){
		this.sid_nm_10 = sid;
	}
	@Override
	public Long getSid() {
		return this.sid_nm_10;
	}
}
