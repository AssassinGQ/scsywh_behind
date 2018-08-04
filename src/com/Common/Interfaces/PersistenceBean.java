package com.Common.Interfaces;

import org.json.JSONObject;

public interface PersistenceBean {
	public void updateFromData(String data, String lengthstr);
	//public boolean Validation();
	public int getKeyType(String key);
	public JSONObject getJsonObject();
	public void createtime(Long createid);
	public void updatetime(Long updateid);
}
