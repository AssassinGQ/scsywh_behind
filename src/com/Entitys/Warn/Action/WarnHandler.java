package com.Entitys.Warn.Action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Daos.QueryBean;
import com.Common.Entitys.BasicInfo;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.PushUtils;
import com.Common.Utils.Utils;
import com.Entitys.User.Entity.LoginInfo;
import com.Entitys.Warn.Dao.WarnDaoImpl;
import com.Entitys.Warn.Entity.Warn;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class WarnHandler {
	private Long operatesid, corporationsid;
	private Integer operateusertype, role;
	private WarnDaoImpl warnDaoImpl;

	public FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (loginInfo == null)
			return NettyUtils.getTokenError(iskeepAlive);
		operateusertype = loginInfo.getType();
		operatesid = loginInfo.getUsersid();
		warnDaoImpl = new WarnDaoImpl(session);
		if (path[1].equals("response")) {// 司机响应异常
			return OnResponse(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("query")) {// 查询异常
			return OnQuery(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("receive")) {// 司机已经接受到异常
			return OnReceive(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}

	protected FullHttpResponse OnReceive(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in Warnhandler-OnResponse");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		String sidstr = params.get("sid");
		if (sidstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请填写异常信息的sid");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "异常信息sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Warn warn = null;
		try {
			warn = warnDaoImpl.getById(sid, true);
			if(warn.getStatus() != Warn.STATUS_PUSHED){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "异常信息状态错误");
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

		String respstr = params.get("respstring");
		if (respstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传司机处理后的描述");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Transaction tx = session.beginTransaction();
		String oldwarn = Utils.getJsonObject(warn).toString();
		warn.setWarndriverresp(respstr);
		warn.setStatus(Warn.STATUS_RECEIVED);
		warn.updatetime(operatesid);
		try {
			warnDaoImpl.update(warn, oldwarn, operatesid);
			tx.commit();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 0);
			retObject.put("msg", "上传成功");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
	}

	protected FullHttpResponse OnResponse(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in Warnhandler-OnResponse");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		String sidstr = params.get("sid");
		if (sidstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请填写异常信息的sid");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "异常信息sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Warn warn = null;
		try {
			warn = warnDaoImpl.getById(sid, true);
			if(warn.getStatus() != Warn.STATUS_RECEIVED){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "异常信息状态错误");
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

		String respstr = params.get("respstring");
		if (respstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传司机处理后的描述");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Transaction tx = session.beginTransaction();
		String oldwarn = Utils.getJsonObject(warn).toString();
		warn.setWarndriverresp(respstr);
		warn.setStatus(Warn.STATUS_HANDLED);
		warn.updatetime(operatesid);
		try {
			warnDaoImpl.update(warn, oldwarn, operatesid);
			tx.commit();
			JSONObject pushObject = new JSONObject();
			pushObject.put("type", "23");
			pushObject.put("sid", warn.getSid());
			String pushcontent = pushObject.toString();
			if(!PushUtils.Push2Admin(pushcontent, session, warn.getCorporationsid(), null, null))
				System.err.println("司机对异常进行反馈后推送给管理员失败");
			JSONObject retObject = new JSONObject();
			retObject.put("status", 0);
			retObject.put("msg", "上传成功");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
	}

	protected FullHttpResponse OnQuery(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in Warnhandler-OnQuery");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if (operateusertype == LoginInfo.TYPE_ADMIN) {
			role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {
			role = BasicInfo.ROLE_CORPORATION;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {
			role = BasicInfo.ROLE_GOVERNMENT;
		} else if (operateusertype == LoginInfo.TYPE_MANUFACTURER) {
			role = BasicInfo.ROLE_MANUFACTURER;
		} else if (operateusertype == LoginInfo.TYPE_SELLER || operateusertype == LoginInfo.TYPE_DRIVER) {
			role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		StringBuilder hql = new StringBuilder();
		StringBuilder hql2 = new StringBuilder();
		hql.append("from Warn");
		hql2.append("select count(*) from Warn");
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
			queryList.add(new QueryBean(Warn.class.getName(), "warn", field, value, valuetype));
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
		List<Warn> result = null;
		Long total = null;
		try {
			result = warnDaoImpl.getListBy(queryList, true, intlimit, intpage);
			total = warnDaoImpl.selectCount(queryList, true);
		} catch (Exception e) {
			JSONObject jsonObject2 = new JSONObject();
			jsonObject2.put("status", 4);
			jsonObject2.put("msg", e.getMessage());
			jsonObject2.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询异常信息成功");
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
			JSONObject tmpjsonObject = Utils.getJsonObject(result.get(i));
			dataArray.put(tmpjsonObject);
		}
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}
	
//	@SuppressWarnings("unchecked")
//	private <T> void json2list(String jstr, List<T> list) {
//		if (list == null)
//			list = new ArrayList<T>();
//		else
//			list.clear();
//		if (jstr == null)
//			return;
//		try {
//			JSONArray jsonArray = new JSONArray(jstr);
//			for (int i = 0; i < jsonArray.length(); i++) {
//				T tmpobject = (T) jsonArray.get(i);
//				list.add(tmpobject);
//			}
//			return;
//		} catch (JSONException e) {
//			list.clear();
//			return;
//		} catch (Exception e) {
//			list.clear();
//			return;
//		}
//	}
}
