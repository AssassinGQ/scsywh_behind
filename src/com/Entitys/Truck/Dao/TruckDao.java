package com.Entitys.Truck.Dao;

import com.Common.Daos.BaseDao;
import com.Entitys.Truck.Entity.Truck;

public interface TruckDao extends BaseDao<Truck> {
	public Truck getByTrucknumber(String trucknumber, boolean needVaild);
}
