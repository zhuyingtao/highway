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
 * @date 2014年12月16日 下午3:12:33
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
		// 1. 从数据库中根据指定时间间隔批量得到一批数据
		ArrayList<UserData> nowData = this.getUserData(intervalTime);
		if (userDataPool == null) {
			// 第一批数据，初始化userDataPool
			userDataPool = nowData;
			return;
		}
		logger.info("data size : user_data_pool -> " + userDataPool.size()
				+ " , now_data -> " + nowData.size());

		int computeNum = 0;
		for (int i = 0; i < nowData.size(); i++) {
			// 2. 用userDataPool暂存上一批的数据，将最新一批的数据与上一批数据比较，
			// 如果车辆id匹配，则得出时间距离差，算出每一辆车的平均速度，然后更新数据；
			// 如果没有匹配车辆，则将新车辆直接插入userDataPool中。
			UserData nowUd = nowData.get(i);
			double speed = this.computeOneSpeed(nowUd);
			if (speed == -1)
				continue;

			// 3. 将计算出的车辆速度补足到经过的每一个路段区间中
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
		// 重置清零
		newNum = 0;
		matchesNum = 0;
		sameNum = 0;

		// 4. 计算每一个路段区间的平均速度，分双向，并打印输出
		this.computeRoadSpeed(forwardRS);
		this.computeRoadSpeed(reverseRS);
		this.dumpRoadSpeeds();

		// 5. 维护UserDataPool,去除长时间未出现的数据。
		if (cachedTime >= Configuration.CACHE_TIME) {
			filterUserDataPool();
			cachedTime = 0;
		}
		cachedTime += intervalTime;
	}

	public double computeOneSpeed(UserData nowUd) {
		double speed = -1;
		int index = userDataPool.indexOf(nowUd);
		// 有车辆id匹配
		if (index >= 0) {
			matchesNum++;
			UserData lastUd = userDataPool.get(index);
			direction = this.getDirection(nowUd, lastUd);
			double distance = this.getDistance(direction);
			double time = this.getIntervalTime(nowUd, lastUd);
			// userDataPool更新要提前，否则可能因提前return无法更新
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
			// 没有车辆id匹配
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
				// 清空speed表，以便下次使用
				rs.clear();
				rs = reverseRS.get(i);
				str.append("\t||\t" + df.format(rs.getAvgSpeed()) + " & "
						+ df.format(rs.getFilterAvgSpeed()) + "\n");
				bw.write(str.toString());
				// 清空speed表，以便下次使用
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
			// 一个路段可能包含多个（位置相同）的基站
			if (rs.contains(nowCellid))
				nowRSid = i;
			if (rs.contains(lastCellid))
				lastRSid = i;
			// 两个位置都已找到，直接跳出
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
		// 两次数据处于同一路段，无法计算距离，返回-1
		if (nowRSid == lastRSid)
			return -1;
		// 只算基站与基站之间的距离，偏小
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
	 * @Description: 从数据库中获取一批数据
	 * @param intervalTime
	 */
	public ArrayList<UserData> getUserData(int intervalTime) {
		Date start = null;
		Date end = null;
		try {
			start = sdf.parse(startTimeStr);
			end = new Date(start.getTime() + 1000 * intervalTime);// 间隔1分钟
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
		this.forwardRS = new CellStationInfo().readFromFile("data/路段.txt");
		this.reverseRS = new CellStationInfo().readFromFile("data/路段.txt");
	}
}
