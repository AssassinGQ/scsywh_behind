package com.Entitys.Fareform.Dao;

import org.hibernate.Session;

import com.Common.Daos.MulTabBaseDaoImpl;
import com.Entitys.Fareform.Entity.FareForm;

public class FareformDaoImpl extends MulTabBaseDaoImpl<FareForm> implements FareformDao {

	public FareformDaoImpl(Session session) {
		super(session, FareForm.class);
		this.pretname = "fareform_";
		this.tAliasname = "fareform";
	}
}
