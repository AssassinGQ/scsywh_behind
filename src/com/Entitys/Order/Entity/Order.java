package com.Entitys.Order.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;
import com.Common.Entitys.LoginableBasicInfo;

@Entity
@Table(name = "orderp")
public class Order  extends Bean {
	public final static int STATUS_ORDERED = 0;// 已下单
	public final static int STATUS_DISPATCHED = 1;// 已调度
	public final static int STATUS_CHECKED = 2;// 已安检
	public final static int STATUS_DISTRIBUTED = 3;// 已派发
	public final static int STATUS_RECEIVED = 4;// 司机已经收到
	public final static int STATUS_LOADED = 5;// 装货完成
	public final static int STATUS_UNLOADED = 6;// 卸货完成
	public final static int STATUS_RETURNED = 7;// 司机已回场，上传了回执和费用清单
	// public final static int STATUS_PRICED = 8;// 填写单价,在路线信息里面改
	public final static int STATUS_VERIFYEDPPP = 9;// 第一次审核通过(审核公里数)
	public final static int STATUS_VERIFYEDPP = 10;// 第二次审核通过(审核非财务信息)
	public final static int STATUS_VERIFYEDP = 11;// 第三次审核通过（审核财务信息）
	public final static int STATUS_VERIFYED = 12;// 第四次审核通过，完成(审核全部信息)
	public final static int STATUS_REVERIFY = 13;// 审核失败，等待修改后再从第一步审核
	// sid自动生成
	// 订单状态只能由系统更改,0已下单1已派发2已装货3已卸货4已回场
	@Column(name = "orderstatus", length = 4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer orderstatus;
	// 下订单时明确
	@Column(name = "ordertime", length = 13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long ordertime;
	@Column(name = "sellersid", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long sellersid;
	@Column(name = "buyersid", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long buyersid;
	@Column(name = "productsid", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long productsid;
	@Column(name = "loaddateddl", length = 13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long loaddateddl;
	@Column(name = "unloaddateddl", length = 13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long unloaddateddl;
	@Column(name = "loadaddr")
	@Valid(needValid = false)
	private String loadaddr;
	@Column(name = "unloadaddr")
	@Valid(needValid = false)
	private String unloadaddr;
	@Column(name = "productweight", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double productweight;
	@Column(name = "productvol", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double productvol;
	/////////////////////////////////////////////////////////////////////
	// 调度时明确
	@Column(name = "dispatchtime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long dispatchtime;
	@Column(name = "trucksid", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long trucksid;
	@Column(name = "driversid", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long driversid;
	@Column(name = "escortsid", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long escortsid;
	@Column(name = "trailersid", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long trailersid;
	@Column(name = "routesid", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long routesid;
	@Column(name = "price", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double price;
	@Column(name = "remark")
	@Valid(needValid = false)
	private String remark;
	////////////////////////////////////////////////////////////////////////////
	// 安全检查时明确
	@Column(name = "checktime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long checktime;
	@Column(name = "checkret", length = 30)
	@Valid(nullAble = true, varType = VarType.String, maxLength = 30)
	private String checkret;// 用二进制字符串表示
	@Column(name = "checkstatus", length = 4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 1D)
	private Integer checkstatus;// 0通过1不通过
	///////////////////////////////////////////////////////////////////////
	// 派发时系统生成
	@Column(name = "distributetime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long distributetime;
	@Column(name = "fareformsid", length = 20)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 99999999999999999999D)
	private Long fareformsid;
	/////////////////////////////////////////////////////////////////////////
	// 接受任务时
	@Column(name = "receivetime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long receivetime;
	//////////////////////////////////////////////////////////////////////
	// 装货完成后
	@Column(name = "loadtime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long loadtime;
	@Column(name = "loadweight", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double loadweight;
	@Column(name = "zbweight", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double zbweight; // 折白重量
	/////////////////////////////////////////////////////////////////////////
	// 卸货完成后
	@Column(name = "unloadtime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long unloadtime;
	@Column(name = "unloadweight", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double unloadweight;
	///////////////////////////////////////////////////////////////////
	// 运输任务完成，获得回执时
	@Column(name = "returntime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long returntime;
	@Column(name = "returnaddr")
	@Valid(needValid = false)
	private String returnaddr;
	///////////////////////////////////////////////////////////////////
	// 第一次审核
	@Column(name = "verifyppptime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long verifyppptime;
	@Column(name = "verifypppstatus", length = 4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer verifypppstatus;
	@Column(name = "verifypppret")
	@Valid(needValid = false)
	private String verifypppret;
	///////////////////////////////////////////////////////////////////
	// 第一次审核
	// @Column(name = "pricetime", length = 13)
	// private Long pricetime;
	///////////////////////////////////////////////////////////////////
	// 第二次审核
	@Column(name = "verifypptime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long verifypptime;
	@Column(name = "verifyppstatus", length = 4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer verifyppstatus;
	@Column(name = "verifyppret")
	@Valid(needValid = false)
	private String verifyppret;
	///////////////////////////////////////////////////////////////////
	// 第三次审核
	@Column(name = "verifyptime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long verifyptime;
	@Column(name = "verifypstatus", length = 4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer verifypstatus;
	@Column(name = "verifypret")
	@Valid(needValid = false)
	private String verifypret;
	///////////////////////////////////////////////////////////////////
	// 通过审核后，整个订单完成
	@Column(name = "verifytime", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long verifytime;
	@Column(name = "verifystatus", length = 4)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer verifystatus;
	@Column(name = "verifyret")
	@Valid(needValid = false)
	private String verifyret;
	//////////////////////////////////////////////////////////
	// 用于统计的信息
	@Column(name = "fuelused", length=10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double fuelused;
	@Column(name = "distance", length=10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double distance;
	@Column(name = "output", length=10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double output;

	public Order() {
		super();
	}

	public Long getFareformsid() {
		return fareformsid;
	}

	public void setFareformsid(Long fareformsid) {
		this.fareformsid = fareformsid;
	}


	public Integer getOrderstatus() {
		return orderstatus;
	}

	public void setOrderstatus(Integer orderstatus) {
		this.orderstatus = orderstatus;
	}

	public Long getOrdertime() {
		return ordertime;
	}

	public void setOrdertime(Long ordertime) {
		this.ordertime = ordertime;
	}


	public Long getSellersid() {
		return sellersid;
	}

	public void setSellersid(Long sellersid) {
		this.sellersid = sellersid;
	}

	public Long getBuyersid() {
		return buyersid;
	}

	public void setBuyersid(Long buyersid) {
		this.buyersid = buyersid;
	}

	public Long getProductsid() {
		return productsid;
	}

	public void setProductsid(Long productsid) {
		this.productsid = productsid;
	}

	public Long getLoaddateddl() {
		return loaddateddl;
	}

	public void setLoaddateddl(Long loaddateddl) {
		this.loaddateddl = loaddateddl;
	}

	public Long getUnloaddateddl() {
		return unloaddateddl;
	}

	public void setUnloaddateddl(Long unloaddateddl) {
		this.unloaddateddl = unloaddateddl;
	}

//	public String getCorporationname() {
//		return corporationname;
//	}
//
//	public void setCorporationname(String corporationname) {
//		this.corporationname = corporationname;
//	}
//
//	public String getSellername() {
//		return sellername;
//	}
//
//	public void setSellername(String sellername) {
//		this.sellername = sellername;
//	}
//
//	public String getSellerphone_fm_11() {
//		return sellerphone_fm_11;
//	}
//
//	public void setSellerphone_fm_11(String sellerphone_fm_11) {
//		this.sellerphone_fm_11 = sellerphone_fm_11;
//	}

	public String getLoadaddr() {
		return loadaddr;
	}

	public void setLoadaddr(String loadaddr) {
		this.loadaddr = loadaddr;
	}

//	public String getBuyername() {
//		return buyername;
//	}
//
//	public void setBuyername(String buyername) {
//		this.buyername = buyername;
//	}
//
//	public String getBuyerphone_fm_11() {
//		return buyerphone_fm_11;
//	}
//
//	public void setBuyerphone_fm_11(String buyerphone_fm_11) {
//		this.buyerphone_fm_11 = buyerphone_fm_11;
//	}

	public String getUnloadaddr() {
		return unloadaddr;
	}

	public void setUnloadaddr(String unloadaddr) {
		this.unloadaddr = unloadaddr;
	}

//	public String getProductname() {
//		return productname;
//	}
//
//	public void setProductname(String productname) {
//		this.productname = productname;
//	}
//
//	public String getProducttype() {
//		return producttype;
//	}
//
//	public void setProducttype(String producttype) {
//		this.producttype = producttype;
//	}
//
//	public String getPackettype() {
//		return packettype;
//	}
//
//	public void setPackettype(String packettype) {
//		this.packettype = packettype;
//	}

	public Double getProductweight() {
		return productweight;
	}

	public void setProductweight(Double productweight) {
		this.productweight = productweight;
	}

	public Double getProductvol() {
		return productvol;
	}

	public void setProductvol(Double productvol) {
		this.productvol = productvol;
	}

	public Long getDispatchtime() {
		return dispatchtime;
	}

	public void setDispatchtime(Long dispatchtime) {
		this.dispatchtime = dispatchtime;
	}

	public Long getTrucksid() {
		return trucksid;
	}

	public void setTrucksid(Long trucksid) {
		this.trucksid = trucksid;
	}

	public Long getDriversid() {
		return driversid;
	}

	public void setDriversid(Long driversid) {
		this.driversid = driversid;
	}

	public Long getEscortsid() {
		return escortsid;
	}

	public void setEscortsid(Long escortsid) {
		this.escortsid = escortsid;
	}

	public Long getTrailersid() {
		return trailersid;
	}

	public void setTrailersid(Long trailersid) {
		this.trailersid = trailersid;
	}

	public Long getRoutesid() {
		return routesid;
	}

	public void setRoutesid(Long routesid) {
		this.routesid = routesid;
	}

//	public String getTrucknumber() {
//		return trucknumber;
//	}
//
//	public void setTrucknumber(String trucknumber) {
//		this.trucknumber = trucknumber;
//	}
//
//	public String getDrivername() {
//		return drivername;
//	}
//
//	public void setDrivername(String drivername) {
//		this.drivername = drivername;
//	}
//
//	public String getTrailernumber() {
//		return trailernumber;
//	}
//
//	public void setTrailernumber(String trailernumber) {
//		this.trailernumber = trailernumber;
//	}
//
//	public String getEscortname() {
//		return escortname;
//	}
//
//	public void setEscortname(String escortname) {
//		this.escortname = escortname;
//	}
//
//	public String getRoutename() {
//		return routename;
//	}
//
//	public void setRoutename(String routename) {
//		this.routename = routename;
//	}
//
//	public Double getRoutedistance() {
//		return routedistance;
//	}
//
//	public void setRoutedistance(Double routedistance) {
//		this.routedistance = routedistance;
//	}

	public Double getFuelused() {
		return fuelused;
	}

	public void setFuelused(Double fuelused) {
		this.fuelused = fuelused;
	}

	public Double getOutput() {
		return output;
	}

	public void setOutput(Double output) {
		this.output = output;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Long getChecktime() {
		return checktime;
	}

	public void setChecktime(Long checktime) {
		this.checktime = checktime;
	}

	public String getCheckret() {
		return checkret;
	}

	public void setCheckret(String checkret) {
		this.checkret = checkret;
	}

	public Integer getCheckstatus() {
		return checkstatus;
	}

	public void setCheckstatus(Integer checkstatus) {
		this.checkstatus = checkstatus;
	}

	public Long getDistributetime() {
		return distributetime;
	}

	public void setDistributetime(Long distributetime) {
		this.distributetime = distributetime;
	}

//	public Long getTrucklogsid() {
//		return trucklogsid;
//	}
//
//	public void setTrucklogsid(Long trucklogsid) {
//		this.trucklogsid = trucklogsid;
//	}

	public Long getReceivetime() {
		return receivetime;
	}

	public void setReceivetime(Long receivetime) {
		this.receivetime = receivetime;
	}

	public Long getLoadtime() {
		return loadtime;
	}

	public void setLoadtime(Long loadtime) {
		this.loadtime = loadtime;
	}

	public Double getLoadweight() {
		return loadweight;
	}

	public void setLoadweight(Double loadweight) {
		this.loadweight = loadweight;
	}

	public Double getZbweight() {
		return zbweight;
	}

	public void setZbweight(Double zbweight) {
		this.zbweight = zbweight;
	}

	public Long getUnloadtime() {
		return unloadtime;
	}

	public void setUnloadtime(Long unloadtime) {
		this.unloadtime = unloadtime;
	}

	public Double getUnloadweight() {
		return unloadweight;
	}

	public void setUnloadweight(Double unloadweight) {
		this.unloadweight = unloadweight;
	}

	public Long getReturntime() {
		return returntime;
	}

	public void setReturntime(Long returntime) {
		this.returntime = returntime;
	}

	public String getReturnaddr() {
		return returnaddr;
	}

	public void setReturnaddr(String returnaddr) {
		this.returnaddr = returnaddr;
	}

	public Integer getVerifyppstatus() {
		return verifyppstatus;
	}

	public void setVerifyppstatus(Integer verifyppstatus) {
		this.verifyppstatus = verifyppstatus;
	}

	public Integer getVerifypstatus() {
		return verifypstatus;
	}

	public void setVerifypstatus(Integer verifypstatus) {
		this.verifypstatus = verifypstatus;
	}

	public Integer getVerifystatus() {
		return verifystatus;
	}

	public void setVerifystatus(Integer verifystatus) {
		this.verifystatus = verifystatus;
	}

	public Long getVerifyppptime() {
		return verifyppptime;
	}

	public void setVerifyppptime(Long verifyppptime) {
		this.verifyppptime = verifyppptime;
	}

	public Long getVerifypptime() {
		return verifypptime;
	}

	public void setVerifypptime(Long verifypptime) {
		this.verifypptime = verifypptime;
	}

	public Long getVerifyptime() {
		return verifyptime;
	}

	public void setVerifyptime(Long verifyptime) {
		this.verifyptime = verifyptime;
	}

	public Long getVerifytime() {
		return verifytime;
	}

	public void setVerifytime(Long verifytime) {
		this.verifytime = verifytime;
	}

	public Integer getVerifypppstatus() {
		return verifypppstatus;
	}

	public void setVerifypppstatus(Integer verifypppstatus) {
		this.verifypppstatus = verifypppstatus;
	}

	public String getVerifypppret() {
		return verifypppret;
	}

	public void setVerifypppret(String verifypppret) {
		this.verifypppret = verifypppret;
	}

	// public Long getPricetime() {
	// return pricetime;
	// }
	//
	// public void setPricetime(Long pricetime) {
	// this.pricetime = pricetime;
	// }

	public String getVerifyppret() {
		return verifyppret;
	}

	public void setVerifyppret(String verifyppret) {
		this.verifyppret = verifyppret;
	}

	public String getVerifypret() {
		return verifypret;
	}

	public void setVerifypret(String verifypret) {
		this.verifypret = verifypret;
	}

	public String getVerifyret() {
		return verifyret;
	}

	public void setVerifyret(String verifyret) {
		this.verifyret = verifyret;
	}

	// public boolean WritePremission(String key, int role) {
	// if(role == LoginableBasicInfo.ROLE_SYSTEM)
	// return true;
	// switch (key) {
	// case "sid":
	// case "corporationsid":
	// case "createdat":
	// case "createdid":
	// case "updatedat":
	// case "updatedid":
	// case "orderstatus":
	// case "ordertime":
	// case "dispatchtime":
	// case "checktime":
	// case "distributetime":
	// case "trucklogsid":
	// case "fareFormsid":
	// case "receivetime":
	// case "loadtime":
	// case "unloadtime":
	// case "veritypptime":
	// case "verityptime":
	// case "veritytime":
	// case "":
	// return role > LoginableBasicInfo.ROLE_SYSTEM;
	// case "trucksid":
	// case "trailersid":
	// case "driversid":
	// case "escortsid":
	// case "routesid":
	// case "trucknumber":
	// case "trailernumber":
	// case "drivername":
	// case "escortname":
	// case "routename":
	// case "distance":
	// case "remark":
	// case "returntime":
	// case "verityppstatus":
	// case "veritypstatus":
	// case "veritystatus":
	// return role >= LoginableBasicInfo.ROLE_ADMIN;
	// case "sellersid":
	// case "buyersid":
	// case "productsid":
	// case "loaddateddl":
	// case "unloaddateddl":
	// case "corporationname":
	// case "sellername":
	// case "sellerphone":
	// case "loadaddr":
	// case "buyername":
	// case "buyerphone":
	// case "unloadaddr":
	// case "productname":
	// case "producttype":
	// case "packettype":
	// case "productweight":
	// case "productvol":
	// case "checkret":
	// case "checkstatus":
	// case "loadweight":
	// case "zbweight":
	// case "unloadweight":
	// case "returnaddr":
	// return role >= LoginableBasicInfo.ROLE_USER;
	// default:
	// return false;
	// }
	// }
	public boolean ReadPremission(String key, int role) {
		if(role == LoginableBasicInfo.ROLE_GOVERNMENT)
			return true;
		switch (key) {
		case "trucklogsid":
		case "fareFormsid":
			return role >= LoginableBasicInfo.ROLE_SUPER;
		case "createdat":
		case "createdid":
		case "updatedat":
		case "updatedid":
		case "corporationsid":
		case "sellersid":
		case "buyersid":
		case "productsid":
		case "trucksid":
		case "driversid":
		case "trailersid":
		case "escortsid":
		case "routesid":
		case "distance":
		case "fuelused":
		case "output":
			return role >= LoginableBasicInfo.ROLE_ADMIN;
		case "sid":
		case "ordertime":
		case "dispatchtime":
		case "checktime":
		case "orderstatus":
		case "loaddateddl":
		case "unloaddateddl":
		case "corporationname":
		case "sellername":
		case "sellerphone":
		case "loadaddr":
		case "unloadaddr":
		case "buyername":
		case "buyerphone":
		case "productname":
		case "producttype":
		case "productweight":
		case "productvol":
		case "trucknumber":
		case "trailernumber":
		case "drivername":
		case "escortname":
		case "routename":
		case "routedistance":
		case "remark":
		case "checkret":
		case "checkstatus":
		case "distributetime":
		case "receivetime":
		case "loadtime":
		case "loadweight":
		case "zbweight":
		case "price":
		case "unloadtime":
		case "unloadweight":
		case "returntime":
		case "returnaddr":
		case "veritypptime":
		case "verityppstatus":
		case "verityptime":
		case "veritypstatus":
		case "veritytime":
		case "veritystatus":
			return role >= LoginableBasicInfo.ROLE_USER;
		default:
			return false;
		}
	}
}
