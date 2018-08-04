package com.Entitys.Product.Dao;

import org.hibernate.Session;

import com.Common.Daos.SingTabBaseDaoImpl;
import com.Entitys.Product.Entity.Product;

public class ProductDaoImpl extends SingTabBaseDaoImpl<Product> implements ProductDao {
	public ProductDaoImpl(Session session) {
		super(session, Product.class);
		this.pretname = "product_";
		this.tAliasname = "product";
	}
}
