package com.RequestHandlers;

import com.Common.Entitys.Bean;
import com.Common.Utils.MySession;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
import com.Entitys.User.Entity.LoginInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class AuthHandler {
	public static FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
		if (path[1].equals("dae")) {//学习资料考试方面的权限
			if(path[2].equals("adduser")){
				return OnAddUser(ctx, session, method, params, iskeepAlive, loginInfo);
			}else if(path[2].equals("removeuser")){
				return OnRemoveUser(ctx, session, method, params, iskeepAlive, loginInfo);
			} else{
				return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
			}
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}
	
	private static FullHttpResponse OnAddUser(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
        if(inloginInfo == null)
            return NettyUtils.getTokenError(iskeepAlive);
        if(!inloginInfo.getUsername().equals("superadmin"))
            return NettyUtils.getTokenError(iskeepAlive);
		String usersidstr = params.get("usersid");
		if(usersidstr == null){
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传用户ID");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long usersid = null;
		try{
            usersid = Long.parseLong(usersidstr);
        }catch(Exception e){
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "用户ID格式不正确");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }

        LoginInfo loginInfoq = null;
        try {
            loginInfoq = session.createQuery("from LoginInfo where datastatus = "+Bean.CREATED+" and sid = '"+usersid+"'", LoginInfo.class).uniqueResult();
            if(loginInfoq == null){
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "查无此用户");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
        } catch (Exception e) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "用户信息数据库出错，主键重复");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        Transaction tx = session.beginTransaction();
        String oldlogininfo = Utils.getJsonObject(loginInfoq).toString();
        loginInfoq.setIsDocAuthd(LoginInfo.AUTHD_TRUE);
        loginInfoq.setIsExmAuthd(LoginInfo.AUTHD_TRUE);
        MySession.OnUpdate(oldlogininfo, loginInfoq, session, inloginInfo.getSid());
        tx.commit();
        JSONObject retObject = new JSONObject();
        retObject.put("status", 0);
        retObject.put("msg", "授权成功");
        retObject.put("content", "");
        return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}
	
	private static FullHttpResponse OnRemoveUser(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
			throws JSONException, InstantiationException, IllegalAccessException {
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
        if(inloginInfo == null)
            return NettyUtils.getTokenError(iskeepAlive);
        if(!inloginInfo.getUsername().equals("superadmin"))
            return NettyUtils.getTokenError(iskeepAlive);
        String usersidstr = params.get("usersid");
        if(usersidstr == null){
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请上传用户ID");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        Long usersid = null;
        try{
            usersid = Long.parseLong(usersidstr);
        }catch(Exception e){
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "用户ID格式不正确");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }

        LoginInfo loginInfoq = null;
        try {
            loginInfoq = session.createQuery("from LoginInfo where datastatus = "+Bean.CREATED+" and sid = '"+usersid+"'", LoginInfo.class).uniqueResult();
            if(loginInfoq == null){
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "查无此用户");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
        } catch (Exception e) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "用户信息数据库出错，主键重复");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        Transaction tx = session.beginTransaction();
        String oldlogininfo = Utils.getJsonObject(loginInfoq).toString();
        loginInfoq.setIsDocAuthd(LoginInfo.AUTHD_FALSE);
        loginInfoq.setIsExmAuthd(LoginInfo.AUTHD_FALSE);
        MySession.OnUpdate(oldlogininfo, loginInfoq, session, inloginInfo.getSid());
        tx.commit();
        JSONObject retObject = new JSONObject();
        retObject.put("status", 0);
        retObject.put("msg", "取消授权成功");
        retObject.put("content", "");
        return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

}
