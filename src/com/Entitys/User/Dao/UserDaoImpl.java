package com.Entitys.User.Dao;

import org.hibernate.Session;

import com.Common.Daos.SingTabBaseDaoImpl;
import com.Entitys.User.Entity.LoginInfo;

public class UserDaoImpl extends SingTabBaseDaoImpl<LoginInfo> implements UserDao {

	public UserDaoImpl(Session session) {
		super(session, LoginInfo.class);
		this.pretname = "logininfo_";
		this.tAliasname = "logininfo";
	}

}
