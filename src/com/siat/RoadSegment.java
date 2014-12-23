package com.siat;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName RoadSegment
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月16日 下午2:54:15
 */
public class RoadSegment {

	int id;
	int startStation;
	int endStation;

	CellStation start;
	CellStation end;

	private double avgSpeed;
	private double filterAvgSpeed;
	ArrayList<Double> speeds;

	/**
	 * 
	 */
	public RoadSegment() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param id
	 * @param startStation
	 * @param endStation
	 * @param direction
	 */
	public RoadSegment(int id, int startStation, int endStation) {
		this.id = id;
		this.startStation = startStation;
		this.endStation = endStation;
		this.speeds = new ArrayList<>();
	}

	public void addSpeed(double speed) {
		speeds.add(speed);
	}

	public void clear() {
		this.avgSpeed = 0;
		this.speeds.clear();
	}

	public void computeAvgSpeed() {
		double sum = 0;
		for (int i = 0; i < speeds.size(); i++) {
			sum += speeds.get(i);
		}
		this.avgSpeed = sum / speeds.size();
	}

	public void computeFilterAvgSpeed() {
		if (this.avgSpeed == 0)
			this.computeAvgSpeed();
		List<Double> qualifiedSpeeds = this.getQualifiedData();
		double sum = 0;
		for (int i = 0; i < qualifiedSpeeds.size(); i++) {
			sum += qualifiedSpeeds.get(i);
		}
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
	 * @Description: 根据正态分布，剔除无效数据
	 * @param speed
	 * @param aveSpeed
	 * @return
	 */
	public List<Double> getQualifiedData() {
		List<Double> qualifiedSpeeds = new ArrayList<Double>();
		double variance = variance(speeds, avgSpeed);
		for (int i = 0; i < speeds.size(); i++) {
			if (Math.abs(speeds.get(i) - avgSpeed) < 3 * variance) {
				qualifiedSpeeds.add(speeds.get(i));
			} else {
				System.out.println("===> Filter one speed : " + speeds.get(i));
			}
		}
		return qualifiedSpeeds;
	}

	/**
	 * @Title: variance
	 * @Description: 计算方差
	 * @param speed
	 * @param aveSpeed
	 * @return
	 */
	public double variance(List<Double> speed, double aveSpeed) {
		double sum = 0;
		for (int i = 0; i < speed.size(); i++)
			sum += (speed.get(i) - aveSpeed) * (speed.get(i) - aveSpeed);
		sum /= speed.size();
		System.out.println(sum);
		return sum;
	}
}
