package com.Entitys.Driver.Dao;

import org.hibernate.Session;

import com.Common.Daos.SingTabBaseDaoImpl;
import com.Entitys.Driver.Entity.Driver;

public class DriverDaoImpl extends SingTabBaseDaoImpl<Driver> implements DriverDao {
	public DriverDaoImpl(Session session) {
		super(session, Driver.class);
		this.pretname = "driver_";
		this.tAliasname = "driver";
	}
}
