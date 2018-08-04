package com.Common.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Exceptions.DaoException;

public class ValidUtils {
	public static boolean ValidationWithExp(Object clazz) throws DaoException {
		try {
			ValidationImpl(clazz);
			return true;
		} catch (DaoException e) {
			System.err.println(e.getMessage());
			throw new DaoException(e.getMessage());
		}
	}
	
	public static boolean Validation(Object clazz) {
		try {
			ValidationImpl(clazz);
			return true;
		} catch (DaoException e) {
			System.err.println(e.getMessage());
			return false;
		}
	}

	public static void ValidationImpl(Object clazz) throws DaoException {
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
			String fieldname = field.getName();
			Object object = null;
			 try {
				object = field.get(clazz);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new DaoException("反射异常: " +e.getMessage());
			}
			Valid valid = field.getAnnotation(Valid.class);
			if(valid.needValid()){
				if(valid.nullAble() && object == null){
					continue;
				}else if(!valid.nullAble() && object == null){
					throw new DaoException(fieldname + "不能为空");
				}else {
					if(valid.varType() == VarType.Number){
						double maxvalue = valid.maxValue();
						double minvalue = valid.minValue();
						double value = Double.parseDouble(String.valueOf(object));
						if(!(value >= minvalue && value <= maxvalue))
							throw new DaoException(fieldname + "的取值必须是[" + minvalue + " ," + maxvalue + "]");
					}else if(valid.varType() == VarType.String){
						int minlength = valid.minLength();
						int maxlength = valid.maxLength();
						String regex = valid.regex();
						int length = Utils.getLengthOfObject(object);
						if(!(length >= minlength && length <= maxlength))
							throw new DaoException(fieldname + "的长度必须是[" + minlength + " ," + maxlength + "]");
						if(!regex.equals("")){
							String objectstr = String.valueOf(object);
							Pattern pattern = Pattern.compile(regex);
							Matcher matcher = pattern.matcher(objectstr);
							if(!matcher.matches()){
								throw new DaoException(fieldname + "的格式不正确");
							}
						}
					}else {
						throw new DaoException(fieldname + "的注解存在问题");
					}
				}
			}else {
				continue;
			}
		}
	}
}
