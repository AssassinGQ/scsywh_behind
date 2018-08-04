package com.Entitys;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="errormsgs")
public class ErrorMsgs {
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="sid")
	private Long sid;
	@Column(name="time")
	private String time;	//when
	@Column(name="ip")
	private String ip;		//where
	@Column(name="addr")
	private String addr;	//where
	@Column(name="logininfosid")
	private Long logininfosid;	//who
	@Column(name="username")
	private String username;
	@Column(name="corporation")
	private Long corporation;
	@Column(name="path")
	private String path;
	@Column(name="params")
	private String params;
	@Column(name="errormsg")	//what
	private String errormsg;
	public ErrorMsgs() {
		super();
	}
	public Long getSid() {
		return sid;
	}
	public void setSid(Long sid) {
		this.sid = sid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Long getCorporation() {
		return corporation;
	}
	public void setCorporation(Long corporation) {
		this.corporation = corporation;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Long getLogininfosid() {
		return logininfosid;
	}
	public void setLogininfosid(Long logininfosid) {
		this.logininfosid = logininfosid;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}
	public String getErrormsg() {
		return errormsg;
	}
	public void setErrormsg(String errormsg) {
		this.errormsg = errormsg;
	}
}
