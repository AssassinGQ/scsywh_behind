package com.Entitys.Seller.Dao;

import org.hibernate.Session;

import com.Common.Daos.SingTabBaseDaoImpl;
import com.Entitys.Seller.Entity.Seller;

public class SellerDaoImpl extends SingTabBaseDaoImpl<Seller> implements SellerDao {
	public SellerDaoImpl(Session session) {
		super(session, Seller.class);
		this.pretname = "seller_";
		this.tAliasname = "seller";
	}
}
