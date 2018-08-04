package com.Entitys;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="tablechange")
public class TableChange {
	public final static int OPERATOR_CREATE = 0;
	public final static int OPERATOR_UPDATE = 1;
	public final static int OPERATOR_DELETE = 2;
	
	public final static int TABLETYPE_ADMIN = 0;
	public final static int TABLETYPE_BUYER = 1;
	public final static int TABLETYPE_CORPORATION = 2;
	public final static int TABLETYPE_ESCORT = 3;
	public final static int TABLETYPE_GOVERNMENT = 4;
	public final static int TABLETYPE_LOGININFO = 5;
	public final static int TABLETYPE_MANUFACTURER = 6;
	public final static int TABLETYPE_PRODUCT = 7;
	public final static int TABLETYPE_ROUTE = 8;
	public final static int TABLETYPE_SELLER = 9;
	public final static int TABLETYPE_TRAILER = 10;
	public final static int TABLETYPE_ORDERMONTHSTATISTIC = 11;
	public final static int TABLETYPE_ORDERYEARSTATISTIC = 12;
	public final static int TABLETYPE_TRUCKMAINTAINSTATISTIC = 13;
	public final static int TABLETYPE_WARNMONTHSTATISTIC = 14;
	public final static int TABLETYPE_WARNYEARSTATISTIC = 15;
	public final static int TABLETYPE_TRUCK = 16;
	public final static int TABLETYPE_FAREFORM = 17;
	public final static int TABLETYPE_FILESTORE2 = 18;//////////////////
	public final static int TABLETYPE_LOCK = 19;
	public final static int TABLETYPE_ORDER = 20;
	public final static int TABLETYPE_TRUCKARCHIVES = 21;
	public final static int TABLETYPE_TRUCKCHECK = 22;//////////
	public final static int TABLETYPE_TRUCKMAINTAIN = 23;
	public final static int TABLETYPE_WARN = 24;
	public final static int TABLETYPE_TRUCKLOG = 25;
	public final static int TABLETYPE_TRUCKLOGS = 26;;
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="sid", length=10)
	private int sid;
	@Column(name="time", length=13)
	private Long time;
	@Column(name="tabletype", length=4)
	private Integer tabletype;
	@Column(name="opearatortype", length=4)
	private Integer opearatortype;
	@Column(name="operatorsid", length=10)
	private Long operatorsid;
	@Column(name="oldobejct")
	private String oldobejct;
	@Column(name="newobject")
	private String newobject;
	public TableChange() {
		super();
	}
	public int getSid() {
		return sid;
	}
	public void setSid(int sid) {
		this.sid = sid;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public Integer getTabletype() {
		return tabletype;
	}
	public void setTabletype(Integer tabletype) {
		this.tabletype = tabletype;
	}
	public Integer getOpearatortype() {
		return opearatortype;
	}
	public void setOpearatortype(Integer opearatortype) {
		this.opearatortype = opearatortype;
	}
	public Long getOperatorsid() {
		return operatorsid;
	}
	public void setOperatorsid(Long operatorsid) {
		this.operatorsid = operatorsid;
	}
	public String getOldobejct() {
		return oldobejct;
	}
	public void setOldobejct(String oldobejct) {
		this.oldobejct = oldobejct;
	}
	public String getNewobject() {
		return newobject;
	}
	public void setNewobject(String newobject) {
		this.newobject = newobject;
	}
	@Override
	public String toString() {
		return "Table_Change_Log [sid=" + sid + ", time=" + time + ", tabletype=" + tabletype + ", opearatortype="
				+ opearatortype + ", operatorsid=" + operatorsid + ", oldobejct=" + oldobejct + ", newobject="
				+ newobject + "]";
	}
}
