package com.siat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName SpeedAlgorithm
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月16日 下午3:12:33
 */
public class SpeedAlgorithm {

	public static void main(String[] args) {
		SpeedAlgorithm sa = new SpeedAlgorithm(Configuration.START_TIME);
		sa.getUserData(60);
	}

	private DBService db = null;
	private String startTimeStr = null;
	private ArrayList<UserData> userDataPool = null;

	private int cachedTime = 0;
	private CellStation nowCs = null;

	private CellStation lastCs = null;
	private ArrayList<RoadSegment> forwardRS;
	private ArrayList<RoadSegment> reverseRS;

	private SimpleDateFormat sdf = null;

	private boolean direction;

	/**
	 * 
	 */
	public SpeedAlgorithm(String startTimeStr) {
		// TODO Auto-generated constructor stub
		db = new DBService();
		this.startTimeStr = startTimeStr;
		this.sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	}

	public void computeAvgSpeed(int intervalTime) {
		// 1. 从数据库中根据指定时间间隔批量得到一批数据
		ArrayList<UserData> nowData = this.getUserData(intervalTime);
		if (userDataPool == null) {
			// 第一批数据，初始化userDataPool
			userDataPool = nowData;
			return;
		}

		for (int i = 0; i < nowData.size(); i++) {
			// 2. 用userDataPool暂存上一批的数据，将最新一批的数据与上一批数据比较，
			// 如果车辆id匹配，则得出时间距离差，算出每一辆车的平均速度，然后更新数据；
			// 如果没有匹配车辆，则将新车辆直接插入userDataPool中。
			UserData nowUd = nowData.get(i);
			double speed = this.computeOneSpeed(nowUd);
			if (speed == -1)
				continue;

			// 3. 判断车辆行驶方向，并将计算出的车辆速度补足到经过的每一个路段区间中
			boolean isForward = this.direction;
			if (isForward) {
				for (int j = lastCs.getCellId(); j <= nowCs.getCellId(); j++) {

				}
			} else {

			}
		}

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
			UserData lastUd = userDataPool.get(index);
			double time = this.getIntervalTime(nowUd, lastUd);
			double distance = this.getDistance(nowUd, lastUd);
			direction = this.getDirection(nowUd, lastUd);
			speed = distance / time;
			if (speed < 0 || speed > 150) {
				// System.out.println("illegal speed : " + speed);
			}
			userDataPool.set(index, nowUd);

			nowCs = new CellStation(nowUd.getCellid());
			lastCs = new CellStation(lastUd.getCellid());
		} else {
			// 没有车辆id匹配
			userDataPool.add(nowUd);
		}
		return speed;
	}

	public void computeRoadSpeed(List<RoadSegment> list) {
		for (int i = 0; i < list.size(); i++) {
			RoadSegment rs = list.get(i);
			rs.computeAvgSpeed();
			rs.computeFilterAvgSpeed();
			// 清空speed表，以便下次使用
			rs.clear();
		}
	}

	public void dumpRoadSpeeds() {
		this.dumpRoadSpeeds(forwardRS, true);
		this.dumpRoadSpeeds(reverseRS, false);
	}

	public void dumpRoadSpeeds(List<RoadSegment> list, boolean forward) {
		if (forward)
			System.out.println("========= Forward ========>>>>>");
		else
			System.out.println("<<<<<==== Reverse ============");

		for (int i = 0; i < list.size(); i++) {
			RoadSegment rs = list.get(i);
			System.out.println("@ " + rs.id + " --- " + rs.getAvgSpeed()
					+ " & " + rs.getFilterAvgSpeed());
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
		boolean forward = true;
		if (nowUd.getCellid() == lastUd.getCellid()) {
			System.out.println("~~~~~~~ Can't determined the direction @ "
					+ nowUd.getCellid() + " " + nowUd.getTimestamp() + " & "
					+ lastUd.getCellid() + " " + lastUd.getTimestamp());
		}
		if (nowUd.getCellid() < lastUd.getCellid())
			forward = false;
		return forward;
	}

	public double getDistance(UserData nowUd, UserData lastUd) {
		double distance = 10;

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
		// System.out.println(userDatas);
		// System.out.println(userDatas.size());

		this.startTimeStr = endStr;
		return userDatas;
	}

	public void initialRoadSegments() {
		this.forwardRS = new ArrayList<>();
		this.reverseRS = new ArrayList<>();
	}
}
