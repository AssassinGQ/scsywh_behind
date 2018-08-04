package com.Entitys.Trucklog.Dao;

import java.util.List;
import java.util.Map;

import com.Common.Daos.BaseDao;
import com.Common.Daos.QueryBean;
import com.Entitys.Trucklog.Entity.TruckLog;

public interface TrucklogDao extends BaseDao<TruckLog> {
	public List<TruckLog> getByCorporation(Long corporationsid, List<QueryBean> queryList, Integer limit, Integer page, Integer order, String orderfield);
	public void fetchByCorporation(Long corporationsid, List<QueryBean> queryList, boolean needVaild, Integer limit, Integer page, List<Map<String, Object>> result, Class<?>... clazzs);
	public void fetchByCorporation(Long corporationsid, List<QueryBean> queryList, boolean needVaild, Integer limit, Integer page, Integer order, String orderfield, List<Map<String, Object>> result, Class<?>... clazzs);
	public long selectCountByCorporation(Long corporationsid, List<QueryBean> queryList, boolean needVaild);
}
