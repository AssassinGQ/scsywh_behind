package com.Entitys.Document.Dao;

import org.hibernate.Session;

import com.Common.Daos.SingTabBaseDaoImpl;
import com.Entitys.Document.Entity.ReadDurHistory;

public class ReadDurHistoryDaoImpl extends SingTabBaseDaoImpl<ReadDurHistory> implements ReadDurHistoryDao {

	public ReadDurHistoryDaoImpl(Session session) {
		super(session, ReadDurHistory.class);
		this.pretname = "readdurhistory_";
		this.tAliasname = "readdurhistory";
	}
}
