package com.Entitys.Lock.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.Common.Entitys.Bean;
import com.Entitys.Corporation.Entity.Corporation;
import com.Entitys.Truck.Dao.TruckDaoImpl;
import com.RequestHandlers.TestHandler;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Daos.QueryBean;
import com.Common.Entitys.BasicInfo;
import com.Common.Utils.MySession;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.PushUtils;
import com.Common.Utils.Utils;
import com.Entitys.Driver.Entity.Driver;
import com.Entitys.Lock.Dao.LockinfoDaoImpl;
import com.Entitys.Lock.Entity.Lock;
import com.Entitys.Order.Dao.OrderDaoImpl;
import com.Entitys.Order.Entity.Order;
import com.Entitys.Truck.Entity.Truck;
import com.Entitys.User.Entity.LoginInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class LockHandler {
	private Long operatesid, corporationsid;
	@SuppressWarnings("unused")
	private Integer operateusertype, role;
	private OrderDaoImpl orderDaoImpl;
	private LockinfoDaoImpl lockinfoDaoImpl;

	public FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		this.orderDaoImpl = new OrderDaoImpl(session);
		this.lockinfoDaoImpl = new LockinfoDaoImpl(session);
		if (path[1].equals("request")) {// 司机
			if (loginInfo == null)
				return NettyUtils.getTokenError(iskeepAlive);
			operateusertype = loginInfo.getType();
			operatesid = loginInfo.getUsersid();
			return OnRequest(ctx, path, session, method, params, iskeepAlive, loginInfo);
		}
		else if (path[1].equals("request_car")) {// 司机在车上按键请求
			return OnRequestCar(ctx, path, session, method, params, iskeepAlive, loginInfo);
		}
		else if (path[1].equals("query")) {// 管理员
			if (loginInfo == null)
				return NettyUtils.getTokenError(iskeepAlive);
			operateusertype = loginInfo.getType();
			operatesid = loginInfo.getUsersid();
			return OnQuery(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("response")) {// 管理员
			if (loginInfo == null)
				return NettyUtils.getTokenError(iskeepAlive);
			operateusertype = loginInfo.getType();
			operatesid = loginInfo.getUsersid();
			return OnResponse(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("operate")) {// 硬件平台
			return OnOperateComplete(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}

	protected FullHttpResponse OnRequest(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in LockHandler-OnRequest");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		Order order = null;
		Truck truck = null;
		Driver driver = null;
		Corporation corporation = null;
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		List<QueryBean> queryBean = new ArrayList<QueryBean>();
		queryBean.add(new QueryBean(Order.class.getName(), "orderp", "driversid", loginInfo.getUsersid(), QueryBean.TYPE_EQUAL));
		JSONArray orderstatusArray = new JSONArray();
		orderstatusArray.put(Order.STATUS_DISTRIBUTED);
		orderstatusArray.put(Order.STATUS_RECEIVED);
		orderstatusArray.put(Order.STATUS_LOADED);
		orderstatusArray.put(Order.STATUS_UNLOADED);
		orderstatusArray.put(Order.STATUS_RETURNED);
		orderstatusArray.put(Order.STATUS_VERIFYEDPPP);
		orderstatusArray.put(Order.STATUS_VERIFYEDPP);
		orderstatusArray.put(Order.STATUS_VERIFYEDP);
		queryBean.add(new QueryBean(Order.class.getName(), "orderp", "orderstatus", orderstatusArray.toString(), QueryBean.TYPE_JSONARRAY));
		try {
			orderDaoImpl.fetchListBy(queryBean, result, true, Truck.class, Driver.class, Corporation.class);
			if(result.size() > 1)
				throw new Exception("数据库错误，一个司机对应多个活跃的订单");
			if(result.size() == 0){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "该司机没有活跃的订单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			Map<String, Object> tmpret = result.get(0);
			order = (Order) tmpret.get("Order");
			truck = (Truck) tmpret.get("Truck");
			driver = (Driver) tmpret.get("Driver");
			corporation = (Corporation)tmpret.get("Corporation");
			if(order == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "该司机没有活跃的订单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(truck == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库错误，订单对应的拖车信息不存在");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(driver == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库错误，订单对应的驾驶员信息不存在");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
            if(corporation == null){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "数据库错误，订单对应的承运方信息不存在");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库错误，请联系系统管理员:" + e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		String type = params.get("type");
		String requestdesc = params.get("requestdesc");
		if(type == null){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传请求类型");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Map<String, String> tmpmap = new HashMap<String, String>();
		tmpmap.put("requestfrom", String.valueOf(driver.getSid()));
		tmpmap.put("drivername", String.valueOf(driver.getName()));
		tmpmap.put("trucknumber", String.valueOf(truck.getTrucknumber()));
		tmpmap.put("request", type);
		tmpmap.put("requestdesc", requestdesc);
		tmpmap.put("status", String.valueOf(Lock.STATUS_REQUESTED));
		tmpmap.put("requestat", String.valueOf(Utils.getCurrenttimeMills()));
		tmpmap.put("corporationsid", String.valueOf(corporationsid));
        tmpmap.put("corporationname", corporation.getName());
		Transaction tx = session.beginTransaction();
		try{
			Lock lock = new Lock();
			Utils.UpdateFromMap(tmpmap, lock);
			long sid = lockinfoDaoImpl.insert(lock, operatesid);
			MySession.AfterSave(lock, session, operatesid);
			tx.commit();
			// 推送给管理员
			JSONObject pushObject = new JSONObject();
			pushObject.put("type", "10");
			pushObject.put("sid", sid);
			String pushcontent = pushObject.toString();
			if(!PushUtils.Push2Admin(pushcontent, session, order.getCorporationsid(), null, null))
				System.err.println("锁请求司机请求推送给管理员失败");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "开锁请求成功");
			JSONObject contentjb = new JSONObject();
			contentjb.put("sid", sid);
			jsonObject.put("content", contentjb);
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}catch(Exception e){
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 1);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
	}

	protected FullHttpResponse OnRequestCar(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
											Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in LockHandler-OnRequestCar");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}

		Truck truck;
		String trucknumber = params.get("trucknumber");
		try {
			TruckDaoImpl truckDaoImpl = new TruckDaoImpl(session);
			truck = truckDaoImpl.getByTrucknumber(trucknumber, true);
			if(truck == null || truck.getTruckstatus() == Truck.TRUCKSTATUS_IDLE ||
					truck.getTruckstatus() == Truck.TRUCKSTATUS_INVAILD){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此牵引车");
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
		Order order = null;
		Driver driver = null;
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		List<QueryBean> queryBean = new ArrayList<QueryBean>();
		queryBean.add(new QueryBean(Order.class.getName(), "orderp", "trucksid", truck.getSid(), QueryBean.TYPE_EQUAL));
		JSONArray orderstatusArray = new JSONArray();
		orderstatusArray.put(Order.STATUS_DISTRIBUTED);
		orderstatusArray.put(Order.STATUS_RECEIVED);
		orderstatusArray.put(Order.STATUS_LOADED);
		orderstatusArray.put(Order.STATUS_UNLOADED);
		orderstatusArray.put(Order.STATUS_RETURNED);
		orderstatusArray.put(Order.STATUS_VERIFYEDPPP);
		orderstatusArray.put(Order.STATUS_VERIFYEDPP);
		orderstatusArray.put(Order.STATUS_VERIFYEDP);
		queryBean.add(new QueryBean(Order.class.getName(), "orderp", "orderstatus", orderstatusArray.toString(), QueryBean.TYPE_JSONARRAY));
		try {
			orderDaoImpl.fetchListBy(queryBean, result, true, Driver.class);
			if(result.size() > 1)
				throw new Exception("数据库错误，一辆牵引车对应多个活跃的订单");
			if(result.size() == 0){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "该牵引车没有活跃的订单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			Map<String, Object> tmpret = result.get(0);
			order = (Order) tmpret.get("Order");
			driver = (Driver) tmpret.get("Driver");
			if(order == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "该牵引车没有活跃的订单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(driver == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库错误，订单对应的驾驶员信息不存在");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库错误，请联系系统管理员:" + e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		String type = params.get("type");
		String requestdesc = params.get("requestdesc");
		if(type == null){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传请求类型");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		corporationsid = truck.getCorporationsid();
		Map<String, String> tmpmap = new HashMap<String, String>();
		tmpmap.put("requestfrom", String.valueOf(driver.getSid()));
		tmpmap.put("drivername", String.valueOf(driver.getName()));
		tmpmap.put("trucknumber", String.valueOf(truck.getTrucknumber()));
		tmpmap.put("request", type);
		tmpmap.put("requestdesc", requestdesc);
		tmpmap.put("status", String.valueOf(Lock.STATUS_REQUESTED));
		tmpmap.put("requestat", String.valueOf(Utils.getCurrenttimeMills()));
		tmpmap.put("corporationsid", String.valueOf(corporationsid));
		Corporation corporation = null;
		try {
			corporation = session.createQuery("from Corporation where datastatus = " + Bean.CREATED + " and sid = "
					+ corporationsid, Corporation.class).uniqueResult();
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "承运方数据库信息错误，请联系系统管理员");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (corporation == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "承运方数据库信息错误，请联系系统管理员");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		tmpmap.put("corporationname", corporation.getName());
		Transaction tx = session.beginTransaction();
		Lock lock = new Lock();
		Utils.UpdateFromMap(tmpmap, lock);
		lock.createtime(operatesid);
		Long sid = null;
		try {
			sid = lockinfoDaoImpl.insert(lock, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		MySession.AfterSave(lock, session, operatesid);
		tx.commit();
		// 推送给管理员
		JSONObject pushObject = new JSONObject();
		pushObject.put("type", "10");
		pushObject.put("sid", String.valueOf(sid));
		String pushcontent = pushObject.toString();
		if(!PushUtils.Push2Admin(pushcontent, session, order.getCorporationsid(), null, null))
			System.err.println("锁请求司机请求推送给管理员失败");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "开锁请求成功");
		JSONObject contentjb = new JSONObject();
		contentjb.put("sid", String.valueOf(sid));
		jsonObject.put("content", contentjb);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnQuery(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in LockHandler-OnQuery");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SELLER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
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
			queryList.add(new QueryBean(Lock.class.getName(), "lockinfo", field, value, valuetype));
		}
		queryList.add(new QueryBean(Lock.class.getName(), "lockinfo", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
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
		List<Lock> result = new ArrayList<Lock>();
		Long total = null;
		try {
			result = lockinfoDaoImpl.getListBy(queryList, true, intlimit, intpage);
			total = lockinfoDaoImpl.selectCount(queryList, true);
		} catch (Exception e) {
		    e.printStackTrace();
			JSONObject jsonObject2 = new JSONObject();
			jsonObject2.put("status", 4);
			jsonObject2.put("msg", e.getMessage());
			jsonObject2.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询锁请求信息成功");
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
			dataArray.put(Utils.getJsonObject(result.get(i)));
		}
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnResponse(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in LockHandler-OnResponse");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_SELLER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		String sid = params.get("sid");
		String response = params.get("response");
		if (sid == null || response == null)
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		queryList.add(new QueryBean(Lock.class.getName(), "lockinfo", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
		queryList.add(new QueryBean(Lock.class.getName(), "lockinfo", "sid", sid, QueryBean.TYPE_EQUAL));
		Lock lock = null;
		try {
			List<Lock> locks = lockinfoDaoImpl.getListBy(queryList, true);
			if(locks.size() > 1){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，锁请求主键重复");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(locks.size() == 0){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此开锁请求");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			lock = locks.get(0);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (lock.getStatus() == Lock.STATUS_RESPONSED || 
				lock.getStatus() == Lock.STATUS_OPERATED) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "该开锁请求已经响应过");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		
		Map<String, String> tmpmap = new HashMap<String, String>();
		tmpmap.put("response", response);
		tmpmap.put("status", String.valueOf(Lock.STATUS_RESPONSED));
		tmpmap.put("responseat", String.valueOf(Utils.getCurrenttimeMills()));
		Transaction tx = session.beginTransaction();
		String oldobject = Utils.getJsonObject(lock).toString();
		Utils.UpdateFromMap(tmpmap, lock);
		lock.updatetime(operatesid);
		try {
			lockinfoDaoImpl.update(lock, oldobject, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		tx.commit();
		// 推送给司机
		{
			JSONObject pushObject = new JSONObject();
			pushObject.put("type", "11");
			pushObject.put("sid", lock.getSid());
			pushObject.put("response", response.equals("0") ? "通过" : "拒绝");
			if(!PushUtils.Push2Driver(lock.getRequestfrom(), pushObject.toString(), session, corporationsid))
				System.err.println("锁请求管理员响应后推送给司机失败");
		}
		// 推送给硬件平台
		if (response.equals("0")) {
			Order order = null;
			Truck truck = null;
			List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
			List<QueryBean> queryBean = new ArrayList<QueryBean>();
			queryBean.add(new QueryBean(Order.class.getName(), "orderp", "driversid", lock.getRequestfrom(), QueryBean.TYPE_EQUAL));
			JSONArray orderstatusArray = new JSONArray();
			orderstatusArray.put(Order.STATUS_DISTRIBUTED);
			orderstatusArray.put(Order.STATUS_RECEIVED);
			orderstatusArray.put(Order.STATUS_LOADED);
			orderstatusArray.put(Order.STATUS_UNLOADED);
			orderstatusArray.put(Order.STATUS_RETURNED);
			orderstatusArray.put(Order.STATUS_VERIFYEDPPP);
			orderstatusArray.put(Order.STATUS_VERIFYEDPP);
			orderstatusArray.put(Order.STATUS_VERIFYEDP);
			queryBean.add(new QueryBean(Order.class.getName(), "orderp", "orderstatus", orderstatusArray.toString(), QueryBean.TYPE_JSONARRAY));
			try {
				orderDaoImpl.fetchListBy(queryBean, result, true, Truck.class);
				if(result.size() > 1)
					throw new Exception("数据库错误，一个司机对应多个活跃的订单");
				if(result.size() == 0){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "该司机没有活跃的订单");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				Map<String, Object> tmpret = result.get(0);
				order = (Order) tmpret.get("Order");
				truck = (Truck) tmpret.get("Truck");
				if(order == null){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "该司机没有活跃的订单");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				if(truck == null){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "数据库错误，订单对应的拖车信息不存在");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库错误，请联系系统管理员:" + e.getMessage());
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			String cid = truck.getCid();
			JSONObject pushObject = new JSONObject();
			pushObject.put("type", "11");
			pushObject.put("sid", lock.getSid());
			pushObject.put("operate", String.valueOf(lock.getRequest()));
			pushObject.put("isTest", "0");
			String pushcontent = pushObject.toString();
			PushUtils.Push2Truck(cid, pushcontent);
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "响应开锁请求成功");
		JSONObject contentjb = new JSONObject();
		contentjb.put("sid", sid);
		jsonObject.put("content", contentjb);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnOperateComplete(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in LockHandler-OnOperateComplete");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		String sid = params.get("sid");
		String operate = params.get("operate");
		String isTest = params.get("isTest");
		if (sid == null)
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		if(isTest.equals("0")){//正常流程需要更新lock信息，同时同时司机和管理员
			Lock lock = null;
			Transaction tx = session.beginTransaction();
			try {
				lock = lockinfoDaoImpl.getById(Long.parseLong(sid), true);
				if(lock == null){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "查无此开锁请求");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				Map<String, String> tmpmap = new HashMap<String, String>();
				tmpmap.put("operate", operate);
				tmpmap.put("status", String.valueOf(Lock.STATUS_RESPONSED));
				tmpmap.put("operatedat", String.valueOf(Utils.getCurrenttimeMills()));
				String oldobject = Utils.getJsonObject(lock).toString();
				Utils.UpdateFromMap(tmpmap, lock);
				lock.updatetime(Long.parseLong("0"));
				lockinfoDaoImpl.update(lock, oldobject, Long.parseLong("0"));
				tx.commit();
				// 推送给管理员
				JSONObject pushObject = new JSONObject();
				pushObject.put("type", "12");
				pushObject.put("sid", lock.getSid());
				pushObject.put("result", String.valueOf(operate));
				String pushcontent = pushObject.toString();
				if(!PushUtils.Push2Admin(pushcontent, session, lock.getCorporationsid(), null, null))
					System.err.println("锁请求硬件完成操作后推送给管理员失败");
				// 推送给司机
				if(!PushUtils.Push2Driver(lock.getRequestfrom(), pushcontent, session, lock.getCorporationsid()))
					System.err.println("锁请求硬件完成操作后推送给司机失败");
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 0);
				jsonObject.put("msg", "上传操作结果成功");
				JSONObject contentjb = new JSONObject();
				contentjb.put("sid", sid);
				jsonObject.put("content", contentjb);
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			} catch (Exception e) {
				tx.rollback();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", e.getMessage());
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}else if(isTest.equals("1")){//测试的时候,推送给测试账号
			try{
				JSONObject pushObject = new JSONObject();
				pushObject.put("retid", "1");//推送类型：1：锁请求返回，0：行车日志返回
				String type = params.get("type");
				pushObject.put("type", type);
				pushObject.put("operate", operate);
				PushUtils.Push2Getui(TestHandler.testcid, pushObject.toString(), PushUtils.ID_TEST);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 0);
				jsonObject.put("msg", "上传操作结果成功");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}catch(Exception e){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", e.getMessage());
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}else{
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "isTest状态码错误");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
	}
}
