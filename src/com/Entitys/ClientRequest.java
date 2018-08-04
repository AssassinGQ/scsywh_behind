package com.Entitys;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="requesthistory")
public class ClientRequest {
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="SID")
	private long SID;
	@Column(name="IP", length=15)
	private String clientip;
	@Column(name="username", length = 30)
	private String username;
	@Column(name="corporation", length = 5)
	private Long corporation;
	@Column(name="time", length=20)
	private String time;
	@Column(name="path")
	private String path;
	@Column(name="head")
	private String head;
	@Column(name="content")
	private String content;
	@Column(name="response")
	private String response;
	
	public ClientRequest() {
		super();
	}

	public long getSID() {
		return SID;
	}

	public void setSID(long sID) {
		SID = sID;
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

	public String getClientip() {
		return clientip;
	}

	public void setClientip(String clientip) {
		this.clientip = clientip;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	@Override
	public String toString() {
		return "ClientRequest [SID=" + SID + ", clientip=" + clientip + ", time=" + time + ", path=" + path + ", head="
				+ head + ", content=" + content + ", response=" + response + "]";
	}
	
}
