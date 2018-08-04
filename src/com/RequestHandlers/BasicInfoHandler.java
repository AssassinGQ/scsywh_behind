package com.RequestHandlers;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Entitys.BasicInfo;
import com.Common.Entitys.Bean;
import com.Common.Interfaces.GetKeyTypeCallback;
import com.Common.Utils.MySession;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
import com.Common.Utils.ValidUtils;
import com.Entitys.User.Entity.LoginInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class BasicInfoHandler<T extends Bean> {
	protected Class<T> clazz;
	protected String clazzname;
	private Long operatesid, corporationsid;
	private Integer operateusertype, role;

	public BasicInfoHandler(Class<T> clazz) {
		this.clazz = clazz;
		String[] tmp = clazz.getName().split("\\.");
		this.clazzname = tmp[tmp.length - 1];
	}

	public FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
		System.err.println("in Basic" + clazzname + "Request");
		if (loginInfo == null)
			return NettyUtils.getTokenError(iskeepAlive);
		operateusertype = loginInfo.getType();
		operatesid = loginInfo.getUsersid();
		if (path[2].equals("create")) {
			return OnCreate(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("update")) {
			return OnUpdate(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("delete")) {
			return OnDelete(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[2].equals("query")) {
			return OnQuery(ctx, session, method, params, iskeepAlive, loginInfo);
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}

	protected FullHttpResponse OnCreate(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
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
		
		if (clazzname.equals("Trailer")) {
			String trailernumber = params.get("trailernumber");
			if (trailernumber == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请上传车牌号");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			try {
				Long total1 = session.createQuery("select count(*) from Truck where datastatus = " + Bean.CREATED
						+ " and trucknumber = '" + trailernumber + "'", Long.class).uniqueResult();
				Long total2 = session.createQuery("select count(*) from Trailer where datastatus = " + Bean.CREATED
						+ " and trailernumber = '" + trailernumber + "'", Long.class).uniqueResult();
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
				jsonObject.put("msg", "车辆数据库异常" + e.getMessage());
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}

		Transaction tx = session.beginTransaction();
		T object = clazz.newInstance();
		Map<String, String> newparams = Utils.paramswritefilter(params, object, role, corporationsid);
		Utils.UpdateFromMap(newparams, object);
		try {
			ValidUtils.ValidationWithExp(object);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		object.createtime(operatesid);
		long sid = MySession.OnSave(object, session, operatesid);
		// Corporation corporation = session.createQuery("from Corporation where
		// sid = "+loginInfo.getUsersid_nm_10(),
		// Corporation.class).uniqueResult();
		// if(corporation == null){
		// tx.rollback();
		// JSONObject jsonObject = new JSONObject();
		// jsonObject.put("status", 4);
		// jsonObject.put("msg", "承运方数据库信息出错");
		// jsonObject.put("content", "");
		// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		// }
		// String oldObject = Utils.getJsonObject(corporation).toString();
		// corporation.AddQuote();
		// Utils.updatetime(operatesid, corporation);
		// MySession.OnUpdate(oldObject, corporation, session, operatesid);
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "创建" + clazzname + "信息成功");
		JSONObject contentjb = new JSONObject();
		contentjb.put("sid", sid);
		jsonObject.put("content", contentjb);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnUpdate(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
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
		String corporationstr = params.get("corporationsid");
		if (corporationstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传corporationsid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}

		Long sid = null, corporationsid_ToUpdate = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "sid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		try {
			corporationsid_ToUpdate = Long.parseLong(corporationstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "corporationsid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 首先根据sid,type和公司sid查询基础信息，是否存在
		T object = session.createQuery("from " + clazzname + " where datastatus = " + Bean.CREATED + " and sid = " + sid
				+ " and corporationsid = " + corporationsid_ToUpdate, clazz).uniqueResult();
		if (object == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", clazzname + "信息不存在");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype >= LoginInfo.TYPE_DRIVER && operateusertype <= LoginInfo.TYPE_BUYER) {// 本公司分配员工无权修改信息
			return NettyUtils.getTokenError(iskeepAlive);
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			if (corporationsid_ToUpdate == corporationsid)
				role = BasicInfo.ROLE_ADMIN; // 可以修改本公司的
			else {
				return NettyUtils.getTokenError(iskeepAlive); // 不可以修改他人公司的
			}
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			if (corporationsid_ToUpdate == corporationsid)
				role = BasicInfo.ROLE_CORPORATION; // 可以修改本公司的
			else {
				return NettyUtils.getTokenError(iskeepAlive); // 不可以修改他人公司的
			}
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {// 政府无权修改信息
			return NettyUtils.getTokenError(iskeepAlive);
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {// 超级管理员可以修改任意信息
			role = BasicInfo.ROLE_SUPER;
			corporationsid = corporationsid_ToUpdate;
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 更新基本信息
		Transaction tx = session.beginTransaction();
		String oldobject = Utils.getJsonObject(object).toString();
		Map<String, String> newparams = Utils.paramswritefilter(params, object, role, corporationsid);
		Utils.UpdateFromMap(newparams, object);
		object.updatetime(operatesid);
		try {
			ValidUtils.ValidationWithExp(object);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnUpdate(oldobject, object, session, operatesid);
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "更新" + clazzname + "成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnDelete(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
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
		String corporationstr = params.get("corporationsid");
		if (corporationstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传corporationsid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long sid = null, corporationsid_ToUpdate = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "sid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		try {
			corporationsid_ToUpdate = Long.parseLong(corporationstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "corporationsid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 首先根据sid,type和公司sid查询基础信息和登录信息，是否存在
		T object = session.createQuery("from " + clazzname + " where datastatus = " + Bean.CREATED + " and sid = " + sid
				+ " and corporationsid = " + corporationsid_ToUpdate, clazz).uniqueResult();
		if (object == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", clazzname + "信息不存在");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			if (corporationsid_ToUpdate == corporationsid)
				role = BasicInfo.ROLE_ADMIN; // 可以删除本公司的
			else {
				return NettyUtils.getTokenError(iskeepAlive); // 不可以删除他人公司的
			}
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以删除本公司信息
			corporationsid = loginInfo.getCorporationsid();
			if (corporationsid_ToUpdate == corporationsid)
				role = BasicInfo.ROLE_CORPORATION; // 可以删除本公司的
			else {
				return NettyUtils.getTokenError(iskeepAlive); // 不可以删除他人公司的
			}
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 删除信息
		// if(!object.Deletable()){
		// JSONObject jsonObject = new JSONObject();
		// jsonObject.put("status", 4);
		// jsonObject.put("msg", "该信息已被引用，暂时不能删除");
		// jsonObject.put("content", "");
		// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		// }
		Transaction tx = session.beginTransaction();
		MySession.OnDelete(object, session, operatesid);
		// Corporation corporation = session.createQuery("from Corporation where
		// sid = "+loginInfo.getUsersid_nm_10(),
		// Corporation.class).uniqueResult();
		// if(corporation == null){
		// tx.rollback();
		// JSONObject jsonObject = new JSONObject();
		// jsonObject.put("status", 4);
		// jsonObject.put("msg", "承运方数据库信息出错");
		// jsonObject.put("content", "");
		// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		// }
		// String oldObject = Utils.getJsonObject(corporation).toString();
		// corporation.SubQuote();
		// Utils.updatetime(operatesid, corporation);
		// MySession.OnUpdate(oldObject, corporation, session, operatesid);
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", clazzname + "信息删除成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnQuery(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
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
		} else if (operateusertype == LoginInfo.TYPE_SUPERADMIN) {// 超级管理员可以修改任意信息
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

		StringBuilder hql = new StringBuilder();
		StringBuilder hql2 = new StringBuilder();
		hql.append("from " + clazzname);
		hql2.append("select count(*) from " + clazzname);
		Map<String, String> newparams = Utils.paramsqueryfilter(params, role, corporationsid, operatesid);
		newparams.put("datastatus", String.valueOf(Bean.CREATED));
		String conditionstr = Utils.getConditionStr(newparams, new GetKeyTypeCallback() {
			@Override
			public int getKeyType(String key) {
				try {
					return Utils.getKeyType(key, clazz.getName());					
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
		Query<T> query = session.createQuery(hql.toString(), clazz);
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
			intpage = Integer.valueOf(page) - 1;
			query.setFirstResult(intpage * intlimit);
		}
		List<T> objects = query.list();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询" + clazzname + "信息成功");
		JSONObject contentJsonObject = new JSONObject();
		if (limit != null) {
			if (intlimit * intpage >= total) {
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("status", 4);
				jsonObject2.put("msg", "第" + (intpage + 1) + "页不存在");
				jsonObject2.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
			}
			contentJsonObject.put("total", total);
			contentJsonObject.put("perpage", limit);
			contentJsonObject.put("current_page", intpage + 1);
			contentJsonObject.put("from", intpage * intlimit + 1);
			if ((intpage + 1) * intlimit > total)
				contentJsonObject.put("to", total);
			else
				contentJsonObject.put("to", (intpage + 1) * intlimit);
		} else {
			contentJsonObject.put("total", total);
			contentJsonObject.put("perpage", total);
			contentJsonObject.put("current_page", 1);
			contentJsonObject.put("from", 1);
			contentJsonObject.put("to", total);
		}
		JSONArray dataArray = new JSONArray();
		for (int i = 0; i < objects.size(); i++)
			dataArray.put(Utils.getJsonObjectWithPremission(objects.get(i), role));
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}
}
