package com.Entitys.Route.Dao;

import org.hibernate.Session;

import com.Common.Daos.SingTabBaseDaoImpl;
import com.Entitys.Route.Entity.Route;

public class RouteDaoImpl extends SingTabBaseDaoImpl<Route> implements RouteDao {
	public RouteDaoImpl(Session session) {
		super(session, Route.class);
		this.pretname = "route_";
		this.tAliasname = "route";
	}
}
