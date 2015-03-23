package com.siat.msg.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.siat.ds.Node;
import com.siat.ds.NodeSegment;
import com.siat.ds.Station;
import com.siat.ds.StationSegment;
import com.siat.msg.Configuration;
import com.siat.msg.UserData;
import com.siat.msg.util.DataLogger;
import com.siat.msg.util.Utility;

/**
 * @ClassName DBServiceForOracle
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2015年1月12日 下午9:05:27
 */
public class DBServiceForOracle {

	String driver = "oracle.jdbc.driver.OracleDriver";
	String url = "jdbc:oracle:thin:@210.75.252.44:1521:ORCL";
	String user = "hw";
	String password = "hw";

	static Connection conn = null;
	PreparedStatement pstm = null;
	Logger logger = null;

	/**
	 * 
	 */
	public DBServiceForOracle() {
		// TODO Auto-generated constructor stub
		if (conn == null)
			conn = this.getConnection();
		if (logger == null)
			logger = DataLogger.getLogger();
	}

	/**
	 * @Title: getConnect
	 * @Description: 连接数据库
	 */
	private Connection getConnection() {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * @Title: executeSQL
	 * @Description: 执行SQL脚本文件
	 * @param sqlPath
	 */
	public void executeSQL() {
		// TODO Auto-generated method stub
		try {
			Statement stm = conn.createStatement();
			// String sql = "CREATE TABLE section_speeds(id NUMBER(10));";
			// + "(id NUMBER,name VARCHAR2(50),time DATE,"
			// + "direction NUMBER,max_speed NUMBER,min_speed NUMBER,"
			// + "avg_speed NUMBER,num NUMBER);";

			ResultSet rs = stm.executeQuery("select * from hw_road_node");
			while (rs.next()) {
				System.out.println(rs.getString(1) + " , " + rs.getString(2)
						+ " , " + rs.getString(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<NodeSegment> selectNodeSegment() {
		Date date1 = new Date();
		logger.info("==== begin to select node segment ... ");
		String sql = "select id, direction, start_road_node, end_road_node, length from "
				+ "hw_road_node_segment order by id";
		List<NodeSegment> list = new ArrayList<NodeSegment>();

		try {
			pstm = conn.prepareStatement(sql);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				int direction = rs.getInt(2);
				int startNode = rs.getInt(3);
				int endNode = rs.getInt(4);
				double length = rs.getDouble(5);
				NodeSegment ns = new NodeSegment(id, startNode, endNode,
						direction, length);
				list.add(ns);
			}
			rs.close();
			pstm.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date date2 = new Date();
		logger.info("==== end of select, count = " + list.size() + " , time = "
				+ Utility.intervalTime(date1, date2));
		return list;
	}

	public List<Node> selectNode() {
		Date date1 = new Date();
		logger.info("==== begin to select node ... ");
		String sql = "select id, direction, cell_id, length from "
				+ "hw_road_node";
		List<Node> list = new ArrayList<Node>();

		try {
			pstm = conn.prepareStatement(sql);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				int direction = rs.getInt(2);
				int cellId = rs.getInt(3);
				double length = rs.getDouble(4);
				Node node = new Node(id, cellId, length, direction);
				list.add(node);
			}
			rs.close();
			pstm.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date date2 = new Date();
		logger.info("==== end of select, count = " + list.size() + " , time = "
				+ Utility.intervalTime(date1, date2));
		return list;
	}

	public List<Station> selectStation() {
		Date date1 = new Date();
		logger.info("==== begin to select station ... ");
		String sql = "select cell_id, lac_id, longitude, latitude from "
				+ "hw_station where serial_number = ? ";
		List<Station> list = new ArrayList<Station>();

		try {
			pstm = conn.prepareStatement(sql);
			pstm.setInt(1, 6); // select the stations in line 6 (福银高速);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				int lacId = rs.getInt(2);
				double longitude = rs.getDouble(3);
				double latitude = rs.getDouble(3);
				Station s = new Station(id, lacId, longitude, latitude);
				list.add(s);
			}
			rs.close();
			pstm.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date date2 = new Date();
		logger.info("==== end of select, count = " + list.size() + " , time = "
				+ Utility.intervalTime(date1, date2));
		return list;
	}

	public List<StationSegment> selectStationSegment() {
		Date date1 = new Date();
		logger.info("==== begin to select station segment... ");
		String sql = "select id, roadLength, direction, cell_id_start, cell_id_end from "
				+ "hw_station_segment";
		List<StationSegment> list = new ArrayList<StationSegment>();

		try {
			pstm = conn.prepareStatement(sql);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				double length = rs.getDouble(2);
				int direction = rs.getInt(3);
				// parse start string to integer array;
				String[] starts = rs.getString(4).split(";");
				List<Integer> startIds = new ArrayList<Integer>();
				for (int i = 0; i < starts.length; i++) {
					startIds.add(Integer.parseInt(starts[i]));
				}
				// parse end string to integer array;
				String[] ends = rs.getString(5).split(";");
				List<Integer> endIds = new ArrayList<Integer>();
				for (int i = 0; i < ends.length; i++) {
					endIds.add(Integer.parseInt(ends[i]));
				}
				StationSegment ss = new StationSegment(id, length, startIds,
						endIds, direction);
				list.add(ss);
			}
			rs.close();
			pstm.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date date2 = new Date();
		logger.info("==== end of select, count = " + list.size() + " , time = "
				+ Utility.intervalTime(date1, date2));
		return list;
	}

	/**
	 * @Title: selectHistoryUserData
	 * @Description: select history user data from database,because history data
	 *               usually very large, so we can't do the duplicate checking
	 *               here;
	 * @param start
	 * @param end
	 * @return
	 */
	public List<UserData> selectHistoryUserData(String start, String end) {
		// TODO Auto-generated method stub
		Date date1 = new Date();
		logger.info("==== begin to select history data : from " + start
				+ ", to " + end);
		List<UserData> userDatas = new ArrayList<>();
		// there are two kinds of method to send date type;
		// String sql =
		// "select tmsi,to_char(timestamp,'yyyy-mm-dd hh24:mi:ss'), "
		// + "lac, cellid, eventid, id from hw_data_user_tmp"
		// + " where timestamp between ? and ? ";
		// + "order by timestamp";
		String sql = "select tmsi,to_char(timestamp,'yyyy-mm-dd hh24:mi:ss'), "
				+ "lac, cellid, eventid, id from hw_data_user where timestamp"
				+ " between to_date(?,'yyyy-mm-dd hh24:mi:ss') and "
				+ "to_date(?,'yyyy-mm-dd hh24:mi:ss') order by timestamp";
		int allNum = 0;
		int unusedNum = 0;
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, start);
			pstm.setString(2, end);
			ResultSet rs = pstm.executeQuery();
			logger.info("==== select has over, using time = "
					+ Utility.intervalTime(date1, new Date()) + " s ");
			while (rs.next()) {
				allNum++;
				String tmsi = rs.getString(1);
				Date timestamp = rs.getTimestamp(2);
				int lac = rs.getInt(3);
				int cellid = rs.getInt(4);
				int eventid = rs.getInt(5);
				int id = rs.getInt(6);
				// not to check now , all id has been used;
				// filter some unused data ：cellid = unusedId;
				// boolean unused = false;
				// for (int i = 0; i < Configuration.unused.length; i++) {
				// if (cellid == Configuration.unused[i]) {
				// unused = true;
				// break;
				// }
				// }
				// if (unused) {
				// unusedNum++;
				// continue;
				// }

				// because the data number is usually very large, so we can't do
				// the duplicate checking, just add directly;
				UserData ud = new UserData(tmsi, timestamp, lac, cellid,
						eventid, id);
				userDatas.add(ud);
			}
			rs.close();
			pstm.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("==== traverse data structure has over : all-> " + allNum
				+ " , unused->" + unusedNum + " , remaining -> "
				+ (allNum - unusedNum) + ", time -> "
				+ Utility.intervalTime(date1, new Date()) + " s ");
		return userDatas;
	}

	/**
	 * @Title: insertStationSpeeds
	 * @Description: insert station speeds into database;
	 * @param ss
	 * @param timeStamp
	 * @param direction
	 */
	public void insertStationSpeeds(List<StationSegment> ss, String timeStamp) {
		// before this insertion, we should check whether the data of same time
		// has ever been inserted;
		// String sql1 =
		// "select id from hw_station_segment_speed where time = ? "
		// + "and direction = ? and rowNum <=1";
		// try {
		// pstm = conn.prepareStatement(sql1);
		// pstm.setTimestamp(1, Timestamp.valueOf(timeStamp.replace('/', '-')));
		// pstm.setInt(2, direction);
		// ResultSet rs = pstm.executeQuery();
		// // if the result is not null, then do not insert again;
		// if (rs.next()) {
		// logger.warning(" ==== The data have already been inserted into database!!");
		// return;
		// } else {
		// rs.close();
		// }
		// } catch (SQLException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		String sql = "insert into hw_station_segment_speed values (?,?,?,?,?,?,?)";
		try {
			pstm = conn.prepareStatement(sql);
			int count = 0;
			for (int i = 0; i < ss.size(); i++) {
				StationSegment rs = ss.get(i);
				pstm.setInt(1, rs.id);
				pstm.setTimestamp(2,
						Timestamp.valueOf(timeStamp.replace('/', '-')));
				pstm.setInt(3, rs.getMaxSpeed());
				pstm.setInt(4, rs.getMinSpeed());
				pstm.setInt(5, rs.getAvgSpeed());
				pstm.setInt(6, rs.getFilterAvgSpeed());
				pstm.setInt(7, rs.getRealNum());
				pstm.execute();
				count++;
			}
			logger.info(" ==== insert StationSpeed into databases, count = "
					+ count);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.info(" ==== duplicated ! the data of time = " + timeStamp
					+ " has been "
					+ "insert into the hw_station_segment_speed !");
		}
	}

	/**
	 * @Title: insertNodeSpeeds
	 * @Description: insert node speeds into database;
	 * @param ns
	 * @param timeStamp
	 */
	public void insertNodeSpeeds(List<NodeSegment> nss, String timeStamp) {
		// before this insertion, we should check whether the data of same time
		// has ever been inserted;
		// String sql1 =
		// "select id from hw_road_node_segment_speed where time = ? "
		// + "and id = ? and rowNum<=1";
		// try {
		// pstm = conn.prepareStatement(sql1);
		// // id and time identify one record;
		// pstm.setTimestamp(1, Timestamp.valueOf(timeStamp.replace('/', '-')));
		// pstm.setInt(2, rss.get(10).id); // use a random id;
		// ResultSet rs = pstm.executeQuery();
		// // if the result is not null, then do not insert again;
		// if (rs.next()) {
		// logger.warning(" ==== The data have already been inserted into database!!");
		// return;
		// } else {
		// rs.close();
		// }
		// } catch (SQLException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		String sql = "insert into hw_road_node_segment_speed values (?,?,?,?,?,?)";
		try {
			pstm = conn.prepareStatement(sql);
			int count = 0;
			for (int i = 0; i < nss.size(); i++) {
				NodeSegment ns = nss.get(i);
				pstm.setInt(1, ns.getId());
				pstm.setTimestamp(2,
						Timestamp.valueOf(timeStamp.replace('/', '-')));
				pstm.setInt(3, ns.getMaxSpeed());
				pstm.setInt(4, ns.getMinSpeed());
				pstm.setInt(5, ns.getAvgSpeed());
				pstm.setInt(6, ns.getSpeedNum());
				pstm.execute();
				count++;
			}
			pstm.close();
			logger.info(" ==== insert NodeSegmentSpeed into databases, count = "
					+ count);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.info(" ==== duplicated , the data of time = " + timeStamp
					+ " has been " + "insert into the hw_node_segment_speed !");
		}
	}

	/**
	 * @Title: selectUserData
	 * @Description: 从数据库中获取数据
	 * @param start
	 * @param end
	 * @return
	 */
	public ArrayList<UserData> selectUserData(String start, String end) {
		// TODO Auto-generated method stub
		ArrayList<UserData> userDatas = new ArrayList<>();
		String sql = "select * from user_data where timestamp >= ? and timestamp < ?";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, start);
			pstm.setString(2, end);

			ResultSet rs = pstm.executeQuery();
			int allNum = 0;
			int filterNum = 0;
			int unusedNum = 0;

			while (rs.next()) {
				allNum++;
				String tmsi = rs.getString(1);
				Date timestamp = rs.getTimestamp(2);
				int lac = rs.getInt(3);
				int cellid = rs.getInt(4);
				int eventid = rs.getInt(5);
				int id = rs.getInt(6);
				// 过滤掉某些有问题数据：cellid= unusedId;
				boolean unused = false;
				for (int i = 0; i < Configuration.unusedId.length; i++) {
					if (cellid == Configuration.unusedId[i]) {
						unused = true;
						break;
					}
				}
				if (unused) {
					unusedNum++;
					continue;
				}
				// 过滤掉重复的数据
				UserData ud = new UserData(tmsi, timestamp, lac, cellid,
						eventid, id);
				int index = userDatas.indexOf(ud);
				if (index >= 0) {
					filterNum++;
					if (userDatas.get(index).getTimestamp().before(timestamp))
						userDatas.set(index, ud);
				} else {
					userDatas.add(ud);
				}
			}
			Logger logger = DataLogger.getLogger();
			logger.info("select from database : all-> " + allNum + ", same-> "
					+ filterNum + " , unused->" + unusedNum
					+ " , remaining -> " + (allNum - filterNum - unusedNum));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userDatas;
	}

	public int checkRequest() {
		int flag = 0;
		String sql = "select to_char(startTime,'yyyy-mm-dd hh24:mi:ss'),"
				+ "to_char(endTime,'yyyy-mm-dd hh24:mi:ss'),"
				+ "to_char(setTime,'yyyy-mm-dd hh24:mi:ss'), interval "
				+ " from hw_para_time_set";
		try {
			// count the record number,if it > 1, then may be something error;
			int count = 0;
			pstm = conn.prepareStatement(sql);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				count++;
				String setTime = rs.getString(3);
				// if setTime is not changed, that means there is no new
				// request;
				if (setTime == null)
					continue;
				if (setTime.equals(Configuration.setTime)) {
					logger.info("==== set time is " + setTime
							+ " , equals configuration , count = " + count);
					break;
				}
				// if setTime is changed, then update the related variables;
				logger.info("==== set time has been updated, begin to compute, count = "
						+ count);
				Configuration.setTime = setTime;
				Configuration.startTime = rs.getString(1);
				Configuration.endTime = rs.getString(2);
				Configuration.rate = rs.getInt(4);
				flag = 1;
			}
			rs.close();
			pstm.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return flag;
	}

	public void updateTime(String time) {
		String sql = "update hw_para_time_set set updatetime = ? where settime = ?";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setTimestamp(1, Timestamp.valueOf(time.replace('/', '-')));
			pstm.setTimestamp(2,
					Timestamp.valueOf(Configuration.setTime.replace('/', '-')));
			pstm.execute();
			pstm.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void insertTime(String startTime, String endTime, int rate,
			String setTime, String updateTime) {
		String sql = "insert into hw_para_time_set values(?,?,?,?,?)";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setTimestamp(1, Timestamp.valueOf(startTime.replace('/', '-')));
			pstm.setTimestamp(2, Timestamp.valueOf(endTime.replace('/', '-')));
			pstm.setTimestamp(4, Timestamp.valueOf(setTime.replace('/', '-')));
			pstm.setTimestamp(5,
					Timestamp.valueOf(updateTime.replace('/', '-')));
			pstm.setInt(3, 1);
			pstm.execute();
			pstm.close();
			System.out.println("Time has been initialized ! ");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// new DBServiceForOracle().selectHistoryUserData("2015/02/04 14:15:00",
		// "2015/02/04 15:00:00");
		// new DBServiceForOracle().updateTime("2015/03/15 15:00:24");
		new DBServiceForOracle().insertTime("2015/02/04 15:00:00",
				"2015/02/04 20:00:00", 1, "2015/03/17 10:00:00",
				"2015/03/17 09:00:00");
	}
}
