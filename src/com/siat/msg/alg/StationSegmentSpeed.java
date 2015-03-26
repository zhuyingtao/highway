package com.siat.msg.alg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import com.siat.ds.Station;
import com.siat.ds.StationSegment;
import com.siat.ds.UserData;
import com.siat.msg.Configuration;
import com.siat.msg.db.DBServiceForOracle;
import com.siat.msg.util.DataLogger;

/**
 * @ClassName SpeedAlgorithm
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月16日 下午3:12:33
 */
public class StationSegmentSpeed {

	private DBServiceForOracle db = null;
	private HashMap<String, UserData> userDataPool = null;

	private int cachedTime = 0;

	private int nowSegmentId = 0;
	private int lastSegmentId = 0;

	private List<StationSegment> stationSegments;

	private Logger logger = null;

	private int newNum = 0; // new adding into the data pool;
	private int matchesNum = 0; // matches one in the data pool;
	private int sameNum = 0; // matches one and they are in the same segment;
	private int computeNum = 0; // matches one and they are not in the same
								// segment;

	private SimpleDateFormat sdf = null;

	public StationSegmentSpeed() {
		// TODO Auto-generated constructor stub
		this.db = new DBServiceForOracle();
		this.logger = DataLogger.getLogger();
		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public void addSpeed(int speed) {
		for (int i = lastSegmentId; i <= nowSegmentId; i++) {
			StationSegment ss = stationSegments.get(i);
			ss.addSpeed(speed);
			ss.addExpected();
			if (i == nowSegmentId)
				ss.addReal();
		}
		computeNum++;
	}

	// clear the flags used for logger;
	public void clearFlags() {
		newNum = 0;
		matchesNum = 0;
		sameNum = 0;
		computeNum = 0;
	}

	// clear the speeds in station segments to prepare for next batch;
	public void clearSpeeds() {
		for (int i = 0; i < stationSegments.size(); i++) {
			stationSegments.get(i).clearSpeeds();
		}
	}

	public void computeAvgSpeed(int intervalTime) {
		// // 1. 从数据库中根据指定时间间隔批量得到一批数据
		// List<UserData> nowData = this.getUserData(intervalTime);
		// // 第一批数据，初始化userDataPool，然后返回
		// if (userDataPool == null) {
		// userDataPool = nowData;
		// return;
		// }
		// logger.info("data size : user_data_pool -> " + userDataPool.size()
		// + " , now_data -> " + nowData.size());
		//
		// // 2. 用userDataPool暂存上一批的数据，将最新一批的数据与上一批数据比较
		// for (int i = 0; i < nowData.size(); i++) {
		// // 如果车辆id匹配，则得出时间距离差，算出每一辆车的平均速度，
		// // 然后更新userDataPool中的数据;
		// // 如果没有匹配车辆，则将新车辆直接插入userDataPool中。
		// int speed = -1;
		// UserData nowUd = nowData.get(i);
		// int index = userDataPool.indexOf(nowUd);
		// if (index >= 0) {
		// matchesNum++;
		// UserData lastUd = userDataPool.get(index);
		// speed = (int) this.computeOneSpeed(nowUd, lastUd);
		// userDataPool.set(index, nowUd);
		// } else {
		// newNum++;
		// userDataPool.add(nowUd);
		// }
		// // if not matches or the speed is illegal,then next data;
		// if (speed == -1)
		// continue;
		//
		// // 3. 将计算出的车辆速度补足到经过的每一个路段区间中
		// int large = -1, small = -1;
		// if (isForward) {
		// small = lastSegmentId;
		// large = nowSegmentId;
		// } else {
		// small = nowSegmentId;
		// large = lastSegmentId;
		// }
		// // < or <= ?
		// for (int j = small; j <= large; j++) {
		// StationSegment rs = isForward ? (forwardStations.get(j))
		// : (reverseStations.get(j));
		// rs.addSpeed(speed);
		// if (j == nowSegmentId)
		// rs.addReal();
		// }
		// computeNum++;
		// }
		// logger.info("========= in one batch : new_add = " + newNum
		// + " , matches = " + matchesNum + " { in_same_segment = "
		// + sameNum + " , compute = " + computeNum + " }");
		// this.clearFlags();
		//
		// // 4. 计算每一个路段区间的平均速度，分双向，并打印输出
		// this.computeStationSpeed(forwardStations);
		// this.computeStationSpeed(reverseStations);
		// // 另：计算每一个服务区间的平均速度，分双向，并打印输出
		// this.nss.computeAvgSpeed(stationSegments, startTimeStr);
		// this.dumpData(startTimeStr);
		// this.storeData(startTimeStr);
		//
		// // 5. 维护UserDataPool,去除长时间未出现的数据。
		// if (cachedTime >= Configuration.CACHE_TIME) {
		// filterUserDataPool();
		// cachedTime = 0;
		// }
		// cachedTime += intervalTime;
	}

	/**
	 * @Title: computeAvgSpeed
	 * @Description: compute the average speed of every batch;
	 * @param batchData
	 */
	public boolean computeAvgSpeed(List<UserData> batchData, String timeStamp) {
		// if it is the first batch : initial the userDataPool, then return.
		// useDataPool is a list that contains the UserData of last batchData or
		// unused up till now; it will be refreshed in a specific time to keep
		// size.
		if (userDataPool == null) {
			userDataPool = new HashMap<>();
			for (int i = 0; i < batchData.size(); i++) {
				UserData ud = batchData.get(i);
				userDataPool.put(ud.getTmsi(), ud);
			}
			return true;
		}

		logger.info("**** data size : user_data_pool -> " + userDataPool.size()
				+ " , batch_data -> " + batchData.size());

		// compute every UserData in the batchData with UserDataPool;
		for (int i = 0; i < batchData.size(); i++) {
			// if the car data matches, then compute the interval time and
			// distance, then compute the speed of this car, and update the data
			// of this car in the UserDataPool;
			// if not, then just insert the car data into the UserDataPool;
			int speed = -1;
			UserData nowUd = batchData.get(i);
			// int index = userDataPool.indexOf(nowUd);
			// if (index >= 0) {
			// matchesNum++;
			// UserData lastUd = userDataPool.get(index);
			// speed = (int) this.computeOneSpeed(nowUd, lastUd);
			// // update when the nowUd is fresher;
			// if (lastSegmentId == -1
			// || (nowUd.isLater(lastUd) && nowSegmentId != -1))
			// userDataPool.set(index, nowUd);
			// } else {
			// newNum++;
			// userDataPool.add(nowUd);
			// }

			String key = nowUd.getTmsi();
			boolean contain = userDataPool.containsKey(key);
			if (contain) {
				matchesNum++;
				UserData lastUd = userDataPool.get(key);
				speed = (int) this.computeOneSpeed(nowUd, lastUd);
				// update when the nowUd is fresher;
				if (lastSegmentId == -1
						|| (nowUd.isLater(lastUd) && nowSegmentId != -1))
					userDataPool.put(key, nowUd);
			} else {
				newNum++;
				userDataPool.put(key, nowUd);
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
		// clear flags for next batch;
		this.clearFlags();

		// compute every Station Segment's speed;
		this.computeStationSpeed(stationSegments);
		// store result into database;
		if (Configuration.WRITE_TO_DATABASE)
			this.storeData(timeStamp);
		// dump result into text;
		if (Configuration.WRITE_TO_FILE)
			this.dumpData(timeStamp);

		// maintain the UserDataPool, delete the data which is not used for a
		// period of time;
		if (cachedTime >= Configuration.CACHE_TIME) {
			filterUserDataPool(timeStamp);
			cachedTime = 0;
		}
		cachedTime += Configuration.INTERVAL_TIME;

		return false;
	}

	// public void computeHistorySpeed(String startTime, String endTime, int
	// rate) {
	// Date startDate = new Date(); // used for recording computing time;
	//
	// this.startTimeStr = startTime;
	// // not to clear here;
	// // this.userDataPool = null;
	// int startIndex = 0;
	// int batchNum = 0;
	// // set the interval computing time of every batch, if it is not enough,
	// // then just wait; if it is exceed, then give a warning;
	// int batchInterval = Configuration.INTERVAL_TIME / rate;
	//
	// // 1. get all the history data from databases into memory according to
	// // the startTime and endTime;
	// List<UserData> allData = this.getHistoryUserData(startTime, endTime);
	// // 2. compute the average speed of every batch;
	// logger.info("**** All batches begin to compute ... ");
	// while (startIndex < allData.size()) {
	// Date date1 = new Date();
	//
	// // before every computing, check whether the request has been
	// // updated;
	// boolean updated = this.checkRequest();
	// if (updated) {
	// logger.info("New request has coming ... now return ");
	// return;
	// }
	// // if not updated, then begin to compute;
	// List<UserData> batchData = this
	// .getOneBatchData(allData, startIndex);
	// // update the start time, the time must update before write to
	// // database;
	// this.startTimeStr = this.updateTime(startTimeStr);
	// boolean firstCompute = this.computeAvgSpeed(batchData);
	// if (!firstCompute)
	// nss.computeAvgSpeed(stationSegments, startTimeStr);
	// startIndex = endIndex;
	// batchNum++;
	//
	// // clear flags and station segments for next batch;
	// this.clearFlags();
	// this.clearSpeeds();
	//
	// // check the computing time;
	// Date date2 = new Date();
	// double time = (date2.getTime() - date1.getTime()) / 1000.0; // s
	// if (time > batchInterval) {
	// // computing time is exceed;
	// logger.severe("'''''''' over time , stand = " + batchInterval
	// + " s , real = " + time + " s");
	// } else {
	// // computing time is not enough;
	// double waitTime = batchInterval - time;
	// logger.severe("''''''' wait time , sleep = " + waitTime + " s ");
	// // Utility.sleep(waitTime);
	// }
	// }
	// logger.info("**** All batches have finished ... count : " + batchNum
	// + " , time = " + Utility.intervalTime(startDate, new Date()));
	// }

	/**
	 * @Title: computeOneSpeed
	 * @Description: compute one car's speed according to the user data;
	 * @param nowUd
	 * @param lastUd
	 * @return
	 */
	public int computeOneSpeed(UserData nowUd, UserData lastUd) {
		int speed = -1;
		this.getDirection(nowUd, lastUd);
		double time = this.getIntervalTime(nowUd, lastUd);
		double distance = this.getDistance();

		if (distance == -1) {
			sameNum++;
		} else {
			speed = (int) (distance / time);
			// if the speed is too large and negative, then it is illegal;
			if (speed < 0 || speed > 250) {
				logger.warning("illegal speed : d = " + distance + ", t = "
						+ time + ", s = " + speed + "\t , " + nowUd.getCellid()
						+ "\t" + lastUd.getCellid());
				speed = -1;
			}
		}
		return speed;
	}

	/**
	 * @Title: computeStationSpeed
	 * @Description: compute every station segment's speed;
	 * @param list
	 */
	public void computeStationSpeed(List<StationSegment> list) {
		for (int i = 0; i < list.size(); i++) {
			StationSegment rs = list.get(i);
			rs.computeAvgSpeed();
			rs.computeFilterAvgSpeed();
		}
	}

	/**
	 * @Title: dumpData
	 * @Description: write the speed data to file directly, so it is easier to
	 *               examine the result;
	 */
	public void dumpData(String timeStamp) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter("station_speeds.txt", true));

			bw.write("=======Time : " + timeStamp + " =====\n");
			StringBuffer str = new StringBuffer();
			str.append("id\t\tdir\t\tmax\t\tmin\t\tavg\t\tfilter\t\tnum\n");
			// write out all the node segments information;
			for (int i = 0; i < stationSegments.size(); i++) {
				StationSegment rs = stationSegments.get(i);
				str.append(rs.id + " -- " + rs.getDirection() + " : "
						+ rs.getMaxSpeed() + " , " + rs.getMinSpeed() + " , "
						+ rs.getAvgSpeed() + " , " + rs.getFilterAvgSpeed()
						+ " , (" + rs.getRealNum() + ")\n");
			}
			bw.write(str.toString() + "\n\n");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @Title: filterUserDataPool
	 * @Description: filter the dead data (not used for a long time) in the user
	 *               data pool;
	 */
	public void filterUserDataPool(String timeStamp) {
		Date nowDate = null;
		int removedNum = 0;
		try {
			nowDate = sdf.parse(timeStamp);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < userDataPool.size(); i++) {
			UserData ud = userDataPool.get(i);
			Date udDate = ud.getTimestamp();
			double interval = Math.abs(nowDate.getTime() - udDate.getTime()) / 1000;
			if (interval >= Configuration.CACHE_TIME) {
				userDataPool.remove(i);
				removedNum++;
				logger.info("remove dead data --> " + ud.getTmsi() + " @ "
						+ nowDate + " - " + udDate);
			}
		}
		logger.info(timeStamp + "========== removed [" + removedNum
				+ "] dead car !");
	}

	/**
	 * @Title: getDirection
	 * @Description: determine the direction that the car runs in now. As the
	 *               station segment is in ascending order, so we can find the
	 *               station segment the car of now and last belongs to , then
	 *               based on the id to determine the direction.
	 * @param nowUd
	 * @param lastUd
	 * @return
	 */
	public boolean getDirection(UserData nowUd, UserData lastUd) {
		int nowId = nowUd.getCellid();
		int lastId = lastUd.getCellid();
		// the boundary of forward and reverse;
		int boundary = stationSegments.size() / 2;
		nowSegmentId = -1;
		lastSegmentId = -1;

		for (int i = 0; i < boundary; i++) {
			StationSegment ss = stationSegments.get(i);
			// a segment may contains more than one station (same location);
			if (ss.contains(nowId))
				nowSegmentId = i;
			if (ss.contains(lastId))
				lastSegmentId = i;
			// the two segmentId have both been found, then break;
			if (nowSegmentId != -1 && lastSegmentId != -1)
				break;
		}
		if (nowSegmentId == -1 || lastSegmentId == -1) {
			logger.fine("===== road segment fault ! now_seg = " + nowSegmentId
					+ ", now_id = " + nowId + " , last_seg = " + lastSegmentId
					+ " , last_id = " + lastId);
			return false;
		}

		if (nowSegmentId >= lastSegmentId) {
			return true;
		} else {
			// this is in reverse direction, then we do some changes, so that
			// the nowSegmentId always after the lastSegmentId;
			logger.fine("<<<<< ---- change segment id now = " + nowSegmentId
					+ " , last = " + lastSegmentId);
			// here must reset these value, or they won't enter the cycle;
			nowSegmentId = -1;
			lastSegmentId = -1;
			for (int i = boundary; i < stationSegments.size(); i++) {
				StationSegment rs = stationSegments.get(i);
				if (rs.contains(nowId))
					nowSegmentId = i;
				if (rs.contains(lastId))
					lastSegmentId = i;
				if (nowSegmentId != -1 && lastSegmentId != -1)
					break;
			}
			logger.fine(">>>>> now = " + nowSegmentId + " , last = "
					+ lastSegmentId);
			return false;
		}
	}

	/**
	 * @Title: getDistance
	 * @Description: get the distance of two related user data;
	 * @return
	 */
	public double getDistance() {
		double distance = 0;
		// one of the two data is illegal;
		if (nowSegmentId == -1 || lastSegmentId == -1)
			return -1;
		// the two data is in same segment (or may be in two contiguous
		// segments)，then return -1;
		if (nowSegmentId == lastSegmentId)
			return -1;
		// the two data is in different segment;
		for (int i = lastSegmentId; i < nowSegmentId; i++) {
			distance += stationSegments.get(i).getLength();
		}
		return distance;
	}

	/**
	 * @Title: getIntervalTime
	 * @Description: get the interval time (hour) of last data and now data.
	 * @param nowUd
	 * @param lastUd
	 * @return the interval hour;
	 */
	public double getIntervalTime(UserData nowUd, UserData lastUd) {
		// mostly, the nowUd time is later than lastUd; but there are still some
		// exceptions(if the selected data is not in order) that nowUd time is
		// earlier, so we use the absolute value to keep the interval time is
		// always positive;
		return (Math.abs(nowUd.getTimestamp().getTime()
				- lastUd.getTimestamp().getTime()))
				/ (1000.0 * 60 * 60);
	}

	/**
	 * @Title: getUserData
	 * @Description: get REAL TIME data from databases;
	 * @param intervalTime
	 */
	String startTimeStr = null;

	public List<UserData> getUserData(int intervalTime) {
		Date start = null;
		Date end = null;
		try {
			start = sdf.parse(startTimeStr);
			end = new Date(start.getTime() + 1000 * intervalTime);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		String startStr = sdf.format(start);
		String endStr = sdf.format(end);
		List<UserData> userDatas = db.selectUserData(startStr, endStr);

		this.startTimeStr = endStr;
		return userDatas;
	}

	/**
	 * @Title: initialStationSegments
	 * @Description: initial the station segments and the stations;
	 * @return
	 */
	public List<StationSegment> initialStationSegments() {
		logger.info("initial station segments ... ");
		List<Station> stations = db.selectStation();
		List<StationSegment> segments = db.selectStationSegment();
		for (int i = 0; i < segments.size(); i++) {
			StationSegment ss = segments.get(i);
			ss.initStarts(stations);
			ss.initEnds(stations);
		}
		this.stationSegments = segments;
		return segments;
	}

	/**
	 * @Title: storeData
	 * @Description: store the speed data into the database;
	 * @param timeStamp
	 */
	public void storeData(String timeStamp) {
		db.insertStationSpeeds(stationSegments, timeStamp);
		// update the value in certain database to inform that new data has been
		// inserted into database;
		db.updateTime(timeStamp);
	}

	public List<StationSegment> getStationSegments() {
		return stationSegments;
	}

	public void setStationSegments(List<StationSegment> stationSegments) {
		this.stationSegments = stationSegments;
	}
}
