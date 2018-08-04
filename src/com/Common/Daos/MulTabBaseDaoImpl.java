package com.Common.Daos;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import com.Common.Entitys.Bean;
import com.Common.Enums.SnowFlake_DataID;
import com.Common.Exceptions.DaoException;
import com.Common.Utils.DaoUtils;
import com.Common.Utils.SnowFlake;
import com.Common.Utils.Utils;
import com.Common.Utils.ValidUtils;
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

public abstract class MulTabBaseDaoImpl<T extends Bean> implements BaseDao<T> {
	public static int QUERY_ORDER_DESC = 0;
	public static int QUERY_ORDER_ASC = 1;
	protected Session session;
	//数据库表的前缀，如orderp_1的前缀为orderp_
	protected String pretname;
	//join查询中，主表的别名
	protected String tAliasname;
	//查询主表对应的javabean类
	protected Class<T> tClass;
	//查询主表对应的javabean类的类名
	protected String tClassname;
	//当前Dao的类名
	protected String Daoname;

	public MulTabBaseDaoImpl(Session session, Class<T> tclazz) {
		this.session = session;
		tClass = tclazz;
		tClassname = tclazz.getSimpleName();
		Daoname = this.getClass().getSimpleName();	
	}

	public long insert(T object, Long operatersid) throws DaoException {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.insert,session为空", Daoname);
		}
		try {
			ValidUtils.ValidationWithExp(object);
		} catch (Exception e) {
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.insert.{%s}", Daoname, e.getMessage());
		}
		SnowFlake snowFlake = new SnowFlake(0, SnowFlake_DataID.getEnum(tClassname).getDataId());
		long sid = snowFlake.nextId();
		object.setSid(sid);
		Query<T> query = null;
		int result = 0;
		try {
			String tablename = pretname + String.valueOf(sid % 10);
			List<Object> retList = new ArrayList<Object>();
			query = session.createNativeQuery(DaoUtils.Gene_Insert_Sql(object, retList, tablename), tClass);
			for (int i = 0; i < retList.size(); i++) {
				query.setParameter(i + 1, retList.get(i));
			}
			result = query.executeUpdate();
		} catch (Exception e) {
			if(query == null)
				throw DaoException.DB_INSERT_EXCEPTION.newInstance("In %s.insert.{%s}", Daoname, Utils.getErrorStr(e));
			else
				throw DaoException.DB_INSERT_EXCEPTION.newInstance("In %s.insert.{%s}.{%s}", Daoname, query.getQueryString(), Utils.getErrorStr(e));		
		}
		if (result <= 0) {
			throw DaoException.DB_INSERT_EXCEPTION.newInstance("In %s.insert.{%s}.{%s}", Daoname, query.getQueryString(), "数据库0行记录受到影响");
		}
		try {
			TableChange changeLog = new TableChange();
			changeLog.setTime(Utils.getCurrenttimeMills());
			changeLog.setTabletype(getType(object));
			changeLog.setOpearatortype(TableChange.OPERATOR_CREATE);
			changeLog.setOperatorsid(operatersid);
			changeLog.setOldobejct(null);
			changeLog.setNewobject(Utils.getJsonObject(object).toString());
			session.save(changeLog);
			session.clear();			
		} catch (Exception e) {
			throw DaoException.DB_INSERT_EXCEPTION.newInstance("In %s.insert.{%s}", Daoname, Utils.getErrorStr(e));
		}
		return sid;
	}

	public int update(T object, String oldobject, Long operatersid) throws Exception {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.update,session为空", Daoname);
		}
		if(object == null)
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.update,%s为空", Daoname, tClassname);
		try {
			ValidUtils.ValidationWithExp(object);
		} catch (Exception e) {
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.update.{%s}", Daoname, e.getMessage());
		}
		if(object.getSid() == null)
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.update,%s.sid为空", Daoname, tClassname);
		long sid = object.getSid().longValue();
		String tablename = pretname + String.valueOf(sid % 10);
		List<Object> retList = new ArrayList<Object>();
		Query<T> query = null;
		int result = 0;
		try {
			query = session.createNativeQuery(DaoUtils.Gene_Update_Sql(object, retList, tablename), tClass);
			retList.add(retList.size(), sid);
			for (int i = 0; i < retList.size(); i++) {
				query.setParameter(i + 1, retList.get(i));
			}
			result = query.executeUpdate();
		} catch (Exception e) {
			if(query == null)
				throw DaoException.DB_UPDATE_EXCEPTION.newInstance("In %s.update.{%s}", Daoname, Utils.getErrorStr(e));
			else
				throw DaoException.DB_UPDATE_EXCEPTION.newInstance("In %s.update.{%s}.{%s}", Daoname, query.getQueryString(), Utils.getErrorStr(e));
		}
		if (result <= 0) {
			throw DaoException.DB_UPDATE_EXCEPTION.newInstance("In %s.update.{%s}.{%s}", Daoname, query.getQueryString(), "数据库0行记录受到影响");
		}
		try {
			TableChange changeLog = new TableChange();
			changeLog.setTime(Utils.getCurrenttimeMills());
			changeLog.setTabletype(getType(object));
			changeLog.setOpearatortype(TableChange.OPERATOR_UPDATE);
			changeLog.setOperatorsid(operatersid);
			changeLog.setOldobejct(oldobject);
			changeLog.setNewobject(Utils.getJsonObject(object).toString());
			session.save(changeLog);
			session.clear();
		} catch (Exception e) {
			throw DaoException.DB_UPDATE_EXCEPTION.newInstance("In %s.update.{%s}", Daoname, Utils.getErrorStr(e));
		}
		return result;
	}
	
	public int delete(T object, Long operatersid) throws Exception {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.delete,session为空", Daoname);
		}
		if(object == null)
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.delete,%s为空", Daoname, tClassname);
		if(object.getSid() == null)
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.delete,%s.sid为空", Daoname, tClassname);
		long sid = object.getSid().longValue();
		Query<T> query = null;
		int result = 0;
		try {
			TableChange changeLog = new TableChange();
			changeLog.setTime(Utils.getCurrenttimeMills());
			changeLog.setTabletype(getType(object));
			changeLog.setOpearatortype(TableChange.OPERATOR_DELETE);
			changeLog.setOperatorsid(operatersid);
			changeLog.setOldobejct(Utils.getJsonObject(object).toString());
			object.SetDeleted();
			String tablename = pretname + String.valueOf(sid % 10);
			List<Object> retList = new ArrayList<Object>();
			query = session.createNativeQuery(DaoUtils.Gene_Update_Sql(object, retList, tablename), tClass);
			retList.add(retList.size(), sid);
			for (int i = 0; i < retList.size(); i++) {
				query.setParameter(i + 1, retList.get(i));
			}
			result = query.executeUpdate();
			changeLog.setNewobject(Utils.getJsonObject(object).toString());
			session.save(changeLog);
		} catch (Exception e) {
			if(query == null)
				throw DaoException.DB_UPDATE_EXCEPTION.newInstance("In %s.delete.{%s}", Daoname, Utils.getErrorStr(e));
			else
				throw DaoException.DB_UPDATE_EXCEPTION.newInstance("In %s.delete.{%s}.{%s}", Daoname, query.getQueryString(), Utils.getErrorStr(e));
		}
		if (result <= 0) {
			throw DaoException.DB_UPDATE_EXCEPTION.newInstance("In %s.delete.{%s}.{%s}", Daoname, query.getQueryString(), "数据库0行记录受到影响");
		}
		session.clear();
		return result;
	}
	//根据sid条件查询唯一Order
	public T getById(long sid, boolean needVaild) throws DaoException {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.getById,session为空", Daoname);
		}
		T object = null;
		String tablename = pretname + String.valueOf(sid % 10);
		String select_sql = null;
		if(needVaild)
			select_sql = "SELECT * FROM " + tablename + " WHERE sid = " + sid + " and datastatus = " + Bean.CREATED + " ORDER BY createdat DESC";
		else {
			select_sql = "SELECT * FROM " + tablename + " WHERE sid = " + sid + " ORDER BY createdat DESC";
		}
		try {
			Query<T> query = session.createNativeQuery(select_sql, tClass);
			object = query.uniqueResult();
			session.clear();
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.getById.{%s}", Daoname, Utils.getErrorStr(e));
		}
		if(object == null)
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.getById.{%s}", Daoname, "查无此记录");
		return object;
	}
	//根据sid条件查询唯一Order，并将clazzs对应的关联对象同时join查询
	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	public void fetchById(long sid, Map<String, Object> result, boolean needVaild, Class<?>... clazzs) throws DaoException {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.fetchById,session为空", Daoname);
		}
		if(result == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.fetchById,resultMap未初始化", Daoname);
		}
		result.clear();
		if(clazzs.length == 0){
			T object = getById(sid, needVaild);
			result.put(tClassname, object);
			return;
		}
		List<Object[]> rets = null;
		String tablename = pretname + String.valueOf(sid % 10);
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		sb1.append("SELECT {").append(tAliasname).append(".*},");
		sb2.append(" FROM ").append(tablename).append(" orderp");
		for(int i = 0; i < clazzs.length; i++){
			String Clazzname = clazzs[i].getSimpleName();
			String clazzname = Clazzname.toLowerCase();
			sb1.append(" {").append(clazzname).append(".*},");
			sb2.append(" LEFT JOIN ").append(clazzname)
				.append(" ON ( ").append(tAliasname).append(".`").append(clazzname).append("sid` = ")
				.append(clazzname).append(".`sid`");
			if(needVaild)
				sb2.append("and ").append(clazzname).append(".`datastatus` = ").append(Bean.CREATED);
			sb2.append(" )");
		}
		sb1.deleteCharAt(sb1.length()-1);
		sb2.append(" WHERE ").append(tAliasname).append(".`sid` = ").append(sid);
		if(needVaild)
			sb2.append(" and ").append(tAliasname).append(".`datastatus` = ").append(Bean.CREATED);
		String select_sql = sb1.toString()+sb2.toString();
		try {
			NativeQuery query = session.createNativeQuery(select_sql).addEntity(tAliasname, tClass);
			for(int i = 0; i < clazzs.length; i++){
				String Clazzname = clazzs[i].getSimpleName();
				String clazzname = Clazzname.toLowerCase();
				query.addEntity(clazzname, clazzs[i]);
			}
			rets = query.list();
			session.clear();
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchById.{%s}", Daoname, Utils.getErrorStr(e));
		}
		if(rets.size() > 1)
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchById.{%s}", Daoname, "主键重复");
		if(rets.size() == 0)
			return;
		Object[] ret = rets.get(0);
		try {
			if(ret.length != clazzs.length+1)
				throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchById.{%s}", Daoname, "没有返回预期个数的Entity");
			result.put(tClassname, ret[0]);
			for(int i = 0; i < clazzs.length; i++){
				String Clazzname = clazzs[i].getSimpleName();
				result.put(Clazzname, ret[i+1]);
			}
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchById.{%s}", Daoname, "JOIN查询错误:"+Utils.getErrorStr(e));
		}
	}
	//根据paramMap条件查询Order
	public List<T> getListBy(List<QueryBean> paramList, boolean needVaild) throws DaoException {
		return getListBy(paramList, needVaild, null, null);
	}
	
	public List<T> getListBy(List<QueryBean> paramList, boolean needVaild, Integer limit, Integer page) throws DaoException {
		return getListBy(paramList, needVaild, limit, page, MulTabBaseDaoImpl.QUERY_ORDER_DESC, "createdat");
	}
	
	public List<T> getListBy(List<QueryBean> paramList, boolean needVaild, Integer limit, Integer page, Integer order, String orderfield) throws DaoException {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.getListBy,session为空", Daoname);
		}
		if(paramList == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.getListBy,paramList为空", Daoname);
		}
		if(order == null || orderfield == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.getListBy,ORDER参数为空", Daoname);
		}
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		queryList.addAll(paramList);
		if(needVaild)
			queryList.add(new QueryBean(tClass.getName(), tAliasname, "datastatus", Bean.CREATED, QueryBean.TYPE_EQUAL));
		List<T> ret = new ArrayList<T>();
		try {
			int beginindex = 0, endindex = 9;
			for(int i = 0; i < queryList.size(); i++){
				if(queryList.get(i).getField().equals("sid")){
					Long tmpsid = null;
					try {
						tmpsid = Long.parseLong(queryList.get(i).getValue());
					} catch (Exception e) {
						throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.getListBy,sid格式不正确", Daoname);
					}
					int tableid = (int) (tmpsid % 10); 
					beginindex = tableid;
					endindex = tableid;
					break;
				}
			}
			for(int i = beginindex; i <= endindex; i++){
				String tablename = pretname + String.valueOf(i);
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT * FROM ").append(tablename).append(" ").append(tAliasname);
				List<Object> retList = new ArrayList<Object>();
				sb.append(DaoUtils.Gene_Query_Sql(queryList, retList));
				sb.append(" ORDER BY "+tAliasname+".`"+orderfield+"` ");
				if(order == MulTabBaseDaoImpl.QUERY_ORDER_DESC)
					sb.append("DESC");
				else
					sb.append("ASC");
//				if(limit != null){
//					if(page == null)
//						page = 1;
//					sb.append(" LIMIT ").append((page-1)*limit).append(", ").append(limit);					
//				}
				Query<T> query = session.createNativeQuery(sb.toString(), tClass);
				for(int j = 0; j < retList.size(); j++)
					query.setParameter(j+1, retList.get(j));
				ret.addAll(query.getResultList());
			}	
			session.clear();
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.getListBy.{%s}", Daoname, Utils.getErrorStr(e));
		}
		
		List<T> result = new ArrayList<T>();
		if(order == MulTabBaseDaoImpl.QUERY_ORDER_DESC){
			for(int i = 0; i < ret.size(); i++){
				long maxtime = 0L;
				int maxindex = 0;
				for(int j = 0; j < ret.size(); j++){
					Long nowtime = ret.get(j).getCreatedat();
					if(nowtime == null)
						nowtime = 0L;
					if(maxtime < nowtime){
						maxindex = j;
						maxtime = nowtime;
					}
				}
				T tmpmap = ret.get(maxindex);
				ret.remove(maxindex);
				ret.add(0, tmpmap);
			}
		}
		if(order == MulTabBaseDaoImpl.QUERY_ORDER_ASC){
			for(int i = 0; i < ret.size(); i++){
				long mintime = Long.MAX_VALUE;
				int minindex = 0;
				for(int j = 0; j < ret.size(); j++){
					Long nowtime = ret.get(j).getCreatedat();
					if(nowtime == null)
						nowtime = 0L;
					if(mintime > nowtime){
						minindex = j;
						mintime = nowtime;
					}
				}
				T tmpmap = ret.get(minindex);
				ret.remove(minindex);
				ret.add(0, tmpmap);
			}
		}
		if(limit != null){
			if(page == null)
				page = 1;
			int firstindex = (page - 1) * limit;
			int lastindex = firstindex + limit - 1;
			if(lastindex > (ret.size() - 1))
				lastindex = ret.size() - 1;
			for(int i = firstindex; i <= lastindex; i++)
				result.add(ret.get(i));
		}else {
			result.addAll(ret);
		}
		return result;
	}
	//根据paramMap条件查询Order，并将clazzs对应的关联对象同时join查询
	public void fetchListBy(List<QueryBean> paramList, List<Map<String, Object>> result, boolean needVaild, Class<?>... clazzs) throws DaoException {
		fetchListBy(paramList, result, needVaild, null, null, clazzs);
	}

	public void fetchListBy(List<QueryBean> paramList, List<Map<String, Object>> result, boolean needVaild, Integer limit, Integer page, Class<?>... clazzs) throws DaoException {
		fetchListBy(paramList, result, needVaild, limit, page, MulTabBaseDaoImpl.QUERY_ORDER_DESC, "createdat", clazzs);
	}
	
	@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
	public void fetchListBy(List<QueryBean> paramList, List<Map<String, Object>> result, boolean needVaild, Integer limit, Integer page, Integer order, String orderfield, Class<?>... clazzs) throws DaoException {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.fetchListBy,session为空", Daoname);
		}
		if(paramList == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.fetchListBy,paramList为空", Daoname);
		}
		if(result == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.fetchListBy,resultList为空", Daoname);
		}
		if(order == null || orderfield == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.fetchListBy,ORDER参数为空", Daoname);
		}
		result.clear();
		if(clazzs.length == 0){
			List<T> tmpret = getListBy(paramList, needVaild, limit, page, order, orderfield);
			for(int i = 0; i < tmpret.size(); i++){
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put(tClassname, tmpret.get(i));
				result.add(i, tmpMap);
			}
			return;
		}
		List<Object[]> tmpresult = new ArrayList<Object[]>();
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		queryList.addAll(paramList);
		if(needVaild)
			queryList.add(new QueryBean(tClass.getName(), tAliasname, "datastatus", Bean.CREATED, QueryBean.TYPE_EQUAL));
		int beginindex = 0, endindex = 9;
		for(int i = 0; i < queryList.size(); i++){
			if(queryList.get(i).getField().equals("sid")){
				Long tmpsid = null;
				try {
					tmpsid = Long.parseLong(queryList.get(i).getValue());
				} catch (Exception e) {
					throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.getListBy,sid格式不正确", Daoname);
				}
				int tableid = (int) (tmpsid % 10); 
				beginindex = tableid;
				endindex = tableid;
				break;
			}
		}
		for(int j = beginindex; j <= endindex; j++){
			List<Object[]> rets = null;
			String tablename = pretname + String.valueOf(j);
			StringBuilder sb1 = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			sb1.append("SELECT {").append(tAliasname).append(".*},");
			sb2.append(" FROM ").append(tablename).append(" ").append(tAliasname);
			for(int i = 0; i < clazzs.length; i++){
				String Clazzname = clazzs[i].getSimpleName();
				String clazzname = Clazzname.toLowerCase();
				sb1.append(" {").append(clazzname).append(".*},");
				sb2.append(" LEFT JOIN ").append(clazzname)
					.append(" ON ( ").append(tAliasname).append(".`").append(clazzname).append("sid` = ")
					.append(clazzname).append(".`sid`");
				if(needVaild)
					sb2.append("and ").append(clazzname).append(".`datastatus` = ").append(Bean.CREATED);
				sb2.append(" )");
			}
			sb1.deleteCharAt(sb1.length()-1);
			List<Object> retList = new ArrayList<Object>();
			String query_sql = null;
			try {
				query_sql = DaoUtils.Gene_Query_Sql(queryList, retList);
			} catch (DaoException e) {
				throw e.newInstance("In %s.fetchListBy.{%s}", Daoname, e.getMessage());
			}
			sb2.append(query_sql);
			sb2.append(" ORDER BY ").append(tAliasname).append(".`"+orderfield+"` ");
			if(order == MulTabBaseDaoImpl.QUERY_ORDER_DESC)
				sb2.append("DESC");
			else
				sb2.append("ASC");
//			if(limit != null){
//				if(page == null)
//					page = 1;
//				sb2.append(" LIMIT ").append((page-1)*limit).append(", ").append(limit);					
//			}
			String select_sql = sb1.toString()+sb2.toString();
			try {
				NativeQuery query = session.createNativeQuery(select_sql).addEntity(tAliasname, tClass);
				for(int i = 0; i < clazzs.length; i++){
					String Clazzname = clazzs[i].getSimpleName();
					String clazzname = Clazzname.toLowerCase();
					query.addEntity(clazzname, clazzs[i]);
				}
				for(int i = 0; i < retList.size(); i++)
					query.setParameter(i+1, retList.get(i));
				rets = query.list();
			} catch (Exception e) {
				throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchListBy.{%s}", Daoname, Utils.getErrorStr(e));
			}
			tmpresult.addAll(rets);
		}
		session.clear();
		List<Map<String, Object>> allresult = new ArrayList<Map<String, Object>>();
		for(int i = 0; i < tmpresult.size(); i++){
			Object[] ret = tmpresult.get(i);
			try {
				if(ret.length != clazzs.length+1)
					throw new Exception("没有返回预期个数的Entity");
				Map<String, Object> resultitem = new HashMap<String, Object>();
				resultitem.put(tClassname, ret[0]);
				T tobject = tClass.cast(ret[0]);
				long time = tobject.getCreatedat();
				resultitem.put("time", time);
				for(int j = 0; j < clazzs.length; j++){
					String Clazzname = clazzs[j].getSimpleName();
					resultitem.put(Clazzname, ret[j+1]);
				}
				allresult.add(resultitem);
			} catch (Exception e) {
				throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchListBy.{%s}", Daoname, "JOIN查询错误:"+Utils.getErrorStr(e));
			}
		}
		
		if(order == MulTabBaseDaoImpl.QUERY_ORDER_DESC){
			for(int i = 0; i < allresult.size(); i++){
				long maxtime = 0L;
				int maxindex = 0;
				for(int j = 0; j < allresult.size(); j++){
					long nowtime = (long)allresult.get(j).get("time");
					if(maxtime < nowtime){
						maxindex = j;
						maxtime = nowtime;
					}
				}
				Map<String, Object> tmpmap = allresult.get(maxindex);
				allresult.remove(maxindex);
				allresult.add(0, tmpmap);
			}
		}
		if(order == MulTabBaseDaoImpl.QUERY_ORDER_ASC){
			for(int i = 0; i < allresult.size(); i++){
				long mintime = Long.MAX_VALUE;
				int minindex = 0;
				for(int j = 0; j < allresult.size(); j++){
					long nowtime = (long)allresult.get(j).get("time");
					if(mintime > nowtime){
						minindex = j;
						mintime = nowtime;
					}
				}
				Map<String, Object> tmpmap = allresult.get(minindex);
				allresult.remove(minindex);
				allresult.add(0, tmpmap);
			}
		}
		if(limit != null){
			if(page == null)
				page = 1;
			int firstindex = (page - 1) * limit;
			int lastindex = firstindex + limit - 1;
			if(lastindex > (allresult.size() - 1))
				lastindex = allresult.size() - 1;
			for(int i = firstindex; i <= lastindex; i++)
				result.add(allresult.get(i));
		}else {
			result.addAll(allresult);
		}
	}
	
	public long selectCount(List<QueryBean> paramList, boolean needVaild, Class<?>... clazzs) throws DaoException {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.selectCount,session为空", Daoname);
		}
		if(paramList == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.selectCount,paramMap为空", Daoname);
		}
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		queryList.addAll(paramList);
		if(needVaild)
			queryList.add(new QueryBean(tClass.getName(), tAliasname, "datastatus", Bean.CREATED, QueryBean.TYPE_EQUAL));
		try {
			long total = 0;
			int beginindex = 0, endindex = 9;
			for(int i = 0; i < queryList.size(); i++){
				if(queryList.get(i).getField().equals("sid")){
					Long tmpsid = null;
					try {
						tmpsid = Long.parseLong(queryList.get(i).getValue());
					} catch (Exception e) {
						throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.getListBy,sid格式不正确", Daoname);
					}
					int tableid = (int) (tmpsid % 10); 
					beginindex = tableid;
					endindex = tableid;
					break;
				}
			}
			for(int i = beginindex; i <= endindex; i++){
				String tablename = pretname + String.valueOf(i);
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT COUNT(*) FROM ").append(tablename).append(" ").append(tAliasname);
				for(int j = 0; j < clazzs.length; j++){
					String Clazzname = clazzs[j].getSimpleName();
					String clazzname = Clazzname.toLowerCase();
					sb.append(" LEFT JOIN ").append(clazzname)
						.append(" ON ( ").append(tAliasname).append(".`").append(clazzname).append("sid` = ")
						.append(clazzname).append(".`sid`");
					if(needVaild)
						sb.append("and ").append(clazzname).append(".`datastatus` = ").append(Bean.CREATED);
					sb.append(" )");
				}
				List<Object> retList = new ArrayList<Object>();
				sb.append(DaoUtils.Gene_Query_Sql(queryList, retList));
				@SuppressWarnings("rawtypes")
				Query query = session.createNativeQuery(sb.toString());
				for(int j = 0; j < retList.size(); j++)
					query.setParameter(j+1, retList.get(j));
				@SuppressWarnings("unchecked")
				List<BigInteger> tmplist = query.list();
				if(tmplist.size() != 1){
					throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.selectCount.{%s}", Daoname, "SELECT COUNT返回的结果不是一项");
				}
				total += tmplist.get(0).longValue();
			}		
			session.clear();
			return total;
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.selectCount.{%s}", Daoname, Utils.getErrorStr(e));
		}
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
