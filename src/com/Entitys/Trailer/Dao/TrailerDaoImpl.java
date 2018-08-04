package com.Entitys.Trailer.Dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import com.Common.Daos.QueryBean;
import com.Common.Daos.SingTabBaseDaoImpl;
import com.Common.Exceptions.DaoException;
import com.Entitys.Trailer.Entity.Trailer;

public class TrailerDaoImpl extends SingTabBaseDaoImpl<Trailer> implements TrailerDao {

	public TrailerDaoImpl(Session session) {
		super(session, Trailer.class);
		this.pretname = "trailer_";
		this.tAliasname = "trailer";
	}

	@Override
	public Trailer getByTrailernumber(String trailernumber, boolean needVaild) {
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		queryList.add(new QueryBean(Trailer.class.getName(), "trailer", "trailernumber", trailernumber, QueryBean.TYPE_EQUAL));
		List<Trailer> trailers = this.getListBy(queryList, true);
		if(trailers.size() == 0)
			return null;
		if(trailers.size() > 1)
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.getByTrailernumber.{%s}", Daoname, "拖车数据库出错，车牌号"+trailernumber+"重复");
		return trailers.get(0);
	}
	
}
