package com.siat.msg.alg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.siat.ds.NodeSegment;
import com.siat.ds.StationSegment;
import com.siat.msg.Configuration;
import com.siat.msg.UserData;
import com.siat.msg.db.DBService;
import com.siat.msg.util.DataLogger;

/**
 * @ClassName SpeedAlgorithm
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014��12��16�� ����3:12:33
 */
public class SegmentSpeed {

	private DBService db = null;
	private String startTimeStr = null;
	private List<UserData> userDataPool = null;

	private int cachedTime = 0;

	private int nowStationId = 0;
	private int lastStationId = 0;

	// Lists for StationSegment and NodeSegment stored the computed speed and
	// some other information;
	private ArrayList<StationSegment> forwardStations;
	private ArrayList<StationSegment> reverseStations;
	private ArrayList<NodeSegment> forwardNodes;
	private ArrayList<NodeSegment> reverseNodes;

	private SimpleDateFormat sdf = null;

	private boolean isForward; // �������������ʻ

	private Logger logger = null;

	private int newNum = 0; // ����ӽ����ݳص���Ŀ
	private int matchesNum = 0; // �����ݳ�������ƥ�����Ŀ
	private int sameNum = 0; // �����ݳ������ݴ���ͬһ·����Ŀ
	private int computeNum = 0; // �����ݳ������ݲ�����ͬһ·����Ŀ

	// used for history speed, marked the index of one batch data;
	private int endIndex = 0;

	public SegmentSpeed(String startTimeStr) {
		// TODO Auto-generated constructor stub
		this.db = new DBService();
		this.startTimeStr = startTimeStr;
		this.sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		this.logger = DataLogger.getLogger();
		this.initialRoadSegments();
		this.initialRoadSections();
	}

	// clear the flags used for logger;
	public void clearFlags() {
		newNum = 0;
		matchesNum = 0;
		sameNum = 0;
		computeNum = 0;
	}

	public boolean checkUpdate() {
		boolean updated = false;
		return updated;
	}

	/**
	 * @Title: getOneBatchData
	 * @Description: get the data of one batch from the all data
	 * @param allData
	 * @param startIndex
	 * @return
	 */
	public List<UserData> getOneBatchData(List<UserData> allData, int startIndex) {
		UserData ud = allData.get(startIndex);
		List<UserData> batchData = new ArrayList<UserData>();
		// find the end index of one batch based on interval time;
		endIndex = startIndex;
		int count = 0; // count the distinct data num;
		while (endIndex < allData.size()) {
			endIndex++;
			UserData ud1 = allData.get(endIndex);
			if (ud1.getTimestamp().getTime() - ud.getTimestamp().getTime() > Configuration.INTERVAL_TIME * 1000) {
				break;
			}
			// at here we use the OLDEST DATA of the same user data; so the
			// speed may be SMALLER than before;
			int index = batchData.indexOf(ud1);
			if (index < 0) {
				batchData.add(ud1);
				count++;
			}
		}
		logger.info("all data size : " + allData.size() + ", one batch : "
				+ startIndex + ", " + endIndex + ", count = " + count);
		return batchData;
	}

	/**
	 * @Title: computeHistorySpeed
	 * @Description: TODO
	 * @param startTime
	 * @param endTime
	 * @param rate
	 */
	public void computeHistorySpeed(String startTime, String endTime, int rate) {
		// 1. get all the history data from databases into memory according to
		// the startTime and endTime;

		List<UserData> allData = this.getHistoryUserData(startTime, endTime);
		this.userDataPool = null;
		int startIndex = 0;
		int batchNum = 0;
		// 2. compute the average speed of every batch;
		logger.info("**** All batches begin to compute ... ");
		while (startIndex < allData.size()) {
			// before every computing, check whether the request has been
			// updated;
			boolean updated = this.checkUpdate();
			if (updated) {
				logger.info("New request has coming ... now return ");
				return;
			}
			// if not updated, then begin to compute;
			List<UserData> batchData = this
					.getOneBatchData(allData, startIndex);
			this.computeAvgSpeed(batchData);
			startIndex = endIndex;
			batchNum++;
		}
		logger.info("**** All batches have finished ... count : " + batchNum);
	}

	/**
	 * @Title: computeAvgSpeed
	 * @Description: compute the average speed of every batch;
	 * @param batchData
	 */
	public void computeAvgSpeed(List<UserData> batchData) {
		// if it is the first batch : initial the userDataPool, then return.
		// useDataPool is a list that contains the UserData of last batchData or
		// unused; it will be refreshed in a specific time to keep size.
		if (userDataPool == null) {
			userDataPool = batchData;
			return;
		}

		logger.info("data size : user_data_pool -> " + userDataPool.size()
				+ " , batch_data -> " + batchData.size());

		// compute every UserData in the batchData with UserDataPool;
		for (int i = 0; i < batchData.size(); i++) {
			// if the car data matches, then compute the interval time and
			// distance, then compute the speed of this car, and update the data
			// of this car in the UserDataPool;
			// if not, then just insert the car data into the UserDataPool;
			int speed = -1;
			UserData nowUd = batchData.get(i);
			int index = userDataPool.indexOf(nowUd);
			if (index >= 0) {
				matchesNum++;
				UserData lastUd = userDataPool.get(index);
				speed = (int) this.computeOneSpeed(nowUd, lastUd);
				userDataPool.set(index, nowUd);
			} else {
				newNum++;
				userDataPool.add(nowUd);
			}
			// if not matches or the speed is illegal,then next data;
			if (speed == -1)
				continue;

			// add the speed into every Station Segment that related;
			this.addSpeed(speed);

		}
		logger.info("========= in one batch : new_add = " + newNum
				+ " , matches = " + matchesNum + " { in_same_segment = "
				+ sameNum + " , compute = " + computeNum + " }");
		this.clearFlags();

		// compute every Station Segment's speed;
		this.computeStationSpeed(forwardStations);
		this.computeStationSpeed(reverseStations);
		// compute every Node Segment's speed;
		this.computeSectionSpeed(forwardNodes);
		this.computeSectionSpeed(reverseNodes);
		// dump result into text;
		this.dumpRoadSpeeds();
		// store result into database;
		// this.storeData();

		// 5. ά��UserDataPool,ȥ����ʱ��δ���ֵ����ݡ�
		if (cachedTime >= Configuration.CACHE_TIME) {
			filterUserDataPool();
			cachedTime = 0;
		}
		cachedTime += Configuration.INTERVAL_TIME;
	}

	public void addSpeed(int speed) {
		// determine the start and the end;
		int large = -1, small = -1;
		if (isForward) {
			small = lastStationId;
			large = nowStationId;
		} else {
			small = nowStationId;
			large = lastStationId;
		}
		// < or <= ?
		for (int j = small; j <= large; j++) {
			StationSegment rs = isForward ? (forwardStations.get(j))
					: (reverseStations.get(j));
			rs.addSpeed(speed);
			if (j == nowStationId)
				rs.addReal();
		}
		computeNum++;
	}

	public void computeAvgSpeed(int intervalTime) {
		// 1. �����ݿ��и���ָ��ʱ���������õ�һ������
		List<UserData> nowData = this.getUserData(intervalTime);
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
			int speed = -1;
			UserData nowUd = nowData.get(i);
			int index = userDataPool.indexOf(nowUd);
			if (index >= 0) {
				matchesNum++;
				UserData lastUd = userDataPool.get(index);
				speed = (int) this.computeOneSpeed(nowUd, lastUd);
				userDataPool.set(index, nowUd);
			} else {
				newNum++;
				userDataPool.add(nowUd);
			}
			// if not matches or the speed is illegal,then next data;
			if (speed == -1)
				continue;

			// 3. ��������ĳ����ٶȲ��㵽������ÿһ��·��������
			int large = -1, small = -1;
			if (isForward) {
				small = lastStationId;
				large = nowStationId;
			} else {
				small = nowStationId;
				large = lastStationId;
			}
			// < or <= ?
			for (int j = small; j <= large; j++) {
				StationSegment rs = isForward ? (forwardStations.get(j))
						: (reverseStations.get(j));
				rs.addSpeed(speed);
				if (j == nowStationId)
					rs.addReal();
			}
			computeNum++;
		}
		logger.info("========= in one batch : new_add = " + newNum
				+ " , matches = " + matchesNum + " { in_same_segment = "
				+ sameNum + " , compute = " + computeNum + " }");
		this.clearFlags();

		// 4. ����ÿһ��·�������ƽ���ٶȣ���˫�򣬲���ӡ���
		this.computeStationSpeed(forwardStations);
		this.computeStationSpeed(reverseStations);
		// ������ÿһ�����������ƽ���ٶȣ���˫�򣬲���ӡ���
		this.computeSectionSpeed(forwardNodes);
		this.computeSectionSpeed(reverseNodes);
		this.dumpRoadSpeeds();
		this.storeData();

		// 5. ά��UserDataPool,ȥ����ʱ��δ���ֵ����ݡ�
		if (cachedTime >= Configuration.CACHE_TIME) {
			filterUserDataPool();
			cachedTime = 0;
		}
		cachedTime += intervalTime;
	}

	public int computeOneSpeed(UserData nowUd, UserData lastUd) {
		int speed = -1;
		isForward = this.getDirection(nowUd, lastUd);
		double time = this.getIntervalTime(nowUd, lastUd);
		double distance = this.getDistance(isForward);

		if (distance == -1) {
			sameNum++;
		} else {
			speed = (int) (distance / time);
			// �ٶȳ���������Χ������������-1
			if (speed < 0 || speed > 250) {
				logger.warning("illegal speed : d = " + distance + ", t = "
						+ time + ", s = " + speed + "\t , " + nowUd.getCellid()
						+ "\t" + lastUd.getCellid());
				speed = -1;
			}
		}
		return speed;
	}

	public void computeStationSpeed(List<StationSegment> list) {
		for (int i = 0; i < list.size(); i++) {
			StationSegment rs = list.get(i);
			rs.computeAvgSpeed();
			rs.computeFilterAvgSpeed();
			logger.info(" ***** " + rs.id + "\t speeds : "
					+ rs.getSpeeds().toString());
			// logger.info(" &&&&& " + rs.id + "\t filterSpeeds : "
			// + rs.getQualifiedData().toString());
			rs.genAvgSpeedStr(startTimeStr);
		}
	}

	public void computeSectionSpeed(List<NodeSegment> list) {
		for (int i = 0; i < list.size(); i++) {
			NodeSegment rs = list.get(i);
			int start = rs.startNode.cellId;
			int end = rs.endNode.cellId;
			int direction = rs.direction;

			int speed = 0;
			int speedNum = 0;
			int count = 0;
			int maxSpeed = 0;
			int minSpeed = 100;

			int startIndex = -1;
			int endIndex = -1;
			for (int j = 0; j < forwardStations.size(); j++) {
				StationSegment rse = forwardStations.get(j);
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
					StationSegment segment = forwardStations.get(j);
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
					speed += reverseStations.get(j).getAvgSpeed();
					speedNum += reverseStations.get(j).getRealNum();
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
		BufferedWriter bw2 = null;
		try {
			bw = new BufferedWriter(new FileWriter("station_speeds.txt", true));
			bw2 = new BufferedWriter(new FileWriter("section_speeds.txt", true));

			// ��ʱ�������������·���ٶ���Ϣ
			bw.write("=======Time : " + startTimeStr + " =====\n");
			for (int i = 0; i < forwardStations.size(); i++) {
				StringBuffer str = new StringBuffer();
				StationSegment rs = forwardStations.get(i);
				str.append("@" + rs.id + " --- ");
				str.append(Math.round(rs.getAvgSpeed()) + " & "
						+ Math.round(rs.getFilterAvgSpeed()));
				// ���speed���Ա��´�ʹ��
				rs.clear();
				rs = reverseStations.get(i);
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
			bw2.write("=======Time : " + startTimeStr + " =====\n");
			for (int i = 0; i < forwardNodes.size(); i++) {
				StringBuffer str = new StringBuffer();
				NodeSegment rs = forwardNodes.get(i);
				str.append("@" + rs.id + " : " + rs.startNode.roadNodeName
						+ " --- " + rs.endNode.roadNodeName + "\n");
				str.append("speed = " + Math.round(rs.avgSpeed) + " , num = "
						+ rs.speedNum);
				rs = reverseNodes.get(i);
				str.append("\t||\tspeed = " + Math.round(rs.avgSpeed)
						+ " , num = " + rs.speedNum + "\n");
				bw2.write(str.toString());
			}

			bw.write("\n\n");
			bw.flush();
			bw.close();

			bw2.write("\n\n");
			bw2.flush();
			bw2.close();
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
		nowStationId = -1;
		lastStationId = -1;
		for (int i = 0; i < forwardStations.size(); i++) {
			StationSegment rs = forwardStations.get(i);
			// һ��·�ο��ܰ��������λ����ͬ���Ļ�վ
			if (rs.contains(nowCellid))
				nowStationId = i;
			if (rs.contains(lastCellid))
				lastStationId = i;
			// ����λ�ö����ҵ���ֱ������
			if (nowStationId != -1 && lastStationId != -1)
				break;
		}
		if (nowStationId == -1 || lastStationId == -1)
			logger.severe("===== road segment fault ! " + nowStationId + "\t"
					+ lastStationId + "\t" + nowCellid + "\t" + lastCellid);
		if (nowStationId > lastStationId)
			return true;
		else
			return false;
	}

	public double getDistance(boolean direction) {
		double distance = 0;
		// �������ݴ���ͬһ·�Σ��޷�������룬����-1
		if (nowStationId == lastStationId)
			return -1;
		// ֻ���վ���վ֮��ľ��룬ƫС
		if (direction == true) {
			for (int i = lastStationId; i < nowStationId; i++) {
				distance += forwardStations.get(i).length;
			}
		} else {
			for (int i = nowStationId; i < lastStationId; i++) {
				distance += reverseStations.get(i).length;
			}
		}
		return distance;
	}

	public double getIntervalTime(UserData nowUd, UserData lastUd) {
		return (nowUd.getTimestamp().getTime() - lastUd.getTimestamp()
				.getTime()) / (1000.0 * 60 * 60);
	}

	public ArrayList<StationSegment> getRoadSegmentList(boolean isForward) {
		if (isForward)
			return this.forwardStations;
		else
			return this.reverseStations;
	}

	/**
	 * @Title: getUserData
	 * @Description: get HISTORY data from databases;
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public List<UserData> getHistoryUserData(String startTime, String endTime) {
		List<UserData> userDatas = db.selectHistoryUserData(startTime, endTime);
		return userDatas;
	}

	/**
	 * @Title: getUserData
	 * @Description: get REAL TIME data from databases;
	 * @param intervalTime
	 */
	public List<UserData> getUserData(int intervalTime) {
		Date start = null;
		Date end = null;
		try {
			start = sdf.parse(startTimeStr);
			end = new Date(start.getTime() + 1000 * intervalTime);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String startStr = sdf.format(start);
		String endStr = sdf.format(end);
		List<UserData> userDatas = db.selectUserData(startStr, endStr);

		this.startTimeStr = endStr;
		return userDatas;
	}

	public void initialRoadSections() {
		ArrayList<NodeSegment> rss = NodeSegment.initial();
		this.forwardNodes = new ArrayList<>();
		this.reverseNodes = new ArrayList<>();
		for (int i = 0; i < 24; i++) {
			forwardNodes.add(rss.get(i));
		}
		for (int i = 24; i < 48; i++) {
			reverseNodes.add(rss.get(i));
		}
	}

	public void initialRoadSegments() {
		// ��ʱ����:��������·�ζ�����ȫһ��(��0��n);
		this.forwardStations = StationSegment.readFromFile("data/·��.txt");
		this.reverseStations = StationSegment.readFromFile("data/·��.txt");
	}

	public void storeData() {
		db.insertStationSpeeds(forwardStations, startTimeStr, 1);
		db.insertStationSpeeds(reverseStations, startTimeStr, 2);
		db.insertNodeSpeeds(forwardNodes, startTimeStr);
		db.insertNodeSpeeds(reverseNodes, startTimeStr);
	}
}
