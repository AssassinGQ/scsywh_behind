package com.RequestHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Entitys.Bean;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
import com.Entitys.Statistics.OrderMonthStatistics;
import com.Entitys.Statistics.OrderYearStatistics;
import com.Entitys.Statistics.StatisticsStatic;
import com.Entitys.Statistics.WarnMonthStatistics;
import com.Entitys.Statistics.WarnYearStatistics;
import com.Entitys.User.Entity.LoginInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class StatisticsHandler {
	private Long operatesid, corporationsid;
	private Integer operateusertype;

	public FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (loginInfo == null)
			return NettyUtils.getTokenError(iskeepAlive);
		operateusertype = loginInfo.getType();
		operatesid = loginInfo.getUsersid();
		corporationsid = null;
		if (path[1].equals("query_ordermonth")) {
			return OnQueryOrderMonth(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("query_orderyear")) {
			return OnQueryOrderYear(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("query_warnmonth")) {
			return OnQueryWarnMonth(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("query_warnyear")) {
			return OnQueryWarnYear(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void json2list(String jstr, List<T> list) {
		if (list == null)
			list = new ArrayList<T>();
		else
			list.clear();
		if (jstr == null)
			return;
		try {
			JSONArray jsonArray = new JSONArray(jstr);
			for (int i = 0; i < jsonArray.length(); i++) {
				T tmpobject = (T) jsonArray.get(i);
				list.add(tmpobject);
			}
			return;
		} catch (JSONException e) {
			list.clear();
			return;
		} catch (Exception e) {
			list.clear();
			return;
		}
	}

	// 托运方查询自己某几个月的订单统计
	// 司机查询自己某几个月的订单统计
	// 管理员或者承运方或政府查询某些承运方某几个月的订单统计（管理员或承运方只能查询本承运方的）
	// 管理员或者承运方或者政府查询承运方下某些托运方或者某些司机或者某些车辆某几个月的订单统计（管理员或承运方只能查询本承运方的）
	protected FullHttpResponse OnQueryOrderMonth(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in StatisticsHandler-OnQueryOrderMonth");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		Integer objecttype = null;
		List<Long> corporationsids = new ArrayList<Long>();
		List<Long> objectsids = new ArrayList<Long>();
		List<String> years = new ArrayList<String>();
		List<String> months = new ArrayList<String>();
		String yearstr = params.get("year");
		String monthstr = params.get("month");
		json2list(yearstr, years);
		json2list(monthstr, months);
		if (operateusertype == LoginInfo.TYPE_ADMIN || operateusertype == LoginInfo.TYPE_CORPORATION) {
			// role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
			corporationsids.add(corporationsid);
			String objecttypestr = params.get("objecttype");
			if (objecttypestr == null)
				objecttype = StatisticsStatic.OBJTECTYPE_CORPORATION;
			else {
				try {
					objecttype = Integer.parseInt(objecttypestr);
					if (objecttype < StatisticsStatic.OBJTECTYPE_CORPORATION
							|| objecttype > StatisticsStatic.OBJTECTYPE_TRUCK)
						throw new Exception();
				} catch (Exception e) {
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("status", 4);
					jsonObject2.put("msg", "objecttype格式不正确");
					jsonObject2.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
				}
			}
			if (objecttype == StatisticsStatic.OBJTECTYPE_CORPORATION)
				objectsids.add(corporationsid);
			else {
				String objectsidstr = params.get("objectsid");
				json2list(objectsidstr, objectsids);
			}
		} else if (operateusertype == LoginInfo.TYPE_SELLER) {
			// role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
			corporationsids.add(corporationsid);
			objecttype = StatisticsStatic.OBJTECTYPE_SELLER;
			objectsids.add(operatesid);
		} else if (operateusertype == LoginInfo.TYPE_DRIVER) {
			// role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
			corporationsids.add(corporationsid);
			objecttype = StatisticsStatic.OBJTECTYPE_DRIVER;
			objectsids.add(operatesid);
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {
			// role = BasicInfo.ROLE_GOVERNMENT;
			String objecttypestr = params.get("objecttype");
			String objectsidstr = params.get("objectsid");
			String corporationsidstr = params.get("corporationsid");
			if (objecttypestr != null) {
				try {
					objecttype = Integer.parseInt(objecttypestr);
				} catch (Exception e) {
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("status", 4);
					jsonObject2.put("msg", "objecttype格式不正确");
					jsonObject2.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
				}
				json2list(objectsidstr, objectsids);
			}
			json2list(corporationsidstr, corporationsids);
		} else if (operateusertype == LoginInfo.TYPE_MANUFACTURER) {
			return NettyUtils.getTokenError(iskeepAlive);
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		StringBuilder hql = new StringBuilder();
		StringBuilder hql2 = new StringBuilder();
		hql.append("from OrderMonthStatistics where datastatus = " + Bean.CREATED);
		hql2.append("select count(*) from OrderMonthStatistics where datastatus = " + Bean.CREATED);
		if (objecttype != null) {
			hql.append(" and objecttype = " + objecttype);
			hql2.append(" and objecttype = " + objecttype);
		}
		if (corporationsids.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < corporationsids.size(); i++) {
				if (inisfirst) {
					hql.append(" corporationsid = " + corporationsids.get(i));
					hql2.append(" corporationsid = " + corporationsids.get(i));
					inisfirst = false;
				} else {
					hql.append(" or corporationsid = " + corporationsids.get(i));
					hql2.append(" or corporationsid = " + corporationsids.get(i));
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		if (objectsids.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < objectsids.size(); i++) {
				if (inisfirst) {
					hql.append(" objectsid = " + objectsids.get(i));
					hql2.append(" objectsid = " + objectsids.get(i));
					inisfirst = false;
				} else {
					hql.append(" or objectsid = " + objectsids.get(i));
					hql2.append(" or objectsid = " + objectsids.get(i));
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		if (years.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < years.size(); i++) {
				if (inisfirst) {
					hql.append(" year = '" + years.get(i) + "'");
					hql2.append(" year = '" + years.get(i) + "'");
					inisfirst = false;
				} else {
					hql.append(" or year = '" + years.get(i) + "'");
					hql2.append(" or year = '" + years.get(i) + "'");
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		if (months.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < months.size(); i++) {
				if (inisfirst) {
					hql.append(" month = '" + months.get(i) + "'");
					hql2.append(" month = '" + months.get(i) + "'");
					inisfirst = false;
				} else {
					hql.append(" or month = '" + months.get(i) + "'");
					hql2.append(" or month = '" + months.get(i) + "'");
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		Query<OrderMonthStatistics> query = session.createQuery(hql.toString(), OrderMonthStatistics.class);
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
		List<OrderMonthStatistics> omss = query.list();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询成功");
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
		for (int i = 0; i < omss.size(); i++)
			dataArray.put(Utils.getJsonObject(omss.get(i)));
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnQueryOrderYear(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in StatisticsHandler-OnQueryOrderYear");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		Integer objecttype = null;
		List<Long> corporationsids = new ArrayList<Long>();
		List<Long> objectsids = new ArrayList<Long>();
		List<String> years = new ArrayList<String>();
		String yearstr = params.get("year");
		json2list(yearstr, years);
		if (operateusertype == LoginInfo.TYPE_ADMIN || operateusertype == LoginInfo.TYPE_CORPORATION) {
			// role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
			corporationsids.add(corporationsid);
			String objecttypestr = params.get("objecttype");
			if (objecttypestr == null)
				objecttype = StatisticsStatic.OBJTECTYPE_CORPORATION;
			else {
				try {
					objecttype = Integer.parseInt(objecttypestr);
					if (objecttype < StatisticsStatic.OBJTECTYPE_CORPORATION
							|| objecttype > StatisticsStatic.OBJTECTYPE_TRUCK)
						throw new Exception();
				} catch (Exception e) {
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("status", 4);
					jsonObject2.put("msg", "objecttype格式不正确");
					jsonObject2.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
				}
			}
			if (objecttype == StatisticsStatic.OBJTECTYPE_CORPORATION)
				objectsids.add(corporationsid);
			else {
				String objectsidstr = params.get("objectsid");
				json2list(objectsidstr, objectsids);
			}
		} else if (operateusertype == LoginInfo.TYPE_SELLER) {
			// role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
			corporationsids.add(corporationsid);
			objecttype = StatisticsStatic.OBJTECTYPE_SELLER;
			objectsids.add(operatesid);
		} else if (operateusertype == LoginInfo.TYPE_DRIVER) {
			// role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
			corporationsids.add(corporationsid);
			objecttype = StatisticsStatic.OBJTECTYPE_DRIVER;
			objectsids.add(operatesid);
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {
			// role = BasicInfo.ROLE_GOVERNMENT;
			String objecttypestr = params.get("objecttype");
			String objectsidstr = params.get("objectsid");
			String corporationsidstr = params.get("corporationsid");
			if (objecttypestr != null) {
				try {
					objecttype = Integer.parseInt(objecttypestr);
				} catch (Exception e) {
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("status", 4);
					jsonObject2.put("msg", "objecttype格式不正确");
					jsonObject2.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
				}
				json2list(objectsidstr, objectsids);
			}
			json2list(corporationsidstr, corporationsids);
		} else if (operateusertype == LoginInfo.TYPE_MANUFACTURER) {
			return NettyUtils.getTokenError(iskeepAlive);
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		StringBuilder hql = new StringBuilder();
		StringBuilder hql2 = new StringBuilder();
		hql.append("from OrderYearStatistics where datastatus = " + Bean.CREATED);
		hql2.append("select count(*) from OrderYearStatistics where datastatus = " + Bean.CREATED);
		if (objecttype != null) {
			hql.append(" and objecttype = " + objecttype);
			hql2.append(" and objecttype = " + objecttype);
		}
		if (corporationsids.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < corporationsids.size(); i++) {
				if (inisfirst) {
					hql.append(" corporationsid = " + corporationsids.get(i));
					hql2.append(" corporationsid = " + corporationsids.get(i));
					inisfirst = false;
				} else {
					hql.append(" or corporationsid = " + corporationsids.get(i));
					hql2.append(" or corporationsid = " + corporationsids.get(i));
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		if (objectsids.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < objectsids.size(); i++) {
				if (inisfirst) {
					hql.append(" objectsid = " + objectsids.get(i));
					hql2.append(" objectsid = " + objectsids.get(i));
					inisfirst = false;
				} else {
					hql.append(" or objectsid = " + objectsids.get(i));
					hql2.append(" or objectsid = " + objectsids.get(i));
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		if (years.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < years.size(); i++) {
				if (inisfirst) {
					hql.append(" year = '" + years.get(i) + "'");
					hql2.append(" year = '" + years.get(i) + "'");
					inisfirst = false;
				} else {
					hql.append(" or year = '" + years.get(i) + "'");
					hql2.append(" or year = '" + years.get(i) + "'");
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		Query<OrderYearStatistics> query = session.createQuery(hql.toString(), OrderYearStatistics.class);
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
		List<OrderYearStatistics> oyss = query.list();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询成功");
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
		for (int i = 0; i < oyss.size(); i++)
			dataArray.put(Utils.getJsonObject(oyss.get(i)));
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnQueryWarnMonth(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in StatisticsHandler-OnQueryWarnMonth");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		Integer objecttype = null;
		List<Long> corporationsids = new ArrayList<Long>();
		List<Long> objectsids = new ArrayList<Long>();
		List<String> years = new ArrayList<String>();
		List<String> months = new ArrayList<String>();
		String yearstr = params.get("year");
		String monthstr = params.get("month");
		json2list(yearstr, years);
		json2list(monthstr, months);
		if (operateusertype == LoginInfo.TYPE_ADMIN || operateusertype == LoginInfo.TYPE_CORPORATION) {
			// role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
			corporationsids.add(corporationsid);
			String objecttypestr = params.get("objecttype");
			if (objecttypestr == null)
				objecttype = StatisticsStatic.OBJTECTYPE_CORPORATION;
			else {
				try {
					objecttype = Integer.parseInt(objecttypestr);
					if (objecttype < StatisticsStatic.OBJTECTYPE_CORPORATION
							|| objecttype > StatisticsStatic.OBJTECTYPE_TRUCK)
						throw new Exception();
				} catch (Exception e) {
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("status", 4);
					jsonObject2.put("msg", "objecttype格式不正确");
					jsonObject2.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
				}
			}
			if (objecttype == StatisticsStatic.OBJTECTYPE_CORPORATION)
				objectsids.add(corporationsid);
			else {
				String objectsidstr = params.get("objectsid");
				json2list(objectsidstr, objectsids);
			}
		} else if (operateusertype == LoginInfo.TYPE_SELLER) {
			// role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
			corporationsids.add(corporationsid);
			objecttype = StatisticsStatic.OBJTECTYPE_SELLER;
			objectsids.add(operatesid);
		} else if (operateusertype == LoginInfo.TYPE_DRIVER) {
			// role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
			corporationsids.add(corporationsid);
			objecttype = StatisticsStatic.OBJTECTYPE_DRIVER;
			objectsids.add(operatesid);
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {
			// role = BasicInfo.ROLE_GOVERNMENT;
			String objecttypestr = params.get("objecttype");
			String objectsidstr = params.get("objectsid");
			String corporationsidstr = params.get("corporationsid");
			if (objecttypestr != null) {
				try {
					objecttype = Integer.parseInt(objecttypestr);
				} catch (Exception e) {
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("status", 4);
					jsonObject2.put("msg", "objecttype格式不正确");
					jsonObject2.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
				}
				json2list(objectsidstr, objectsids);
			}
			json2list(corporationsidstr, corporationsids);
		} else if (operateusertype == LoginInfo.TYPE_MANUFACTURER) {
			return NettyUtils.getTokenError(iskeepAlive);
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		StringBuilder hql = new StringBuilder();
		StringBuilder hql2 = new StringBuilder();
		hql.append("from WarnMonthStatistics where datastatus = " + Bean.CREATED);
		hql2.append("select count(*) from WarnMonthStatistics where datastatus = " + Bean.CREATED);
		if (objecttype != null) {
			hql.append(" and objecttype = " + objecttype);
			hql2.append(" and objecttype = " + objecttype);
		}
		if (corporationsids.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < corporationsids.size(); i++) {
				if (inisfirst) {
					hql.append(" corporationsid = " + corporationsids.get(i));
					hql2.append(" corporationsid = " + corporationsids.get(i));
					inisfirst = false;
				} else {
					hql.append(" or corporationsid = " + corporationsids.get(i));
					hql2.append(" or corporationsid = " + corporationsids.get(i));
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		if (objectsids.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < objectsids.size(); i++) {
				if (inisfirst) {
					hql.append(" objectsid = " + objectsids.get(i));
					hql2.append(" objectsid = " + objectsids.get(i));
					inisfirst = false;
				} else {
					hql.append(" or objectsid = " + objectsids.get(i));
					hql2.append(" or objectsid = " + objectsids.get(i));
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		if (years.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < years.size(); i++) {
				if (inisfirst) {
					hql.append(" year = '" + years.get(i) + "'");
					hql2.append(" year = '" + years.get(i) + "'");
					inisfirst = false;
				} else {
					hql.append(" or year = '" + years.get(i) + "'");
					hql2.append(" or year = '" + years.get(i) + "'");
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		if (months.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < months.size(); i++) {
				if (inisfirst) {
					hql.append(" month = '" + months.get(i) + "'");
					hql2.append(" month = '" + months.get(i) + "'");
					inisfirst = false;
				} else {
					hql.append(" or month = '" + months.get(i) + "'");
					hql2.append(" or month = '" + months.get(i) + "'");
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		Query<WarnMonthStatistics> query = session.createQuery(hql.toString(), WarnMonthStatistics.class);
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
		List<WarnMonthStatistics> wmss = query.list();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询成功");
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
		for (int i = 0; i < wmss.size(); i++)
			dataArray.put(Utils.getJsonObject(wmss.get(i)));
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnQueryWarnYear(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in StatisticsHandler-OnQueryWarnYear");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		Integer objecttype = null;
		List<Long> corporationsids = new ArrayList<Long>();
		List<Long> objectsids = new ArrayList<Long>();
		List<String> years = new ArrayList<String>();
		String yearstr = params.get("year");
		json2list(yearstr, years);
		if (operateusertype == LoginInfo.TYPE_ADMIN || operateusertype == LoginInfo.TYPE_CORPORATION) {
			// role = BasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
			corporationsids.add(corporationsid);
			String objecttypestr = params.get("objecttype");
			if (objecttypestr == null)
				objecttype = StatisticsStatic.OBJTECTYPE_CORPORATION;
			else {
				try {
					objecttype = Integer.parseInt(objecttypestr);
					if (objecttype < StatisticsStatic.OBJTECTYPE_CORPORATION
							|| objecttype > StatisticsStatic.OBJTECTYPE_TRUCK)
						throw new Exception();
				} catch (Exception e) {
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("status", 4);
					jsonObject2.put("msg", "objecttype格式不正确");
					jsonObject2.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
				}
			}
			if (objecttype == StatisticsStatic.OBJTECTYPE_CORPORATION)
				objectsids.add(corporationsid);
			else {
				String objectsidstr = params.get("objectsid");
				json2list(objectsidstr, objectsids);
			}
		} else if (operateusertype == LoginInfo.TYPE_SELLER) {
			// role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
			corporationsids.add(corporationsid);
			objecttype = StatisticsStatic.OBJTECTYPE_SELLER;
			objectsids.add(operatesid);
		} else if (operateusertype == LoginInfo.TYPE_DRIVER) {
			// role = BasicInfo.ROLE_USER;
			corporationsid = loginInfo.getCorporationsid();
			corporationsids.add(corporationsid);
			objecttype = StatisticsStatic.OBJTECTYPE_DRIVER;
			objectsids.add(operatesid);
		} else if (operateusertype == LoginInfo.TYPE_GOVERNMENT) {
			// role = BasicInfo.ROLE_GOVERNMENT;
			String objecttypestr = params.get("objecttype");
			String objectsidstr = params.get("objectsid");
			String corporationsidstr = params.get("corporationsid");
			if (objecttypestr != null) {
				try {
					objecttype = Integer.parseInt(objecttypestr);
				} catch (Exception e) {
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("status", 4);
					jsonObject2.put("msg", "objecttype格式不正确");
					jsonObject2.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
				}
				json2list(objectsidstr, objectsids);
			}
			json2list(corporationsidstr, corporationsids);
		} else if (operateusertype == LoginInfo.TYPE_MANUFACTURER) {
			return NettyUtils.getTokenError(iskeepAlive);
		} else {
			return NettyUtils.getTokenError(iskeepAlive);
		}

		StringBuilder hql = new StringBuilder();
		StringBuilder hql2 = new StringBuilder();
		hql.append("from WarnYearStatistics where datastatus = " + Bean.CREATED);
		hql2.append("select count(*) from WarnYearStatistics where datastatus = " + Bean.CREATED);
		if (objecttype != null) {
			hql.append(" and objecttype = " + objecttype);
			hql2.append(" and objecttype = " + objecttype);
		}
		if (corporationsids.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < corporationsids.size(); i++) {
				if (inisfirst) {
					hql.append(" corporationsid = " + corporationsids.get(i));
					hql2.append(" corporationsid = " + corporationsids.get(i));
					inisfirst = false;
				} else {
					hql.append(" or corporationsid = " + corporationsids.get(i));
					hql2.append(" or corporationsid = " + corporationsids.get(i));
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		if (objectsids.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < objectsids.size(); i++) {
				if (inisfirst) {
					hql.append(" objectsid = " + objectsids.get(i));
					hql2.append(" objectsid = " + objectsids.get(i));
					inisfirst = false;
				} else {
					hql.append(" or objectsid = " + objectsids.get(i));
					hql2.append(" or objectsid = " + objectsids.get(i));
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		if (years.size() > 0) {
			hql.append(" and (");
			hql2.append(" and (");
			boolean inisfirst = true;
			for (int i = 0; i < years.size(); i++) {
				if (inisfirst) {
					hql.append(" year = '" + years.get(i) + "'");
					hql2.append(" year = '" + years.get(i) + "'");
					inisfirst = false;
				} else {
					hql.append(" or year = '" + years.get(i) + "'");
					hql2.append(" or year = '" + years.get(i) + "'");
				}
			}
			hql.append(")");
			hql2.append(")");
		}
		Query<WarnYearStatistics> query = session.createQuery(hql.toString(), WarnYearStatistics.class);
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
		List<WarnYearStatistics> wyss = query.list();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询成功");
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
		for (int i = 0; i < wyss.size(); i++)
			dataArray.put(Utils.getJsonObject(wyss.get(i)));
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}
}
