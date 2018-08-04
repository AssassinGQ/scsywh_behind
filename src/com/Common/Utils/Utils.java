package com.Common.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.Common.Entitys.BasicInfo;
import com.Common.Entitys.Bean;
import com.Common.Interfaces.GetKeyTypeCallback;
import com.Entitys.Order.Entity.Order;

public class Utils {
	
	public static String getErrorStr(Exception e){
		String error;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			e.printStackTrace(new PrintStream(baos));
			error = baos.toString();
		} catch (Exception e2) {
			error = "异常数据保存失败:" + e.getMessage();
		} finally {
			try {
				baos.close();
			} catch (IOException e1) {
				System.err.println("baos关闭失败" + e1.getMessage());
			}
		}
		if (error.length() > 60000)
			error = error.substring(0, 60000);
		return error;
	}
	
	public static String getYear(){
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		return String.valueOf(year);
	}
	
	public static String getMonth(){
		Calendar now = Calendar.getInstance();
		int month = now.get(Calendar.MONTH)+1;
		return String.valueOf(month);
	}
	
	public static String getMonth(Long stamp){
		Calendar time = Calendar.getInstance();
		time.setTime(new Date(stamp));
		int month = time.get(Calendar.MONTH)+1;
		return String.valueOf(month);
	}
	
	public static String[] getYearMonth(){
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		String[] ret = new String[2];
		ret[0] = String.valueOf(year);
		ret[1] = String.valueOf(month);
		return ret;
	}
	
	public static Map<String, String> paramswritefilter(Map<String, String> params, Bean basicInfo, int role, Long corporationsid){
		Map<String, String> ret = new HashMap<String, String>();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.equals("datastatus"))
				continue;
			if(basicInfo.WritePremission(key, role))
				ret.put(key, value);
		}
		ret.put("corporationsid", String.valueOf(corporationsid));
		return ret;
	}
	
	public static Map<String, String> paramsloginqueryfilter(Map<String, String> params, int role, Long corporationsid, Long operatesid){
		Map<String, String> ret = paramsqueryfilter(params, role, corporationsid, operatesid);
		if(role <= BasicInfo.ROLE_USER && role != BasicInfo.ROLE_GOVERNMENT)
			ret.put("sid", String.valueOf(operatesid));
		return ret;
	}
	
	public static Map<String, String> paramsqueryfilter(Map<String, String> params, int role, Long corporationsid, Long operatesid){
		Map<String, String> ret = new HashMap<String, String>();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			ret.put(entry.getKey(), entry.getValue());
		}
		if(role == BasicInfo.ROLE_CORPORATION || role == BasicInfo.ROLE_ADMIN || 
				role == BasicInfo.ROLE_USER)
			ret.put("corporationsid", String.valueOf(corporationsid));
		return ret;
	}
	
	public static JSONObject getJsonObjectWithPremission(Bean clazz, int role) {
		JSONObject jsonObject = new JSONObject();
		List<Field> fields = new ArrayList<Field>();
		Class<?> tempClass = clazz.getClass();
		while(tempClass != null){
			fields.addAll(Arrays.asList(tempClass.getDeclaredFields()));
			tempClass = tempClass.getSuperclass();
		}
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			field.setAccessible(true);
			if ((field.getModifiers()&(Modifier.PRIVATE|Modifier.PROTECTED)) == 0)
				continue;
			try {
				String fieldsname = field.getName();
				String[] tmp = fieldsname.split("_");
				if(tmp == null)
					continue;
				fieldsname = tmp[0];
				if(clazz.ReadPremission(fieldsname, role)){
					Object object = field.get(clazz);
					if(fieldsname.equals("sid")){
						Long sid = (Long) object;
						jsonObject.put("sid", String.valueOf(sid));
					} else if(fieldsname.equals("ordersid")){
						Long ordersid = (Long) object;
						jsonObject.put("ordersid", String.valueOf(ordersid));
					} else if(fieldsname.equals("fareformsid")){
						Long fareformsid = (Long) object;
						jsonObject.put("fareformsid", String.valueOf(fareformsid));
					} else{
						jsonObject.put(fieldsname, object);
					}
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		return jsonObject;
	}
	
	public static JSONObject getOrderJsonObjectWithPremission(Order order, int role) {
		JSONObject jsonObject = new JSONObject();
		List<Field> fields = new ArrayList<Field>();
		Class<?> tempClass = Order.class;
		while(tempClass != null){
			fields.addAll(Arrays.asList(tempClass.getDeclaredFields()));
			tempClass = tempClass.getSuperclass();
		}
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			field.setAccessible(true);
			if ((field.getModifiers()&(Modifier.PRIVATE|Modifier.PROTECTED)) == 0)
				continue;
			try {
				String fieldsname = field.getName();
				String[] tmp = fieldsname.split("_");
				if(tmp == null)
					continue;
				fieldsname = tmp[0];
				
				if(order.ReadPremission(fieldsname, role)){
					Object object = field.get(order);
					if(fieldsname.equals("sid")){
						Long sid = (Long) object;
						jsonObject.put("sid", String.valueOf(sid));
					} else if(fieldsname.equals("ordersid")){
						Long ordersid = (Long) object;
						jsonObject.put("ordersid", String.valueOf(ordersid));
					} else if(fieldsname.equals("fareformsid")){
						Long fareformsid = (Long) object;
						jsonObject.put("fareformsid", String.valueOf(fareformsid));
					} else
						jsonObject.put(fieldsname, object);
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		return jsonObject;
	}
	
	public static JSONObject getJsonObject(Object clazz) {
		JSONObject jsonObject = new JSONObject();
		if(clazz == null)
			return jsonObject;
		List<Field> fields = new ArrayList<Field>();
		Class<?> tempClass = clazz.getClass();
		while(tempClass != null){
			fields.addAll(Arrays.asList(tempClass.getDeclaredFields()));
			tempClass = tempClass.getSuperclass();
		}
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			field.setAccessible(true);
			if ((field.getModifiers()&(Modifier.PRIVATE|Modifier.PROTECTED)) == 0)
				continue;
			try {
				String fieldsname = field.getName();
				String[] tmp = fieldsname.split("_");
				if(tmp == null)
					continue;
				fieldsname = tmp[0];
				Object object = field.get(clazz);
				if(fieldsname.equals("sid")){
					Long sid = (Long) object;
					jsonObject.put("sid", String.valueOf(sid));
				} else if(fieldsname.equals("ordersid")){
					Long ordersid = (Long) object;
					jsonObject.put("ordersid", String.valueOf(ordersid));
				} else if(fieldsname.equals("fareformsid")){
					Long fareformsid = (Long) object;
					jsonObject.put("fareformsid", String.valueOf(fareformsid));
				} else
					jsonObject.put(fieldsname, object);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		return jsonObject;
	}
	
	public static boolean isMobileNum(String number){
		Pattern p = Pattern.compile("^((13[0-9])|(14[57])|(15[^4,\\D])|(17[5678])|(18[0-9]))\\d{8}$");
		Matcher m = p.matcher(number);
		return m.matches();
	}
	
	public static boolean isPassword(String psw){
//		if(psw.length() < 8)
//			return false;
//		boolean flag[] = {false, false, false};
//		for(int i = 0; i < psw.length(); i++){
//			char c = psw.charAt(i);
//			if(Character.isDigit(c))
//				flag[0] = true;
//			else if(Character.isLetter(c)){
//				if(Character.isLowerCase(c))
//					flag[1] = true;
//				else
//					flag[2] = true;
//			}
//		}
//		if(flag[0] && flag[1] && flag[2])
//			return true;
//		return false;
		return true;
	}
	
	public static int getKeyType(String key, String clazzname) throws ClassNotFoundException {
		if (key.equals("limit") || key.equals("page") || key.equals("token"))
			return 1;// 跳过字段
		List<Field> fields = new ArrayList<Field>();
		Class<?> tempClass = Class.forName(clazzname);
		while(tempClass != null){
			fields.addAll(Arrays.asList(tempClass.getDeclaredFields()));
			tempClass = tempClass.getSuperclass();
		}
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			field.setAccessible(true);
			if ((field.getModifiers()&(Modifier.PRIVATE|Modifier.PROTECTED)) == 0)
				continue;
			String fieldsname = field.getName();
			if (key.equals(fieldsname)) {
				String type = (field.getGenericType().getTypeName());
				String[] tmp = type.split("\\.");
				type = tmp[tmp.length - 1];
				if (type.equals("Integer") || type.equals("Float") || type.equals("Double") || type.equals("Long"))
					return 2;// 数字字段
				else
					return 3;// 字符串字段
			}
		}
		return 4;// 非属性字段
	}
	
	public static void UpdateFromMap(Map<String, String> params, Object clazz) {
		List<Field> fields = new ArrayList<Field>();
		Class<?> tempClass = clazz.getClass();
		while(tempClass != null){
			fields.addAll(Arrays.asList(tempClass.getDeclaredFields()));
			tempClass = tempClass.getSuperclass();
		}
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			field.setAccessible(true);
			if ((field.getModifiers()&(Modifier.PRIVATE|Modifier.PROTECTED)) == 0)
				continue;
			String fieldsname = field.getName();
			String[] tmp = fieldsname.split("_");
			if(tmp == null)
				continue;
			if(tmp[0].equals("sid") || tmp[0].equals("createdat") || tmp[0].equals("createdid")
					|| tmp[0].equals("updatedat") || tmp[0].equals("updatedid"))
				continue;
			String instr = params.get(tmp[0]);
			try {
				if (instr != null) {
					Object obj = Utils.Getparse(instr, field.getGenericType().getTypeName());
					field.set(clazz, obj);
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}

	public static Object Getparse(String in, String classname) {
		if (classname.equals("java.lang.Long")) {
			try {
				Long result = Long.parseLong(in);
				return result;
			} catch (Exception e) {
				return null;
			}
		} else if (classname.equals("java.lang.Integer")) {
			try {
				Integer result = Integer.parseInt(in);
				return result;
			} catch (Exception e) {
				return null;
			}
		} else if (classname.equals("java.lang.Float")) {
			try {
				Float result = Float.parseFloat(in);
				return result;
			} catch (Exception e) {
				return null;
			}
		} else if (classname.equals("java.lang.Double")) {
			try {
				Double result = Double.parseDouble(in);
				return result;
			} catch (Exception e) {
				return null;
			}
		} else if (classname.equals("java.lang.String")) {
			return in;
		} else {
			return null;
		}
	}

	public static Object Getparse(String in, Object tmp) {
		if (tmp instanceof Long) {
			try {
				Long result = Long.parseLong(in);
				return result;
			} catch (Exception e) {
				return null;
			}
		} else if (tmp instanceof Integer) {
			try {
				Integer result = Integer.parseInt(in);
				return result;
			} catch (Exception e) {
				return null;
			}
		} else if (tmp instanceof Float) {
			try {
				Float result = Float.parseFloat(in);
				return result;
			} catch (Exception e) {
				return null;
			}
		} else if (tmp instanceof Double) {
			try {
				Double result = Double.parseDouble(in);
				return result;
			} catch (Exception e) {
				return null;
			}
		} else if (tmp instanceof String) {
			return in;
		} else {
			return null;
		}
	}

	public static int getLengthOfObject(Object in) {
		String str = String.valueOf(in);
		if (str == null)
			return 0;
		else
			return str.length();
	}

	public static String getRandomString(int length) {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}
	
	//获得0-max-1之间的随机整数
	private static int getRandomInt(int max){
		return (int)(Math.random()*max%max);
	}
	
	public static String getPassword(int length) {
		boolean flag[] = {false, false, false};
		int len = 0;
		StringBuilder sb = new StringBuilder();
		while(!flag[0] || !flag[1] || !flag[2] || len < length){
			int type = getRandomInt(3);
			if(type == 0){
				sb.append((char)('0'+getRandomInt(10)));
				flag[0] = true;
			}else if(type == 1){
				sb.append((char)('a'+getRandomInt(26)));
				flag[1] = true;
			}else if(type == 2){
				sb.append((char)('A'+getRandomInt(26)));
				flag[2] = true;
			}
			len++;
			if(len > 15){
				len = 0;
				flag[0] = false;
				flag[2] = false;
				flag[3] = false;
				sb = new StringBuilder();
			}
		}
		return sb.toString();
	}
	
	public static String getPassword() {
		return getPassword(8);
	}

	public static String getToken() {
		UUID uuid = UUID.randomUUID();
		String token = uuid.toString();
		token = token.toUpperCase();
		token = token.replace("-", "");
		return token;
	}

	public static String encoderUTF(String in) {
		try {
			return URLEncoder.encode(in, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String getCurrenttime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date());
	}

	public static Long getCurrenttimeMills() {
		return System.currentTimeMillis();
	}

	public static String map2String(Map<String, String> params) {
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		StringBuilder sb = new StringBuilder();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			sb.append(entry.getKey() + " = " + entry.getValue() + "\n");
		}
		return sb.toString();
	}

	public static String getConditionStr(Map<String, String> map, GetKeyTypeCallback getKeyTypeCallback) {
		if (map.isEmpty())
			return null;
		else {
			StringBuilder sb = new StringBuilder();
			boolean flag = false;
			Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> entry = iterator.next();
				String key = entry.getKey();
				String value = entry.getValue();
				int keytype = getKeyTypeCallback.getKeyType(key);
				if (keytype == 1)				//跳过的字段
					continue;
				if (value != null) {
					if (keytype == 2) {			//数字字段
						JSONObject jsonObject = null;
						try {
							jsonObject = new JSONObject(value);
						} catch (Exception e) {
							jsonObject = null;
						}
						if(jsonObject == null){			//数字相等
							if (flag)
								sb.append("and " + key + " = " + value + " ");
							else
								sb.append(" " + key + " = " + value + " ");
							flag = true;
						}else {							//数字范围
							String min, max;
							try {
								min = jsonObject.getString("min");
							} catch (Exception e) {
								min = "0";
							}
							try {
								max = jsonObject.getString("max");
							} catch (Exception e) {
								max = String.valueOf(Utils.getCurrenttimeMills() + 10000);
							}
							if (flag)
								sb.append("and " + key + " >= " + min + " and " + key + " <= " + max + " ");
							else
								sb.append(" " + key + " >= " + min + " and " + key + " <= " + max + " ");
							flag = true;
						}
					} else if (keytype == 3) {		//字符串相等字段
						if (flag)
							sb.append("and " + key + " = '" + value + "' ");
						else
							sb.append(" " + key + " = '" + value + "' ");
						flag = true;
					}
				}
			}
			if (!flag)
				return null;
			else
				return sb.toString();
		}
	}
}
