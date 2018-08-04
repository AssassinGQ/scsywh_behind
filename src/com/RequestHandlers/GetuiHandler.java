package com.RequestHandlers;

import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Entitys.Bean;
import com.Common.Utils.MySession;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
import com.Common.Utils.WebSocketUtils;
import com.Entitys.Truck.Entity.Truck;
import com.Entitys.User.Entity.LoginInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class GetuiHandler {
	public static FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (path[1].equals("postcid")) {
			return OnCidPost(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}

	protected static FullHttpResponse OnCidPost(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in GetuiHandler-OnCidPost");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		String cid = params.get("cid");
		String typestr = params.get("type");
		if (cid == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请输入cid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		} else if (Utils.getLengthOfObject(cid) != 32) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "cid的长度必须是32位");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (typestr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请输入用户类型");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Integer type = null;
		try {
			type = Integer.parseInt(typestr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "type格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}

		if (type == 100) {
			String trucknumber = params.get("trucknumber");
			if (trucknumber == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "请输入车牌号");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			Truck truck = null;
			try {
				truck = session.createQuery(
						"from Truck where datastatus = " + Bean.CREATED + " and trucknumber = '" + trucknumber + "'",
						Truck.class).uniqueResult();
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "拖车数据库出错");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if (truck == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此车");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			Transaction tx = session.beginTransaction();
			String oldtruck = Utils.getJsonObject(truck).toString();
			truck.setCid(cid);
			truck.updatetime("0");
			MySession.OnUpdate(oldtruck, truck, session, "0");
			tx.commit();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "绑定cid成功");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		} else {
			if (loginInfo == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 0);
				jsonObject.put("msg", "请填写token");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			Long operatersid = loginInfo.getUsersid();
			Transaction tx = session.beginTransaction();
			String oldlogininfo = Utils.getJsonObject(loginInfo).toString();
			if (loginInfo.getOnlinetype() != null && loginInfo.getOnlinetype() == LoginInfo.ONLINETYPE_WEB) {
				WebSocketUtils.RemoveCtx(loginInfo.getWsid());
			}
			loginInfo.setOnlinetype(LoginInfo.ONLINETYPE_APP);
			loginInfo.setCid(cid);
			loginInfo.updatetime(operatersid);
			MySession.OnUpdate(oldlogininfo, loginInfo, session, operatersid);
			tx.commit();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "绑定cid成功");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
	}
}
