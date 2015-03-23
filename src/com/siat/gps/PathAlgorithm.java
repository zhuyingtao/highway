//package com.siat.gps;
//
//import java.io.Serializable;
//import java.text.DecimalFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.List;
//import java.util.logging.Logger;
//
//import com.siat.ds.NodeSegment;
//import com.siat.ds.StationSegment;
//import com.siat.msg.UserData;
//
///**
// * @ClassName PathAlgorithm
// * @Description This is the main class for computing the car's information and
// *              return some useful information to the streaming.
// * @author Zhu Yingtao
// * @date 2014年11月7日 下午3:11:33
// */
//public class PathAlgorithm implements Serializable {
//
//	private static final long serialVersionUID = 1L;
//
//	private String startTimeStr = null;
//	private ArrayList<StationSegment> forwardRS;
//	private ArrayList<StationSegment> reverseRS;
//
//	private ArrayList<NodeSegment> forwardRSe;
//	private ArrayList<NodeSegment> reverseRSe;
//
//	private ArrayList<GpsData> gpsDataPool = null;
//
//	private SimpleDateFormat sdf = null;
//	private Logger logger = null;
//	private DBService db = null;
//	
//	private int newNum = 0; // 新添加进数据池的数目
//	private int matchesNum = 0; // 与数据池中数据匹配的数目
//	private int sameNum = 0; // 与数据池中数据处于同一路段数目
//	private int computeNum = 0; // 与数据池中数据不处于同一路段数目
//
//	List<Car> cars = null;
//
//	static int carId = 0; // the id of the car, count from 0;
//	Car earliestCar = null;
//	Car latestCar = null;
//
//	public PathAlgorithm(String startTimeStr) {
//		// TODO Auto-generated constructor stub
//		if (cars == null)
//			cars = new ArrayList<Car>();
//
//		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		this.logger = GpsLogger.getLogger();
//		this.initialRoadSegments();
//		this.initialRoadSections();
//	}
//
//	public ArrayList<GpsData> getGpsData(int intervalTime) {
//		Date start = null;
//		Date end = null;
//		try {
//			start = sdf.parse(startTimeStr);
//			end = new Date(start.getTime() + 1000 * intervalTime);// 间隔1分钟
//		} catch (ParseException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		String startStr = sdf.format(start);
//		String endStr = sdf.format(end);
//		ArrayList<GpsData> gpsDatas = db.selectGpsData(startStr, endStr);
//
//		this.startTimeStr = endStr;
//		return gpsDatas;
//	}
//
//	public void computeAvgSpeed(int intervalTime) {
//		// 1. 从数据库中根据指定时间间隔批量得到一批数据
//		ArrayList<GpsData> nowData = this.getGpsData(intervalTime);
//		// 第一批数据，初始化userDataPool，然后返回
//		if (gpsDataPool == null) {
//			gpsDataPool = nowData;
//			return;
//		}
//		logger.info("data size : gps_data_pool -> " + gpsDataPool.size()
//				+ " , now_data -> " + nowData.size());
//
//		// 2. 用gpsDataPool暂存上一批的数据，将最新一批的数据与上一批数据比较
//		for (int i = 0; i < nowData.size(); i++) {
//			// 如果车辆id匹配，则得出时间距离差，算出每一辆车的平均速度，然后更新gpsDataPool中的数据;
//			// 如果没有匹配车辆，则将新车辆直接插入gpsDataPool中。
//			GpsData nowGd = nowData.get(i);
//			int speed = (int) this.computeOneSpeed(nowGd);
//			if (speed == -1)
//				continue;
//
//			// 3. 将计算出的车辆速度补足到经过的每一个路段区间中
//			int large = -1, small = -1;
//			if (isForward) {
//				small = lastRSid;
//				large = nowRSid;
//			} else {
//				small = nowRSid;
//				large = lastRSid;
//			}
//			// < or <= ?
//			for (int j = small; j <= large; j++) {
//				StationSegment rs = isForward ? (forwardRS.get(j)) : (reverseRS
//						.get(j));
//				rs.addSpeed(speed);
//				if (j == nowRSid)
//					rs.addReal();
//			}
//			computeNum++;
//		}
//		logger.info("========= in one batch : new_add = " + newNum
//				+ " , matches = " + matchesNum + " { in_same_segment = "
//				+ sameNum + " , compute = " + computeNum + " }");
//		this.clearLogger();
//
//	}
//
//	public double computeOneSpeed(GpsData nowGd) {
//		double speed = -1;
//		int index = gpsDataPool.indexOf(nowGd);
//		// 有车辆id匹配
//		if (index >= 0) {
//			matchesNum++;
//			GpsData lastUd = gpsDataPool.get(index);
//			isForward = this.getDirection(nowGd, lastUd);
//			double distance = this.getDistance(isForward);
//			double time = this.getIntervalTime(nowGd, lastUd);
//			// userDataPool更新要提前，否则可能因提前return无法更新
//			gpsDataPool.set(index, nowGd);
//			if (distance == -1) {
//				sameNum++;
//				return -1;
//			}
//
//			speed = distance / time;
//			// 速度超出正常范围，则丢弃，返回-1
//			if (speed < 0 || speed > 150) {
//				logger.warning("illegal speed : d = " + distance + ", t = "
//						+ time + ", s = " + speed + "\t , " + nowGd.getCellid()
//						+ "\t" + lastUd.getCellid());
//				speed = -1;
//			}
//			// 没有车辆id匹配
//		} else {
//			newNum++;
//			gpsDataPool.add(nowGd);
//		}
//		return speed;
//	}
//
//	/**
//	 * @Title: computeRoadNode
//	 * @Description: this is the main method using to compute some roadNodes'
//	 *               'average' speed at a period of time according to a giving
//	 *               carInfo String. The primary steps have been given in the
//	 *               comments as follows.
//	 * @param carInfo
//	 * @return a sequence of roadNodes' speed
//	 */
//	public String computeRoadNode(String carInfo) {
//		// 1. Split the carInfo line and generate a Car instance.
//		Car nowCar = splitCarInfo(carInfo);
//
//		// 2. Compute the car belongs to which roadNode including different
//		// orientation.
//		int nodeId = getNodeID(nowCar);
//		if (nodeId < 0)
//			return null;
//
//		int orientation = getOrientation(nowCar, forwardRS.get(nodeId));
//		if (orientation == 1)
//			nodeId++;
//		if (nowCar.speed > 80) {
//			System.out.println("speed > 80 ...." + nowCar.number + "\t"
//					+ nodeId + "\t" + nowCar.speed);
//		}
//		nowCar.setRoadNodeId(nodeId); // update the car's property;
//
//		// 3. get the last nodeId of the car and according nowCar and
//		// lastCar,generate a sequence about a series of
//		// roadNodes' 'average'speed;
//		Car lastCar = this.getLastCar(nowCar);
//		double speed = this.getSpeed(nowCar, lastCar);
//		if (speed > 80)
//			System.out.println("avgSpeed ===> " + speed);
//		String speedString = null;
//		if (speed != -1) {
//			speedString = this.genSpeedString(nowCar, lastCar, speed);
//		}
//
//		// 4. Check whether the car having the same NUMBER has existed in the
//		// cars TreeSet.If so, replace the old one with this new one , otherwise
//		// add this new one.
//		if (cars.contains(nowCar)) {
//			cars.remove(nowCar);
//			System.out.println("remove " + nowCar);
//		}
//		cars.add(nowCar);
//
//		if (earliestCar == null || earliestCar.date.after(nowCar.date))
//			earliestCar = nowCar;
//		if (latestCar == null || latestCar.date.before(nowCar.date))
//			latestCar = nowCar;
//
//		// 5. Remove the "dead car"(don't send info over 10 minutes) in the
//		// TreeSet.
//		this.removeDeadCar(10);
//		// return the speed sequence like this:"3:24;5:24;7:24;9:24;";
//		return speedString;
//	}
//
//	/**
//	 * @Title: genSpeedString
//	 * @Description: TODO
//	 * @param nowCar
//	 * @param lastCar
//	 * @param speed
//	 * @return
//	 */
//	public String genSpeedString(Car nowCar, Car lastCar, double speed1) {
//		StringBuffer sb = new StringBuffer();
//		String speed = new DecimalFormat("0.0").format(speed1);
//		if (lastCar == null) {
//			sb.append(nowCar.roadNodeId + ":" + speed + ";");
//		} else {
//			int large = nowCar.roadNodeId;
//			int small = lastCar.roadNodeId;
//			if (large < small) {
//				int temp = large;
//				large = small;
//				small = temp;
//			}
//			for (int i = small; i <= large; i += 2) {// notice i+2
//				sb.append(i + ":" + speed + ";");
//			}
//		}
//		sb.append(nowCar.roadNodeId);
//		return sb.toString();
//	}
//
//	/**
//	 * @Title: getLastCar
//	 * @Description: get the last car of same carNumber in the treeSet;
//	 * @param car
//	 * @return the last car of same carNumber
//	 */
//	public Car getLastCar(Car car) {
//		Car lastCar = null;
//		Iterator<Car> carList = cars.iterator();
//		while (carList.hasNext()) {
//			Car car1 = carList.next();
//			if (car.equals(car1)) {
//				lastCar = car1;
//				break;
//			}
//		}
//		return lastCar;
//	}
//
//	/**
//	 * @Title: getNodeID
//	 * @Description: compute the giving car belongs to which node without
//	 *               orientation.
//	 * @param gpsData
//	 * @return
//	 */
//	// ================>TODO: This method should be optimized...
//	public int getNodeID(GpsData gpsData) {
//		double a2; // 点到线段前一个node的距离平方
//		int key;
//		int roadID;
//
//		if (!isInBoundary(gpsData)) {
//			return -1;
//		}
//
//		// 先比较第一个node.
//		double dis2 = Utility.disSquare(gpsData.point, forwardRS.get(0));
//		key = forwardRS.get(0).id;
//		roadID = forwardRS.get(0).roadID;
//
//		for (int i = 2; i < forwardRS.size(); i += 2) {
//			a2 = Utility.disSquare(gpsData.point, forwardRS.get(i));
//			if (a2 < dis2) {
//				dis2 = a2;
//				key = forwardRS.get(i).id;
//				roadID = forwardRS.get(i).roadID;
//			}
//		}
//
//		if (roadID != Configuration.roadID)
//			return -1;
//		return key;
//	}
//
//	/**
//	 * @Title: getNodeOrientation
//	 * @Description: determine whether the car runs forward or backward,
//	 *               according to the radian difference between the car and the
//	 *               roadNode it belongs to.
//	 * @param car
//	 * @param node
//	 * @return 0 is forward; 1 is backward.
//	 */
//	public int getOrientation(Car car, RoadNode node) {
//		// the azimuth of the road node;
//		double rodeDirection = Utility.getAzimuth(node.start, node.end);
//		double radDiff = 0;
//		// Note: if car's speed == 0, then its direction may be nonsense, so we
//		// should determine its direction according to its last position and its
//		// now position.
//		if (car.speed != 0) {
//			radDiff = Math.abs(rodeDirection - car.getRad());
//		} else {
//			Car lastCar = this.getLastCar(car);// the lastCar may be null
//			double carDirection = 0;
//			// may be this is inaccurate,this is to be determined.
//			if (lastCar == null) {
//				carDirection = car.direction;
//			} else {
//				carDirection = Utility.getAzimuth(car.point, lastCar.point);
//			}
//			radDiff = Math.abs(rodeDirection - carDirection);
//		}
//		if (radDiff < Math.PI / 2 || radDiff > 1.5 * Math.PI)
//			return 0; // the car runs in the forward direction.
//		else
//			return 1; // the car runs in the reverse direction.
//	}
//
//	/**
//	 * @Title: getSpeed
//	 * @Description: get the speed of the car in a period of time;it is an
//	 *               average speed.
//	 * @param nowCar
//	 * @param lastCar
//	 * @return
//	 */
//	public double getSpeed(Car nowCar, Car lastCar) {
//		if (lastCar == null)
//			// return nowCar.speed;
//			return -1;
//
//		double speed = 0;
//		double intervalTime = Math.abs((nowCar.date.getTime() - lastCar.date
//				.getTime()) / (1000.0 * 60 * 60));
//		if (intervalTime == 0)
//			return nowCar.speed;
//
//		double distance = 0;
//
//		int nowRoadID = nowCar.roadNodeId;
//
//		if (Math.sqrt(Utility.disSquare(nowCar.point, forwardRS.get(nowRoadID))) * 1000 > 30) {
//			return -1;
//		}
//
//		int lastRoadID = lastCar.roadNodeId;
//		int intervalRoadID = nowRoadID - lastRoadID;
//		if (intervalRoadID == 0) { // at the same roadNode
//			distance = Utility.getDistance(nowCar.point, lastCar.point);
//			speed = distance / intervalTime;
//		} else { // at different roadNodes
//			// the distance includes 3 parts:
//			distance += disOfNodeRoadEndPoint(lastCar);
//			if (intervalRoadID > 0) { // the car runs forward;
//				for (int i = 2; i < intervalRoadID; i += 2)
//					distance += forwardRS.get(lastRoadID + i).length();
//			} else { // the car runs backward;
//				for (int i = -2; i > intervalRoadID; i -= 2)
//					distance += forwardRS.get(lastRoadID + i).length();
//			}
//			distance += disOfNodeRoadStartPoint(nowCar);
//			speed = distance / intervalTime;
//		}
//		if (distance <= 0 | intervalTime <= 0)
//			System.out.println("=======>illegal arguments: " + distance + " "
//					+ intervalTime);
//		if (Double.isNaN(speed) || speed < 0 || speed > 150) {
//			System.out.println("=======> rubbish speed : " + speed);
//			return -1;
//		}
//		// System.out.println("==========>>>>>>>>> " + distance + "\t"
//		// + intervalTime + "\t" + speed);
//
//		return speed;
//	}
//
//	/**
//	 * @Title: removeDeadCar
//	 * @Description: remove the dead car(don't update over some time) in the
//	 *               treeSet according to the giving limit time.
//	 * @param minutes
//	 */
//	public void removeDeadCar(int minutes) {
//		long latestTime = latestCar.date.getTime();
//		long earliestTime = earliestCar.date.getTime();
//		if (latestTime - earliestTime > minutes * 60 * 1000) {
//			cars.remove(earliestCar);
//			System.out.println("remove dead car : " + earliestCar);
//			Collections.sort(cars);
//			earliestCar = cars.get(0);
//		}
//	}
//
//	/**
//	 * @Title: splitCarInfo
//	 * @Description: split the carInfo String and generate a car instance.
//	 * @param carInfo
//	 * @return
//	 */
//	public static Car splitCarInfo(String carInfo) {
//		String[] array = carInfo.split("\t");
//		int id = carId++;
//		String carNumber = array[0];
//		String dateStr = array[3];
//		// String timeStr = array[4];
//
//		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd#HH:mm:ss");
//		Date date = null;
//		try {
//			// date = sdf.parse(dateStr + " " + timeStr);
//			date = sdf.parse(dateStr);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		double lon = Double.parseDouble(array[1]);
//		double lat = Double.parseDouble(array[2]);
//		// double speed = Double.parseDouble(array[5]);
//		double speed = Double.parseDouble(array[4]);
//		// int direction = Integer.parseInt(array[6].trim());
//		int direction = Integer.parseInt(array[5].trim());
//		Car car = new Car(id, carNumber, date, new Point(lon, lat), speed,
//				direction);
//		return car;
//	}
//
//	/**
//	 * @Title: disOfNodeRoadStartPoint
//	 * @Description: compute the distance between the car and the start point of
//	 *               roadnode the car belongs to using cosine theorem.
//	 * @param car
//	 *            instance
//	 * @return distance
//	 */
//	public double disOfNodeRoadStartPoint(Car car) {
//		RoadNode roadNode = forwardRS.get(car.roadNodeId);
//		double a2 = Utility.disSquare(car.point, roadNode.start);
//		double b2 = Utility.disSquare(car.point, roadNode.end);
//		double c2 = roadNode.length();
//		double result = Math.abs((a2 + c2 * c2 - b2) / (2 * c2));
//		// double dis = Math.sqrt(disSquare(car.point,
//		// roadNodes.get(car.roadNodeId)))*1000;
//		// System.out.println("disOfNodeRoadStartPoint =  " + result * 1000 +
//		// "    dis=" + dis);
//		// System.out.println("disOfNodeRoadStartPoint =  " + result * 1000);
//		return result;
//	}
//
//	/**
//	 * @Title: disOfNodeRoadEndPoint
//	 * @Description: compute the distance between the car and the end point of
//	 *               roadnode the car belongs to using cosine theorem.
//	 * @param car
//	 *            instance
//	 * @return distance
//	 */
//
//	public double disOfNodeRoadEndPoint(Car car) {
//		RoadNode roadNode = forwardRS.get(car.roadNodeId);
//		double a2 = Utility.disSquare(car.point, roadNode.start);
//		double b2 = Utility.disSquare(car.point, roadNode.end);
//		double c2 = roadNode.length();
//		double result = Math.abs((b2 + c2 * c2 - a2) / (2 * c2));
//		// double dis = Math.sqrt(disSquare(car.point,
//		// roadNodes.get(car.roadNodeId)))*1000;
//		// System.out.println("disOfNodeRoadEndPoint = " + result * 1000 +
//		// "    dis= " + dis);
//		// System.out.println("disOfNodeRoadEndPoint = " + result * 1000);
//		return result;
//	}
//
//	public double[] getRoadBoundary(ArrayList<RoadNode> roadNodes, int roadID) {
//		double[] boundary = new double[4];
//		double maxX = 0;
//		double minX = 0;
//		double maxY = 0;
//		double minY = 0;
//		RoadNode roadNode = null;
//		for (int i = 0; i < roadNodes.size(); i++) {
//			roadNode = roadNodes.get(i);
//			if (roadNode.roadID == roadID) {
//				if (roadNode.start.getX() > maxX)
//					maxX = roadNode.start.getX();
//				if (roadNode.start.getX() < minX)
//					minX = roadNode.start.getX();
//				if (roadNode.start.getY() > maxY)
//					maxY = roadNode.start.getY();
//				if (roadNode.start.getY() < minY)
//					minY = roadNode.start.getY();
//			}
//		}
//		boundary[0] = maxX + 0.005; // 最大经度区域
//		boundary[1] = minX - 0.005; // 最小经度区域
//		boundary[2] = maxY + 0.005; // 最大纬度区域
//		boundary[3] = minY - 0.005; // 最小纬度区域
//		return boundary;
//	}
//
//	public boolean isInBoundary(Car car) {
//		double[] bountary = getRoadBoundary(forwardRS, Configuration.roadID);
//		if (car.point.getX() <= bountary[0] && car.point.getX() >= bountary[1]
//				&& car.point.getY() <= bountary[2]
//				&& car.point.getY() >= bountary[3])
//			return true;
//		return false;
//	}
//
//	public void initialRoadSegments() {
//		// 暂时处理:正向逆向路段对象完全一样(从0到n);
//		this.forwardRS = StationSegment.readFromFile("data/路段.txt");
//		this.reverseRS = StationSegment.readFromFile("data/路段.txt");
//	}
//
//	public void initialRoadSections() {
//		ArrayList<NodeSegment> rss = NodeSegment.initial();
//		this.forwardRSe = new ArrayList<>();
//		this.reverseRSe = new ArrayList<>();
//		for (int i = 0; i < 24; i++) {
//			forwardRSe.add(rss.get(i));
//		}
//		for (int i = 24; i < 48; i++) {
//			reverseRSe.add(rss.get(i));
//		}
//	}
// }
