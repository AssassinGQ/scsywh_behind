package com.Common.Daos;

public class QueryBean {
	public static int TYPE_EQUAL = 0;
	public static int TYPE_JSONOBJECT = 1;
	public static int TYPE_JSONARRAY = 2;
	public static int ACCRATE = 0;
	public static int UNACCRATE = 1;
	private String classname;
	private String alias;
	private String field;
	private String value;
	private int valuetype;
	private int isAccurate;
	public QueryBean() {
		super();
	}
	public QueryBean(String classname, String alias, String field, String value, int valuetype) {
		super();
		this.classname = classname;
		this.alias = alias;
		this.field = field;
		this.value = value;
		if(valuetype < 0 || valuetype > 2)
			valuetype = 0;
		this.valuetype = valuetype;
	}
	public QueryBean(String classname, String alias, String field, Object value, int valuetype) {
		super();
		this.classname = classname;
		this.alias = alias;
		this.field = field;
		this.value = String.valueOf(value);
		if(valuetype < 0 || valuetype > 2)
			this.valuetype = 0;
		this.valuetype = valuetype;
		this.isAccurate = ACCRATE;
	}
	public QueryBean(String classname, String alias, String field, Object value, int valuetype, int isaccrate) {
		super();
		this.classname = classname;
		this.alias = alias;
		this.field = field;
		this.value = String.valueOf(value);
		if(valuetype < 0 || valuetype > 2)
			this.valuetype = 0;
		this.valuetype = valuetype;
		if(isaccrate <0 || isaccrate > 1)
			this.isAccurate = ACCRATE;
		this.isAccurate = isaccrate;
	}
	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getValuetype() {
		return valuetype;
	}
	public void setValuetype(int valuetype) {
		this.valuetype = valuetype;
	}
	public int getIsAccurate() {
		return isAccurate;
	}
	public void setIsAccurate(int isAccurate) {
		this.isAccurate = isAccurate;
	}
	@Override
	public String toString() {
		return "QueryBean [alias=" + alias + ", field=" + field + ", value=" + value + ", valuetype=" + valuetype
				+ ", isAccurate=" + isAccurate + "]";
	}
}
