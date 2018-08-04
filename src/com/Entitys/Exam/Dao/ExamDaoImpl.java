package com.Entitys.Exam.Dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import com.Common.Daos.QueryBean;
import com.Common.Daos.SingTabBaseDaoImpl;
import com.Common.Entitys.Bean;
import com.Common.Exceptions.DaoException;
import com.Common.Utils.DaoUtils;
import com.Common.Utils.Utils;
import com.Entitys.Driver.Entity.Driver;
import com.Entitys.Exam.Entity.Exam;
import com.Entitys.Exam.Entity.ScoreHistory;

public class ExamDaoImpl extends SingTabBaseDaoImpl<Exam> implements ExamDao {

	public ExamDaoImpl(Session session) {
		super(session, Exam.class);
		this.pretname = "exam_";
		this.tAliasname = "exam";
	}

	@Override
	public List<ScoreHistory> getScoreHistory(List<QueryBean> paramList, boolean needVaild, Integer limit, Integer page) throws Exception {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.getScoreHistory,session为空", Daoname);
		}
		if(paramList == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.getScoreHistory,paramList为空", Daoname);
		}
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		queryList.addAll(paramList);
		if(needVaild)
			queryList.add(new QueryBean(ScoreHistory.class.getName(), "scorehistory", "datastatus", Bean.CREATED, QueryBean.TYPE_EQUAL));
		List<ScoreHistory> ret = new ArrayList<ScoreHistory>();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT * FROM scorehistory");
			List<Object> retList = new ArrayList<Object>();
			sb.append(DaoUtils.Gene_Query_Sql(queryList, retList));
			sb.append(" ORDER BY scorehistory.`createdat` DESC");
			if(limit != null){
				if(page == null)
					page = 1;
				sb.append(" LIMIT ").append((page-1)*limit).append(", ").append(limit);					
			}
			Query<ScoreHistory> query = session.createNativeQuery(sb.toString(), ScoreHistory.class);
			for(int j = 0; j < retList.size(); j++){
				query.setParameter(j+1, retList.get(j));
			}
			ret.addAll(query.getResultList());
			session.clear();
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.getScoreHistory.{%s}", Daoname, Utils.getErrorStr(e));
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> fetchScoreHistory(List<QueryBean> paramList, boolean needVaild, Integer limit, Integer page) throws Exception {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.fetchScoreHistory,session为空", Daoname);
		}
		if(paramList == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.fetchScoreHistory,paramList为空", Daoname);
		}
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		queryList.addAll(paramList);
		if(needVaild)
			queryList.add(new QueryBean(ScoreHistory.class.getName(), "scorehistory", "datastatus", Bean.CREATED, QueryBean.TYPE_EQUAL));
		List<Object[]> ret = new ArrayList<Object[]>();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT {scorehistory.*}, {exam.*}, {driver.*} FROM scorehistory scorehistory LEFT JOIN exam exam ON (scorehistory.`exam_sid` = exam.`sid`");
			if(needVaild)
				sb.append(" AND exam.`datastatus` = ").append(Bean.CREATED);
			sb.append(") LEFT JOIN driver driver ON (scorehistory.`user_sid` = driver.`sid`");
			if(needVaild)
				sb.append(" AND driver.`datastatus` = ").append(Bean.CREATED);
			sb.append(")");
			List<Object> retList = new ArrayList<Object>();
			sb.append(DaoUtils.Gene_Query_Sql(queryList, retList));
			sb.append(" ORDER BY scorehistory.`createdat` DESC");
			if(limit != null){
				if(page == null)
					page = 1;
				sb.append(" LIMIT ").append((page-1)*limit).append(", ").append(limit);					
			}
			System.err.println(sb.toString());
			@SuppressWarnings({ "deprecation", "rawtypes" })
			NativeQuery query = session.createNativeQuery(sb.toString())
							.addEntity("scorehistory", ScoreHistory.class)
							.addEntity("driver", Driver.class)
							.addEntity("exam", Exam.class);
			for(int j = 0; j < retList.size(); j++){
				query.setParameter(j+1, retList.get(j));
			}
			ret = query.list();
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchScoreHistory.{%s}", Daoname, Utils.getErrorStr(e));
		}
		session.clear();
		List<Map<String, Object>> result = new ArrayList<>();
		for(int i = 0; i < ret.size(); i++){
			Object[] item = ret.get(i);
			try {
				if(item.length != 3)
					throw new Exception("没有返回预期个数的Entity");
				Map<String, Object> resultitem = new HashMap<String, Object>();
				resultitem.put("Scorehistory", item[0]);
				resultitem.put("Driver", item[1]);
				resultitem.put("Exam", item[2]);
				result.add(resultitem);
			} catch (Exception e) {
				throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.fetchListBy.{%s}", Daoname, "JOIN查询错误:"+Utils.getErrorStr(e));
			}
		}
		return result;
	}
	
	@Override
	public long selectScoreHistoryCount(List<QueryBean> paramList, boolean needVaild) throws DaoException {
		if (session == null) {
			throw DaoException.DB_SESSION_NULL.newInstance("In %s.selectScoreHistoryCount,session为空", Daoname);
		}
		if(paramList == null){
			throw DaoException.DB_INPUT_EXCEPTION.newInstance("In %s.selectCount,paramMap为空", Daoname);
		}
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		queryList.addAll(paramList);
		if(needVaild)
			queryList.add(new QueryBean(ScoreHistory.class.getName(), "scorehistory", "datastatus", Bean.CREATED, QueryBean.TYPE_EQUAL));
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT COUNT(*) FROM scorehistory");
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
			long total = tmplist.get(0).longValue();	
			session.clear();
			return total;
		} catch (Exception e) {
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.selectCount.{%s}", Daoname, Utils.getErrorStr(e));
		}
	}
}
