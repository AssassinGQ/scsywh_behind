package com.RequestHandlers;

import com.Common.Entitys.BasicInfo;
import com.Common.Entitys.Bean;
import com.Common.Utils.*;
import com.Entitys.Corporation.Entity.Corporation;
import com.Entitys.Government.Entity.Government;
import com.Entitys.Manufacturer.Entity.Manufacturer;
import com.Entitys.User.Entity.LoginInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class LoginHandler {
    public static FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
                                          Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
            throws JSONException, InstantiationException, IllegalAccessException {
        if (path[1].equals("register_corporation")) {// 承运方注册
            return OnCorporationRegister(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("register_government")) {// 政府注册
            return OnGovernmentRegister(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("register_manufacturer")) {// 生产商注册
            return OnManufacturerRegister(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("login")) {
            return OnLogin(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("getvcode")) {
            return OnGetVcode(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("update_corporation")) {
            return OnUpdateCorporation(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("update_manufacturer")) {
            return OnUpdateManufacturer(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("update_government")) {
            return OnUpdateGovernment(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("query_corporation")) {// 查询承运方信息（托运方，承运方，政府）
            return OnQueryCorporation(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("query_manufacturer")) {// 查询生产商信息（生产商，政府）
            return OnQueryManufacturer(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("query_government")) {// 查询政府信息（政府）
            return OnQueryGovernment(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("getAccount")) {
            return OnGetAccount(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("changepsw")) {
            return OnChangePSW(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("getList")) {
            return OnListAll(ctx, session, method, params, iskeepAlive, loginInfo);
        } else if (path[1].equals("getById")) {
            return OnGetById(ctx, session, method, params, iskeepAlive, loginInfo);
        } else {
            return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
        }
    }

    private static FullHttpResponse OnGetById(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                              Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
            throws JSONException {
        if (method != HttpMethod.GET) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        if (inloginInfo == null)
            return NettyUtils.getTokenError(iskeepAlive);
        if (!inloginInfo.getUsername().equals("superadmin"))
            return NettyUtils.getTokenError(iskeepAlive);
        String sidstr = params.get("sid");
        Long sid = null;
        try {
            sid = Long.parseLong(sidstr);
            if (sid == null)
                throw new Exception();
        } catch (Exception e) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "sid格式不正确");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        try {
            LoginInfo loginInfo = session
                    .createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and sid = " + sid, LoginInfo.class).uniqueResult();
            if (loginInfo == null) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "查无此信息");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            JSONObject retObject = new JSONObject();
            retObject.put("status", 0);
            retObject.put("msg", "查询成功");
            JSONObject tmpObject = new JSONObject();
            tmpObject.put("sid", loginInfo.getSid());
            tmpObject.put("usersid", loginInfo.getUsersid());
            tmpObject.put("username", loginInfo.getUsername());
            tmpObject.put("phone", loginInfo.getPhone());
            tmpObject.put("type", loginInfo.getType());
            tmpObject.put("token", loginInfo.getToken());
            tmpObject.put("isdocauthd", loginInfo.getIsDocAuthd());
            tmpObject.put("isexamauthd", loginInfo.getIsExmAuthd());
            JSONObject contentObject = new JSONObject();
//            JSONArray dataArray = new JSONArray();
//            dataArray.put(tmpObject);
//            contentObject.put("data", dataArray);
            retObject.put("content", tmpObject);
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        } catch (Exception e) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", e.getMessage());
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
    }

    private static FullHttpResponse OnListAll(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                              Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
            throws JSONException {
        if (method != HttpMethod.GET) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        if (inloginInfo == null)
            return NettyUtils.getTokenError(iskeepAlive);
        if (!inloginInfo.getUsername().equals("superadmin"))
            return NettyUtils.getTokenError(iskeepAlive);
        try {
            List<LoginInfo> loginInfos = session
                    .createQuery("from LoginInfo where datastatus = " + Bean.CREATED, LoginInfo.class).list();
            JSONObject retObject = new JSONObject();
            retObject.put("status", 0);
            retObject.put("msg", "查询成功");
            JSONArray dataArray = new JSONArray();
            for (int i = 0; i < loginInfos.size(); i++) {
                LoginInfo loginInfo_tmp = loginInfos.get(i);
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("sid", loginInfo_tmp.getSid());
                tmpObject.put("usersid", loginInfo_tmp.getUsersid());
                tmpObject.put("username", loginInfo_tmp.getUsername());
                tmpObject.put("phone", loginInfo_tmp.getPhone());
                tmpObject.put("type", loginInfo_tmp.getType());
                tmpObject.put("token", loginInfo_tmp.getToken());
                tmpObject.put("isdocauthd", loginInfo_tmp.getIsDocAuthd());
                tmpObject.put("isexamauthd", loginInfo_tmp.getIsExmAuthd());
                dataArray.put(tmpObject);
            }
            JSONObject contentObject = new JSONObject();
            contentObject.put("data", dataArray);
            retObject.put("content", contentObject);
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        } catch (Exception e) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", e.getMessage());
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
    }

    private static FullHttpResponse OnChangePSW(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                                Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
            throws JSONException, InstantiationException, IllegalAccessException {
        if (method != HttpMethod.POST) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        String phone = params.get("phone");
        if (phone == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请上传手机号");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        String vcode = params.get("vcode");
        if (vcode == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请上传验证码");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        String password = params.get("password");
        if (password == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请上传新密码");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        if (!Utils.isMobileNum(phone)) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "手机号格式不正确");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        if (!Utils.isPassword(password)) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "密码必须包含数字和大小写字母，且不得小于8位");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        LoginInfo loginInfoq = null;
        try {
            loginInfoq = session.createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and phone = '" + phone + "'", LoginInfo.class).uniqueResult();
        } catch (Exception e) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "用户信息数据库出错，多个相同的手机号");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        if (loginInfoq == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "查无此用户");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        if (loginInfoq.getVcode() == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请先获取验证码");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        if (!vcode.equals(loginInfoq.getVcode())) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "验证码不在正确");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        Transaction tx = session.beginTransaction();
        String oldlogininfo = Utils.getJsonObject(loginInfoq).toString();
        String username = loginInfoq.getUsername();
        String salt = Utils.getRandomString(10);
        String pswDB = MD5Utils.MD5(username, password, salt);
        loginInfoq.setSalt(salt);
        loginInfoq.setPassword(pswDB);
        loginInfoq.updatetime("0");
        MySession.OnUpdate(oldlogininfo, loginInfoq, session, "0");
        tx.commit();
        JSONObject retObject = new JSONObject();
        retObject.put("status", 0);
        retObject.put("msg", "密码修改成功");
        JSONObject contentObject = new JSONObject();
        contentObject.put("username", loginInfoq.getUsername());
        contentObject.put("password", password);
        retObject.put("content", contentObject);
        return NettyUtils.getResponse(iskeepAlive, retObject.toString());
    }

    private static FullHttpResponse OnGetAccount(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                                 Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
            throws JSONException, InstantiationException, IllegalAccessException {
        if (method != HttpMethod.POST) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        if (inloginInfo == null)
            return NettyUtils.getTokenError(iskeepAlive);
        if (!inloginInfo.getUsername().equals("superadmin"))
            return NettyUtils.getTokenError(iskeepAlive);
        String typestr = params.get("type");
        if (typestr == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请上传账号类型");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        Integer type = null;
        try {
            type = Integer.parseInt(typestr);
        } catch (Exception e) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "账号类型格式不正确");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        String name = params.get("name");
        if (name == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请上传名称");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        String phone = params.get("phone");
        if (phone == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请上传名称");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        if (!Utils.isMobileNum(phone)) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "手机号格式不正确");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        {
            try {
                LoginInfo loginInfo_checkphone = session.createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and phone = '" + phone + "'", LoginInfo.class).uniqueResult();
                if (loginInfo_checkphone != null) {
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", "该手机号已被使用");
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
            } catch (Exception e) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "登录信息数据库出错");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
        }
        String username = params.get("username");
        if(username == null){
            while (true) {
                if (type == 0)
                    username = "corp" + Utils.getRandomString(5);
                else if (type == 1)
                    username = "gov" + Utils.getRandomString(5);
                else if (type == 2)
                    username = "manu" + Utils.getRandomString(5);
                else {
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", "账号类型格式不正确");
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                try {
                    LoginInfo loginInfo_checkuname = session.createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and username = '" + username + "'", LoginInfo.class).uniqueResult();
                    if (loginInfo_checkuname == null) {
                        break;
                    }
                } catch (Exception e) {
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", "登录信息数据库出错");
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
            }
        } else{
            try {
                LoginInfo loginInfo_checkuname = session.createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and username = '" + username + "'", LoginInfo.class).uniqueResult();
                if (loginInfo_checkuname != null) {
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", "用户名已经存在");
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
            } catch (Exception e) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "登录信息数据库出错");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
        }

        String password = params.get("password");
        if(password == null){
            password = Utils.getPassword();
        }
        String salt = Utils.getRandomString(10);
        String pswDB = MD5Utils.MD5(password, salt);
        if (type == 0) {//承运方
            Transaction tx = session.beginTransaction();
            Corporation corporation = new Corporation();
            corporation.setName(name);
            corporation.setUsername(username);
            corporation.setPhone(phone);
            corporation.createtime("0");
            Long corpsid = MySession.OnSave(corporation, session, "0");
            String oldcorp = Utils.getJsonObject(corporation).toString();
            corporation.setCorporationsid(corpsid);
            MySession.OnUpdate(oldcorp, corporation, session, "0");
            LoginInfo loginInfo = new LoginInfo();
            loginInfo.setCorporationsid(corpsid);
            loginInfo.setUsername(username);
            loginInfo.setSalt(salt);
            loginInfo.setPassword(pswDB);
            loginInfo.setPhone(phone);
            loginInfo.setUsersid(corpsid);
            loginInfo.setStatus(1);
            loginInfo.setType(LoginInfo.TYPE_CORPORATION);
            loginInfo.createtime("0");
            MySession.OnSave(loginInfo, session, "0");
            tx.commit();
            JSONObject retObject = new JSONObject();
            retObject.put("status", 0);
            retObject.put("msg", "成功");
            JSONObject infoObject = new JSONObject();
            infoObject.put("username", username);
            infoObject.put("password", password);
            retObject.put("content", infoObject);
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        } else if (type == 1) {//政府
            String deptstr = params.get("dept");
            if (deptstr == null) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "请上传部门信息");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            Integer dept = null;
            try {
                dept = Integer.parseInt(deptstr);
            } catch (Exception e) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "部门信息格式不正确");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            Transaction tx = session.beginTransaction();
            Government government = new Government();
            government.setName(name);
            government.setUsername(username);
            government.setCorporationsid(-1L);
            government.setDept(dept);
            government.setPhone(phone);
            government.createtime("0");
            Long govesid = MySession.OnSave(government, session, "0");
            LoginInfo loginInfo = new LoginInfo();
            loginInfo.setCorporationsid(govesid);
            loginInfo.setUsername(username);
            loginInfo.setSalt(salt);
            loginInfo.setPassword(pswDB);
            loginInfo.setPhone(phone);
            loginInfo.setUsersid(govesid);
            loginInfo.setStatus(1);
            loginInfo.setDept(government.getDept());
            loginInfo.setType(LoginInfo.TYPE_GOVERNMENT);
            loginInfo.createtime("0");
            MySession.OnSave(loginInfo, session, "0");
            tx.commit();
            JSONObject retObject = new JSONObject();
            retObject.put("status", 0);
            retObject.put("msg", "成功");
            JSONObject infoObject = new JSONObject();
            infoObject.put("username", username);
            infoObject.put("password", password);
            retObject.put("content", infoObject);
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        } else if (type == 2) {//设备商
            Transaction tx = session.beginTransaction();
            Manufacturer Manufacturer = new Manufacturer();
            Manufacturer.setName(name);
            Manufacturer.setCorporationsid(-1L);
            Manufacturer.setUsername(username);
            Manufacturer.setPhone(phone);
            Manufacturer.createtime("0");
            Long manusid = MySession.OnSave(Manufacturer, session, "0");
            LoginInfo loginInfo = new LoginInfo();
            loginInfo.setCorporationsid(manusid);
            loginInfo.setUsername(username);
            loginInfo.setSalt(salt);
            loginInfo.setPassword(pswDB);
            loginInfo.setPhone(phone);
            loginInfo.setUsersid(manusid);
            loginInfo.setStatus(1);
            loginInfo.setType(LoginInfo.TYPE_MANUFACTURER);
            loginInfo.createtime("0");
            MySession.OnSave(loginInfo, session, "0");
            tx.commit();
            JSONObject retObject = new JSONObject();
            retObject.put("status", 0);
            retObject.put("msg", "成功");
            JSONObject infoObject = new JSONObject();
            infoObject.put("username", username);
            infoObject.put("password", password);
            retObject.put("content", infoObject);
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        } else {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "账号类型格式不正确");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
    }

    private static FullHttpResponse OnCorporationRegister(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                                          Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
            throws JSONException, InstantiationException, IllegalAccessException {
        System.out.println("in LoginHandler-OnCorporationRegister");
        if (method != HttpMethod.POST) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        // 检测是否有重复的用户名
        String username = params.get("username");
        if (username != null) {
            Long total = session.createQuery("select count(*) from LoginInfo where datastatus = " + Bean.CREATED
                    + " and username = '" + username + "'", Long.class).uniqueResult();
            if (total > 0) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "用户名已经被使用");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
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
        } else
            password = Utils.getPassword();
        // 检测是否有重复的手机号
        String phone = params.get("phone");
        if (phone == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请填写手机号");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        if (!Utils.isMobileNum(phone)) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请填写有效的手机号");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        List<LoginInfo> loginInfos = session
                .createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and phone = '" + phone + "'",
                        LoginInfo.class)
                .getResultList();
        if (loginInfos.size() == 1) { // 有一个结果，正常
            LoginInfo loginInfo = loginInfos.get(0);
            if (loginInfo.getStatus() == 1) { // 该手机号已注册
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "手机号已经被使用");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            } else { // 该手机号未注册
                String vcodein = params.get("vcode");
                if (vcodein == null) {
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", "请填写验证码");
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                String vcode = loginInfo.getVcode();
                if (vcode == null) { // 验证码为空，没出错是不会出现这种状况的
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", "请先获取验证码");
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                if (!vcode.equals(vcodein)) { // 验证码不正确
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", "验证码不正确");
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                // 验证码正确，更新用户登录信息
                Transaction tx = session.beginTransaction();
                // 生成承运方基本信息
                String corporationname = params.get("name");
                Corporation corporation = new Corporation();
                corporation.setName(corporationname);
                corporation.setUsername(username);
                corporation.setPhone(phone);
                corporation.createtime("0");
                try {
                    //Utils.Validation(corporation);
                } catch (Exception e) {
                    tx.rollback();
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", e.getMessage());
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                Long corporationsid = MySession.OnSave(corporation, session, "0");
                String oldcorporation = Utils.getJsonObject(corporation).toString();
                corporation.setCorporationsid(corporationsid);
                MySession.OnUpdate(oldcorporation, corporation, session, "0");
                String oldobject = Utils.getJsonObject(loginInfo).toString();
                loginInfo.setType(LoginInfo.TYPE_CORPORATION);
                loginInfo.setStatus(1);
                loginInfo.setUsersid(corporationsid);
                loginInfo.setCorporationsid(corporationsid);
                // Map<String, String> newparams = new HashMap<String,
                // String>();
                // newparams.put("username", username);
                // newparams.put("password", password);
                // try {
                // loginInfo.UpdateFromMap(newparams, session);
                // } catch (Exception e) {

                loginInfo.setUsername(username);
                loginInfo.setPassword(password);
                // tx.rollback();
                // JSONObject retObject = new JSONObject();
                // retObject.put("status", 4);
                // retObject.put("msg", e.getMessage());
                // retObject.put("content", "");
                // return NettyUtils.getResponse(iskeepAlive,
                // retObject.toString());
                // }
                loginInfo.updatetime("0");
                try {
                    ValidUtils.ValidationWithExp(loginInfo);
                } catch (Exception e) {
                    tx.rollback();
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", e.getMessage());
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                MySession.OnUpdate(oldobject, loginInfo, session, "0");
                tx.commit();
                JSONObject retObject = new JSONObject();
                retObject.put("status", 0);
                retObject.put("msg", "注册成功");
                JSONObject contentjb = new JSONObject();
                contentjb.put("sid", loginInfo.getUsersid());
                contentjb.put("username", loginInfo.getUsername());
                retObject.put("content", contentjb);
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
        } else if (loginInfos.size() == 0) { // 有零个结果，不正常，应当先获取验证码
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请先获取验证码");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        } else { // 有两个或两个以上结果，不正常，数据库数据异常，管理员手动修改数据库
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "数据库错误，请联系系统管理员");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
    }

    private static FullHttpResponse OnGovernmentRegister(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                                         Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
            throws JSONException, InstantiationException, IllegalAccessException {
        System.out.println("in LoginHandler-OnGovernmentRegister");
        if (method != HttpMethod.POST) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        // 检测是否有重复的用户名
        String username = params.get("username");
        if (username != null) {
            Long total = session.createQuery("select count(*) from LoginInfo where datastatus = " + Bean.CREATED
                    + " and username = '" + username + "'", Long.class).uniqueResult();
            if (total > 0) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "用户名已经被使用");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
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
        }
        if (password == null)
            password = Utils.getPassword();
        // 检测是否有重复的手机号
        String phone = params.get("phone");
        if (phone == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请填写手机号");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        if (!Utils.isMobileNum(phone)) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请填写有效的手机号");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        String deptstr = params.get("dept");
        if (deptstr == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请填写部门编号");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        Integer dept = null;
        try {
            dept = Integer.parseInt(deptstr);
        } catch (Exception e) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "部门编号格式不正确");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        List<LoginInfo> loginInfos = session
                .createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and phone = '" + phone + "'",
                        LoginInfo.class)
                .getResultList();
        if (loginInfos.size() == 1) { // 有一个结果，正常
            LoginInfo loginInfo = loginInfos.get(0);
            if (loginInfo.getStatus() == 1) { // 该手机号已注册
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "手机号已经被使用");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            } else { // 该手机号未注册
                String vcodein = params.get("vcode");
                if (vcodein == null) {
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", "请填写验证码");
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                String vcode = loginInfo.getVcode();
                if (vcode == null) { // 验证码为空，没出错是不会出现这种状况的
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", "请先获取验证码");
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                if (!vcode.equals(vcodein)) { // 验证码不正确
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", "验证码不正确");
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                // 验证码正确，更新用户登录信息
                Transaction tx = session.beginTransaction();
                // 生成政府基本信息
                String governmentname = params.get("name");
                Government government = new Government();
                government.setName(governmentname);
                government.setCorporationsid(-1L);
                government.setUsername(username);
                government.setPhone(phone);
                government.setDept(dept);
                government.createtime("0");
                try {
                    //Utils.Validation(government);
                } catch (Exception e) {
                    tx.rollback();
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", e.getMessage());
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                Long governmentsid = MySession.OnSave(government, session, "0");
                String oldobject = Utils.getJsonObject(loginInfo).toString();
                loginInfo.setType(LoginInfo.TYPE_GOVERNMENT);
                loginInfo.setStatus(1);
                loginInfo.setUsersid(governmentsid);
                loginInfo.setDept(government.getDept());
                loginInfo.setCorporationsid(Long.parseLong("-1"));
                // Map<String, String> newparams = new HashMap<String,
                // String>();
                // newparams.put("username", username);
                // newparams.put("password", password);
                // try {
                // loginInfo.UpdateFromMap(newparams, session);
                // } catch (Exception e) {

                loginInfo.setUsername(username);
                loginInfo.setPassword(password);
                // tx.rollback();
                // JSONObject retObject = new JSONObject();
                // retObject.put("status", 4);
                // retObject.put("msg", e.getMessage());
                // retObject.put("content", "");
                // return NettyUtils.getResponse(iskeepAlive,
                // retObject.toString());
                // }
                loginInfo.updatetime("0");
                try {
                    ValidUtils.ValidationWithExp(loginInfo);
                } catch (Exception e) {
                    tx.rollback();
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", e.getMessage());
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                MySession.OnUpdate(oldobject, loginInfo, session, "0");
                tx.commit();
                JSONObject retObject = new JSONObject();
                retObject.put("status", 0);
                retObject.put("msg", "注册成功");
                JSONObject contentjb = new JSONObject();
                contentjb.put("sid", loginInfo.getUsersid());
                contentjb.put("username", loginInfo.getUsername());
                contentjb.put("dept", loginInfo.getDept());
                retObject.put("content", contentjb);
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
        } else if (loginInfos.size() == 0) { // 有零个结果，不正常，应当先获取验证码
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请先获取验证码");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        } else { // 有两个或两个以上结果，不正常，数据库数据异常，管理员手动修改数据库
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "数据库错误，请联系系统管理员");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
    }

    private static FullHttpResponse OnManufacturerRegister(ChannelHandlerContext ctx, Session session,
                                                           HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
            throws JSONException, InstantiationException, IllegalAccessException {
        System.out.println("in LoginHandler-OnManufacturerRegister");
        if (method != HttpMethod.POST) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        // 检测是否有重复的用户名
        String username = params.get("username");
        if (username != null) {
            Long total = session.createQuery("select count(*) from LoginInfo where datastatus = " + Bean.CREATED
                    + " and username = '" + username + "'", Long.class).uniqueResult();
            if (total > 0) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "用户名已经被使用");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
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
        }
        if (password == null)
            password = Utils.getPassword();
        // 检测是否有重复的手机号
        String phone = params.get("phone");
        if (phone == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请填写手机号");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        if (!Utils.isMobileNum(phone)) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请填写有效的手机号");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        List<LoginInfo> loginInfos = session
                .createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and phone = '" + phone + "'",
                        LoginInfo.class)
                .getResultList();
        if (loginInfos.size() == 1) { // 有一个结果，正常
            LoginInfo loginInfo = loginInfos.get(0);
            if (loginInfo.getStatus() == 1) { // 该手机号已注册
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "手机号已经被使用");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            } else { // 该手机号未注册
                String vcodein = params.get("vcode");
                if (vcodein == null) {
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", "请填写验证码");
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                String vcode = loginInfo.getVcode();
                if (vcode == null) { // 验证码为空，没出错是不会出现这种状况的
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", "请先获取验证码");
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                if (!vcode.equals(vcodein)) { // 验证码不正确
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", "验证码不正确");
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                // 验证码正确，更新用户登录信息
                Transaction tx = session.beginTransaction();
                // 生成承运方基本信息
                String manufacturername = params.get("name");
                Manufacturer manufacturer = new Manufacturer();
                manufacturer.setName(manufacturername);
                manufacturer.setCorporationsid(-1L);
                manufacturer.setUsername(username);
                manufacturer.setPhone(phone);
                manufacturer.createtime("0");
                try {
                    //Utils.Validation(manufacturer);
                } catch (Exception e) {
                    tx.rollback();
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", e.getMessage());
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                Long manufacturersid = MySession.OnSave(manufacturer, session, "0");
                String oldobject = Utils.getJsonObject(loginInfo).toString();
                loginInfo.setType(LoginInfo.TYPE_MANUFACTURER);
                loginInfo.setStatus(1);
                loginInfo.setUsersid(manufacturersid);
                loginInfo.setCorporationsid(Long.parseLong("-1"));
                // Map<String, String> newparams = new HashMap<String,
                // String>();
                // newparams.put("username", username);
                // newparams.put("password", password);
                // try {
                // loginInfo.UpdateFromMap(newparams, session);
                // } catch (Exception e) {

                loginInfo.setUsername(username);
                loginInfo.setPassword(password);
                // tx.rollback();
                // JSONObject retObject = new JSONObject();
                // retObject.put("status", 4);
                // retObject.put("msg", e.getMessage());
                // retObject.put("content", "");
                // return NettyUtils.getResponse(iskeepAlive,
                // retObject.toString());
                // }
                loginInfo.updatetime("0");
                try {
                    ValidUtils.ValidationWithExp(loginInfo);
                } catch (Exception e) {
                    tx.rollback();
                    JSONObject retObject = new JSONObject();
                    retObject.put("status", 4);
                    retObject.put("msg", e.getMessage());
                    retObject.put("content", "");
                    return NettyUtils.getResponse(iskeepAlive, retObject.toString());
                }
                MySession.OnUpdate(oldobject, loginInfo, session, "0");
                tx.commit();
                JSONObject retObject = new JSONObject();
                retObject.put("status", 0);
                retObject.put("msg", "注册成功");
                JSONObject contentjb = new JSONObject();
                contentjb.put("sid", loginInfo.getUsersid());
                contentjb.put("username", loginInfo.getUsername());
                retObject.put("content", contentjb);
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
        } else if (loginInfos.size() == 0) { // 有零个结果，不正常，应当先获取验证码
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请先获取验证码");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        } else { // 有两个或两个以上结果，不正常，数据库数据异常，管理员手动修改数据库
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "数据库错误，请联系系统管理员");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
    }

    private static FullHttpResponse OnLogin(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                            Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
            throws JSONException, InstantiationException, IllegalAccessException {
        System.out.println("in LoginHandler-OnLogin");
        if (method != HttpMethod.POST) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        String username = params.get("username");
        if (username == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请填写账号");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        LoginInfo loginInfo = session
                .createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and username = '" + username + "'",
                        LoginInfo.class)
                .uniqueResult();
        if (loginInfo == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "用户名不存在");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        String password = params.get("password");
        if (password == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请填写密码");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        if (loginInfo.getStatus() == 0) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "请先注册");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        if (loginInfo.getSalt() == null) {
            String savedpassword = loginInfo.getPassword();
            if (savedpassword == null) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "数据库错误，请联系系统管理员");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            if (!(savedpassword.equals(password))) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "密码不正确");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            Transaction tx = session.beginTransaction();
            if (loginInfo.getOnlinetype() != null && loginInfo.getOnlinetype() == LoginInfo.ONLINETYPE_WEB
                    && loginInfo.getToken() != null)
                WebSocketUtils.RemoveCtx(loginInfo.getToken());
            String oldobject = Utils.getJsonObject(loginInfo).toString();
            loginInfo.setLastlogintime(Utils.getCurrenttimeMills());
            String token = Utils.getToken();
            loginInfo.setToken(token);
            loginInfo.setTokentime(Utils.getCurrenttimeMills());
            loginInfo.updatetime("0");
            MySession.OnUpdate(oldobject, loginInfo, session, "0");
            tx.commit();
            JSONObject retObject = new JSONObject();
            retObject.put("status", 0);
            retObject.put("msg", "登录成功");
            retObject.put("content", Utils.getJsonObject(loginInfo));
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        } else {
            String pswDB = loginInfo.getPassword();
            String salt = loginInfo.getSalt();
            String pswIN = MD5Utils.MD5(username, password, salt);
            String pswIN2 = MD5Utils.MD5(password, salt);
            if (pswDB == null) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "数据库错误，请联系系统管理员");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            if (!(pswDB.equals(pswIN)) && !(pswDB.equals(pswIN2))) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "密码不正确");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            Transaction tx = session.beginTransaction();
            if (loginInfo.getOnlinetype() != null && loginInfo.getOnlinetype() == LoginInfo.ONLINETYPE_WEB
                    && loginInfo.getToken() != null)
                WebSocketUtils.RemoveCtx(loginInfo.getToken());
            String oldobject = Utils.getJsonObject(loginInfo).toString();
            loginInfo.setLastlogintime(Utils.getCurrenttimeMills());
            String token = Utils.getToken();
            loginInfo.setToken(token);
            loginInfo.setTokentime(Utils.getCurrenttimeMills());
            loginInfo.updatetime("0");
            MySession.OnUpdate(oldobject, loginInfo, session, "0");
            tx.commit();
            JSONObject retObject = new JSONObject();
            retObject.put("status", 0);
            retObject.put("msg", "登录成功");
            retObject.put("content", Utils.getJsonObject(loginInfo));
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
    }

    private static FullHttpResponse OnGetVcode(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                               Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
            throws JSONException, InstantiationException, IllegalAccessException {
        System.out.println("in LoginHandler-OnGetVcode");
        if (method != HttpMethod.GET) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        String phone = params.get("phone");
        if (!(phone != null && phone.length() == 11 && Utils.isMobileNum(phone))) { // 检测是否为有效手机号码
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "手机号格式不正确");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        // 检测是否有重复的手机号
        List<LoginInfo> loginInfos = session
                .createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and phone = '" + phone + "'",
                        LoginInfo.class)
                .getResultList();
        if (loginInfos.size() == 0) { // 没有该手机号码的数据，正常，未注册过也未获取过验证码，发送验证码并保存对象
            String vcode = Utils.getRandomString(6);
            // 在这里发送验证码，并判断是否发送成功
            Transaction tx = session.beginTransaction();
            LoginInfo loginInfo = new LoginInfo();
            loginInfo.setPhone(phone);
            loginInfo.setVcode(vcode);
            loginInfo.setStatus(0);
            loginInfo.createtime("0");
            // session.save(loginInfo);
            MySession.OnSave(loginInfo, session, "0");
            tx.commit();
            JSONObject retObject = new JSONObject();
            retObject.put("status", 0);
            retObject.put("msg", "验证码已发送");
            JSONObject contentobject = new JSONObject();
            contentobject.put("vcode", vcode);
            retObject.put("content", contentobject);
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        } else if (loginInfos.size() == 1) { // 有一条该手机号码的数据，正常，发送验证码，并更新对象
            LoginInfo loginInfo = loginInfos.get(0);
            String oldobject = Utils.getJsonObject(loginInfo).toString();
            String vcode = Utils.getRandomString(6);
            // 在这里发送验证码，并判断是否发送成功
            Transaction tx = session.beginTransaction();
            loginInfo.setVcode(vcode);
            loginInfo.setStatus(1);
            loginInfo.updatetime("0");
            MySession.OnUpdate(oldobject, loginInfo, session, "0");
            // session.update(loginInfo);
            tx.commit();
            JSONObject retObject = new JSONObject();
            retObject.put("status", 0);
            retObject.put("msg", "验证码已发送");
            JSONObject contentobject = new JSONObject();
            contentobject.put("vcode", vcode);
            retObject.put("content", contentobject);
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        } else { // 有多条该手机号码的数据，不正常，数据库数据异常，管理员手动修改数据库
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "数据库错误，请联系系统管理员");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
    }

    private static FullHttpResponse OnQueryCorporation(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                                       Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo) throws JSONException {
        System.out.println("in LoginHandler-OnQueryCorporation");
        if (method != HttpMethod.GET) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        if (inloginInfo == null) {
            return NettyUtils.getTokenError(iskeepAlive);
        }
        int operatertype = inloginInfo.getType();
        int role;
        if (operatertype == LoginInfo.TYPE_GOVERNMENT) {
            role = BasicInfo.ROLE_GOVERNMENT;
        } else if (operatertype == LoginInfo.TYPE_CORPORATION) {// 查自己信息
            role = BasicInfo.ROLE_CORPORATION;
        } else if (operatertype == LoginInfo.TYPE_SELLER) {// 托运方查承运方名字
            role = BasicInfo.ROLE_USER;
        } else if (operatertype == LoginInfo.TYPE_ADMIN) {
            role = BasicInfo.ROLE_ADMIN;
        } else {
            return NettyUtils.getTokenError(iskeepAlive);
        }

        // 先处理托运方请求
        if (role == BasicInfo.ROLE_USER) {
            Corporation corporation = null;
            try {
                corporation = session.createQuery("from Corporation where datastatus = " + Bean.CREATED + " and sid = "
                        + inloginInfo.getCorporationsid(), Corporation.class).uniqueResult();
            } catch (Exception e) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "承运方数据库出错，请联系系统管理员");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            if (corporation == null) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "未找到相应承运方信息");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            JSONObject retObject = new JSONObject();
            retObject.put("status", 0);
            retObject.put("msg", "查询承运方信息成功");
            JSONObject contentobject = new JSONObject();
            contentobject.put("corporationname", corporation.getName());
            retObject.put("content", contentobject);
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        //再处理管理员
        if (role == BasicInfo.ROLE_ADMIN) {
            Corporation corporation = null;
            try {
                corporation = session.createQuery("from Corporation where datastatus = " + Bean.CREATED + " and sid = "
                        + inloginInfo.getCorporationsid(), Corporation.class).uniqueResult();
            } catch (Exception e) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "承运方数据库出错，请联系系统管理员");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            if (corporation == null) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "未找到相应承运方信息");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            JSONObject retObject = new JSONObject();
            retObject.put("status", 0);
            retObject.put("msg", "查询承运方信息成功");
            retObject.put("content", Utils.getJsonObject(corporation));
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        // 再处理承运方请求
        if (role == BasicInfo.ROLE_CORPORATION) {
            Corporation corporation = null;
            try {
                corporation = session.createQuery("from Corporation where datastatus = " + Bean.CREATED + " and sid = "
                        + inloginInfo.getUsersid(), Corporation.class).uniqueResult();
            } catch (Exception e) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "承运方数据库出错，请联系系统管理员");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            if (corporation == null) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "未找到相应承运方信息");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            JSONObject retObject = new JSONObject();
            retObject.put("status", 0);
            retObject.put("msg", "查询承运方信息成功");
            retObject.put("content", Utils.getJsonObject(corporation));
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        // 政府或者超级管理员请求
        String sidstr = params.get("sid");
        Long sid = null;
        if (sidstr != null) {
            try {
                sid = Long.parseLong(sidstr);
            } catch (Exception e) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "sid格式有误");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
        }

        StringBuilder hql = new StringBuilder();
        StringBuilder hql2 = new StringBuilder();
        hql.append("from Corporation");
        hql2.append("select count(*) from Corporation");
        if (sid != null) {
            hql.append(" where datastatus = " + Bean.CREATED + " and sid = " + sid);
            hql2.append(" where datastatus = " + Bean.CREATED + " and sid = " + sid);
        }
        System.out.println(hql.toString());
        Query<Corporation> query = session.createQuery(hql.toString(), Corporation.class);
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
        List<Corporation> objects = query.list();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", 0);
        jsonObject.put("msg", "查询承运方信息成功");
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
        for (int i = 0; i < objects.size(); i++) {
            dataArray.put(Utils.getJsonObject(objects.get(i)));
        }
        contentJsonObject.put("data", dataArray);
        jsonObject.put("content", contentJsonObject);
        return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
    }

    private static FullHttpResponse OnQueryManufacturer(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                                        Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo) throws JSONException {
        System.out.println("in LoginHandler-OnQueryManufacturer");
        if (method != HttpMethod.GET) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        if (inloginInfo == null) {
            return NettyUtils.getTokenError(iskeepAlive);
        }
        int operatertype = inloginInfo.getType();
        int role;
        if (operatertype == LoginInfo.TYPE_GOVERNMENT) {
            role = BasicInfo.ROLE_GOVERNMENT;
        } else if (operatertype == LoginInfo.TYPE_MANUFACTURER) {// 查自己信息
            role = BasicInfo.ROLE_MANUFACTURER;
        } else {
            return NettyUtils.getTokenError(iskeepAlive);
        }

        // 先处理生产商请求
        if (role == BasicInfo.ROLE_MANUFACTURER) {
            Manufacturer manufacturer = null;
            try {
                manufacturer = session.createQuery("from Manufacturer where datastatus = " + Bean.CREATED
                        + " and sid = " + inloginInfo.getUsersid(), Manufacturer.class).uniqueResult();
            } catch (Exception e) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "生产商数据库出错，请联系系统管理员");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            if (manufacturer == null) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "未找到相应生产商信息");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            JSONObject retObject = new JSONObject();
            retObject.put("status", 0);
            retObject.put("msg", "查询生产商信息成功");
            retObject.put("content", Utils.getJsonObject(manufacturer));
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        // 政府或者超级管理员请求
        String sidstr = params.get("sid");
        Long sid = null;
        if (sidstr != null) {
            try {
                sid = Long.parseLong(sidstr);
            } catch (Exception e) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "sid格式有误");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
        }

        StringBuilder hql = new StringBuilder();
        StringBuilder hql2 = new StringBuilder();
        hql.append("from Manufacturer");
        hql2.append("select count(*) from Manufacturer");
        if (sid != null) {
            hql.append(" where datastatus = " + Bean.CREATED + " and sid = " + sid);
            hql2.append(" where datastatus = " + Bean.CREATED + " and sid = " + sid);
        }
        System.out.println(hql.toString());
        Query<Corporation> query = session.createQuery(hql.toString(), Corporation.class);
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
        List<Corporation> objects = query.list();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", 0);
        jsonObject.put("msg", "查询生产商信息成功");
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
        for (int i = 0; i < objects.size(); i++) {
            dataArray.put(Utils.getJsonObject(objects.get(i)));
        }
        contentJsonObject.put("data", dataArray);
        jsonObject.put("content", contentJsonObject);
        return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
    }

    private static FullHttpResponse OnQueryGovernment(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                                      Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo) throws JSONException {
        System.out.println("in LoginHandler-OnQueryGovernment");
        if (method != HttpMethod.GET) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        if (inloginInfo == null) {
            return NettyUtils.getTokenError(iskeepAlive);
        }
        int operatertype = inloginInfo.getType();
        if (operatertype != LoginInfo.TYPE_GOVERNMENT)
            return NettyUtils.getTokenError(iskeepAlive);

        Government government = null;
        try {
            government = session.createQuery("from Government where datastatus = " + Bean.CREATED + " and sid = "
                    + inloginInfo.getUsersid(), Government.class).uniqueResult();
        } catch (Exception e) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "政府数据库出错，请联系系统管理员");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        if (government == null) {
            JSONObject retObject = new JSONObject();
            retObject.put("status", 4);
            retObject.put("msg", "未找到相应政府信息");
            retObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, retObject.toString());
        }
        JSONObject retObject = new JSONObject();
        retObject.put("status", 0);
        retObject.put("msg", "查询政府信息成功");
        retObject.put("content", Utils.getJsonObject(government));
        return NettyUtils.getResponse(iskeepAlive, retObject.toString());
    }

    private static FullHttpResponse OnUpdateCorporation(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                                        Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
            throws JSONException, InstantiationException, IllegalAccessException {
        System.out.println("in LoginHandler-OnUpdateCorporation");
        if (method != HttpMethod.POST) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        System.err.println("updateCorp" + inloginInfo);
        if (inloginInfo == null) {
            return NettyUtils.getTokenError(iskeepAlive);
        }
        int operatertype = inloginInfo.getType();
        Long operatersid = inloginInfo.getUsersid();
        Corporation corporation = null;
        if (operatertype == LoginInfo.TYPE_CORPORATION) {
            try {
                corporation = session.createQuery("from Corporation where datastatus = " + Bean.CREATED + " and sid = "
                        + inloginInfo.getUsersid(), Corporation.class).uniqueResult();
            } catch (Exception e) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "数据库信息错误，请联系系统管理员");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            if (corporation == null) {
                Transaction tx = session.beginTransaction();
                MySession.OnDelete(inloginInfo, session, operatersid);
                tx.commit();
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "查无此承运方信息");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
        } else {
            return NettyUtils.getTokenError(iskeepAlive);
        }

        String username = params.get("username");
        String password = params.get("password");
        String phone = params.get("phone");
        String name = params.get("name");

        Transaction tx = session.beginTransaction();
        String oldlogininfo = Utils.getJsonObject(inloginInfo).toString();
        String oldcorporation = Utils.getJsonObject(corporation).toString();
        if (username != null) {
            List<LoginInfo> loginInfos_checkusername = session
                    .createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and username = '" + username
                            + "' and sid <> " + inloginInfo.getSid(), LoginInfo.class)
                    .list();
            if (loginInfos_checkusername.size() > 1) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "数据库出错，请联系系统管理员");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            if (loginInfos_checkusername.size() == 1) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "该用户名已被使用");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            inloginInfo.setUsername(username);
            corporation.setUsername(username);
        }
        if (password != null) {
            if (Utils.getLengthOfObject(password) != 6) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "密码长度必须为6位");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            String salt = Utils.getRandomString(10);
            String pswDB = MD5Utils.MD5(username, password, salt);
            inloginInfo.setPassword(pswDB);
            inloginInfo.setSalt(salt);
        }
        if (phone != null) {
            if (!Utils.isMobileNum(phone)) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "手机号码格式不对");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            List<LoginInfo> loginInfos_checkphone = session
                    .createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and phone = '" + phone
                            + "' and sid <> " + inloginInfo.getSid(), LoginInfo.class)
                    .list();
            if (loginInfos_checkphone.size() > 1) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "数据库出错，请联系系统管理员");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            if (loginInfos_checkphone.size() == 1) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "该手机号已被使用");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            inloginInfo.setPhone(phone);
            corporation.setPhone(phone);
        }
        if (name != null) {
            corporation.setName(name);
        }
        try {
            ValidUtils.ValidationWithExp(inloginInfo);
            ValidUtils.ValidationWithExp(corporation);
        } catch (Exception e) {
            tx.rollback();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", 4);
            jsonObject.put("msg", e.getMessage());
            jsonObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
        }
        inloginInfo.updatetime(operatersid);
        corporation.updatetime(operatersid);
        MySession.OnUpdate(oldlogininfo, inloginInfo, session, operatersid);
        MySession.OnUpdate(oldcorporation, corporation, session, operatersid);
        tx.commit();
        JSONObject retObject = new JSONObject();
        retObject.put("status", 0);
        retObject.put("msg", "更新承运方信息成功");
        retObject.put("content", "");
        return NettyUtils.getResponse(iskeepAlive, retObject.toString());
    }

    private static FullHttpResponse OnUpdateManufacturer(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                                         Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
            throws JSONException, InstantiationException, IllegalAccessException {
        System.out.println("in LoginHandler-OnUpdateManufacturer");
        if (method != HttpMethod.POST) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        if (inloginInfo == null) {
            return NettyUtils.getTokenError(iskeepAlive);
        }
        int operatertype = inloginInfo.getType();
        Long operatersid = inloginInfo.getUsersid();
        Manufacturer manufacturer = null;
        if (operatertype == LoginInfo.TYPE_MANUFACTURER) {
            try {
                manufacturer = session.createQuery("from Manufacturer where datastatus = " + Bean.CREATED
                        + " and sid = " + inloginInfo.getUsersid(), Manufacturer.class).uniqueResult();
            } catch (Exception e) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "数据库信息错误，请联系系统管理员");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            if (manufacturer == null) {
                Transaction tx = session.beginTransaction();
                MySession.OnDelete(inloginInfo, session, operatersid);
                tx.commit();
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "查无此生产商信息");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
        } else {
            return NettyUtils.getTokenError(iskeepAlive);
        }

        String username = params.get("username");
        String password = params.get("password");
        String phone = params.get("phone");
        String name = params.get("name");

        Transaction tx = session.beginTransaction();
        String oldlogininfo = Utils.getJsonObject(inloginInfo).toString();
        String oldmanufacturer = Utils.getJsonObject(manufacturer).toString();
        if (username != null) {
            List<LoginInfo> loginInfos_checkusername = session
                    .createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and username = '" + username
                            + "' and sid <> " + inloginInfo.getSid(), LoginInfo.class)
                    .list();
            if (loginInfos_checkusername.size() > 1) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "数据库出错，请联系系统管理员");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            if (loginInfos_checkusername.size() == 1) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "该用户名已被使用");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            inloginInfo.setUsername(username);
            manufacturer.setUsername(username);
        }
        if (password != null) {
            if (Utils.getLengthOfObject(password) != 6) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "密码长度必须为6位");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            String salt = Utils.getRandomString(10);
            String pswDB = MD5Utils.MD5(username, password, salt);
            inloginInfo.setPassword(pswDB);
            inloginInfo.setSalt(salt);
        }
        if (phone != null) {
            if (!Utils.isMobileNum(phone)) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "手机号码格式不对");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            List<LoginInfo> loginInfos_checkphone = session
                    .createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and phone = '" + phone
                            + "' and sid <> " + inloginInfo.getSid(), LoginInfo.class)
                    .list();
            if (loginInfos_checkphone.size() > 1) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "数据库出错，请联系系统管理员");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            if (loginInfos_checkphone.size() == 1) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "该手机号已被使用");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            inloginInfo.setPhone(phone);
            manufacturer.setPhone(phone);
        }
        if (name != null) {
            manufacturer.setName(name);
        }
        try {
            ValidUtils.ValidationWithExp(inloginInfo);
            ValidUtils.ValidationWithExp(manufacturer);
        } catch (Exception e) {
            tx.rollback();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", 4);
            jsonObject.put("msg", e.getMessage());
            jsonObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
        }
        inloginInfo.updatetime(operatersid);
        manufacturer.updatetime(operatersid);
        MySession.OnUpdate(oldlogininfo, inloginInfo, session, operatersid);
        MySession.OnUpdate(oldmanufacturer, manufacturer, session, operatersid);
        tx.commit();
        JSONObject retObject = new JSONObject();
        retObject.put("status", 0);
        retObject.put("msg", "更新生产商信息成功");
        retObject.put("content", "");
        return NettyUtils.getResponse(iskeepAlive, retObject.toString());
    }

    private static FullHttpResponse OnUpdateGovernment(ChannelHandlerContext ctx, Session session, HttpMethod method,
                                                       Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo)
            throws JSONException, InstantiationException, IllegalAccessException {
        System.out.println("in LoginHandler-OnUpdateGovernment");
        if (method != HttpMethod.POST) {
            return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        if (inloginInfo == null) {
            return NettyUtils.getTokenError(iskeepAlive);
        }
        int operatertype = inloginInfo.getType();
        Long operatersid = inloginInfo.getUsersid();
        Government government = null;
        if (operatertype == LoginInfo.TYPE_GOVERNMENT) {
            try {
                government = session.createQuery("from Government where datastatus = " + Bean.CREATED + " and sid = "
                        + inloginInfo.getUsersid(), Government.class).uniqueResult();
            } catch (Exception e) {
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "数据库信息错误，请联系系统管理员");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
            if (government == null) {
                Transaction tx = session.beginTransaction();
                MySession.OnDelete(inloginInfo, session, operatersid);
                tx.commit();
                JSONObject retObject = new JSONObject();
                retObject.put("status", 4);
                retObject.put("msg", "查无此政府信息");
                retObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, retObject.toString());
            }
        } else {
            return NettyUtils.getTokenError(iskeepAlive);
        }

        String username = params.get("username");
        String password = params.get("password");
        String phone = params.get("phone");
        String name = params.get("name");

        Transaction tx = session.beginTransaction();
        String oldlogininfo = Utils.getJsonObject(inloginInfo).toString();
        String oldgovernment = Utils.getJsonObject(government).toString();
        if (username != null) {
            List<LoginInfo> loginInfos_checkusername = session
                    .createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and username = '" + username
                            + "' and sid <> " + inloginInfo.getSid(), LoginInfo.class)
                    .list();
            if (loginInfos_checkusername.size() > 1) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "数据库出错，请联系系统管理员");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            if (loginInfos_checkusername.size() == 1) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "该用户名已被使用");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            inloginInfo.setUsername(username);
            government.setUsername(username);
        }
        if (password != null) {
            if (Utils.getLengthOfObject(password) != 6) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "密码长度必须为6位");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            String salt = Utils.getRandomString(10);
            String pswDB = MD5Utils.MD5(username, password, salt);
            inloginInfo.setPassword(pswDB);
            inloginInfo.setSalt(salt);
        }
        if (phone != null) {
            if (!Utils.isMobileNum(phone)) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "手机号码格式不对");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            List<LoginInfo> loginInfos_checkphone = session
                    .createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and phone = '" + phone
                            + "' and sid <> " + inloginInfo.getSid(), LoginInfo.class)
                    .list();
            if (loginInfos_checkphone.size() > 1) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "数据库出错，请联系系统管理员");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            if (loginInfos_checkphone.size() == 1) {
                tx.rollback();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 4);
                jsonObject.put("msg", "该手机号已被使用");
                jsonObject.put("content", "");
                return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
            }
            inloginInfo.setPhone(phone);
            government.setPhone(phone);
        }
        if (name != null) {
            government.setName(name);
        }
        try {
            ValidUtils.ValidationWithExp(inloginInfo);
            ValidUtils.ValidationWithExp(government);
        } catch (Exception e) {
            tx.rollback();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", 4);
            jsonObject.put("msg", e.getMessage());
            jsonObject.put("content", "");
            return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
        }
        inloginInfo.updatetime(operatersid);
        government.updatetime(operatersid);
        MySession.OnUpdate(oldlogininfo, inloginInfo, session, operatersid);
        MySession.OnUpdate(oldgovernment, government, session, operatersid);
        tx.commit();
        JSONObject retObject = new JSONObject();
        retObject.put("status", 0);
        retObject.put("msg", "更新政府信息成功");
        retObject.put("content", "");
        return NettyUtils.getResponse(iskeepAlive, retObject.toString());
    }
}
