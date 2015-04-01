package com.siat.ds;

import java.util.ArrayList;
import java.util.List;

import com.siat.msg.util.DataLogger;

/**
 * @ClassName RoadSegment
 * @Description 基站与基站之间的路段 (Station and Station)
 * @author Zhu Yingtao
 * @date 2014年12月16日 下午2:54:15
 */
public class StationSegment {

	public int id; // 自定义编号
	public int startStation;
	public int endStation;

	private int direction;
	List<Integer> startIds;
	List<Integer> endIds;

	private double length; // 路段的长度
	// 存在相同经纬度，不同cellid的基站
	ArrayList<Station> starts;
	ArrayList<Station> ends;

	private int avgSpeed = 80; // set default is 80;
	private int filterAvgSpeed = 80;
	private int maxSpeed = -1;
	private int minSpeed = 100;
	private int realNum = 0; // the real number in this station segment;
	private int expectedNum = 0; // the expected number in this station segment;
	// 保存一批次数据中当前路段上每一辆车的速度
	private List<Integer> speeds = new ArrayList<Integer>();

	// 保存不同批次数据计算出来的平均速度（时间+速度）
	private List<String> avgSpeedStrs = new ArrayList<String>();

	public StationSegment(int id, double length, List<Integer> startIds,
			List<Integer> endIds, int direction) {
		this.id = id;
		this.setLength(length);
		this.startIds = startIds;
		this.endIds = endIds;
		this.setDirection(direction);
	}

	public StationSegment(int id, ArrayList<Station> starts,
			ArrayList<Station> ends) {
		this.id = id;
		this.starts = starts;
		this.ends = ends;
	}

	public StationSegment(int id, ArrayList<Station> starts, double length) {
		this.id = id;
		this.starts = starts;
		this.setLength(length);
	}

	/**
	 * @Title: initStarts
	 * @Description: After select data from database, the StationSegment and
	 *               Station are still unlinked (because the StationSegment only
	 *               selected the Station id, not object), this method is used
	 *               to linked them;
	 * @param stations
	 */
	public void initStarts(List<Station> stations) {
		this.starts = new ArrayList<>();
		for (int i = 0; i < this.startIds.size(); i++) {
			int id = startIds.get(i);
			for (int j = 0; j < stations.size(); j++) {
				if (stations.get(j).getCellId() == id) {
					this.starts.add(stations.get(j));
					break;
				}
			}
		}
	}

	public void initEnds(List<Station> stations) {
		this.ends = new ArrayList<>();
		for (int i = 0; i < this.endIds.size(); i++) {
			int id = endIds.get(i);
			for (int j = 0; j < stations.size(); j++) {
				if (stations.get(j).getCellId() == id) {
					this.ends.add(stations.get(j));
					break;
				}
			}
		}
	}

	public void addReal() {
		this.realNum++;
	}

	public void addExpected() {
		this.expectedNum++;
	}

	public void addSpeed(int speed) {
		this.speeds.add(speed);
		if (maxSpeed < speed)
			maxSpeed = speed;
		if (minSpeed > speed)
			minSpeed = speed;
	}

	public void clearSpeeds() {
		this.speeds.clear();
		this.realNum = 0;
		this.expectedNum = 0;
	}

	public void computeAvgSpeed() {
		// if this batch is empty, then just return and maintain the last
		// average speed of this segment;
		if (this.speeds.size() == 0)
			return;

		double sum = 0;
		for (int i = 0; i < this.speeds.size(); i++) {
			sum += this.speeds.get(i);
		}
		this.avgSpeed = (int) (sum / speeds.size());
	}

	public void computeFilterAvgSpeed() {
		if (this.speeds.size() == 0)
			return;

		List<Integer> qualifiedSpeeds = this.getQualifiedData();
		double sum = 0;
		for (int i = 0; i < qualifiedSpeeds.size(); i++) {
			sum += qualifiedSpeeds.get(i);
		}
		if (qualifiedSpeeds.size() > 0)
			this.filterAvgSpeed = (int) (sum / qualifiedSpeeds.size());
	}

	/**
	 * @Title: contains
	 * @Description: determine whether the car belongs to this segment. Now we
	 *               just assert that if the cellId of the data is contained in
	 *               the startIds, then it belongs to this segment. This may be
	 *               INACCURATE.
	 * @param cellid
	 * @return
	 */
	public boolean contains(int cellid) {
		boolean contains = false;
		for (int i = 0; i < startIds.size(); i++) {
			if (startIds.get(i) == cellid) {
				contains = true;
				break;
			}
		}
		return contains;
	}

	public String dumpAvgSpeedStr() {
		StringBuffer sb = new StringBuffer();
		for (String str : this.avgSpeedStrs) {
			sb.append(str + "\n");
		}
		return sb.toString();
	}

	public void genAvgSpeedStr(String timeStr) {
		String str = timeStr + " --> speed = " + Math.round(this.avgSpeed)
				+ " & " + Math.round(this.filterAvgSpeed) + " , real = "
				+ this.realNum + " , expect = " + this.getSpeeds().size();
		this.avgSpeedStrs.add(str);
	}

	public int getAvgSpeed() {
		// if (this.expectedNum < 10)
		// return 80;
		return this.avgSpeed;
	}

	public List<String> getAvgSpeedStrs() {
		return avgSpeedStrs;
	}

	public int getFilterAvgSpeed() {
		// if (this.expectedNum < 10)
		// return 80;
		return this.filterAvgSpeed;
	}

	public int getMaxSpeed() {
		return this.maxSpeed;
	}

	public int getMinSpeed() {
		return this.minSpeed;
	}

	/**
	 * @Title: getQualifiedData
	 * @Description: 根据正态分布，剔除无效数据
	 * @param avgSpeed
	 * @param aveSpeed
	 * @return
	 */
	public List<Integer> getQualifiedData() {
		List<Integer> qualifiedSpeeds = new ArrayList<Integer>();
		double variance = variance(speeds, avgSpeed);
		double svar = Math.sqrt(variance);
		for (int i = 0; i < speeds.size(); i++) {
			if (Math.abs(speeds.get(i) - avgSpeed) <= 3 * svar) {
				qualifiedSpeeds.add(speeds.get(i));
			} else {
				DataLogger.getLogger().fine(
						"===> Filter one speed : avg = " + this.avgSpeed
								+ " , speed = " + speeds.get(i)
								+ " , variance = " + variance);
			}
		}
		return qualifiedSpeeds;
	}

	public int getRealNum() {
		return this.realNum;
	}

	public int getExpectedNum() {
		return this.expectedNum;
	}

	public List<Integer> getSpeeds() {
		return speeds;
	}

	public String getStarts() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < starts.size(); i++) {
			sb.append(starts.get(i).getCellId() + ",");
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	public void setAvgSpeedStrs(List<String> avgSpeedStrs) {
		this.avgSpeedStrs = avgSpeedStrs;
	}

	/**
	 * @Title: variance
	 * @Description: 计算方差
	 * @param speed
	 * @param aveSpeed
	 * @return
	 */
	public double variance(List<Integer> speed, double aveSpeed) {
		double sum = 0;
		for (int i = 0; i < speed.size(); i++)
			sum += Math.pow((speed.get(i) - aveSpeed), 2);
		sum = sum / speed.size();
		return sum;
	}

	/**
	 * @return the direction
	 */
	public int getDirection() {
		return direction;
	}

	/**
	 * @param direction
	 *            the direction to set
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}

	/**
	 * @return the length
	 */
	public double getLength() {
		return length;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(double length) {
		this.length = length;
	}
}
