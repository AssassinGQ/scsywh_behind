package com.Entitys.User.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sid", length = 10)
	private Long sid_nm_10; // 系统分配
	@Column(name = "usersid", length = 10)
	private Long usersid_nm_10; // 关联用户信息，保留
	@Column(name = "corporationsid", length = 10)
	private Long corporationsid_nm_10; // 用户所属公司sid
	@Column(name = "dept", length = 4)
	private Integer dept_fm_4;
	@Column(name = "vcode", length = 6)
	private String vcode_ne_6; // 最后一次发送的验证码
	@Column(name = "status", length = 4)
	private Integer status_nm_4; // 0未注册1已注册
	@Column(name = "username", length = 30)
	private String username_tm_30; // 用户名，用于登录，不填时系统自动生成，承运方5位sid，其他所属承运方5位sid+type1位+sid5位，11位
	@Column(name = "salt", length = 10)
	private String salt_te_10;
	@Column(name = "password", length = 100)
	private String password_tm_100; // 登录用密码，必须,6位
	@Column(name = "phone", length = 11)
	private String phone_fe_11;
	@Column(name = "type", length = 4)
	private Integer type_fm_4; // 用户类型，0管理员1司机2托运方3收货方4承运方5政府
	@Column(name = "tokentime", length = 13)
	private Long tokentime_nm_13;
	@Column(name = "token", length = 32)
	private String token_nm_32;
	@Column(name = "datastatus", length = 4)
	private Integer datastatus_fm_4;
	public final static int ONLINETYPE_APP = 0;
	public final static int ONLINETYPE_WEB = 1;
	@Column(name = "onlinetype", length = 4)
	private Integer onlinetype_fm_4; // 在线状态，0：app在线，1网页在线
	@Column(name = "cid", length = 32)
	private String cid_fe_32; // 用于getui推送
	@Column(name = "wsid", length = 32)
	private String wsid_fe_32; // 用token表示ctxs的name

	@Column(name = "lastlogintime", length = 13)
	private Long lastlogintime_nm_13; // 最后登录时间，系统自动维护
	@Column(name = "createdat", length = 13)
	private Long createdat_nm_13;
	@Column(name = "createdid", length = 10)
	private Long createdid_nm_10;
	@Column(name = "updatedat", length = 13)
	private Long updatedat_nm_13;
	@Column(name = "updatedid", length = 10)
	private Long updatedid_nm_10;

	public LoginInfo() {
		super();
		this.datastatus_fm_4 = Bean.CREATED;
	}

	public Long getSid_nm_10() {
		return sid_nm_10;
	}

	public void setSid_nm_10(Long sid_nm_10) {
		this.sid_nm_10 = sid_nm_10;
	}

	public Integer getOnlinetype_fm_4() {
		return onlinetype_fm_4;
	}

	public void setOnlinetype_fm_4(Integer onlinetype_fm_4) {
		this.onlinetype_fm_4 = onlinetype_fm_4;
	}

	public String getCid_fe_32() {
		return cid_fe_32;
	}

	public void setCid_fe_32(String cid_fe_32) {
		this.cid_fe_32 = cid_fe_32;
	}

	public Integer getDatastatus_fm_4() {
		return datastatus_fm_4;
	}

	public void setDatastatus_fm_4(Integer datastatus_fm_4) {
		this.datastatus_fm_4 = datastatus_fm_4;
	}

	public String getWsid_fe_32() {
		return wsid_fe_32;
	}

	public void setWsid_fe_32(String wsid_fe_32) {
		this.wsid_fe_32 = wsid_fe_32;
	}

	public Long getCorporationsid_nm_10() {
		return corporationsid_nm_10;
	}

	public void setCorporationsid_nm_10(Long corporationsid_nm_10) {
		this.corporationsid_nm_10 = corporationsid_nm_10;
	}

	public Integer getDept_fm_4() {
		return dept_fm_4;
	}

	public void setDept_fm_4(Integer dept_fm_4) {
		this.dept_fm_4 = dept_fm_4;
	}

	public Long getUsersid_nm_10() {
		return usersid_nm_10;
	}

	public void setUsersid_nm_10(Long usersid_nm_10) {
		this.usersid_nm_10 = usersid_nm_10;
	}

	public String getVcode_ne_6() {
		return vcode_ne_6;
	}

	public void setVcode_ne_6(String vcode_ne_6) {
		this.vcode_ne_6 = vcode_ne_6;
	}

	public Long getTokentime_nm_13() {
		return tokentime_nm_13;
	}

	public void setTokentime_nm_13(Long tokentime_nm_13) {
		this.tokentime_nm_13 = tokentime_nm_13;
	}

	public String getPhone_fe_11() {
		return phone_fe_11;
	}

	public void setPhone_fe_11(String phone_fe_11) {
		this.phone_fe_11 = phone_fe_11;
	}

	public String getToken_nm_32() {
		return token_nm_32;
	}

	public void setToken_nm_32(String token_nm_32) {
		this.token_nm_32 = token_nm_32;
	}

	public Integer getStatus_nm_4() {
		return status_nm_4;
	}

	public void setStatus_nm_4(Integer status_nm_4) {
		this.status_nm_4 = status_nm_4;
	}

	public String getUsername_tm_30() {
		return username_tm_30;
	}

	public void setUsername_tm_30(String username_tm_30) {
		this.username_tm_30 = username_tm_30;
	}

	public Integer getType_fm_4() {
		return type_fm_4;
	}

	public void setType_fm_4(Integer type_fm_4) {
		this.type_fm_4 = type_fm_4;
	}

	public String getSalt_te_10() {
		return salt_te_10;
	}

	public void setSalt_te_10(String salt_te_10) {
		this.salt_te_10 = salt_te_10;
	}

	public String getPassword_tm_100() {
		return password_tm_100;
	}

	public void setPassword_tm_100(String password_tm_100) {
		this.password_tm_100 = password_tm_100;
	}

	public Long getLastlogintime_nm_13() {
		return lastlogintime_nm_13;
	}

	public void setLastlogintime_nm_13(Long lastlogintime_nm_13) {
		this.lastlogintime_nm_13 = lastlogintime_nm_13;
	}

	public Long getCreatedat_nm_13() {
		return createdat_nm_13;
	}

	public void setCreatedat_nm_13(Long createdat_nm_13) {
		this.createdat_nm_13 = createdat_nm_13;
	}

	public Long getCreatedid_nm_10() {
		return createdid_nm_10;
	}

	public void setCreatedid_nm_10(Long createdid_nm_10) {
		this.createdid_nm_10 = createdid_nm_10;
	}

	public Long getUpdatedat_nm_13() {
		return updatedat_nm_13;
	}

	public void setUpdatedat_nm_13(Long updatedat_nm_13) {
		this.updatedat_nm_13 = updatedat_nm_13;
	}

	public Long getUpdatedid_nm_10() {
		return updatedid_nm_10;
	}

	public void setUpdatedid_nm_10(Long updatedid_nm_10) {
		this.updatedid_nm_10 = updatedid_nm_10;
	}

	// public void UpdateFromMap(Map<String, String> params, Session session)
	// throws Exception{
	// String username = params.get("username");
	// if(username != null){//全部role都可以修改
	// LoginInfo logininfo_checkusername = session.createQuery(
	// "from LoginInfo where username = '" + username + "' and sid <> " +
	// this.sid_nm_10,
	// LoginInfo.class).uniqueResult();
	// System.err.println("from LoginInfo where username = " + username);
	// if(logininfo_checkusername != null){
	// System.err.println(logininfo_checkusername);
	// throw new Exception("username already used");
	// }else{
	// this.username_tm_30 = username;
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
	@Override
	public boolean IsDeleted() {
		return this.datastatus_fm_4 == Bean.DELETED;
	}

	@Override
	public void SetDeleted() {
		this.datastatus_fm_4 = Bean.DELETED;
	}
	@Override
	public void setSid(Long sid){
		this.sid_nm_10 = sid;
	}
	@Override
	public Long getSid() {
		return this.sid_nm_10;
	}

	@Override
	public String toString() {
		return "LoginInfo [sid_nm_10=" + sid_nm_10 + ", usersid_nm_10=" + usersid_nm_10 + ", corporationsid_nm_10="
				+ corporationsid_nm_10 + ", dept_fm_4=" + dept_fm_4 + ", vcode_ne_6=" + vcode_ne_6 + ", status_nm_4="
				+ status_nm_4 + ", username_tm_30=" + username_tm_30 + ", salt_te_10=" + salt_te_10
				+ ", password_tm_100=" + password_tm_100 + ", phone_fe_11=" + phone_fe_11 + ", type_fm_4=" + type_fm_4
				+ ", tokentime_nm_13=" + tokentime_nm_13 + ", token_nm_32=" + token_nm_32 + ", datastatus_fm_4="
				+ datastatus_fm_4 + ", onlinetype_fm_4=" + onlinetype_fm_4 + ", cid_fe_32=" + cid_fe_32
				+ ", wsid_fe_32=" + wsid_fe_32 + ", lastlogintime_nm_13=" + lastlogintime_nm_13 + ", createdat_nm_13="
				+ createdat_nm_13 + ", createdid_nm_10=" + createdid_nm_10 + ", updatedat_nm_13=" + updatedat_nm_13
				+ ", updatedid_nm_10=" + updatedid_nm_10 + "]";
	}
	
}
