package com.Entitys.Seller.Action;

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
import com.Common.Entitys.Bean;
import com.Common.Entitys.LoginableBasicInfo;
import com.Common.Utils.MD5Utils;
import com.Common.Utils.MySession;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
import com.Common.Utils.ValidUtils;
import com.Common.Utils.WebSocketUtils;
import com.Entitys.Seller.Dao.SellerDaoImpl;
import com.Entitys.Seller.Entity.Seller;
import com.Entitys.User.Entity.LoginInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class SellerHandler {
	private Long operatesid, corporationsid;
	private Integer operateusertype, role;
	private SellerDaoImpl sellerDaoImpl;

	public FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
		System.err.println("in SellerHandler");
		if (loginInfo == null)
			return NettyUtils.getTokenError(iskeepAlive);
		operateusertype = loginInfo.getType();
		operatesid = loginInfo.getUsersid();
		sellerDaoImpl = new SellerDaoImpl(session);
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
			role = LoginableBasicInfo.ROLE_ADMIN;
			corporationsid = loginInfo.getCorporationsid();
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以创建本公司信息
			role = LoginableBasicInfo.ROLE_CORPORATION;
			corporationsid = loginInfo.getCorporationsid();
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}

		String phone = params.get("phone");
		if (phone != null) {
			if (!Utils.isMobileNum(phone)) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "手机号码格式不对");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			Long total = session.createQuery("select count(*) from LoginInfo where datastatus = " + Bean.CREATED
					+ " and phone = '" + phone + "'", Long.class).uniqueResult();
			if (total > 1) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，请联系系统管理员");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if (total == 1) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "该手机号已被使用");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}
		String username = params.get("username");
		if (username != null) {
			Long total = session.createQuery("select count(*) from LoginInfo where datastatus = " + Bean.CREATED
					+ " and username = '" + username + "'", Long.class).uniqueResult();
			if (total > 1) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，请联系系统管理员");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if (total == 1) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "该用户名已被使用");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else {
			username = String.valueOf(Utils.getCurrenttimeMills());
		}
		String password = params.get("password");
		if (password != null) {
			if (!Utils.isPassword(password)) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "密码长度至少8位，且必须包括数字和大小写字母");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else {
			password = Utils.getPassword();
		}

		Transaction tx = session.beginTransaction();
		// 创建基本信息
		Seller seller = new Seller();
		params.put("username", username);
		if (phone != null)
			params.put("phone", phone);
		Map<String, String> newparams = Utils.paramswritefilter(params, seller, role, corporationsid);
		Utils.UpdateFromMap(newparams, seller);
		seller.createtime(operatesid);
		Long sid = null;
		try {
			sid = sellerDaoImpl.insert(seller, operatesid);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		// 创建相应的登录信息
		String salt = Utils.getRandomString(10);
		String pswDB = MD5Utils.MD5(password, salt);
		LoginInfo newlogininfo = new LoginInfo();
		newlogininfo.setUsersid(sid.longValue());
		newlogininfo.setCorporationsid(corporationsid);
		newlogininfo.setStatus(1);
		newlogininfo.setUsername(username);
		newlogininfo.setSalt(salt);
		newlogininfo.setPassword(pswDB);
		if (phone != null)
			newlogininfo.setPhone(phone);
		newlogininfo.setType(LoginInfo.TYPE_SELLER);
		newlogininfo.createtime(operatesid);
		try {
			ValidUtils.ValidationWithExp(newlogininfo);
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnSave(newlogininfo, session, operatesid);
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "托运方创建成功,请记下账号密码，防止遗失");
		JSONObject contentjb = new JSONObject();
		contentjb.put("sid", sid);
		contentjb.put("username", newlogininfo.getUsername());
		contentjb.put("password", password);
		jsonObject.put("content", contentjb);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnUpdate(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		// 角色解析，所属公司sid确定（只能修改所属公司的信息，当然super可以指定任意公司，政府没有修改权限）
		Long updatesid = null;
		if (operateusertype == LoginInfo.TYPE_SELLER) {// 本公司分配员工无权修改信息
			corporationsid = loginInfo.getCorporationsid();
			role = LoginableBasicInfo.ROLE_USER;
			updatesid = operatesid;
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = LoginableBasicInfo.ROLE_ADMIN; // 可以修改本公司的
			String sidstr = params.get("sid");
			if (sidstr == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请上传sid");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			try {
				updatesid = Long.parseLong(sidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "sid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = LoginableBasicInfo.ROLE_CORPORATION; // 可以修改本公司的
			String sidstr = params.get("sid");
			if (sidstr == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请上传sid");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			try {
				updatesid = Long.parseLong(sidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "sid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		Seller seller = null;
		LoginInfo sellerLoginInfo = null;
		try {
			List<QueryBean> sellerQueryBean = new ArrayList<QueryBean>();
			sellerQueryBean.add(new QueryBean(Seller.class.getName(), "seller", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
			sellerQueryBean.add(new QueryBean(Seller.class.getName(), "seller", "sid", updatesid, QueryBean.TYPE_EQUAL));
			List<Seller> sellers = sellerDaoImpl.getListBy(sellerQueryBean, true);
			if (sellers.size() == 0) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此托运方信息");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if (sellers.size() > 1) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，托运方主键重复");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			seller = sellers.get(0);
			sellerLoginInfo = session
					.createQuery(
							"from LoginInfo where datastatus = " + Bean.CREATED + " and usersid = " + updatesid
									+ " and type = " + LoginInfo.TYPE_SELLER + " and corporationsid = " + corporationsid,
							LoginInfo.class)
					.uniqueResult();
			if (sellerLoginInfo == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此托运方信息");
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

		String phone = params.get("phone");
		if (phone != null) {
			if (!Utils.isMobileNum(phone)) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "手机号码格式不对");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			Long total = session.createQuery("select count(*) from LoginInfo where datastatus = " + Bean.CREATED
					+ " and phone = '" + phone + "' and usersid <> " + updatesid, Long.class).uniqueResult();
			if (total > 1) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，请联系系统管理员");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if (total == 1) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "该手机号已被使用");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}
		String username = params.get("username");
		if (username != null) {
			Long total = session
					.createQuery("select count(*) from LoginInfo where datastatus = " + Bean.CREATED
							+ " and username = '" + username + "' and usersid <> " + updatesid, Long.class)
					.uniqueResult();
			if (total > 1) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，请联系系统管理员");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if (total == 1) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "该用户名已被使用");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}
		// String password = params.get("password");
		// if(password == null){
		// JSONObject jsonObject = new JSONObject();
		// jsonObject.put("status", 4);
		// jsonObject.put("msg", "请输入原密码");
		// jsonObject.put("content", "");
		// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		// }
		// String oldsalt = objectloginInfo.getSalt_te_10();
		// String oldusername = objectloginInfo.getUsername();
		// String oldpswDB = objectloginInfo.getPassword_tm_100();
		// if(!oldpswDB.equals(MD5Utils.MD5(oldusername, password, oldsalt))){
		// JSONObject jsonObject = new JSONObject();
		// jsonObject.put("status", 4);
		// jsonObject.put("msg", "原密码不正确");
		// jsonObject.put("content", "");
		// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		// }
		// String newpassword = params.get("newpassword");
		// if (newpassword != null) {
		// if (!Utils.isPassword(newpassword)) {
		// JSONObject jsonObject = new JSONObject();
		// jsonObject.put("status", 4);
		// jsonObject.put("msg", "密码长度至少8位，且必须包括数字和大小写字母");
		// jsonObject.put("content", "");
		// return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		// }
		// }
		String password = params.get("password");
		if (password != null) {
			if (!Utils.isPassword(password)) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "密码长度至少8位，且必须包括数字和大小写字母");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}
		Transaction tx = session.beginTransaction();
		// 更新登录信息
		String oldlogininfo = Utils.getJsonObject(sellerLoginInfo).toString();
		if (username != null) {
			sellerLoginInfo.setUsername(username);
		}
		if (password != null) {
			String salt = Utils.getRandomString(10);
			String pswDB = MD5Utils.MD5(password, salt);
			sellerLoginInfo.setPassword(pswDB);
			sellerLoginInfo.setSalt(salt);
		}
		// if (username != null && newpassword == null){
		// String salt = Utils.getRandomString(10);
		// String pswDB = MD5Utils.MD5(username, password, salt);
		// objectloginInfo.setPassword(pswDB);
		// objectloginInfo.setSalt_te_10(salt);
		// objectloginInfo.setUsername(username);
		// }
		// if (newpassword != null && username == null){
		// String salt = Utils.getRandomString(10);
		// String pswDB = MD5Utils.MD5(objectloginInfo.getUsername(),
		// newpassword, salt);
		// objectloginInfo.setPassword(pswDB);
		// objectloginInfo.setSalt_te_10(salt);
		// }
		// if(newpassword != null && username != null){
		// String salt = Utils.getRandomString(10);
		// String pswDB = MD5Utils.MD5(username, newpassword, salt);
		// objectloginInfo.setPassword(pswDB);
		// objectloginInfo.setSalt_te_10(salt);
		// objectloginInfo.setUsername(username);
		// }
		if (phone != null)
			sellerLoginInfo.setPhone(phone);
		sellerLoginInfo.updatetime(operatesid);
		try {
			ValidUtils.ValidationWithExp(sellerLoginInfo);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnUpdate(oldlogininfo, sellerLoginInfo, session, operatesid);
		// 更新基础信息
		String oldseller = Utils.getJsonObject(seller).toString();
		if (username != null)
			params.put("username", username);
		if (phone != null)
			params.put("phone", phone);
		Map<String, String> newparams = Utils.paramswritefilter(params, seller, role, corporationsid);
		Utils.UpdateFromMap(newparams, seller);
		seller.updatetime(operatesid);
		try {
			sellerDaoImpl.update(seller, oldseller, operatesid);
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
		jsonObject.put("msg", "托运方信息更新成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnDelete(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		Long updatesid = null;
		if (operateusertype == LoginInfo.TYPE_SELLER) {// 本公司分配员工无权修改信息
			corporationsid = loginInfo.getCorporationsid();
			role = LoginableBasicInfo.ROLE_USER;
			updatesid = operatesid;
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = LoginableBasicInfo.ROLE_ADMIN; // 可以修改本公司的
			String sidstr = params.get("sid");
			if (sidstr == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请上传sid");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			try {
				updatesid = Long.parseLong(sidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "sid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = LoginableBasicInfo.ROLE_CORPORATION; // 可以修改本公司的
			String sidstr = params.get("sid");
			if (sidstr == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请上传sid");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			try {
				updatesid = Long.parseLong(sidstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "sid格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		// 获取输入并验证
		Seller seller = null;
		LoginInfo sellerLoginInfo = null;
		try {
			List<QueryBean> sellerQueryBean = new ArrayList<QueryBean>();
			sellerQueryBean.add(new QueryBean(Seller.class.getName(), "seller", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
			sellerQueryBean.add(new QueryBean(Seller.class.getName(), "seller", "sid", updatesid, QueryBean.TYPE_EQUAL));
			List<Seller> sellers = sellerDaoImpl.getListBy(sellerQueryBean, true);
			if (sellers.size() == 0) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此托运方信息");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if (sellers.size() > 1) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "数据库出错，托运方主键重复");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			seller = sellers.get(0);
			sellerLoginInfo = session
					.createQuery(
							"from LoginInfo where datastatus = " + Bean.CREATED + " and usersid = " + updatesid
									+ " and type = " + LoginInfo.TYPE_SELLER + " and corporationsid = " + corporationsid,
							LoginInfo.class)
					.uniqueResult();
			if (sellerLoginInfo == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此托运方信息");
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

		// 删除信息
		Transaction tx = session.beginTransaction();
		// 删除关联信息
		{
			if (sellerLoginInfo.getOnlinetype() != null
					&& sellerLoginInfo.getOnlinetype() == LoginInfo.ONLINETYPE_WEB
					&& sellerLoginInfo.getToken() != null)
				WebSocketUtils.RemoveCtx(sellerLoginInfo.getToken());
			// 暂时不删除统计信息
			// Integer objecttype = null;
			// Long objectsid = null;
			// if (object instanceof Driver) {
			// Driver driver = (Driver) object;
			// objecttype = StatisticsStatic.OBJTECTYPE_DRIVER;
			// objectsid = driver.getSid_nm_10();
			// }
			// if (object instanceof Seller) {
			// Seller seller = (Seller) object;
			// objecttype = StatisticsStatic.OBJTECTYPE_SELLER;
			// objectsid = seller.getSid_nm_10();
			// }
			// if (objecttype != null && objectsid != null) {
			// List<OrderMonthStatistics> orderMonthStatistics_delete =
			// session.createQuery(
			// "from OrderMonthStatistics where datastatus = " + Bean.CREATED +
			// " and objecttype = "
			// + objecttype + " and objectsid = " + objectsid,
			// OrderMonthStatistics.class).list();
			// List<OrderYearStatistics> orderYearStatistics_delete =
			// session.createQuery(
			// "from OrderYearStatistics where datastatus = " + Bean.CREATED + "
			// and objecttype = "
			// + objecttype + " and objectsid = " + objectsid,
			// OrderYearStatistics.class).list();
			// List<WarnMonthStatistics> WarnMonthStatistics_delete =
			// session.createQuery(
			// "from WarnMonthStatistics where datastatus = " + Bean.CREATED + "
			// and objecttype = "
			// + objecttype + " and objectsid = " + objectsid,
			// WarnMonthStatistics.class).list();
			// List<WarnYearStatistics> WarnYearStatistics_delete = session
			// .createQuery("from WarnYearStatistics where datastatus = " +
			// Bean.CREATED + " and objecttype = "
			// + objecttype + " and objectsid = " + objectsid,
			// WarnYearStatistics.class)
			// .list();
			// for (int i = 0; i < orderMonthStatistics_delete.size(); i++)
			// MySession.OnDelete(orderMonthStatistics_delete.get(i), session,
			// operatesid);
			// for (int i = 0; i < orderYearStatistics_delete.size(); i++)
			// MySession.OnDelete(orderYearStatistics_delete.get(i), session,
			// operatesid);
			// for (int i = 0; i < WarnMonthStatistics_delete.size(); i++)
			// MySession.OnDelete(WarnMonthStatistics_delete.get(i), session,
			// operatesid);
			// for (int i = 0; i < WarnYearStatistics_delete.size(); i++)
			// MySession.OnDelete(WarnYearStatistics_delete.get(i), session,
			// operatesid);
			// }
		}
		try {
			sellerDaoImpl.delete(seller, operatesid);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnDelete(sellerLoginInfo, session, operatesid);
		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "托运方信息删除成功");
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
		if (operateusertype == LoginInfo.TYPE_SELLER || operateusertype == LoginInfo.TYPE_BUYER) {
			corporationsid = loginInfo.getCorporationsid();
			role = BasicInfo.ROLE_ADMIN;
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员只能查询本公司的信息
			corporationsid = loginInfo.getCorporationsid();
			role = LoginableBasicInfo.ROLE_ADMIN;
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以查询本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = LoginableBasicInfo.ROLE_CORPORATION;
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
			role = LoginableBasicInfo.ROLE_GOVERNMENT;
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
			role = LoginableBasicInfo.ROLE_SUPER;
		} else { // 其他登录信息，逻辑错误
			return NettyUtils.getTokenError(iskeepAlive);
		}
		
		Map<String, String> newparams = Utils.paramsloginqueryfilter(params, role, corporationsid, operatesid);
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
			queryList.add(new QueryBean(Seller.class.getName(), "seller", field, value, valuetype));
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
		List<Seller> sellers = null;
		Long total = null;
		try {
			sellers = sellerDaoImpl.getListBy(queryList, true, intlimit, intpage);
			total = sellerDaoImpl.selectCount(queryList, true);
		} catch (Exception e) {
			JSONObject jsonObject2 = new JSONObject();
			jsonObject2.put("status", 4);
			jsonObject2.put("msg", e.getMessage());
			jsonObject2.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
		}
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询托运方信息成功");
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
		for (int i = 0; i < sellers.size(); i++) {
			JSONObject tmpjsonObject = Utils.getJsonObject(sellers.get(i));
			dataArray.put(tmpjsonObject);
		}
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}
}
