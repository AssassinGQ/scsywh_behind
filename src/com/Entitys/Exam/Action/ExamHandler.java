package com.Entitys.Exam.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Daos.QueryBean;
import com.Common.Exceptions.DaoException;
import com.Common.Utils.MySession;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
import com.Entitys.Driver.Dao.DriverDaoImpl;
import com.Entitys.Driver.Entity.Driver;
import com.Entitys.Exam.Dao.ExamDaoImpl;
import com.Entitys.Exam.Entity.Exam;
import com.Entitys.Exam.Entity.ScoreHistory;
import com.Entitys.User.Entity.LoginInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class ExamHandler {
	private Long operatesid, corporationsid;
	private Integer operateusertype;
	private ExamDaoImpl examDaoImpl;
	
	public FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (loginInfo == null)
			return NettyUtils.getTokenError(iskeepAlive);
		operateusertype = loginInfo.getType();
		operatesid = loginInfo.getUsersid();
		examDaoImpl = new ExamDaoImpl(session);
		if (path[1].equals("create")) {
			return OnCreate(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("update")) {
			return OnUpdate(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("delete")) {
			return OnDelete(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("getList")) {
			return OnGetList(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("getById")) {
			return OnGetById(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("submit")) {
			return OnSubmit(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if(path[1].equals("queryScore")){
			return OnQueryScore(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}
	
	private FullHttpResponse OnQueryScore(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if(operateusertype == LoginInfo.TYPE_DRIVER && loginInfo.getIsExmAuthd() == LoginInfo.AUTHD_FALSE){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		Long driversid = null;
		if(operateusertype == LoginInfo.TYPE_ADMIN){
			corporationsid = loginInfo.getCorporationsid();
			String driversidstr = params.get("driversid");
			if(driversidstr != null){
				try {
					driversid = Long.parseLong(driversidstr);
				} catch (Exception e) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "driversid格式不正确");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
			}
		}else if(operateusertype == LoginInfo.TYPE_DRIVER){
			corporationsid = loginInfo.getCorporationsid();
			driversid = operatesid;
		}else {
			return NettyUtils.getTokenError(iskeepAlive);
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
		if(driversid != null){
			DriverDaoImpl driverDaoImpl = new DriverDaoImpl(session);
			List<QueryBean> driverqueryList = new ArrayList<QueryBean>();
			driverqueryList.add(new QueryBean(Driver.class.getName(), "driver", "sid", driversid, QueryBean.TYPE_EQUAL));
			long drivertotal = driverDaoImpl.selectCount(driverqueryList, true);
			if(drivertotal == 0){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有相应的驾驶员信息");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if(drivertotal > 1){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "数据库出错，驾驶员信息主键重复");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		}
		List<Map<String, Object>> result = null;
		Long total = null;
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		queryList.add(new QueryBean(ScoreHistory.class.getName(), "scorehistory", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
		if(driversid != null)
			queryList.add(new QueryBean(ScoreHistory.class.getName(), "scorehistory", "user_sid", driversid, QueryBean.TYPE_EQUAL));
		try {
			result = examDaoImpl.fetchScoreHistory(queryList, true, intlimit, intpage);
			total = examDaoImpl.selectScoreHistoryCount(queryList, true);
			if(result == null){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "没有符合条件的结果");
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
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询历史成绩成功");
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
			Map<String, Object> item = result.get(i);
			ScoreHistory scoreHistory = (ScoreHistory) item.get("Scorehistory");
			Driver driver = (Driver) item.get("Driver");
			Exam exam = (Exam) item.get("Exam");
			JSONObject scorehistoryObject = null;
			if(scoreHistory == null)
				scorehistoryObject = new JSONObject();
			else
				scorehistoryObject = Utils.getJsonObject(scoreHistory);
			scorehistoryObject.put("examname", exam == null ? "null" : exam.getName());
			scorehistoryObject.put("drivername", driver == null ? "null" : driver.getName());
			dataArray.put(scorehistoryObject);
		}
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	private FullHttpResponse OnSubmit(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if(operateusertype != LoginInfo.TYPE_DRIVER){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		if(loginInfo.getIsExmAuthd() == LoginInfo.AUTHD_FALSE){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		corporationsid = loginInfo.getCorporationsid();
		String str_sid = params.get("sid");
		if(str_sid == null){
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid不能为空");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(str_sid);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		String answer = params.get("answer");
		JSONObject answers_user = null;
		try {
			answers_user = new JSONObject(answer);			
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "answer格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Transaction tx = session.beginTransaction();
		try {
			Exam exam = examDaoImpl.getById(sid, true);
			if(!exam.getCorporationsid().equals(corporationsid)){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "查无此试卷");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			Double score_total = exam.getScore(answers_user);
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(new QueryBean(ScoreHistory.class.getName(), "scorehistory", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(ScoreHistory.class.getName(), "scorehistory", "user_sid", operatesid, QueryBean.TYPE_EQUAL));
			List<ScoreHistory> scoreHistories = examDaoImpl.getScoreHistory(queryList, true, null, null);
			if(scoreHistories.size() > 1){
				throw DaoException.DB_QUERY_EXCEPTION.newInstance("In ExamHandler.getScoreHistory.{数据库出错，历史成绩信息外键重复}");
			}
			if(scoreHistories.size() == 0){
				ScoreHistory scoreHistory = new ScoreHistory(operatesid, sid, score_total);
				scoreHistory.setCorporationsid(corporationsid);
				scoreHistory.createtime(operatesid);
				session.save(scoreHistory);
				MySession.AfterSave(scoreHistory, session, operatesid);
			}else {
				ScoreHistory scoreHistory = scoreHistories.get(0);
				String oldscorehistory = Utils.getJsonObject(scoreHistory).toString();
				scoreHistory.setScore(score_total);
				scoreHistory.updatetime(operatesid);
				session.update(scoreHistory);
				MySession.AfterUpdate(oldscorehistory, scoreHistory, session, operatesid);
			}
			tx.commit();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "上传考试结果成功");
			JSONObject contentObject = new JSONObject();
			contentObject.put("score", score_total);
			jsonObject.put("content", contentObject);
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
	}

	private FullHttpResponse OnGetById(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if(operateusertype != LoginInfo.TYPE_ADMIN && operateusertype != LoginInfo.TYPE_DRIVER){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		if(operateusertype == LoginInfo.TYPE_DRIVER && loginInfo.getIsExmAuthd() == LoginInfo.AUTHD_FALSE){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		corporationsid = loginInfo.getCorporationsid();
		String str_sid = params.get("sid");
		Long sid = null;
		try {
			sid = Long.parseLong(str_sid);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			Exam exam = examDaoImpl.getById(sid, true);
			if(!exam.getCorporationsid().equals(corporationsid)){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "查无此试卷");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "查询订单信息成功");
			jsonObject.put("content", Utils.getJsonObject(exam));
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
	}

	private FullHttpResponse OnGetList(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if(operateusertype != LoginInfo.TYPE_ADMIN && operateusertype != LoginInfo.TYPE_DRIVER){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		if(operateusertype == LoginInfo.TYPE_DRIVER && loginInfo.getIsExmAuthd() == LoginInfo.AUTHD_FALSE){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		corporationsid = loginInfo.getCorporationsid();
		String sidstr = params.get("sid");
		Long sid = null;
		if(sidstr != null){
			try {
				sid = Long.parseLong(sidstr);
			} catch (Exception e) {
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("status", 4);
				jsonObject2.put("msg", "sid格式不正确");
				jsonObject2.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
			}			
		}
		String name = params.get("name");
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		queryList.add(new QueryBean(Exam.class.getName(), "exam", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
		if(name != null)
			queryList.add(new QueryBean(Exam.class.getName(), "exam", "name", name, QueryBean.TYPE_EQUAL));
		if(sid != null)
			queryList.add(new QueryBean(Exam.class.getName(), "exam", "sid", sid, QueryBean.TYPE_EQUAL));
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
		try {
			List<Exam> examList = examDaoImpl.getListBy(queryList, true, intlimit, intpage);
			Long total = examDaoImpl.selectCount(queryList, true);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "查询试卷成功");
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
			for (int i = 0; i < examList.size(); i++) {
				JSONObject tmpjsonObject = Utils.getJsonObject(examList.get(i));
				dataArray.put(tmpjsonObject);
			}
			contentJsonObject.put("data", dataArray);
			jsonObject.put("content", contentJsonObject);
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
	}

	private FullHttpResponse OnDelete(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if(operateusertype != LoginInfo.TYPE_ADMIN){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		corporationsid = loginInfo.getCorporationsid();
		String str_sid = params.get("sid");
		Long sid = null;
		try {
			sid = Long.parseLong(str_sid);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Transaction tx = session.beginTransaction();
		Exam exam = null;
		try {
			exam = examDaoImpl.getById(sid, true);
			if(exam == null){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "查无此试卷");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if(!exam.getCorporationsid().equals(corporationsid)){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "查无此试卷");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			examDaoImpl.delete(exam, operatesid);
			tx.commit();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 0);
			retObject.put("msg", "删除试卷成功");
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

	private FullHttpResponse OnUpdate(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if(operateusertype != LoginInfo.TYPE_ADMIN){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		corporationsid = loginInfo.getCorporationsid();
		String str_sid = params.get("sid");
		Long sid = null;
		try {
			sid = Long.parseLong(str_sid);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Transaction tx = session.beginTransaction();
		Exam exam = null;
		try {
			exam = examDaoImpl.getById(sid, true);
			if(!exam.getCorporationsid().equals(corporationsid)){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "查无此试卷");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			String oldexam = Utils.getJsonObject(exam).toString();
			String name = params.get("name");
			if(name != null)
				exam.setName(name);
			String desc = params.get("desc");
			if(desc != null)
				exam.setDesc(desc);
			String questions = params.get("questions");
			if(questions != null)
				exam.setQuestions(questions);
			exam.updatetime(operatesid);
			examDaoImpl.update(exam, oldexam, operatesid);
			tx.commit();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 0);
			retObject.put("msg", "修改试卷成功");
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

	private FullHttpResponse OnCreate(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if(operateusertype != LoginInfo.TYPE_ADMIN){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		corporationsid = loginInfo.getCorporationsid();
		String name = params.get("name");
		String desc = params.get("desc");
		String questions = params.get("questions");
		Transaction tx = session.beginTransaction();
		Exam exam = new Exam();
		exam.setCorporationsid(corporationsid);
		exam.setName(name);
		exam.setDesc(desc);
		exam.setQuestions(questions);
		exam.createtime(operatesid);
		Long sid = null;
		try {
			sid = examDaoImpl.insert(exam, operatesid);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "上传试卷成功");
		retObject.put("content", sid);
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}
}
