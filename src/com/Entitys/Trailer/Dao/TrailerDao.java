package com.Entitys.Trailer.Dao;

import com.Common.Daos.BaseDao;
import com.Entitys.Trailer.Entity.Trailer;

public interface TrailerDao extends BaseDao<Trailer> {
	public Trailer getByTrailernumber(String trucknumber, boolean needVaild);
}
