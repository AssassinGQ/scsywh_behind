package com.Entitys.Document.Dao;

import org.hibernate.Session;

import com.Common.Daos.SingTabBaseDaoImpl;
import com.Entitys.Document.Entity.TotalDur;

public class TotalDurDaoImpl extends SingTabBaseDaoImpl<TotalDur> implements TotalDurDao {

	public TotalDurDaoImpl(Session session) {
		super(session, TotalDur.class);
		this.pretname = "totaldur_";
		this.tAliasname = "totaldur";
	}

}
