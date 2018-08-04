package com.Entitys.Exam.Dao;

import java.util.List;
import java.util.Map;

import com.Common.Daos.BaseDao;
import com.Common.Daos.QueryBean;
import com.Common.Exceptions.DaoException;
import com.Entitys.Exam.Entity.Exam;
import com.Entitys.Exam.Entity.ScoreHistory;

public interface ExamDao extends BaseDao<Exam> {
	public List<ScoreHistory> getScoreHistory(List<QueryBean> paramList, boolean needVaild, Integer limit, Integer page) throws Exception;
	public List<Map<String, Object>> fetchScoreHistory(List<QueryBean> paramList, boolean needVaild, Integer limit, Integer page) throws Exception;
	public long selectScoreHistoryCount(List<QueryBean> paramList, boolean needVaild) throws DaoException;
}
