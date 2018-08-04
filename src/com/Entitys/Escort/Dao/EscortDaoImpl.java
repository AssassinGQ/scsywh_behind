package com.Entitys.Escort.Dao;

import org.hibernate.Session;

import com.Common.Daos.SingTabBaseDaoImpl;
import com.Entitys.Escort.Entity.Escort;

public class EscortDaoImpl extends SingTabBaseDaoImpl<Escort> implements EscortDao {
	public EscortDaoImpl(Session session) {
		super(session, Escort.class);
		this.pretname = "escort_";
		this.tAliasname = "escort";
	}
}
