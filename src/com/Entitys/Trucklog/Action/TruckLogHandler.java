package com.Entitys.Trucklog.Action;

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

import com.Common.Daos.MulTabBaseDaoImpl;
import com.Common.Daos.QueryBean;
import com.Common.Entitys.Bean;
import com.Common.Utils.MySession;
import com.Common.Utils.NettyUtils;
import com.Common.Utils.PushUtils;
import com.Common.Utils.Utils;
import com.Entitys.Corporation.Entity.Corporation;
import com.Entitys.Driver.Entity.Driver;
import com.Entitys.Government.Entity.Government;
import com.Entitys.Order.Dao.OrderDaoImpl;
import com.Entitys.Order.Entity.Order;
import com.Entitys.Seller.Entity.Seller;
import com.Entitys.Statistics.StatisticsStatic;
import com.Entitys.Statistics.WarnMonthStatistics;
import com.Entitys.Statistics.WarnYearStatistics;
import com.Entitys.Truck.Entity.Truck;
import com.Entitys.Trucklog.Dao.TrucklogDaoImpl;
import com.Entitys.Trucklog.Entity.LastTruckLog;
import com.Entitys.Trucklog.Entity.TruckLog;
import com.Entitys.User.Entity.LoginInfo;
import com.Entitys.Warn.Dao.WarnDaoImpl;
import com.Entitys.Warn.Entity.Warn;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class TruckLogHandler {// 先查Trucklogs表，根据trucksid和status=0，查询本车当前活跃的行车日志数据表名,对该数据表进行增删查改
	private OrderDaoImpl orderDaoImpl;
	private TrucklogDaoImpl trucklogDaoImpl;
	private WarnDaoImpl warnDaoImpl;

	public FullHttpResponse dowork(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		this.orderDaoImpl = new OrderDaoImpl(session);
		this.trucklogDaoImpl = new TrucklogDaoImpl(session);
		this.warnDaoImpl = new WarnDaoImpl(session);
		if (path[1].equals("add_log")) {
			return OnAddLog(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("query_early")) {
			return OnQueryEarlyLog(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("query_new")) {
			return OnQueryNewLog(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else if (path[1].equals("query_corporation")) {
			return OnQueryCorporationLog(ctx, path, session, method, params, iskeepAlive, loginInfo);
		} else {
			return NettyUtils.sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}

	protected FullHttpResponse OnAddLog(ChannelHandlerContext ctx, String[] path, Session session, HttpMethod method,
			Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo) throws JSONException {
		System.out.println("in TruckLogHandler-OnAddLog");
		if (method != HttpMethod.POST) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
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
		// battery
		String battery = params.get("battery");
		if (battery == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请上传电量");
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
			if (warntype < Warn.WARNTYPE_LOCK || warntype > Warn.WARNTYPE_LOWBATTERY) {
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
					Long total = session.createQuery("select count(*) from FileStore where datastatus = "
							+ Bean.CREATED + " and sid = " + upimage1sid, Long.class).uniqueResult();
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
					Long total = session.createQuery("select count(*) from FileStore where datastatus = "
							+ Bean.CREATED + " and sid = " + upimage2sid, Long.class).uniqueResult();
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
					Long total = session.createQuery("select count(*) from FileStore where datastatus = "
							+ Bean.CREATED + " and sid = " + upimage3sid, Long.class).uniqueResult();
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
		Order order = null;
		Corporation corporation = null;
		Driver driver = null;
		Truck truck = null;
		Seller seller = null;
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		try {
			JSONObject orderstatusObject = new JSONObject();
			orderstatusObject.put("min", String.valueOf(Order.STATUS_DISTRIBUTED));
			orderstatusObject.put("max", String.valueOf(Order.STATUS_UNLOADED));
			List<QueryBean> queryList = new ArrayList<QueryBean>();
			queryList.add(
					new QueryBean(Order.class.getName(), "orderp", "orderstatus", orderstatusObject.toString(), QueryBean.TYPE_JSONOBJECT));
			queryList.add(new QueryBean(Truck.class.getName(), "truck", "trucknumber", trucknumber, QueryBean.TYPE_EQUAL));
			orderDaoImpl.fetchListBy(queryList, result, true, Truck.class, Driver.class, Corporation.class,
					Seller.class);
			if (result.size() == 0) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "该车没有在执行中的订单");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (result.size() > 1) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "数据库出错，一辆车对应多个活跃中的订单");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			Map<String, Object> tmpret = result.get(0);
			order = (Order) tmpret.get("Order");
			truck = (Truck) tmpret.get("Truck");
			driver = (Driver) tmpret.get("Driver");
			seller = (Seller) tmpret.get("Seller");
			corporation = (Corporation) tmpret.get("Corporation");
			if (order == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "该车没有在执行中的订单");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (truck == null || corporation == null || seller == null || driver == null) {
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "数据库出错，订单（" + order.getSid() + "）对应的关联数据不存在");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Transaction tx = session.beginTransaction();
		Long warnsid = null;
		if (haswarn == TruckLog.HASWARN_YES) { // 有异常
			Warn warn = new Warn();
			warn.setCorporationsid(corporation.getSid());
			warn.setCorporationname(corporation.getName());
			warn.setTrucksid(truck.getSid());
			warn.setTrucknumber(truck.getTrucknumber());
			warn.setDriversid(driver.getSid());
			warn.setDrivername(driver.getName());
			warn.setOrdersid(order.getSid());
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
			try {
				warnsid = warnDaoImpl.insert(warn, Long.parseLong("0"));
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", e.getMessage());
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			// 计算年月
			Long corporationsid = order.getCorporationsid();
			String[] tmp = Utils.getYearMonth();
			String year = tmp[0];
			String month = tmp[1];
			// 更新承运方异常月统计
			WarnMonthStatistics wmsCorporation = null;
			try {
				wmsCorporation = session.createQuery(
						"from WarnMonthStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
								+ StatisticsStatic.OBJTECTYPE_CORPORATION + " and year = '" + year + "' and month = '"
								+ month + "' and objectsid = " + order.getCorporationsid(),
						WarnMonthStatistics.class).uniqueResult();
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "承运方异常月统计数据库出错" + e.getMessage());
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (wmsCorporation == null) {
				wmsCorporation = new WarnMonthStatistics(corporationsid, year, month, corporationsid,
						StatisticsStatic.OBJTECTYPE_CORPORATION);
				wmsCorporation.setObjectname(corporation.getName());
				wmsCorporation.addWarn(warntype);
				wmsCorporation.createtime("0");
				MySession.OnSave(wmsCorporation, session, "0");
			} else {
				String oldobject = Utils.getJsonObject(wmsCorporation).toString();
				wmsCorporation.setObjectname(corporation.getName());
				wmsCorporation.addWarn(warntype);
				wmsCorporation.updatetime("0");
				MySession.OnUpdate(oldobject, wmsCorporation, session, "0");
			}
			// 更新驾驶员异常月统计
			WarnMonthStatistics wmsDriver = null;
			try {
				wmsDriver = session
						.createQuery(
								"from WarnMonthStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
										+ StatisticsStatic.OBJTECTYPE_DRIVER + " and year = '" + year
										+ "' and month = '" + month + "' and objectsid = " + order.getDriversid(),
								WarnMonthStatistics.class)
						.uniqueResult();
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "驾驶员异常月统计数据库出错");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (wmsDriver == null) {
				wmsDriver = new WarnMonthStatistics(corporationsid, year, month, order.getDriversid(),
						StatisticsStatic.OBJTECTYPE_DRIVER);
				wmsDriver.setObjectname(driver.getName());
				wmsDriver.addWarn(warntype);
				wmsDriver.createtime("0");
				MySession.OnSave(wmsDriver, session, "0");
			} else {
				String oldobject = Utils.getJsonObject(wmsDriver).toString();
				wmsDriver.setObjectname(driver.getName());
				wmsDriver.addWarn(warntype);
				wmsDriver.updatetime("0");
				MySession.OnUpdate(oldobject, wmsDriver, session, "0");
			}
			// 更新托运方异常月统计
			WarnMonthStatistics wmsSeller = null;
			try {
				wmsSeller = session
						.createQuery(
								"from WarnMonthStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
										+ StatisticsStatic.OBJTECTYPE_SELLER + " and year = '" + year
										+ "' and month = '" + month + "' and objectsid = " + order.getSellersid(),
								WarnMonthStatistics.class)
						.uniqueResult();
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "托运方异常月统计数据库出错");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (wmsSeller == null) {
				wmsSeller = new WarnMonthStatistics(corporationsid, year, month, order.getSellersid(),
						StatisticsStatic.OBJTECTYPE_SELLER);
				wmsSeller.setObjectname(seller.getName());
				wmsSeller.addWarn(warntype);
				wmsSeller.createtime("0");
				MySession.OnSave(wmsSeller, session, "0");
			} else {
				String oldobject = Utils.getJsonObject(wmsSeller).toString();
				wmsSeller.setObjectname(seller.getName());
				wmsSeller.addWarn(warntype);
				wmsSeller.updatetime("0");
				MySession.OnUpdate(oldobject, wmsSeller, session, "0");
			}
			// 更新货车异常月统计
			WarnMonthStatistics wmsTruck = null;
			try {
				wmsTruck = session
						.createQuery(
								"from WarnMonthStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
										+ StatisticsStatic.OBJTECTYPE_TRUCK + " and year = '" + year + "' and month = '"
										+ month + "' and objectsid = " + order.getTrucksid(),
								WarnMonthStatistics.class)
						.uniqueResult();
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "车辆异常月统计数据库出错");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (wmsTruck == null) {
				wmsTruck = new WarnMonthStatistics(corporationsid, year, month, order.getTrucksid(),
						StatisticsStatic.OBJTECTYPE_TRUCK);
				wmsTruck.setObjectname(truck.getTrucknumber());
				wmsTruck.addWarn(warntype);
				wmsTruck.createtime("0");
				MySession.OnSave(wmsTruck, session, "0");
			} else {
				String oldobject = Utils.getJsonObject(wmsTruck).toString();
				wmsTruck.setObjectname(truck.getTrucknumber());
				wmsTruck.addWarn(warntype);
				wmsTruck.updatetime("0");
				MySession.OnUpdate(oldobject, wmsTruck, session, "0");
			}
			// 更新承运方异常年统计
			WarnYearStatistics wysCorporation = null;
			try {
				wysCorporation = session
						.createQuery(
								"from WarnYearStatistics where datastatus = " + Bean.CREATED + " and objecttype = "
										+ StatisticsStatic.OBJTECTYPE_CORPORATION + " and year = '" + year
										+ "' and objectsid = " + order.getCorporationsid(),
								WarnYearStatistics.class)
						.uniqueResult();
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "承运方异常年统计数据库出错");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (wysCorporation == null) {
				wysCorporation = new WarnYearStatistics(corporationsid, year, corporationsid,
						StatisticsStatic.OBJTECTYPE_CORPORATION);
				wysCorporation.setObjectname(corporation.getName());
				wysCorporation.addWarn(warntype);
				wysCorporation.createtime("0");
				MySession.OnSave(wysCorporation, session, "0");
			} else {
				String oldobject = Utils.getJsonObject(wysCorporation).toString();
				wysCorporation.setObjectname(corporation.getName());
				wysCorporation.addWarn(warntype);
				wysCorporation.updatetime("0");
				MySession.OnUpdate(oldobject, wysCorporation, session, "0");
			}
			// 更新驾驶员异常年统计
			WarnYearStatistics wysDriver = null;
			try {
				wysDriver = session.createQuery("from WarnYearStatistics where datastatus = " + Bean.CREATED
						+ " and objecttype = " + StatisticsStatic.OBJTECTYPE_DRIVER + " and year = '" + year
						+ "' and objectsid = " + order.getDriversid(), WarnYearStatistics.class).uniqueResult();
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "驾驶员异常年统计数据库出错");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (wysDriver == null) {
				wysDriver = new WarnYearStatistics(corporationsid, year, order.getDriversid(),
						StatisticsStatic.OBJTECTYPE_DRIVER);
				wysDriver.setObjectname(driver.getName());
				wysDriver.addWarn(warntype);
				wysDriver.createtime("0");
				MySession.OnSave(wysDriver, session, "0");
			} else {
				String oldobject = Utils.getJsonObject(wysDriver).toString();
				wysDriver.setObjectname(driver.getName());
				wysDriver.addWarn(warntype);
				wysDriver.updatetime("0");
				MySession.OnUpdate(oldobject, wysDriver, session, "0");
			}
			// 更新托运方异常年统计
			WarnYearStatistics wysSeller = null;
			try {
				wysSeller = session.createQuery("from WarnYearStatistics where datastatus = " + Bean.CREATED
						+ " and objecttype = " + StatisticsStatic.OBJTECTYPE_SELLER + " and year = '" + year
						+ "' and objectsid = " + order.getSellersid(), WarnYearStatistics.class).uniqueResult();
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "托运方异常年统计数据库出错");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (wysSeller == null) {
				wysSeller = new WarnYearStatistics(corporationsid, year, order.getSellersid(),
						StatisticsStatic.OBJTECTYPE_SELLER);
				wysSeller.setObjectname(seller.getName());
				wysSeller.addWarn(warntype);
				wysSeller.createtime("0");
				MySession.OnSave(wysSeller, session, "0");
			} else {
				String oldobject = Utils.getJsonObject(wysSeller).toString();
				wysSeller.setObjectname(seller.getName());
				wysSeller.addWarn(warntype);
				wysSeller.updatetime("0");
				MySession.OnUpdate(oldobject, wysSeller, session, "0");
			}
			// 更新车辆异常年统计
			WarnYearStatistics wysTruck = null;
			try {
				wysTruck = session.createQuery("from WarnYearStatistics where datastatus = " + Bean.CREATED
						+ " and objecttype = " + StatisticsStatic.OBJTECTYPE_TRUCK + " and year = '" + year
						+ "' and objectsid = " + order.getTrucksid(), WarnYearStatistics.class).uniqueResult();
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", "车辆异常年统计数据库出错");
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
			if (wysTruck == null) {
				wysTruck = new WarnYearStatistics(corporationsid, year, order.getTrucksid(),
						StatisticsStatic.OBJTECTYPE_TRUCK);
				wysTruck.setObjectname(truck.getTrucknumber());
				wysTruck.addWarn(warntype);
				wysTruck.createtime("0");
				MySession.OnSave(wysTruck, session, "0");
			} else {
				String oldobject = Utils.getJsonObject(wysTruck).toString();
				wysTruck.setObjectname(truck.getTrucknumber());
				wysTruck.addWarn(warntype);
				wysTruck.updatetime("0");
				MySession.OnUpdate(oldobject, wysTruck, session, "0");
			}
			// 推送给驾驶员
			{
				JSONObject pushObject = new JSONObject();
				pushObject.put("type", "20");
				pushObject.put("sid", warnsid);
				String pushcontent = pushObject.toString();
				if (!PushUtils.Push2Driver(order.getDriversid(), pushcontent, session,
						order.getCorporationsid()))
					System.err.println("硬件上传异常信息推送给司机失败");
			}
			// 推送给管理员
			{
				JSONObject pushObject = new JSONObject();
				pushObject.put("type", "21");
				pushObject.put("sid", warnsid);
				String pushcontent = pushObject.toString();
				PushUtils.Push2Admin(pushcontent, session, order.getCorporationsid(), null, null);
			}
			// 推送给政府
			{
				JSONObject pushObject = new JSONObject();
				pushObject.put("type", "22");
				pushObject.put("sid", warnsid);
				String pushcontent = pushObject.toString();
				switch (warntype) {
				case Warn.WARNTYPE_LOCK:// 无
					break;
				case Warn.WARNTYPE_LEAK:// 环保部门，消防部门，安全监管部门
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_ENVIRONMENT, null);
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_FIRE, null);
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_SAFETY, null);
					break;
				case Warn.WARNTYPE_TIRE:// 无
					break;
				case Warn.WARNTYPE_FUEL:// 无
					break;
				case Warn.WARNTYPE_OVERSPEED:// 交警，安全监管部门，运输管理部门
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_TRAFFIC, null);
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_SAFETY, null);
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_TRANSPORT, null);
					break;
				case Warn.WARNTYPE_PARK:// 无
					break;
				case Warn.WARNTYPE_FATIGUEDRIVING:// 交警部门，安全监管部门
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_TRAFFIC, null);
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_SAFETY, null);
					break;
				case Warn.WARNTYPE_SUDDENBRAKE:// 无
					break;
				case Warn.WARNTYPE_SUDDENACCELERATE:// 无
					break;
				case Warn.WARNTYPE_ACCIDENT:// 交警，消防，安全监管部门
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_TRAFFIC, null);
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_FIRE, null);
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_SAFETY, null);
					break;
				case Warn.WARNTYPE_OVERLOAD:// 运输管理部门，交警，安全监管部门
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_TRANSPORT, null);
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_TRAFFIC, null);
					PushUtils.Push2Government(pushcontent, session, Government.DEPT_SAFETY, null);
					break;
				case Warn.WARNTYPE_LOWBATTERY:// 无
					break;
				default:
					break;
				}
			}
			session.flush();
			// 更新异常
			String oldwarn = Utils.getJsonObject(warn).toString();
			warn.setStatus(Warn.STATUS_PUSHED);
			warn.updatetime("0");
			try {
				warnDaoImpl.update(warn, oldwarn, Long.parseLong("0"));
			} catch (Exception e) {
				tx.rollback();
				JSONObject retObject = new JSONObject();
				retObject.put("status", 4);
				retObject.put("msg", e.getMessage());
				retObject.put("content", "");
				return NettyUtils.getResponse(iskeepAlive, retObject.toString());
			}
		}
		TruckLog truckLog = new TruckLog();
		truckLog.setTrucksid(truck.getSid());
		truckLog.setOrdersid(order.getSid());
		truckLog.setTime(time);
		truckLog.setCorporationsid(truck.getCorporationsid());
		truckLog.setDistance(distance);
		truckLog.setGpsx(gpsx);
		truckLog.setGpsy(gpsy);
		truckLog.setSpeed(speed);
		truckLog.setFuelvol(fuelvol);
		truckLog.setBattery(battery);
		truckLog.setLefttirepressure(lefttirepressure);
		truckLog.setRighttirepressure(righttirepressure);
		truckLog.setLefttiretemp(lefttiretemp);
		truckLog.setRighttiretemp(righttiretemp);
		truckLog.setPosture(posture);
		truckLog.setLock(lock);
		truckLog.setHaswarn(haswarn);
		truckLog.setWarnsid(warnsid);
		truckLog.createtime("0");
		try {
			trucklogDaoImpl.insert(truckLog, Long.parseLong("0"));
		} catch (Exception e) {
			tx.rollback();
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", e.getMessage());
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		MySession.AfterSave(truckLog, session, "0");
		tx.commit();
		JSONObject retObject = new JSONObject();
		retObject.put("status", 0);
		retObject.put("msg", "添加行车日志成功");
		retObject.put("content", "");
		return NettyUtils.getResponse(iskeepAlive, retObject.toString());
	}

	protected FullHttpResponse OnQueryEarlyLog(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in TruckLogHandler-OnQueryEarlyLog");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		String trucksidstr = params.get("trucksid");
		String trucknumber = params.get("trucknumber");
		if (trucksidstr == null && trucknumber == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请填写车辆sid或者车牌号");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		if (trucknumber != null) {
			queryList.add(new QueryBean(Truck.class.getName(), "truck", "trucknumber", trucknumber, QueryBean.TYPE_EQUAL));
			params.remove("trucknumber");
		}
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
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
				} catch (Exception e1) {
				}
			}
			queryList.add(new QueryBean(TruckLog.class.getName(), "trucklog", field, value, valuetype));
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
		} else {
			intlimit = null;
			intpage = null;
		}
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Long total = null;
		try {
			trucklogDaoImpl.fetchListBy(queryList, result, true, intlimit, intpage, MulTabBaseDaoImpl.QUERY_ORDER_ASC, "time",
					Truck.class);
			total = trucklogDaoImpl.selectCount(queryList, true);
		} catch (Exception e) {
			JSONObject jsonObject2 = new JSONObject();
			jsonObject2.put("status", 4);
			jsonObject2.put("msg", e.getMessage());
			jsonObject2.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
		}
		// return result
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询行车日志成功");
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
			Map<String, Object> tmpret = result.get(i);
			TruckLog truckLog = (TruckLog) tmpret.get("TruckLog");
			Truck truck = (Truck) tmpret.get("Truck");
			JSONObject tmpjsonObject = Utils.getJsonObject(truckLog);
			tmpjsonObject.put("trucksid", truck.getSid());
			tmpjsonObject.put("trucknumber", truck.getTrucknumber());
			dataArray.put(tmpjsonObject);
		}
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}

	protected FullHttpResponse OnQueryNewLog(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in TruckLogHandler-OnQueryNewLog");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		String trucksidstr = params.get("trucksid");
		String trucknumber = params.get("trucknumber");
		if (trucksidstr == null && trucknumber == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请填写车辆sid或者车牌号");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		if (trucknumber != null) {
			queryList.add(new QueryBean(Truck.class.getName(), "truck", "trucknumber", trucknumber, QueryBean.TYPE_EQUAL));
			params.remove("trucknumber");
		}
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
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
				} catch (Exception e1) {
				}
			}
			queryList.add(new QueryBean(TruckLog.class.getName(), "trucklog", field, value, valuetype));
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
		} else {
			intlimit = null;
			intpage = null;
		}
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Long total = null;
		try {
			trucklogDaoImpl.fetchListBy(queryList, result, true, intlimit, intpage, MulTabBaseDaoImpl.QUERY_ORDER_DESC,
					"createdat", Truck.class);
			total = trucklogDaoImpl.selectCount(queryList, true, Truck.class);
		} catch (Exception e) {
			JSONObject jsonObject2 = new JSONObject();
			jsonObject2.put("status", 4);
			jsonObject2.put("msg", e.getMessage());
			jsonObject2.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
		}
		// return result
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询行车日志成功");
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
			Map<String, Object> tmpret = result.get(i);
			TruckLog truckLog = (TruckLog) tmpret.get("TruckLog");
			Truck truck = (Truck) tmpret.get("Truck");
			JSONObject tmpjsonObject = Utils.getJsonObject(truckLog);
			tmpjsonObject.put("trucksid", truck.getSid());
			tmpjsonObject.put("trucknumber", truck.getTrucknumber());
			dataArray.put(tmpjsonObject);
		}
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}
	//查询某公司每个订单最新的一条行车日志
	protected FullHttpResponse OnQueryCorporationLog(ChannelHandlerContext ctx, String[] path, Session session,
			HttpMethod method, Map<String, String> params, boolean iskeepAlive, LoginInfo loginInfo)
			throws JSONException {
		System.out.println("in TruckLogHandler-OnQueryCorporationLog");
		if (method != HttpMethod.GET) {
			return NettyUtils.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
		String corporationsidstr = params.get("corporationsid");
		if (corporationsidstr == null) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "请填写承运方sid");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		Long corporationsid = null;
		try {
			corporationsid = Long.parseLong(corporationsidstr);
		} catch (Exception e) {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "承运方sid格式不正确");
			retObject.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, retObject.toString());
		}
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
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
				} catch (Exception e1) {
				}
			}
			queryList.add(new QueryBean(LastTruckLog.class.getName(), "lastlog", field, value, valuetype));
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
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Long total = null;
		try {
			trucklogDaoImpl.fetchByCorporation(corporationsid, queryList, true, intlimit, intpage, result, Truck.class);
			total = trucklogDaoImpl.selectCountByCorporation(corporationsid, queryList, true);
//			if(result.size() > 1){
//				JSONObject jsonObject2 = new JSONObject();
//				jsonObject2.put("status", 4);
//				jsonObject2.put("msg", "未知数据库错误，limit(1)返回多个结果");
//				jsonObject2.put("content", "");
//				return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
//			}
		} catch (Exception e) {
			JSONObject jsonObject2 = new JSONObject();
			jsonObject2.put("status", 4);
			jsonObject2.put("msg", e.getMessage());
			jsonObject2.put("content", "");
			return NettyUtils.getResponse(iskeepAlive, jsonObject2.toString());
		}
		// return result
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", 0);
		jsonObject.put("msg", "查询行车日志成功");
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
			Map<String, Object> tmpret = result.get(i);
			TruckLog truckLog = (TruckLog) tmpret.get("TruckLog");
			Truck truck = (Truck) tmpret.get("Truck");
			JSONObject tmpjsonObject = Utils.getJsonObject(truckLog);
			tmpjsonObject.put("trucksid", truck == null ? "null" : truck.getSid());
			tmpjsonObject.put("trucknumber", truck == null ? "null" : truck.getTrucknumber());
			dataArray.put(tmpjsonObject);
		}
		contentJsonObject.put("data", dataArray);
		jsonObject.put("content", contentJsonObject);
		return NettyUtils.getResponse(iskeepAlive, jsonObject.toString());
	}
}
