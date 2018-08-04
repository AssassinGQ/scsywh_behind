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
import com.Common.Entitys.LoginableBasicInfo;
import com.Common.Interfaces.GetKeyTypeCallback;
import com.Common.Utils.MD5Utils;
import com.Common.Utils.MySession;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
import com.Common.Utils.ValidUtils;
import com.Common.Utils.WebSocketUtils;
import com.Entitys.Admin.Entity.Admin;
import com.Entitys.Driver.Entity.Driver;
import com.Entitys.Seller.Entity.Seller;
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

public class LoginableBasicInfoHandler<T extends LoginableBasicInfo> {
	protected Class<T> clazz;
	protected String clazzname;
	private Long operatesid, corporationsid;
	private Integer operateusertype, role, basicinfousertype;

	public LoginableBasicInfoHandler(Class<T> clazz) {
		this.clazz = clazz;
		String[] tmp = clazz.getName().split("\\.");
		this.clazzname = tmp[tmp.length - 1];
	}

	public FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
		System.out.println("in Basic" + clazzname + "Request");
		if (loginInfo == null)
			return NettyUtils.getTokenError(iskeepAlive);
		basicinfousertype = clazz.newInstance().USERTYPE;
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
			if (clazzname.equals("Admin"))
				return NettyUtils.getTokenError(iskeepAlive);
			else {
				role = LoginableBasicInfo.ROLE_ADMIN;
				corporationsid = loginInfo.getCorporationsid();
			}
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
		String deptstr = params.get("dept");
		Integer dept = null;
		if (clazzname.equals("Admin")) {
			if (deptstr == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请填写部门信息");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			try {
				dept = Integer.parseInt(deptstr);
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "部门信息格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if (dept < Admin.DEPT_STORAGE || dept > Admin.DEPT_MANAGER) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "部门信息格式不正确");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		}

		Transaction tx = session.beginTransaction();
		// 创建基本信息
		T object = clazz.newInstance();
		params.put("username", username);
		if (phone != null)
			params.put("phone", phone);
		if (clazzname.equals("Admin"))
			params.put("dept", String.valueOf(dept));
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
		// 创建相应的登录信息
		String salt = Utils.getRandomString(10);
//		String pswDB = MD5Utils.MD5(username, password, salt);
		String pswDB = MD5Utils.MD5(password, salt);
		LoginInfo newlogininfo = new LoginInfo();
		newlogininfo.setUsersid(sid);
		newlogininfo.setCorporationsid(corporationsid);
		newlogininfo.setStatus(1);
		newlogininfo.setUsername(username);
		newlogininfo.setSalt(salt);
		newlogininfo.setPassword(pswDB);
		if (phone != null)
			newlogininfo.setPhone(phone);
		newlogininfo.setType(basicinfousertype);
		if (clazzname.equals("Admin"))
			newlogininfo.setDept(dept);
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
		jsonObject.put("msg", clazzname + "创建成功,请记下账号密码，防止遗失");
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
		Integer updatetype = null;
		if (operateusertype >= LoginInfo.TYPE_DRIVER && operateusertype <= LoginInfo.TYPE_BUYER) {// 本公司分配员工无权修改信息
			corporationsid = loginInfo.getCorporationsid();
			if (operateusertype == basicinfousertype) { // 相同的类型，可以修改本人信息
				role = LoginableBasicInfo.ROLE_USER;
				updatetype = operateusertype;
				updatesid = operatesid;
			} else {
				return NettyUtils.getTokenError(iskeepAlive); // 否则，无权限修改
			}
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = LoginableBasicInfo.ROLE_ADMIN; // 可以修改本公司的
			if (clazzname.equals("Admin")) {
				updatetype = operateusertype;
				updatesid = operatesid;
			} else {
				updatetype = basicinfousertype;
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
			}
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = LoginableBasicInfo.ROLE_CORPORATION; // 可以修改本公司的
			updatetype = basicinfousertype;
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
		T object = null;
		LoginInfo objectloginInfo = null;
		try {
			object = session.createQuery("from " + clazzname + " where datastatus = " + Bean.CREATED + " and sid = "
					+ updatesid + " and corporationsid = " + corporationsid, clazz).uniqueResult();
			objectloginInfo = session
					.createQuery(
							"from LoginInfo where datastatus = " + Bean.CREATED + " and usersid = " + updatesid
									+ " and type = " + updatetype + " and corporationsid = " + corporationsid,
							LoginInfo.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系系统管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (object == null) {
			if (objectloginInfo != null) {
				Transaction tx = session.beginTransaction();
				MySession.OnDelete(objectloginInfo, session, "0");
				tx.commit();
			}
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", clazzname + "信息不存在");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (objectloginInfo == null) {
			if (object != null) {
				Transaction tx = session.beginTransaction();
				MySession.OnDelete(object, session, "0");
				tx.commit();
			}
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", clazzname + "信息不存在");
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
//		String password = params.get("password");
//		if(password == null){
//			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("status", 4);
//			jsonObject.put("msg", "请输入原密码");
//			jsonObject.put("content", "");
//			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
//		}
//		String oldsalt = objectloginInfo.getSalt();
//		String oldusername = objectloginInfo.getUsername_tm_30();
//		String oldpswDB = objectloginInfo.getPassword_tm_100();
//		if(!oldpswDB.equals(MD5Utils.MD5(oldusername, password, oldsalt))){
//			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("status", 4);
//			jsonObject.put("msg", "原密码不正确");
//			jsonObject.put("content", "");
//			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
//		}
//		String newpassword = params.get("newpassword");
//		if (newpassword != null) {
//			if (!Utils.isPassword(newpassword)) {
//				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("status", 4);
//				jsonObject.put("msg", "密码长度至少8位，且必须包括数字和大小写字母");
//				jsonObject.put("content", "");
//				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
//			}
//		}
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
		String oldlogininfo = Utils.getJsonObject(objectloginInfo).toString();
		if(username != null){
			objectloginInfo.setUsername(username);
		}
		if(password != null){
			String salt = Utils.getRandomString(10);
			String pswDB = MD5Utils.MD5(password, salt);
			objectloginInfo.setPassword(pswDB);
			objectloginInfo.setSalt(salt);
		}
//		if (username != null && newpassword == null){
//			String salt = Utils.getRandomString(10);
//			String pswDB = MD5Utils.MD5(username, password, salt);
//			objectloginInfo.setPassword_tm_100(pswDB);
//			objectloginInfo.setSalt(salt);
//			objectloginInfo.setUsername_tm_30(username);
//		}
//		if (newpassword != null && username == null){
//			String salt = Utils.getRandomString(10);
//			String pswDB = MD5Utils.MD5(objectloginInfo.getUsername_tm_30(), newpassword, salt);
//			objectloginInfo.setPassword_tm_100(pswDB);
//			objectloginInfo.setSalt(salt);
//		}
//		if(newpassword != null && username != null){
//			String salt = Utils.getRandomString(10);
//			String pswDB = MD5Utils.MD5(username, newpassword, salt);
//			objectloginInfo.setPassword_tm_100(pswDB);
//			objectloginInfo.setSalt(salt);
//			objectloginInfo.setUsername_tm_30(username);
//		}
		if (phone != null)
			objectloginInfo.setPhone(phone);
		objectloginInfo.updatetime(operatesid);
		try {
			ValidUtils.ValidationWithExp(objectloginInfo);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		MySession.OnUpdate(oldlogininfo, objectloginInfo, session, operatesid);
		// 更新基础信息
		String oldobject = Utils.getJsonObject(object).toString();
		if (username != null)
			params.put("username", username);
		if (phone != null)
			params.put("phone", phone);
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
		jsonObject.put("msg", clazzname + "信息更新成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnDelete(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		Long updatesid = null;
		Integer updatetype = null;
		if (operateusertype >= LoginInfo.TYPE_DRIVER && operateusertype <= LoginInfo.TYPE_BUYER) {// 本公司分配员工无权修改信息
			corporationsid = loginInfo.getCorporationsid();
			if (operateusertype == basicinfousertype) { // 相同的类型，可以修改本人信息
				role = LoginableBasicInfo.ROLE_USER;
				updatetype = operateusertype;
				updatesid = operatesid;
			} else {
				return NettyUtils.getTokenError(iskeepAlive); // 否则，无权限修改
			}
		} else if (operateusertype == LoginInfo.TYPE_ADMIN) {// 公司管理员
			corporationsid = loginInfo.getCorporationsid();
			role = LoginableBasicInfo.ROLE_ADMIN; // 可以修改本公司的
			if (clazzname.equals("Admin")) {
				return NettyUtils.getTokenError(iskeepAlive); // 无权删除管理员信息
			} else {
				updatetype = basicinfousertype;
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
			}
		} else if (operateusertype == LoginInfo.TYPE_CORPORATION) {// 公司可以修改本公司信息
			corporationsid = loginInfo.getCorporationsid();
			role = LoginableBasicInfo.ROLE_CORPORATION; // 可以修改本公司的
			updatetype = basicinfousertype;
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
		T object = null;
		LoginInfo objectloginInfo = null;
		try {
			object = session.createQuery("from " + clazzname + " where datastatus = " + Bean.CREATED + " and sid = "
					+ updatesid + " and corporationsid = " + corporationsid, clazz).uniqueResult();
			objectloginInfo = session
					.createQuery(
							"from LoginInfo where datastatus = " + Bean.CREATED + " and usersid = " + updatesid
									+ " and type = " + updatetype + " and corporationsid = " + corporationsid,
							LoginInfo.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系系统管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (object == null) {
			if (objectloginInfo != null) {
				Transaction tx = session.beginTransaction();
				MySession.OnDelete(objectloginInfo, session, "0");
				tx.commit();
			}
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", clazzname + "信息不存在");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (objectloginInfo == null) {
			if (object != null) {
				Transaction tx = session.beginTransaction();
				MySession.OnDelete(object, session, "0");
				tx.commit();
			}
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", clazzname + "信息不存在");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		
		// 删除信息
		Transaction tx = session.beginTransaction();
		// 删除关联信息
		{
			if (objectloginInfo.getOnlinetype() != null
					&& objectloginInfo.getOnlinetype() == LoginInfo.ONLINETYPE_WEB
					&& objectloginInfo.getToken() != null)
				WebSocketUtils.RemoveCtx(objectloginInfo.getToken());
			Integer objecttype = null;
			Long objectsid = null;
			if (object instanceof Driver) {
				Driver driver = (Driver) object;
				objecttype = StatisticsStatic.OBJTECTYPE_DRIVER;
				objectsid = driver.getSid();
			}
			if (object instanceof Seller) {
				Seller seller = (Seller) object;
				objecttype = StatisticsStatic.OBJTECTYPE_SELLER;
				objectsid = seller.getSid();
			}
			if (objecttype != null && objectsid != null) {
				List<OrderMonthStatistics> orderMonthStatistics_delete = session.createQuery(
						"from OrderMonthStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
								+ objecttype + " and objectsid = " + objectsid,
						OrderMonthStatistics.class).list();
				List<OrderYearStatistics> orderYearStatistics_delete = session.createQuery(
						"from OrderYearStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
								+ objecttype + " and objectsid = " + objectsid,
						OrderYearStatistics.class).list();
				List<WarnMonthStatistics> WarnMonthStatistics_delete = session.createQuery(
						"from WarnMonthStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
								+ objecttype + " and objectsid = " + objectsid,
						WarnMonthStatistics.class).list();
				List<WarnYearStatistics> WarnYearStatistics_delete = session
						.createQuery("from WarnYearStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
								+ objecttype + " and objectsid = " + objectsid, WarnYearStatistics.class)
						.list();
				for (int i = 0; i < orderMonthStatistics_delete.size(); i++)
					MySession.OnDelete(orderMonthStatistics_delete.get(i), session, operatesid);
				for (int i = 0; i < orderYearStatistics_delete.size(); i++)
					MySession.OnDelete(orderYearStatistics_delete.get(i), session, operatesid);
				for (int i = 0; i < WarnMonthStatistics_delete.size(); i++)
					MySession.OnDelete(WarnMonthStatistics_delete.get(i), session, operatesid);
				for (int i = 0; i < WarnYearStatistics_delete.size(); i++)
					MySession.OnDelete(WarnYearStatistics_delete.get(i), session, operatesid);
			}
		}
		MySession.OnDelete(object, session, operatesid);
		MySession.OnDelete(objectloginInfo, session, operatesid);
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
		if (operateusertype == LoginInfo.TYPE_DRIVER) {// 本公司分配员工只能查询自己的信息
			if (basicinfousertype == operateusertype) {
				role = LoginableBasicInfo.ROLE_USER;
				corporationsid = loginInfo.getCorporationsid();
			} else
				return NettyUtils.getTokenError(iskeepAlive);
		} else if (operateusertype == LoginInfo.TYPE_SELLER || operateusertype == LoginInfo.TYPE_BUYER) {
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

		StringBuilder hql = new StringBuilder();
		StringBuilder hql2 = new StringBuilder();
		hql.append("from " + clazzname);
		hql2.append("select count(*) from " + clazzname);
		Map<String, String> newparams = Utils.paramsloginqueryfilter(params, role, corporationsid, operatesid);
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
		jsonObject.put("msg", clazzname + "信息查询成功");
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
