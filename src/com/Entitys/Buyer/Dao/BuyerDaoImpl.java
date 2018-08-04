package com.Entitys.Buyer.Dao;

import org.hibernate.Session;

import com.Common.Daos.SingTabBaseDaoImpl;
import com.Entitys.Buyer.Entity.Buyer;

public class BuyerDaoImpl extends SingTabBaseDaoImpl<Buyer> implements BuyerDao {
	public BuyerDaoImpl(Session session) {
		super(session, Buyer.class);
		this.pretname = "buyer_";
		this.tAliasname = "buyer";
	}
}
