package com.Entitys.Warn.Dao;

import org.hibernate.Session;

import com.Common.Daos.MulTabBaseDaoImpl;
import com.Entitys.Warn.Entity.Warn;

public class WarnDaoImpl extends MulTabBaseDaoImpl<Warn> implements WarnDao {

	public WarnDaoImpl(Session session) {
		super(session, Warn.class);
		this.pretname = "warn_";
		this.tAliasname = "warn";
	}

}
