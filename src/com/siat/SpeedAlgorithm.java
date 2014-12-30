package com.siat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * @ClassName SpeedAlgorithm
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014��12��16�� ����3:12:33
 */
public class SpeedAlgorithm {

	private DBService db = null;
	private String startTimeStr = null;
	private ArrayList<UserData> userDataPool = null;

	private int cachedTime = 0;

	private int nowRSid = 0;
	private int lastRSid = 0;
	private ArrayList<RoadSegment> forwardRS;
	private ArrayList<RoadSegment> reverseRS;

	private SimpleDateFormat sdf = null;
	private DecimalFormat df = null;

	private boolean direction;

	private Logger logger = null;

	private int matchesNum = 0;
	private int newNum = 0;
	private int sameNum = 0;

	public SpeedAlgorithm(String startTimeStr) {
		// TODO Auto-generated constructor stub
		this.db = new DBService();
		this.startTimeStr = startTimeStr;
		this.sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		this.df = new DecimalFormat("0.0");
		this.logger = DataLogger.getLogger();
		this.initialRoadSegments();
	}

	public void computeAvgSpeed(int intervalTime) {
		// 1. �����ݿ��и���ָ��ʱ���������õ�һ������
		ArrayList<UserData> nowData = this.getUserData(intervalTime);
		if (userDataPool == null) {
			// ��һ�����ݣ���ʼ��userDataPool
			userDataPool = nowData;
			return;
		}
		logger.info("data size : user_data_pool -> " + userDataPool.size()
				+ " , now_data -> " + nowData.size());

		int computeNum = 0;
		for (int i = 0; i < nowData.size(); i++) {
			// 2. ��userDataPool�ݴ���һ�������ݣ�������һ������������һ�����ݱȽϣ�
			// �������idƥ�䣬��ó�ʱ��������ÿһ������ƽ���ٶȣ�Ȼ��������ݣ�
			// ���û��ƥ�䳵�������³���ֱ�Ӳ���userDataPool�С�
			UserData nowUd = nowData.get(i);
			double speed = this.computeOneSpeed(nowUd);
			if (speed == -1)
				continue;

			// 3. ��������ĳ����ٶȲ��㵽������ÿһ��·��������
			boolean isForward = this.direction;
			if (isForward) {
				// ------- < or <=
				for (int j = lastRSid; j <= nowRSid; j++) {
					forwardRS.get(j).addSpeed(speed);
				}
			} else {
				for (int j = nowRSid; j <= lastRSid; j++) {
					reverseRS.get(j).addSpeed(speed);
				}
			}
			computeNum++;
		}
		logger.info("========= in one batch : new_add = " + newNum
				+ " , matches = " + matchesNum + " , in_same_segment = "
				+ sameNum);
		logger.info("==========> compute [ " + computeNum + " ] speeds ");
		// ��������
		newNum = 0;
		matchesNum = 0;
		sameNum = 0;

		// 4. ����ÿһ��·�������ƽ���ٶȣ���˫�򣬲���ӡ���
		this.computeRoadSpeed(forwardRS);
		this.computeRoadSpeed(reverseRS);
		this.dumpRoadSpeeds();

		// 5. ά��UserDataPool,ȥ����ʱ��δ���ֵ����ݡ�
		if (cachedTime >= Configuration.CACHE_TIME) {
			filterUserDataPool();
			cachedTime = 0;
		}
		cachedTime += intervalTime;
	}

	public double computeOneSpeed(UserData nowUd) {
		double speed = -1;
		int index = userDataPool.indexOf(nowUd);
		// �г���idƥ��
		if (index >= 0) {
			matchesNum++;
			UserData lastUd = userDataPool.get(index);
			direction = this.getDirection(nowUd, lastUd);
			double distance = this.getDistance(direction);
			double time = this.getIntervalTime(nowUd, lastUd);
			// userDataPool����Ҫ��ǰ�������������ǰreturn�޷�����
			userDataPool.set(index, nowUd);
			if (distance == -1) {
				sameNum++;
				return -1;
			}

			speed = distance / time;
			if (speed < 0 || speed > 150) {
				logger.warning("illegal speed : d = " + distance + ", t = "
						+ time + ", s = " + speed);
			}

		} else {
			// û�г���idƥ��
			newNum++;
			userDataPool.add(nowUd);
		}
		return speed;
	}

	public void computeRoadSpeed(List<RoadSegment> list) {
		for (int i = 0; i < list.size(); i++) {
			RoadSegment rs = list.get(i);
			rs.computeAvgSpeed();
			rs.computeFilterAvgSpeed();
		}
	}

	public void dumpRoadSpeeds() {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter("speeds.txt", true));
			bw.write("=======Time : " + startTimeStr + " =====\n");
			for (int i = 0; i < forwardRS.size(); i++) {
				StringBuffer str = new StringBuffer();
				RoadSegment rs = forwardRS.get(i);
				str.append("@" + rs.id + " --- ");
				str.append(df.format(rs.getAvgSpeed()) + " & "
						+ df.format(rs.getFilterAvgSpeed()));
				// ���speed���Ա��´�ʹ��
				rs.clear();
				rs = reverseRS.get(i);
				str.append("\t||\t" + df.format(rs.getAvgSpeed()) + " & "
						+ df.format(rs.getFilterAvgSpeed()) + "\n");
				bw.write(str.toString());
				// ���speed���Ա��´�ʹ��
				rs.clear();
			}

			bw.write("\n\n");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void filterUserDataPool() {
		Date nowDate = null;
		try {
			nowDate = sdf.parse(startTimeStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < userDataPool.size(); i++) {
			UserData ud = userDataPool.get(i);
			Date udDate = ud.getTimestamp();
			double interval = Math.abs(nowDate.getTime() - udDate.getTime()) / 1000;
			if (interval > Configuration.CACHE_TIME) {
				userDataPool.remove(i);
				System.out.println("remove dead data --> " + ud.getTmsi()
						+ " @ " + nowDate + " - " + udDate);
			}
		}
	}

	public boolean getDirection(UserData nowUd, UserData lastUd) {
		int nowCellid = nowUd.getCellid();
		int lastCellid = lastUd.getCellid();
		nowRSid = -1;
		lastRSid = -1;
		for (int i = 0; i < forwardRS.size(); i++) {
			RoadSegment rs = forwardRS.get(i);
			// һ��·�ο��ܰ��������λ����ͬ���Ļ�վ
			if (rs.contains(nowCellid))
				nowRSid = i;
			if (rs.contains(lastCellid))
				lastRSid = i;
			// ����λ�ö����ҵ���ֱ������
			if (nowRSid != -1 && lastRSid != -1)
				break;
		}
		if (nowRSid == -1 || lastRSid == -1)
			logger.severe("===== road segment fault ! " + nowRSid + "\t"
					+ lastRSid + "\t" + nowCellid + "\t" + lastCellid);
		if (nowRSid > lastRSid)
			return true;
		else
			return false;
	}

	public double getDistance(boolean direction) {
		double distance = 0;
		// �������ݴ���ͬһ·�Σ��޷�������룬����-1
		if (nowRSid == lastRSid)
			return -1;
		// ֻ���վ���վ֮��ľ��룬ƫС
		if (direction == true) {
			for (int i = lastRSid; i < nowRSid; i++) {
				distance += forwardRS.get(i).length;
			}
		} else {
			for (int i = nowRSid; i < lastRSid; i++) {
				distance += reverseRS.get(i).length;
			}
		}
		return distance;
	}

	public double getIntervalTime(UserData nowUd, UserData lastUd) {
		return (nowUd.getTimestamp().getTime() - lastUd.getTimestamp()
				.getTime()) / (1000.0 * 60 * 60);
	}

	/**
	 * @Title: getUserData
	 * @Description: �����ݿ��л�ȡһ������
	 * @param intervalTime
	 */
	public ArrayList<UserData> getUserData(int intervalTime) {
		Date start = null;
		Date end = null;
		try {
			start = sdf.parse(startTimeStr);
			end = new Date(start.getTime() + 1000 * intervalTime);// ���1����
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String startStr = sdf.format(start);
		String endStr = sdf.format(end);
		ArrayList<UserData> userDatas = db.selectUserData(startStr, endStr);

		this.startTimeStr = endStr;
		return userDatas;
	}

	public void initialRoadSegments() {
		// now, just read info from file;
		this.forwardRS = new CellStationInfo().readFromFile("data/·��.txt");
		this.reverseRS = new CellStationInfo().readFromFile("data/·��.txt");
	}
}
