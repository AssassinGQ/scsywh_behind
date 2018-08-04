package com.Common.Daos;

import java.util.List;
import java.util.Map;

public interface BaseDao<T> {

	public long insert(T object, Long operatersid) throws Exception;

	public int update(T object, String oldobject, Long operatersid) throws Exception;

	public int delete(T object, Long operatersid) throws Exception;
	// 根据sid条件查询唯一Order
	public T getById(long sid, boolean needVaild) throws Exception;

	// 根据sid条件查询唯一Order，并将clazzs对应的关联对象同时join查询
	public void fetchById(long sid, Map<String, Object> result, boolean needVaild, Class<?>... clazzs) throws Exception;

	public List<T> getListBy(List<QueryBean> paramList, boolean needVaild) throws Exception;

	public List<T> getListBy(List<QueryBean> paramList, boolean needVaild, Integer limit, Integer page) throws Exception;
	
	public List<T> getListBy(List<QueryBean> paramList, boolean needVaild, Integer limit, Integer page, Integer order, String orderfield) throws Exception;

	// 根据paramMap条件查询Order，并将clazzs对应的关联对象同时join查询
	public void fetchListBy(List<QueryBean> paramList, List<Map<String, Object>> result, boolean needVaild,
                            Class<?>... clazzs) throws Exception;

	public void fetchListBy(List<QueryBean> paramList, List<Map<String, Object>> result, boolean needVaild,
                            Integer limit, Integer page, Class<?>... clazzs) throws Exception;
	
	public void fetchListBy(List<QueryBean> paramList, List<Map<String, Object>> result, boolean needVaild,
                            Integer limit, Integer page, Integer order, String orderfield, Class<?>... clazzs) throws Exception;

	public long selectCount(List<QueryBean> paramList, boolean needVaild, Class<?>... clazzs) throws Exception;
}
