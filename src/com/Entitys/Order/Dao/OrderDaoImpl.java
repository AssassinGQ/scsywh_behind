package com.Entitys.Order.Dao;

import org.hibernate.Session;

import com.Common.Daos.MulTabBaseDaoImpl;
import com.Entitys.Order.Entity.Order;

public class OrderDaoImpl extends MulTabBaseDaoImpl<Order> implements OrderDao {
	public OrderDaoImpl(Session session) {
		super(session, Order.class);
		this.pretname = "orderp_";
		this.tAliasname = "orderp";
	}
}
