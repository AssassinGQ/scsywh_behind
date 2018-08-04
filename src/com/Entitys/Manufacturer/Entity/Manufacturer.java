package com.Entitys.Manufacturer.Entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Entitys.LoginableBasicInfo;

@Entity
@Table(name="manufacturer")
public class Manufacturer extends LoginableBasicInfo {

	public Manufacturer() {
		super();
	}
	// @Override
	// public void UpdateLoginInfo(Map<String, String> params) {
	// // TODO Auto-generated method stub
	//
	// }
}
