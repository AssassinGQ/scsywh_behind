package com.Common.Utils;

import org.hibernate.Session;

import com.Common.Entitys.Bean;
import com.Entitys.TableChange;
import com.Entitys.Admin.Entity.Admin;
import com.Entitys.Buyer.Entity.Buyer;
import com.Entitys.Corporation.Entity.Corporation;
import com.Entitys.Escort.Entity.Escort;
import com.Entitys.Fareform.Entity.FareForm;
import com.Entitys.Government.Entity.Government;
import com.Entitys.Lock.Entity.Lock;
import com.Entitys.Manufacturer.Entity.Manufacturer;
import com.Entitys.Order.Entity.Order;
import com.Entitys.Product.Entity.Product;
import com.Entitys.Route.Entity.Route;
import com.Entitys.Seller.Entity.Seller;
import com.Entitys.Statistics.OrderMonthStatistics;
import com.Entitys.Statistics.OrderYearStatistics;
import com.Entitys.Statistics.TruckMaintainStatistics;
import com.Entitys.Statistics.WarnMonthStatistics;
import com.Entitys.Statistics.WarnYearStatistics;
import com.Entitys.Trailer.Entity.Trailer;
import com.Entitys.Truck.Entity.Truck;
import com.Entitys.Truck.Entity.TruckArchives;
import com.Entitys.Truck.Entity.TruckMaintain;
import com.Entitys.Trucklog.Entity.TruckLog;
import com.Entitys.User.Entity.LoginInfo;
import com.Entitys.Warn.Entity.Warn;

public class MySession {
	public static Long OnSave(Bean object, Session session, String operatorsid){
		TableChange changeLog = new TableChange();
		changeLog.setTime(Utils.getCurrenttimeMills());
		changeLog.setTabletype(getType(object));
		changeLog.setOpearatortype(TableChange.OPERATOR_CREATE);
		changeLog.setOperatorsid(Long.parseLong(operatorsid));
		changeLog.setOldobejct(null);
		changeLog.setNewobject(Utils.getJsonObject(object).toString());
		session.save(changeLog);
		return (Long) session.save(object);
	}
	public static Long OnSave(Bean object, Session session, Long operatorsid){
		TableChange changeLog = new TableChange();
		changeLog.setTime(Utils.getCurrenttimeMills());
		changeLog.setTabletype(getType(object));
		changeLog.setOpearatortype(TableChange.OPERATOR_CREATE);
		changeLog.setOperatorsid(operatorsid);
		changeLog.setOldobejct(null);
		changeLog.setNewobject(Utils.getJsonObject(object).toString());
		session.save(changeLog);
		return (Long) session.save(object);
	}
	public static void AfterSave(Bean object, Session session, String operatorsid){
		TableChange changeLog = new TableChange();
		changeLog.setTime(Utils.getCurrenttimeMills());
		changeLog.setTabletype(getType(object));
		changeLog.setOpearatortype(TableChange.OPERATOR_CREATE);
		changeLog.setOperatorsid(Long.parseLong(operatorsid));
		changeLog.setOldobejct(null);
		changeLog.setNewobject(Utils.getJsonObject(object).toString());
		session.save(changeLog);
	}
	public static void AfterSave(Bean object, Session session, Long operatorsid){
		TableChange changeLog = new TableChange();
		changeLog.setTime(Utils.getCurrenttimeMills());
		changeLog.setTabletype(getType(object));
		changeLog.setOpearatortype(TableChange.OPERATOR_CREATE);
		changeLog.setOperatorsid(operatorsid);
		changeLog.setOldobejct(null);
		changeLog.setNewobject(Utils.getJsonObject(object).toString());
		session.save(changeLog);
	}
	public static void OnUpdate(String oldobject, Bean newobject, Session session, String operatorsid){
		TableChange changeLog = new TableChange();
		changeLog.setTime(Utils.getCurrenttimeMills());
		changeLog.setTabletype(getType(newobject));
		changeLog.setOpearatortype(TableChange.OPERATOR_UPDATE);
		changeLog.setOperatorsid(Long.parseLong(operatorsid));
		changeLog.setOldobejct(oldobject);
		changeLog.setNewobject(Utils.getJsonObject(newobject).toString());
		session.save(changeLog);
		session.update(newobject);
		return;
	}
	public static void OnUpdate(String oldobject, Bean newobject, Session session, Long operatorsid){
		TableChange changeLog = new TableChange();
		changeLog.setTime(Utils.getCurrenttimeMills());
		changeLog.setTabletype(getType(newobject));
		changeLog.setOpearatortype(TableChange.OPERATOR_UPDATE);
		changeLog.setOperatorsid(operatorsid);
		changeLog.setOldobejct(oldobject);
		changeLog.setNewobject(Utils.getJsonObject(newobject).toString());
		session.save(changeLog);
		session.update(newobject);
		return;
	}
	public static void AfterUpdate(String oldobject, Bean newobject, Session session, String operatorsid){
		TableChange changeLog = new TableChange();
		changeLog.setTime(Utils.getCurrenttimeMills());
		changeLog.setTabletype(getType(newobject));
		changeLog.setOpearatortype(TableChange.OPERATOR_UPDATE);
		changeLog.setOperatorsid(Long.parseLong(operatorsid));
		changeLog.setOldobejct(oldobject);
		changeLog.setNewobject(Utils.getJsonObject(newobject).toString());
		session.save(changeLog);
	}
	public static void AfterUpdate(String oldobject, Bean newobject, Session session, Long operatorsid){
		TableChange changeLog = new TableChange();
		changeLog.setTime(Utils.getCurrenttimeMills());
		changeLog.setTabletype(getType(newobject));
		changeLog.setOpearatortype(TableChange.OPERATOR_UPDATE);
		changeLog.setOperatorsid(operatorsid);
		changeLog.setOldobejct(oldobject);
		changeLog.setNewobject(Utils.getJsonObject(newobject).toString());
		session.save(changeLog);
	}
	public static void OnDelete(Bean object, Session session, String operatorsid){
		TableChange changeLog = new TableChange();
		changeLog.setTime(Utils.getCurrenttimeMills());
		changeLog.setTabletype(getType(object));
		changeLog.setOpearatortype(TableChange.OPERATOR_DELETE);
		changeLog.setOperatorsid(Long.parseLong(operatorsid));
		changeLog.setOldobejct(Utils.getJsonObject(object).toString());
		object.SetDeleted();
		changeLog.setNewobject(Utils.getJsonObject(object).toString());
		session.save(changeLog);
		session.update(object);
		return;
	}
	public static void OnDelete(Bean object, Session session, Long operatorsid){
		TableChange changeLog = new TableChange();
		changeLog.setTime(Utils.getCurrenttimeMills());
		changeLog.setTabletype(getType(object));
		changeLog.setOpearatortype(TableChange.OPERATOR_DELETE);
		changeLog.setOperatorsid(operatorsid);
		changeLog.setOldobejct(Utils.getJsonObject(object).toString());
		object.SetDeleted();
		changeLog.setNewobject(Utils.getJsonObject(object).toString());
		session.save(changeLog);
		session.update(object);
		return;
	}
	public static void AfterDelete(Bean object, Session session, String operatorsid){
		TableChange changeLog = new TableChange();
		changeLog.setTime(Utils.getCurrenttimeMills());
		changeLog.setTabletype(getType(object));
		changeLog.setOpearatortype(TableChange.OPERATOR_DELETE);
		changeLog.setOperatorsid(Long.parseLong(operatorsid));
		changeLog.setOldobejct(Utils.getJsonObject(object).toString());
		object.SetDeleted();
		changeLog.setNewobject(Utils.getJsonObject(object).toString());
		session.save(changeLog);
	}
	
	private static int getType(Bean object){
		if(object instanceof Admin)
			return TableChange.TABLETYPE_ADMIN;
		else if(object instanceof Buyer)
			return TableChange.TABLETYPE_BUYER;
		else if(object instanceof Corporation)
			return TableChange.TABLETYPE_CORPORATION;
		else if(object instanceof Escort)
			return TableChange.TABLETYPE_ESCORT;
		else if(object instanceof Government)
			return TableChange.TABLETYPE_GOVERNMENT;
		else if(object instanceof LoginInfo)
			return TableChange.TABLETYPE_LOGININFO;
		else if(object instanceof Manufacturer)
			return TableChange.TABLETYPE_MANUFACTURER;
		else if(object instanceof Product)
			return TableChange.TABLETYPE_PRODUCT;
		else if(object instanceof Route)
			return TableChange.TABLETYPE_ROUTE;
		else if(object instanceof Seller)
			return TableChange.TABLETYPE_SELLER;
		else if(object instanceof Trailer)
			return TableChange.TABLETYPE_TRAILER;
		else if(object instanceof OrderMonthStatistics)
			return TableChange.TABLETYPE_ORDERMONTHSTATISTIC;
		else if(object instanceof OrderYearStatistics)
			return TableChange.TABLETYPE_ORDERYEARSTATISTIC;
		else if(object instanceof TruckMaintainStatistics)
			return TableChange.TABLETYPE_TRUCKMAINTAINSTATISTIC;
		else if(object instanceof WarnMonthStatistics)
			return TableChange.TABLETYPE_WARNMONTHSTATISTIC;
		else if(object instanceof WarnYearStatistics)
			return TableChange.TABLETYPE_WARNYEARSTATISTIC;
		else if(object instanceof Truck)
			return TableChange.TABLETYPE_TRUCK;
		else if(object instanceof FareForm)
			return TableChange.TABLETYPE_FAREFORM;
		else if(object instanceof Lock)
			return TableChange.TABLETYPE_LOCK;
		else if(object instanceof Order)
			return TableChange.TABLETYPE_ORDER;
		else if(object instanceof TruckArchives)
			return TableChange.TABLETYPE_TRUCKARCHIVES;
		else if(object instanceof TruckMaintain)
			return TableChange.TABLETYPE_TRUCKMAINTAIN;
		else if(object instanceof Warn)
			return TableChange.TABLETYPE_WARN;
		else if(object instanceof TruckLog)
			return TableChange.TABLETYPE_TRUCKLOG;
		else
			return -1;
	}
}
