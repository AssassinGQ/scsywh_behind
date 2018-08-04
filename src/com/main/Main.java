package com.main;

import org.hibernate.Session;

import com.Common.Utils.HibernateUtil;
import com.Common.Utils.JDBCUtil;
import com.Netty.NettyHttpServer;

public class Main {
	
	public static void main(String[] args) {
//		SnowFlake snowFlake = new SnowFlake(0, SnowFlake_DataID.getEnum("Order").getDataId());
//		long sid = snowFlake.nextId();
//		System.err.println(sid);
//		System.err.println(String.valueOf(sid).length());
		System.out.println("Version 1.1808011928");
		int port = 85;
		if(args.length>0)
		{
			try {
				port = Integer.parseInt(args[0]);				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		try {
			JDBCUtil.initDB();			
		} catch (Exception e) {
			System.out.println("创建数据库数据表出错");
			return;
		}
		try {
			Session session = HibernateUtil.openSession();
			session.close();
		} catch (Exception e) {
			System.out.println("初始化hibernate出错："+e.getMessage());
			return;
		}
		try {
			new NettyHttpServer().setup(port);
		} catch (Exception e) {
			System.out.println("Start HttpServer error: " + e.getMessage());
		}
	}
}
