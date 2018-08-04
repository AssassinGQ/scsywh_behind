package com.Entitys.Lock.Dao;

import org.hibernate.Session;

import com.Common.Daos.MulTabBaseDaoImpl;
import com.Common.Exceptions.DaoException;
import com.Entitys.Lock.Entity.Lock;

public class LockinfoDaoImpl extends MulTabBaseDaoImpl<Lock> implements LockinfoDao {
	public LockinfoDaoImpl(Session session) {
		super(session, Lock.class);
		this.pretname = "lockinfo_";
		this.tAliasname = "lockinfo";
	}
	
//	@Override
//	public int update(Lock lock, String oldobject, Long operatersid) throws Exception {
//		throw DaoException.DB_UPDATE_EXCEPTION.newInstance("In %s.update.{%s}", Daoname, "锁请求信息不能修改");
//	}
	
	@Override
	public int delete(Lock lock, Long operatersid) throws Exception {
		throw DaoException.DB_UPDATE_EXCEPTION.newInstance("In %s.delete.{%s}", Daoname, "锁请求信息不能删除");
	}
}
