package com.Common.Utils;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS;
import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;

public class NettyUtils {
	public static FullHttpResponse sendError(HttpResponseStatus status)
	{
		String responsestr = "Failure: "+status+"\r\n";
		//responsestr = Utils.encoderUTF(responsestr);
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer(responsestr, CharsetUtil.UTF_8));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		response.headers().set(ACCESS_CONTROL_ALLOW_METHODS, "POST,GET,OPTIONS");
		//ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		return response;
	}
	
	public static FullHttpResponse getTokenError(boolean keepalive)
	{
		try {
			JSONObject retObject = new JSONObject();
			retObject.put("status", 4);
			retObject.put("msg", "权限不足");
			retObject.put("content", "");
			String response = retObject.toString();
			//response = Utils.encoderUTF(response);
			FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
			resp.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
			resp.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			resp.headers().set(ACCESS_CONTROL_ALLOW_METHODS, "POST,GET,OPTIONS");
			if(keepalive)
			{
				resp.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			}
			resp.headers().set(CONTENT_LENGTH, resp.content().readableBytes());
			return resp;
		} catch (Exception e) {
			System.err.println("in getTokenError:"+e.getMessage());
			return sendError(HttpResponseStatus.BAD_REQUEST);
		}
	}
	
	public static FullHttpResponse getResponse(boolean keepalive, String response)
	{
		//response = Utils.encoderUTF(response);
		FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
		resp.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
		resp.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		resp.headers().set(ACCESS_CONTROL_ALLOW_METHODS, "POST,GET,OPTIONS");
		if(keepalive)
		{
			resp.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}
		resp.headers().set(CONTENT_LENGTH, resp.content().readableBytes());
		return resp;
	}
	
	public static String getRemoteip(HttpRequest request, ChannelHandlerContext ctx)
	{
		String ip = request.headers().get("X-Forwarded-For");
		if(ip == null)
		{
			InetSocketAddress inSocketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
			ip = inSocketAddress.getHostName();
		}
		return ip;
	}
	
	public static Map<String, String> getParams(HttpRequest request, HttpContent content) throws Exception
	{
		Map<String, String> params = new HashMap<String, String>();
		
		if(request.method().equals(HttpMethod.GET))
		{
			QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
			Map<String, List<String>> uriAttributes = decoder.parameters();
			for (Entry<String, List<String>> attr: uriAttributes.entrySet()) {
                for (String attrVal: attr.getValue()) {
                    params.put(attr.getKey(), attrVal);
                }
            }
		}
		else if(request.method().equals(HttpMethod.POST))
		{
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
			decoder.offer(content);
			List<InterfaceHttpData> paramList = decoder.getBodyHttpDatas();
			for(InterfaceHttpData param : paramList)
			{
				Attribute data = (Attribute)param;
				params.put(data.getName(), data.getValue());
			}
		}
		else {
			params = null;
		}
		if(params != null){
			List<String> keys = new ArrayList<String>();
			Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> entry = iterator.next();
				if(entry.getValue().equals(""))
					//params.remove(entry.getKey());
					keys.add(entry.getKey());
			}
			for(int i = 0; i < keys.size(); i++)
				params.remove(keys.get(i));
		}
		return params;
	}
	
	public static String getContent(HttpRequest request, HttpContent content) throws Exception
	{
		Map<String, String> params = getParams(request, content);
		if(params != null)
			if(!params.isEmpty())
			{
				StringBuilder sb = new StringBuilder();
				Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
				while(iterator.hasNext())
				{
					Entry<String, String> entry = iterator.next();
					sb.append(entry.getKey() + " = " + entry.getValue() + "\n");
				}
				return sb.toString();
			}
		return null;
	}
	
	public static String getContent(Map<String, String> params)
	{
		if(params != null){
			if(!params.isEmpty())
			{
				StringBuilder sb = new StringBuilder();
				Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
				while(iterator.hasNext())
				{
					Entry<String, String> entry = iterator.next();
					if(entry.getValue().length() > 50000){
						sb.append(entry.getKey() + " = value too long;");
					}else
						sb.append(entry.getKey() + " = " + entry.getValue() + ";");
				}
				return sb.toString();
			}
		}
		return null;
	}
	
	public static String getUri(HttpRequest request)
	{
		if(request.method().equals(HttpMethod.GET))
		{
			QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
			return decoder.path();
		}
		else if(request.method().equals(HttpMethod.POST))
		{
			return request.uri();
		}
		else {
			return "";
		}
	}
	
	public static String[] getUris(HttpRequest request)
	{
		String uri = getUri(request);
		uri = uri.substring(1, uri.length());
		return uri.split("/");
	}
	
	public static String[] getUris(String uri)
	{
		uri = uri.substring(1, uri.length());
		return uri.split("/");
	}
}
