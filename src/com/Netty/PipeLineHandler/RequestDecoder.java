package com.Netty.PipeLineHandler;

import org.hibernate.Session;

import com.Common.Entitys.Request;
import com.Common.Utils.HibernateUtil;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
import com.RequestHandlers.ErrorHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class RequestDecoder extends ChannelInboundHandlerAdapter {
	private HttpRequest httpRequest = null;
	private HttpContent httpContent = null;
    private Request request;
    private Session session;
    private boolean isHttpComplete = false;

	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		this.session = HibernateUtil.openSession();
        try{
        	if(msg instanceof FullHttpRequest){
        		FullHttpRequest fullHttpRequest = (FullHttpRequest)msg;
        		if (fullHttpRequest instanceof HttpRequest) {
                    httpRequest = (HttpRequest) fullHttpRequest;
                    if (httpRequest.decoderResult().isSuccess()) {
                        request = new Request();
                        isHttpComplete = false;
                        request.setRequestType(Request.REQUESTTYPE_HTTP);
                        request.setRequestOk(true);
                        request.setSession(session);
                        request.setHttpRequest(httpRequest);
                        request.setMethod(httpRequest.method());
                        request.setPath(NettyUtils.getUris(httpRequest));
                        request.setKeepAlive(HttpUtil.isKeepAlive(httpRequest));
                    } else {
                        request = new Request();
                        isHttpComplete = false;
                        request.setRequestType(Request.REQUESTTYPE_HTTP);
                        request.setSession(session);
                        request.setRequestOk(false);
                    }
                }
        		if (fullHttpRequest instanceof HttpContent){
                    httpContent = (HttpContent)fullHttpRequest;
                    isHttpComplete = true;
                    if(httpContent.decoderResult().isSuccess()){
                        request.setContentOk(true);
                        request.setSession(session);
                        request.setHttpContent(httpContent);
                    }else {
                    	request.setSession(session);
                        request.setContentOk(false);
                    }
                }
        	}
            else if (msg instanceof WebSocketFrame) {
                request = new Request();
                request.setRequestType(Request.REQUESTTYPE_WEBSOCKET);
                request.setSession(session);
                request.setWebSocketFrame((WebSocketFrame)msg);
            } else {
                request = new Request();
                request.setSession(session);
                request.setRequestType(Request.REQUESTTYPE_UNKNOW);
            }
        }catch (Exception e){
        	String error = Utils.getErrorStr(e);
			String ip = null;
			try {
				ip = NettyUtils.getRemoteip(httpRequest, ctx);
			} catch (Exception e2) {
				ip = "";
			}
			String uri = null;
			try {
				uri = NettyUtils.getUri(httpRequest);
			} catch (Exception e2) {
				uri = "";
			}
			String content = null;
			try {
				content = NettyUtils.getContent(NettyUtils.getParams(httpRequest, httpContent));
			} catch (Exception e2) {
				content = "";
			}
			ErrorHandler.saveError(session, ip, null, uri, content, error);
			request = null;
        }
        if(request != null && request.getRequestType() != null && (
        		((request.getRequestType() == Request.REQUESTTYPE_HTTP) && isHttpComplete) ||
        		(request.getRequestType() == Request.REQUESTTYPE_WEBSOCKET)	))
        	ctx.fireChannelRead(request);
    }

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
		ctx.flush();
		if(request != null && request.getSession() != null && request.getSession().isConnected())
			request.getSession().close();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
