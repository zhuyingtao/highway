package com.siat.ds;

/**
 * @ClassName Station
 * @Description 基站信息
 * @author Zhu Yingtao
 * @date 2014年12月16日 下午2:54:05
 */
public class Station {

	private int serialNumber;
	private int cellId;
	private int lacId;
	private int netType;
	private double longitude;
	private double latitude;

	/**
	 * @param cellId
	 * @param lacId
	 * @param longitude
	 * @param latitude
	 */
	public Station(int cellId, int lacId, double longitude, double latitude) {
		super();
		this.cellId = cellId;
		this.lacId = lacId;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public Station(int cellId) {
		this.cellId = cellId;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}

	public int getCellId() {
		return cellId;
	}

	public void setCellId(int cellId) {
		this.cellId = cellId;
	}

	public int getLacId() {
		return lacId;
	}

	public void setLacId(int lacId) {
		this.lacId = lacId;
	}

	public int getNetType() {
		return netType;
	}

	public void setNetType(int netType) {
		this.netType = netType;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
}
