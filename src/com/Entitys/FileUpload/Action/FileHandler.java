package com.Entitys.FileUpload.Action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Entitys.Bean;
import com.Common.Utils.MySession;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.Utils;
import com.Common.Utils.ValidUtils;
import com.Entitys.Fareform.Dao.FareformDaoImpl;
import com.Entitys.Fareform.Entity.FareForm;
import com.Entitys.FileUpload.Entity.FileStore;
import com.Entitys.User.Entity.LoginInfo;
import com.Entitys.Warn.Dao.WarnDaoImpl;
import com.Entitys.Warn.Entity.Warn;

import Decoder.BASE64Decoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class FileHandler {
	private Integer logintype = null;
	private Long usersid = null;
	private Long corporationsid = null;
	public FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive) throws JSONException {
		if (!path[1].equals("upload")) {
			String token = params.get("token");
			if (token == null)
				return NettyUtils.getTokenError(iskeepAlive);
			LoginInfo loginInfo = null;
			try {
				loginInfo = session
						.createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and token = '" + token+"'",
								LoginInfo.class)
						.uniqueResult();
			} catch (Exception e) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "登录信息数据库出错");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(loginInfo == null)
				return NettyUtils.getTokenError(iskeepAlive);
			logintype = loginInfo.getType();
			usersid = loginInfo.getSid();
			corporationsid = loginInfo.getCorporationsid();
		}
		if (path[1].equals("upload")) {
			return OnUpload(ctx, path, session, method, params, iskeepAlive);
		} else if (path[1].equals("fareformimg_remove")) {
			return FareFormRemoveImage(ctx, path, session, method, params, iskeepAlive);
		} else if (path[1].equals("fareformimg_upload")) {
			return FareFormAddImage(ctx, path, session, method, params, iskeepAlive);
		} else if (path[1].equals("warnimg_remove")) {
			return WarnRemoveImage(ctx, path, session, method, params, iskeepAlive);
		} else if (path[1].equals("warnimg_upload")) {
			return WarnAddImage(ctx, path, session, method, params, iskeepAlive);
		} else if (path[1].equals("download")) {
			return OnDownload(ctx, path, session, method, params, iskeepAlive);
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}

	protected FullHttpResponse WarnRemoveImage(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive) throws JSONException {
		System.out.println("in UploadImageHandler-FareFormAddImage");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if(logintype != LoginInfo.TYPE_DRIVER)
			return NettyUtils.getTokenError(iskeepAlive); 
		String warnsidstr = params.get("warnsid");
		String filesidstr = params.get("filesid");
		if (warnsidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传异常信息sid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (filesidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传文件sid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long warnsid = null, filesid = null;
		try {
			warnsid = Long.parseLong(warnsidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "异常信息sid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		try {
			filesid = Long.parseLong(filesidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "文件sid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Warn warn = null;
		WarnDaoImpl warnDaoImpl = new WarnDaoImpl(session);
		try {
			warn = warnDaoImpl.getById(warnsid, true);
			if (warn == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此异常信息");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(warn.getStatus() == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "异常信息数据库出错,异常状态为空");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(warn.getStatus() != Warn.STATUS_RECEIVED){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此异常信息");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "异常信息数据库错误:"+e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		FileStore fileStore2 = null;
		try {
			fileStore2 = session
					.createQuery("from FileStore where datastatus = " + Bean.CREATED + " and sid = " + filesid,
							FileStore.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "文件数据库错误");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (fileStore2 == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "查无此文件");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}

		Transaction tx = session.beginTransaction();
		try {
			String oldwarn = Utils.getJsonObject(warn).toString();
			if (warn.removedriverimage(filesid)) {
				warn.updatetime(usersid);
				try {
					warnDaoImpl.update(warn, oldwarn, usersid);
				} catch (Exception e) {
					tx.rollback();
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", e.getMessage());
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
			} else {
				tx.rollback();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "删除图片失败");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			MySession.OnDelete(fileStore2, session, usersid);
			File file = new File(fileStore2.getSavepath());
			file.delete();
			tx.commit();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "删除图片成功");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "删除图片失败");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
	}

	protected FullHttpResponse WarnAddImage(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive) throws JSONException {
		System.out.println("in UploadImageHandler-WarnAddDriverImage");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if(logintype != LoginInfo.TYPE_DRIVER)
			return NettyUtils.getTokenError(iskeepAlive); 
		String warnsidstr = params.get("warnsid");
		String filestr = params.get("file");
		String filename = params.get("filename");
		if (warnsidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传异常信息sid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (filestr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传文件");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (filename == null) {
			filename = String.valueOf(Utils.getCurrenttimeMills());
		}
		Long warnsid = null;
		try {
			warnsid = Long.parseLong(warnsidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "异常信息sid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Warn warn = null;
		WarnDaoImpl warnDaoImpl = new WarnDaoImpl(session);
		try {
			warn = warnDaoImpl.getById(warnsid, true);
			if (warn == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此异常信息");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(warn.getStatus() == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "异常信息数据库出错,异常状态为空");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(warn.getStatus() != Warn.STATUS_RECEIVED){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此异常信息");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "异常信息数据库错误:"+e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}

		Transaction tx = session.beginTransaction();
		try {
			String[] ret = saveImg(filestr, "jpg");
			FileStore fileStore2 = new FileStore();
			fileStore2.setCorporationsid(corporationsid);
			fileStore2.setFilename(filename);
			fileStore2.setFiletype("jpg");
			fileStore2.setTime(Utils.getCurrenttimeMills());
			fileStore2.setUrl(ret[0]);
			fileStore2.setSavepath(ret[1]);
			try {
				ValidUtils.ValidationWithExp(fileStore2);
			} catch (Exception e) {
				tx.rollback();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", e.getMessage());
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			Long sid = (Long) session.save(fileStore2);
			session.flush();
//			Blob blob = Hibernate.getLobCreator(session).createBlob(filestr.getBytes());
//			FileStore fileStore = new FileStore();
//			fileStore.setFilename(filename);
//			fileStore.setFiletype("jpg");
//			fileStore.setTime(Utils.getCurrenttimeMills());
//			fileStore.setData_fm_480000(blob);
//			try {
//				Utils.ValidationWithExp(fileStore);
//			} catch (Exception e) {
//				tx.rollback();
//				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("status", 4);
//				jsonObject.put("msg", Utils.encoderUTF(e.getMessage()));
//				jsonObject.put("content", "");
//				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
//			}
//			Long sid = (Long) session.save(fileStore);
			String oldwarn = Utils.getJsonObject(warn).toString();
			warn.adddriverimage(sid);
			warn.updatetime(usersid);
			try {
				warnDaoImpl.update(warn, oldwarn, usersid);
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
			jsonObject.put("msg", "上传文件成功");
			JSONObject contentObject = new JSONObject();
			contentObject.put("sid", sid);
			contentObject.put("url", ret[0]);
			jsonObject.put("content", contentObject);
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "上传文件失败");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
	}

	protected FullHttpResponse FareFormRemoveImage(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive) throws JSONException {
		System.out.println("in UploadImageHandler-FareFormAddImage");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if(logintype != LoginInfo.TYPE_DRIVER)
			return NettyUtils.getTokenError(iskeepAlive); 
		String fareformsidstr = params.get("fareformsid");
		String filesidstr = params.get("filesid");
		if (fareformsidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传清单sid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (filesidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传文件sid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long fareformsid = null, filesid = null;
		try {
			fareformsid = Long.parseLong(fareformsidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "费用清单sid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		try {
			filesid = Long.parseLong(fareformsidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "文件sid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		FareForm fareForm = null;
		FareformDaoImpl fareformDaoImpl = new FareformDaoImpl(session);
		try {
			fareForm = fareformDaoImpl.getById(fareformsid, true);
//			fareForm = session.createQuery("from FareForm where datastatus = " + Bean.CREATED + " and sid = "
//					+ fareformsid + " and editable = " + FareForm.STATUS_EDITABLE, FareForm.class).uniqueResult();
			if (fareForm == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此费用清单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(fareForm.getEditable() == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "费用清单数据库出错，费用清单编辑状态为空");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(fareForm.getEditable() != FareForm.STATUS_EDITABLE){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此费用清单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "费用清单数据库错误");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		FileStore fileStore2 = null;
		try {
			fileStore2 = session
					.createQuery("from FileStore where datastatus = " + Bean.CREATED + " and sid = " + filesid,
							FileStore.class)
					.uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "文件数据库错误");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (fileStore2 == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "查无此文件");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}

		Transaction tx = session.beginTransaction();
		try {
			String oldfareform = Utils.getJsonObject(fareForm).toString();
			if (fareForm.removeimage(filesid)) {
				fareForm.updatetime(usersid);
				try {
					fareformDaoImpl.update(fareForm, oldfareform, usersid);
				} catch (Exception e) {
					tx.rollback();
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("status", 4);
					jsonObject.put("msg", e.getMessage());
					jsonObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
				}
			} else {
				tx.rollback();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "删除图片失败");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			MySession.OnDelete(fileStore2, session, usersid);
			File file = new File(fileStore2.getSavepath());
			file.delete();
			tx.commit();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "删除图片成功");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "删除图片失败");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
	}

	protected FullHttpResponse FareFormAddImage(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive) throws JSONException {
		System.out.println("in UploadImageHandler-FareFormAddImage");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		if(logintype != LoginInfo.TYPE_DRIVER)
			return NettyUtils.getTokenError(iskeepAlive); 
		String fareformsidstr = params.get("fareformsid");
		String filestr = params.get("file");
		String filename = params.get("filename");
		if (fareformsidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传清单sid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (filestr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传文件");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (filename == null) {
			filename = String.valueOf(Utils.getCurrenttimeMills());
		}
		Long fareformsid = null;
		try {
			fareformsid = Long.parseLong(fareformsidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "费用清单sid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		FareForm fareForm = null;
		FareformDaoImpl fareformDaoImpl = new FareformDaoImpl(session);
		try {
			fareForm = fareformDaoImpl.getById(fareformsid, true);
//			fareForm = session.createQuery("from FareForm where datastatus = " + Bean.CREATED + " and sid = "
//					+ fareformsid + " and editable = " + FareForm.STATUS_EDITABLE, FareForm.class).uniqueResult();
			if (fareForm == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "查无此费用清单");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(fareForm.getEditable() == null){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "费用清单数据库出错，费用清单编辑状态为空");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			if(fareForm.getEditable() != FareForm.STATUS_EDITABLE){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", "费用清单当前不能修改");
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
		} catch (Exception e) {
//			e.printStackTrace();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "费用清单数据库错误");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}

		Transaction tx = session.beginTransaction();
		try {
			String[] ret = saveImg(filestr, "jpg");
			FileStore fileStore2 = new FileStore();
			fileStore2.setCorporationsid(corporationsid);
			fileStore2.setFilename(filename);
			fileStore2.setFiletype("jpg");
			fileStore2.setTime(Utils.getCurrenttimeMills());
			fileStore2.setUrl(ret[0]);
			fileStore2.setSavepath(ret[1]);
			try {
				ValidUtils.ValidationWithExp(fileStore2);
			} catch (Exception e) {
				tx.rollback();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", Utils.encoderUTF(e.getMessage()));
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			Long sid = (Long) session.save(fileStore2);
			session.flush();
//			Blob blob = Hibernate.getLobCreator(session).createBlob(filestr.getBytes());
//			FileStore fileStore = new FileStore();
//			fileStore.setFilename(filename);
//			fileStore.setFiletype("jpg");
//			fileStore.setTime(Utils.getCurrenttimeMills());
//			fileStore.setData_fm_480000(blob);
//			try {
//				Utils.ValidationWithExp(fileStore);
//			} catch (Exception e) {
//				tx.rollback();
//				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("status", 4);
//				jsonObject.put("msg", Utils.encoderUTF(e.getMessage()));
//				jsonObject.put("content", "");
//				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
//			}
//			Long sid = (Long) session.save(fileStore);
			String oldfareform = Utils.getJsonObject(fareForm).toString();
			fareForm.addimage(sid);
			fareForm.updatetime(usersid);
			try {
				fareformDaoImpl.update(fareForm, oldfareform, usersid);
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
			jsonObject.put("msg", "上传文件成功");
			JSONObject contentObject = new JSONObject();
			contentObject.put("sid", sid);
			contentObject.put("url", ret[0]);
			jsonObject.put("content", contentObject);
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		} catch (Exception e) {
			tx.rollback();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "上传文件失败");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
	}

	protected FullHttpResponse OnUpload(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive) throws JSONException {
		System.out.println("in UploadImageHandler-OnUpload");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		String filestr = params.get("file");
		String filename = params.get("filename");
		String filetype = params.get("filetype");
		if (filetype == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请填写文件扩展名");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (filestr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传文件");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (filename == null) {
			filename = String.valueOf(Utils.getCurrenttimeMills());
		}
		Transaction tx = session.beginTransaction();
		try {
			String[] ret = saveImg(filestr, filetype);
			FileStore fileStore2 = new FileStore();
			fileStore2.setFilename(filename);
			fileStore2.setFiletype(filetype);
			fileStore2.setTime(Utils.getCurrenttimeMills());
			fileStore2.setUrl(ret[0]);
			fileStore2.setSavepath(ret[1]);
			try {
				ValidUtils.ValidationWithExp(fileStore2);
			} catch (Exception e) {
				tx.rollback();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", 4);
				jsonObject.put("msg", Utils.encoderUTF(e.getMessage()));
				jsonObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
			}
			Long sid = (Long) session.save(fileStore2);
//			Blob blob = Hibernate.getLobCreator(session).createBlob(filestr.getBytes());
//			FileStore fileStore = new FileStore();
//			fileStore.setFilename(filename);
//			fileStore.setFiletype(filetype);
//			fileStore.setTime(Utils.getCurrenttimeMills());
//			fileStore.setData_fm_480000(blob);
//			try {
//				Utils.ValidationWithExp(fileStore);
//			} catch (Exception e) {
//				tx.rollback();
//				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("status", 4);
//				jsonObject.put("msg", Utils.encoderUTF(e.getMessage()));
//				jsonObject.put("content", "");
//				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
//			}
//			Long sid = (Long) session.save(fileStore);
			tx.commit();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "上传文件成功");
			JSONObject contentObject = new JSONObject();
			contentObject.put("sid", sid);
			contentObject.put("url", ret[0]);
			jsonObject.put("content", contentObject);
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "上传文件失败:" + e.getMessage());
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
	}

	protected FullHttpResponse OnDownload(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive) throws JSONException {
		System.out.println("in UploadImageHandler-OnDownload");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		String sidstr = params.get("sid");
		if (sidstr == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传sid");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Long sid = null;
		try {
			sid = Long.parseLong(sidstr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "sid格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		FileStore fileStore2 = null;
		try {
			fileStore2 = session.createQuery("from FileStore where datastatus = "+Bean.CREATED+" and sid = " + sid, FileStore.class).uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "数据库出错，请联系系统管理员");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if (fileStore2 == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "查无此文件");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		} else {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "查询文件成功");
			jsonObject.put("content", fileStore2.getUrl());
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());

		}
	}
	
	private String[] saveImg(String data, String format) throws IOException{
		BASE64Decoder base64Decoder = new BASE64Decoder();
		byte[] filebytes = base64Decoder.decodeBuffer(data);
		String savepath = "//upload//";
		String savename = String.valueOf(System.currentTimeMillis())+"."+format;
		File dirfile = new File(System.getProperty("user.dir")+savepath);
		if(!dirfile.exists() || !dirfile.isDirectory())
			dirfile.mkdirs();
		File savefile = new File(System.getProperty("user.dir")+savepath+savename);
		FileOutputStream write = new FileOutputStream(savefile);
		write.write(filebytes);
		write.close();
		String[] ret = new String[2];
		ret[0] = "http://120.76.219.196:2333//upload//"+savename;
		ret[1] = savefile.getAbsolutePath();
		return ret;
	}
}

//protected FullHttpResponse OnDownload(ChannelHandlerContext ctx, String[] path, Session session,
//		HttpMethod method, Map<String, String> params, boolean iskeepAlive) throws JSONException {
//	System.out.println("in UploadImageHandler-OnDownload");
//	if (method != HttpMethod.GET) {
//		return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
//	}
//	String sidstr = params.get("sid");
//	if (sidstr == null) {
//		JSONObject jsonObject = new JSONObject();
//		jsonObject.put("status", 4);
//		jsonObject.put("msg", "请上传sid");
//		jsonObject.put("content", "");
//		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
//	}
//	Long sid = null;
//	try {
//		sid = Long.parseLong(sidstr);
//	} catch (Exception e) {
//		JSONObject jsonObject = new JSONObject();
//		jsonObject.put("status", 4);
//		jsonObject.put("msg", "sid格式不正确");
//		jsonObject.put("content", "");
//		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
//	}
//	FileStore fileStore = null;
//	try {
//		fileStore = session.createQuery("from FileStore where sid = " + sid, FileStore.class).uniqueResult();
//	} catch (Exception e) {
//		JSONObject jsonObject = new JSONObject();
//		jsonObject.put("status", 4);
//		jsonObject.put("msg", "数据库出错，请联系系统管理员");
//		jsonObject.put("content", "");
//		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
//	}
//	if (fileStore == null) {
//		JSONObject jsonObject = new JSONObject();
//		jsonObject.put("status", 4);
//		jsonObject.put("msg", "查无此文件");
//		jsonObject.put("content", "");
//		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
//	} else {
//		Blob blob = fileStore.getData_fm_480000();
//		byte[] bytes = null;
//		BufferedInputStream bis = null;
//		try {
//			bis = new BufferedInputStream(blob.getBinaryStream());
//			bytes = new byte[(int) blob.length()];
//			int len = bytes.length;
//			int offset = 0;
//			int read;
//			while (offset < len && (read = bis.read(bytes, offset, len - offset)) >= 0) {
//				offset += read;
//			}
//		} catch (Exception e) {
//			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("status", 4);
//			jsonObject.put("msg", "读取文件失败");
//			jsonObject.put("content", "");
//			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
//		} finally {
//			try {
//				if (bis != null) {
//					bis.close();
//					bis = null;
//				}
//			} catch (Exception e2) {
//				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("status", 4);
//				jsonObject.put("msg", "读取文件失败");
//				jsonObject.put("content", "");
//				return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
//			}
//		}
//		JSONObject jsonObject = new JSONObject();
//		jsonObject.put("status", 0);
//		jsonObject.put("msg", "查询文件成功");
//		jsonObject.put("content", new String(bytes));
//		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
//
//	}
//}
