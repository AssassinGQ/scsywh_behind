package com.Common.Utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
	private static SessionFactory sessionFactory = null;
	
	private HibernateUtil(){}
	
	static
	{
		init();
	}
	
	private static void init(){
		try {
			sessionFactory = new Configuration().configure().buildSessionFactory();
		} catch (Throwable ex) {
			throw new ExceptionInInitializerError("Hibernate初始化失败:"+ex);
		}
	}
	
	
	public static SessionFactory getSessionFactory()
	{
		if(sessionFactory == null)
			init();
		return sessionFactory;
	}
	
	public static Session openSession(){
		return getSessionFactory().openSession();
	}
	
	public static void shutdown()
	{
		sessionFactory.close();
	}
}
