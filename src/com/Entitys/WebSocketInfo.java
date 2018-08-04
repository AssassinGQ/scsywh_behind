package com.Entitys;

import io.netty.channel.ChannelHandlerContext;

public class WebSocketInfo {
	private Integer type;
	private Long sid;
	private ChannelHandlerContext ctx;
	public WebSocketInfo() {
		super();
	}
	public WebSocketInfo(Integer type, Long sid, ChannelHandlerContext ctx) {
		super();
		this.type = type;
		this.sid = sid;
		this.ctx = ctx;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Long getSid() {
		return sid;
	}
	public void setSid(Long sid) {
		this.sid = sid;
	}
	public ChannelHandlerContext getCtx() {
		return ctx;
	}
	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}
	@Override
	public String toString() {
		return "WebSocketInfo [type=" + type + ", sid=" + sid + ", ctx=" + ctx + "]";
	}
}
