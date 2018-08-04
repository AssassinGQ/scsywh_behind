package com.Dao.BaseDao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import com.Entitys.BaseEntity.Bean;
import com.exception.DaoException;
import com.utils.DaoUtils;
import com.utils.SnowFlake;
import com.utils.Utils;

public abstract class BaseDaoImpl<T extends Bean> implements BaseDao<T> {
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

	public BaseDaoImpl(Session session, Class<T> tclazz) {
		this.session = session;
		tClass = tclazz;
		{
			String[] tmp = tclazz.getName().split("\\.");
			tClassname = tmp[tmp.length - 1];
		}
		{
			String[] tmp = this.getClass().getName().split("\\.");
			Daoname = tmp[tmp.length - 1];	
		}
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public long insert(T object) throws DaoException {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.insert,session为空", Daoname);
		}
		try {
			TestUtils.ValidationWithExp(object);
		} catch (Exception e) {
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.insert.{%s}", Daoname, e.getMessage());
		}
		SnowFlake snowFlake = new SnowFlake(0, SnowFlake.FAREFORM_DATAID);
		long sid = snowFlake.nextId();
		object.setSid(sid);
		Query<T> query = null;
		int result = 0;
		try {
			String tablename = pretname + String.valueOf(sid % 10);
			List<Object> retList = new ArrayList<Object>();
			query = session.createNativeQuery(DaoUtils.Gene_Insert_Sql(object, retList, tablename)).addEntity(tClass);
			for (int i = 0; i < retList.size(); i++) {
				query.setParameter(i + 1, retList.get(i));
			}
			result = query.executeUpdate();
			session.clear();
		} catch (Exception e) {
			if(query == null)
				throw DaoException.DB_INSERT_EXCEPTION.newInstance("In %s.insert.{%s}", Daoname, e.getMessage());
			else
				throw DaoException.DB_INSERT_EXCEPTION.newInstance("In %s.insert.{%s}.{%s}", Daoname, query.getQueryString(), e.getMessage());		}
		if (result <= 0) {
			throw DaoException.DB_INSERT_EXCEPTION.newInstance("In %s.insert.{%s}.{%s}", Daoname, query.getQueryString(), "数据库0行记录受到影响");
		}
		return sid;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public int update(T object) throws DaoException {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.update,session为空", Daoname);
		}
		if(object == null)
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.update,%s为空", Daoname, tClassname);
		try {
			TestUtils.ValidationWithExp(object);
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
			query = session.createNativeQuery(DaoUtils.Gene_Update_Sql(object, retList, tablename)).addEntity(tClass);
			retList.add(retList.size(), sid);
			for (int i = 0; i < retList.size(); i++) {
				query.setParameter(i + 1, retList.get(i));
			}
			result = query.executeUpdate();
			session.clear();
		} catch (Exception e) {
			if(query == null)
				throw DaoException.DB_UPDATE_EXCEPTION.newInstance("In %s.update.{%s}", Daoname, e.getMessage());
			else
				throw DaoException.DB_INSERT_EXCEPTION.newInstance("In %s.update.{%s}.{%s}", Daoname, query.getQueryString(), e.getMessage());
		}
		if (result <= 0) {
			throw DaoException.DB_INSERT_EXCEPTION.newInstance("In %s.update.{%s}.{%s}", Daoname, query.getQueryString(), "数据库0行记录受到影响");
		}
		return result;
	}
	//根据sid条件查询唯一Order
	@SuppressWarnings({ "deprecation", "unchecked" })
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
			Query<T> query = session.createNativeQuery(select_sql).addEntity(tClass);
			object = query.uniqueResult();
			session.clear();
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.getById.{%s}", Daoname, e.getMessage());
		}
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
			Class clazz = clazzs[i];
			String[] tmp = clazz.getName().split("\\.");
			String Clazzname = tmp[tmp.length - 1];
			String clazzname = Clazzname.toLowerCase();
			sb1.append(" {").append(clazzname).append(".*},");
			sb2.append(" LEFT JOIN ").append(Clazzname).append(" ").append(clazzname)
				.append(" ON ( ").append(tAliasname).append(".`").append(clazzname).append("sid` = ")
				.append(clazzname).append(".`sid`");
			if(needVaild)
				sb2.append("and ").append(clazzname).append(".`datastatus` = ").append(Bean.CREATED);
			sb2.append(" )");
		}
		sb1.deleteCharAt(sb1.length()-1);
		sb2.append(" WHERE ").append(tAliasname).append(".`sid` = ").append(sid).append(" and ").append(tAliasname).append(".`datastatus` = ").append(Bean.CREATED);
		String select_sql = sb1.toString()+sb2.toString();
		try {
			NativeQuery query = session.createNativeQuery(select_sql).addEntity(tAliasname, tClass);
			for(int i = 0; i < clazzs.length; i++){
				Class clazz = clazzs[i];
				String[] tmp = clazz.getName().split("\\.");
				String Clazzname = tmp[tmp.length - 1];
				String clazzname = Clazzname.toLowerCase();
				query.addEntity(clazzname, clazz);
			}
			rets = query.list();
			session.clear();
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchById.{%s}", Daoname, e.getMessage());
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
				Class clazz = clazzs[i];
				String[] tmp = clazz.getName().split("\\.");
				String Clazzname = tmp[tmp.length - 1];
				result.put(Clazzname, ret[i+1]);
			}
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchById.{%s}", Daoname, "JOIN查询错误:"+e.getMessage());
		}
	}
	//根据paramMap条件查询Order
	public List<T> getListBy(Map<String, String> paramMap, boolean needVaild) throws DaoException {
		return getListBy(paramMap, needVaild, null, null);
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public List<T> getListBy(Map<String, String> paramMap, boolean needVaild, Integer limit, Integer page) throws DaoException {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.listBy,session为空", Daoname);
		}
		if(paramMap == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.listBy,paramMap为空", Daoname);
		}
		if(needVaild)
			paramMap.put("datastatus", String.valueOf(Bean.CREATED));
		List<T> ret = new ArrayList<T>();
		try {
			for(int i = 0; i < 10; i++){
				String tablename = pretname + String.valueOf(i);
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT * FROM ").append(tablename);
				List<Object> retList = new ArrayList<Object>();
				sb.append(DaoUtils.Gene_Query_Sql(paramMap, tClass, retList, tablename));
				sb.append(" ORDER BY "+tablename+".`createdat` DESC");
				if(limit != null){
					if(page == null)
						page = 1;
					sb.append(" LIMIT ").append((page-1)*limit).append(", ").append(limit);					
				}
				Query<T> query = session.createNativeQuery(sb.toString()).addEntity(tClass);
				for(int j = 0; j < retList.size(); j++)
					query.setParameter(j+1, retList.get(j));
				ret.addAll(query.getResultList());
			}	
			session.clear();
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.listBy.{%s}", Daoname, e.getMessage());
		}
		return ret;
	}
	//根据paramMap条件查询Order，并将clazzs对应的关联对象同时join查询
	public void fetchListBy(Map<String, String> paramMap, List<Map<String, Object>> result, boolean needVaild, Class<?>... clazzs) throws DaoException {
		fetchListBy(paramMap, result, needVaild, null, null, clazzs);
	}

	@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
	public void fetchListBy(Map<String, String> paramMap, List<Map<String, Object>> result, boolean needVaild, Integer limit, Integer page, Class<?>... clazzs) throws DaoException {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.fetchListBy,session为空", Daoname);
		}
		if(paramMap == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.fetchListBy,paramMap为空", Daoname);
		}
		if(result == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.fetchListBy,resultList为空", Daoname);
		}
		result.clear();
		if(clazzs.length == 0){
			List<T> tmpret = getListBy(paramMap, needVaild, limit, page);
			for(int i = 0; i < tmpret.size(); i++){
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put(tClassname, tmpret.get(i));
				result.add(i, tmpMap);
			}
			return;
		}
		List<Object[]> tmpresult = new ArrayList<Object[]>();
		if(needVaild)
			paramMap.put("datastatus", String.valueOf(Bean.CREATED));
		for(int j = 0; j < 10; j++){
			List<Object[]> rets = null;
			String tablename = pretname + String.valueOf(j);
			StringBuilder sb1 = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			sb1.append("SELECT {").append(tAliasname).append(".*},");
			sb2.append(" FROM ").append(tablename).append(" ").append(tAliasname);
			for(int i = 0; i < clazzs.length; i++){
				Class<?> clazz = clazzs[i];
				String[] tmp = clazz.getName().split("\\.");
				String Clazzname = tmp[tmp.length - 1];
				String clazzname = Clazzname.toLowerCase();
				sb1.append(" {").append(clazzname).append(".*},");
				sb2.append(" LEFT JOIN ").append(Clazzname).append(" ").append(clazzname)
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
				DaoUtils.Gene_Query_Sql(paramMap, tClass, retList, tAliasname);
			} catch (DaoException e) {
				throw e.newInstance("In %s.fetchListBy.{%s}", Daoname, e.getMessage());
			}
			sb2.append(query_sql);
			sb2.append(" ORDER BY ").append(tAliasname).append(".`createdat` DESC");
			if(limit != null){
				if(page == null)
					page = 1;
				sb2.append(" LIMIT ").append((page-1)*limit).append(", ").append(limit);					
			}
			String select_sql = sb1.toString()+sb2.toString();
			try {
				NativeQuery query = session.createNativeQuery(select_sql).addEntity(tAliasname, tClass);
				for(int i = 0; i < clazzs.length; i++){
					Class clazz = clazzs[i];
					String[] tmp = clazz.getName().split("\\.");
					String Clazzname = tmp[tmp.length - 1];
					String clazzname = Clazzname.toLowerCase();
					query.addEntity(clazzname, clazz);
				}
				for(int i = 0; i < retList.size(); i++)
					query.setParameter(i+1, retList.get(i));
				rets = query.list();
			} catch (Exception e) {
				throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchListBy.{%s}", Daoname, e.getMessage());
			}
			tmpresult.addAll(rets);
		}
		session.clear();
		for(int i = 0; i < tmpresult.size(); i++){
			Object[] ret = tmpresult.get(i);
			try {
				if(ret.length != clazzs.length+1)
					throw new Exception("没有返回预期个数的Entity");
				Map<String, Object> resultitem = new HashMap<String, Object>();
				resultitem.put(tClassname, ret[0]);
				for(int j = 0; j < clazzs.length; j++){
					Class clazz = clazzs[j];
					String[] tmp = clazz.getName().split("\\.");
					String Clazzname = tmp[tmp.length - 1];
					resultitem.put(Clazzname, ret[i+1]);
				}
				result.add(resultitem);
			} catch (Exception e) {
				throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchListBy.{%s}", Daoname, "JOIN查询错误:"+e.getMessage());
			}
		}
	}
	
	public long selectCount(Map<String, String> paramMap, boolean needVaild) throws DaoException {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.selectCount,session为空", Daoname);
		}
		if(paramMap == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.selectCount,paramMap为空", Daoname);
		}
		if(needVaild)
			paramMap.put("datastatus", String.valueOf(Bean.CREATED));
		try {
			long total = 0;
			for(int i = 0; i < 10; i++){
				String tablename = pretname + String.valueOf(i);
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT COUNT(*) FROM ").append(tablename);
				List<Object> retList = new ArrayList<Object>();
				sb.append(DaoUtils.Gene_Query_Sql(paramMap, tClass, retList, tablename));
				@SuppressWarnings("rawtypes")
				Query query = session.createNativeQuery(sb.toString());
				for(int j = 0; j < retList.size(); j++)
					query.setParameter(j+1, retList.get(j));
				@SuppressWarnings("unchecked")
				List<BigInteger> tmplist = query.list();
				if(tmplist.size() != 1){
					throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.selectCount.{%s}", Daoname, "SELECT COUNT得到多个结果");
				}
				total += tmplist.get(0).longValue();
			}		
			session.clear();
			return total;
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.selectCount.{%s}", Daoname, e.getMessage());
		}
	}
}
