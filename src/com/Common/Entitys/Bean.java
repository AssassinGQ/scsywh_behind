package com.Common.Entitys;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Utils.Utils;

@MappedSuperclass
public class Bean {
	public final static int CREATED = 0;
	public final static int DELETED = 1;
	
	public final static int ROLE_SYSTEM = 6;
	public final static int ROLE_SUPER = 5;
	public final static int ROLE_CORPORATION = 4;
	public final static int ROLE_ADMIN = 3;
	public final static int ROLE_USER = 2;
	public final static int ROLE_GOVERNMENT = 1;
	public final static int ROLE_MANUFACTURER = 0;
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sid", length=20)
	@Valid(needValid = false)
	protected Long sid;
	@Column(name = "datastatus", length=4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 1)
	protected Integer datastatus;
	@Column(name = "corporationsid", length=5)
	@Valid(varType = VarType.Number, minValue = -1, maxValue = 99999)
	protected Long corporationsid;
	@Column(name="createdat", length=13)
	@Valid(needValid = false)
	protected Long createdat;
	@Column(name="createdid", length=10)
	@Valid(needValid = false)
	protected Long createdid;
	@Column(name="updatedat", length=13)
	@Valid(needValid = false)
	protected Long updatedat;
	@Column(name="updatedid", length=10)
	@Valid(needValid = false)
	protected Long updatedid;
	public Bean() {
		this.datastatus = CREATED;
	}
	public Long getSid() {
		return sid;
	}
	public void setSid(Long sid) {
		this.sid = sid;
	}
	public Integer getDatastatus() {
		return datastatus;
	}
	public void setDatastatus(Integer datastatus) {
		this.datastatus = datastatus;
	}
	public Long getCorporationsid() {
		return corporationsid;
	}
	public void setCorporationsid(Long corporationsid) {
		this.corporationsid = corporationsid;
	}
	public Long getCreatedat() {
		return createdat;
	}
	public void setCreatedat(Long createdat) {
		this.createdat = createdat;
	}
	public Long getCreatedid() {
		return createdid;
	}
	public void setCreatedid(Long createdid) {
		this.createdid = createdid;
	}
	public Long getUpdatedat() {
		return updatedat;
	}
	public void setUpdatedat(Long updatedat) {
		this.updatedat = updatedat;
	}
	public Long getUpdatedid() {
		return updatedid;
	}
	public void setUpdatedid(Long updatedid) {
		this.updatedid = updatedid;
	}
	public boolean IsDeleted(){
		return this.datastatus == DELETED;
	}
	public void SetDeleted(){
		this.datastatus = DELETED;
	}
	public boolean WritePremission(String key, int role) {
		return true;
	}
	public boolean ReadPremission(String key, int role) {
		return true;
	}
	public void createtime(Long operatesid){
		Long currenttime = Utils.getCurrenttimeMills();
		this.createdat = currenttime;
		this.updatedat = currenttime;
		this.createdid = operatesid;
		this.updatedid = operatesid;
	}
	public void updatetime(Long operatesid){
		Long currenttime = Utils.getCurrenttimeMills();
		this.updatedat = currenttime;
		this.updatedid = operatesid;
	}
	public void createtime(Long operatesid, Long stamp){
		this.createdat = stamp;
		this.updatedat = stamp;
		this.createdid = operatesid;
		this.updatedid = operatesid;
	}
	public void updatetime(Long operatesid, Long stamp){
		this.updatedat = stamp;
		this.updatedid = operatesid;
	}
	public void createtime(String operatesid){
		Long currenttime = Utils.getCurrenttimeMills();
		this.createdat = currenttime;
		this.updatedat = currenttime;
		this.createdid = Long.parseLong(operatesid);
		this.updatedid = Long.parseLong(operatesid);
	}
	public void updatetime(String operatesid){
		Long currenttime = Utils.getCurrenttimeMills();
		this.updatedat = currenttime;
		this.updatedid = Long.parseLong(operatesid);
	}
}
