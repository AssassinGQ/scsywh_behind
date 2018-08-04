package com.Common.Enums;

public enum SnowFlake_DataID {
	ORDER_DATAID("Order", 0L),
	FAREFORM_DATAID("FareForm", 1L),
	LOCK_DATAID("Lock", 2L),
	TRUCKLOG_DATAID("TruckLog", 3L),
	WARN_DATAID("Warn", 4L),
	LASTTRUCKLOG_DATAID("LastTruckLog", 5L);
	
	private String DataName;
	private long DataId;
	private SnowFlake_DataID(String dataname, long dataid) {
		this.DataName = dataname;
		this.DataId = dataid;
	}
	
	public String getDataName() {
		return DataName;
	}

	public long getDataId() {
		return DataId;
	}

	public static SnowFlake_DataID getEnum(String dataName){
		SnowFlake_DataID entityStatus = null;
		SnowFlake_DataID[] enumAry = SnowFlake_DataID.values();
		for(int i = 0; i < enumAry.length; i++){
			if(enumAry[i].getDataName().equals(dataName)){
				entityStatus = enumAry[i];
				break;
			}
		}
		return entityStatus;
	}
	
	public static SnowFlake_DataID getEnum(long dataid){
		SnowFlake_DataID entityStatus = null;
		SnowFlake_DataID[] enumAry = SnowFlake_DataID.values();
		for(int i = 0; i < enumAry.length; i++){
			if(enumAry[i].getDataId() == dataid){
				entityStatus = enumAry[i];
				break;
			}
		}
		return entityStatus;
	}
	
	public static boolean contains(String dataname){
		SnowFlake_DataID[] enumAry = SnowFlake_DataID.values();
		for(int i = 0; i < enumAry.length; i++){
			if(enumAry[i].getDataName().equals(dataname)){
				return true;
			}
		}
		return false;
	}
	
	public static boolean contains(long dataid){
		SnowFlake_DataID[] enumAry = SnowFlake_DataID.values();
		for(int i = 0; i < enumAry.length; i++){
			if(enumAry[i].getDataId() == dataid){
				return true;
			}
		}
		return false;
	}
}
