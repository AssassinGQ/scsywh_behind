package com.Entitys.User.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;

@Entity
@Table(name = "logininfo")
public class LoginInfo extends Bean {
	public final static int TYPE_DRIVER = 0;
	public final static int TYPE_ESCORT = 7;
	public final static int TYPE_SELLER = 1;
	public final static int TYPE_BUYER = 2;
	public final static int TYPE_ADMIN = 3;
	public final static int TYPE_CORPORATION = 4;
	public final static int TYPE_MANUFACTURER = 5;
	public final static int TYPE_GOVERNMENT = 6;
	public final static int TYPE_SUPERADMIN = 233;
    public final static int AUTHD_TRUE = 0;
    public final static int AUTHD_FALSE = 1;
	@Column(name = "usersid", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long usersid; // 关联用户信息，保留
	@Column(name = "dept", length = 4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer dept;
	@Column(name = "vcode", length = 6)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 6)
	private String vcode; // 最后一次发送的验证码
	@Column(name = "status", length = 4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer status; // 0未注册1已注册
	@Column(name = "username", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String username; // 用户名，用于登录，不填时系统自动生成，承运方5位sid，其他所属承运方5位sid+type1位+sid5位，11位
	@Column(name = "salt", length = 10)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 10)
	private String salt;
	@Column(name = "password", length = 100)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 100)
	private String password; // 登录用密码
	@Column(name = "phone", length = 11)
	@Valid(nullAble = true, varType = VarType.String, minLength = 11, maxLength = 11)
	private String phone;
	@Column(name = "type", length = 4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer type; // 用户类型，0管理员1司机2托运方3收货方4承运方5政府
	@Column(name = "tokentime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long tokentime;
	@Column(name = "token", length = 32)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 32)
	private String token;
	public final static int ONLINETYPE_APP = 0;
	public final static int ONLINETYPE_WEB = 1;
	@Column(name = "onlinetype", length = 4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer onlinetype; // 在线状态，0：app在线，1网页在线
	@Column(name = "cid", length = 32)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 32)
	private String cid; // 用于getui推送
	@Column(name = "wsid", length = 32)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 32)
	private String wsid; // 用token表示ctxs的name

	@Column(name = "lastlogintime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long lastlogintime; // 最后登录时间，系统自动维护

	@Column(name = "isdocauthd", length = 4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 1)
	private Integer isDocAuthd;//0有权限，1没权限
	@Column(name = "isexmauthd", length = 4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 1)
	private Integer isExmAuthd;//0有权限，1没权限

	public LoginInfo() {
		super();
		isDocAuthd = AUTHD_FALSE;
		isExmAuthd = AUTHD_FALSE;
	}
	public Integer getOnlinetype() {
		return onlinetype;
	}

	public void setOnlinetype(Integer onlinetype) {
		this.onlinetype = onlinetype;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getWsid() {
		return wsid;
	}

	public void setWsid(String wsid) {
		this.wsid = wsid;
	}
	public Integer getDept() {
		return dept;
	}

	public void setDept(Integer dept) {
		this.dept = dept;
	}

	public Long getUsersid() {
		return usersid;
	}

	public void setUsersid(Long usersid) {
		this.usersid = usersid;
	}

	public String getVcode() {
		return vcode;
	}

	public void setVcode(String vcode) {
		this.vcode = vcode;
	}

	public Long getTokentime() {
		return tokentime;
	}

	public void setTokentime(Long tokentime) {
		this.tokentime = tokentime;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getLastlogintime() {
		return lastlogintime;
	}

	public void setLastlogintime(Long lastlogintime) {
		this.lastlogintime = lastlogintime;
	}

	public Integer getIsDocAuthd() {
		return isDocAuthd;
	}

	public void setIsDocAuthd(Integer isDocAuthd) {
		this.isDocAuthd = isDocAuthd;
	}

	public Integer getIsExmAuthd() {
		return isExmAuthd;
	}

	public void setIsExmAuthd(Integer isExmAuthd) {
		this.isExmAuthd = isExmAuthd;
	}

	// public void UpdateFromMap(Map<String, String> params, Session session)
	// throws Exception{
	// String username = params.get("username");
	// if(username != null){//全部role都可以修改
	// LoginInfo logininfo_checkusername = session.createQuery(
	// "from LoginInfo where username = '" + username + "' and sid <> " +
	// this.sid,
	// LoginInfo.class).uniqueResult();
	// System.err.println("from LoginInfo where username = " + username);
	// if(logininfo_checkusername != null){
	// System.err.println(logininfo_checkusername);
	// throw new Exception("username already used");
	// }else{
	// this.username = username;
	// }
	// }
	// String password = params.get("password");
	// if(password != null){
	// if(Utils.getLengthOfObject(password) != 6){
	// throw new Exception("password length should be 6");
	// }else {
	// this.password_te_6 = password;
	// }
	// }
	// String phone = params.get("phone");
	// if(phone != null){
	// if(Utils.isMobileNum(phone)){
	// this.phone_te_11 = phone;
	// }else
	// throw new Exception("phone format error");
	// }
	// }
}
