package com.Common.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Daos.QueryBean;
import com.Common.Exceptions.DaoException;

public class DaoUtils<T> {
	public static String Gene_Insert_Sql(Object object, List<Object> retList, String tablename) throws DaoException {
		if (retList == null)
			throw DaoException.DB_BUILDSQL_EXCEPTION.newInstance("接受结果的List为空");
		retList.clear();
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		sb1.append("INSERT INTO ").append(tablename).append(" (");
		sb2.append(" VALUES(");
		List<Field> fields = new ArrayList<Field>();
		Class<?> tempClass = object.getClass();
		while(tempClass != null){
			fields.addAll(Arrays.asList(tempClass.getDeclaredFields()));
			tempClass = tempClass.getSuperclass();
		}
		for (int index = 0, i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			field.setAccessible(true);
			if ((field.getModifiers()&(Modifier.PRIVATE|Modifier.PROTECTED)) == 0)
				continue;
			try {
				String fieldsname = field.getName();
				String[] tmp = fieldsname.split("_");
				if (tmp == null)
					continue;
				fieldsname = tmp[0];
				Object value = field.get(object);
				sb1.append(" " + tablename + ".`" + fieldsname + "`,");
				sb2.append(" ?,");
				retList.add(index++, value);
			} catch (Exception e) {
				throw DaoException.DB_BUILDSQL_EXCEPTION.newInstance("对象反射失败：%s", e.getMessage());
			}
		}
		sb1.deleteCharAt(sb1.length() - 1);
		sb2.deleteCharAt(sb2.length() - 1);
		sb1.append(" )");
		sb2.append(" )");
		return sb1.toString() + sb2.toString();
	}

	public static String Gene_Update_Sql(Object object, List<Object> retList, String tablename) throws DaoException {
		if (retList == null)
			throw DaoException.DB_BUILDSQL_EXCEPTION.newInstance("接受结果的List为空");
		retList.clear();
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ").append(tablename).append(" SET");
		List<Field> fields = new ArrayList<Field>();
		Class<?> tempClass = object.getClass();
		while(tempClass != null){
			fields.addAll(Arrays.asList(tempClass.getDeclaredFields()));
			tempClass = tempClass.getSuperclass();
		}
		for (int i = 0, index = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			field.setAccessible(true);
			if ((field.getModifiers()&(Modifier.PRIVATE|Modifier.PROTECTED)) == 0)
				continue;
			try {
				String fieldsname = field.getName();
				String[] tmp = fieldsname.split("_");
				if (tmp == null)
					continue;
				fieldsname = tmp[0];
				Object value = field.get(object);
				sb.append(" ").append(tablename).append(".`").append(fieldsname).append("`").append("=?,");
				retList.add(index++, value);
			} catch (Exception e) {
				throw DaoException.DB_BUILDSQL_EXCEPTION.newInstance("对象反射失败：%s", e.getMessage());
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(" WHERE sid = ?");
		return sb.toString();
	}

	public static String Gene_Query_Sql(List<QueryBean> paramList, List<Object> retList) throws DaoException {
		if (retList == null)
			throw DaoException.DB_BUILDSQL_EXCEPTION.newInstance("接受结果的List为空");
		retList.clear();
		if (paramList == null) {
			throw DaoException.DB_BUILDSQL_EXCEPTION.newInstance("条件paramList为空");
		} else {
			StringBuilder sb = new StringBuilder();
			boolean flag = false;
			for(int i = 0; i < paramList.size(); i++){
				QueryBean queryBean = paramList.get(i);
				String classname = queryBean.getClassname();
				String aliasname = queryBean.getAlias();
				String key = queryBean.getField();
				String value = queryBean.getValue();
				int valuetype = queryBean.getValuetype();
				int keytype = 4;
				try {
					keytype = Utils.getKeyType(key, classname);
				} catch (Exception e) {
					throw DaoException.DB_BUILDSQL_EXCEPTION.newInstance("getKeyType Error:"+e.getMessage());
				}
				if (keytype == 1) // 跳过的字段
					continue;
				if (value != null) {
					if (keytype == 2) { // 数字字段
						if(valuetype == QueryBean.TYPE_EQUAL){
							if (flag) {
								sb.append("and " + aliasname + ".`" + key + "` = ? ");
								retList.add(retList.size(), Double.parseDouble(value));
							} else {
								sb.append(" WHERE " + aliasname + ".`" + key + "` = ? ");
								retList.add(retList.size(), Double.parseDouble(value));
							}
							flag = true;
						}else if(valuetype == QueryBean.TYPE_JSONOBJECT){
							JSONObject jsonObject = null;
							try {
								jsonObject = new JSONObject(value);
							} catch (Exception e) {
								continue;
							}
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
							if (flag) {
								sb.append("and " + aliasname + ".`" + key + "` >= ? and " + aliasname + ".`" + key
										+ "` <= ? ");
								retList.add(retList.size(), Double.parseDouble(min));
								retList.add(retList.size(), Double.parseDouble(max));
							} else {
								sb.append(" WHERE " + aliasname + ".`" + key + "` >= ? and " + aliasname + ".`" + key
										+ "` <= ? ");
								retList.add(retList.size(), Double.parseDouble(min));
								retList.add(retList.size(), Double.parseDouble(max));
							}
							flag = true;
						}else if(valuetype == QueryBean.TYPE_JSONARRAY){
							JSONArray jsonArray = null;
							try {
								jsonArray = new JSONArray(value);
							} catch (Exception e) {
								continue;
							}
							try {
								if (flag) {
									sb.append("and ( ");
								} else {
									sb.append(" WHERE ( ");
								}
								for (int j = 0; j < jsonArray.length(); j++) {
									if (j != 0)
										sb.append("or ");
									sb.append(aliasname).append(".`").append(key).append("` = ? ");
									retList.add(retList.size(), jsonArray.getDouble(j));
								}
								sb.append(") ");
								flag = true;
							} catch (JSONException e) {
								throw DaoException.DB_BUILDSQL_EXCEPTION.newInstance(e.getMessage());
							}
						}
					} else if (keytype == 3) { // 字符串相等字段
						if (flag) {
							sb.append("and " + aliasname + ".`" + key + "` = ? ");
							retList.add(retList.size(), value);
						} else {
							sb.append(" WHERE " + aliasname + ".`" + key + "` = ? ");
							retList.add(retList.size(), value);
						}
						flag = true;
					}
				}
			}
			return sb.toString();
		}
	}
	
//	public static String Gene_Query_Sql(Map<String, String> paramMap, Class<?> clazz, List<Object> retList,
//			String tablename) throws DaoException {
//		if (retList == null)
//			throw DaoException.DB_BUILDSQL_EXCEPTION.newInstance("接受结果的List为空");
//		retList.clear();
//		if (paramMap == null) {
//			throw DaoException.DB_BUILDSQL_EXCEPTION.newInstance("条件paramMap为空");
//		} else {
//			StringBuilder sb = new StringBuilder();
//			boolean flag = false;
//			Iterator<Entry<String, String>> iterator = paramMap.entrySet().iterator();
//			while (iterator.hasNext()) {
//				Entry<String, String> entry = iterator.next();
//				String key = entry.getKey();
//				String value = entry.getValue();
//				int keytype = getKeyType(key, clazz);
//				if (keytype == 1) // 跳过的字段
//					continue;
//				if (value != null) {
//					if (keytype == 2) { // 数字字段
//						JSONObject jsonObject = null;
//						JSONArray jsonArray = null;
//						try {
//							jsonObject = new JSONObject(value);
//						} catch (Exception e) {
//							jsonObject = null;
//						}
//						try {
//							jsonArray = new JSONArray(value);
//						} catch (Exception e) {
//							jsonArray = null;
//						}
//						if (jsonObject != null) { // 数字范围
//							String min, max;
//							try {
//								min = jsonObject.getString("min");
//							} catch (Exception e) {
//								min = "0";
//							}
//							try {
//								max = jsonObject.getString("max");
//							} catch (Exception e) {
//								max = String.valueOf(Utils.getCurrenttimeMills() + 10000);
//							}
//							if (flag) {
//								sb.append("and " + tablename + ".`" + key + "` >= ? and " + tablename + ".`" + key
//										+ "` <= ? ");
//								retList.add(retList.size(), Double.parseDouble(min));
//								retList.add(retList.size(), Double.parseDouble(max));
//							} else {
//								sb.append(" WHERE " + tablename + ".`" + key + "` >= ? and " + tablename + ".`" + key
//										+ "` <= ? ");
//								retList.add(retList.size(), Double.parseDouble(min));
//								retList.add(retList.size(), Double.parseDouble(max));
//							}
//							flag = true;
//						} else if (jsonArray != null) { // 数字散列
//							try {
//								if (flag) {
//									sb.append("and ( ");
//								} else {
//									sb.append(" WHERE ( ");
//								}
//								for (int i = 0; i < jsonArray.length(); i++) {
//									if (i != 0)
//										sb.append("or ");
//									sb.append(tablename).append(".`").append(key).append("` = ? ");
//									retList.add(retList.size(), jsonArray.getDouble(i));
//								}
//								sb.append(") ");
//							} catch (JSONException e) {
//								throw DaoException.DB_BUILDSQL_EXCEPTION.newInstance(e.getMessage());
//							}
//						} else { // 数字相等
//							if (flag) {
//								sb.append("and " + tablename + ".`" + key + "` = ? ");
//								retList.add(retList.size(), Double.parseDouble(value));
//							} else {
//								sb.append(" WHERE " + tablename + ".`" + key + "` = ? ");
//								retList.add(retList.size(), Double.parseDouble(value));
//							}
//							flag = true;
//						}
//					} else if (keytype == 3) { // 字符串相等字段
//						if (flag) {
//							sb.append("and " + tablename + ".`" + key + "` = ? ");
//							retList.add(retList.size(), value);
//						} else {
//							sb.append(" WHERE " + tablename + ".`" + key + "` = ? ");
//							retList.add(retList.size(), value);
//						}
//						flag = true;
//					}
//				}
//			}
//			return sb.toString();
//		}
//	}

//	private static int getKeyType(String key, String clazzname) throws ClassNotFoundException {
//		if (key.equals("limit") || key.equals("page") || key.equals("token"))
//			return 1;// 跳过字段
//		List<Field> fields = new ArrayList<Field>();
//		Class<?> tempClass = Class.forName(clazzname);
//		while(tempClass != null){
//			fields.addAll(Arrays.asList(tempClass.getDeclaredFields()));
//			tempClass = tempClass.getSuperclass();
//		}
//		for (int i = 0; i < fields.size(); i++) {
//			Field field = fields.get(i);
//			field.setAccessible(true);
//			if ((field.getModifiers()&(Modifier.PRIVATE|Modifier.PROTECTED)) == 0)
//				continue;
//			String fieldsname = field.getName();
//			String[] tmp = fieldsname.split("_");
//			if (tmp == null)
//				continue;
//			String name = tmp[0];
//			if (key.equals(name)) {
//				String type = (field.getGenericType().getTypeName());
//				tmp = type.split("\\.");
//				type = tmp[tmp.length - 1];
//				if (type.equals("Integer") || type.equals("Float") || type.equals("Double") || type.equals("Long"))
//					return 2;// 数字字段
//				else
//					return 3;// 字符串字段
//			}
//		}
//		return 4;// 非属性字段
//	}
}
