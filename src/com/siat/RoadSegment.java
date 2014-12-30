package com.siat;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName RoadSegment
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014��12��16�� ����2:54:15
 */
public class RoadSegment {

	int id; // �Զ�����
	int startStation;
	int endStation;
	double length; // ·�εĳ���

	// ������ͬ��γ�ȣ���ͬcellid�Ļ�վ
	ArrayList<CellStation> starts;
	ArrayList<CellStation> ends;

	private double avgSpeed;
	private double filterAvgSpeed;
	ArrayList<Double> speeds = new ArrayList<Double>();

	/**
	 * 
	 */
	public RoadSegment() {
		// TODO Auto-generated constructor stub
	}

	public RoadSegment(int id, ArrayList<CellStation> starts, double length) {
		this.id = id;
		this.starts = starts;
		this.length = length;
	}

	/**
	 * @param id
	 * @param startStation
	 * @param endStation
	 * @param direction
	 */
	public RoadSegment(int id, ArrayList<CellStation> starts,
			ArrayList<CellStation> ends) {
		this.id = id;
		this.starts = starts;
		this.ends = ends;
	}

	public void addSpeed(double speed) {
		speeds.add(speed);
	}

	public void clear() {
		this.speeds.clear();
	}

	public boolean contains(int cellid) {
		boolean contains = false;
		for (int i = 0; i < starts.size(); i++) {
			if (starts.get(i).getCellId() == cellid) {
				contains = true;
				break;
			}
		}
		return contains;
	}

	public void computeAvgSpeed() {
		if (speeds.size() == 0) {
			return;
		}
		double sum = 0;

		for (int i = 0; i < speeds.size(); i++) {
			sum += speeds.get(i);
		}
		this.avgSpeed = sum / speeds.size();
	}

	public void computeFilterAvgSpeed() {
		if (speeds.size() == 0)
			return;
		List<Double> qualifiedSpeeds = this.getQualifiedData();
		double sum = 0;
		for (int i = 0; i < qualifiedSpeeds.size(); i++) {
			sum += qualifiedSpeeds.get(i);
		}
		if (qualifiedSpeeds.size() > 0)
			this.filterAvgSpeed = sum / qualifiedSpeeds.size();
	}

	public double getAvgSpeed() {
		return this.avgSpeed;
	}

	public double getFilterAvgSpeed() {
		return this.filterAvgSpeed;
	}

	/**
	 * @Title: getQualifiedData
	 * @Description: ������̬�ֲ����޳���Ч����
	 * @param speed
	 * @param aveSpeed
	 * @return
	 */
	public List<Double> getQualifiedData() {
		List<Double> qualifiedSpeeds = new ArrayList<Double>();
		double variance = variance(speeds, avgSpeed);
		double svar = Math.sqrt(variance);
		for (int i = 0; i < speeds.size(); i++) {
			if (Math.abs(speeds.get(i) - avgSpeed) <= 2 * svar) {
				qualifiedSpeeds.add(speeds.get(i));
			} else {
				DataLogger.getLogger().info(
						"===> Filter one speed : avg = " + this.avgSpeed
								+ " , speed = " + speeds.get(i)
								+ " , variance = " + variance);
			}
		}
		return qualifiedSpeeds;
	}

	/**
	 * @Title: variance
	 * @Description: ���㷽��
	 * @param speed
	 * @param aveSpeed
	 * @return
	 */
	public double variance(List<Double> speed, double aveSpeed) {
		double sum = 0;
		for (int i = 0; i < speed.size(); i++)
			sum += Math.pow((speed.get(i) - aveSpeed), 2);
		sum = sum / speed.size();
		return sum;
	}
}
