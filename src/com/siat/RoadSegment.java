package com.siat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName RoadSegment
 * @Description ��վ���վ֮���·�� (CellStation and CellStation)
 * @author Zhu Yingtao
 * @date 2014��12��16�� ����2:54:15
 */
public class RoadSegment {

	// ���ļ����ȡ��վ���վ֮���·����Ϣ
	public static ArrayList<RoadSegment> readFromFile(String filePath) {
		ArrayList<RoadSegment> rss = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			br.readLine();
			String line = br.readLine();
			int id = 0;
			while (line != null) {
				String[] parts = line.split("\t");
				double xs = Double.parseDouble(parts[0]);
				double ys = Double.parseDouble(parts[1]);
				double xe = Double.parseDouble(parts[2]);
				double ye = Double.parseDouble(parts[3]);
				double length = Double.parseDouble(parts[4]);
				String cellidStr = parts[5];
				String lacidStr = parts[6];
				String[] cellids = cellidStr.split(",");
				ArrayList<CellStation> starts = new ArrayList<>();
				for (int i = 0; i < cellids.length; i++) {
					CellStation cs = new CellStation(
							Integer.parseInt(cellids[i]));
					starts.add(cs);
				}
				RoadSegment rs = new RoadSegment(id++, starts, length);
				rss.add(rs);
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rss;
	}

	int id; // �Զ�����
	int startStation;
	int endStation;

	double length; // ·�εĳ���
	// ������ͬ��γ�ȣ���ͬcellid�Ļ�վ
	ArrayList<CellStation> starts;

	ArrayList<CellStation> ends;
	private double avgSpeed;
	private double filterAvgSpeed;
	private double maxSpeed;

	private double minSpeed = 100;
	private int realNum; // �ڵ�ǰ·���ϵĳ�����
	// ����һ���������е�ǰ·����ÿһ�������ٶ�
	List<Double> speeds = new ArrayList<Double>();

	// ���治ͬ�������ݼ��������ƽ���ٶȣ�ʱ��+�ٶȣ�
	List<String> avgSpeedStrs = new ArrayList<String>();

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

	public RoadSegment(int id, ArrayList<CellStation> starts, double length) {
		this.id = id;
		this.starts = starts;
		this.length = length;
	}

	public void addReal() {
		this.realNum++;
	}

	public void addSpeed(double speed) {
		speeds.add(speed);
		if (maxSpeed < speed && maxSpeed < 150)
			maxSpeed = speed;
		if (minSpeed > speed)
			minSpeed = speed;
	}

	public void clear() {
		this.speeds.clear();
		realNum = 0;
	}

	public void computeAvgSpeed() {
		// ��������ε�����Ϊ�գ���ֱ�ӷ��أ�ʹ����һ�����ݵĽ��
		if (speeds.size() == 0)
			return;

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
				+ this.realNum + " , expect = " + this.speeds.size();
		this.avgSpeedStrs.add(str);
	}

	public int getAvgSpeed() {
		return (int) this.avgSpeed;
	}

	public int getFilterAvgSpeed() {
		return (int) this.filterAvgSpeed;
	}

	public int getMaxSpeed() {
		return (int) this.maxSpeed;
	}

	public int getMinSpeed() {
		return (int) this.minSpeed;
	}

	/**
	 * @Title: getQualifiedData
	 * @Description: ������̬�ֲ����޳���Ч����
	 * @param avgSpeed
	 * @param aveSpeed
	 * @return
	 */
	public List<Double> getQualifiedData() {
		List<Double> qualifiedSpeeds = new ArrayList<Double>();
		double variance = variance(speeds, avgSpeed);
		double svar = Math.sqrt(variance);
		for (int i = 0; i < speeds.size(); i++) {
			if (Math.abs(speeds.get(i) - avgSpeed) <= 3 * svar) {
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

	public int getRealNum() {
		return this.realNum;
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
