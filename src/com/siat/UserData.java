/**
 * 
 */
package com.siat;

import java.util.Date;

/**
 * @ClassName UserData
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014��12��16�� ����2:55:04
 */
public class UserData {

	private String tmsi; // Ψһ��ʶ�ֻ�
	private Date timestamp; // ʱ���
	private int lac; // λ�������
	private int cellid; // ��վ����
	private int eventid; // �¼�����
	private int id; // ��·ID

	/**
	 * @param tmsi
	 * @param timestamp
	 * @param lac
	 * @param cellid
	 * @param eventid
	 * @param id
	 */
	public UserData(String tmsi, Date timestamp, int lac, int cellid,
			int eventid, int id) {
		super();
		this.tmsi = tmsi;
		this.timestamp = timestamp;
		this.lac = lac;
		this.cellid = cellid;
		this.eventid = eventid;
		this.id = id;
	}

	public String getTmsi() {
		return tmsi;
	}

	public void setTmsi(String tmsi) {
		this.tmsi = tmsi;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getLac() {
		return lac;
	}

	public void setLac(int lac) {
		this.lac = lac;
	}

	public int getCellid() {
		return cellid;
	}

	public void setCellid(int cellid) {
		this.cellid = cellid;
	}

	public int getEventid() {
		return eventid;
	}

	public void setEventid(int eventid) {
		this.eventid = eventid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return this.tmsi.equals(((UserData) obj).tmsi);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.tmsi + "," + this.timestamp + "," + this.lac + ","
				+ this.cellid + "," + this.eventid + "," + this.id + "\n";
	}

}
