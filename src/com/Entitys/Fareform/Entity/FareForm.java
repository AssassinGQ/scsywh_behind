package com.Entitys.Fareform.Entity;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;

@Entity
@Table(name = "fareform")
public class FareForm  extends Bean {
	@Column(name = "ordersid", length = 20)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 99999999999999999999D)
	private Long ordersid;
	@Column(name = "trucksid", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long trucksid;
	@Column(name = "driversid", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long driversid;
	@Column(name = "escortsid", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long escortsid;
	@Column(name = "buyersid", length = 10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long buyersid;
	@Column(name = "loadaddr")
	@Valid(needValid = false)
	private String loadaddr;
	@Column(name = "unloadaddr")
	@Valid(needValid = false)
	private String unloadaddr;
	@Column(name = "loaddate", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long loaddate;
	@Column(name = "unloaddate", length = 13)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long unloaddate;
	@Column(name = "loadweight", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double loadweight;
	@Column(name = "zbweight", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double zbweight;
	@Column(name = "returnaddr")
	@Valid(needValid = false)
	private String returnaddr;
//	private String buyername_fm_30;
	@Column(name = "price", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double price;
	@Column(name = "mileload", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double mileload;
	@Column(name = "mileunload", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double mileunload;
	@Column(name = "roadtollload", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double roadtollload;
	@Column(name = "roadtollunload", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double roadtollunload;
	@Column(name = "roadtollcash", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double roadtollcash;
	@Column(name = "addfuelvol", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double addfuelvol;//变为总数
	@Column(name = "addfuelmoney", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double addfuelmoney;//变为总数
	@Column(name = "addfuelcash", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double addfuelcash;//变为总数
	@Column(name = "addfuel")
	@Valid(needValid = false)
	private String addfuel; //jsonarray。每项为jsonobject，包含addfuelvol， addfuelmoney，addfuelcash
	@Column(name = "allowancetravel", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double allowancetravel;
	@Column(name = "allowancenationalroad", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double allowancenationalroad;
	@Column(name = "fareaddwater", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double fareaddwater;
	@Column(name = "faremaintain", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double faremaintain;
	@Column(name = "farefine", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double farefine;
	@Column(name = "fareother", length = 10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double fareother;
	@Column(name = "remark")
	@Valid(needValid = false)
	private String remark;
//	@Column(name = "realloadweight", length = 10)
//	private Double realloadweight_fm_10; // 重车实际过磅重量总数
//	@Column(name = "realunloadweight", length = 10)
//	private Double realunloadweight_fm_10; // 空车实际过磅重量总数
//	@Column(name = "realloadweights")
//	private String realloadweights_fm_1000;//用jsonarry表示的各次过路费
//	@Column(name = "realunloadweights")
//	private String realunloadweights_fm_1000;//用jsonarry表示的各次过路费
	@Column(name = "realloadweight")
	@Valid(needValid = false)
	private String realloadweight;//jsonarray,各次重车过路费及其奖励，重车过路费奖励=1+2+...+(49-realloadweight)
	@Column(name = "realunloadweight")
	@Valid(needValid = false)
	private String realunloadweight;//jsonarray,各次空车过路费及其奖励，空车过路费奖励=1+2+...+(19-realloadweight)

	public final static int STATUS_CREATED = 0;
	public final static int STATUS_COMPLETED = 1;
	@Column(name = "fareformstatus", length = 4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer fareformstatus;
	public final static int STATUS_EDITABLE = 0;
	public final static int STATUS_UNEDITABLE = 1;
	@Column(name = "editable", length = 4)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999D)
	private Integer editable;
	@Column(name = "images")
	@Valid(needValid = false)
	private String images; // 上传的图像的名字
	// 自动生成
	@Column(name = "freight", length = 10)
	@Valid(needValid = false)
	private Double freight;// price*loadweight
	@Column(name = "miletotal", length = 10)
	@Valid(needValid = false)
	private Double miletotal;
	@Column(name = "allowanceloadroadtoll", length = 10)
	@Valid(needValid = false)
	private Double allowanceloadroadtoll; // 总重车过路费奖励
	@Column(name = "allowanceunloadroadtoll", length = 10)
	@Valid(needValid = false)
	private Double allowanceunloadroadtoll; // 总空车过路费奖励
	@Column(name = "addfueltotal", length = 10)
	@Valid(needValid = false)
	private Double addfueltotal; // 油费合计=addfuelmoney+addfuelcash
	@Column(name = "roadtolltotal", length = 10)
	@Valid(needValid = false)
	private Double roadtolltotal; // 过路费合计=roadtollload+roadtollunload+roadtollcash
	@Column(name = "allowancetotal", length = 10)
	@Valid(needValid = false)
	private Double allowancetotal; // 补助奖励合计=allowanceloadroadtoll+allowanceunloadroadtoll
	@Column(name = "drivercash", length = 10)
	@Valid(needValid = false)
	private Double drivercash; // 司机发生费用=roadtollcash+addfuelcash+allowancetravel+fareaddwater+faremaintain+farefine+fareother
	public FareForm() {
		super();
		this.mileload = Double.parseDouble("0");
		this.mileunload = Double.parseDouble("0");
		this.roadtollload = Double.parseDouble("0");
		this.roadtollunload = Double.parseDouble("0");
		this.roadtollcash = Double.parseDouble("0");
		this.addfuelvol = Double.parseDouble("0");
		this.addfuelmoney = Double.parseDouble("0");
		this.addfuelcash = Double.parseDouble("0");
		this.allowancetravel = Double.parseDouble("0");
		this.allowancenationalroad = Double.parseDouble("0");
		this.fareaddwater = Double.parseDouble("0");
		this.faremaintain = Double.parseDouble("0");
		this.farefine = Double.parseDouble("0");
		this.fareother = Double.parseDouble("0");
		this.miletotal = Double.parseDouble("0");
		this.allowanceloadroadtoll = Double.parseDouble("0");
		this.allowanceunloadroadtoll = Double.parseDouble("0");
		this.addfueltotal = Double.parseDouble("0");
		this.roadtolltotal = Double.parseDouble("0");
		this.allowancetotal = Double.parseDouble("0");
		this.drivercash = Double.parseDouble("0");
		this.loadweight = Double.parseDouble("0");
		this.zbweight = Double.parseDouble("0");
		this.price = Double.parseDouble("0");
		this.freight = Double.parseDouble("0");
	}

	public Long getOrdersid() {
		return ordersid;
	}

	public void setOrdersid(Long ordersid) {
		this.ordersid = ordersid;
	}

	public String getLoadaddr() {
		return loadaddr;
	}

	public void setLoadaddr(String loadaddr) {
		this.loadaddr = loadaddr;
	}

	public String getUnloadaddr() {
		return unloadaddr;
	}

	public void setUnloadaddr(String unloadaddr) {
		this.unloadaddr = unloadaddr;
	}

	public String getReturnaddr() {
		return returnaddr;
	}

	public void setReturnaddr(String returnaddr) {
		this.returnaddr = returnaddr;
	}

	public void setAddfuel(String addfuel) {
		this.addfuel = addfuel;
	}

	public void setRealloadweight(String realloadweight) {
		this.realloadweight = realloadweight;
	}

	public void setRealunloadweight(String realunloadweight) {
		this.realunloadweight = realunloadweight;
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

	public Long getBuyersid() {
		return buyersid;
	}

	public void setBuyersid(Long buyersid) {
		this.buyersid = buyersid;
	}

//	public String getBuyername_fm_30() {
//		return buyername_fm_30;
//	}
//
//	public void setBuyername_fm_30(String buyername_fm_30) {
//		this.buyername_fm_30 = buyername_fm_30;
//	}

	public Double getLoadweight() {
		return loadweight;
	}

	public void setLoadweight(Double loadweight) {
		this.loadweight = loadweight;
		this.freight = loadweight * this.price;
	}

	public Double getZbweight() {
		return zbweight;
	}

	public void setZbweight(Double zbweight) {
		this.zbweight = zbweight;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
		this.freight = this.loadweight * price;
	}

	public Double getFreight() {
		return freight;
	}

	public void setFreight(Double freight) {
		this.freight = freight;
	}

	public Long getLoaddate() {
		return loaddate;
	}

	public void setLoaddate(Long loaddate) {
		this.loaddate = loaddate;
	}

	public Long getUnloaddate() {
		return unloaddate;
	}

	public void setUnloaddate(Long unloaddate) {
		this.unloaddate = unloaddate;
	}

	public Double getMileload() {
		return mileload;
	}

	public void setMileload(Double mileload) {
		this.mileload = mileload;
		this.miletotal = this.mileload + this.mileunload;
	}

	public Double getMileunload() {
		return mileunload;
	}

	public void setMileunload(Double mileunload) {
		this.mileunload = mileunload;
		this.miletotal = this.mileload + this.mileunload;
	}

	public Double getRoadtollload() {
		return roadtollload;
	}

	public void setRoadtollload(Double roadtollload) {
		this.roadtollload = roadtollload;
		this.roadtolltotal = this.roadtollload + this.roadtollunload + this.roadtollcash;
	}

	public Double getRoadtollunload() {
		return roadtollunload;
	}

	public void setRoadtollunload(Double roadtollunload) {
		this.roadtollunload = roadtollunload;
		this.roadtolltotal = this.roadtollload + this.roadtollunload + this.roadtollcash;
	}

	public Double getRoadtollcash() {
		return roadtollcash;
	}

	public void setRoadtollcash(Double roadtollcash) {
		this.roadtollcash = roadtollcash;
		this.roadtolltotal = this.roadtollload + this.roadtollunload + this.roadtollcash;
		this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
				+ this.fareaddwater + this.faremaintain + this.farefine
				+ this.fareother;
	}

	public Double getAddfuelvol() {
		return addfuelvol;
	}

	public void setAddfuelvol(Double addfuelvol) {
		this.addfuelvol = addfuelvol;
	}

	public Integer getEditable() {
		return editable;
	}

	public void setEditable(Integer editable) {
		this.editable = editable;
	}

	public Double getAddfuelmoney() {
		return addfuelmoney;
	}

	public void setAddfuelmoney(Double addfuelmoney) {
		this.addfuelmoney = addfuelmoney;
		this.addfueltotal = this.addfuelmoney + this.addfuelcash;
	}

	public Double getAddfuelcash() {
		return addfuelcash;
	}

	public void setAddfuelcash(Double addfuelcash) {
		this.addfuelcash = addfuelcash;
		this.addfueltotal = this.addfuelmoney + this.addfuelcash;
		this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
				+ this.fareaddwater + this.faremaintain + this.farefine
				+ this.fareother;
	}

	public Double getAllowancetravel() {
		return allowancetravel;
	}

	public void setAllowancetravel(Double allowancetravel) {
		this.allowancetravel = allowancetravel;
		this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
				+ this.fareaddwater + this.faremaintain + this.farefine
				+ this.fareother;
	}

	public Double getAllowancenationalroad() {
		return allowancenationalroad;
	}

	public void setAllowancenationalroad(Double allowancenationalroad) {
		this.allowancenationalroad = allowancenationalroad;
	}

	public Double getFareaddwater() {
		return fareaddwater;
	}

	public void setFareaddwater(Double fareaddwater) {
		this.fareaddwater = fareaddwater;
		this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
				+ this.fareaddwater + this.faremaintain + this.farefine
				+ this.fareother;
	}

	public Double getFaremaintain() {
		return faremaintain;
	}

	public void setFaremaintain(Double faremaintain) {
		this.faremaintain = faremaintain;
		this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
				+ this.fareaddwater + this.faremaintain + this.farefine
				+ this.fareother;
	}

	public Double getFarefine() {
		return farefine;
	}

	public void setFarefine(Double farefine) {
		this.farefine = farefine;
		this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
				+ this.fareaddwater + this.faremaintain + this.farefine
				+ this.fareother;
	}

	public Double getFareother() {
		return fareother;
	}

	public void setFareother(Double fareother) {
		this.fareother = fareother;
		this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
				+ this.fareaddwater + this.faremaintain + this.farefine
				+ this.fareother;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getAddfuel() {
		return addfuel;
	}

	public String getRealloadweight() {
		return realloadweight;
	}

	public String getRealunloadweight() {
		return realunloadweight;
	}

	public Integer getFareformstatus() {
		return fareformstatus;
	}

	public void setFareformstatus(Integer fareformstatus) {
		this.fareformstatus = fareformstatus;
	}

	public String getImages() {
		return images;
	}

	public void setImages(String images) {
		this.images = images;
	}

	public Double getMiletotal() {
		return miletotal;
	}

	public void setMiletotal(Double miletotal) {
		this.miletotal = miletotal;
	}

	public Double getAllowanceloadroadtoll() {
		return allowanceloadroadtoll;
	}

	public void setAllowanceloadroadtoll(Double allowanceloadroadtoll) {
		this.allowanceloadroadtoll = allowanceloadroadtoll;
	}

	public Double getAllowanceunloadroadtoll() {
		return allowanceunloadroadtoll;
	}

	public void setAllowanceunloadroadtoll(Double allowanceunloadroadtoll) {
		this.allowanceunloadroadtoll = allowanceunloadroadtoll;
	}

	public Double getAddfueltotal() {
		return addfueltotal;
	}

	public void setAddfueltotal(Double addfueltotal) {
		this.addfueltotal = addfueltotal;
	}

	public Double getRoadtolltotal() {
		return roadtolltotal;
	}

	public void setRoadtolltotal(Double roadtolltotal) {
		this.roadtolltotal = roadtolltotal;
	}

	public Double getAllowancetotal() {
		return allowancetotal;
	}

	public void setAllowancetotal(Double allowancetotal) {
		this.allowancetotal = allowancetotal;
	}

	public Double getDrivercash() {
		return drivercash;
	}

	public void setDrivercash(Double drivercash) {
		this.drivercash = drivercash;
	}

	public void UpdateFromMap(Map<String, String> params) {
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String value = entry.getValue();
			Double dvalue = null;
			try {
				dvalue = Double.parseDouble(value);
			} catch (Exception e) {
			}
			switch (key) {
			case "sid":
			case "ordersid":
			case "createdat":
			case "createdid":
			case "updatedat":
			case "updatedid":
			case "miletotal":
			case "allowanceloadroadtoll":
			case "allowanceunloadroadtoll":
			case "addfueltotal":
			case "roadtolltotal":
			case "allowancetotal":
			case "drivercash":
			case "fareformstatus":
			case "time":
			case "trucksid":
			case "driversid":
			case "escortsid":
			case "buyersid":
			case "loadaddr":
			case "unloadaddr":
			case "price":
			case "freight":
			case "zbweight":
			case "loadweight":
			case "loaddate":
			case "unlaoddate":
			case "fareaddwater":
			case "realloadweight":
			case "realunloadweight":
			case "addfuelvol":
			case "corporationsid":
			case "addfuelmoney":
			case "addfuelcash":
			case "editable":
			case "datastatus":
				break;
			case "mileload":
				this.mileload = dvalue;
				this.miletotal = this.mileload + this.mileunload;
				break;
			case "mileunload":
				this.mileunload = dvalue;
				this.miletotal = this.mileload + this.mileunload;
				break;
			case "roadtollload":
				this.roadtollload = dvalue;
				this.roadtolltotal = this.roadtollload + this.roadtollunload
						+ this.roadtollcash;
				break;
			case "roadtollunload":
				this.roadtollunload = dvalue;
				this.roadtolltotal = this.roadtollload + this.roadtollunload
						+ this.roadtollcash;
				break;
			case "roadtollcash":
				this.roadtollcash = dvalue;
				this.roadtolltotal = this.roadtollload + this.roadtollunload
						+ this.roadtollcash;
				this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
						+ this.fareaddwater + this.faremaintain + this.farefine
						+ this.fareother;
				break;
			case "allowancetravel":
				this.allowancetravel = dvalue;
				this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
						+ this.fareaddwater + this.faremaintain + this.farefine
						+ this.fareother;
				break;
			case "allowancenationalroad":
				this.allowancenationalroad = dvalue;
				break;
			case "faremaintain":
				this.faremaintain = dvalue;
				this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
						+ this.fareaddwater + this.faremaintain + this.farefine
						+ this.fareother;
				break;
			case "farefine":
				this.farefine = dvalue;
				this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
						+ this.fareaddwater + this.faremaintain + this.farefine
						+ this.fareother;
				break;
			case "fareother":
				this.fareother = dvalue;
				this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
						+ this.fareaddwater + this.faremaintain + this.farefine
						+ this.fareother;
				break;
			case "remark":
				this.remark = value;
				break;
			case "images":
				this.images = value;
				break;
			case "returnaddr":
				this.returnaddr = value;
				break;
			default:
				break;
			}
		}
	}
	public void ReupdateFromMap(Map<String, String> params) {
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String value = entry.getValue();
			Double dvalue = null;
			try {
				dvalue = Double.parseDouble(value);
			} catch (Exception e) {
			}
			switch (key) {
			case "sid":
			case "ordersid":
			case "createdat":
			case "createdid":
			case "updatedat":
			case "updatedid":
			case "miletotal":
			case "allowanceloadroadtoll":
			case "allowanceunloadroadtoll":
			case "addfueltotal":
			case "roadtolltotal":
			case "allowancetotal":
			case "drivercash":
			case "fareformstatus":
			case "time":
			case "trucksid":
			case "driversid":
			case "escortsid":
			case "buyersid":
			case "loadaddr":
			case "unloadaddr":
			case "price":
			case "freight":
			case "zbweight":
			case "loadweight":
			case "loaddate":
			case "unlaoddate":
			case "fareaddwater":
			case "realloadweight":
			case "realunloadweight":
			case "addfuelvol":
			case "corporationsid":
			case "images":
			case "addfuelmoney":
			case "addfuelcash":
			case "returnaddr":
			case "editable":
			case "datastatus":
				break;
			case "mileload":
				this.mileload = dvalue;
				this.miletotal = this.mileload + this.mileunload;
				break;
			case "mileunload":
				this.mileunload = dvalue;
				this.miletotal = this.mileload + this.mileunload;
				break;
			case "roadtollload":
				this.roadtollload = dvalue;
				this.roadtolltotal = this.roadtollload + this.roadtollunload
						+ this.roadtollcash;
				break;
			case "roadtollunload":
				this.roadtollunload = dvalue;
				this.roadtolltotal = this.roadtollload + this.roadtollunload
						+ this.roadtollcash;
				break;
			case "roadtollcash":
				this.roadtollcash = dvalue;
				this.roadtolltotal = this.roadtollload + this.roadtollunload
						+ this.roadtollcash;
				this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
						+ this.fareaddwater + this.faremaintain + this.farefine
						+ this.fareother;
				break;
			case "allowancetravel":
				this.allowancetravel = dvalue;
				this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
						+ this.fareaddwater + this.faremaintain + this.farefine
						+ this.fareother;
				break;
			case "allowancenationalroad":
				this.allowancenationalroad = dvalue;
				break;
			case "faremaintain":
				this.faremaintain = dvalue;
				this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
						+ this.fareaddwater + this.faremaintain + this.farefine
						+ this.fareother;
				break;
			case "farefine":
				this.farefine = dvalue;
				this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
						+ this.fareaddwater + this.faremaintain + this.farefine
						+ this.fareother;
				break;
			case "fareother":
				this.fareother = dvalue;
				this.drivercash = this.roadtollcash + this.addfuelcash + this.allowancetravel
						+ this.fareaddwater + this.faremaintain + this.farefine
						+ this.fareother;
				break;
			case "remark":
				this.remark = value;
				break;
			default:
				break;
			}
		}
	}
	public boolean addimage(Long sid){
		JSONArray jsonArray = null;
		if(this.images == null)
			jsonArray = new JSONArray();
		else {
			try {
				jsonArray = new JSONArray(this.images);
			} catch (Exception e) {
				return false;
			}
		}
		jsonArray.put(sid);
		this.images = jsonArray.toString();
		return true;
	}
	public boolean removeimage(Long sid){
		if(sid == null)
			return false;
		if(this.images == null)
			return false;
		else {
			try {
				JSONArray jsonArray = new JSONArray(this.images);
				Integer index = null;
				for(int i = 0; i < jsonArray.length(); i++){
					if(sid.equals(jsonArray.getLong(i))){
						index = i;
						break;
					}
				}
				if(index == null)
					return false;
				else{
					jsonArray.remove(index);
					this.images = jsonArray.toString();
					return true;
				}
			} catch (Exception e) {
				return false;
			}
		}
	}
	public boolean addfuel(Double addfuelvol, Double addfuelmoney, Double addfuelcash){
		JSONArray jsonArray = null;
		if(this.addfuel == null)
			jsonArray = new JSONArray();
		else{
			try {
				jsonArray = new JSONArray(this.addfuel);
			} catch (Exception e) {
				return false;
			}
		}
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("addfuelvol", addfuelvol);
			jsonObject.put("addfuelmoney", addfuelmoney);
			jsonObject.put("addfuelcash", addfuelcash);
		} catch (JSONException e) {
			return false;
		}
		jsonArray.put(jsonObject);
		this.addfuel = jsonArray.toString();
		setAddfuelvol(this.addfuelvol+addfuelvol);
		setAddfuelmoney(this.addfuelmoney+addfuelmoney);
		setAddfuelcash(this.addfuelcash+addfuelcash);
		return true;
	}
	public boolean addrealloadweight(Double realloadweight){
		JSONArray jsonArray = null;
		if(this.realloadweight == null)
			jsonArray = new JSONArray();
		else
			try {
				jsonArray = new JSONArray(this.realloadweight);
			} catch (JSONException e) {
				return false;
			}
		int n = (int) (49 - realloadweight);
		Double allowanceloadroadtoll = ((double) (n + n * n)) / 2;
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("realloadweight", realloadweight);
			jsonObject.put("allowanceloadroadtoll", allowanceloadroadtoll);
		} catch (JSONException e) {
			return false;
		}
		jsonArray.put(jsonObject);
		this.realloadweight = jsonArray.toString();
		this.allowanceloadroadtoll += allowanceloadroadtoll;
		this.allowancetotal += allowanceloadroadtoll;
		return true;
	}
	public boolean addrealunloadweight(Double realunloadweight){
		JSONArray jsonArray = null;
		if(this.realunloadweight == null)
			jsonArray = new JSONArray();
		else
			try {
				jsonArray = new JSONArray(this.realunloadweight);
			} catch (JSONException e) {
				return false;
			}
		int n = (int) (19 - realunloadweight);
		Double allowanceunloadroadtoll = ((double) (n + n * n)) / 2;
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("realunloadweight", realunloadweight);
			jsonObject.put("allowanceunloadroadtoll", allowanceunloadroadtoll);
		} catch (JSONException e) {
			return false;
		}
		jsonArray.put(jsonObject);
		this.realunloadweight = jsonArray.toString();
		this.allowanceunloadroadtoll += allowanceunloadroadtoll;
		this.allowancetotal += allowanceunloadroadtoll;
		return true;
	}
}
