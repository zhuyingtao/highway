package com.siat.alg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.siat.Configuration;
import com.siat.db.DBService;
import com.siat.ds.RoadSection;
import com.siat.ds.RoadSegment;
import com.siat.ds.UserData;
import com.siat.util.DataLogger;

/**
 * @ClassName SpeedAlgorithm
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月16日 下午3:12:33
 */
public class SegmentSpeed {

	private DBService db = null;
	private String startTimeStr = null;
	private ArrayList<UserData> userDataPool = null;

	private int cachedTime = 0;

	private int nowRSid = 0;
	private int lastRSid = 0;
	private ArrayList<RoadSegment> forwardRS;
	private ArrayList<RoadSegment> reverseRS;

	private ArrayList<RoadSection> forwardRSe;
	private ArrayList<RoadSection> reverseRSe;

	private SimpleDateFormat sdf = null;

	private boolean isForward; // 正向或者逆向行驶

	private Logger logger = null;

	private int newNum = 0; // 新添加进数据池的数目
	private int matchesNum = 0; // 与数据池中数据匹配的数目
	private int sameNum = 0; // 与数据池中数据处于同一路段数目
	private int computeNum = 0; // 与数据池中数据不处于同一路段数目

	public SegmentSpeed(String startTimeStr) {
		// TODO Auto-generated constructor stub
		this.db = new DBService();
		this.startTimeStr = startTimeStr;
		this.sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		this.logger = DataLogger.getLogger();
		this.initialRoadSegments();
		this.initialRoadSections();
	}

	// Logger数据重置清零
	public void clearLogger() {
		newNum = 0;
		matchesNum = 0;
		sameNum = 0;
		computeNum = 0;
	}

	public void computeAvgSpeed(int intervalTime) {
		// 1. 从数据库中根据指定时间间隔批量得到一批数据
		ArrayList<UserData> nowData = this.getUserData(intervalTime);
		// 第一批数据，初始化userDataPool，然后返回
		if (userDataPool == null) {
			userDataPool = nowData;
			return;
		}
		logger.info("data size : user_data_pool -> " + userDataPool.size()
				+ " , now_data -> " + nowData.size());

		// 2. 用userDataPool暂存上一批的数据，将最新一批的数据与上一批数据比较
		for (int i = 0; i < nowData.size(); i++) {
			// 如果车辆id匹配，则得出时间距离差，算出每一辆车的平均速度，然后更新userDataPool中的数据;
			// 如果没有匹配车辆，则将新车辆直接插入userDataPool中。
			UserData nowUd = nowData.get(i);
			int speed = (int) this.computeOneSpeed(nowUd);
			if (speed == -1)
				continue;

			// 3. 将计算出的车辆速度补足到经过的每一个路段区间中
			int large = -1, small = -1;
			if (isForward) {
				small = lastRSid;
				large = nowRSid;
			} else {
				small = nowRSid;
				large = lastRSid;
			}
			// < or <= ?
			for (int j = small; j <= large; j++) {
				RoadSegment rs = isForward ? (forwardRS.get(j)) : (reverseRS
						.get(j));
				rs.addSpeed(speed);
				if (j == nowRSid)
					rs.addReal();
			}
			computeNum++;
		}
		logger.info("========= in one batch : new_add = " + newNum
				+ " , matches = " + matchesNum + " { in_same_segment = "
				+ sameNum + " , compute = " + computeNum + " }");
		this.clearLogger();

		// 4. 计算每一个路段区间的平均速度，分双向，并打印输出
		this.computeRoadSpeed(forwardRS);
		this.computeRoadSpeed(reverseRS);
		// 另：计算每一个服务区间的平均速度，分双向，并打印输出
		this.computeSectionSpeed(forwardRSe);
		this.computeSectionSpeed(reverseRSe);
		this.dumpRoadSpeeds();
		this.storeData();

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
			isForward = this.getDirection(nowUd, lastUd);
			double distance = this.getDistance(isForward);
			double time = this.getIntervalTime(nowUd, lastUd);
			// userDataPool更新要提前，否则可能因提前return无法更新
			userDataPool.set(index, nowUd);
			if (distance == -1) {
				sameNum++;
				return -1;
			}

			speed = distance / time;
			// 速度超出正常范围，则丢弃，返回-1
			if (speed < 0 || speed > 150) {
				logger.warning("illegal speed : d = " + distance + ", t = "
						+ time + ", s = " + speed + "\t , " + nowUd.getCellid()
						+ "\t" + lastUd.getCellid());
				speed = -1;
			}
			// 没有车辆id匹配
		} else {
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
			logger.info(" ***** " + rs.id + "\t speeds : "
					+ rs.getSpeeds().toString());
			logger.info(" &&&&& " + rs.id + "\t filterSpeeds : "
					+ rs.getQualifiedData().toString());
			rs.genAvgSpeedStr(startTimeStr);
		}
	}

	public void computeSectionSpeed(List<RoadSection> list) {
		for (int i = 0; i < list.size(); i++) {
			RoadSection rs = list.get(i);
			int start = rs.startNode.cellId;
			int end = rs.endNode.cellId;
			int direction = rs.direction;

			int speed = 0;
			int speedNum = 0;
			int count = 0;
			int maxSpeed = 0;
			int minSpeed = 1000;

			int startIndex = -1;
			int endIndex = -1;
			for (int j = 0; j < forwardRS.size(); j++) {
				RoadSegment rse = forwardRS.get(j);
				// 一个路段可能包含多个（位置相同）的基站
				if (rse.contains(start))
					startIndex = j;
				if (rse.contains(end))
					endIndex = j;
				// 两个位置都已找到，直接跳出
				if (startIndex != -1 && endIndex != -1)
					break;
			}
			if (direction == 1) {
				// forward方向
				for (int j = startIndex; j <= endIndex; j++) {
					RoadSegment segment = forwardRS.get(j);
					speed += segment.getAvgSpeed();
					speedNum += segment.getRealNum();
					int max = segment.getMaxSpeed();
					int min = segment.getMinSpeed();
					if (maxSpeed < max)
						maxSpeed = max;
					if (minSpeed > min)
						minSpeed = min;
					count++;
				}
			} else {
				// reverse方向
				for (int j = startIndex; j <= endIndex; j++) {
					speed += reverseRS.get(j).getAvgSpeed();
					speedNum += reverseRS.get(j).getRealNum();
					count++;
				}
			}
			// 此处只算的平均速度，暂时没有加权
			speed = speed / count;
			rs.avgSpeed = speed;
			rs.speedNum = speedNum;
			rs.maxSpeed = maxSpeed;
			rs.minSpeed = minSpeed;
		}
	}

	public void dumpRoadSpeeds() {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter("speeds.txt", true));

			// 按时间批次输出所有路段速度信息
			bw.write("=======Time : " + startTimeStr + " =====\n");
			for (int i = 0; i < forwardRS.size(); i++) {
				StringBuffer str = new StringBuffer();
				RoadSegment rs = forwardRS.get(i);
				str.append("@" + rs.id + " --- ");
				str.append(Math.round(rs.getAvgSpeed()) + " & "
						+ Math.round(rs.getFilterAvgSpeed()));
				// 清空speed表，以便下次使用
				rs.clear();
				rs = reverseRS.get(i);
				str.append("\t||\t" + Math.round(rs.getAvgSpeed()) + " & "
						+ Math.round(rs.getFilterAvgSpeed()) + "\n");
				bw.write(str.toString());
				// 清空speed表，以便下次使用
				rs.clear();
			}

			// 按路段输出所有批次速度信息
			// for (int i = 0; i < forwardRS.size(); i++) {
			// RoadSegment rs = forwardRS.get(i);
			// String str = rs.dumpAvgSpeedStr();
			// bw.write("============= " + rs.id + " ================\n");
			// bw.write(str + "\n");
			// rs = reverseRS.get(i);
			// str = rs.dumpAvgSpeedStr();
			// bw.write(str + "\n");
			// }

			// 输出区间速度信息
			for (int i = 0; i < forwardRSe.size(); i++) {
				StringBuffer str = new StringBuffer();
				RoadSection rs = forwardRSe.get(i);
				str.append("@" + rs.id + " : " + rs.startNode.roadNodeName
						+ " --- " + rs.endNode.roadNodeName + "\n");
				str.append("speed = " + Math.round(rs.avgSpeed) + " , num = "
						+ rs.speedNum);
				rs = reverseRSe.get(i);
				str.append("\t||\tspeed = " + Math.round(rs.avgSpeed)
						+ " , num = " + rs.speedNum + "\n");
				bw.write(str.toString());
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
		int removedNum = 0;
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
				removedNum++;
				System.out.println("remove dead data --> " + ud.getTmsi()
						+ " @ " + nowDate + " - " + udDate);
			}
		}
		logger.info(startTimeStr + "========== removed [" + removedNum
				+ "] dead car !");
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

	public ArrayList<RoadSegment> getRoadSegmentList(boolean isForward) {
		if (isForward)
			return this.forwardRS;
		else
			return this.reverseRS;
	}

	/**
	 * @Title: getUserData
	 * @Description: 从数据库中获取一批次数据
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

	public void initialRoadSections() {
		ArrayList<RoadSection> rss = RoadSection.initial();
		this.forwardRSe = new ArrayList<>();
		this.reverseRSe = new ArrayList<>();
		for (int i = 0; i < 24; i++) {
			forwardRSe.add(rss.get(i));
		}
		for (int i = 24; i < 48; i++) {
			reverseRSe.add(rss.get(i));
		}
	}

	public void initialRoadSegments() {
		// 暂时处理:正向逆向路段对象完全一样(从0到n);
		this.forwardRS = RoadSegment.readFromFile("data/路段.txt");
		this.reverseRS = RoadSegment.readFromFile("data/路段.txt");
	}

	public void storeData() {
		db.insertSectionSpeeds(forwardRSe, startTimeStr);
		db.insertSectionSpeeds(reverseRSe, startTimeStr);
	}
}
