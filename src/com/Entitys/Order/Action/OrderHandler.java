package com.Entitys.Order.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Daos.MulTabBaseDaoImpl;
import com.Common.Daos.QueryBean;
import com.Common.Entitys.BasicInfo;
import com.Common.Entitys.Bean;
import com.Common.Utils.MySession;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.PushUtils;
import com.Common.Utils.Utils;
import com.Entitys.Admin.Entity.Admin;
import com.Entitys.Buyer.Dao.BuyerDaoImpl;
import com.Entitys.Buyer.Entity.Buyer;
import com.Entitys.Corporation.Entity.Corporation;
import com.Entitys.Driver.Dao.DriverDaoImpl;
import com.Entitys.Driver.Entity.Driver;
import com.Entitys.Escort.Dao.EscortDaoImpl;
import com.Entitys.Escort.Entity.Escort;
import com.Entitys.Fareform.Dao.FareformDaoImpl;
import com.Entitys.Fareform.Entity.FareForm;
import com.Entitys.Order.Dao.OrderDaoImpl;
import com.Entitys.Order.Entity.Order;
import com.Entitys.Product.Dao.ProductDaoImpl;
import com.Entitys.Product.Entity.Product;
import com.Entitys.Route.Dao.RouteDaoImpl;
import com.Entitys.Route.Entity.Route;
import com.Entitys.Seller.Dao.SellerDaoImpl;
import com.Entitys.Seller.Entity.Seller;
import com.Entitys.Statistics.OrderMonthStatistics;
import com.Entitys.Statistics.OrderYearStatistics;
import com.Entitys.Statistics.StatisticsStatic;
import com.Entitys.Trailer.Dao.TrailerDaoImpl;
import com.Entitys.Trailer.Entity.Trailer;
import com.Entitys.Truck.Dao.TruckDaoImpl;
import com.Entitys.Truck.Entity.Truck;
import com.Entitys.Truck.Entity.TruckArchives;
import com.Entitys.Trucklog.Dao.TrucklogDaoImpl;
import com.Entitys.Trucklog.Entity.TruckLog;
import com.Entitys.User.Entity.LoginInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class OrderHandler {
	private Long operatesid, corporationsid;
	private Integer operateusertype, role;
	private OrderDaoImpl orderDaoImpl;
	private FareformDaoImpl fareformDaoImpl;
	private TruckDaoImpl truckDaoImpl;
	private TrailerDaoImpl trailerDaoImpl;
	private DriverDaoImpl driverDaoImpl;
	private EscortDaoImpl escortDaoImpl;
	private RouteDaoImpl routeDaoImpl;

	public FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (loginInfo == null)
			return NettyUtils.getTokenError(iskeepAlive);
		operateusertype = loginInfo.getType();
		operatesid = loginInfo.getUsersid();
		orderDaoImpl = new OrderDaoImpl(session);
		fareformDaoImpl = new FareformDaoImpl(session);
		truckDaoImpl = new TruckDaoImpl(session);
		trailerDaoImpl = new TrailerDaoImpl(session);
		driverDaoImpl = new DriverDaoImpl(session);
		escortDaoImpl = new EscortDaoImpl(session);
		routeDaoImpl = new RouteDaoImpl(session);

		if (path[1].equals("order")) {// 推送管理员
			return ToOrder(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("dispatch")) {// 调度，分配车辆，从此开始车辆司机被占用
			return ToDispatch(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("check")) {// 安检
			return ToCheck(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("distribute")) {// 派发，推送给司机
			return ToDistribute(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("receive")) {// 司机APP接受到推送后给反馈
			return OnReceived(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("load")) {// 装货后托运方上传
			return AfterLoad(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("unload")) {// 卸货后收货方上传
			return AfterUnload(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("return")) {// 提示司机，必须要回场后才能提交，不然会影响里程记录
			return OnReturn(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("reverifyd")) {// 审核不通过，司机修改完后调用此接口推送给管理员重新审核
			return OnReVerifyd(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("verifyppp")) {// 第一次审核（里程审核）结果，网页先从FareFormHandler中获得审核时查看的数据
			return OnVerifyPPP(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("verifypp")) {// 第二次审核（非财务审核）结果，同上
			return OnVerifyPP(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("verifyp")) {// 第三次审核（财务审核）结果，同上
			return OnVerifyP(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("verify")) {// 第四次审核（总体审核）结果，同上，此时车辆司机才空闲下来，才能继续接单
			return OnVerify(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("query")) {
			return OnQuery(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("distributetest")) {// 派发，推送给司机
			return ToDistributeForTest(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}

	protected FullHttpResponse ToOrder(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in OrderHandler-ToOrder");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_SELLER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		String sellersidstr = params.get("sellersid");
		String buyersidstr = params.get("buyersid");
		String productsidstr = params.get("productsid");
		String loadaddr = params.get("loadaddr");
		String unloadaddr = params.get("unloadaddr");
		String loaddateddlstr = params.get("loaddateddl");
		String unloaddateddlstr = params.get("unloaddateddl");
		String productweightstr = params.get("productweight");
		String productvolstr = params.get("productvol");
		if (sellersidstr == null || buyersidstr == null || productsidstr == null || unloaddateddlstr == null
				|| loaddateddlstr == null || productweightstr == null || productvolstr == null) {
			String errorstr = "";
			if (sellersidstr == null)
				errorstr = "请上传sellersid";
			if (buyersidstr == null)
				errorstr = "请上传buyersid";
			if (productsidstr == null)
				errorstr = "请上传productsid";
			if (unloaddateddlstr == null)
				errorstr = "请上传unloaddateddl";
			if (loaddateddlstr == null)
				errorstr = "请上传loaddateddl";
			if (productweightstr == null)
				errorstr = "请上传productweight";
			if (productvolstr == null)
				errorstr = "请上传productvol";
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", errorstr);
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long loaddateddl = null, unloaddateddl = null, sellersid = null;
		Long buyersid = null, productsid = null;
		Double productweight = null, productvol = null;
		Product product = null;
		Seller seller = null;
		Buyer buyer = null;
		try {
			productsid = Long.parseLong(productsidstr);
			buyersid = Long.parseLong(buyersidstr);
			sellersid = Long.parseLong(sellersidstr);
			try {
				product = new ProductDaoImpl(session).getById(productsid, true);
				buyer = new BuyerDaoImpl(session).getById(buyersid, true);
				seller = new SellerDaoImpl(session).getById(sellersid, true);
				if (product == null) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "查无此货物");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (buyer == null) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "查无此收货方");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (seller == null) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "查无此托运方");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
			} catch (Exception e) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "货物数据库出错");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "输入Long型格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			loaddateddl = Long.parseLong(loaddateddlstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "loaddateddl格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			unloaddateddl = Long.parseLong(unloaddateddlstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "unloaddateddl格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			productweight = Double.parseDouble(productweightstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "productweight格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			productvol = Double.parseDouble(productvolstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "productvol格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		//////////////////
		Transaction tx = session.beginTransaction();
		Long nowstamp = System.currentTimeMillis();
		Order order = new Order();
		order.setOrderstatus(Order.STATUS_ORDERED);
		order.setCorporationsid(corporationsid);
		order.setSellersid(sellersid);
		order.setBuyersid(buyersid);
		order.setProductsid(productsid);
		order.setLoaddateddl(loaddateddl);
		order.setUnloaddateddl(unloaddateddl);
		order.setLoadaddr(loadaddr);
		order.setUnloadaddr(unloadaddr);
		order.setProductweight(productweight);
		order.setProductvol(productvol);
		order.setOrdertime(nowstamp);
		order.createtime(operatesid, nowstamp);
		Long sid = null;
		try {
			sid = orderDaoImpl.insert(order, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		// 推送给公司管理员
		JSONObject pushObject = new JSONObject();
		pushObject.put("type", "00");
		pushObject.put("sid", sid);
		String pushcontent = pushObject.toString();
		if (!PushUtils.Push2Admin(pushcontent, session, order.getCorporationsid(), null, null))
			System.err.println("下单推送给公司管理员失败");
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "下单成功");
		JSONObject contentjsonObject = new JSONObject();
		contentjsonObject.put("sid", sid);
		retObject.put("content", contentjsonObject);
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse ToDispatch(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in OrderHandler-ToDispatch");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
			role = BasicInfo.ROLE_SUPER;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 查询要派发的订单
		String sidstr = params.get("sid");
		String trucksidstr = params.get("trucksid");
		String driversidstr = params.get("driversid");
		String escortsidstr = params.get("escortsid");
		String trailersidstr = params.get("trailersid");
		String routesidstr = params.get("routesid");
		String pricestr = params.get("price");
		String remark = params.get("remark");
		if (sidstr == null || trucksidstr == null || driversidstr == null || escortsidstr == null
				|| trailersidstr == null || routesidstr == null || pricestr == null) {
			String errorstr = "";
			if (sidstr == null)
				errorstr = "请上传sid";
			if (trucksidstr == null)
				errorstr = "请上传trucksid";
			if (driversidstr == null)
				errorstr = "请上传driversid";
			if (escortsidstr == null)
				errorstr = "请上传escortsid";
			if (trailersidstr == null)
				errorstr = "请上传trailersid";
			if (routesidstr == null)
				errorstr = "请上传routesid";
			if (pricestr == null)
				errorstr = "请上传price";
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", errorstr);
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long sid = null, trucksid = null, driversid = null, escortsid = null;
		Long trailersid = null, routesid = null;
		Double price = null;
		Order order = null;
		Truck truck = null;
		Trailer trailer = null;
		Driver driver = null;
		Escort escort = null;
		Route route = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			order = orderDaoImpl.getById(sid, true);
			if (order == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "查无此订单");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			trucksid = Long.parseLong(trucksidstr);
			trailersid = Long.parseLong(trailersidstr);
			driversid = Long.parseLong(driversidstr);
			escortsid = Long.parseLong(escortsidstr);
			routesid = Long.parseLong(routesidstr);
			try {
				truck = truckDaoImpl.getById(trucksid, true);
				trailer = trailerDaoImpl.getById(trailersid, true);
				driver = driverDaoImpl.getById(driversid, true);
				escort = escortDaoImpl.getById(escortsid, true);
				route = routeDaoImpl.getById(routesid, true);
				if (truck == null) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "查无此拖车");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (trailer == null) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "查无此挂车");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (driver == null) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "查无此驾驶员");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (escort == null) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "查无此押运员");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (route == null) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "查无此路线信息");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
			} catch (Exception e) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", e.getMessage());
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "输入格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			price = Double.parseDouble(pricestr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "price格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		if (role == BasicInfo.ROLE_SUPER) {
			corporationsid = order.getCorporationsid();
		} else if (role == BasicInfo.ROLE_ADMIN) {
			if (order.getCorporationsid() != corporationsid) {
				return NettyUtils.getTokenError(iskeepAlive);
			}
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		if (order.getOrderstatus() != Order.STATUS_ORDERED) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "订单当前状态不能调度");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		// 获取订单信息
		if (truck.getTruckstatus() != Truck.TRUCKSTATUS_IDLE) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "对应的拖车已在任务中");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (trailer.getTrailerstatus() != Trailer.STATUS_IDLE) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "对应的挂车已在任务中");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (driver.getStatus() != Escort.STATUS_IDLE) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "对应的驾驶员已在任务中");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (escort.getStatus() != Escort.STATUS_IDLE) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "对应的押运员已在任务中");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Transaction tx = session.beginTransaction();
		// 更新订单
		Long nowstamp = System.currentTimeMillis();
		String oldorder = Utils.getJsonObject(order).toString();
		order.setOrderstatus(Order.STATUS_DISPATCHED);
		order.setTrucksid(trucksid);
		order.setTrailersid(trailersid);
		order.setDriversid(driversid);
		order.setEscortsid(escortsid);
		order.setRoutesid(routesid);
		order.setPrice(price);
		if (remark == null)
			remark = "";
		order.setRemark(remark);
		order.setDispatchtime(nowstamp);
		order.updatetime(operatesid, nowstamp);
		try {
			orderDaoImpl.update(order, oldorder, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// 更新基本信息任务状态
		{
			String oldtruck = Utils.getJsonObject(truck).toString();
			String oldtrailer = Utils.getJsonObject(trailer).toString();
			String olddriver = Utils.getJsonObject(driver).toString();
			String oldescort = Utils.getJsonObject(escort).toString();
			truck.setTruckstatus(Truck.TRUCKSTATUS_TASK);
			truck.updatetime("0");
			trailer.setTrailerstatus(Trailer.STATUS_TASK);
			trailer.updatetime("0");
			driver.setStatus(Driver.STATUS_TASK);
			driver.updatetime("0");
			escort.setStatus(Escort.STATUS_TASK);
			escort.updatetime("0");
			try {
				truckDaoImpl.update(truck, oldtruck, operatesid);
				trailerDaoImpl.update(trailer, oldtrailer, operatesid);
				driverDaoImpl.update(driver, olddriver, operatesid);
				escortDaoImpl.update(escort, oldescort, operatesid);
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", e.getMessage());
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		}
		tx.commit();
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "订单调度成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse ToCheck(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in OrderHandler-ToCheck");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
			role = BasicInfo.ROLE_SUPER;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 查询要安检的订单
		String sidstr = params.get("sid");
		String checkret = "1010101010";
		String checkstatusstr = params.get("checkstatus");
		if (sidstr == null || checkstatusstr == null) {
			String errorstr = "";
			if (sidstr == null)
				errorstr = "请上传sid";
			if (checkstatusstr == null)
				errorstr = "请上传checkstatus";
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", errorstr);
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long sid = null;
		Integer checkstatus = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			checkstatus = Integer.parseInt(checkstatusstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "checkstatus格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Map<String, Object> ret = new HashMap<String, Object>();
		try {
			orderDaoImpl.fetchById(sid, ret, true, Truck.class, Trailer.class, Driver.class, Escort.class, Route.class);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Order order = (Order) ret.get("Order");
		Truck truck = (Truck) ret.get("Truck");
		Trailer trailer = (Trailer) ret.get("Trailer");
		Driver driver = (Driver) ret.get("Driver");
		Escort escort = (Escort) ret.get("Escort");
		Route route = (Route) ret.get("Route");
		if (order == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "查无此订单");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (order.getOrderstatus() != Order.STATUS_DISPATCHED) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "订单当前状态不能安检");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (truck == null || trailer == null || driver == null || escort == null || route == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "数据库错误，sid为" + sid + "的订单的关联数据缺失");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (role == BasicInfo.ROLE_SUPER) {
			corporationsid = order.getCorporationsid();
		} else if (role == BasicInfo.ROLE_ADMIN) {
			if (order.getCorporationsid() != corporationsid) {
				return NettyUtils.getTokenError(iskeepAlive);
			}
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		TruckArchives truckArchives = null;
		try {
			truckArchives = session.createQuery("from TruckArchives where datastatus = " + Bean.CREATED
					+ " and trucknumber = '" + truck.getTrucknumber() + "'", TruckArchives.class).uniqueResult();
			if (truckArchives == null)
				throw new Exception();
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "车辆档案数据库出错");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		boolean annualflag = truckArchives.needannualcheck();
		boolean secondflag = truckArchives.needsecondcheck();
		if (annualflag || secondflag) { // 需要车检，回退订单到下单状态
			Transaction tx = session.beginTransaction();
			String oldtruck = Utils.getJsonObject(truck).toString();
			truck.setTruckstatus(Truck.TRUCKSTATUS_IDLE);
			truck.updatetime("0");
			String oldtrailer = Utils.getJsonObject(trailer).toString();
			trailer.setTrailerstatus(Trailer.STATUS_IDLE);
			trailer.updatetime("0");
			String olddriver = Utils.getJsonObject(driver).toString();
			driver.setStatus(Escort.STATUS_IDLE);
			driver.updatetime("0");
			String oldescort = Utils.getJsonObject(escort).toString();
			escort.setStatus(Escort.STATUS_IDLE);
			escort.updatetime("0");
			try {
				truckDaoImpl.update(truck, oldtruck, Long.parseLong("0"));
				trailerDaoImpl.update(trailer, oldtrailer, operatesid);
				driverDaoImpl.update(driver, olddriver, operatesid);
				escortDaoImpl.update(escort, oldescort, operatesid);
				session.flush();
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", e.getMessage());
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}

			String oldorder = Utils.getJsonObject(order).toString();
			order.setOrderstatus(Order.STATUS_ORDERED);
			order.setTrucksid(null);
			order.setTrailersid(null);
			order.setDriversid(null);
			order.setEscortsid(null);
			order.setRoutesid(null);
			order.setPrice(null);
			order.setRemark(null);
			order.setDispatchtime(null);
			order.updatetime(operatesid);
			try {
				orderDaoImpl.update(order, oldorder, operatesid);
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "安检订单失败：" + e.getMessage());
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			tx.commit();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 0);
			if (annualflag)
				retObject.put("msg", "该车辆需要年检");
			if (secondflag)
				retObject.put("msg", "该车辆需要二级维护");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// 车辆已经车检，根据安检状态改变订单
		Transaction tx = session.beginTransaction();
		// 更新订单
		String oldorder = Utils.getJsonObject(order).toString();
		order.setCheckret(checkret);
		order.setCheckstatus(checkstatus);
		if (checkstatus == 0) {// 通过安检
			order.setOrderstatus(Order.STATUS_CHECKED);
			order.setChecktime(Utils.getCurrenttimeMills());
			order.updatetime(operatesid);
			try {
				orderDaoImpl.update(order, oldorder, operatesid);
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "安检订单失败：" + e.getMessage());
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			tx.commit();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 0);
			retObject.put("msg", "上传订单安检状态成功");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		} else {// 没有通过安检，回退订单状态到下单状态
			String oldtruck = Utils.getJsonObject(truck).toString();
			truck.setTruckstatus(Truck.TRUCKSTATUS_IDLE);
			truck.updatetime("0");
			String oldtrailer = Utils.getJsonObject(trailer).toString();
			trailer.setTrailerstatus(Trailer.STATUS_IDLE);
			trailer.updatetime("0");
			String olddriver = Utils.getJsonObject(driver).toString();
			driver.setStatus(Escort.STATUS_IDLE);
			driver.updatetime("0");
			String oldescort = Utils.getJsonObject(escort).toString();
			escort.setStatus(Escort.STATUS_IDLE);
			escort.updatetime("0");
			try {
				truckDaoImpl.update(truck, oldtruck, Long.parseLong("0"));
				trailerDaoImpl.update(trailer, oldtrailer, operatesid);
				driverDaoImpl.update(driver, olddriver, operatesid);
				escortDaoImpl.update(escort, oldescort, operatesid);
				session.flush();
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", e.getMessage());
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}

			order.setOrderstatus(Order.STATUS_ORDERED);
			order.setTrucksid(null);
			order.setTrailersid(null);
			order.setDriversid(null);
			order.setEscortsid(null);
			order.setRoutesid(null);
			order.setPrice(null);
			order.setRemark(null);
			order.setDispatchtime(null);
			order.updatetime(operatesid);
			try {
				orderDaoImpl.update(order, oldorder, operatesid);
				tx.commit();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 0);
				retObject.put("msg", "上传订单安检状态成功");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "安检订单失败：" + e.getMessage());
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		}
	}

	protected FullHttpResponse ToDistribute(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in OrderHandler-ToDistribute");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
			role = BasicInfo.ROLE_SUPER;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 查询要派发的订单
		String sidstr = params.get("sid");
		if (sidstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传sid");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			orderDaoImpl.fetchById(sid, result, true, Truck.class, Driver.class, Escort.class, Buyer.class);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Order order = (Order) result.get("Order");
		Truck truck = (Truck) result.get("Truck");
		Driver driver = (Driver) result.get("Driver");
		Buyer buyer = (Buyer) result.get("Buyer");
		Escort escort = (Escort) result.get("Escort");
		if (order == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "查无此订单");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (order.getOrderstatus() != Order.STATUS_CHECKED) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "订单当前状态不能派发");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (truck == null || buyer == null || driver == null || escort == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "数据库错误，sid为" + sid + "的订单的关联数据缺失");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (role == BasicInfo.ROLE_SUPER) {
			corporationsid = order.getCorporationsid();
		} else if (role == BasicInfo.ROLE_ADMIN) {
			if (order.getCorporationsid() != corporationsid) {
				return NettyUtils.getTokenError(iskeepAlive);
			}
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		Transaction tx = session.beginTransaction();
		// 创建行车日志数据表
		// Long trucksid = order.getTrucksid();
		// Long ordersid = order.getSid();
		// TruckLogs truckLogs = new TruckLogs();
		// String tablename = "trucklog_" + trucksid + "_" + ordersid;
		// truckLogs.setTrucksid(trucksid);
		// truckLogs.setTrucknumber(truck.getTrucknumber());
		// truckLogs.setCorporationsid_fm_5(corporationsid);
		// truckLogs.setOrdersid(order.getSid());
		// truckLogs.setTablename_nm_30(tablename);
		// truckLogs.setStatus(TruckLogs.STATUS_USING);
		// Utils.createtime(operatesid, truckLogs);
		// Long trucklogsid = MySession.OnSave(truckLogs, session, operatesid);
		// String createtable = "CREATE TABLE `" + tablename + "` (" + "`sid`
		// bigint(10) NOT NULL AUTO_INCREMENT,"
		// + "`distance` Double(10,3) DEFAULT NULL," + "`time` bigint(13)
		// DEFAULT NULL,"
		// + "`gpsx` Double(13,9) DEFAULT NULL," + "`gpsy` Double(13,9) DEFAULT
		// NULL,"
		// + "`speed` Double(10,3) DEFAULT NULL," + "`fuelvol` Double(10,3)
		// DEFAULT NULL,"
		// + "`battery` varchar(100) DEFAULT NULL," + "`lefttirepressure`
		// Double(10,3) DEFAULT NULL,"
		// + "`righttirepressure` Double(10,3) DEFAULT NULL," + "`lefttiretemp`
		// Double(10,3) DEFAULT NULL,"
		// + "`righttiretemp` Double(10,3) DEFAULT NULL," + "`posture`
		// varchar(200) DEFAULT NULL,"
		// + "`lock` varchar(10) DEFAULT NULL," + "`haswarn` int(4) DEFAULT
		// NULL,"
		// + "`warnsid` bigint(10) DEFAULT NULL," + "`datastatus` int(4) DEFAULT
		// NULL,"
		// + "`createdat` bigint(13) DEFAULT NULL," + "`createdid` bigint(10)
		// DEFAULT NULL,"
		// + "`updatedat` bigint(13) DEFAULT NULL," + "`updatedid` bigint(10)
		// DEFAULT NULL,"
		// + "PRIMARY KEY (`sid`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		// try {
		// session.createNativeQuery(createtable).executeUpdate();
		// } catch (Exception e) {
		// tx.rollback();
		// JSONObject retObject = new JSONObject();
		// retObject.put("status", 4);
		// retObject.put("msg", "创建行车日志数据表出错");
		// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		// }
		// 创建费用表单
		FareForm fareForm = new FareForm();
		fareForm.setOrdersid(order.getSid());
		fareForm.setFareformstatus(FareForm.STATUS_CREATED);
		fareForm.setEditable(FareForm.STATUS_EDITABLE);
		fareForm.setCorporationsid(order.getCorporationsid());
		// fareForm.setTrucknumber(truck.getTrucknumber());
		// fareForm.setDrivername(driver.getName());
		// fareForm.setEscortname(escort.getName());
		// fareForm.setBuyername(buyer.getName());
		fareForm.setTrucksid(truck.getSid());
		fareForm.setDriversid(driver.getSid());
		fareForm.setEscortsid(escort.getSid());
		fareForm.setBuyersid(buyer.getSid());
		fareForm.setLoadaddr(order.getLoadaddr());
		fareForm.setUnloadaddr(order.getUnloadaddr());
		fareForm.setPrice(order.getPrice());
		fareForm.setFreight(Double.parseDouble("0"));
		fareForm.createtime(operatesid);
		Long fareFormsid = null;
		try {
			fareFormsid = fareformDaoImpl.insert(fareForm, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// 更新订单
		String oldorder = Utils.getJsonObject(order).toString();
		order.setDistributetime(Utils.getCurrenttimeMills());
		// order.setTrucklogsid(trucklogsid);
		order.setFareformsid(fareFormsid);
		order.setOrderstatus(Order.STATUS_DISTRIBUTED);
		order.updatetime(operatesid);
		try {
			orderDaoImpl.update(order, oldorder, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		// 推送给司机
		JSONObject pushObject = new JSONObject();
		pushObject.put("type", "01");
		pushObject.put("sid", order.getSid());
		String pushcontent = pushObject.toString();
		if (!PushUtils.Push2Driver(order.getDriversid(), pushcontent, session, order.getCorporationsid()))
			System.err.println("订单派发推送给司机失败");
		// 推送给硬件
		if (!PushUtils.Push2Truck(order.getTrucksid(), session, pushcontent))
			System.err.println("订单派发推送给硬件失败");
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "订单派发成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnReceived(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in OrderHandler-OnReceived");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		Order order = null;
		try {
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "driversid", operatesid, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "orderstatus", Order.STATUS_DISTRIBUTED, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
			List<Order> tmpret = orderDaoImpl.getListBy(queryList, true);
			if (tmpret.size() > 1) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "数据库错误，一个司机存在多个活跃的订单");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (tmpret.size() == 0) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			order = tmpret.get(0);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Transaction tx = session.beginTransaction();
		String oldorder = Utils.getJsonObject(order).toString();
		order.setOrderstatus(Order.STATUS_RECEIVED);
		order.setReceivetime(Utils.getCurrenttimeMills());
		order.updatetime(operatesid);
		try {
			orderDaoImpl.update(order, oldorder, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "订单更新成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse AfterLoad(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in OrderHandler-AfterLoad");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_SELLER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		String sidstr = params.get("sid");
		String loadweightstr = params.get("loadweight");
		String zbweightstr = params.get("zbweight");
		if (loadweightstr == null || zbweightstr == null || sidstr == null) {
			String errorstr = "";
			if (loadweightstr == null)
				errorstr = "请上传loadweight";
			if (zbweightstr == null)
				errorstr = "请上传zbweight";
			if (sidstr == null)
				errorstr = "请上传sid";
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", errorstr);
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Double loadweight = null, zbweight = null;
		try {
			loadweight = Double.parseDouble(loadweightstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "loadweight格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			zbweight = Double.parseDouble(zbweightstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "zbweight格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Order order = null;
		Truck truck = null;
		FareForm fareForm = null;
		try {
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "sid", sid, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
			List<Map<String, Object>> tmpret = new ArrayList<Map<String, Object>>();
			orderDaoImpl.fetchListBy(queryList, tmpret, true, Truck.class);
			if (tmpret.size() > 1) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "数据库错误，订单的主键重复");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (tmpret.size() == 0) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			Map<String, Object> tmpMap = tmpret.get(0);
			order = (Order) tmpMap.get("Order");
			truck = (Truck) tmpMap.get("Truck");
			if (order == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (order.getOrderstatus() != Order.STATUS_RECEIVED
					&& order.getOrderstatus() != Order.STATUS_LOADED) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单当前状态不能装货");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (truck == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，分配的拖车不存在");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if (fareForm == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，对应的费用清单不存在");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (fareForm.getEditable() != FareForm.STATUS_EDITABLE) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "费用清单数据库出错，费用清单状态不可编辑");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Transaction tx = session.beginTransaction();
		String oldorder = Utils.getJsonObject(order).toString();
		order.setOrderstatus(Order.STATUS_LOADED);
		order.setLoadtime(Utils.getCurrenttimeMills());
		order.setLoadweight(loadweight);
		order.setZbweight(zbweight);
		order.updatetime(operatesid);
		String oldfareForm = Utils.getJsonObject(fareForm).toString();
		fareForm.setLoaddate(Utils.getCurrenttimeMills());
		fareForm.setLoadweight(loadweight);
		fareForm.setFreight(loadweight * fareForm.getPrice());
		fareForm.setZbweight(zbweight);
		fareForm.updatetime(operatesid);
		try {
			orderDaoImpl.update(order, oldorder, operatesid);
			fareformDaoImpl.update(fareForm, oldfareForm, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		try {
			if (truck.getCid() == null)
				throw new Exception("车辆尚未上传cid");
			JSONObject pushObject = new JSONObject();
			pushObject.put("type", "08");
			pushObject.put("sid", order.getSid());
			String pushcontent = pushObject.toString();
			if (!PushUtils.Push2Truck(truck.getCid(), pushcontent))
				throw new Exception("推送失败");
		} catch (Exception e) {
			System.err.println("订单装货推送给硬件失败：" + e.getMessage());
		}
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "装货成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse AfterUnload(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in OrderHandler-AfterUnload");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_BUYER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		String sidstr = params.get("sid");
		String unloadweightstr = params.get("unloadweight");
		if (unloadweightstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传unloadweight");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Double unloadweight = null;
		try {
			unloadweight = Double.parseDouble(unloadweightstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "unloadweight格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Order order = null;
		Truck truck = null;
		FareForm fareForm = null;
		try {
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "sid", sid, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
			List<Map<String, Object>> tmpret = new ArrayList<Map<String, Object>>();
			orderDaoImpl.fetchListBy(queryList, tmpret, true, Truck.class);
			if (tmpret.size() > 1) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "数据库错误，订单的主键重复");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (tmpret.size() == 0) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			Map<String, Object> tmpMap = tmpret.get(0);
			order = (Order) tmpMap.get("Order");
			truck = (Truck) tmpMap.get("Truck");
			if (order == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (order.getOrderstatus() != Order.STATUS_UNLOADED
					&& order.getOrderstatus() != Order.STATUS_LOADED) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单当前状态不能卸货");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (truck == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，分配的拖车不存在");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if (fareForm == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，对应的费用清单不存在");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (fareForm.getEditable() != FareForm.STATUS_EDITABLE) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "费用清单数据库出错，费用清单状态不可编辑");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Transaction tx = session.beginTransaction();
		String oldorder = Utils.getJsonObject(order).toString();
		order.setOrderstatus(Order.STATUS_UNLOADED);
		order.setUnloadtime(Utils.getCurrenttimeMills());
		order.setUnloadweight(unloadweight);
		order.updatetime(operatesid);
		String oldfareForm = Utils.getJsonObject(fareForm).toString();
		fareForm.setUnloaddate(Utils.getCurrenttimeMills());
		fareForm.updatetime(operatesid);
		try {
			orderDaoImpl.update(order, oldorder, operatesid);
			fareformDaoImpl.update(fareForm, oldfareForm, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		try {
			if (truck.getCid() == null)
				throw new Exception("车辆尚未上传cid");
			JSONObject pushObject = new JSONObject();
			pushObject.put("type", "09");
			pushObject.put("sid", order.getSid());
			String pushcontent = pushObject.toString();
			if (!PushUtils.Push2Truck(truck.getCid(), pushcontent))
				throw new Exception("推送失败");
		} catch (Exception e) {
			System.err.println("订单卸货推送给硬件失败：" + e.getMessage());
		}
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "订单更新成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnReturn(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in OrderHandler-OnReturn");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		String returnaddr = params.get("returnaddr");
		if (returnaddr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传returnaddr");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Order order = null;
		FareForm fareForm = null;
		try {
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "driversid", operatesid, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "orderstatus", Order.STATUS_UNLOADED, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
			List<Order> orders = new ArrayList<Order>();
			orders = orderDaoImpl.getListBy(queryList, true);
			if (orders.size() > 1) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "数据库错误，一个司机对应多个活跃的订单");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (orders.size() == 0) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			order = orders.get(0);
			if (order == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if (fareForm == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，对应的费用清单不存在");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (fareForm.getFareformstatus() != FareForm.STATUS_COMPLETED) {
				JSONObject retObject = new JSONObject();
//				retObject.put("status", 4);
				retObject.put("msg", "请先完善费用信息表，并点击保存按钮");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (fareForm.getEditable() != FareForm.STATUS_UNEDITABLE) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "费用清单数据库出错，费用清单状态出错");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Transaction tx = session.beginTransaction();
		String oldorder = Utils.getJsonObject(order).toString();
		order.setOrderstatus(Order.STATUS_RETURNED);
		order.setReturntime(Utils.getCurrenttimeMills());
		order.setReturnaddr(returnaddr);
		order.updatetime(operatesid);
		String oldfareForm = Utils.getJsonObject(fareForm).toString();
		fareForm.setReturnaddr(returnaddr);
		fareForm.updatetime(operatesid);
		try {
			orderDaoImpl.update(order, oldorder, operatesid);
			fareformDaoImpl.update(fareForm, oldfareForm, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// 更新trucklogs
		// TruckLogs truckLogs = null;
		// try {
		// truckLogs = session.createQuery(
		// "from TruckLogs where datastatus = " + Bean.CREATED + " and sid = " +
		// order.getTrucklogsid(),
		// TruckLogs.class).uniqueResult();
		// } catch (Exception e) {
		// System.err.println("in OrderHandler-OnReturn, while verifystatus ==
		// 0,multi-truckLogs");
		// tx.rollback();
		// JSONObject retObject = new JSONObject();
		// retObject.put("status", 4);
		// retObject.put("msg", "更新行车日志状态错误，请联系系统管理员");
		// retObject.put("content", "");
		// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		// }
		// if (truckLogs == null) {
		// System.err.println("in OrderHandler-OnReturn, while verifystatus ==
		// 0,truckLogs == null");
		// tx.rollback();
		// JSONObject retObject = new JSONObject();
		// retObject.put("status", 4);
		// retObject.put("msg", "更新行车日志状态错误，请联系系统管理员");
		// retObject.put("content", "");
		// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		// }
		// String oldtrucklogs = Utils.getJsonObject(truckLogs).toString();
		// truckLogs.setStatus(TruckLogs.STATUS_COMPLETE);
		// Utils.updatetime(operatesid, truckLogs);
		// MySession.OnUpdate(oldtrucklogs, truckLogs, session, operatesid);
		tx.commit();
		// 推送给公司管理员
		JSONObject pushObject = new JSONObject();
		pushObject.put("type", "02");
		pushObject.put("sid", order.getSid());
		String pushcontent = pushObject.toString();
		if (!PushUtils.Push2Admin(pushcontent, session, order.getCorporationsid(), Admin.DEPT_CHEMICAL, null))
			System.err.println("订单回场推送给管理员失败");
		// 推送给硬件
		if (!PushUtils.Push2Truck(order.getTrucksid(), session, pushcontent))
			System.err.println("订单回场推送给硬件失败");
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "订单更新成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnReVerifyd(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in OrderHandler-OnReVerifyd");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		Order order = null;
		FareForm fareForm = null;
		try {
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "driversid", operatesid, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "orderstatus", Order.STATUS_REVERIFY, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
			List<Order> orders = new ArrayList<Order>();
			orders = orderDaoImpl.getListBy(queryList, true);
			if (orders.size() > 1) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "数据库错误，一个司机对应多个活跃的订单");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (orders.size() == 0) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			order = orders.get(0);
			if (order == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if (fareForm == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，没有对应的费用清单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Transaction tx = session.beginTransaction();
		String oldfareform = Utils.getJsonObject(fareForm).toString();
		fareForm.setEditable(FareForm.STATUS_UNEDITABLE);
		fareForm.updatetime(operatesid);
		try {
			fareformDaoImpl.update(fareForm, oldfareform, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		// 推送给管理员
		JSONObject pushObject = new JSONObject();
		pushObject.put("type", "03");
		pushObject.put("sid", order.getSid());
		String pushcontent = pushObject.toString();
		if (!PushUtils.Push2Admin(pushcontent, session, order.getCorporationsid(), Admin.DEPT_CHEMICAL, null))
			System.err.println("订单重审核提交推送给管理员失败");
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "提交成功成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnVerifyPPP(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in OrderHandler-OnVerifyPPP");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
			role = BasicInfo.ROLE_SUPER;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		String sidstr = params.get("sid");
		String verifystatusstr = params.get("verifystatus");
		String verifyret = params.get("verifyret");
		if (sidstr == null || verifystatusstr == null || verifyret == null) {
			String errorstr = "";
			if (sidstr == null)
				errorstr = "请上传sid";
			if (verifystatusstr == null)
				errorstr = "请上传verifystatus";
			if (verifyret == null)
				errorstr = "请上传verifyret";
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", errorstr);
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long sid = null;
		Integer verifystatus = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			verifystatus = Integer.parseInt(verifystatusstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "verifystatus格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Order order = null;
		FareForm fareForm = null;
		try {
			order = orderDaoImpl.getById(sid, true);
			if (order == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (order.getOrderstatus() != Order.STATUS_RETURNED
					&& order.getOrderstatus() != Order.STATUS_REVERIFY) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单状态变更不合法");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if (fareForm == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，没有对应的费用清单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (role == BasicInfo.ROLE_SUPER) {
			corporationsid = order.getCorporationsid();
		} else if (role == BasicInfo.ROLE_ADMIN) {
			if (order.getCorporationsid() != corporationsid) {
				return NettyUtils.getTokenError(iskeepAlive);
			}
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		Transaction tx = session.beginTransaction();
		String oldorder = Utils.getJsonObject(order).toString();
		order.setVerifyppptime(Utils.getCurrenttimeMills());
		order.setVerifypppstatus(verifystatus);
		order.setVerifypppret(verifyret);
		if (verifystatus == Integer.parseInt("1")) { // 审核不通过
			order.setOrderstatus(Order.STATUS_REVERIFY);
			order.updatetime(operatesid);
			String oldfareform = Utils.getJsonObject(fareForm).toString();
			fareForm.setEditable(FareForm.STATUS_EDITABLE);
			fareForm.updatetime(operatesid);
			try {
				fareformDaoImpl.update(fareForm, oldfareform, operatesid);
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", e.getMessage());
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			// 推送给司机
			JSONObject pushObject = new JSONObject();
			pushObject.put("type", "04");
			pushObject.put("sid", order.getSid());
			String pushcontent = pushObject.toString();
			if (!PushUtils.Push2Driver(order.getDriversid(), pushcontent, session,
					order.getCorporationsid()))
				System.err.println("订单一审没通过推送给司机失败");
		} else {
			order.setOrderstatus(Order.STATUS_VERIFYEDPPP);
			order.updatetime(operatesid);
			// 推送给管理员
			JSONObject pushObject = new JSONObject();
			pushObject.put("type", "04");
			pushObject.put("sid", order.getSid());
			String pushcontent = pushObject.toString();
			if (!PushUtils.Push2Admin(pushcontent, session, order.getCorporationsid(), Admin.DEPT_CHEMICAL, null))
				System.err.println("订单一审通过后推送给管理员失败");
		}
		try {
			orderDaoImpl.update(order, oldorder, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "订单更新成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnVerifyPP(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in OrderHandler-OnVerifyPP");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
			role = BasicInfo.ROLE_SUPER;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		String sidstr = params.get("sid");
		String verifystatusstr = params.get("verifystatus");
		String verifyret = params.get("verifyret");
		if (sidstr == null || verifystatusstr == null || verifyret == null) {
			String errorstr = "";
			if (sidstr == null)
				errorstr = "sid";
			if (verifystatusstr == null)
				errorstr = "请上传verifystatus";
			if (verifyret == null)
				errorstr = "请上传verifyret";
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", errorstr);
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long sid = null;
		Integer verifystatus = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			verifystatus = Integer.parseInt(verifystatusstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "verifystatus格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Order order = null;
		FareForm fareForm = null;
		try {
			order = orderDaoImpl.getById(sid, true);
			if (order == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (order.getOrderstatus() != Order.STATUS_VERIFYEDPPP) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单状态变更不合法");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if (fareForm == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，没有对应的费用清单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (role == BasicInfo.ROLE_SUPER) {
			corporationsid = order.getCorporationsid();
		} else if (role == BasicInfo.ROLE_ADMIN) {
			if (order.getCorporationsid() != corporationsid) {
				return NettyUtils.getTokenError(iskeepAlive);
			}
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		Transaction tx = session.beginTransaction();
		String oldorder = Utils.getJsonObject(order).toString();
		order.setVerifypptime(Utils.getCurrenttimeMills());
		order.setVerifyppstatus(verifystatus);
		order.setVerifyppret(verifyret);
		if (verifystatus == Integer.parseInt("1")) { // 审核不通过
			order.setOrderstatus(Order.STATUS_REVERIFY);
			order.updatetime(operatesid);
			String oldfareform = Utils.getJsonObject(fareForm).toString();
			fareForm.setEditable(FareForm.STATUS_EDITABLE);
			fareForm.updatetime(operatesid);
			try {
				fareformDaoImpl.update(fareForm, oldfareform, operatesid);
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", e.getMessage());
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			// 推送给司机
			JSONObject pushObject = new JSONObject();
			pushObject.put("type", "05");
			pushObject.put("sid", order.getSid());
			String pushcontent = pushObject.toString();
			if (!PushUtils.Push2Driver(order.getDriversid(), pushcontent, session,
					order.getCorporationsid()))
				System.err.println("订单二审没通过推送给司机失败");
		} else {
			order.setOrderstatus(Order.STATUS_VERIFYEDPP);
			order.updatetime(operatesid);
			// 推送给管理员
			JSONObject pushObject = new JSONObject();
			pushObject.put("type", "05");
			pushObject.put("sid", order.getSid());
			String pushcontent = pushObject.toString();
			if (!PushUtils.Push2Admin(pushcontent, session, order.getCorporationsid(), Admin.DEPT_FINANCE, null))
				System.err.println("订单二审通过后推送给管理员失败");
		}
		try {
			orderDaoImpl.update(order, oldorder, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "订单更新成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnVerifyP(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in OrderHandler-OnVerifyP");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
			role = BasicInfo.ROLE_SUPER;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		String sidstr = params.get("sid");
		String verifystatusstr = params.get("verifystatus");
		String verifyret = params.get("verifyret");
		if (sidstr == null || verifystatusstr == null || verifyret == null) {
			String errorstr = "";
			if (sidstr == null)
				errorstr = "请上传sid";
			if (verifystatusstr == null)
				errorstr = "请上传verifystatus";
			if (verifyret == null)
				errorstr = "请上传verifyret";
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", errorstr);
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long sid = null;
		Integer verifystatus = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			verifystatus = Integer.parseInt(verifystatusstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "verifystatus格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Order order = null;
		FareForm fareForm = null;
		try {
			order = orderDaoImpl.getById(sid, true);
			if (order == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (order.getOrderstatus() != Order.STATUS_VERIFYEDPP) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单状态变更不合法");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if (fareForm == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，没有对应的费用清单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (role == BasicInfo.ROLE_SUPER) {
			corporationsid = order.getCorporationsid();
		} else if (role == BasicInfo.ROLE_ADMIN) {
			if (order.getCorporationsid() != corporationsid) {
				return NettyUtils.getTokenError(iskeepAlive);
			}
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		Transaction tx = session.beginTransaction();
		String oldorder = Utils.getJsonObject(order).toString();
		order.setVerifyptime(Utils.getCurrenttimeMills());
		order.setVerifypstatus(verifystatus);
		order.setVerifypret(verifyret);
		if (verifystatus == Integer.parseInt("1")) { // 审核不通过
			order.setOrderstatus(Order.STATUS_REVERIFY);
			order.updatetime(operatesid);
			String oldfareform = Utils.getJsonObject(fareForm).toString();
			fareForm.setEditable(FareForm.STATUS_EDITABLE);
			fareForm.updatetime(operatesid);
			try {
				fareformDaoImpl.update(fareForm, oldfareform, operatesid);
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", e.getMessage());
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			// 推送给司机
			JSONObject pushObject = new JSONObject();
			pushObject.put("type", "06");
			pushObject.put("sid", order.getSid());
			String pushcontent = pushObject.toString();
			if (!PushUtils.Push2Driver(order.getDriversid(), pushcontent, session,
					order.getCorporationsid()))
				System.err.println("订单三审没通过推送给司机失败");
		} else {
			order.setOrderstatus(Order.STATUS_VERIFYEDP);
			order.updatetime(operatesid);
			// 推送给管理员
			JSONObject pushObject = new JSONObject();
			pushObject.put("type", "06");
			pushObject.put("sid", order.getSid());
			String pushcontent = pushObject.toString();
			if (!PushUtils.Push2Admin(pushcontent, session, order.getCorporationsid(), Admin.DEPT_MANAGER, null))
				System.err.println("订单三审通过后推送给管理员失败");
		}
		try {
			orderDaoImpl.update(order, oldorder, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "订单更新成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnVerify(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in OrderHandler-OnVerify");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
			role = BasicInfo.ROLE_SUPER;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		String sidstr = params.get("sid");
		String verifystatusstr = params.get("verifystatus");
		String verifyret = params.get("verifyret");
		if (sidstr == null || verifystatusstr == null || verifyret == null) {
			String errorstr = "";
			if (sidstr == null)
				errorstr = "请上传sid";
			if (verifystatusstr == null)
				errorstr = "请上传verifystatus";
			if (verifyret == null)
				errorstr = "请上传verifyret";
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", errorstr);
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long sid = null;
		Integer verifystatus = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			verifystatus = Integer.parseInt(verifystatusstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "verifystatus格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Order order = null;
		FareForm fareForm = null;
		Corporation corporation = null;
		Truck truck = null;
		Driver driver = null;
		Seller seller = null;
		Trailer trailer = null;
		Escort escort = null;
		try {
			Map<String, Object> result = new HashMap<String, Object>();
			orderDaoImpl.fetchById(sid, result, true, Corporation.class, Truck.class, Driver.class,
					Seller.class, Trailer.class, Escort.class);
			order = (Order) result.get("Order");
			corporation = (Corporation) result.get("Corporation");
			truck = (Truck) result.get("Truck");
			driver = (Driver) result.get("Driver");
			seller = (Seller) result.get("Seller");
			trailer = (Trailer) result.get("Trailer");
			escort = (Escort) result.get("Escort");
			if (order == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (order.getOrderstatus() != Order.STATUS_VERIFYEDP) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单状态变更不合法");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if (fareForm == null || seller == null || driver == null || truck == null || corporation == null
					|| trailer == null || escort == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，关联信息被删除");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (role == BasicInfo.ROLE_SUPER) {
			corporationsid = order.getCorporationsid();
		} else if (role == BasicInfo.ROLE_ADMIN) {
			if (order.getCorporationsid() != corporationsid) {
				return NettyUtils.getTokenError(iskeepAlive);
			}
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		Transaction tx = session.beginTransaction();
		String oldorder = Utils.getJsonObject(order).toString();
		order.setVerifytime(Utils.getCurrenttimeMills());
		order.setVerifystatus(verifystatus);
		order.setVerifyret(verifyret);
		if (verifystatus == Integer.parseInt("1")) { // 审核不通过
			order.setOrderstatus(Order.STATUS_REVERIFY);
			order.updatetime(operatesid);
			String oldfareform = Utils.getJsonObject(fareForm).toString();
			fareForm.setEditable(FareForm.STATUS_EDITABLE);
			fareForm.updatetime(operatesid);
			try {
				orderDaoImpl.update(order, oldorder, operatesid);
				fareformDaoImpl.update(fareForm, oldfareform, operatesid);
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", e.getMessage());
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			// 推送给司机
			JSONObject pushObject = new JSONObject();
			pushObject.put("type", "07");
			pushObject.put("sid", order.getSid());
			String pushcontent = pushObject.toString();
			if (!PushUtils.Push2Driver(order.getDriversid(), pushcontent, session,
					order.getCorporationsid()))
				System.err.println("订单四审没通过推送给司机失败");
		} else {
			// 更新订单统计，更新产值统计，更新里程统计，更新耗油统计
			{
				// 计算油耗
				List<TruckLog> earlyresult = new ArrayList<TruckLog>();
				List<TruckLog> newresult = new ArrayList<TruckLog>();
				List<QueryBean> queryList = new ArrayList<QueryBean>();
				queryList.add(new QueryBean(TruckLog.class.getName(), "trucklog", "datastatus", Bean.CREATED, QueryBean.TYPE_EQUAL));
				queryList.add(new QueryBean(TruckLog.class.getName(), "trucklog", "ordersid", order.getSid(), QueryBean.TYPE_EQUAL));
				try {
					TrucklogDaoImpl trucklogDaoImpl = new TrucklogDaoImpl(session);
					earlyresult = trucklogDaoImpl.getListBy(queryList, true, 1, 1, MulTabBaseDaoImpl.QUERY_ORDER_ASC,
							"createdat");
					newresult = trucklogDaoImpl.getListBy(queryList, true, 1, 1, MulTabBaseDaoImpl.QUERY_ORDER_DESC,
							"createdat");
				} catch (Exception e) {
					tx.rollback();
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", e.getMessage());
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				if (earlyresult.size() > 1 || newresult.size() > 1) {
					tx.rollback();
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "数据库未知错误，limit1返回多个结果");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				Double fuelused = null;
				if (earlyresult.size() == 0 || newresult.size() == 0) {
					fuelused = Double.parseDouble("0.0");
				} else {
					TruckLog firsttruckLog = earlyresult.get(0);
					TruckLog lasttruckLog = newresult.get(0);
					Double fuelvolfirst = firsttruckLog.getFuelvol();
					Double fuelvollast = lasttruckLog.getFuelvol();
					Double addfuel = fareForm.getAddfuelvol();
					fuelused = fuelvolfirst + addfuel - fuelvollast;
				}
				// 计算里程
				Double distance = null;
				if (newresult.size() == 0)
					distance = Double.parseDouble("0");
				else
					distance = newresult.get(0).getDistance();
				// 计算利润
				Double output = fareForm.getFreight();
				// 更新订单
				order.setOrderstatus(Order.STATUS_VERIFYED);
				order.setFuelused(fuelused);
				order.setDistance(distance);
				order.setOutput(output);
				order.updatetime(operatesid);
				try {
					orderDaoImpl.update(order, oldorder, operatesid);
				} catch (Exception e) {
					tx.rollback();
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", e.getMessage());
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				// 计算年月
				String[] tmp = Utils.getYearMonth();
				String year = tmp[0];
				String month = tmp[1];
				// 创建承运方月统计表
				OrderMonthStatistics omsCorporation = null;
				try {
					omsCorporation = session.createQuery(
							"from OrderMonthStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
									+ StatisticsStatic.OBJTECTYPE_CORPORATION + " and year = '" + year
									+ "' and month= '" + month + "' and objectsid = " + order.getCorporationsid(),
							OrderMonthStatistics.class).uniqueResult();
				} catch (Exception e) {
					tx.rollback();
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "承运方订单月统计数据库出错");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (omsCorporation == null) {
					omsCorporation = new OrderMonthStatistics(order.getCorporationsid(), year, month,
							order.getCorporationsid(), StatisticsStatic.OBJTECTYPE_CORPORATION);
					omsCorporation.setObjectname(corporation.getName());
					omsCorporation.addOrderAmount();
					omsCorporation.addFuelUsed(fuelused);
					omsCorporation.addDistance(distance);
					omsCorporation.addOutput(output);
					omsCorporation.createtime("0");
					MySession.OnSave(omsCorporation, session, "0");
				} else {
					String oldomsCorporation = Utils.getJsonObject(omsCorporation).toString();
					omsCorporation.setObjectname(corporation.getName());
					omsCorporation.addOrderAmount();
					omsCorporation.addFuelUsed(fuelused);
					omsCorporation.addDistance(distance);
					omsCorporation.addOutput(output);
					omsCorporation.updatetime("0");
					MySession.OnUpdate(oldomsCorporation, omsCorporation, session, "0");
				}
				// 创建驾驶员月统计表
				OrderMonthStatistics omsDriver = null;
				try {
					omsDriver = session.createQuery(
							"from OrderMonthStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
									+ StatisticsStatic.OBJTECTYPE_DRIVER + " and year = '" + year + "' and month= '"
									+ month + "' and objectsid = " + order.getDriversid(),
							OrderMonthStatistics.class).uniqueResult();
				} catch (Exception e) {
					tx.rollback();
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "驾驶员订单月统计数据库出错");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (omsDriver == null) {
					omsDriver = new OrderMonthStatistics(order.getCorporationsid(), year, month,
							order.getDriversid(), StatisticsStatic.OBJTECTYPE_DRIVER);
					omsDriver.setObjectname(driver.getName());
					omsDriver.addOrderAmount();
					omsDriver.addFuelUsed(fuelused);
					omsDriver.addDistance(distance);
					omsDriver.addOutput(output);
					omsDriver.createtime("0");
					MySession.OnSave(omsDriver, session, "0");
				} else {
					String oldomsDriver = Utils.getJsonObject(omsDriver).toString();
					omsDriver.setObjectname(driver.getName());
					omsDriver.addOrderAmount();
					omsDriver.addFuelUsed(fuelused);
					omsDriver.addDistance(distance);
					omsDriver.addOutput(output);
					omsDriver.updatetime("0");
					MySession.OnUpdate(oldomsDriver, omsDriver, session, "0");
				}
				// 创建托运方月统计表
				OrderMonthStatistics omsSeller = null;
				try {
					omsSeller = session.createQuery(
							"from OrderMonthStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
									+ StatisticsStatic.OBJTECTYPE_SELLER + " and year = '" + year + "' and month= '"
									+ month + "' and objectsid = " + order.getSellersid(),
							OrderMonthStatistics.class).uniqueResult();
				} catch (Exception e) {
					tx.rollback();
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "托运方订单月统计数据库出错");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (omsSeller == null) {
					omsSeller = new OrderMonthStatistics(order.getCorporationsid(), year, month,
							order.getSellersid(), StatisticsStatic.OBJTECTYPE_SELLER);
					omsSeller.setObjectname(seller.getName());
					omsSeller.addOrderAmount();
					omsSeller.addFuelUsed(fuelused);
					omsSeller.addDistance(distance);
					omsSeller.addOutput(output);
					omsSeller.createtime("0");
					MySession.OnSave(omsSeller, session, "0");
				} else {
					String oldomsSeller = Utils.getJsonObject(omsSeller).toString();
					omsSeller.setObjectname(seller.getName());
					omsSeller.addOrderAmount();
					omsSeller.addFuelUsed(fuelused);
					omsSeller.addDistance(distance);
					omsSeller.addOutput(output);
					omsSeller.updatetime("0");
					MySession.OnUpdate(oldomsSeller, omsSeller, session, "0");
				}
				// 创建货车月统计表
				OrderMonthStatistics omsTruck = null;
				try {
					omsTruck = session.createQuery(
							"from OrderMonthStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
									+ StatisticsStatic.OBJTECTYPE_TRUCK + " and year = '" + year + "' and month= '"
									+ month + "' and objectsid = " + order.getTrucksid(),
							OrderMonthStatistics.class).uniqueResult();
				} catch (Exception e) {
					tx.rollback();
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "货车订单月统计数据库出错");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (omsTruck == null) {
					omsTruck = new OrderMonthStatistics(order.getCorporationsid(), year, month,
							order.getTrucksid(), StatisticsStatic.OBJTECTYPE_TRUCK);
					omsTruck.setObjectname(truck.getTrucknumber());
					omsTruck.addOrderAmount();
					omsTruck.addFuelUsed(fuelused);
					omsTruck.addDistance(distance);
					omsTruck.addOutput(output);
					omsTruck.createtime("0");
					MySession.OnSave(omsTruck, session, "0");
				} else {
					String oldomsTruck = Utils.getJsonObject(omsTruck).toString();
					omsTruck.setObjectname(truck.getTrucknumber());
					omsTruck.addOrderAmount();
					omsTruck.addFuelUsed(fuelused);
					omsTruck.addDistance(distance);
					omsTruck.addOutput(output);
					omsTruck.updatetime("0");
					MySession.OnUpdate(oldomsTruck, omsTruck, session, "0");
				}
				// 创建承运方年统计表
				OrderYearStatistics oysCorporation = null;
				try {
					oysCorporation = session
							.createQuery(
									"from OrderYearStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
											+ StatisticsStatic.OBJTECTYPE_CORPORATION + " and year = '" + year
											+ "' and objectsid = " + order.getCorporationsid(),
									OrderYearStatistics.class)
							.uniqueResult();
				} catch (Exception e) {
					tx.rollback();
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "承运方订单年统计数据库出错");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (oysCorporation == null) {
					oysCorporation = new OrderYearStatistics(order.getCorporationsid(), year,
							order.getCorporationsid(), StatisticsStatic.OBJTECTYPE_CORPORATION);
					oysCorporation.setObjectname(corporation.getName());
					oysCorporation.addOrderAmount();
					oysCorporation.addFuelUsed(fuelused);
					oysCorporation.addDistance(distance);
					oysCorporation.addOutput(output);
					oysCorporation.createtime("0");
					MySession.OnSave(oysCorporation, session, "0");
				} else {
					String oldoysCorporation = Utils.getJsonObject(oysCorporation).toString();
					oysCorporation.setObjectname(corporation.getName());
					oysCorporation.addOrderAmount();
					oysCorporation.addFuelUsed(fuelused);
					oysCorporation.addDistance(distance);
					oysCorporation.addOutput(output);
					oysCorporation.updatetime("0");
					MySession.OnUpdate(oldoysCorporation, oysCorporation, session, "0");
				}
				// 创建驾驶员年统计表
				OrderYearStatistics oysDriver = null;
				try {
					oysDriver = session
							.createQuery(
									"from OrderYearStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
											+ StatisticsStatic.OBJTECTYPE_DRIVER + " and year = '" + year
											+ "' and objectsid = " + order.getDriversid(),
									OrderYearStatistics.class)
							.uniqueResult();
				} catch (Exception e) {
					tx.rollback();
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "驾驶员订单年统计数据库出错");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (oysDriver == null) {
					oysDriver = new OrderYearStatistics(order.getCorporationsid(), year,
							order.getDriversid(), StatisticsStatic.OBJTECTYPE_DRIVER);
					oysDriver.setObjectname(driver.getName());
					oysDriver.addOrderAmount();
					oysDriver.addFuelUsed(fuelused);
					oysDriver.addDistance(distance);
					oysDriver.addOutput(output);
					oysDriver.createtime("0");
					MySession.OnSave(oysDriver, session, "0");
				} else {
					String oldoysDriver = Utils.getJsonObject(oysDriver).toString();
					oysDriver.setObjectname(driver.getName());
					oysDriver.addOrderAmount();
					oysDriver.addFuelUsed(fuelused);
					oysDriver.addDistance(distance);
					oysDriver.addOutput(output);
					oysDriver.updatetime("0");
					MySession.OnUpdate(oldoysDriver, oysDriver, session, "0");
				}
				// 创建托运方年统计表
				OrderYearStatistics oysSeller = null;
				try {
					oysSeller = session
							.createQuery(
									"from OrderYearStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
											+ StatisticsStatic.OBJTECTYPE_SELLER + " and year = '" + year
											+ "' and objectsid = " + order.getSellersid(),
									OrderYearStatistics.class)
							.uniqueResult();
				} catch (Exception e) {
					tx.rollback();
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "托运方订单年统计数据库出错");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (oysSeller == null) {
					oysSeller = new OrderYearStatistics(order.getCorporationsid(), year,
							order.getSellersid(), StatisticsStatic.OBJTECTYPE_SELLER);
					oysSeller.setObjectname(seller.getName());
					oysSeller.addOrderAmount();
					oysSeller.addFuelUsed(fuelused);
					oysSeller.addDistance(distance);
					oysSeller.addOutput(output);
					oysSeller.createtime("0");
					MySession.OnSave(oysSeller, session, "0");
				} else {
					String oldoysSeller = Utils.getJsonObject(oysSeller).toString();
					oysSeller.setObjectname(seller.getName());
					oysSeller.addOrderAmount();
					oysSeller.addFuelUsed(fuelused);
					oysSeller.addDistance(distance);
					oysSeller.addOutput(output);
					oysSeller.updatetime("0");
					MySession.OnUpdate(oldoysSeller, oysSeller, session, "0");
				}
				// 创建货车年统计表
				OrderYearStatistics oysTruck = null;
				try {
					oysTruck = session
							.createQuery(
									"from OrderYearStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
											+ StatisticsStatic.OBJTECTYPE_TRUCK + " and year = '" + year
											+ "' and objectsid = " + order.getTrucksid(),
									OrderYearStatistics.class)
							.uniqueResult();
				} catch (Exception e) {
					tx.rollback();
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "货车订单年统计数据库出错");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				if (oysTruck == null) {
					oysTruck = new OrderYearStatistics(order.getCorporationsid(), year, order.getTrucksid(),
							StatisticsStatic.OBJTECTYPE_TRUCK);
					oysTruck.setObjectname(truck.getTrucknumber());
					oysTruck.addOrderAmount();
					oysTruck.addFuelUsed(fuelused);
					oysTruck.addDistance(distance);
					oysTruck.addOutput(output);
					oysTruck.createtime("0");
					MySession.OnSave(oysTruck, session, "0");
				} else {
					String oldoysTruck = Utils.getJsonObject(oysTruck).toString();
					oysTruck.setObjectname(truck.getTrucknumber());
					oysTruck.addOrderAmount();
					oysTruck.addFuelUsed(fuelused);
					oysTruck.addDistance(distance);
					oysTruck.addOutput(output);
					oysTruck.updatetime("0");
					MySession.OnUpdate(oldoysTruck, oysTruck, session, "0");
				}
			}
			// 更新任务状态
			{
				String oldtruck = Utils.getJsonObject(truck).toString();
				truck.setTruckstatus(Truck.TRUCKSTATUS_IDLE);
				truck.updatetime(operatesid);
				String oldtrailer = Utils.getJsonObject(trailer).toString();
				trailer.setTrailerstatus(Trailer.STATUS_IDLE);
				trailer.updatetime(operatesid);
				String olddriver = Utils.getJsonObject(driver).toString();
				driver.setStatus(Escort.STATUS_IDLE);
				driver.updatetime(operatesid);
				String oldescort = Utils.getJsonObject(escort).toString();
				escort.setStatus(Escort.STATUS_IDLE);
				escort.updatetime(operatesid);
				try {
					truckDaoImpl.update(truck, oldtruck, operatesid);
					trailerDaoImpl.update(trailer, oldtrailer, operatesid);
					driverDaoImpl.update(driver, olddriver, operatesid);
					escortDaoImpl.update(escort, oldescort, operatesid);
				} catch (Exception e) {
					tx.rollback();
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", e.getMessage());
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
			}
		}
		tx.commit();
		// 推送给领导？
		JSONObject pushObject = new JSONObject();
		pushObject.put("type", "07");
		pushObject.put("sid", order.getSid());
		String pushcontent = pushObject.toString();
		if (!PushUtils.Push2Admin(pushcontent, session, order.getCorporationsid(), Admin.DEPT_MANAGER, null))
			System.err.println("订单四审通过后推送个管理员失败");
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "订单更新成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnQuery(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in OrderHandler-OnQuery");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_SELLER || operateusertype == LoginInfo.TYPE_BUYER
				|| operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {
			role = BasicInfo.ROLE_CORPORATION;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {
			role = BasicInfo.ROLE_GOVERNMENT;
			String corporationsidstr = params.get("corporationsid");
			if (corporationsidstr == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请上传corporationsid");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			corporationsid = null;
			try {
				corporationsid = Long.parseLong(corporationsidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "corporationsid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			LoginInfo logininfo_checkcorporation = null;
			try {
				logininfo_checkcorporation = session.createQuery("from LoginInfo where datastatus = " + Bean.CREATED
						+ " and type = " + LoginInfo.TYPE_CORPORATION + " and usersid = " + corporationsid
						+ " and corporationsid = " + corporationsid, LoginInfo.class).uniqueResult();
			} catch (Exception e) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "承运方信息数据库出错");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (logininfo_checkcorporation == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "corporation信息不存在");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		// SELECT SQL_CALC_FOUND_ROWS * FROM orderp_1 WHERE sid = 1 ORDER BY
		// createdat LIMIT 0,10;
		// SELECT FOUND_ROWS()
		Map<String, String> queryMap = Utils.paramsqueryfilter(params, role, corporationsid, operatesid);
		if (operateusertype == LoginInfo.TYPE_BUYER)
			queryMap.put("buyersid", String.valueOf(operatesid));
		if (operateusertype == LoginInfo.TYPE_SELLER)
			queryMap.put("sellersid", String.valueOf(operatesid));
		if (operateusertype == LoginInfo.TYPE_DRIVER)
			queryMap.put("driversid", String.valueOf(operatesid));
		Iterator<Entry<String, String>> iterator = queryMap.entrySet().iterator();
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String field = entry.getKey();
			String value = entry.getValue();
			int valuetype = QueryBean.TYPE_EQUAL;
			try {
				new JSONObject(value);
				valuetype = QueryBean.TYPE_JSONOBJECT;
			} catch (Exception e) {
				try {
					new JSONArray(value);
					valuetype = QueryBean.TYPE_JSONARRAY;
				} catch (Exception e1) {
				}
			}
			queryList.add(new QueryBean(Order.class.getName(), "orderp", field, value, valuetype));
		}
		String limit = params.get("limit");
		Integer intpage = 0;
		Integer intlimit = 0;
		if (limit != null) {
			intlimit = Integer.valueOf(limit);
			String page = params.get("page");
			if (page == null)
				page = "1";
			intpage = Integer.valueOf(page);
		} else {
			intlimit = null;
			intpage = null;
		}
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Long total = null;
		try {
			orderDaoImpl.fetchListBy(queryList, result, true, intlimit, intpage, Corporation.class, Seller.class,
					Buyer.class, Product.class, Truck.class, Trailer.class, Driver.class, Escort.class, Route.class);
			total = orderDaoImpl.selectCount(queryList, true);
		} catch (Exception e) {
			JSONObject jsonObject2 = new JSONObject();
			jsonObject2.put("status", 4);
			jsonObject2.put("msg", e.getMessage());
			jsonObject2.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
		}

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询订单信息成功");
		JSONObject contentJsonObject = new JSONObject();
		if (limit != null) {
			if (intlimit * (intpage - 1) >= total) {
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("status", 4);
				jsonObject2.put("msg", "第" + intpage + "页不存在");
				jsonObject2.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
			}
			contentJsonObject.put("total", total);
			contentJsonObject.put("perpage", limit);
			contentJsonObject.put("current_page", intpage);
			contentJsonObject.put("from", (intpage - 1) * intlimit + 1);
			if (intpage * intlimit > total)
				contentJsonObject.put("to", total);
			else
				contentJsonObject.put("to", intpage * intlimit);
		} else {
			contentJsonObject.put("total", total);
			contentJsonObject.put("perpage", total);
			contentJsonObject.put("current_page", 1);
			contentJsonObject.put("from", 1);
			contentJsonObject.put("to", total);
		}
		JSONArray dataArray = new JSONArray();
		for (int i = 0; i < result.size(); i++) {
			Map<String, Object> tmpret = result.get(i);
			Order order = (Order) tmpret.get("Order");
			Corporation corporation = (Corporation) tmpret.get("Corporation");
			Seller seller = (Seller) tmpret.get("Seller");
			Buyer buyer = (Buyer) tmpret.get("Buyer");
			Product product = (Product) tmpret.get("Product");
			Truck truck = (Truck) tmpret.get("Truck");
			Trailer trailer = (Trailer) tmpret.get("Trailer");
			Driver driver = (Driver) tmpret.get("Driver");
			Escort escort = (Escort) tmpret.get("Escort");
			Route route = (Route) tmpret.get("Route");
			JSONObject tmpjsonObject = Utils.getOrderJsonObjectWithPremission(order, role);
			tmpjsonObject.put("corporationname", corporation == null ? "null" : corporation.getName());
			tmpjsonObject.put("sellername", seller == null ? "null" : seller.getName());
			tmpjsonObject.put("sellerphone", seller == null ? "null" : seller.getPhone());
			tmpjsonObject.put("buyername", buyer == null ? "null" : buyer.getName());
			tmpjsonObject.put("buyerphone", buyer == null ? "null" : buyer.getPhone());
			tmpjsonObject.put("productname", product == null ? "null" : product.getName());
			tmpjsonObject.put("producttype", product == null ? "null" : product.getType());
			tmpjsonObject.put("packettype", product == null ? "null" : product.getPackettype());
			tmpjsonObject.put("trucknumber", truck == null ? "null" : truck.getTrucknumber());
			tmpjsonObject.put("trailernumber", trailer == null ? "null" : trailer.getTrailernumber());
			tmpjsonObject.put("drivername", driver == null ? "null" : driver.getName());
			tmpjsonObject.put("escortname", escort == null ? "null" : escort.getName());
			tmpjsonObject.put("routename", route == null ? "null" : route.getName());
			tmpjsonObject.put("routedistance", route == null ? "null" : route.getTransportdistance());
			dataArray.put(tmpjsonObject);
		}
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse ToDistributeForTest(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in OrderHandler-ToDistribute");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
			role = BasicInfo.ROLE_SUPER;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 查询要派发的订单
		String sidstr = params.get("sid");
		if (sidstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传sid");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Map<String, Object> result = new HashMap<String, Object>();
		try {
			orderDaoImpl.fetchById(sid, result, true, Truck.class, Driver.class, Escort.class, Buyer.class);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Order order = (Order) result.get("Order");
		Truck truck = (Truck) result.get("Truck");
		Driver driver = (Driver) result.get("Driver");
		Buyer buyer = (Buyer) result.get("Buyer");
		Escort escort = (Escort) result.get("Escort");
		if (order == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "查无此订单");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (truck == null || buyer == null || driver == null || escort == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "数据库错误，sid为" + sid + "的订单的关联数据缺失");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (order.getOrderstatus() != Order.STATUS_CHECKED) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "订单当前状态不能派发");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (role == BasicInfo.ROLE_SUPER) {
			corporationsid = order.getCorporationsid();
		} else if (role == BasicInfo.ROLE_ADMIN) {
			if (order.getCorporationsid() != corporationsid) {
				return NettyUtils.getTokenError(iskeepAlive);
			}
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		Transaction tx = session.beginTransaction();
		// 创建行车日志数据表
		// Long trucksid = order.getTrucksid();
		// Long ordersid = order.getSid();
		// TruckLogs truckLogs = new TruckLogs();
		// String tablename = "trucklog_" + trucksid + "_" + ordersid;
		// truckLogs.setTrucksid(trucksid);
		// truckLogs.setTrucknumber(truck.getTrucknumber());
		// truckLogs.setCorporationsid_fm_5(corporationsid);
		// truckLogs.setOrdersid(order.getSid());
		// truckLogs.setTablename_nm_30(tablename);
		// truckLogs.setStatus(TruckLogs.STATUS_USING);
		// Utils.createtime(operatesid, truckLogs);
		// Long trucklogsid = MySession.OnSave(truckLogs, session, operatesid);
		// String createtable = "CREATE TABLE `" + tablename + "` (" + "`sid`
		// bigint(10) NOT NULL AUTO_INCREMENT,"
		// + "`distance` Double(10,3) DEFAULT NULL," + "`time` bigint(13)
		// DEFAULT NULL,"
		// + "`gpsx` Double(13,9) DEFAULT NULL," + "`gpsy` Double(13,9) DEFAULT
		// NULL,"
		// + "`speed` Double(10,3) DEFAULT NULL," + "`fuelvol` Double(10,3)
		// DEFAULT NULL,"
		// + "`lefttirepressure` Double(10,3) DEFAULT NULL," +
		// "`righttirepressure` Double(10,3) DEFAULT NULL,"
		// + "`lefttiretemp` Double(10,3) DEFAULT NULL," + "`righttiretemp`
		// Double(10,3) DEFAULT NULL,"
		// + "`posture` varchar(200) DEFAULT NULL," + "`lock` varchar(10)
		// DEFAULT NULL,"
		// + "`haswarn` int(4) DEFAULT NULL," + "`warnsid` bigint(10) DEFAULT
		// NULL,"
		// + "`datastatus` int(4) DEFAULT NULL," + "`createdat` bigint(13)
		// DEFAULT NULL,"
		// + "`createdid` bigint(10) DEFAULT NULL," + "`updatedat` bigint(13)
		// DEFAULT NULL,"
		// + "`updatedid` bigint(10) DEFAULT NULL," + "PRIMARY KEY (`sid`)"
		// + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		// try {
		// session.createNativeQuery(createtable).executeUpdate();
		// } catch (Exception e) {
		// tx.rollback();
		// JSONObject retObject = new JSONObject();
		// retObject.put("status", 4);
		// retObject.put("msg", "创建行车日志数据表出错");
		// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		// }
		TruckLog truckLog = new TruckLog();
		truckLog.setTime(Utils.getCurrenttimeMills());
		truckLog.setDistance(Double.parseDouble("100"));
		truckLog.setGpsx(Double.parseDouble("111.11111"));
		truckLog.setGpsy(Double.parseDouble("222.22222"));
		truckLog.setSpeed(Double.parseDouble("333.33"));
		truckLog.setFuelvol(Double.parseDouble("444.44"));
		truckLog.setLefttirepressure(Double.parseDouble("5555.55"));
		truckLog.setRighttirepressure(Double.parseDouble("6666.66"));
		truckLog.setLefttiretemp(Double.parseDouble("7777.77"));
		truckLog.setRighttiretemp(Double.parseDouble("8888.88"));
		truckLog.setPosture("某种姿态");
		truckLog.setLock("0-0-1-1-0");
		truckLog.setHaswarn(Integer.parseInt("0"));
		truckLog.setWarnsid(null);
		truckLog.setCreatedat(Utils.getCurrenttimeMills());
		truckLog.setCreatedid(Long.parseLong("0"));
		truckLog.setUpdatedat(Utils.getCurrenttimeMills());
		truckLog.setUpdatedid(Long.parseLong("0"));
		TrucklogDaoImpl trucklogDaoImpl = new TrucklogDaoImpl(session);
		trucklogDaoImpl.insert(truckLog, operatesid);
		// 创建费用表单
		FareForm fareForm = new FareForm();
		fareForm.setOrdersid(order.getSid());
		fareForm.setFareformstatus(FareForm.STATUS_CREATED);
		fareForm.setEditable(FareForm.STATUS_EDITABLE);
		fareForm.setCorporationsid(order.getCorporationsid());
		// fareForm.setTrucknumber(truck.getTrucknumber());
		// fareForm.setDrivername(driver.getName());
		// fareForm.setEscortname(escort.getName());
		// fareForm.setBuyername(buyer.getName());
		fareForm.setTrucksid(truck.getSid());
		fareForm.setDriversid(driver.getSid());
		fareForm.setEscortsid(escort.getSid());
		fareForm.setBuyersid(buyer.getSid());
		fareForm.setLoadaddr(order.getLoadaddr());
		fareForm.setUnloadaddr(order.getUnloadaddr());
		fareForm.setPrice(order.getPrice());
		fareForm.setFreight(Double.parseDouble("0"));
		fareForm.createtime(operatesid);
		Long fareFormsid = null;
		try {
			fareFormsid = fareformDaoImpl.insert(fareForm, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// 更新订单
		String oldorder = Utils.getJsonObject(order).toString();
		order.setDistributetime(Utils.getCurrenttimeMills());
		// order.setTrucklogsid(trucklogsid);
		order.setFareformsid(fareFormsid);
		order.setOrderstatus(Order.STATUS_DISTRIBUTED);
		order.updatetime(operatesid);
		try {
			orderDaoImpl.update(order, oldorder, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		// 推送给司机
		JSONObject pushObject = new JSONObject();
		pushObject.put("type", "01");
		pushObject.put("sid", order.getSid());
		String pushcontent = pushObject.toString();
		if (!PushUtils.Push2Driver(order.getDriversid(), pushcontent, session, order.getCorporationsid()))
			System.err.println("订单派发推送给司机失败");
		// 推送给硬件
		if (!PushUtils.Push2Truck(order.getTrucksid(), session, pushcontent))
			System.err.println("订单派发推送给硬件失败");
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "订单派发成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}
}

// protected FullHttpResponse OnPrice(ChannelHandlerContext ctx, String[]
// path, Session session, HttpMethod method,
// Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
// throws JSONException {
// System.out.println("in OrderHandler-ToVerify");
// if (method != HttpMethod.POST) {
// return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
// }
// if (operateusertype == LoginInfo.TYPE_ADMIN) {
// role = BasicInfo.ROLE_ADMIN;
// corporationsid = loginInfo.getCorporationsid();
// } else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
// role = BasicInfo.ROLE_SUPER;
// } else {
// return NettyUtils.getTokenError(iskeepAlive);
// }
// String sidstr = params.get("sid");
// String pricestr = params.get("price");
// if (pricestr == null) {
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "请填写单价");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// if (sidstr == null) {
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "请上传sid");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// Long sid = null;
// Double price = null;
// try {
// sid = Long.parseLong(sidstr);
// } catch (Exception e) {
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "sid格式不正确");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// try {
// price = Double.parseDouble(pricestr);
// } catch (Exception e) {
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "单价格式不正确");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
//
// Order order = null;
// try {
// order = session.createQuery("from Order where sid = " + sid,
// Order.class).uniqueResult();
// } catch (Exception e) {
// Transaction tx = session.beginTransaction();
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "订单信息数据库出错");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// if (order == null) {
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "没有此订单信息");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// if (role == BasicInfo.ROLE_SUPER) {
// corporationsid = order.getCorporationsid();
// } else if (role == BasicInfo.ROLE_USER) {
// if (order.getCorporationsid() != corporationsid) {
// return NettyUtils.getTokenError(iskeepAlive);
// }
// } else {
// return NettyUtils.getTokenError(iskeepAlive);
// }
// if (order.getOrderstatus() != Order.STATUS_RETURNED) {
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "订单当前不能填写单价");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// FareForm fareForm = null;
// try {
// fareForm = session.createQuery("from FareForm where sid =
// "+order.getFareFormsid(), FareForm.class).uniqueResult();
// } catch (Exception e) {
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "数据库信息有误，请联系管理员");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// if(fareForm == null){
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "数据库信息有误，请联系管理员");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
//
// Transaction tx = session.beginTransaction();
// String oldorder = Utils.getJsonObject(order).toString();
// order.setOrderstatus(Order.STATUS_PRICED);
// order.setPricetime(Utils.getCurrenttimeMills());
// Utils.updatetime(operatesid, order);
// MySession.OnUpate(oldorder, order, session, operatesid);
// String oldfareform = Utils.getJsonObject(fareForm).toString();
// fareForm.setPrice(price);
// Utils.updatetime(operatesid, fareForm);
// MySession.OnUpate(oldfareform, fareForm, session, operatesid);
// tx.commit();
// //推送给管理员
// JSONObject retObject = new JSONObject();
// retObject.put("status", 0);
// retObject.put("msg", "订单更新成功");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }

// protected FullHttpResponse ToDistribute(ChannelHandlerContext ctx, String[]
// path, Session session,
// HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo
// loginInfo)
// throws JSONException {
// System.out.println("in OrderHandler-ToDistribute");
// if (method != HttpMethod.POST) {
// return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
// }
// // 角色解析
// if (operateusertype == LoginInfo.TYPE_ADMIN) {
// role = BasicInfo.ROLE_ADMIN;
// corporationsid = loginInfo.getCorporationsid();
// } else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
// role = BasicInfo.ROLE_SUPER;
// } else {
// return NettyUtils.getTokenError(iskeepAlive);
// }
// // 查询要派发的订单
// String sidstr = params.get("sid");
// if (sidstr == null) {
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "请上传sid");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// Long sid = null;
// try {
// sid = Long.parseLong(sidstr);
// } catch (Exception e) {
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "sid格式不正确");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
//
// Order order = null;
// try {
// order = session.createQuery("from Order where sid = " + sid,
// Order.class).uniqueResult();
// } catch (Exception e) {
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "订单信息数据库出错");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// if (order == null) {
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "没有此订单信息");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// if (role == BasicInfo.ROLE_SUPER) {
// corporationsid = order.getCorporationsid();
// } else if (role == BasicInfo.ROLE_ADMIN) {
// if (order.getCorporationsid() != corporationsid) {
// return NettyUtils.getTokenError(iskeepAlive);
// }
// } else {
// return NettyUtils.getTokenError(iskeepAlive);
// }
// if (order.getOrderstatus() != Order.STATUS_CHECKED) {
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "订单当前状态不能派发");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
//
// Transaction tx = session.beginTransaction();
// // 创建行车日志数据表
// Long trucksid = order.getTrucksid();
// Long ordersid = order.getSid();
// TruckLogs truckLogs = new TruckLogs();
// String tablename = "trucklog_" + trucksid + "_" + ordersid;
// truckLogs.setTrucksid(trucksid);
// truckLogs.setTrucknumber(order.getTrucknumber());
// truckLogs.setCorporationsid_fm_5(corporationsid);
// truckLogs.setOrdersid(order.getSid());
// truckLogs.setTablename_nm_30(tablename);
// truckLogs.setStatus(TruckLogs.STATUS_USING);
// Utils.createtime(operatesid, truckLogs);
// Long trucklogsid = MySession.OnSave(truckLogs, session, operatesid);
// String createtable = "CREATE TABLE `" + tablename + "` (" + "`sid` bigint(10)
// NOT NULL AUTO_INCREMENT,"
// + "`distance` Double(10,3) DEFAULT NULL," + "`time` bigint(13) DEFAULT NULL,"
// + "`gpsx` Double(13,9) DEFAULT NULL," + "`gpsy` Double(13,9) DEFAULT NULL,"
// + "`speed` Double(10,3) DEFAULT NULL," + "`fuelvol` Double(10,3) DEFAULT
// NULL,"
// + "`lefttirepressure` Double(10,3) DEFAULT NULL," + "`righttirepressure`
// Double(10,3) DEFAULT NULL,"
// + "`lefttiretemp` Double(10,3) DEFAULT NULL," + "`righttiretemp` Double(10,3)
// DEFAULT NULL,"
// + "`posture` varchar(200) DEFAULT NULL," + "`lock` varchar(10) DEFAULT NULL,"
// + "`haswarn` int(4) DEFAULT NULL," + "`warnsid` bigint(10) DEFAULT NULL,"
// + "`createdat` bigint(13) DEFAULT NULL," + "`createdid` bigint(10) DEFAULT
// NULL,"
// + "`updatedat` bigint(13) DEFAULT NULL," + "`updatedid` bigint(10) DEFAULT
// NULL,"
// + "PRIMARY KEY (`sid`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
// try {
// int ret = session.createNativeQuery(createtable).executeUpdate();
// if (ret != 1)
// throw new Exception();
// } catch (Exception e) {
// tx.rollback();
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", "创建行车日志数据表出错");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// // 创建费用表单
// FareForm fareForm = new FareForm();
// fareForm.setOrdersid_nm_10(ordersid);
// fareForm.setFareformstatus(FareForm.STATUS_CREATED);
// fareForm.setEditable(FareForm.STATUS_EDITABLE);
// fareForm.setCorporationsid_fm_5(order.getCorporationsid());
// fareForm.setTrucknumber(order.getTrucknumber());
// fareForm.setDrivername(order.getDrivername());
// fareForm.setEscortname(order.getEscortname());
// fareForm.setBuyername(order.getBuyername());
// fareForm.setLoadaddr(order.getLoadaddr());
// fareForm.setUnloadaddr(order.getUnloadaddr());
// fareForm.setPrice(order.getPrice());
// fareForm.setFreight(Double.parseDouble("0"));
// Utils.createtime(operatesid, fareForm);
// Long fareFormsid = MySession.OnSave(fareForm, session, operatesid);
// // 更新订单
// String oldorder = Utils.getJsonObject(order).toString();
// order.setDistributetime(Utils.getCurrenttimeMills());
// order.setTrucklogsid(trucklogsid);
// order.setFareFormsid(fareFormsid);
// order.setOrderstatus(Order.STATUS_DISTRIBUTED);
// Utils.updatetime(operatesid, order);
// try {
// Utils.ValidationWithExp(order);
// } catch (Exception e) {
// tx.rollback();
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", e.getMessage());
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// MySession.OnUpdate(oldorder, order, session, operatesid);
// // 推送给司机
// JSONObject pushObject = new JSONObject();
// pushObject.put("type", "01");
// pushObject.put("sid", order.getSid());
// String pushcontent = pushObject.toString();
// PushUtils.Push2Driver(order.getDriversid(), pushcontent, session,
// order.getCorporationsid());
// tx.commit();
// JSONObject retObject = new JSONObject();
// retObject.put("status", 0);
// retObject.put("msg", "订单派发成功");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
