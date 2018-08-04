package com.Entitys.Fareform.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.Entitys.Driver.Dao.DriverDaoImpl;
import com.Entitys.Escort.Dao.EscortDaoImpl;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Daos.MulTabBaseDaoImpl;
import com.Common.Daos.QueryBean;
import com.Common.Entitys.BasicInfo;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
import com.Entitys.Buyer.Entity.Buyer;
import com.Entitys.Driver.Entity.Driver;
import com.Entitys.Escort.Entity.Escort;
import com.Entitys.Fareform.Dao.FareformDaoImpl;
import com.Entitys.Fareform.Entity.FareForm;
import com.Entitys.Order.Dao.OrderDaoImpl;
import com.Entitys.Order.Entity.Order;
import com.Entitys.Truck.Entity.Truck;
import com.Entitys.Trucklog.Dao.TrucklogDaoImpl;
import com.Entitys.Trucklog.Entity.TruckLog;
import com.Entitys.User.Entity.LoginInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class FareFormHandler {
	private Long operatesid, corporationsid;
	private Integer operateusertype, role;
	private FareformDaoImpl fareformDaoImpl;
	private OrderDaoImpl orderDaoImpl;

	public FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (loginInfo == null)
			return NettyUtils.getTokenError(iskeepAlive);
		operateusertype = loginInfo.getType();
		operatesid = loginInfo.getUsersid();
		fareformDaoImpl = new FareformDaoImpl(session);
		orderDaoImpl = new OrderDaoImpl(session);

		if (path[1].equals("update")) {// 司机完善信息
			return OnDriverUpdate(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("complete")) {// 司机提交信息
			return OnDriverComplete(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("query")) {// admin、corporation、government、superadmin、driver查询历史费用清单
			return OnQueryFareForm(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("query_driver")) {// driver查询活跃的费用清单
			return OnDriverQueryFareForm(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("add_fuel")) {// 添加加油记录
			return OnAddFuel(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("add_realloadweight")) {// 添加重车实际过磅重量
			return OnAddReadloadweight(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("add_realunloadweight")) {// 添加空车实际过磅重量
			return OnAddReadunloadweight(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("getVerifypppdata")) {// 获取一审数据
			return OnGetVerifyPPPData(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("getVerifyppdata")) {// 获取二审数据
			return OnGetVerifyPPData(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("getVerifypdata")) {// 获取三审数据
			return OnGetVerifyPData(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("getVerifydata")) {//获取四审数据
			return OnGetVerifyData(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}

	protected FullHttpResponse OnDriverUpdate(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in FareFormHandler-OnDriverUpdate");
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
		FareForm fareForm = null;
		Order order = null;
		try {
			JSONArray jsonArray = new JSONArray();
			jsonArray.put(String.valueOf(Order.STATUS_DISTRIBUTED));
			jsonArray.put(String.valueOf(Order.STATUS_RECEIVED));
			jsonArray.put(String.valueOf(Order.STATUS_LOADED));
			jsonArray.put(String.valueOf(Order.STATUS_UNLOADED));
			jsonArray.put(String.valueOf(Order.STATUS_RETURNED));
			jsonArray.put(String.valueOf(Order.STATUS_VERIFYEDPPP));
			jsonArray.put(String.valueOf(Order.STATUS_VERIFYEDPP));
			jsonArray.put(String.valueOf(Order.STATUS_VERIFYEDP));
			jsonArray.put(String.valueOf(Order.STATUS_REVERIFY));
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "driversid", loginInfo.getUsersid(), QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "orderstatus", jsonArray.toString(), QueryBean.TYPE_JSONARRAY));
			List<Order> orders = orderDaoImpl.getListBy(queryList, true);
			if(orders.size() > 1){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库错误，一个司机有多个活跃的订单");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if(orders.size() == 0){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			order = orders.get(0);
			if(order == null){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if (fareForm.getEditable() != FareForm.STATUS_EDITABLE) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "费用清单当前不能修改");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Transaction tx = session.beginTransaction();
		String oldfareForm = Utils.getJsonObject(fareForm).toString();
		fareForm.UpdateFromMap(params);
		fareForm.updatetime(operatesid);
		try {
			fareformDaoImpl.update(fareForm, oldfareForm, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "保存信息成功");
		retObject.put("content", Utils.getJsonObject(fareForm));
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnDriverComplete(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in FareFormHandler-OnDriverComplete");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {
			role = BasicInfo.ROLE_CORPORATION;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
			role = BasicInfo.ROLE_SUPER;
			corporationsid = null;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		FareForm fareForm = null;
		Order order = null;
		try {
			JSONObject orderstatusObject = new JSONObject();
			orderstatusObject.put("min", String.valueOf(Order.STATUS_UNLOADED));
			orderstatusObject.put("max", String.valueOf(Order.STATUS_VERIFYEDP));
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "driversid", loginInfo.getUsersid(), QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "orderstatus", orderstatusObject.toString(), QueryBean.TYPE_JSONOBJECT));
			List<Order> orders = orderDaoImpl.getListBy(queryList, true);
			if(orders.size() > 1){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库错误，一个司机有多个活跃的订单");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if(orders.size() == 0){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			order = orders.get(0);
			if(order == null){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if(fareForm == null){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，对应的费用清单不存在");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (fareForm.getEditable() == FareForm.STATUS_UNEDITABLE) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "费用清单现在不能编辑");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if (fareForm.getFareformstatus() == FareForm.STATUS_COMPLETED) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "费用清单已经提交，不用再次提交");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Transaction tx = session.beginTransaction();
		String oldfareForm = Utils.getJsonObject(fareForm).toString();
		fareForm.setFareformstatus(FareForm.STATUS_COMPLETED);
		fareForm.setEditable(FareForm.STATUS_UNEDITABLE);
		Double mile = fareForm.getMiletotal();
		if (mile <= 150)
			fareForm.setFareaddwater(Double.parseDouble("10"));
		else if (mile <= 450)
			fareForm.setFareaddwater(Double.parseDouble("20"));
		else
			fareForm.setFareaddwater(Double.parseDouble("30"));
		fareForm.updatetime(operatesid);
		try {
			fareformDaoImpl.update(fareForm, oldfareForm, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "保存信息成功");
		retObject.put("content", Utils.getJsonObject(fareForm));
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnAddFuel(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in FareFormHandler-OnAddFuel");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {
			role = BasicInfo.ROLE_CORPORATION;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
			role = BasicInfo.ROLE_SUPER;
			corporationsid = null;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获得输入，并检查
		String addfuelvolstr = params.get("addfuelvol");
		String addfuelmoneystr = params.get("addfuelmoney");
		String addfuelcashstr = params.get("addfuelcash");
		if (addfuelvolstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请填写加油升数");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (addfuelmoneystr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请填写加油卡加油金额");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (addfuelcashstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请填写现金加油金额");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Double addfuelvol = null, addfuelmoney = null, addfuelcash = null;
		try {
			addfuelvol = Double.parseDouble(addfuelvolstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "加油升数格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			addfuelmoney = Double.parseDouble(addfuelmoneystr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "加油卡加油金额格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			addfuelcash = Double.parseDouble(addfuelcashstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "现金加油金额格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// 查找相应费用单
		FareForm fareForm = null;
		Order order = null;
		try {
			JSONObject orderstatusObject = new JSONObject();
			orderstatusObject.put("min", String.valueOf(Order.STATUS_DISTRIBUTED));
			orderstatusObject.put("max", String.valueOf(Order.STATUS_VERIFYEDP));
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "driversid", loginInfo.getUsersid(), QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "orderstatus", orderstatusObject.toString(), QueryBean.TYPE_JSONOBJECT));
			List<Order> orders = orderDaoImpl.getListBy(queryList, true);
			if(orders.size() > 1){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库错误，一个司机有多个活跃的订单");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if(orders.size() == 0){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			order = orders.get(0);
			if(order == null){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if(fareForm == null){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，对应的费用清单不存在");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (fareForm.getEditable() == FareForm.STATUS_UNEDITABLE) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "费用清单现在不能编辑");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Transaction tx = session.beginTransaction();
		String oldfareForm = Utils.getJsonObject(fareForm).toString();
		if (!fareForm.addfuel(addfuelvol, addfuelmoney, addfuelcash)) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "修改清单信息失败");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		fareForm.updatetime(operatesid);
		try {
			fareformDaoImpl.update(fareForm, oldfareForm, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "保存信息成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnAddReadloadweight(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in FareFormHandler-OnAddReadloadweight");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {
			role = BasicInfo.ROLE_CORPORATION;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
			role = BasicInfo.ROLE_SUPER;
			corporationsid = null;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获得输入，并检查
		String realloadweightstr = params.get("realloadweight");
		if (realloadweightstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请填写重车实际过磅重量");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Double realloadweight = null;
		try {
			realloadweight = Double.parseDouble(realloadweightstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "重车实际过磅重量格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// 查找相应费用单
		FareForm fareForm = null;
		Order order = null;
		try {
			JSONObject orderstatusObject = new JSONObject();
			orderstatusObject.put("min", String.valueOf(Order.STATUS_DISTRIBUTED));
			orderstatusObject.put("max", String.valueOf(Order.STATUS_VERIFYEDP));
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "driversid", loginInfo.getUsersid(), QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "orderstatus", orderstatusObject.toString(), QueryBean.TYPE_JSONOBJECT));
			List<Order> orders = orderDaoImpl.getListBy(queryList, true);
			if(orders.size() > 1){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库错误，一个司机有多个活跃的订单");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if(orders.size() == 0){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			order = orders.get(0);
			if(order == null){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if(fareForm == null){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，对应的费用清单不存在");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (fareForm.getEditable() == FareForm.STATUS_UNEDITABLE) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "费用清单现在不能编辑");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Transaction tx = session.beginTransaction();
		String oldfareForm = Utils.getJsonObject(fareForm).toString();
		if (!fareForm.addrealloadweight(realloadweight)) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "修改清单信息失败");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		fareForm.updatetime(operatesid);
		try {
			fareformDaoImpl.update(fareForm, oldfareForm, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "保存信息成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnAddReadunloadweight(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in FareFormHandler-OnAddReadunloadweight");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {
			role = BasicInfo.ROLE_CORPORATION;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
			role = BasicInfo.ROLE_SUPER;
			corporationsid = null;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获得输入，并检查
		String realunloadweightstr = params.get("realunloadweight");
		if (realunloadweightstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请填写空车实际过磅重量");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Double realunloadweight = null;
		try {
			realunloadweight = Double.parseDouble(realunloadweightstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "空车实际过磅重量格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// 查找相应费用单
		FareForm fareForm = null;
		Order order = null;
		try {
			JSONObject orderstatusObject = new JSONObject();
			orderstatusObject.put("min", String.valueOf(Order.STATUS_DISTRIBUTED));
			orderstatusObject.put("max", String.valueOf(Order.STATUS_VERIFYEDP));
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "driversid", loginInfo.getUsersid(), QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "orderstatus", orderstatusObject.toString(), QueryBean.TYPE_JSONOBJECT));
			List<Order> orders = orderDaoImpl.getListBy(queryList, true);
			if(orders.size() > 1){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库错误，一个司机有多个活跃的订单");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if(orders.size() == 0){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			order = orders.get(0);
			if(order == null){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的订单信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if(fareForm == null){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "订单数据库出错，对应的费用清单不存在");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (fareForm.getEditable() == FareForm.STATUS_UNEDITABLE) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "费用清单现在不能编辑");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}

		Transaction tx = session.beginTransaction();
		String oldfareForm = Utils.getJsonObject(fareForm).toString();
		if (!fareForm.addrealunloadweight(realunloadweight)) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "修改清单信息失败");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		fareForm.updatetime(operatesid);
		try {
			fareformDaoImpl.update(fareForm, oldfareForm, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tx.commit();
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "保存信息成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnQueryFareForm(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in FareFormHandler-OnQueryFareForm");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {
			role = BasicInfo.ROLE_CORPORATION;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {
			role = BasicInfo.ROLE_GOVERNMENT;
			corporationsid = null;
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {
			role = BasicInfo.ROLE_SUPER;
			corporationsid = null;
		} else if (operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		
		Map<String, String> queryMap = Utils.paramsqueryfilter(params, role, corporationsid, operatesid);
		if (role == BasicInfo.ROLE_USER) {
			queryMap.put("driversid", String.valueOf(operatesid));
		}
		Iterator<Entry<String, String>> iterator = queryMap.entrySet().iterator();
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		while(iterator.hasNext()){
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
				} catch (Exception e1) {}
			}
			queryList.add(new QueryBean(FareForm.class.getName(), "fareform", field, value, valuetype));
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
		}else{
			intlimit = null;
			intpage = null;
		}
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Long total = null;
		try {
			fareformDaoImpl.fetchListBy(queryList, result, true, intlimit, intpage, Truck.class, Driver.class, Escort.class, Buyer.class);
			total = fareformDaoImpl.selectCount(queryList, true);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询费用清单信息成功");
		JSONObject contentJsonObject = new JSONObject();
		if (limit != null) {
			if (intlimit * (intpage-1) >= total) {
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("status", 4);
				jsonObject2.put("msg", "第" + intpage + "页不存在");
				jsonObject2.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
			}
			contentJsonObject.put("total", total);
			contentJsonObject.put("perpage", limit);
			contentJsonObject.put("current_page", intpage);
			contentJsonObject.put("from", (intpage-1) * intlimit + 1);
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
			FareForm fareForm = (FareForm) tmpret.get("FareForm");
			Truck truck = (Truck) tmpret.get("Truck");
			Driver driver = (Driver) tmpret.get("Driver");
			Escort escort = (Escort) tmpret.get("Escort");
			Buyer buyer = (Buyer) tmpret.get("Buyer");
			Order order = null;
			try {
				if(fareForm != null)
					order = orderDaoImpl.getById(fareForm.getOrdersid(), true);
			} catch (Exception e) {}
			JSONObject tmpjsonObject = Utils.getJsonObject(fareForm);
			tmpjsonObject.put("orderstatus", order == null ? "null" : order.getOrderstatus());
			tmpjsonObject.put("trucknumber", truck == null ? "null" : truck.getTrucknumber());
			tmpjsonObject.put("drivername", driver == null ? "null" : driver.getName());
			tmpjsonObject.put("escortname", escort == null ? "null" : escort.getName());
			tmpjsonObject.put("buyername", buyer == null ? "null" : buyer.getName());
			dataArray.put(tmpjsonObject);
		}
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnDriverQueryFareForm(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in FareFormHandler-OnQueryFareForm");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		JSONArray orderstatusArray = new JSONArray();
		orderstatusArray.put(String.valueOf(Order.STATUS_DISTRIBUTED));
		orderstatusArray.put(String.valueOf(Order.STATUS_RECEIVED));
		orderstatusArray.put(String.valueOf(Order.STATUS_LOADED));
		orderstatusArray.put(String.valueOf(Order.STATUS_UNLOADED));
		orderstatusArray.put(String.valueOf(Order.STATUS_REVERIFY));
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		queryList.add(new QueryBean(Order.class.getName(), "orderp", "orderstatus", orderstatusArray.toString(), QueryBean.TYPE_JSONARRAY));
		if (operateusertype == LoginInfo.TYPE_SELLER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "sellersid", loginInfo.getUsersid(), QueryBean.TYPE_EQUAL));
		} else if (operateusertype == LoginInfo.TYPE_BUYER) {
			role = BasicInfo.ROLE_USER;
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "buyersid", loginInfo.getUsersid(), QueryBean.TYPE_EQUAL));
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
			queryList.add(new QueryBean(Order.class.getName(), "orderp", "driversid", loginInfo.getUsersid(), QueryBean.TYPE_EQUAL));
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Long total = null;
		String limit = params.get("limit");
		Integer intpage = 0;
		Integer intlimit = 0;
		if (limit != null) {
			intlimit = Integer.valueOf(limit);
			String page = params.get("page");
			if (page == null)
				page = "1";
			intpage = Integer.valueOf(page);
		}else{
			intlimit = null;
			intpage = null;
		}
		try {
			orderDaoImpl.fetchListBy(queryList, result, true, intlimit, intpage, Truck.class, Driver.class, Escort.class, Buyer.class);
			total = orderDaoImpl.selectCount(queryList, true);
			if(operateusertype == LoginInfo.TYPE_DRIVER && result.size() > 1){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库错误，一个司机对应多个活跃的订单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		JSONArray dataArray = new JSONArray();
		for (int i = 0; i < result.size(); i++) {
			Map<String, Object> tmpret = result.get(i);
			Order order = (Order) tmpret.get("Order");
			if(order == null)
				continue;
			FareForm fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			Truck truck = (Truck) tmpret.get("Truck");
			Driver driver = (Driver) tmpret.get("Driver");
			Escort escort = (Escort) tmpret.get("Escort");
			Buyer buyer = (Buyer) tmpret.get("Buyer");
			JSONObject tmpjsonObject = Utils.getJsonObject(fareForm);
			tmpjsonObject.put("orderstatus", order.getOrderstatus());
			tmpjsonObject.put("trucknumber", truck.getTrucknumber());
			tmpjsonObject.put("drivername", driver.getName());
			tmpjsonObject.put("escortname", escort.getName());
			tmpjsonObject.put("buyername", buyer.getName());
			dataArray.put(tmpjsonObject);
		}
		JSONObject jsonObject = new JSONObject();
		JSONObject contentJsonObject = new JSONObject();
		if (limit != null) {
			if (intlimit * (intpage-1) >= total) {
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("status", 4);
				jsonObject2.put("msg", "第" + intpage + "页不存在");
				jsonObject2.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
			}
			contentJsonObject.put("total", total);
			contentJsonObject.put("perpage", limit);
			contentJsonObject.put("current_page", intpage);
			contentJsonObject.put("from", (intpage-1) * intlimit + 1);
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
		contentJsonObject.put("data", dataArray);
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询费用清单成功");
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnGetVerifyPPPData(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in FareFormHandler-OnQueryFareForm");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		String sidstr = params.get("ordersid");
		if (sidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请输入订单流水号");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "订单流水号格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Order order = null;
		FareForm fareForm = null;
		Driver driver = null;
		Truck truck = null;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			orderDaoImpl.fetchById(sid, result, true, Driver.class, Truck.class);
			if(result.size() == 0){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "没有对应的订单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			order = (Order) result.get("Order");
			driver = (Driver) result.get("Driver");
			truck = (Truck) result.get("Truck");
			if (order == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "没有对应的订单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if (order.getOrderstatus() != Order.STATUS_RETURNED
					&& order.getOrderstatus() != Order.STATUS_REVERIFY) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "订单当前状态不可审核");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if(fareForm == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，订单没有对应的费用清单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(driver == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，订单没有对应的驾驶员信息");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(truck == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，订单没有对应的拖车信息");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		List<TruckLog> newresult = new ArrayList<TruckLog>();
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		queryList.add(new QueryBean(TruckLog.class.getName(), "lastlog", "ordersid", order.getSid(), QueryBean.TYPE_EQUAL));
		try {
			TrucklogDaoImpl trucklogDaoImpl = new TrucklogDaoImpl(session);
			newresult = trucklogDaoImpl.getByCorporation(corporationsid, queryList, 1, 1, MulTabBaseDaoImpl.QUERY_ORDER_DESC, "createdat");
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if(newresult.size() > 1){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库错误，查询到一个订单对应多个最新行车日志");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
//		if (newresult.size() == 0) {
//			JSONObject retObject = new JSONObject();
//			retObject.put("status", 4);
//			retObject.put("msg", "没有行车日志记录");
//			retObject.put("content", "");
//			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
//		}
		double milereal = 0D;
		if(newresult.size() == 1){
			milereal = newresult.get(0).getDistance();
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询费用清单成功");
		JSONObject retObject = new JSONObject();
		retObject.put("mileload_driver", fareForm.getMileload());
		retObject.put("mileunload_driver", fareForm.getMileunload());
		retObject.put("miletotal_driver", fareForm.getMiletotal());
		retObject.put("milereal", milereal);
		retObject.put("drivername", driver.getName());
		retObject.put("trucknumber", truck.getTrucknumber());
		jsonObject.put("content", retObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnGetVerifyPPData(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in FareFormHandler-OnQueryFareForm");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		String sidstr = params.get("ordersid");
		if (sidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请输入订单流水号");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "订单流水号格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Order order = null;
		FareForm fareForm = null;
		Driver driver = null;
		Escort escort = null;
		try {
			order = orderDaoImpl.getById(sid, true);
			if (order == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "没有对应的订单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if (order.getOrderstatus() != Order.STATUS_VERIFYEDPPP) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "订单当前状态不可审核");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if(fareForm == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，订单没有对应的费用清单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			DriverDaoImpl driverDaoImp = new DriverDaoImpl(session);
			driver = driverDaoImp.getById(order.getDriversid(), true);
			if(driver == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，订单没有对应的驾驶员");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			EscortDaoImpl escortDaoImpl = new EscortDaoImpl(session);
			escort = escortDaoImpl.getById(order.getEscortsid(), true);
			if(escort == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，订单没有对应的押运员");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询费用清单成功");
		JSONObject retObject = Utils.getJsonObject(fareForm);
		retObject.put("orderstatus", order.getOrderstatus());
		retObject.put("drivername", driver.getName());
		retObject.put("escortname", escort.getName());
		jsonObject.put("content", retObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnGetVerifyPData(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in FareFormHandler-OnGetVerifyPData");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		String sidstr = params.get("ordersid");
		if (sidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请输入订单流水号");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "订单流水号格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Order order = null;
		FareForm fareForm = null;
		Driver driver = null;
		Escort escort = null;
		try {
			order = orderDaoImpl.getById(sid, true);
			if (order == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "没有对应的订单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if (order.getOrderstatus() != Order.STATUS_VERIFYEDPP) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "订单当前状态不可审核");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if(fareForm == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，订单没有对应的费用清单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			DriverDaoImpl driverDaoImp = new DriverDaoImpl(session);
			driver = driverDaoImp.getById(order.getDriversid(), true);
			if(driver == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，订单没有对应的驾驶员");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			EscortDaoImpl escortDaoImpl = new EscortDaoImpl(session);
			escort = escortDaoImpl.getById(order.getEscortsid(), true);
			if(escort == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，订单没有对应的押运员");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询费用清单成功");
		JSONObject retObject = Utils.getJsonObject(fareForm);
		retObject.put("orderstatus", order.getOrderstatus());
		retObject.put("drivername", driver.getName());
		retObject.put("escortname", escort.getName());
		jsonObject.put("content", retObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnGetVerifyData(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in FareFormHandler-OnGetVerifyData");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		String sidstr = params.get("ordersid");
		if (sidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请输入订单流水号");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "订单流水号格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Order order = null;
		FareForm fareForm = null;
		Driver driver = null;
		Escort escort = null;
		try {
			order = orderDaoImpl.getById(sid, true);
			if (order == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "没有对应的订单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if (order.getOrderstatus() != Order.STATUS_VERIFYEDP) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "订单当前状态不可审核");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			fareForm = fareformDaoImpl.getById(order.getFareformsid(), true);
			if(fareForm == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，订单没有对应的费用清单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			DriverDaoImpl driverDaoImp = new DriverDaoImpl(session);
			driver = driverDaoImp.getById(order.getDriversid(), true);
			if(driver == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，订单没有对应的驾驶员");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			EscortDaoImpl escortDaoImpl = new EscortDaoImpl(session);
			escort = escortDaoImpl.getById(order.getEscortsid(), true);
			if(escort == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，订单没有对应的押运员");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询费用清单成功");
		JSONObject retObject = Utils.getJsonObject(fareForm);
		retObject.put("orderstatus", order.getOrderstatus());
		retObject.put("drivername", driver.getName());
		retObject.put("escortname", escort.getName());
		jsonObject.put("content", retObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}
}

// protected FullHttpResponse OnAddImage(ChannelHandlerContext ctx, String[]
// path, Session session,
// HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo
// loginInfo)
// throws JSONException {
// System.out.println("in FareFormHandler-OnAddImage");
// if (method != HttpMethod.POST) {
// return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
// }
// // 角色解析
// if (operateusertype == LoginInfo.TYPE_DRIVER) {
// role = BasicInfo.ROLE_USER;
// corporationsid = loginInfo.getCorporationsid();
// } else {
// return NettyUtils.getTokenError(iskeepAlive);
// }
// //输入
// String sidsstr = params.get("imagesids");
// String[] sids = sidsstr.split(":");
// List<Long> finalsids = new ArrayList<Long>();
// List<Integer> inindexs = new ArrayList<Integer>();
// List<Integer> errorindexs = new ArrayList<Integer>();
// if(sids != null){
// int index = 0;
// for(int i = 0; i < sids.length; i++){
// String sidstr = sids[i];
// Long tmpsid = null;
// try {
// tmpsid = Long.parseLong(sidstr);
// try {
// FileStore fileStore = session.createQuery("from FileStore where sid =
// "+tmpsid, FileStore.class).uniqueResult();
// if(fileStore != null){
// finalsids.add(index, tmpsid);
// inindexs.add(index++, i);
// }
// else
// errorindexs.add(i);
// } catch (Exception e) {
// errorindexs.add(i);
// }
// } catch (Exception e) {
// errorindexs.add(i);
// }
// }
// }else {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "请上传图片");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// if(finalsids.size() == 0){
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "上传图片全部无效");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// //查询费用清单
// Order order = null;
// try {
// order = session
// .createQuery(
// "from Order where driversid = " + loginInfo.getUsersid() + " and
// orderstatus >= "
// + Order.STATUS_DISTRIBUTED + " and orderstatus < " + Order.STATUS_RETURNED,
// Order.class)
// .uniqueResult();
// } catch (Exception e) {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "数据库错误，请联系系统管理员:"+e.getMessage());
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// if (order == null) {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "没有需要修改的费用清单");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// FareForm fareForm = null;
// try {
// fareForm = session.createQuery("from FareForm where sid = " +
// order.getFareFormsid(), FareForm.class)
// .uniqueResult();
// } catch (Exception e) {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "数据库错误，请联系系统管理员:"+e.getMessage());
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// if (fareForm == null) {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "数据库错误，请联系系统管理员:订单内费用单号有误");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// if(fareForm.getFareformstatus() == FareForm.STATUS_COMPLETED){
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "费用清单已经提交，不能再修改");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
//
// Transaction tx = session.beginTransaction();
// String oldfareForm = Utils.getJsonObject(fareForm).toString();
// for(int i = 0; i < finalsids.size(); i++)
// if(!fareForm.addimage(finalsids.get(i))){
// errorindexs.add(inindexs.get(i));
// }
// Utils.updatetime(operatesid, fareForm);
// try {
// Utils.ValidationWithExp(fareForm);
// } catch (Exception e) {
// tx.rollback();
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", e.getMessage());
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// MySession.OnUpate(oldfareForm, fareForm, session, operatesid);
// tx.commit();
// JSONObject retObject = new JSONObject();
// retObject.put("status", 0);
// StringBuilder sb = new StringBuilder();
// sb.append("添加图片成功");
// if(errorindexs.size() > 0)
// sb.append("，失败"+errorindexs.size()+"张：");
// for(int i = 0; i < errorindexs.size(); i++){
// if(i == 0)
// sb.append(errorindexs.get(i));
// else
// sb.append(","+errorindexs.get(i));
// }
// retObject.put("msg", sb.toString());
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
//
// protected FullHttpResponse OnDriverReupdate(ChannelHandlerContext ctx,
// String[] path, Session session,
// HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo
// loginInfo)
// throws JSONException {
// System.out.println("in FareFormHandler-OnDriverReupdate");
// if (method != HttpMethod.POST) {
// return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
// }
// // 角色解析
// if (operateusertype == LoginInfo.TYPE_DRIVER) {
// role = BasicInfo.ROLE_USER;
// corporationsid = loginInfo.getCorporationsid();
// } else {
// return NettyUtils.getTokenError(iskeepAlive);
// }
//
// Order order = null;
// try {
// order = session
// .createQuery(
// "from Order where driversid = " + loginInfo.getUsersid() + " and
// orderstatus = "
// + Order.STATUS_REVERIFY,
// Order.class)
// .uniqueResult();
// } catch (Exception e) {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "数据库错误，请联系系统管理员:"+e.getMessage());
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// if (order == null) {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "没有符合条件的费用清单");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// FareForm fareForm = null;
// try {
// fareForm = session.createQuery("from FareForm where sid = " +
// order.getFareFormsid(), FareForm.class)
// .uniqueResult();
// } catch (Exception e) {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "数据库错误，请联系系统管理员:"+e.getMessage());
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// if (fareForm == null) {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "数据库错误，请联系系统管理员:订单内费用单号有误");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// if(fareForm.getFareformstatus() != FareForm.STATUS_COMPLETED){
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "费用清单尚未提交，尚未审核");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
//
// Transaction tx = session.beginTransaction();
// String oldfareForm = Utils.getJsonObject(fareForm).toString();
// fareForm.ReupdateFromMap(params);
// Utils.updatetime(operatesid, fareForm);
// try {
// Utils.ValidationWithExp(fareForm);
// } catch (Exception e) {
// tx.rollback();
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", e.getMessage());
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// MySession.OnUpate(oldfareForm, fareForm, session, operatesid);
// tx.commit();
// JSONObject retObject = new JSONObject();
// retObject.put("status", 0);
// retObject.put("msg", "保存信息成功");
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
//
// protected FullHttpResponse OnReAddImage(ChannelHandlerContext ctx, String[]
// path, Session session,
// HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo
// loginInfo)
// throws JSONException {
// System.out.println("in FareFormHandler-OnReAddImage");
// if (method != HttpMethod.POST) {
// return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
// }
// // 角色解析
// if (operateusertype == LoginInfo.TYPE_DRIVER) {
// role = BasicInfo.ROLE_USER;
// corporationsid = loginInfo.getCorporationsid();
// } else {
// return NettyUtils.getTokenError(iskeepAlive);
// }
// //输入
// String sidsstr = params.get("imagesids");
// String[] sids = sidsstr.split(":");
// List<Long> finalsids = new ArrayList<Long>();
// List<Integer> inindexs = new ArrayList<Integer>();
// List<Integer> errorindexs = new ArrayList<Integer>();
// if(sids != null){
// int index = 0;
// for(int i = 0; i < sids.length; i++){
// String sidstr = sids[i];
// Long tmpsid = null;
// try {
// tmpsid = Long.parseLong(sidstr);
// try {
// FileStore fileStore = session.createQuery("from FileStore where sid =
// "+tmpsid, FileStore.class).uniqueResult();
// if(fileStore != null){
// finalsids.add(index, tmpsid);
// inindexs.add(index++, i);
// }
// else
// errorindexs.add(i);
// } catch (Exception e) {
// errorindexs.add(i);
// }
// } catch (Exception e) {
// errorindexs.add(i);
// }
// }
// }else {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "请上传图片");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// if(finalsids.size() == 0){
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "上传图片全部无效");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// //查询费用清单
// Order order = null;
// try {
// order = session
// .createQuery(
// "from Order where driversid = " + loginInfo.getUsersid() + " and
// orderstatus = "
// + Order.STATUS_REVERIFY,
// Order.class)
// .uniqueResult();
// } catch (Exception e) {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "数据库错误，请联系系统管理员:"+e.getMessage());
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// if (order == null) {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "没有需要修改的费用清单");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// FareForm fareForm = null;
// try {
// fareForm = session.createQuery("from FareForm where sid = " +
// order.getFareFormsid(), FareForm.class)
// .uniqueResult();
// } catch (Exception e) {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "数据库错误，请联系系统管理员:"+e.getMessage());
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// if (fareForm == null) {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "数据库错误，请联系系统管理员:订单内费用单号有误");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// if(fareForm.getFareformstatus() != FareForm.STATUS_COMPLETED){
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "费用清单尚未提交，尚未审核");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
//
// Transaction tx = session.beginTransaction();
// String oldfareForm = Utils.getJsonObject(fareForm).toString();
// for(int i = 0; i < finalsids.size(); i++)
// if(!fareForm.addimage(finalsids.get(i))){
// errorindexs.add(inindexs.get(i));
// }
// Utils.updatetime(operatesid, fareForm);
// try {
// Utils.ValidationWithExp(fareForm);
// } catch (Exception e) {
// tx.rollback();
// JSONObject retObject = new JSONObject();
// retObject.put("status", 4);
// retObject.put("msg", e.getMessage());
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
// MySession.OnUpate(oldfareForm, fareForm, session, operatesid);
// tx.commit();
// JSONObject retObject = new JSONObject();
// retObject.put("status", 0);
// StringBuilder sb = new StringBuilder();
// sb.append("添加图片成功");
// if(errorindexs.size() > 0)
// sb.append("，失败"+errorindexs.size()+"张：");
// for(int i = 0; i < errorindexs.size(); i++){
// if(i == 0)
// sb.append(errorindexs.get(i));
// else
// sb.append(","+errorindexs.get(i));
// }
// retObject.put("msg", sb.toString());
// retObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, retObject.toString());
// }
