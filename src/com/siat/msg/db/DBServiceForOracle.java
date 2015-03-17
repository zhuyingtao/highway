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

import com.siat.ds.NodeSegment;
import com.siat.ds.StationSegment;
import com.siat.msg.Configuration;
import com.siat.msg.UserData;
import com.siat.msg.util.DataLogger;

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

	Connection conn = null;
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
		String sql = "select * from hw_data_user where timestamp between ? and ?";
		// String sql = "select * from hw_data_user where timestamp >= "
		// + "to_date(?,'yyyy-mm-dd hh24:mi:ss.0') and timestamp < "
		// + "to_date(?,'yyyy-mm-dd hh24:mi:ss.0')";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setTimestamp(1, Timestamp.valueOf(start.replace('/', '-')));
			// pstm.setString(1, start);
			pstm.setTimestamp(2, Timestamp.valueOf(end.replace('/', '-')));
			// pstm.setString(2, end);
			ResultSet rs = pstm.executeQuery();
			int allNum = 0;
			int unusedNum = 0;
			Date date3 = new Date();
			logger.info("==== select has over, using time = "
					+ (date3.getTime() - date1.getTime()) / 1000);
			while (rs.next()) {
				allNum++;
				String tmsi = rs.getString(1);
				// Date timestamp = rs.getTimestamp(2);
				Date timestamp = null;
				int lac = rs.getInt(3);
				int cellid = rs.getInt(4);
				int eventid = rs.getInt(5);
				int id = rs.getInt(6);
				// filter some unused data ：cellid = unusedId;
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
				// can't do the duplicate checking, just add directly;
				UserData ud = new UserData(tmsi, timestamp, lac, cellid,
						eventid, id);
				userDatas.add(ud);

			}
			Date date2 = new Date();

			logger.info("==== bulid data structure has over : all-> " + allNum
					+ " , unused->" + unusedNum + " , remaining -> "
					+ (allNum - unusedNum) + ", time -> "
					+ ((date2.getTime() - date1.getTime())) / 1000);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userDatas;
	}

	/**
	 * @Title: insertStationSpeeds
	 * @Description: insert station speeds into database;
	 * @param ss
	 * @param timeStamp
	 * @param direction
	 */
	public void insertStationSpeeds(ArrayList<StationSegment> ss,
			String timeStamp, int direction) {
		// before this insertion, we should check whether the data of same time
		// has ever been inserted;
		String sql1 = "select id from hw_station_segment_speed where time = ? limit 1";
		try {
			pstm = conn.prepareStatement(sql1);
			pstm.setTimestamp(1, Timestamp.valueOf(timeStamp.replace('/', '-')));
			ResultSet rs = pstm.executeQuery();
			// if the result is not null, then do not insert again;
			if (rs.first()) {
				logger.warning(" ==== The data have already been inserted into database!!");
				return;
			} else {
				rs.close();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String sql = "insert into hw_station_segment_speed values (?,?,?,?,?,?,?,?,?)";
		try {
			pstm = conn.prepareStatement(sql);
			int count = 0;
			for (int i = 0; i < ss.size(); i++) {
				StationSegment rs = ss.get(i);
				pstm.setInt(1, rs.id);
				pstm.setString(2, rs.getStarts());
				pstm.setTimestamp(3,
						Timestamp.valueOf(timeStamp.replace('/', '-')));
				pstm.setInt(4, direction);
				pstm.setInt(5, rs.getMaxSpeed());
				pstm.setInt(6, rs.getMinSpeed());
				pstm.setInt(7, rs.getAvgSpeed());
				pstm.setInt(8, rs.getFilterAvgSpeed());
				pstm.setInt(9, rs.getRealNum());
				pstm.execute();
				count++;
			}
			logger.info(" ==== insert StationSpeed into databases, count = "
					+ count);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @Title: insertNodeSpeeds
	 * @Description: insert node speeds into database;
	 * @param rss
	 * @param timeStamp
	 */
	public void insertNodeSpeeds(ArrayList<NodeSegment> rss, String timeStamp) {
		// before this insertion, we should check whether the data of same time
		// has ever been inserted;
		String sql1 = "select id from hw_road_node_segment_speed where time = ? limit 1";
		try {
			pstm = conn.prepareStatement(sql1);
			pstm.setTimestamp(1, Timestamp.valueOf(timeStamp.replace('/', '-')));
			ResultSet rs = pstm.executeQuery();
			// if the result is not null, then do not insert again;
			if (rs.first()) {
				logger.warning(" ==== The data have already been inserted into database!!");
				return;
			} else {
				rs.close();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String sql = "insert into hw_road_node_segment_speed values (?,?,?,?,?,?,?,?)";
		try {
			pstm = conn.prepareStatement(sql);
			int count = 0;
			for (int i = 0; i < rss.size(); i++) {
				NodeSegment rs = rss.get(i);
				pstm.setInt(1, rs.getId());
				pstm.setString(2, rs.getSectionName());
				pstm.setTimestamp(3,
						Timestamp.valueOf(timeStamp.replace('/', '-')));
				pstm.setInt(4, rs.getDirection());
				pstm.setInt(5, rs.getMaxSpeed());
				pstm.setInt(6, rs.getMinSpeed());
				pstm.setInt(7, rs.getAvgSpeed());
				pstm.setInt(8, rs.getSpeedNum());
				pstm.execute();
				count++;
			}
			logger.info(" ==== insert NodeSpeed into databases, count = "
					+ count);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		String sql = "select * from hw_para_time_set";
		try {
			pstm = conn.prepareStatement(sql);
			ResultSet rs = pstm.executeQuery();
			int count = 0;
			while (rs.next()) {
				count++;
				String setTime = rs.getString(4);
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
				Configuration.rate = rs.getInt(3);
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
