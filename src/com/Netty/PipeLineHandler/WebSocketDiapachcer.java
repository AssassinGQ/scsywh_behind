package com.Netty.PipeLineHandler;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Entitys.Request;
import com.Common.Utils.MySession;
import com.Common.Utils.Utils;
import com.Common.Utils.WebSocketUtils;
import com.Entitys.WebSocketInfo;
import com.Entitys.User.Entity.LoginInfo;
import com.Web.WebSocketH5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

public class WebSocketDiapachcer extends ChannelInboundHandlerAdapter {
    private WebSocketServerHandshaker handshaker;
    private WebSocketFrame webSocketFrame;
    private Session session;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof Request){
            Request request = (Request)msg;
            session = request.getSession();
            if(request.getRequestType() == Request.REQUESTTYPE_HTTP){
                String[] path = request.getPath();
                if(path.length == 1){
                    if(path[0].equals("wsshake")){  // http握手
                        String wsLocation = "ws://" + request.getHttpRequest().headers().get(HttpHeaderNames.HOST) + "/wsshake";
                        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(wsLocation, null, false);
                        handshaker = wsFactory.newHandshaker(request.getHttpRequest());
                        if (handshaker == null) {
                            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                        } else {
                            handshaker.handshake(ctx.channel(), request.getHttpRequest());
                        }
                    } else if (path[0].equals("shake")) { // websocket测试用
                        String wsLocation = "ws://" + request.getHttpRequest().headers().get(HttpHeaderNames.HOST) + "/wsshake";
                        ByteBuf content = WebSocketH5.getContent(wsLocation);
                        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
                        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
                        HttpUtil.setContentLength(res, content.readableBytes());
                        sendHttpResponse(ctx, (FullHttpRequest) request.getHttpRequest(), res);
                    }
                }
            }else if(request.getRequestType() == Request.REQUESTTYPE_WEBSOCKET){
                webSocketFrame = request.getWebSocketFrame();
                if(handshaker == null || ctx == null || webSocketFrame == null)
                    return;
                if (webSocketFrame instanceof CloseWebSocketFrame) {
                    handshaker.close(ctx.channel(), (CloseWebSocketFrame) webSocketFrame.retain());
                    return;
                }
                if (webSocketFrame instanceof PingWebSocketFrame) {
                    ctx.channel().write(new PongWebSocketFrame(webSocketFrame.content().retain()));
                    return;
                }
                if (!(webSocketFrame instanceof TextWebSocketFrame)) {
                    String message = "unsupported frame type: " + webSocketFrame.getClass().getName();
                    throw new UnsupportedOperationException(message);
                }
                String reqstr = ((TextWebSocketFrame) webSocketFrame).text();
                String token = null;
                Integer type = null;
                Long sid = null;
                try {
                    JSONObject jsonObject = new JSONObject(reqstr);
                    token = jsonObject.getString("token");
                    type = jsonObject.getInt("type");
                    sid = jsonObject.getLong("sid");
                    if (token == null || type == null || sid == null) {
                        String errorstr = null;
                        if (sid == null)
                            errorstr = "请上传sid";
                        if (type == null)
                            errorstr = "请上传type";
                        if (token == null)
                            errorstr = "请上传token";
                        throw new Exception(errorstr);
                    }
                } catch (Exception e) {
                    try {
                        JSONObject retObject = new JSONObject();
                        retObject.put("status", "4");
                        retObject.put("msg", Utils.encoderUTF("登记推送信息失败：" + e.getMessage()));
                        retObject.put("content", "");
                        ctx.channel().write(new TextWebSocketFrame(retObject.toString()));
                    } catch (JSONException e1) {
                        ctx.channel().write(new TextWebSocketFrame(Utils.encoderUTF("登记推送信息失败")));
                    }
                    return;
                }
                //保存握手信息
                try {
                    LoginInfo loginInfo = session.createQuery(
                            "from LoginInfo where type = " + type + " and token = '" + token + "' and usersid = " + sid,
                            LoginInfo.class).uniqueResult();
                    if (loginInfo == null) {
                        try {
                            JSONObject retObject = new JSONObject();
                            retObject.put("status", "4");
                            retObject.put("msg", Utils.encoderUTF("登记推送信息失败：没有相应登录信息"));
                            retObject.put("content", "");
                            ctx.channel().write(new TextWebSocketFrame(retObject.toString()));
                        } catch (JSONException e1) {
                            ctx.channel().write(new TextWebSocketFrame(Utils.encoderUTF("登记推送信息失败：没有相应登录信息")));
                        }
                        return;
                    }
                    Transaction tx = session.beginTransaction();
                    String oldloginInfo = Utils.getJsonObject(loginInfo).toString();
                    loginInfo.setOnlinetype(LoginInfo.ONLINETYPE_WEB);
                    loginInfo.setWsid(token);
                    loginInfo.updatetime(sid);
                    MySession.OnUpdate(oldloginInfo, loginInfo, session, sid);
                    tx.commit();
                    WebSocketInfo wsInfo = new WebSocketInfo(type, sid, ctx);
                    WebSocketUtils.AddCtx(token, wsInfo);
                    try {
                        JSONObject retObject = new JSONObject();
                        retObject.put("status", "0");
                        retObject.put("msg", Utils.encoderUTF("登记推送信息成功"));
                        retObject.put("content", "");
                        ctx.channel().write(new TextWebSocketFrame(retObject.toString()));
                    } catch (JSONException e1) {
                        ctx.channel().write(new TextWebSocketFrame(Utils.encoderUTF("登记推送信息成功")));
                    }
                    return;
                } catch (Exception e) {
                    try {
                        JSONObject retObject = new JSONObject();
                        retObject.put("status", "4");
                        retObject.put("msg", Utils.encoderUTF("登记推送信息失败：查询到多个登录信息"));
                        retObject.put("content", "");
                        ctx.channel().write(new TextWebSocketFrame(retObject.toString()));
                    } catch (JSONException e1) {
                        ctx.channel().write(new TextWebSocketFrame(Utils.encoderUTF("登记推送信息失败：查询到多个登录信息")));
                    }
                    return;
                }
            }
        }
//        ctx.fireChannelRead(msg);
    }
    
    @Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
    
    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpUtil.setContentLength(res, res.content().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
}
