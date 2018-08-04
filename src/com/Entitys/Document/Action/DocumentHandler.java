package com.Entitys.Document.Action;

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
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
import com.Entitys.Document.Dao.DocumentDaoImpl;
import com.Entitys.Document.Dao.ReadDurHistoryDaoImpl;
import com.Entitys.Document.Dao.TotalDurDaoImpl;
import com.Entitys.Document.Entity.Document;
import com.Entitys.Document.Entity.ReadDurHistory;
import com.Entitys.Document.Entity.TotalDur;
import com.Entitys.User.Entity.LoginInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class DocumentHandler {
	private Long operatesid, corporationsid;
	private Integer operateusertype;
	private DocumentDaoImpl documentDaoImpl;
	
	public FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (loginInfo == null)
			return NettyUtils.getTokenError(iskeepAlive);
		operateusertype = loginInfo.getType();
		operatesid = loginInfo.getUsersid();
		documentDaoImpl = new DocumentDaoImpl(session);
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
		} else if (path[1].equals("submitDuration")) {
			return OnSubmitDuration(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("queryDuration")) {
			return OnQueryDuration(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}
	
	private FullHttpResponse OnQueryDuration(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if(operateusertype != LoginInfo.TYPE_ADMIN && operateusertype != LoginInfo.TYPE_DRIVER){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		if(operateusertype == LoginInfo.TYPE_DRIVER && loginInfo.getIsDocAuthd() == LoginInfo.AUTHD_FALSE){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		Long driversid = null;
		if(operateusertype == LoginInfo.TYPE_ADMIN){
			String driversidstr = params.get("driversid");
			try {
				driversid = Long.parseLong(driversidstr);
			} catch (Exception e) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "driversid格式不正确");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		}else if(operateusertype == LoginInfo.TYPE_DRIVER){
			driversid = operatesid;
		}else {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		corporationsid = loginInfo.getCorporationsid();
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
		TotalDur totalDur = null;
		List<ReadDurHistory> readDurHistories = null;
		Long total = null;
		try {
			TotalDurDaoImpl totalDurDaoImpl = new TotalDurDaoImpl(session);
			List<QueryBean> totaldurQueryBean = new ArrayList<QueryBean>();
			totaldurQueryBean.add(new QueryBean(TotalDur.class.getName(), "totaldur", "driver_sid", driversid, QueryBean.TYPE_EQUAL));
			List<TotalDur> totalDurs = totalDurDaoImpl.getListBy(totaldurQueryBean, false, 2, 1);
			if(totalDurs.size() > 1)
				throw DaoException.DB_QUERY_EXCEPTION.newInstance("In DocumentHandler.getListBy.{数据库出错，一个驾驶员对应多个总阅读时长信息}");
			if(totalDurs.size() == 0){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "该驾驶员尚无阅读记录");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			totalDur = totalDurs.get(0);
			ReadDurHistoryDaoImpl readDurHistoryDaoImpl = new ReadDurHistoryDaoImpl(session);
			List<QueryBean> readdurhistoryQueryBean = new ArrayList<QueryBean>();
			readdurhistoryQueryBean.add(new QueryBean(ReadDurHistory.class.getName(), "readdurhistory", "driver_sid", driversid, QueryBean.TYPE_EQUAL));
			readDurHistories = readDurHistoryDaoImpl.getListBy(readdurhistoryQueryBean, false, intlimit, intpage);
			total = readDurHistoryDaoImpl.selectCount(readdurhistoryQueryBean, false);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询历史阅读记录成功");
		JSONObject contentJsonObject = new JSONObject();
		contentJsonObject.put("totaldur", totalDur.getTotaldur());
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
		for (int i = 0; i < readDurHistories.size(); i++) {
			dataArray.put(Utils.getJsonObject(readDurHistories.get(i)));
		}
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}
	
	private FullHttpResponse OnSubmitDuration(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if(operateusertype != LoginInfo.TYPE_DRIVER){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		if(loginInfo.getIsDocAuthd() == LoginInfo.AUTHD_FALSE){
			return NettyUtils.getTokenError(iskeepAlive);
		}
		corporationsid = loginInfo.getCorporationsid();
		String str_doc_sid = params.get("sid");
		Long doc_sid = null;
		try {
			doc_sid = Long.parseLong(str_doc_sid);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		String str_dur = params.get("duration");
		Long dur = null;
		try {
			dur = Long.parseLong(str_dur);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "dur格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
        Transaction tx = session.beginTransaction();
		try {
			Document document = documentDaoImpl.getById(doc_sid, true);
			if(!document.getCorporationsid().equals(corporationsid)){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此资料");
				jsonObject.put("content", Utils.getJsonObject(document));
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			//
			ReadDurHistoryDaoImpl readDurHistoryDaoImpl = new ReadDurHistoryDaoImpl(session);
			List<QueryBean> readdurhistoryQueryList = new ArrayList<QueryBean>();
			readdurhistoryQueryList.add(new QueryBean(ReadDurHistory.class.getName(), "readdurhistory", "driver_sid", operatesid, QueryBean.TYPE_EQUAL));
			readdurhistoryQueryList.add(new QueryBean(ReadDurHistory.class.getName(), "readdurhistory", "doc_sid", doc_sid, QueryBean.TYPE_EQUAL));
			List<ReadDurHistory> readDurHistories = readDurHistoryDaoImpl.getListBy(readdurhistoryQueryList, false, 2, 1);
            TotalDurDaoImpl totalDurDaoImpl = new TotalDurDaoImpl(session);
            List<QueryBean> totaldurQueryList = new ArrayList<QueryBean>();
            totaldurQueryList.add(new QueryBean(TotalDur.class.getName(), "totaldur", "driver_sid", operatesid, QueryBean.TYPE_EQUAL));
            List<TotalDur> totalDurs = totalDurDaoImpl.getListBy(totaldurQueryList, false, 2, 1);//////////////////
            //
			if(readDurHistories.size() > 1){
				throw DaoException.DB_QUERY_EXCEPTION.newInstance("In DocumentHandler.getListBy.{数据库出错，历史阅读时长信息外键重复}");
			}
			if(readDurHistories.size() == 0){
				ReadDurHistory readDurHistory = new ReadDurHistory(operatesid, doc_sid, dur);
                readDurHistory.createtime(operatesid);
                readDurHistory.setCorporationsid(corporationsid);
                readDurHistoryDaoImpl.insert(readDurHistory, operatesid);
            }else {
				ReadDurHistory readDurHistory = readDurHistories.get(0);
                String oldobject = Utils.getJsonObject(readDurHistory).toString();
                readDurHistory.setDur(dur + readDurHistory.getDur());
                readDurHistory.updatetime(operatesid);
                readDurHistoryDaoImpl.update(readDurHistory, oldobject, operatesid);////////////////
            }
			//
			if(totalDurs.size() > 1)
				throw DaoException.DB_QUERY_EXCEPTION.newInstance("In DocumentHandler.getListBy.{数据库出错，一个驾驶员对应多个总历史阅读时长信息}");
			if(totalDurs.size() == 0){
				TotalDur totalDur = new TotalDur(operatesid, dur);
				totalDur.setCorporationsid(corporationsid);
				totalDur.createtime(operatesid);
				totalDurDaoImpl.insert(totalDur, operatesid);
			}else {
				TotalDur totalDur = totalDurs.get(0);
				String oldobject = Utils.getJsonObject(totalDur).toString();
				totalDur.AddTotalDur(dur);
				totalDur.updatetime(operatesid);
				totalDurDaoImpl.update(totalDur, oldobject, operatesid);
			}
			tx.commit();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "上传阅读时间信息成功");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		} catch (Exception e) {
		    tx.rollback();
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
		if(operateusertype == LoginInfo.TYPE_DRIVER && loginInfo.getIsDocAuthd() == LoginInfo.AUTHD_FALSE){
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
			Document document = documentDaoImpl.getById(sid, true);
			if(!document.getCorporationsid().equals(corporationsid)){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此资料");
				jsonObject.put("content", Utils.getJsonObject(document));
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "查询学习资料信息成功");
			jsonObject.put("content", Utils.getJsonObject(document));
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
		if(operateusertype == LoginInfo.TYPE_DRIVER && loginInfo.getIsDocAuthd() == LoginInfo.AUTHD_FALSE){
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
		queryList.add(new QueryBean(Document.class.getName(), "document", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
		if(name != null)
			queryList.add(new QueryBean(Document.class.getName(), "document", "name", name, QueryBean.TYPE_EQUAL));
		if(sid != null)
			queryList.add(new QueryBean(Document.class.getName(), "document", "sid", sid, QueryBean.TYPE_EQUAL));
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
			List<Document> documentList = documentDaoImpl.getListBy(queryList, true, intlimit, intpage);
			Long total = documentDaoImpl.selectCount(queryList, true);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "查询学习资料成功");
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
			for (int i = 0; i < documentList.size(); i++) {
				JSONObject tmpjsonObject = Utils.getJsonObject(documentList.get(i));
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
		Document document = null;
		try {
			document = documentDaoImpl.getById(sid, true);
			if(!document.getCorporationsid().equals(corporationsid)){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "查无此学习资料");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			documentDaoImpl.delete(document, operatesid);
			tx.commit();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 0);
			retObject.put("msg", "删除学习资料成功");
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
		try {
			Document document = documentDaoImpl.getById(sid, true);
			if(!document.getCorporationsid().equals(corporationsid)){
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "查无此学习资料");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			String olddoc = Utils.getJsonObject(document).toString();
			String title = params.get("title");
			if(title != null)
				document.setTitle(title);
			String desc = params.get("desc");
			if(desc != null)
				document.setDesc(desc);
			String content = params.get("content");
			if(content != null)
				document.setContent(content);
			document.updatetime(operatesid);
			documentDaoImpl.update(document, olddoc, operatesid);
			tx.commit();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 0);
			retObject.put("msg", "修改学习资料成功");
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
		String title = params.get("title");
		String desc = params.get("desc");
		String content = params.get("content");
		Transaction tx = session.beginTransaction();
		Document document = new Document(corporationsid, title, desc, content);
		document.createtime(operatesid);
		Long sid = null;
		try {
			sid = documentDaoImpl.insert(document, operatesid);
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
		retObject.put("msg", "上传学习资料成功");
		retObject.put("content", sid);
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}
}
