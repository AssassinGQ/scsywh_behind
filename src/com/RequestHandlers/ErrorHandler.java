package com.RequestHandlers;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONObject;

import com.Common.Interfaces.HttpCallBack;
import com.Common.Utils.HttpUtils;
import com.Common.Utils.Utils;
import com.Entitys.ErrorMsgs;
import com.Entitys.User.Entity.LoginInfo;

public class ErrorHandler {
	public static void saveError(Session session, String ip, LoginInfo loginInfo, String path, String content, String errormsg) {
		Transaction tx_check = session.getTransaction();
		if(tx_check.isActive())
			tx_check.rollback();
		Map<String, String> params = new HashMap<String, String>();
		params.put("ip", ip);
		HttpUtils.PostAsyc("http://ip.taobao.com/service/getIpInfo.php", params, new HttpCallBack() {
			@Override
			public void OnError(String errmsg) {
				System.err.println(errmsg);
				Transaction tx = session.beginTransaction();
				ErrorMsgs errorMsgs = new ErrorMsgs();
				errorMsgs.setTime(Utils.getCurrenttime());
				errorMsgs.setIp(ip);
				errorMsgs.setAddr("");
				if(loginInfo != null){
					errorMsgs.setLogininfosid(loginInfo.getSid());
					errorMsgs.setUsername(loginInfo.getUsername());
					errorMsgs.setCorporation(loginInfo.getCorporationsid());
				}
				errorMsgs.setPath(path);
				errorMsgs.setParams(content);
				errorMsgs.setErrormsg(errormsg);
				session.save(errorMsgs);
				tx.commit();
			}
			@Override
			public void OnComplete(String result) {
				try {
					JSONObject jsonObject = new JSONObject(result);
					String addr = "";
					if(jsonObject.getInt("code") == 0){
						JSONObject dataObject = jsonObject.getJSONObject("data");
						StringBuilder sb = new StringBuilder();
						sb.append(dataObject.getString("country"));
						sb.append(dataObject.getString("region"));
						sb.append(dataObject.getString("city"));
						sb.append(dataObject.getString("county"));
						addr = sb.toString();
					}
					Transaction tx = session.beginTransaction();
					ErrorMsgs errorMsgs = new ErrorMsgs();
					errorMsgs.setTime(Utils.getCurrenttime());
					errorMsgs.setIp(ip);
					errorMsgs.setAddr(addr);
					if(loginInfo != null){
						errorMsgs.setLogininfosid(loginInfo.getSid());
						errorMsgs.setUsername(loginInfo.getUsername());
						errorMsgs.setCorporation(loginInfo.getCorporationsid());
					}
					errorMsgs.setPath(path);
					errorMsgs.setParams(content);
					errorMsgs.setErrormsg(errormsg);
					session.save(errorMsgs);
					tx.commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
}
