package com.Entitys.Truck.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Daos.QueryBean;
import com.Common.Entitys.BasicInfo;
import com.Common.Entitys.Bean;
import com.Common.Interfaces.GetKeyTypeCallback;
import com.Common.Utils.MySession;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
import com.Common.Utils.ValidUtils;
import com.Entitys.Driver.Entity.Driver;
import com.Entitys.Escort.Entity.Escort;
import com.Entitys.Statistics.OrderMonthStatistics;
import com.Entitys.Statistics.OrderYearStatistics;
import com.Entitys.Statistics.StatisticsStatic;
import com.Entitys.Statistics.TruckMaintainStatistics;
import com.Entitys.Statistics.WarnMonthStatistics;
import com.Entitys.Statistics.WarnYearStatistics;
import com.Entitys.Trailer.Dao.TrailerDaoImpl;
import com.Entitys.Trailer.Entity.Trailer;
import com.Entitys.Truck.Dao.TruckDaoImpl;
import com.Entitys.Truck.Entity.Truck;
import com.Entitys.Truck.Entity.TruckArchives;
import com.Entitys.Truck.Entity.TruckMaintain;
import com.Entitys.User.Entity.LoginInfo;
import com.Entitys.Warn.Entity.Warn;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class TruckHandler {
	private Long operatesid, corporationsid;
	private Integer operateusertype, role;
	private TruckDaoImpl truckDaoImpl;
	private TrailerDaoImpl trailerDaoImpl;

	public FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
		if (loginInfo == null)
			return NettyUtils.getTokenError(iskeepAlive);
		operateusertype = loginInfo.getType();
		operatesid = loginInfo.getUsersid();
		this.truckDaoImpl = new TruckDaoImpl(session);
		this.trailerDaoImpl = new TrailerDaoImpl(session);
		if (path[2].equals("create")) {
			return OnCreate(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("update")) {
			return OnUpdate(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("delete")) {
			return OnDelete(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("query")) {
			return OnQuery(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("truckarchives_update")) {
			return OnTruckArchivesUpdate(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("truckarchives_query")) {
			return OnTruckArchivesQuery(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("truckarchives_setannualduration")) {
			return SetAnnualDuration(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("truckarchives_setsecondduration")) {
			return SetSecondDuration(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("truckarchives_setlastannual")) {
			return SetLastAnnual(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("truckarchives_setlastsecond")) {
			return SetLastSecond(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("truckarchives_checkannual")) {
			return NeedAnnualCheck(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("truckarchives_checksecond")) {
			return NeedSecondCheck(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("truckmaintain_add")) {
			return AddMaintain(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("truckmaintain_update")) {
			return OnMaintainUpdate(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("truckmaintain_delete")) {
			return OnMaintainDelete(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("truckmaintain_query")) {
			return OnTruckMaintainQuery(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("truckmaintainstatistics_query")) {
			return OnTruckMaintainStatisticsQuery(ctx, session, method, params, iskeepAlive, loginInfo);
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}

	protected FullHttpResponse OnCreate(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
		System.err.println("in TruckHandler-onCreate");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能创建所属公司的信息，当然super可以指定任意公司，政府没有创建权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员可以创建本公司信息
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以创建本公司信息
			role = BasicInfo.ROLE_CORPORATION;
			corporationsid = loginInfo.getCorporationsid();
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}

		String trucknumber = params.get("trucknumber");
		if (trucknumber == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传车牌号");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		try {
			List<QueryBean> queryList1 = new ArrayList<QueryBean>();
			queryList1.add(new QueryBean(Truck.class.getName(), "truck", "trucknumber", trucknumber, QueryBean.TYPE_EQUAL));
			List<QueryBean> queryList2 = new ArrayList<QueryBean>();
			queryList2.add(new QueryBean(Trailer.class.getName(), "trailer", "trailernumber", trucknumber, QueryBean.TYPE_EQUAL));
			Long total1 = truckDaoImpl.selectCount(queryList1, true);
			Long total2 = trailerDaoImpl.selectCount(queryList2, true);
			if (total1 + total2 > 0) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "该车牌号已被占用");
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

		Transaction tx = session.beginTransaction();
		Truck truck = new Truck();
		Map<String, String> newparams = Utils.paramswritefilter(params, truck, role, corporationsid);
		System.out.println(newparams.get("driversid"));
//		newparams.put("trailersid", newparams.get("defaulttrailer"));
//		newparams.put("driversid", newparams.get("defaultdriver"));
//		newparams.put("escortsid", newparams.get("defaultescort"));
//		newparams.remove("defaulttrailer");
//		newparams.remove("defaultdriver");
//		newparams.remove("defaultescort");
		Utils.UpdateFromMap(newparams, truck);
		///
		TruckArchives truckArchives = new TruckArchives();
		truckArchives.setCorporationsid(truck.getCorporationsid());
		truckArchives.setTrucknumber(truck.getTrucknumber());
		truckArchives.createtime(operatesid);
		long truckArchivessid = MySession.OnSave(truckArchives, session, operatesid);
		///
		TruckMaintainStatistics truckMaintainStatistics = new TruckMaintainStatistics();
		truckMaintainStatistics.setCorporationsid(truck.getCorporationsid());
		truckMaintainStatistics.setTrucknumber(truck.getTrucknumber());
		truckMaintainStatistics.createtime(operatesid);
		long truckMaintainStatisticssid = MySession.OnSave(truckMaintainStatistics, session, operatesid);
		////////////////////////////////////////////////////////////////
		truck.setTruckarchivessid(truckArchivessid);
		truck.setTruckmaintainstatisticssid(truckMaintainStatisticssid);
		truck.createtime(operatesid);
		Long sid = null;
		try {
			sid = truckDaoImpl.insert(truck, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "创建拖车信息成功");
		JSONObject contentjb = new JSONObject();
		contentjb.put("sid", sid);
		jsonObject.put("content", contentjb);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnUpdate(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.err.println("in TruckHandler-OnUpdate");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN; // 可以修改本公司的
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION; // 可以修改本公司的
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		String sidstr = params.get("sid");
		if (sidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传sid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		try {
			Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "sid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 首先根据sid查询基础信息，是否存在
		Truck truck = null;
		try {
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(Truck.class.getName(), "truck", "sid", sidstr, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Truck.class.getName(), "truck", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
			List<Truck> trucks = truckDaoImpl.getListBy(queryList, true);
			if(trucks.size() == 0){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "拖车信息不存在");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(trucks.size() > 1){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "拖车数据库出错，拖车主键"+sidstr+"重复");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			truck = trucks.get(0);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 更新基本信息
		Transaction tx = session.beginTransaction();
		String oldobject = Utils.getJsonObject(truck).toString();
		Map<String, String> newparams = Utils.paramswritefilter(params, truck, role, corporationsid);
		Utils.UpdateFromMap(newparams, truck);
		truck.updatetime(operatesid);
		try {
			truckDaoImpl.update(truck, oldobject, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "更新拖车信息成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnDelete(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.err.println("in TruckHandler-OnDelete");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN; // 可以修改本公司的
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION; // 可以修改本公司的
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		String sidstr = params.get("sid");
		if (sidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传sid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		try {
			Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "sid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 首先根据sid查询基础信息和登录信息，是否存在
		Truck truck = null;
		try {
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(Truck.class.getName(), "truck", "sid", sidstr, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Truck.class.getName(), "truck", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
			List<Truck> trucks = truckDaoImpl.getListBy(queryList, true);
			if(trucks.size() == 0){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "拖车信息不存在");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(trucks.size() > 1){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "拖车数据库出错，拖车主键"+sidstr+"重复");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			truck = trucks.get(0);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 删除信息
		Transaction tx = session.beginTransaction();
		try {
			List<TruckArchives> truckArchivess_delete = session.createQuery("from TruckArchives where datastatus = "
					+ Bean.CREATED + " and sid = " + truck.getTruckarchivessid(), TruckArchives.class).list();
			List<TruckMaintain> truckMaintains_delete = session.createQuery("from TruckMaintain where datastatus = "
					+ Bean.CREATED + " and trucknumber = '" + truck.getTrucknumber() + "'", TruckMaintain.class)
					.list();
			List<TruckMaintainStatistics> truckMaintainStatisticss_delete = session
					.createQuery("from TruckMaintainStatistics where datastatus = " + Bean.CREATED + " and sid = "
							+ truck.getTruckmaintainstatisticssid(), TruckMaintainStatistics.class)
					.list();
			List<Warn> warns_delete = session.createQuery("from Warn where datastatus = " + Bean.CREATED
					+ " and trucknumber = '" + truck.getTrucknumber() + "'", Warn.class).list();
			List<OrderMonthStatistics> orderMonthStatistics_delete = session.createQuery(
					"from OrderMonthStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
							+ StatisticsStatic.OBJTECTYPE_TRUCK + " and objectsid = " + truck.getSid(),
					OrderMonthStatistics.class).list();
			List<OrderYearStatistics> orderYearStatistics_delete = session.createQuery(
					"from OrderYearStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
							+ StatisticsStatic.OBJTECTYPE_TRUCK + " and objectsid = " + truck.getSid(),
					OrderYearStatistics.class).list();
			List<WarnMonthStatistics> WarnMonthStatistics_delete = session.createQuery(
					"from WarnMonthStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
							+ StatisticsStatic.OBJTECTYPE_TRUCK + " and objectsid = " + truck.getSid(),
					WarnMonthStatistics.class).list();
			List<WarnYearStatistics> WarnYearStatistics_delete = session.createQuery(
					"from WarnYearStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
							+ StatisticsStatic.OBJTECTYPE_TRUCK + " and objectsid = " + truck.getSid(),
					WarnYearStatistics.class).list();
			for (int i = 0; i < truckArchivess_delete.size(); i++)
				MySession.OnDelete(truckArchivess_delete.get(i), session, operatesid);
			for (int i = 0; i < truckMaintains_delete.size(); i++)
				MySession.OnDelete(truckMaintains_delete.get(i), session, operatesid);
			for (int i = 0; i < truckMaintainStatisticss_delete.size(); i++)
				MySession.OnDelete(truckMaintainStatisticss_delete.get(i), session, operatesid);
//			for (int i = 0; i < truckLogss_delete.size(); i++) {
//				String tablename = truckLogss_delete.get(i).getTablename_nm_30();
//				MySession.OnDelete(truckLogss_delete.get(i), session, operatesid);
//				List<TruckLog> trucklogs_delete = session
//						.createNativeQuery("from " + tablename + " where datastatus = " + Bean.CREATED, TruckLog.class)
//						.list();
//				for (int j = 0; j < trucklogs_delete.size(); j++)
//					MySession.OnDelete(trucklogs_delete.get(i), session, operatesid);
//			}
			for (int i = 0; i < warns_delete.size(); i++)
				MySession.OnDelete(warns_delete.get(i), session, operatesid);
			for (int i = 0; i < orderMonthStatistics_delete.size(); i++)
				MySession.OnDelete(orderMonthStatistics_delete.get(i), session, operatesid);
			for (int i = 0; i < orderYearStatistics_delete.size(); i++)
				MySession.OnDelete(orderYearStatistics_delete.get(i), session, operatesid);
			for (int i = 0; i < WarnMonthStatistics_delete.size(); i++)
				MySession.OnDelete(WarnMonthStatistics_delete.get(i), session, operatesid);
			for (int i = 0; i < WarnYearStatistics_delete.size(); i++)
				MySession.OnDelete(WarnYearStatistics_delete.get(i), session, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "拖车数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		try {
			truckDaoImpl.delete(truck, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "拖车信息删除成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnQuery(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
		System.err.println("in TruckHandler-OnQuery");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_DRIVER || operateusertype == LoginInfo.TYPE_BUYER
				|| operateusertype == LoginInfo.TYPE_SELLER) {// 本公司分配员工可以查询信息
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员只能查询本公司的信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN;
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以查询本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION;
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {// 政府可以查询任意信息
			String corporationstr = params.get("corporationsid");
			Long corporationsid_ToUpdate = null;
			if (corporationstr != null) {
				try {
					corporationsid_ToUpdate = Long.parseLong(corporationstr);
				} catch (Exception e) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "corporationsid格式不正确");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
			}
			corporationsid = corporationsid_ToUpdate;
			role = BasicInfo.ROLE_GOVERNMENT;
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {// 超级管理员可以查询信息
			String corporationstr = params.get("corporationsid");
			Long corporationsid_ToUpdate = null;
			if (corporationstr != null) {
				try {
					corporationsid_ToUpdate = Long.parseLong(corporationstr);
				} catch (Exception e) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "corporationsid格式不正确");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
			}
			corporationsid = corporationsid_ToUpdate;
			role = BasicInfo.ROLE_SUPER;
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}

		Map<String, String> newparams = Utils.paramsqueryfilter(params, role, corporationsid, operatesid);
		Iterator<Entry<String, String>> iterator = newparams.entrySet().iterator();
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
			queryList.add(new QueryBean(Truck.class.getName(), "truck", field, value, valuetype));
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
			truckDaoImpl.fetchListBy(queryList, result, true, intlimit, intpage, Trailer.class, Driver.class, Escort.class);
			total = truckDaoImpl.selectCount(queryList, true);
		} catch (Exception e) {
			JSONObject jsonObject2 = new JSONObject();
			jsonObject2.put("status", 4);
			jsonObject2.put("msg", e.getMessage());
			jsonObject2.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
		}
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询拖车信息成功");
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
			Truck truck = (Truck) tmpret.get("Truck");
			Trailer trailer = (Trailer) tmpret.get("Trailer");
			Driver driver = (Driver) tmpret.get("Driver");
			Escort escort = (Escort) tmpret.get("Escort");
			JSONObject tmpjsonObject = Utils.getJsonObject(truck);
			tmpjsonObject.put("trailernumbr", trailer == null ? "null" : trailer.getTrailernumber());
			tmpjsonObject.put("drivername", driver == null ? "null" : driver.getName());
			tmpjsonObject.put("escortname", escort == null ? "null" : escort.getName());
			dataArray.put(tmpjsonObject);
		}
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnTruckArchivesUpdate(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.err.println("in TruckHandler-OnTruckArchivesUpdate");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN; // 可以修改本公司的
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION; // 可以修改本公司的
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		String sidstr = params.get("sid");
		Long sid = null;
		if (sidstr == null) {
			String trucksidstr = params.get("trucksid");
			if (trucksidstr == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请填写拖车sid或者拖车档案sid");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			try {
				Long.parseLong(trucksidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "trucksid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			// 首先根据sid查询基础信息和登录信息，是否存在
			try {
				List<QueryBean> queryList = new ArrayList<QueryBean>();
				queryList.add(new QueryBean(Truck.class.getName(), "truck", "sid", trucksidstr, QueryBean.TYPE_EQUAL));
				queryList.add(new QueryBean(Truck.class.getName(), "truck", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
				List<Truck> trucks = truckDaoImpl.getListBy(queryList, true);
				if(trucks.size() > 1){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "拖车数据库出错，主键"+trucksidstr+"重复");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				if(trucks.size() == 0){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "查无此拖车信息");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				sid = trucks.get(0).getTruckarchivessid();
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", e.getMessage());
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else {
			try {
				sid = Long.parseLong(sidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "sid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}
		TruckArchives truckArchives = null;
		try {
			truckArchives = session
					.createQuery("from TruckArchives where datastatus = " + Bean.CREATED + " and sid = " + sid,
							TruckArchives.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (truckArchives == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}

		// 更新基本信息
		Transaction tx = session.beginTransaction();
		String oldobject = Utils.getJsonObject(truckArchives).toString();
		Map<String, String> newparams = Utils.paramswritefilter(params, truckArchives, role, corporationsid);
		Utils.UpdateFromMap(newparams, truckArchives);
		truckArchives.updatetime(operatesid);
		try {
			ValidUtils.ValidationWithExp(truckArchives);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnUpdate(oldobject, truckArchives, session, operatesid);
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "更新拖车档案信息成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnTruckArchivesQuery(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
		System.err.println("in TruckHandler-OnTruckArchivesQuery");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_DRIVER || operateusertype == LoginInfo.TYPE_BUYER
				|| operateusertype == LoginInfo.TYPE_SELLER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN;
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION;
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {
			String corporationstr = params.get("corporationsid");
			Long corporationsid_ToUpdate = null;
			if (corporationstr != null) {
				try {
					corporationsid_ToUpdate = Long.parseLong(corporationstr);
				} catch (Exception e) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "corporationsid格式不正确");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
			}
			corporationsid = corporationsid_ToUpdate;
			role = BasicInfo.ROLE_GOVERNMENT;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		StringBuilder hql = new StringBuilder();
		StringBuilder hql2 = new StringBuilder();
		hql.append("from TruckArchives");
		hql2.append("select count(*) from TruckArchives");
		Map<String, String> newparams = new HashMap<String, String>();
		newparams.put("datastatus", String.valueOf(Bean.CREATED));
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.equals("trucknumber") || key.equals("sid") || key.equals("dph") || key.equals("cp")
					|| key.equals("corporationsid") || key.equals("ygjg") || key.equals("cxfl")) {
				newparams.put(key, value);
			}
		}
		if (role != BasicInfo.ROLE_GOVERNMENT) {
			newparams.put("corporationsid", String.valueOf(corporationsid));
		}
		String conditionstr = Utils.getConditionStr(newparams, new GetKeyTypeCallback() {
			@Override
			public int getKeyType(String key) {
				try {
					return Utils.getKeyType(key, TruckArchives.class.getName());					
				} catch (Exception e) {
					return 4;
				}
			}
		});
		if (conditionstr != null) {
			hql.append(" where" + conditionstr);
			hql2.append(" where" + conditionstr);
		}
		Query<TruckArchives> query = session.createQuery(hql.toString(), TruckArchives.class);
		Query<Long> query2 = session.createQuery(hql2.toString(), Long.class);
		Long total = query2.uniqueResult();
		String limit = params.get("limit");
		int intpage = 0;
		int intlimit = 0;
		if (limit != null) {
			intlimit = Integer.valueOf(limit);
			query.setMaxResults(intlimit);
			String page = params.get("page");
			if (page == null)
				page = "1";
			intpage = Integer.valueOf(page);
			query.setFirstResult((intpage-1) * intlimit);
		}
		List<TruckArchives> truckArchivess = query.list();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询拖车档案信息成功");
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
		for (int i = 0; i < truckArchivess.size(); i++){
			JSONObject jsonObject2 = Utils.getJsonObjectWithPremission(truckArchivess.get(i), role);
			dataArray.put(jsonObject2);
		}
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse SetAnnualDuration(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.err.println("in TruckHandler-SetAnnualDuration");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN; // 可以修改本公司的
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION; // 可以修改本公司的
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		String sidstr = params.get("sid");
		String annualdurationstr = params.get("annualduration");
		System.out.println("annualduration = " +annualdurationstr);
		if (annualdurationstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请填写年检周期");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long annualduration = null;
		try {
			annualduration = Long.parseLong(annualdurationstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "年检周期格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long sid = null;
		if (sidstr == null) {
			String trucksidstr = params.get("trucksid");
			String trucknumber = params.get("trucknumber");
			if (trucksidstr == null && trucknumber == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请填写拖车sid或拖车车牌号或者拖车档案记录表sid");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(trucksidstr != null){
				try {
					Long.parseLong(trucksidstr);
				} catch (Exception e) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "trucksid格式不正确");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
			}
			// 首先根据sid查询基础信息和登录信息，是否存在
			try {
				List<QueryBean> queryList = new ArrayList<QueryBean>();
				if(trucksidstr != null)
					queryList.add(new QueryBean(Truck.class.getName(), "truck", "sid", trucksidstr, QueryBean.TYPE_EQUAL));
				if(trucknumber != null)
					queryList.add(new QueryBean(Truck.class.getName(), "truck", "trucknumber", trucknumber, QueryBean.TYPE_EQUAL));
				queryList.add(new QueryBean(Truck.class.getName(), "truck", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
				List<Truck> trucks = truckDaoImpl.getListBy(queryList, true);
				if(trucks.size() > 1){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "拖车数据库出错，主键重复");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				if(trucks.size() == 0){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "查无此拖车信息");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				sid = trucks.get(0).getTruckarchivessid();
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", e.getMessage());
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else {
			try {
				sid = Long.parseLong(sidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "sid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}
		TruckArchives truckArchives = null;
		try {
			truckArchives = session
					.createQuery("from TruckArchives where datastatus = " + Bean.CREATED + " and sid = " + sid,
							TruckArchives.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (truckArchives == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 更新基本信息
		Transaction tx = session.beginTransaction();
		String oldobject = Utils.getJsonObject(truckArchives).toString();
		truckArchives.setAnnalduration(annualduration);
		truckArchives.updatetime(operatesid);
		try {
			ValidUtils.ValidationWithExp(truckArchives);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnUpdate(oldobject, truckArchives, session, operatesid);
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "设置年检周期成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse SetSecondDuration(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.err.println("in TruckHandler-SetSecondDuration");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN; // 可以修改本公司的
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION; // 可以修改本公司的
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		String sidstr = params.get("sid");
		String seconddurationstr = params.get("secondduration");
		if (seconddurationstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请填写二级维护周期");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long secondduration = null;
		try {
			secondduration = Long.parseLong(seconddurationstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "二级维护周期格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long sid = null;
		if (sidstr == null) {
			String trucksidstr = params.get("trucksid");
			if (trucksidstr == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请填写拖车sid或者拖车档案记录表sid");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			try {
				Long.parseLong(trucksidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "trucksid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			// 首先根据sid查询基础信息和登录信息，是否存在
			try {
				List<QueryBean> queryList = new ArrayList<QueryBean>();
				queryList.add(new QueryBean(Truck.class.getName(), "truck", "sid", trucksidstr, QueryBean.TYPE_EQUAL));
				queryList.add(new QueryBean(Truck.class.getName(), "truck", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
				List<Truck> trucks = truckDaoImpl.getListBy(queryList, true);
				if(trucks.size() > 1){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "拖车数据库出错，主键"+trucksidstr+"重复");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				if(trucks.size() == 0){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "查无此拖车信息");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				sid = trucks.get(0).getTruckarchivessid();
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", e.getMessage());
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else {
			try {
				sid = Long.parseLong(sidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "sid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}
		TruckArchives truckArchives = null;
		try {
			truckArchives = session
					.createQuery("from TruckArchives where datastatus = " + Bean.CREATED + " and sid = " + sid,
							TruckArchives.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (truckArchives == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 更新基本信息
		Transaction tx = session.beginTransaction();
		String oldobject = Utils.getJsonObject(truckArchives).toString();
		truckArchives.setSecondduration(secondduration);
		truckArchives.updatetime(operatesid);
		try {
			ValidUtils.ValidationWithExp(truckArchives);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnUpdate(oldobject, truckArchives, session, operatesid);
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "设置二级维护周期成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse SetLastAnnual(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.err.println("in TruckHandler-SetLastAnnual");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN; // 可以修改本公司的
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION; // 可以修改本公司的
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		String sidstr = params.get("sid");
		String lastannualtimestr = params.get("lastannualtime");
		if (lastannualtimestr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请填写上次年检时间");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long lastannualtime = null;
		try {
			lastannualtime = Long.parseLong(lastannualtimestr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "上次年检时间格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long sid = null;
		if (sidstr == null) {
			String trucksidstr = params.get("trucksid");
			if (trucksidstr == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请填写拖车sid或者拖车档案记录表sid");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			try {
				Long.parseLong(trucksidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "trucksid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			// 首先根据sid查询基础信息和登录信息，是否存在
			try {
				List<QueryBean> queryList = new ArrayList<QueryBean>();
				queryList.add(new QueryBean(Truck.class.getName(), "truck", "sid", trucksidstr, QueryBean.TYPE_EQUAL));
				queryList.add(new QueryBean(Truck.class.getName(), "truck", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
				List<Truck> trucks = truckDaoImpl.getListBy(queryList, true);
				if(trucks.size() > 1){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "拖车数据库出错，主键"+trucksidstr+"重复");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				if(trucks.size() == 0){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "查无此拖车信息");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				sid = trucks.get(0).getTruckarchivessid();
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", e.getMessage());
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else {
			try {
				sid = Long.parseLong(sidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "sid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}
		TruckArchives truckArchives = null;
		try {
			truckArchives = session
					.createQuery("from TruckArchives where datastatus = " + Bean.CREATED + " and sid = " + sid,
							TruckArchives.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (truckArchives == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 更新基本信息
		Transaction tx = session.beginTransaction();
		String oldobject = Utils.getJsonObject(truckArchives).toString();
		truckArchives.setLastannualtime(lastannualtime);
		truckArchives.updatetime(operatesid);
		try {
			ValidUtils.ValidationWithExp(truckArchives);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnUpdate(oldobject, truckArchives, session, operatesid);
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "设置上次年检时间成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse SetLastSecond(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.err.println("in TruckHandler-SetLastSecond");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN; // 可以修改本公司的
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION; // 可以修改本公司的
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		String sidstr = params.get("sid");
		String lastsecondtimestr = params.get("lastsecondtime");
		String lastsecondcontent = params.get("lastsecondcontent");
		if (lastsecondcontent == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请填写上次二级维护具体内容");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (lastsecondtimestr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请填写上次二级维护时间");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long lastsecondtime = null;
		try {
			lastsecondtime = Long.parseLong(lastsecondtimestr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "上次二级维护时间格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long sid = null;
		if (sidstr == null) {
			String trucksidstr = params.get("trucksid");
			if (trucksidstr == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请填写拖车sid或者拖车档案记录表sid");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			try {
				Long.parseLong(trucksidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "trucksid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			// 首先根据sid查询基础信息和登录信息，是否存在
			try {
				List<QueryBean> queryList = new ArrayList<QueryBean>();
				queryList.add(new QueryBean(Truck.class.getName(), "truck", "sid", trucksidstr, QueryBean.TYPE_EQUAL));
				queryList.add(new QueryBean(Truck.class.getName(), "truck", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
				List<Truck> trucks = truckDaoImpl.getListBy(queryList, true);
				if(trucks.size() > 1){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "拖车数据库出错，主键"+trucksidstr+"重复");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				if(trucks.size() == 0){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "查无此拖车信息");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				sid = trucks.get(0).getTruckarchivessid();
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", e.getMessage());
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else {
			try {
				sid = Long.parseLong(sidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "sid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}
		TruckArchives truckArchives = null;
		try {
			truckArchives = session
					.createQuery("from TruckArchives where datastatus = " + Bean.CREATED + " and sid = " + sid,
							TruckArchives.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (truckArchives == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 更新基本信息
		Transaction tx = session.beginTransaction();
		String oldobject = Utils.getJsonObject(truckArchives).toString();
		truckArchives.setLastsecondtime(lastsecondtime);
		truckArchives.setLastsecondcontent_fm_100(lastsecondcontent);
		truckArchives.updatetime(operatesid);
		try {
			ValidUtils.ValidationWithExp(truckArchives);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnUpdate(oldobject, truckArchives, session, operatesid);
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "设置上次二级维护成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse NeedAnnualCheck(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.err.println("in TruckHandler-NeedAnnualCheck");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN; // 可以修改本公司的
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION; // 可以修改本公司的
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {
			String corporationstr = params.get("corporationsid");
			Long corporationsid_ToUpdate = null;
			if (corporationstr != null) {
				try {
					corporationsid_ToUpdate = Long.parseLong(corporationstr);
				} catch (Exception e) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "corporationsid格式不正确");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
			}
			corporationsid = corporationsid_ToUpdate;
			role = BasicInfo.ROLE_GOVERNMENT;
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		String sidstr = params.get("sid");
		Long sid = null;
		if (sidstr == null) {
			String trucksidstr = params.get("trucksid");
			if (trucksidstr == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请填写拖车sid或者拖车档案记录表sid");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			try {
				Long.parseLong(trucksidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "trucksid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			// 首先根据sid查询基础信息和登录信息，是否存在
			try {
				List<QueryBean> queryList = new ArrayList<QueryBean>();
				queryList.add(new QueryBean(Truck.class.getName(), "truck", "sid", trucksidstr, QueryBean.TYPE_EQUAL));
				queryList.add(new QueryBean(Truck.class.getName(), "truck", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
				List<Truck> trucks = truckDaoImpl.getListBy(queryList, true);
				if(trucks.size() > 1){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "拖车数据库出错，主键"+trucksidstr+"重复");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				if(trucks.size() == 0){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "查无此拖车信息");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				sid = trucks.get(0).getTruckarchivessid();
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", e.getMessage());
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else {
			try {
				sid = Long.parseLong(sidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "sid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}
		TruckArchives truckArchives = null;
		try {
			truckArchives = session
					.createQuery("from TruckArchives where datastatus = " + Bean.CREATED + " and sid = " + sid,
							TruckArchives.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (truckArchives == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		if (truckArchives.needannualcheck())
			jsonObject.put("msg", "需要年检");
		else {
			jsonObject.put("msg", "尚未超过年检周期");
		}
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse NeedSecondCheck(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.err.println("in TruckHandler-NeedSecondCheck");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN; // 可以修改本公司的
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION; // 可以修改本公司的
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {
			String corporationstr = params.get("corporationsid");
			Long corporationsid_ToUpdate = null;
			if (corporationstr != null) {
				try {
					corporationsid_ToUpdate = Long.parseLong(corporationstr);
				} catch (Exception e) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "corporationsid格式不正确");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
			}
			corporationsid = corporationsid_ToUpdate;
			role = BasicInfo.ROLE_GOVERNMENT;
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		String sidstr = params.get("sid");
		Long sid = null;
		if (sidstr == null) {
			String trucksidstr = params.get("trucksid");
			if (trucksidstr == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请填写拖车sid或者拖车档案记录表sid");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			try {
				Long.parseLong(trucksidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "trucksid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			// 首先根据sid查询基础信息和登录信息，是否存在
			try {
				List<QueryBean> queryList = new ArrayList<QueryBean>();
				queryList.add(new QueryBean(Truck.class.getName(), "truck", "sid", trucksidstr, QueryBean.TYPE_EQUAL));
				queryList.add(new QueryBean(Truck.class.getName(), "truck", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
				List<Truck> trucks = truckDaoImpl.getListBy(queryList, true);
				if(trucks.size() > 1){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "拖车数据库出错，主键"+trucksidstr+"重复");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				if(trucks.size() == 0){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "查无此拖车信息");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
				sid = trucks.get(0).getTruckarchivessid();
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", e.getMessage());
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else {
			try {
				sid = Long.parseLong(sidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "sid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}
		TruckArchives truckArchives = null;
		try {
			truckArchives = session
					.createQuery("from TruckArchives where datastatus = " + Bean.CREATED + " and sid = " + sid,
							TruckArchives.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (truckArchives == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		if (truckArchives.needsecondcheck())
			jsonObject.put("msg", "需要二级维护");
		else {
			jsonObject.put("msg", "尚未超过二级维护周期");
		}
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse AddMaintain(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.err.println("in TruckHandler-AddMaintain");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN; // 可以修改本公司的
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION; // 可以修改本公司的
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		String jcrqstr = params.get("jcrq");
		String ccrqstr = params.get("ccrq");
		if (jcrqstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请填写进厂日期");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long jcrq = null;
		try {
			jcrq = Long.parseLong(jcrqstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "进厂日期格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (ccrqstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请填写出厂日期");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long ccrq = null;
		try {
			ccrq = Long.parseLong(ccrqstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "出厂日期格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		String trucksidstr = params.get("trucksid");
		if (trucksidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请填写拖车sid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		try {
			Long.parseLong(trucksidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "trucksid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 首先根据sid查询基础信息和登录信息，是否存在
		Truck truck = null;
		try {
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(Truck.class.getName(), "truck", "sid", trucksidstr, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Truck.class.getName(), "truck", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
			List<Truck> trucks = truckDaoImpl.getListBy(queryList, true);
			if(trucks.size() > 1){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "拖车数据库出错，主键"+trucksidstr+"重复");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(trucks.size() == 0){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此拖车信息");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			truck = trucks.get(0);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		TruckMaintainStatistics truckMaintainStatistics = null;
		try {
			truckMaintainStatistics = session
					.createQuery("from TruckMaintainStatistics where datastatus = " + Bean.CREATED + " and sid = "
							+ truck.getTruckmaintainstatisticssid(), TruckMaintainStatistics.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (truckMaintainStatistics == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 更新基本信息
		Transaction tx = session.beginTransaction();
		TruckMaintain truckMaintain = new TruckMaintain();
		Map<String, String> newparams = Utils.paramswritefilter(params, truckMaintain, role, corporationsid);
		newparams.put("trucknumber", truck.getTrucknumber());
		Utils.UpdateFromMap(newparams, truckMaintain);
		truckMaintain.createtime(operatesid);
		try {
			ValidUtils.ValidationWithExp(truckMaintain);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnSave(truckMaintain, session, operatesid);
		String oldobject = Utils.getJsonObject(truckMaintainStatistics).toString();
		truckMaintainStatistics.addmaintain();
		truckMaintainStatistics.setZjjcrq(jcrq);
		truckMaintainStatistics.setZjccrq(ccrq);
		truckMaintainStatistics.updatetime(operatesid);
		try {
			ValidUtils.ValidationWithExp(truckMaintainStatistics);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnUpdate(oldobject, truckMaintainStatistics, session, operatesid);
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "添加拖车修理记录成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnMaintainUpdate(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.err.println("in TruckHandler-OnMaintainUpdate");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN; // 可以修改本公司的
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION; // 可以修改本公司的
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		String sidstr = params.get("sid");
		if (sidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请填写要修改的拖车维修单的sid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "拖车维修单sid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		TruckMaintain truckMaintain = null;
		try {
			truckMaintain = session.createQuery("from TruckMaintain where datastatus = " + Bean.CREATED
					+ " and corporationsid = " + corporationsid + " and sid = " + sid, TruckMaintain.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (truckMaintain == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "查无此车辆维修单");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (!corporationsid.equals(truckMaintain.getCorporationsid())) {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		Truck truck = null;
		try {
			truck = truckDaoImpl.getByTrucknumber(truckMaintain.getTrucknumber(), false);
			if(!truck.getCorporationsid().equals(corporationsid))
				truck = null;
			if (truck == null) {
				throw new Exception("没有对应的拖车信息");
			}
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		TruckMaintainStatistics truckMaintainStatistics = null;
		try {
			truckMaintainStatistics = session
					.createQuery("from TruckMaintainStatistics where datastatus = " + Bean.CREATED + " and sid = "
							+ truck.getTruckmaintainstatisticssid(), TruckMaintainStatistics.class)
					.uniqueResult();
			if (truckMaintainStatistics == null) {
				throw new Exception("没有对应的车辆维修统计信息");
			}
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员：" + e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}

		// 更新基本信息
		Transaction tx = session.beginTransaction();
		Map<String, String> newparams = Utils.paramswritefilter(params, truckMaintain, role, corporationsid);
		newparams.put("trucknumber", truck.getTrucknumber());
		String oldmaintain = Utils.getJsonObject(truckMaintain).toString();
		Utils.UpdateFromMap(newparams, truckMaintain);
		truckMaintain.updatetime(operatesid);
		try {
			ValidUtils.ValidationWithExp(truckMaintain);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnUpdate(oldmaintain, truckMaintain, session, operatesid);
		String jcrqstr = params.get("jcrq");
		String ccrqstr = params.get("ccrq");
		Long jcrq = null;
		Long ccrq = null;
		if (jcrqstr != null) {
			try {
				jcrq = Long.parseLong(jcrqstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "进厂日期格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}
		if (ccrqstr != null) {
			try {
				ccrq = Long.parseLong(ccrqstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "出厂日期格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}
		if (jcrq != null || ccrq != null) {
			String oldobject = Utils.getJsonObject(truckMaintainStatistics).toString();
			if (jcrq != null)
				truckMaintainStatistics.setZjjcrq(jcrq);
			if (ccrq != null)
				truckMaintainStatistics.setZjccrq(ccrq);
			truckMaintainStatistics.updatetime(operatesid);
			try {
				ValidUtils.ValidationWithExp(truckMaintainStatistics);
			} catch (Exception e) {
				tx.rollback();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", e.getMessage());
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			MySession.OnUpdate(oldobject, truckMaintainStatistics, session, operatesid);
		}
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "修改拖车修理记录成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnMaintainDelete(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.err.println("in TruckHandler-OnMaintainDelete");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN; // 可以删除本公司的
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以删除本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION; // 可以删除本公司的
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		String sidstr = params.get("sid");
		if (sidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请填写要修改的拖车维修单的sid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "拖车维修单sid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		TruckMaintain truckMaintain = null;
		try {
			truckMaintain = session.createQuery("from TruckMaintain where datastatus = " + Bean.CREATED
					+ " and corporationsid = " + corporationsid + " and sid = " + sid, TruckMaintain.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (truckMaintain == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "查无此车辆维修单");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (!corporationsid.equals(truckMaintain.getCorporationsid())) {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		Truck truck = null;
		try {
			truck = truckDaoImpl.getByTrucknumber(truckMaintain.getTrucknumber(), false);
			if(!truck.getCorporationsid().equals(corporationsid))
				truck = null;
			if (truck == null) {
				throw new Exception("没有对应的车辆信息");
			}
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "拖车数据库出错，请联系管理员:" + e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		TruckMaintainStatistics truckMaintainStatistics = null;
		try {
			truckMaintainStatistics = session
					.createQuery("from TruckMaintainStatistics where datastatus = " + Bean.CREATED + " and sid = "
							+ truck.getTruckmaintainstatisticssid(), TruckMaintainStatistics.class)
					.uniqueResult();
			if (truckMaintainStatistics == null) {
				throw new Exception("没有对应的车辆维修统计信息");
			}
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系管理员：" + e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}

		// 更新基本信息
		Transaction tx = session.beginTransaction();
		MySession.OnDelete(truckMaintain, session, operatesid);
		String oldobject = Utils.getJsonObject(truckMaintainStatistics).toString();
		truckMaintainStatistics.setZjjcrq(null);
		truckMaintainStatistics.setZjccrq(null);
		truckMaintainStatistics.submaintain();
		truckMaintainStatistics.updatetime(operatesid);
		try {
			ValidUtils.ValidationWithExp(truckMaintainStatistics);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnUpdate(oldobject, truckMaintainStatistics, session, operatesid);
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "删除拖车修理记录成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnTruckMaintainQuery(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
		System.err.println("in TruckHandler-OnTruckMaintainQuery");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_DRIVER || operateusertype == LoginInfo.TYPE_BUYER
				|| operateusertype == LoginInfo.TYPE_SELLER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN;
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION;
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {
			String corporationstr = params.get("corporationsid");
			Long corporationsid_ToUpdate = null;
			if (corporationstr != null) {
				try {
					corporationsid_ToUpdate = Long.parseLong(corporationstr);
				} catch (Exception e) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "corporationsid格式不正确");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
			}
			corporationsid = corporationsid_ToUpdate;
			role = BasicInfo.ROLE_GOVERNMENT;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		StringBuilder hql = new StringBuilder();
		StringBuilder hql2 = new StringBuilder();
		hql.append("from TruckMaintain");
		hql2.append("select count(*) from TruckMaintain");
		Map<String, String> newparams = new HashMap<String, String>();
		newparams.put("datastatus", String.valueOf(Bean.CREATED));
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.equals("sid") || key.equals("trucknumber") || key.equals("corporationsid")) {
				newparams.put(key, value);
			}
		}
		if (role != BasicInfo.ROLE_GOVERNMENT) {
			newparams.put("corporationsid", String.valueOf(corporationsid));
		}
		String conditionstr = Utils.getConditionStr(newparams, new GetKeyTypeCallback() {
			@Override
			public int getKeyType(String key) {
				try {
					return Utils.getKeyType(key, TruckArchives.class.getName());					
				} catch (Exception e) {
					return 4;
				}
			}
		});
		if (conditionstr != null) {
			hql.append(" where" + conditionstr);
			hql2.append(" where" + conditionstr);
		}
		Query<TruckMaintain> query = session.createQuery(hql.toString(), TruckMaintain.class);
		Query<Long> query2 = session.createQuery(hql2.toString(), Long.class);
		Long total = query2.uniqueResult();
		String limit = params.get("limit");
		int intpage = 0;
		int intlimit = 0;
		if (limit != null) {
			intlimit = Integer.valueOf(limit);
			query.setMaxResults(intlimit);
			String page = params.get("page");
			if (page == null)
				page = "1";
			intpage = Integer.valueOf(page);
			query.setFirstResult((intpage-1) * intlimit);
		}
		List<TruckMaintain> truckMaintains = query.list();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询拖车维修信息成功");
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
		for (int i = 0; i < truckMaintains.size(); i++)
			dataArray.put(Utils.getJsonObject(truckMaintains.get(i)));
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnTruckMaintainStatisticsQuery(ChannelHandlerContext ctx, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
		System.err.println("in TruckHandler-OnTruckMaintainStatisticsQuery");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_DRIVER || operateusertype == LoginInfo.TYPE_BUYER
				|| operateusertype == LoginInfo.TYPE_SELLER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN;
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_CORPORATION;
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {
			String corporationstr = params.get("corporationsid");
			Long corporationsid_ToUpdate = null;
			if (corporationstr != null) {
				try {
					corporationsid_ToUpdate = Long.parseLong(corporationstr);
				} catch (Exception e) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", "corporationsid格式不正确");
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
			}
			corporationsid = corporationsid_ToUpdate;
			role = BasicInfo.ROLE_GOVERNMENT;
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		StringBuilder hql = new StringBuilder();
		StringBuilder hql2 = new StringBuilder();
		hql.append("from TruckMaintainStatistics");
		hql2.append("select count(*) from TruckMaintainStatistics");
		Map<String, String> newparams = new HashMap<String, String>();
		newparams.put("datastatus", String.valueOf(Bean.CREATED));
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.equals("sid") || key.equals("trucknumber") || key.equals("corporationsid")) {
				newparams.put(key, value);
			}
		}
		if (role != BasicInfo.ROLE_GOVERNMENT) {
			newparams.put("corporationsid", String.valueOf(corporationsid));
		}
		String conditionstr = Utils.getConditionStr(newparams, new GetKeyTypeCallback() {
			@Override
			public int getKeyType(String key) {
				try {
					return Utils.getKeyType(key, TruckArchives.class.getName());					
				} catch (Exception e) {
					return 4;
				}
			}
		});
		if (conditionstr != null) {
			hql.append(" where" + conditionstr);
			hql2.append(" where" + conditionstr);
		}
		System.out.println(hql.toString());
		Query<TruckMaintainStatistics> query = session.createQuery(hql.toString(), TruckMaintainStatistics.class);
		Query<Long> query2 = session.createQuery(hql2.toString(), Long.class);
		Long total = query2.uniqueResult();
		String limit = params.get("limit");
		int intpage = 0;
		int intlimit = 0;
		if (limit != null) {
			intlimit = Integer.valueOf(limit);
			query.setMaxResults(intlimit);
			String page = params.get("page");
			if (page == null)
				page = "1";
			intpage = Integer.valueOf(page);
			query.setFirstResult((intpage-1) * intlimit);
		}
		List<TruckMaintainStatistics> truckMaintainStatisticss = query.list();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询拖车维修统计信息成功");
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
		for (int i = 0; i < truckMaintainStatisticss.size(); i++)
			dataArray.put(Utils.getJsonObject(truckMaintainStatisticss.get(i)));
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}
}

// protected FullHttpResponse OnTruckCheckQuery(ChannelHandlerContext ctx,
// Session session, HttpMethod method,
// Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
// throws JSONException, InstantiationException, IllegalAccessException {
// if (method != HttpMethod.GET) {
// return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
// }
// if (operateusertype == LoginInfo.TYPE_DRIVER || operateusertype ==
// LoginInfo.TYPE_BUYER
// || operateusertype == LoginInfo.TYPE_SELLER) {
// role = BasicInfo.ROLE_USER;
// corporationsid = loginInfo.getCorporationsid();
// } else if (operateusertype == LoginInfo.TYPE_ADMIN) {
// corporationsid = loginInfo.getCorporationsid();
// role = BasicInfo.ROLE_ADMIN;
// } else if (operateusertype == LoginInfo.TYPE_CORPORATION) {
// corporationsid = loginInfo.getCorporationsid();
// role = BasicInfo.ROLE_CORPORATION;
// } else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {
// String corporationstr = params.get("corporationsid");
// Long corporationsid_ToUpdate = null;
// if (corporationstr != null) {
// try {
// corporationsid_ToUpdate = Long.parseLong(corporationstr);
// } catch (Exception e) {
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 4);
// jsonObject.put("msg", "corporationsid格式不正确");
// jsonObject.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
// }
// corporationsid = corporationsid_ToUpdate;
// role = BasicInfo.ROLE_GOVERNMENT;
// } else {
// return NettyUtils.getTokenError(iskeepAlive);
// }
//
// StringBuilder hql = new StringBuilder();
// StringBuilder hql2 = new StringBuilder();
// hql.append("from TruckCheck");
// hql2.append("select count(*) from TruckCheck");
// Map<String, String> newparams = new HashMap<String, String>();
// Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
// while (iterator.hasNext()) {
// Entry<String, String> entry = iterator.next();
// String key = entry.getKey();
// String value = entry.getValue();
// if (key.equals("sid") || key.equals("trucknumber") ||
// key.equals("corporationsid")) {
// newparams.put(key, value);
// }
// }
// if (role != BasicInfo.ROLE_GOVERNMENT) {
// newparams.put("corporationsid", String.valueOf(corporationsid));
// }
// String conditionstr = Utils.getConditionStr(newparams, new
// GetKeyTypeCallback() {
// @Override
// public int getKeyType(String key) {
// return Utils.getKeyType(key, TruckArchives.class);
// }
// });
// if (conditionstr != null) {
// hql.append(" where" + conditionstr);
// hql2.append(" where" + conditionstr);
// }
// System.out.println(hql.toString());
// Query<TruckCheck> query = session.createQuery(hql.toString(),
// TruckCheck.class);
// Query<Long> query2 = session.createQuery(hql2.toString(), Long.class);
// Long total = query2.uniqueResult();
// String limit = params.get("limit");
// int intpage = 0;
// int intlimit = 0;
// if (limit != null) {
// intlimit = Integer.valueOf(limit);
// query.setMaxResults(intlimit);
// String page = params.get("page");
// if (page == null)
// page = "1";
// intpage = Integer.valueOf(page);
// query.setFirstResult(intpage * intlimit);
// }
// List<TruckCheck> truckChecks = query.list();
// JSONObject jsonObject = new JSONObject();
// jsonObject.put("status", 0);
// jsonObject.put("msg", "query truckChecks succeed");
// JSONObject contentJsonObject = new JSONObject();
// if (limit != null) {
// if (intlimit * intpage >= total) {
// JSONObject jsonObject2 = new JSONObject();
// jsonObject2.put("status", 4);
// jsonObject2.put("msg", "第" + (intpage + 1) + "页不存在");
// jsonObject2.put("content", "");
// return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
// }
// contentJsonObject.put("total", total);
// contentJsonObject.put("perpage", limit);
// contentJsonObject.put("current_page", intpage + 1);
// contentJsonObject.put("from", intpage * intlimit + 1);
// if ((intpage + 1) * intlimit > total)
// contentJsonObject.put("to", total);
// else
// contentJsonObject.put("to", (intpage + 1) * intlimit);
// } else {
// contentJsonObject.put("total", total);
// contentJsonObject.put("perpage", total);
// contentJsonObject.put("current_page", 1);
// contentJsonObject.put("from", 1);
// contentJsonObject.put("to", total);
// }
// JSONArray dataArray = new JSONArray();
// for (int i = 0; i < truckChecks.size(); i++)
// dataArray.put(Utils.getJsonObject(truckChecks.get(i)));
// contentJsonObject.put("data", dataArray);
// jsonObject.put("content", contentJsonObject);
// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
// }
