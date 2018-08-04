package com.Entitys.Trucklog.Dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import com.Common.Daos.MulTabBaseDaoImpl;
import com.Common.Daos.QueryBean;
import com.Common.Entitys.Bean;
import com.Common.Exceptions.DaoException;
import com.Common.Utils.DaoUtils;
import com.Entitys.Trucklog.Entity.LastTruckLog;
import com.Entitys.Trucklog.Entity.TruckLog;

public class TrucklogDaoImpl extends MulTabBaseDaoImpl<TruckLog> implements TrucklogDao {

	public TrucklogDaoImpl(Session session) {
		super(session, TruckLog.class);
		this.pretname = "trucklog_";
		this.tAliasname = "trucklog";
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public long insert(TruckLog truckLog, Long operatersid) throws DaoException {
		long sid = super.insert(truckLog, operatersid);
		long corporationsid = truckLog.getCorporationsid();
		String LastTrucklogTableName = "lasttrucklog_" + String.valueOf(corporationsid % 10);
		String select_sql = "SELECT * FROM " + LastTrucklogTableName + " WHERE corporationsid = " + corporationsid
				+ " and datastatus = " + Bean.CREATED;
		Query<LastTruckLog> check_query = session.createNativeQuery(select_sql, LastTruckLog.class);
		List<LastTruckLog> check_rets = check_query.getResultList();
		if (check_rets.size() > 1) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.insert.{%s}.{%s}", Daoname,
					check_query.getQueryString(), "数据库返回多个LastTruckLog");
		}
		if (check_rets.size() == 0) {// insert
			LastTruckLog lastTruckLog = new LastTruckLog(truckLog);
			List<Object> retList = new ArrayList<Object>();
			Query<LastTruckLog> query = session
					.createNativeQuery(DaoUtils.Gene_Insert_Sql(lastTruckLog, retList, LastTrucklogTableName))
					.addEntity(LastTruckLog.class);
			for (int i = 0; i < retList.size(); i++) {
				query.setParameter(i + 1, retList.get(i));
			}
			if (query.executeUpdate() < 0)
				throw DaoException.DB_INSERT_EXCEPTION.newInstance("In %s.insert.{%s}.{%s}", Daoname,
						query.getQueryString(), "数据库0行记录受到影响");
		} else {// update
			LastTruckLog lastTruckLog = check_rets.get(0);
			lastTruckLog.updateFromTruckLog(truckLog);
			List<Object> retList = new ArrayList<Object>();
			Query<LastTruckLog> query = session
					.createNativeQuery(DaoUtils.Gene_Update_Sql(lastTruckLog, retList, LastTrucklogTableName))
					.addEntity(LastTruckLog.class);
			retList.add(retList.size(), lastTruckLog.getSid());
			for (int i = 0; i < retList.size(); i++) {
				query.setParameter(i + 1, retList.get(i));
			}
			if (query.executeUpdate() < 0)
				throw DaoException.DB_INSERT_EXCEPTION.newInstance("In %s.insert.{%s}.{%s}", Daoname,
						query.getQueryString(), "数据库0行记录受到影响");
		}
		session.clear();
		return sid;
	}

	@Override
	public int update(TruckLog truckLog, String oldobject, Long operatersid) throws Exception {
		throw DaoException.DB_UPDATE_EXCEPTION.newInstance("In %s.delete.{%s}", Daoname, "行车日志不能修改");
	}
	
	@Override
	public int delete(TruckLog truckLog, Long operatersid) throws Exception {
		throw DaoException.DB_UPDATE_EXCEPTION.newInstance("In %s.delete.{%s}", Daoname, "行车日志不能删除");
	}

	@Override
	public List<TruckLog> getByCorporation(Long corporationsid, List<QueryBean> queryList, Integer limit,
			Integer page, Integer order, String orderfield) {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.getByCorporation,session为空", Daoname);
		}
		if (queryList == null) {
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.getByCorporation,queryList为空", Daoname);
		}
		if (corporationsid == null) {
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.getByCorporation,corporationsid为空", Daoname);
		}
		try {
			String LastTrucklogTableName = "lasttrucklog_" + String.valueOf(corporationsid % 10);
			queryList.add(new QueryBean(LastTruckLog.class.getName(), "lastlog", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
			queryList.add(new QueryBean(LastTruckLog.class.getName(), "lastlog", "datastatus", Bean.CREATED, QueryBean.TYPE_EQUAL));
			StringBuilder select_sql = new StringBuilder();
			select_sql.append("SELECT * FROM ").append(LastTrucklogTableName).append(" lastlog");
			List<Object> retList = new ArrayList<Object>();
			select_sql.append(DaoUtils.Gene_Query_Sql(queryList, retList));
			select_sql.append(" ORDER BY lastlog.`createdat` DESC");
			if (limit != null) {
				if (page == null)
					page = 1;
				select_sql.append(" LIMIT ").append((page - 1) * limit).append(", ").append(limit);
			}
			Query<LastTruckLog> query = session.createNativeQuery(select_sql.toString(), LastTruckLog.class);
			for (int i = 0; i < retList.size(); i++)
				query.setParameter(i + 1, retList.get(i));
			List<LastTruckLog> tmpret = query.getResultList();
			List<TruckLog> result = new ArrayList<TruckLog>();
			for (int i = 0; i < tmpret.size(); i++) {
				result.add(tmpret.get(i).getTruckLog());
			}
			session.clear();
			return result;
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.getByCorporation.{%s}", Daoname, e.getMessage());
		}
	}

	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	@Override
	public void fetchByCorporation(Long corporationsid, List<QueryBean> queryList, boolean needVaild, Integer limit, Integer page,
			Integer order, String orderfield, List<Map<String, Object>> result, Class<?>... clazzs) {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.fetchByCorporation,session为空", Daoname);
		}
		if (queryList == null) {
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.fetchByCorporation,queryList为空", Daoname);
		}
		if(result == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.fetchByCorporation,resultList为空", Daoname);
		}
		if(order == null || orderfield == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.fetchByCorporation,ORDER参数为空", Daoname);
		}
		result.clear();
		if(clazzs.length == 0){
			List<TruckLog> tmpret = getByCorporation(corporationsid, queryList, limit, page, order, orderfield);
			for(int i = 0; i < tmpret.size(); i++){
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("TruckLog", tmpret.get(i));
				result.add(i, tmpMap);
			}
			return;
		}
		if(needVaild)
			queryList.add(new QueryBean(LastTruckLog.class.getName(), "lastlog", "datastatus", Bean.CREATED, QueryBean.TYPE_EQUAL));
		String LastTrucklogTableName = "lasttrucklog_" + String.valueOf(corporationsid % 10);
		List<Object[]> rets = null;
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		sb1.append("SELECT {lastlog.*},");
		sb2.append(" FROM ").append(LastTrucklogTableName).append(" lastlog");
		for(int i = 0; i < clazzs.length; i++){
			Class<?> clazz = clazzs[i];
			String[] tmp = clazz.getName().split("\\.");
			String Clazzname = tmp[tmp.length - 1];
			String clazzname = Clazzname.toLowerCase();
			sb1.append(" {").append(clazzname).append(".*},");
			sb2.append(" LEFT JOIN ").append(clazzname)
				.append(" ON ( lastlog.`").append(clazzname).append("sid` = ")
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
			throw e.newInstance("In %s.fetchByCorporation.{%s}", Daoname, e.getMessage());
		}
		sb2.append(query_sql);
		sb2.append(" ORDER BY lastlog.`"+orderfield+"` ");
		if(order == MulTabBaseDaoImpl.QUERY_ORDER_DESC)
			sb2.append("DESC");
		else
			sb2.append("ASC");
		if(limit != null){
			if(page == null)
				page = 1;
			sb2.append(" LIMIT ").append((page-1)*limit).append(", ").append(limit);					
		}
		String select_sql = sb1.toString()+sb2.toString();
		try {
			NativeQuery query = session.createNativeQuery(select_sql).addEntity("lastlog", LastTruckLog.class);
			for(int i = 0; i < clazzs.length; i++){
				String Clazzname = clazzs[i].getSimpleName();
				String clazzname = Clazzname.toLowerCase();
				query.addEntity(clazzname, clazzs[i]);
			}
			for(int i = 0; i < retList.size(); i++)
				query.setParameter(i+1, retList.get(i));
			rets = query.list();
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchByCorporation.{%s}", Daoname, e.getMessage());
		}
		session.clear();
		if(rets == null)
			return;
		for(int i = 0; i < rets.size(); i++){
			Object[] ret = rets.get(i);
			try {
				if(ret.length != clazzs.length+1)
					throw new Exception("没有返回预期个数的Entity");
				Map<String, Object> resultitem = new HashMap<String, Object>();
				LastTruckLog lastTruckLog = (LastTruckLog) ret[0];
				resultitem.put("TruckLog", lastTruckLog.getTruckLog());
				for(int j = 0; j < clazzs.length; j++){
					Class clazz = clazzs[j];
					String[] tmp = clazz.getName().split("\\.");
					String Clazzname = tmp[tmp.length - 1];
					resultitem.put(Clazzname, ret[j+1]);
				}
				result.add(resultitem);
			} catch (Exception e) {
				throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchByCorporation.{%s}", Daoname, "JOIN查询错误:"+e.getMessage());
			}
		}
	}

	@Override
	public void fetchByCorporation(Long corporationsid, List<QueryBean> queryList, boolean needVaild, Integer limit,
			Integer page, List<Map<String, Object>> result, Class<?>... clazzs) {
		fetchByCorporation(corporationsid, queryList, needVaild, limit, page, MulTabBaseDaoImpl.QUERY_ORDER_DESC, "createdat", result, clazzs);
		
	}

	@Override
	public long selectCountByCorporation(Long corporationsid, List<QueryBean> queryList, boolean needVaild) {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.selectCountByCorporation,session为空", Daoname);
		}
		if (queryList == null) {
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.selectCountByCorporation,queryList为空", Daoname);
		}
		if (corporationsid == null) {
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.selectCountByCorporation,corporationsid为空", Daoname);
		}
		try {
			String LastTrucklogTableName = "lasttrucklog_" + String.valueOf(corporationsid % 10);
			queryList.add(new QueryBean(LastTruckLog.class.getName(), "lastlog", "corporationsid", corporationsid, QueryBean.TYPE_EQUAL));
			if(needVaild)
				queryList.add(new QueryBean(LastTruckLog.class.getName(), "lastlog", "datastatus", Bean.CREATED, QueryBean.TYPE_EQUAL));
			StringBuilder select_sql = new StringBuilder();
			select_sql.append("SELECT COUNT(*) FROM ").append(LastTrucklogTableName).append(" lastlog");
			List<Object> retList = new ArrayList<Object>();
			select_sql.append(DaoUtils.Gene_Query_Sql(queryList, retList));
			@SuppressWarnings("rawtypes")
			Query query = session.createNativeQuery(select_sql.toString());
			for (int i = 0; i < retList.size(); i++)
				query.setParameter(i + 1, retList.get(i));
			@SuppressWarnings("unchecked")
			List<BigInteger> tmplist = query.list();
			if(tmplist.size() != 1){
				throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.selectCountByCorporation.{%s}", Daoname, "SELECT COUNT返回的结果不是一项");
			}
			session.clear();
			return tmplist.get(0).longValue();
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.selectCountByCorporation.{%s}", Daoname, e.getMessage());
		}
	}

}
