package com.RequestHandlers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.Common.Utils.NettyUtils;
import com.Entitys.ClientRequest;
import com.Entitys.User.Entity.LoginInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

public class RequestHistoryHandler {

	public static void saveRequest(Session session, ChannelHandlerContext ctx, HttpRequest request,
			String content, String response, LoginInfo logininfo) {
		String ip = NettyUtils.getRemoteip(request, ctx);
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String time = dateFormat.format(now);
		String path = NettyUtils.getUri(request);
		String head = request.toString();
		Transaction tx = session.beginTransaction();
		ClientRequest clientRequest = new ClientRequest();
		clientRequest.setClientip(ip);
		if(logininfo != null){
			clientRequest.setUsername(logininfo.getUsername());
			clientRequest.setCorporation(logininfo.getCorporationsid());
		}
		clientRequest.setTime(time);
		clientRequest.setPath(path);
		clientRequest.setHead(head);
		clientRequest.setContent(content);
		clientRequest.setResponse(response);
		session.save(clientRequest);
		tx.commit();
	}
}
