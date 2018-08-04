package com.Netty;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Entitys.Bean;
import com.Common.Exceptions.DaoException;
import com.Common.Utils.HibernateUtil;
import com.Common.Utils.MySession;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
import com.Common.Utils.WebSocketUtils;
import com.Entitys.WebSocketInfo;
import com.Entitys.Admin.Action.AdminHandler;
import com.Entitys.Buyer.Action.BuyerHandler;
import com.Entitys.Corporation.Entity.Corporation;
import com.Entitys.Document.Action.DocumentHandler;
import com.Entitys.Driver.Action.DriverHandler;
import com.Entitys.Escort.Action.EscortHandler;
import com.Entitys.Exam.Action.ExamHandler;
import com.Entitys.Fareform.Action.FareFormHandler;
import com.Entitys.FileUpload.Action.FileHandler;
import com.Entitys.Lock.Action.LockHandler;
import com.Entitys.Order.Action.OrderHandler;
import com.Entitys.Product.Entity.Product;
import com.Entitys.Route.Entity.Route;
import com.Entitys.Seller.Action.SellerHandler;
import com.Entitys.Trailer.Entity.Trailer;
import com.Entitys.Truck.Action.TruckHandler;
import com.Entitys.Trucklog.Action.TruckLogHandler;
import com.Entitys.User.Entity.LoginInfo;
import com.Entitys.Warn.Action.WarnHandler;
import com.RequestHandlers.BasicInfoHandler;
import com.RequestHandlers.ErrorHandler;
import com.RequestHandlers.GetuiHandler;
import com.RequestHandlers.LoginHandler;
import com.RequestHandlers.RequestHistoryHandler;
import com.RequestHandlers.StatisticsHandler;
import com.RequestHandlers.TestHandler;
import com.Web.WebSocketH5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

public class RequestDiapathcer extends SimpleChannelInboundHandler<Object> {

	private HttpRequest request;
	private HttpContent content;
	private Map<String, String> params;
	private FullHttpResponse response;
	private boolean iskeepAlive, isRequestok;
	private LoginInfo tokenLoginInfo;
	private int msgtype;
	private String[] path;
	private HttpMethod method;
	private Session session;
	private WebSocketServerHandshaker handshaker;

	public RequestDiapathcer() {
		this.session = HibernateUtil.openSession();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
		try {
			if (msg instanceof FullHttpRequest) {
				msgtype = 0;
				HttpDiapathcer(ctx, (FullHttpRequest) msg);
			} else if (msg instanceof WebSocketFrame) {
				msgtype = 1;
				WebSocketDiapathcer(ctx, (WebSocketFrame) msg);
			} else {
				msgtype = 2;
			}
		} catch (Exception e) {
			String error = Utils.getErrorStr(e);
			String ip = null;
			try {
				ip = NettyUtils.getRemoteip(request, ctx);
			} catch (Exception e2) {
				ip = "";
			}
			String uri = null;
			try {
				uri = NettyUtils.getUri(request);
			} catch (Exception e2) {
				uri = "";
			}
			String content = null;
			try {
				content = NettyUtils.getContent(params);
			} catch (Exception e2) {
				content = "";
			}
			ErrorHandler.saveError(session, ip, tokenLoginInfo, uri, content, error);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		try {
			if (msgtype == 0) {
				if (response != null) {
					if (response.status() == HttpResponseStatus.OK) {
						// if(iskeepAlive)
						// ctx.writeAndFlush(response);
						// else
						{
							String responsestr = new String(response.content().array());
							if (responsestr != null && responsestr.length() > 15000)
								responsestr = responsestr.substring(0, 10000);
							String content = NettyUtils.getContent(params);
							if (content != null && content.length() > 15000)
								content = content.substring(0, 10000);
							RequestHistoryHandler.saveRequest(session, ctx, request, content, responsestr, tokenLoginInfo);
							ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
						}
					} else {
						RequestHistoryHandler.saveRequest(session, ctx, request, NettyUtils.getContent(params),
								response.status().reasonPhrase(), tokenLoginInfo);
						ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
					}
					session.close();
				}
			} else if (msgtype == 1) {
				ctx.flush();
			} else {

			}
		} catch (Exception e) {
			String error = Utils.getErrorStr(e);
			String ip = null;
			try {
				ip = NettyUtils.getRemoteip(request, ctx);
			} catch (Exception e2) {
				ip = "";
			}
			String uri = null;
			try {
				uri = NettyUtils.getUri(request);
			} catch (Exception e2) {
				uri = "";
			}
			String content = null;
			try {
				content = NettyUtils.getContent(params);
			} catch (Exception e2) {
				content = "";
			}
			ErrorHandler.saveError(session, ip, tokenLoginInfo, uri, content, error);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	private void HttpDiapathcer(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
		if (msg instanceof HttpRequest) {
			System.out.println("receive HttpRequest");
			request = (HttpRequest) msg;
			iskeepAlive = HttpUtil.isKeepAlive(request);
			method = request.method();
			path = NettyUtils.getUris(request);
			if (!request.decoderResult().isSuccess()) {
				// NettyUtils.sendError(ctx, BAD_REQUEST);
				isRequestok = false;
			} else {
				isRequestok = true;
			}
			if(path == null || path.length == 0)
				isRequestok = false;
		}

		if (msg instanceof HttpContent) {
			System.out.println("receive HttpContent");
			content = (HttpContent) msg;
			if (!content.decoderResult().isSuccess() || !isRequestok) {
				response = NettyUtils.sendError(BAD_REQUEST);
			} else {
				if (!session.isConnected()) {
					System.err.println("Session already closed,reopen Session...");
					session = HibernateUtil.getSessionFactory().openSession();
				}
				params = NettyUtils.getParams(request, content);
				String token = params.get("token");
				try {
					tokenLoginInfo = getInfo(token);
					HttpRequestDiapathcer(ctx, msg, tokenLoginInfo); // http请求响应					
				} catch (DaoException e) {
					if(e.getMessage().equals("_tokenexception")){
						JSONObject retObject = new JSONObject();
						retObject.put("status", 4);
						retObject.put("msg", "token失效");
						retObject.put("content", "");
						response = NettyUtils.getResponse(iskeepAlive, retObject.toString());						
					}
					else
						throw e;
				}
			}
		}
	}

	private void HttpRequestDiapathcer(ChannelHandlerContext ctx, FullHttpRequest msg, LoginInfo loginInfo)
			throws Exception {
		if (path[0].equals("user")) { // 承运方/政府/设备生厂商 注册&登录&查询&修改信息&获取验证码
			if (path.length != 2)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else
				response = LoginHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[0].equals("basic_info")) { // 基本信息
			if (path.length != 3)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else {
				if (path[1].equals("corporation")) {
					BasicInfoHandler<Corporation> corporationRequest = new BasicInfoHandler<Corporation>(
							Corporation.class);
					response = corporationRequest.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("admin_info")) { // 公司员工
//					LoginableBasicInfoHandler<Admin> adminRequest = new LoginableBasicInfoHandler<Admin>(Admin.class);
					AdminHandler adminHandler = new AdminHandler();
					response = adminHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("escort_info")) { // 押运员
//					LoginableBasicInfoHandler<Escort> escortRequest = new LoginableBasicInfoHandler<Escort>(
//							Escort.class);
					EscortHandler escortHandler = new EscortHandler();
					response = escortHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("driver_info")) { // 驾驶员
//					LoginableBasicInfoHandler<Driver> driverRequest = new LoginableBasicInfoHandler<Driver>(
//							Driver.class);
					DriverHandler driverHandler = new DriverHandler();
					response = driverHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("buyer_info")) { // 收货方
//					LoginableBasicInfoHandler<Buyer> buyerRequest = new LoginableBasicInfoHandler<Buyer>(Buyer.class);
					BuyerHandler buyerHandler = new BuyerHandler();
					response = buyerHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("seller_info")) { // 托运方
//					LoginableBasicInfoHandler<Seller> sellerRequest = new LoginableBasicInfoHandler<Seller>(
//							Seller.class);
					SellerHandler sellerHandler = new SellerHandler();
					response = sellerHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("product_info")) { // 货物
					BasicInfoHandler<Product> productRequest = new BasicInfoHandler<Product>(Product.class);
					response = productRequest.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("truck_info")) { // 拖车
					// BasicInfoHandler<Truck> truckRequest = new
					// BasicInfoHandler<Truck>(Truck.class);
					// response = truckRequest.dowork(ctx, path, session,
					// method, params, iskeepAlive, loginInfo);
					TruckHandler truckHandler = new TruckHandler();
					response = truckHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("trailer_info")) { // 挂车
					BasicInfoHandler<Trailer> trailerRequest = new BasicInfoHandler<Trailer>(Trailer.class);
					response = trailerRequest.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("route_info")) { // 路线
					BasicInfoHandler<Route> routeRequest = new BasicInfoHandler<Route>(Route.class);
					response = routeRequest.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else
					response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			}
		} else if (path[0].equals("trucklogs")) { // 添加日志
			if (path.length != 2)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else{
				TruckLogHandler truckLogHandler = new TruckLogHandler();
				response = truckLogHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
			}
		} else if (path[0].equals("order")) { // 订单流程
			if (path.length != 2)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else {
				OrderHandler orderHandler = new OrderHandler();
				response = orderHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
			}
		} else if (path[0].equals("fareform")) { // 费用清单请求
			if (path.length != 2)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else {
				FareFormHandler fareFormHandler = new FareFormHandler();
				response = fareFormHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
			}
		} else if (path[0].equals("lock")) { // 阀门锁请求
			if (path.length != 2)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else {
				LockHandler lockHandler = new LockHandler();
				response = lockHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
			}
		} else if (path[0].equals("statistics")) { // 订单异常统计查询
			if (path.length != 2)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else {
				StatisticsHandler statisticsHandler = new StatisticsHandler();
				response = statisticsHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
			}
		} else if (path[0].equals("warn")) { // 异常
			if (path.length != 2)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else {
				WarnHandler warnHandler = new WarnHandler();
				response = warnHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
			}
		} else if (path[0].equals("getui")) { // app端登记个推cid
			if (path.length != 2)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else {
				response = GetuiHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
			}
		} else if(path[0].equals("file")){		//上传下载文件
			if(path.length != 2)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else {
				FileHandler fileHandler = new FileHandler();
				response = fileHandler.dowork(ctx, path, session, method, params, iskeepAlive);
			}
		} else if (path[0].equals("exam")) { // 驾驶员测试
			if (path.length != 2)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else {
				ExamHandler examHandler = new ExamHandler();
				response = examHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
			}
		} else if (path[0].equals("document")) { // 学习资料
			if (path.length != 2)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else {
				DocumentHandler documentHandler = new DocumentHandler();
				response = documentHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
			}
		} else if (path[0].equals("test")) { // 测试用
			if (path.length != 2)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else {
				response = TestHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
			}
		} else if (path[0].equals("wsshake")) { // websocket握手
			String wsLocation = "ws://" + msg.headers().get(HttpHeaderNames.HOST) + "/wsshake";
			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(wsLocation, null, false);
			handshaker = wsFactory.newHandshaker(msg);
			if (handshaker == null) {
				WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
			} else {
				handshaker.handshake(ctx.channel(), msg);
			}
		} else if (path[0].equals("shake")) { // websocket测试用
			String wsLocation = "ws://" + msg.headers().get(HttpHeaderNames.HOST) + "/wsshake";
			ByteBuf content = WebSocketH5.getContent(wsLocation);
			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

			res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
			HttpUtil.setContentLength(res, content.readableBytes());
			sendHttpResponse(ctx, msg, res);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Request Path: " + path + "\r\n");
			sb.append("Request Method: " + method + "\r\n");
			sb.append("Request Params: \r\n");
			Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> entry = iterator.next();
				sb.append(entry.getKey() + " = " + entry.getValue() + "\r\n");
			}
			sb.append("Just for Test" + "\r\n");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("msg", "success");
			jsonObject.put("status", 0);
			jsonObject.put("content", sb);
			String respstr = jsonObject.toString();
			respstr = Utils.encoderUTF(respstr);
			response = NettyUtils.getResponse(iskeepAlive, respstr);
		}
	}

	private LoginInfo getInfo(String token) throws DaoException {
		if (token == null)
			return null;
		List<LoginInfo> loginInfos = session
				.createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and token = '" + token + "'",
						LoginInfo.class)
				.getResultList();
		if (loginInfos.size() == 1) {
			if(token.equals("superadmin")){
				return loginInfos.get(0);
			}else {
				Long lastlogintime = loginInfos.get(0).getLastlogintime();
				Long duration = Long.valueOf(6*60*60*1000);
				if(lastlogintime + duration < System.currentTimeMillis()){//token失效
					throw new DaoException("_tokenexception");
				}
				return loginInfos.get(0);				
			}
		} else {
			return null;
		}
	}

	private void WebSocketDiapathcer(ChannelHandlerContext ctx, WebSocketFrame msg) {
		if (handshaker == null || ctx == null || msg == null)
			return;
		if (msg instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) msg.retain());
			return;
		}

		if (msg instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(msg.content().retain()));
			return;
		}

		if (!(msg instanceof TextWebSocketFrame)) {
			String message = "unsupported frame type: " + msg.getClass().getName();
			throw new UnsupportedOperationException(message);
		}
		String reqstr = ((TextWebSocketFrame) msg).text();
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
		if (!session.isConnected()) {
			session = HibernateUtil.getSessionFactory().openSession();
		}
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
