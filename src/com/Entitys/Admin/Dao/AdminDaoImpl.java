package com.Entitys.Admin.Dao;

import org.hibernate.Session;

import com.Common.Daos.SingTabBaseDaoImpl;
import com.Entitys.Admin.Entity.Admin;

public class AdminDaoImpl extends SingTabBaseDaoImpl<Admin> implements AdminDao {

	public AdminDaoImpl(Session session) {
		super(session, Admin.class);
		this.pretname = "admin_";
		this.tAliasname = "admin";
	}

}
