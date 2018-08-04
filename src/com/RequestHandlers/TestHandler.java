package com.RequestHandlers;

import com.Common.Daos.QueryBean;
import com.Common.Entitys.Bean;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.PushUtils;
import com.Common.Utils.Utils;
import com.Entitys.Admin.Entity.Admin;
import com.Entitys.Order.Dao.OrderDaoImpl;
import com.Entitys.Order.Entity.Order;
import com.Entitys.Truck.Dao.TruckDaoImpl;
import com.Entitys.Truck.Entity.Truck;
import com.Entitys.Trucklog.Entity.TruckLog;
import com.Entitys.User.Entity.LoginInfo;
import com.Entitys.Warn.Entity.Warn;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestHandler {
	private static long ordersid = 469409777277992960L;
	public static String testcid = "96fcf828357a8ebd3bafdc11016b609e";
	public static FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws Exception {
		if (path[1].equals("push")) {// 测试用
			return OnPush(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("getTruckLogReq")) {
			return getTruckLogReq(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("getTruckLogResp")) {
			return getTruckLogResp(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("locktest")) {
			return setlock(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("mytest")) {
			return MyTest(ctx, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("resetOrder")) {
			return resetOrder(ctx, session, method, params, iskeepAlive, loginInfo);
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}

	private static FullHttpResponse resetOrder(ChannelHandlerContext ctx, Session session, HttpMethod method,
											Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo) throws Exception {
		System.out.println("in LockHandler-resetOrder");
//		if (method != HttpMethod.GET) {
//			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
//		}
		if(!inloginInfo.getUsername().equals("admintest")) {
			return NettyUtils.getTokenError(iskeepAlive);
		}
		String orderstatusstr = params.get("orderstatus");
		int orderstatus;
		try{
			orderstatus = Integer.parseInt(orderstatusstr);
		}catch (Exception e){
			orderstatus = 0;
		}
		OrderDaoImpl orderDaoImpl = new OrderDaoImpl(session);
		Order order = orderDaoImpl.getById(ordersid, true);
		if(order == null){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "操作失败");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
//		Transaction tx = session.beginTransaction();
//		String oldobject = Utils.getJsonObject(order).toString();
//		order.setOrderstatus(orderstatus);
////		order.setOrderstatus(Order.STATUS_RETURNED);
//		orderDaoImpl.update(order, oldobject, Long.parseLong("0"));
//		tx.commit();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "操作成功");
		jsonObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}
	
	private static FullHttpResponse setlock(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo) throws Exception {
		System.out.println("in LockHandler-setlock");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		String trucknumber = params.get("trucknumber");
		if(trucknumber == null){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传车牌号");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		String typestr = params.get("type");
		if(typestr == null){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请上传请求类型");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Integer type = null;
		try {
			type = Integer.parseInt(typestr);
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请求类型格式不正确");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		Truck truck = null;
		try {
			truck = session.createQuery("from Truck where datastatus ="+Bean.CREATED+" and trucknumber = '"+trucknumber+"'", Truck.class).uniqueResult();
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "多辆车有相同车牌号");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		if(truck == null){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "查无此车");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
		JSONObject pushObject = new JSONObject();
		pushObject.put("type", "11");
		//pushObject.put("sid", null);
		pushObject.put("isTest", "1");
		pushObject.put("operate", String.valueOf(type));
		String pushcontent = pushObject.toString();
		if(PushUtils.Push2Truck(truck.getCid(), pushcontent)){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 0);
			jsonObject.put("msg", "请求成功");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());			
		}else {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("status", 4);
			jsonObject.put("msg", "请求失败");
			jsonObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
		}
	}

	private static FullHttpResponse MyTest(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo) throws Exception {
		System.out.println("in TestHandler-MyTest");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		String type = params.get("type");
		if(type.equals("create")){
//			Transaction tx = session.beginTransaction();
//			T_person t_person = new T_person();
//			T_idcard t_idcard = new T_idcard();
//			T_group t_group = new T_group();
//			t_group.setName("6舍");
//			t_idcard.setName("齐孝勇身份证");
//			t_idcard.setT_person(t_person);
//			t_person.setName("齐孝勇");
//			t_person.setT_idcard(t_idcard);
//			t_person.setT_group(t_group);
//			session.save(t_group);
//			tx.commit();
			Transaction tx = session.beginTransaction();
			Admin admin = new Admin();
			admin.setUsername("asd");
			session.save(admin);
			tx.commit();
			return NettyUtils.getResponse(iskeepAlive, "ok");
		}else if(type.equals("update")){
//			Transaction tx = session.beginTransaction();
//			T_person t_person = session.get(T_person.class, Long.parseLong("1"));
//			T_idcard t_idcard = t_person.getT_idcard();
//			t_person.setName("qixiaoyong");
//			session.update(t_idcard);
//			tx.commit();
			return NettyUtils.getResponse(iskeepAlive, "ok");
		}else if(type.equals("delete")){
//			Transaction tx = session.beginTransaction();
//			T_person t_person = session.get(T_person.class, Long.parseLong("1"));
//			session.delete(t_person);
//			tx.commit();
			return NettyUtils.getResponse(iskeepAlive, "ok");
		}else if(type.equals("query")){
//			Transaction tx = session.beginTransaction();
//			T_person t_person = session.get(T_person.class, Long.parseLong("2"));
//			T_idcard t_idcard = t_person.getT_idcard();
//			T_idcard t_idcard = session.get(T_idcard.class, Long.parseLong("10"));
//			T_person t_person = t_idcard.getT_person();
//			T_group t_group = t_person.getT_group();
//			Set<T_person> persons = t_group.getPersons();
//			System.err.println(t_person.getName());
//			System.err.println(t_idcard.getName());
//			System.err.println(t_group.getName());
//			Iterator<T_person> iterator = persons.iterator();
//			while(iterator.hasNext()){
//				System.err.println(iterator.next().getName());
//			}
//			tx.commit();
			Order order = new Order();
			order.setSid(12345454L);
			System.err.println(Utils.getJsonObject(order));
			return NettyUtils.getResponse(iskeepAlive, "ok");
		} else if(type.equals("annotation")){
//			Field[] fields = T_group.class.getDeclaredFields();
//			for (int i = 0; i < fields.length; i++) {
//				Field field = fields[i];
//				field.setAccessible(true);
//				Valid valid = field.getAnnotation(Valid.class);
//				String fieldname = field.getName();
//				if(valid == null){
//					System.err.println(fieldname+":"+null);
//				}else {
//					boolean needvalid = valid.NeedValid();
//					ValidType validType = valid.Type();
//					int length = valid.Length();
//					System.err.println(fieldname+":needvalid="+needvalid+",validtype="+validType+",length="+length);
//				}
//			}
			return NettyUtils.getResponse(iskeepAlive, "ok");
		} else if(type.equals("daotest")){
//			Map<String, Object> ret = new HashMap<String, Object>();
//			OrderDaoImpl orderDaoImpl = new OrderDaoImpl(session);
//			Map<String, String> paramMap = new HashMap<String, String>();
//			paramMap.put("createdat", "1508727783945");
//			paramMap.put("trucksid", "2345");
//			paramMap.put("loadaddr", "ssss");
//			paramMap.put("loadaddddr", "ssss");
			Transaction tx = session.beginTransaction();
			try {
				//insert
//				Order order = new Order();
//				order.setBuyersid_fm_10(11L);
//				System.err.println(orderDaoImpl.insert(order));
				//getById
//				Order order = orderDaoImpl.getById(371973665983692800L, true);
//				System.err.println(order);
				//update
//				Utils.createtime("0", order);
//				System.err.println(orderDaoImpl.update(order));
				//fetchById
//				Map<String, Object> result = new HashMap<String, Object>();
//				orderDaoImpl.fetchById(371973665983692800L, result, true, Buyer.class);
//				Order order = (Order) result.get("Order");
//				Buyer buyer = (Buyer) result.get("Buyer");
//				System.err.println(order);
//				System.err.println(buyer);
				//getListBy
//				List<Order> result = orderDaoImpl.getListBy(paramMap, true);
//				for(int i = 0; i < result.size(); i++){
//					Order order = result.get(i);
//					System.err.println(order);
//				}
				//fetchListBy
//				List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
//				orderDaoImpl.fetchListBy(paramMap, result, true, Buyer.class);
//				for(int i = 0; i < result.size(); i++){
//					Map<String, Object> tmpret = result.get(i);
//					Order order = (Order) tmpret.get("Order");
//					Buyer buyer = (Buyer) tmpret.get("Buyer");
//					System.err.println(order);
//					System.err.println(buyer);
//				}
				//selectCount
//				System.err.println(orderDaoImpl.selectCount(paramMap, true));
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				e.printStackTrace();
			}
//			Order order = (Order) ret.get("Order");
//			Truck truck = (Truck) ret.get("Truck");
//			System.err.println(order.toString());
//			System.err.println(truck);
			return NettyUtils.getResponse(iskeepAlive, "ok");
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}
	
	private static FullHttpResponse getTruckLogReq(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo) throws JSONException {
		System.out.println("in TestHandler-getTruckLogReq");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		String trucknumber = params.get("trucknumber");
		if(trucknumber == null){
			return NettyUtils.getResponse(iskeepAlive, "请上传车牌号");
		}
		Truck truck = null;
		try {
			truck = session.createQuery("from Truck where datastatus = "+Bean.CREATED+" and trucknumber = '"+trucknumber+"'", Truck.class).uniqueResult();
		} catch (Exception e) {
			return NettyUtils.getResponse(iskeepAlive, "车辆数据库错误，相同的车牌号");
		}
		if(truck == null)
			return NettyUtils.getResponse(iskeepAlive, "车牌号对应车辆不存在");
		JSONObject pushObject = new JSONObject();
		pushObject.put("type", "30");
		String pushcontent = pushObject.toString();
		PushUtils.Push2Truck(truck.getCid(), pushcontent);
		return NettyUtils.getResponse(iskeepAlive, "ok");
	}

	private static FullHttpResponse getTruckLogResp(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo) throws JSONException {
		System.out.println("in TestHandler-getTruckLogResp");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
//		OrderDaoImpl orderDaoImpl = new OrderDaoImpl(session);
		TruckDaoImpl truckDaoImpl = new TruckDaoImpl(session);
		// 获得输入，并检查/////////////////////////////////////////////////////////
		String trucknumber = params.get("trucknumber");
		if (trucknumber == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传车牌号");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// time
		String timestr = params.get("time");
		if (timestr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传时间");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long time = null;
		try {
			time = Long.parseLong(timestr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "时间格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// distance
		String distancestr = params.get("distance");
		if (distancestr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传里程数（千米）");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Double distance = null;
		try {
			distance = Double.parseDouble(distancestr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "里程数格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// gpsx, gpsy
		String gpsxstr = params.get("gpsx");
		String gpsystr = params.get("gpsy");
		Double gpsx = null, gpsy = null;
		if (gpsxstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传GPS坐标");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (gpsystr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传GPS坐标");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			gpsx = Double.parseDouble(gpsxstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "GPS坐标格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			gpsy = Double.parseDouble(gpsystr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "GPS坐标格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// speed
		String speedstr = params.get("speed");
		if (speedstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传时速");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Double speed = null;
		try {
			speed = Double.parseDouble(speedstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "时速格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// fuelvol
		String fuelvolstr = params.get("fuelvol");
		if (fuelvolstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传油量信息");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Double fuelvol = null;
		try {
			fuelvol = Double.parseDouble(fuelvolstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "油量格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// lefttirepressure, rightirepressure
		String lefttirepressurestr = params.get("lefttirepressure");
		String righttirepressurestr = params.get("righttirepressure");
		String lefttiretempstr = params.get("lefttiretemp");
		String righttiretempstr = params.get("righttiretemp");
		Double lefttirepressure = null, righttirepressure = null;
		Double lefttiretemp = null, righttiretemp = null;
		if (lefttirepressurestr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传胎压信息");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			lefttirepressure = Double.parseDouble(lefttirepressurestr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "胎压格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (righttirepressurestr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传胎压信息");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			righttirepressure = Double.parseDouble(righttirepressurestr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "胎压格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (lefttiretempstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传胎温信息");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			lefttiretemp = Double.parseDouble(lefttiretempstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "胎温格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (righttiretempstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传胎温信息");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		try {
			righttiretemp = Double.parseDouble(righttiretempstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "胎温格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		String lock = params.get("lock");
		if (lock == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传锁信息");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		String posture = params.get("posture");
		// haswarn
		String haswarnstr = params.get("haswarn");
		if (haswarnstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传是否有异常");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Integer haswarn = null;
		try {
			haswarn = Integer.parseInt(haswarnstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "是否有异常标志位格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		if (haswarn != TruckLog.HASWARN_NO && haswarn != TruckLog.HASWARN_YES) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "是否有异常标志位格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		// warntype
		String warntypestr = params.get("warntype");
		Integer warntype = null;
		if (haswarn == TruckLog.HASWARN_YES) {
			if (warntypestr == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "请上传异常类型");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			try {
				warntype = Integer.parseInt(warntypestr);
			} catch (Exception e) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "异常类型格式不正确");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (warntype < Warn.WARNTYPE_LOCK || warntype > Warn.WARNTYPE_OVERLOAD) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "异常类型格式不正确");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		}
		String warnvalue = params.get("warnvalue");
		Long upimage1sid = null, upimage2sid = null, upimage3sid = null;
		if (haswarn == TruckLog.HASWARN_YES) {
			String upimg1sidstr = params.get("snapshot1");
			String upimg2sidstr = params.get("snapshot2");
			String upimg3sidstr = params.get("snapshot3");
			if (upimg1sidstr != null) {
				try {
					upimage1sid = Long.parseLong(upimg1sidstr);
				} catch (Exception e) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "图片sid格式不正确");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				try {
					Long total = session.createQuery("select count(*) from FileStore where datastatus = " + Bean.CREATED
							+ " and sid = " + upimage1sid, Long.class).uniqueResult();
					if (total == 0)
						throw new Exception();
					if (total > 1) {
						throw new Exception();
					}
				} catch (Exception e) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "图片尚未上传");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
			}
			if (upimg2sidstr != null) {
				try {
					upimage2sid = Long.parseLong(upimg2sidstr);
				} catch (Exception e) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "图片sid格式不正确");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				try {
					Long total = session.createQuery("select count(*) from FileStore where datastatus = " + Bean.CREATED
							+ " and sid = " + upimage2sid, Long.class).uniqueResult();
					if (total == 0)
						throw new Exception();
					if (total > 1) {
						throw new Exception();
					}
				} catch (Exception e) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "图片尚未上传");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
			}
			if (upimg3sidstr != null) {
				try {
					upimage3sid = Long.parseLong(upimg3sidstr);
				} catch (Exception e) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "图片sid格式不正确");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
				try {
					Long total = session.createQuery("select count(*) from FileStore where datastatus = " + Bean.CREATED
							+ " and sid = " + upimage3sid, Long.class).uniqueResult();
					if (total == 0)
						throw new Exception();
					if (total > 1) {
						throw new Exception();
					}
				} catch (Exception e) {
					JSONObject retObject = new JSONObject();
					retObject.put("status", 4);
					retObject.put("msg", "图片尚未上传");
					retObject.put("content", "");
					return NettyUtils.getResponse(iskeepAlive, retObject.toString());
				}
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		Order order = null;
//		Driver driver = null;
//		Corporation corporation = null;
//		Truck truck = null;
//		try {
//			List<QueryBean> queryList = new ArrayList<QueryBean>();
////			queryList.add(new QueryBean(Order.class.getName(), "orderp", "trucknumber", trucknumber, QueryBean.TYPE_EQUAL));
//			queryList.add(new QueryBean(Truck.class.getName(), "truck", "trucknumber", trucknumber, QueryBean.TYPE_EQUAL));
//			JSONArray orderstatusArray = new JSONArray();
//			orderstatusArray.put(Order.STATUS_DISTRIBUTED);
//			orderstatusArray.put(Order.STATUS_RECEIVED);
//			orderstatusArray.put(Order.STATUS_LOADED);
//			orderstatusArray.put(Order.STATUS_UNLOADED);
//			queryList.add(new QueryBean(Order.class.getName(), "orderp", "orderstatus", orderstatusArray.toString(), QueryBean.TYPE_JSONARRAY));
//			List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
//			orderDaoImpl.fetchListBy(queryList, result, true, Truck.class, Driver.class, Corporation.class);
//			if(result.size() > 1){
//				JSONObject retObject = new JSONObject();
//				retObject.put("status", 4);
//				retObject.put("msg", "订单数据库异常，一辆拖车存在多个活跃的订单");
//				retObject.put("content", "");
//				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
//			}
//			if(result.size() == 0){
//				JSONObject retObject = new JSONObject();
//				retObject.put("status", 4);
//				retObject.put("msg", "该车没有在执行中的订单");
//				retObject.put("content", "");
//				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
//			}
//			Map<String, Object> tmpret = result.get(0);
//			order = (Order) tmpret.get("Order");
//			driver = (Driver) tmpret.get("Driver");
//			corporation = (Corporation) tmpret.get("Corporation");
//			truck = (Truck) tmpret.get("Truck");
//			if(order == null){
//				JSONObject retObject = new JSONObject();
//				retObject.put("status", 4);
//				retObject.put("msg", "该车没有在执行中的订单");
//				retObject.put("content", "");
//				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
//			}
//			if(truck == null || corporation == null || driver == null){
//				JSONObject retObject = new JSONObject();
//				retObject.put("status", 4);
//				retObject.put("msg", "数据库出错，订单对应的关联信息丢失");
//				retObject.put("content", "");
//				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
//			}
//		} catch (Exception e) {
//			JSONObject retObject = new JSONObject();
//			retObject.put("status", 4);
//			retObject.put("msg", e.getMessage());
//			retObject.put("content", "");
//			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
//		}
		Truck truck = null;
		try{
			List<QueryBean> queryList = new ArrayList<QueryBean>();
//			queryList.add(new QueryBean(Order.class.getName(), "orderp", "trucknumber", trucknumber, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(Truck.class.getName(), "truck", "trucknumber", trucknumber, QueryBean.TYPE_EQUAL));
			truck = truckDaoImpl.getByTrucknumber(trucknumber, true);
		}catch(Exception e){e.printStackTrace();}
		if(truck == null){
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "车辆"+trucknumber+"不存在");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Warn warn = null;
		if (haswarn == TruckLog.HASWARN_YES) { // 有异常
			warn = new Warn();
			warn.setCorporationsid(truck.getCorporationsid());
			warn.setCorporationname("");
			warn.setTrucksid(truck.getSid());
			warn.setTrucknumber(truck.getTrucknumber());
			warn.setDriversid(-1L);
			warn.setDrivername("");
			warn.setOrdersid(-1L);
			warn.setStatus(Warn.STATUS_CREATED);
			warn.setWarntype(warntype);
			warn.setWarntime(time);
			warn.setWarnvalue(warnvalue);
			if (upimage1sid != null)
				warn.adduploadimage(upimage1sid);
			if (upimage2sid != null)
				warn.adduploadimage(upimage2sid);
			if (upimage3sid != null)
				warn.adduploadimage(upimage3sid);
			warn.setGpsx(gpsx);
			warn.setGpsy(gpsy);
			warn.createtime("0");
//			try {
//				ValidUtils.ValidationWithExp(warn);
//			} catch (Exception e) {
//				JSONObject retObject = new JSONObject();
//				retObject.put("status", 4);
//				retObject.put("msg", e.getMessage());
//				retObject.put("content", "");
//				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
//			}
		}
		TruckLog truckLog = new TruckLog();
		truckLog.setTrucksid(truck.getSid());
		truckLog.setOrdersid(-1L);
		truckLog.setTime(time);
		truckLog.setDistance(distance);
		truckLog.setGpsx(gpsx);
		truckLog.setGpsy(gpsy);
		truckLog.setSpeed(speed);
		truckLog.setFuelvol(fuelvol);
		truckLog.setLefttirepressure(lefttirepressure);
		truckLog.setRighttirepressure(righttirepressure);
		truckLog.setLefttiretemp(lefttiretemp);
		truckLog.setRighttiretemp(righttiretemp);
		truckLog.setPosture(posture);
		truckLog.setLock(lock);
		truckLog.setHaswarn(haswarn);
		truckLog.createtime("0");
//		try {
//			ValidUtils.ValidationWithExp(truckLog);
//		} catch (Exception e) {
//			JSONObject retObject = new JSONObject();
//			retObject.put("status", 4);
//			retObject.put("msg", e.getMessage());
//			retObject.put("content", "");
//			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
//		}
		//push
		JSONObject pushObject = new JSONObject();
		pushObject.put("type", "31");
		JSONObject contentobject = new JSONObject();
		contentobject.put("trucklog", Utils.getJsonObject(truckLog));
		contentobject.put("warn", Utils.getJsonObject(warn));
		pushObject.put("content", contentobject);
//		PushUtils.Push2Admin(pushObject.toString(), session, order.getCorporationsid(), null, null);
		try{
			pushObject.put("retid", "0");//推送类型：1：锁请求返回，0：行车日志返回
			PushUtils.Push2Getui(testcid, pushObject.toString(), PushUtils.ID_TEST);
		}catch(Exception e){}
		////////////////////////////////////////
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "上传行车日志成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	private static FullHttpResponse OnPush(ChannelHandlerContext ctx, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo inloginInfo) throws JSONException {
		System.out.println("in TestHandler-OnPush");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		String corporationsidstr = params.get("corporationsid");//推送给那个公司的
		String typestr = params.get("type");//推送给哪一类用户
		String username = params.get("username");//推送给具体哪一个用户的用户名
		Long corporationsid = null;
		Integer type = null;
		if(corporationsidstr != null){
			try {
				corporationsid = Long.parseLong(corporationsidstr);
			} catch (Exception e) {
				return NettyUtils.getResponse(iskeepAlive, "corporationsid格式不正确");
			}
		}
		if(typestr != null){
			try {
				type = Integer.parseInt(typestr);
			} catch (Exception e) {
				return NettyUtils.getResponse(iskeepAlive, "type格式不正确");
			}
		}
		JSONObject pushObject = new JSONObject();
		pushObject.put("type", "01");
		String pushcontent = pushObject.toString();
		if(username == null){
			try {
				PushUtils.Push2TypeWithExp(pushcontent, session, corporationsid, type);
				return NettyUtils.getResponse(iskeepAlive, "推送成功");
			} catch (Exception e) {
				return NettyUtils.getResponse(iskeepAlive, "推送失败:"+e.getMessage());
			}
		}else {
			LoginInfo loginInfo = null;
			try {
				loginInfo = session.createQuery("from LoginInfo where status = 1 and datastatus = "+Bean.CREATED+" and username = '"+username+"'", LoginInfo.class).uniqueResult();
			} catch (Exception e) {
				return NettyUtils.getResponse(iskeepAlive, "推送失败:LoginInfo数据库出错，多个信息有相同的username");
			}
			if(loginInfo == null)
				return NettyUtils.getResponse(iskeepAlive, "推送失败:对应此username的用户");
			try {
				PushUtils.Push2Single(loginInfo, pushcontent, PushUtils.ID_OTHER);
				return NettyUtils.getResponse(iskeepAlive, "推送成功");
			} catch (Exception e) {
				return NettyUtils.getResponse(iskeepAlive, "推送失败："+e.getMessage());
			}
		}
	}
}
