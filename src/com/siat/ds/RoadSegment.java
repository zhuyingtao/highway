package com.siat.ds;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.siat.util.DataLogger;

/**
 * @ClassName RoadSegment
 * @Description ��վ���վ֮���·�� (CellStation and CellStation)
 * @author Zhu Yingtao
 * @date 2014��12��16�� ����2:54:15
 */
public class RoadSegment {

	// ���ļ����ȡ��վ���վ֮���·����Ϣ
	@SuppressWarnings("unused")
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

	public int id; // �Զ�����
	public int startStation;
	public int endStation;

	public double length; // ·�εĳ���
	// ������ͬ��γ�ȣ���ͬcellid�Ļ�վ
	ArrayList<CellStation> starts;

	ArrayList<CellStation> ends;
	private int avgSpeed;
	private int filterAvgSpeed;
	private int maxSpeed;

	private int minSpeed = 100;
	private int realNum; // �ڵ�ǰ·���ϵĳ�����
	// ����һ���������е�ǰ·����ÿһ�������ٶ�
	private List<Integer> speeds = new ArrayList<Integer>();

	// ���治ͬ�������ݼ��������ƽ���ٶȣ�ʱ��+�ٶȣ�
	private List<String> avgSpeedStrs = new ArrayList<String>();

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

	public void addSpeed(int speed) {
		this.speeds.add(speed);
		if (maxSpeed < speed)
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
		if (this.speeds.size() == 0)
			return;

		double sum = 0;
		for (int i = 0; i < getSpeeds().size(); i++) {
			sum += getSpeeds().get(i);
		}
		this.avgSpeed = (int) (sum / speeds.size());
	}

	public void computeFilterAvgSpeed() {
		if (getSpeeds().size() == 0)
			return;
		List<Integer> qualifiedSpeeds = this.getQualifiedData();
		double sum = 0;
		for (int i = 0; i < qualifiedSpeeds.size(); i++) {
			sum += qualifiedSpeeds.get(i);
		}
		if (qualifiedSpeeds.size() > 0)
			this.filterAvgSpeed = (int) (sum / qualifiedSpeeds.size());
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
				+ this.realNum + " , expect = " + this.getSpeeds().size();
		this.avgSpeedStrs.add(str);
	}

	public int getAvgSpeed() {
		return this.avgSpeed;
	}

	public List<String> getAvgSpeedStrs() {
		return avgSpeedStrs;
	}

	public int getFilterAvgSpeed() {
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
	 * @Description: ������̬�ֲ����޳���Ч����
	 * @param avgSpeed
	 * @param aveSpeed
	 * @return
	 */
	public List<Integer> getQualifiedData() {
		List<Integer> qualifiedSpeeds = new ArrayList<Integer>();
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

	public int getRealNum() {
		return this.realNum;
	}

	public List<Integer> getSpeeds() {
		return speeds;
	}

	public void setAvgSpeedStrs(List<String> avgSpeedStrs) {
		this.avgSpeedStrs = avgSpeedStrs;
	}

	/**
	 * @Title: variance
	 * @Description: ���㷽��
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

	public static void main(String[] args) {

	}
}
