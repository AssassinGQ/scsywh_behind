package com.Entitys.Truck.Dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import com.Common.Daos.QueryBean;
import com.Common.Daos.SingTabBaseDaoImpl;
import com.Common.Exceptions.DaoException;
import com.Entitys.Truck.Entity.Truck;

public class TruckDaoImpl extends SingTabBaseDaoImpl<Truck> implements TruckDao {
	public TruckDaoImpl(Session session) {
		super(session, Truck.class);
		this.pretname = "truck_";
		this.tAliasname = "truck";
	}

	@Override
	public Truck getByTrucknumber(String trucknumber, boolean needVaild) throws DaoException {
		List<QueryBean> queryList = new ArrayList<QueryBean>();
		queryList.add(new QueryBean(Truck.class.getName(), "truck", "trucknumber", trucknumber, QueryBean.TYPE_EQUAL));
		List<Truck> trucks = this.getListBy(queryList, true);
		if(trucks.size() == 0)
			return null;
		if(trucks.size() > 1)
			throw DaoException.DB_QUERY_EXCEPTION.newInstance("In %s.getByTrucknumber.{%s}", Daoname, "拖车数据库出错，车牌号"+trucknumber+"重复");
		return trucks.get(0);
	}
}
