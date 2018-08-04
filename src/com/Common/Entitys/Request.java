package com.Common.Entitys;

import java.util.Arrays;

import org.hibernate.Session;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class Request {
	 public static final int REQUESTTYPE_HTTP = 0;
	    public static final int REQUESTTYPE_WEBSOCKET = 1;
	    public static final int REQUESTTYPE_UNKNOW = 2;
	    private Integer requestType;
	    private Session session;
	    //http
	    private HttpRequest httpRequest;
	    private boolean isRequestOk;
	    private HttpContent httpContent;
	    private boolean isContentOk;
	    private HttpMethod method;
	    private String[] path;
	    private boolean isKeepAlive;
	    //websocket
	    private WebSocketFrame webSocketFrame;

	    public Integer getRequestType() {
	        return requestType;
	    }

	    public void setRequestType(Integer requestType) {
	        this.requestType = requestType;
	    }

	    public Session getSession() {
			return session;
		}

		public void setSession(Session session) {
			this.session = session;
		}

		public void setHttpRequest(HttpRequest httpRequest) {
	        this.httpRequest = httpRequest;
	    }

	    public HttpRequest getHttpRequest() {
	        return httpRequest;
	    }

	    public boolean isRequestOk() {
	        return isRequestOk;
	    }

	    public void setRequestOk(boolean requestOk) {
	        isRequestOk = requestOk;
	    }

		public boolean isContentOk() {
	        return isContentOk;
	    }

	    public void setContentOk(boolean contentOk) {
	        isContentOk = contentOk;
	    }

	    public HttpContent getHttpContent() {
	        return httpContent;
	    }

	    public void setHttpContent(HttpContent httpContent) {
	        this.httpContent = httpContent;
	    }

	    public HttpMethod getMethod() {
	        return method;
	    }

	    public void setMethod(HttpMethod method) {
	        this.method = method;
	    }

	    public String[] getPath() {
	        return path;
	    }

	    public void setPath(String[] path) {
	        this.path = path;
	    }

		public boolean isKeepAlive() {
	        return isKeepAlive;
	    }

	    public void setKeepAlive(boolean keepAlive) {
	        isKeepAlive = keepAlive;
	    }

	    public WebSocketFrame getWebSocketFrame() {
	        return webSocketFrame;
	    }

	    public void setWebSocketFrame(WebSocketFrame webSocketFrame) {
	        this.webSocketFrame = webSocketFrame;
	    }

		@Override
		public String toString() {
			return "Request [requestType=" + requestType + ", session=" + session + ", httpRequest=" + httpRequest
					+ ", isRequestOk=" + isRequestOk + ", httpContent=" + httpContent + ", isContentOk=" + isContentOk
					+ ", method=" + method + ", path=" + Arrays.toString(path) + ", isKeepAlive=" + isKeepAlive
					+ ", webSocketFrame=" + webSocketFrame + "]";
		}
}
