package com.Common.Utils;

import java.util.HashMap;
import java.util.Map;

import com.Entitys.WebSocketInfo;

public class WebSocketUtils {
	public static Map<String, WebSocketInfo> ctxs = null;
	
	public static String GetCtxId(WebSocketInfo wsInfo){
		String str = wsInfo.getCtx().toString();
		String[] tmp = new String[4];
		tmp = str.split(":");
		str = tmp[1];
		tmp = str.split(",");
		str = tmp[0].trim();
		return str;
	}
	
	public static void AddCtx(String name, WebSocketInfo wsInfo){
		System.out.println("Addctx:name="+name);
		if(ctxs == null)
			ctxs = new HashMap<String, WebSocketInfo>();
		ctxs.put(name, wsInfo);
	}
	
	public static void AddCtx(WebSocketInfo wsInfo){
		AddCtx(GetCtxId(wsInfo), wsInfo);
	}
	
	public static void RemoveCtx(String name){
		if(ctxs == null)
			return;
		ctxs.remove(name);
	}
}
