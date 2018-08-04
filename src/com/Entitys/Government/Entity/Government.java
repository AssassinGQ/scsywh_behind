package com.Entitys.Government.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.LoginableBasicInfo;

@Entity
@Table(name="government")
public class Government extends LoginableBasicInfo {
	public final static int DEPT_TRANSPORT = 0;//运输管理
	public final static int DEPT_TRAFFIC = 1;//交警
	public final static int DEPT_ENVIRONMENT = 2;//环保
	public final static int DEPT_FIRE = 3;//消防
	public final static int DEPT_SAFETY = 4;//安全监管
	@Column(name="dept", length=4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer dept;
	public Government() {
		super();
	}
	public Integer getDept() {
		return dept;
	}
	public void setDept(Integer dept) {
		if(dept < DEPT_TRANSPORT || dept > DEPT_SAFETY)
			dept = DEPT_SAFETY;
		this.dept = dept;
	}
//	@Override
//	public void UpdateLoginInfo(Map<String, String> params) {
//		// TODO Auto-generated method stub
//		
//	}
}
