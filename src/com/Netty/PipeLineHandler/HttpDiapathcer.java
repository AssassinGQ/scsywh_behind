package com.Netty.PipeLineHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.RequestHandlers.*;
import org.hibernate.Session;
import org.json.JSONObject;

import com.Common.Entitys.Bean;
import com.Common.Entitys.Request;
import com.Common.Exceptions.DaoException;
import com.Common.Utils.HibernateUtil;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
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

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpDiapathcer extends ChannelInboundHandlerAdapter {

	private Request request;
	private Session session;
	private LoginInfo tokenLoginInfo;
	private String[] path;
	private HttpMethod method;
	private boolean iskeepAlive;
	private FullHttpResponse response = null;
	private Map<String, String> params;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Request) {
			request = (Request) msg;
			if (request.getRequestType() == Request.REQUESTTYPE_HTTP) {
				session = request.getSession();
				path = request.getPath();
				method = request.getMethod();
				iskeepAlive = request.isKeepAlive();
				params = NettyUtils.getParams(request.getHttpRequest(), request.getHttpContent());
				if (!session.isConnected()) {
					System.err.println("Session already closed,reopen Session...");
					session = HibernateUtil.getSessionFactory().openSession();
				}
				String token = params.get("token");
				try {
					tokenLoginInfo = getInfo(token);
					HttpRequestDiapathcer(ctx, tokenLoginInfo); // http请求响应
				} catch (DaoException e) {
					if (e.getMessage().equals("_tokenexception")) {
						JSONObject retObject = new JSONObject();
						retObject.put("status", 4);
						retObject.put("msg", "token失效");
						retObject.put("content", "");
						response = NettyUtils.getResponse(iskeepAlive, retObject.toString());
					} else
						throw e;
				}
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
							RequestHistoryHandler.saveRequest(session, ctx, request.getHttpRequest(), content,
									responsestr, tokenLoginInfo);
							// ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
							ctx.write(response).addListener(ChannelFutureListener.CLOSE);
						}
					} else {
						RequestHistoryHandler.saveRequest(session, ctx, request.getHttpRequest(),
								NettyUtils.getContent(params), response.status().reasonPhrase(), tokenLoginInfo);
						// ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
						ctx.write(response).addListener(ChannelFutureListener.CLOSE);
					}
				}
			}
			ctx.fireChannelRead(msg);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	private LoginInfo getInfo(String token) throws DaoException {
		if (token == null)
			return null;
		List<LoginInfo> loginInfos = session
				.createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and token = '" + token + "'",
						LoginInfo.class)
				.getResultList();
		if (loginInfos.size() == 1) {
//			if (loginInfos.get(0).getUsername().equals("superadmin")) {
//				return loginInfos.get(0);
//			} else {
				Long lastlogintime = loginInfos.get(0).getLastlogintime();
				Long duration = Long.valueOf(6 * 60 * 60 * 1000);
				if (lastlogintime + duration < System.currentTimeMillis()) {// token失效
					throw new DaoException("_tokenexception");
				}
				return loginInfos.get(0);
//			}
		} else {
			return null;
		}
	}

	private void HttpRequestDiapathcer(ChannelHandlerContext ctx, LoginInfo loginInfo) throws Exception {
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
					AdminHandler adminHandler = new AdminHandler();
					response = adminHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("escort_info")) { // 押运员
					EscortHandler escortHandler = new EscortHandler();
					response = escortHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("driver_info")) { // 驾驶员
					DriverHandler driverHandler = new DriverHandler();
					response = driverHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("buyer_info")) { // 收货方
					BuyerHandler buyerHandler = new BuyerHandler();
					response = buyerHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("seller_info")) { // 托运方
					SellerHandler sellerHandler = new SellerHandler();
					response = sellerHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("product_info")) { // 货物
					BasicInfoHandler<Product> productRequest = new BasicInfoHandler<Product>(Product.class);
					response = productRequest.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
				} else if (path[1].equals("truck_info")) { // 拖车
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
		} else if (path[0].equals("auth")) { // 权限管理
			if (path.length != 3)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else {
				AuthHandler authHandler = new AuthHandler();
				response = authHandler.dowork(ctx, path, session, method, params, iskeepAlive, loginInfo);
			}
		} else if (path[0].equals("trucklogs")) { // 添加日志
			if (path.length != 2)
				response = NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			else {
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
		} else if (path[0].equals("file")) { // 上传下载文件
			if (path.length != 2)
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
		} else if(path[0].equals("wsshake")){  // http握手
        } else if (path[0].equals("shake")) { // websocket测试用
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
}
