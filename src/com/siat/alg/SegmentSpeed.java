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
 * @date 2014��12��16�� ����3:12:33
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

	private boolean isForward; // �������������ʻ

	private Logger logger = null;

	private int newNum = 0; // ����ӽ����ݳص���Ŀ
	private int matchesNum = 0; // �����ݳ�������ƥ�����Ŀ
	private int sameNum = 0; // �����ݳ������ݴ���ͬһ·����Ŀ
	private int computeNum = 0; // �����ݳ������ݲ�����ͬһ·����Ŀ

	public SegmentSpeed(String startTimeStr) {
		// TODO Auto-generated constructor stub
		this.db = new DBService();
		this.startTimeStr = startTimeStr;
		this.sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		this.logger = DataLogger.getLogger();
		this.initialRoadSegments();
		this.initialRoadSections();
	}

	// Logger������������
	public void clearLogger() {
		newNum = 0;
		matchesNum = 0;
		sameNum = 0;
		computeNum = 0;
	}

	public void computeAvgSpeed(int intervalTime) {
		// 1. �����ݿ��и���ָ��ʱ���������õ�һ������
		ArrayList<UserData> nowData = this.getUserData(intervalTime);
		// ��һ�����ݣ���ʼ��userDataPool��Ȼ�󷵻�
		if (userDataPool == null) {
			userDataPool = nowData;
			return;
		}
		logger.info("data size : user_data_pool -> " + userDataPool.size()
				+ " , now_data -> " + nowData.size());

		// 2. ��userDataPool�ݴ���һ�������ݣ�������һ������������һ�����ݱȽ�
		for (int i = 0; i < nowData.size(); i++) {
			// �������idƥ�䣬��ó�ʱ��������ÿһ������ƽ���ٶȣ�Ȼ�����userDataPool�е�����;
			// ���û��ƥ�䳵�������³���ֱ�Ӳ���userDataPool�С�
			UserData nowUd = nowData.get(i);
			int speed = (int) this.computeOneSpeed(nowUd);
			if (speed == -1)
				continue;

			// 3. ��������ĳ����ٶȲ��㵽������ÿһ��·��������
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

		// 4. ����ÿһ��·�������ƽ���ٶȣ���˫�򣬲���ӡ���
		this.computeRoadSpeed(forwardRS);
		this.computeRoadSpeed(reverseRS);
		// ������ÿһ�����������ƽ���ٶȣ���˫�򣬲���ӡ���
		this.computeSectionSpeed(forwardRSe);
		this.computeSectionSpeed(reverseRSe);
		this.dumpRoadSpeeds();
		this.storeData();

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
			isForward = this.getDirection(nowUd, lastUd);
			double distance = this.getDistance(isForward);
			double time = this.getIntervalTime(nowUd, lastUd);
			// userDataPool����Ҫ��ǰ�������������ǰreturn�޷�����
			userDataPool.set(index, nowUd);
			if (distance == -1) {
				sameNum++;
				return -1;
			}

			speed = distance / time;
			// �ٶȳ���������Χ������������-1
			if (speed < 0 || speed > 150) {
				logger.warning("illegal speed : d = " + distance + ", t = "
						+ time + ", s = " + speed + "\t , " + nowUd.getCellid()
						+ "\t" + lastUd.getCellid());
				speed = -1;
			}
			// û�г���idƥ��
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
				// һ��·�ο��ܰ��������λ����ͬ���Ļ�վ
				if (rse.contains(start))
					startIndex = j;
				if (rse.contains(end))
					endIndex = j;
				// ����λ�ö����ҵ���ֱ������
				if (startIndex != -1 && endIndex != -1)
					break;
			}
			if (direction == 1) {
				// forward����
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
				// reverse����
				for (int j = startIndex; j <= endIndex; j++) {
					speed += reverseRS.get(j).getAvgSpeed();
					speedNum += reverseRS.get(j).getRealNum();
					count++;
				}
			}
			// �˴�ֻ���ƽ���ٶȣ���ʱû�м�Ȩ
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

			// ��ʱ�������������·���ٶ���Ϣ
			bw.write("=======Time : " + startTimeStr + " =====\n");
			for (int i = 0; i < forwardRS.size(); i++) {
				StringBuffer str = new StringBuffer();
				RoadSegment rs = forwardRS.get(i);
				str.append("@" + rs.id + " --- ");
				str.append(Math.round(rs.getAvgSpeed()) + " & "
						+ Math.round(rs.getFilterAvgSpeed()));
				// ���speed���Ա��´�ʹ��
				rs.clear();
				rs = reverseRS.get(i);
				str.append("\t||\t" + Math.round(rs.getAvgSpeed()) + " & "
						+ Math.round(rs.getFilterAvgSpeed()) + "\n");
				bw.write(str.toString());
				// ���speed���Ա��´�ʹ��
				rs.clear();
			}

			// ��·��������������ٶ���Ϣ
			// for (int i = 0; i < forwardRS.size(); i++) {
			// RoadSegment rs = forwardRS.get(i);
			// String str = rs.dumpAvgSpeedStr();
			// bw.write("============= " + rs.id + " ================\n");
			// bw.write(str + "\n");
			// rs = reverseRS.get(i);
			// str = rs.dumpAvgSpeedStr();
			// bw.write(str + "\n");
			// }

			// ��������ٶ���Ϣ
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

	public ArrayList<RoadSegment> getRoadSegmentList(boolean isForward) {
		if (isForward)
			return this.forwardRS;
		else
			return this.reverseRS;
	}

	/**
	 * @Title: getUserData
	 * @Description: �����ݿ��л�ȡһ��������
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
		// ��ʱ����:��������·�ζ�����ȫһ��(��0��n);
		this.forwardRS = RoadSegment.readFromFile("data/·��.txt");
		this.reverseRS = RoadSegment.readFromFile("data/·��.txt");
	}

	public void storeData() {
		db.insertSectionSpeeds(forwardRSe, startTimeStr);
		db.insertSectionSpeeds(reverseRSe, startTimeStr);
	}
}
