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
 * @date 2014��12��16�� ����3:12:33
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
		// 1. �����ݿ��и���ָ��ʱ���������õ�һ������
		ArrayList<UserData> nowData = this.getUserData(intervalTime);
		if (userDataPool == null) {
			// ��һ�����ݣ���ʼ��userDataPool
			userDataPool = nowData;
			return;
		}

		for (int i = 0; i < nowData.size(); i++) {
			// 2. ��userDataPool�ݴ���һ�������ݣ�������һ������������һ�����ݱȽϣ�
			// �������idƥ�䣬��ó�ʱ��������ÿһ������ƽ���ٶȣ�Ȼ��������ݣ�
			// ���û��ƥ�䳵�������³���ֱ�Ӳ���userDataPool�С�
			UserData nowUd = nowData.get(i);
			double speed = this.computeOneSpeed(nowUd);
			if (speed == -1)
				continue;

			// 3. �жϳ�����ʻ���򣬲���������ĳ����ٶȲ��㵽������ÿһ��·��������
			boolean isForward = this.direction;
			if (isForward) {
				for (int j = lastCs.getCellId(); j <= nowCs.getCellId(); j++) {

				}
			} else {

			}
		}

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
			// û�г���idƥ��
			userDataPool.add(nowUd);
		}
		return speed;
	}

	public void computeRoadSpeed(List<RoadSegment> list) {
		for (int i = 0; i < list.size(); i++) {
			RoadSegment rs = list.get(i);
			rs.computeAvgSpeed();
			rs.computeFilterAvgSpeed();
			// ���speed���Ա��´�ʹ��
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
