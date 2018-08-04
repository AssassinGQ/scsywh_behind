package com.Common.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.Common.Interfaces.HttpCallBack;

public class HttpUtils {

	private static final int CONNECT_TIMEOUT = 2*1000;  
	private static final int READ_TIMEOUT = 5*1000;
		
	private static String encoderUTF(String in)
	{
		try {
			return URLEncoder.encode(in, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static String Map2param(Map<String, String> params) throws UnsupportedEncodingException
	{
		StringBuilder param = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        while(iterator.hasNext())
        {
        	Entry<String, String> entry = iterator.next();
        	param.append(encoderUTF(entry.getKey())+"="+encoderUTF(entry.getValue())+"&");
        	//param.append(entry.getKey()+"="+entry.getValue()+"&");
        }
        if(param.toString().length() > 0)
        	param.deleteCharAt(param.length()-1);
        return param.toString();
	}
	
	public static String Get(String url, Map<String, String> params) throws UnsupportedEncodingException {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + Map2param(params);
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "No-Alive");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
//            connection.setRequestProperty("user-agent",
//                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            Map<String, List<String>> map = connection.getHeaderFields();
//            for (String key : map.keySet()) {
//                System.out.println(key + ": " + map.get(key));
//            }
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
                result += "\r\n";
            }
        } catch (Exception e) {
            System.out.println("Error in Get" + e);
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return URLDecoder.decode(result, "UTF-8");
    }
	
    public static String Post(String url, Map<String, String> params) throws UnsupportedEncodingException {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
//            System.out.println(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
//            conn.setRequestProperty("user-agent",
//                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(Map2param(params));
            out.flush();
            Map<String, List<String>> map = conn.getHeaderFields();
//            for (String key : map.keySet()) {
//                System.out.println(key + ": " + map.get(key));
//            }
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
                result += "\r\n";
            }
        } catch (Exception e) {
            System.out.println("Error in Post"+e);
            e.printStackTrace();
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return URLDecoder.decode(result, "UTF-8");
    }    
    
    public static void PostAsyc(String url, Map<String, String> params, HttpCallBack httpCallBack) {
    	new Thread(new Runnable() {
			@Override
			public void run() {
				PrintWriter out = null;
		        BufferedReader in = null;
		        String result = "";
		        try {
		            URL realUrl = new URL(url);
		            URLConnection conn = realUrl.openConnection();
		            conn.setRequestProperty("accept", "*/*");
		            conn.setRequestProperty("connection", "Keep-Alive");
		            conn.setConnectTimeout(CONNECT_TIMEOUT);
		            conn.setReadTimeout(READ_TIMEOUT);
		            //conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		            conn.setDoOutput(true);
		            conn.setDoInput(true);
		            out = new PrintWriter(conn.getOutputStream());
		            out.print(Map2param(params));
		            out.flush();
		            Map<String, List<String>> map = conn.getHeaderFields();
//		            for (String key : map.keySet()) {
//		                System.out.println(key + ": " + map.get(key));
//		            }
		            in = new BufferedReader(
		                    new InputStreamReader(conn.getInputStream()));
		            String line;
		            while ((line = in.readLine()) != null) {
		                result += line;
		                result += "\r\n";
		            }
		            try {
						result = URLDecoder.decode(result, "UTF-8");
					} catch (Exception e) {
						httpCallBack.OnError(e.getMessage());
					}
			        httpCallBack.OnComplete(result);
		        } catch (Exception e) {
		            httpCallBack.OnError(e.getMessage());
		        }
		        finally{
		            try{
		                if(out!=null){
		                    out.close();
		                }
		                if(in!=null){
		                    in.close();
		                }
		            }
		            catch(IOException ex){
		            	System.err.println(ex.getMessage());
		            }
		        }
			}
		}).start();
    }    
}
