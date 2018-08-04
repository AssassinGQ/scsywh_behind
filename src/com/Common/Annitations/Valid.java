package com.Common.Annitations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Valid {
	public enum VarType{Number, String};
	public enum ExtraType{Phone, Email};
	
	boolean needValid() default true;
	boolean nullAble() default false;
	VarType varType() default VarType.String;
	//String
	int minLength() default 0;
	int maxLength() default Integer.MAX_VALUE;
	String regex() default "";
	//Number
	double minValue() default Double.MIN_VALUE;
	double maxValue() default Double.MAX_VALUE;
}
